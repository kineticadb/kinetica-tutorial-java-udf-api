import com.gpudb.*;
import com.gpudb.protocol.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;

public class UdfTcManager
{
    static String DATABASE_HOST = "127.0.0.1";
    static String DATABASE_PORT = "9191";
    static String INPUT_TABLE = "udf_tc_java_in_table";
    static Integer MAX_RECORDS = 10000;
    static String OUTPUT_TABLE = "udf_tc_java_out_table";

    static String CSV_FILE_NAME = "rank_tom.csv";
    static String JAR_HOME = "/opt/gpudb/udf/api/java/proc-api/";
    static String JAR_FILE = "kinetica-proc-api-1.0-jar-with-dependencies.jar";
    static String PROC_NAME = "UdfTcJavaProc";

    static String PROC_JAR_FILE = PROC_NAME + ".jar";

    static String CLASS_PATH = JAR_HOME + JAR_FILE + ":" + PROC_JAR_FILE;

    static Random rand = new Random();

    public static class InTable extends RecordObject
    {
        @RecordObject.Column(order=0, properties = {"int16", "primary_key"})
        public Integer id;
        @RecordObject.Column(order=1)
        public Float x;
        @RecordObject.Column(order=1)
        public Float y;

        public InTable() {}

        public InTable(Integer id, Float x, Float y)
        {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    public static class OutTable extends RecordObject
    {
        @RecordObject.Column(order=0, properties = {"int16", "primary_key"})
        public Integer id;
        @RecordObject.Column(order=1)
        public Float a;
        @RecordObject.Column(order=2)
        public Float b;

        public OutTable() {}

        public OutTable(Integer id, Float a, Float b)
        {
            this.id = id;
            this.a = a;
            this.b = b;
        }
    }

    public static void main(String[] args) throws GPUdbException
    {
        if (args.length > 1)
            DATABASE_HOST = args[1];
        if (args.length > 2)
            DATABASE_PORT = args[2];

        String mode = args[0];

        switch (mode)
        {
            case "init":
                init();
                break;
            case "exec":
                exec();
                break;
            default:
                throw new RuntimeException();
        }
    }

    public static void init() throws GPUdbException
    {
        System.out.println();
        System.out.println("JAVA UDF TABLE COPY INITIALIZATION");
        System.out.println("==================================");
        System.out.println();

        // Connect to Kinetica
        GPUdb hDb = new GPUdb("http://" + DATABASE_HOST + ":" + DATABASE_PORT);

        // Create input data table
        hDb.clearTable(INPUT_TABLE, null, GPUdb.options("no_error_if_not_exists", "true"));
        String inTableId = RecordObject.createType(InTable.class, hDb);
        hDb.createTable(INPUT_TABLE, inTableId, null);
        System.out.println("Input table successfully created:");
        ShowTableResponse showInputTable = hDb.showTable(INPUT_TABLE, null);
        System.out.println(showInputTable.getTableNames().get(0) + "with type id " + showInputTable.getTypeIds().get(0));

        // Insert randomly-generated data into the input table
        ArrayList<InTable> allRecords = new ArrayList<>();
        for (int i = 0; i < MAX_RECORDS; i++) {
            InTable singleRecord = new InTable();
            singleRecord.id = i;
            singleRecord.x = (float) rand.nextGaussian() * 1 + 1;
            singleRecord.y = (float) rand.nextGaussian() * 1 + 2;
            allRecords.add(singleRecord);
        }
        hDb.insertRecords(INPUT_TABLE, allRecords, null);
        GetRecordsResponse getRecordsResponse = hDb.getRecords(INPUT_TABLE, 0, GPUdbBase.END_OF_SET, null);
        System.out.println("Number of records inserted into the input table: " + getRecordsResponse.getTotalNumberOfRecords());
        System.out.println();

        // Create output data table
        hDb.clearTable(OUTPUT_TABLE, null, GPUdb.options("no_error_if_not_exists", "true"));
        String outTableId = RecordObject.createType(OutTable.class, hDb);
        hDb.createTable(OUTPUT_TABLE, outTableId, null);
        System.out.println("Output table successfully created:");
        ShowTableResponse showOutputTable = hDb.showTable(OUTPUT_TABLE, null);
        System.out.println(showOutputTable.get(0) + " with type id " + showOutputTable.get(3));
    }

    public static void exec() throws GPUdbException
    {

        System.out.println();
        System.out.println("JAVA UDF TABLE COPY EXECUTION");
        System.out.println("=============================");
        System.out.println();

        System.out.println("Reading in the 'UdfTcJavaProc.java' and 'rank_tom.csv' file as bytes...");
        System.out.println();

        // Connect to Kinetica
        GPUdb hDb = new GPUdb("http://" + DATABASE_HOST + ":" + DATABASE_PORT);

        try
        {
            // For the given files, add them to a byte array and put them in a
            // map
            Map<String, ByteBuffer> filesMap = new HashMap<>();
            for (String fileName : Arrays.asList(CSV_FILE_NAME, PROC_JAR_FILE))
            {
                byte [] fileAsBytes = Files.readAllBytes(new File(fileName).toPath());
                ByteBuffer fileByteBuffer = ByteBuffer.wrap(fileAsBytes);
                filesMap.put(fileName, fileByteBuffer);
            }

            if (hDb.hasProc(PROC_NAME, null).getProcExists())
                hDb.deleteProc(PROC_NAME, null);

            // Create the proc
            System.out.println("Registering distributed proc...");
            CreateProcResponse createProcResponse = hDb.createProc(
                    PROC_NAME,
                    "distributed",
                    filesMap,
                    "java",
                    Arrays.asList("-cp", CLASS_PATH, PROC_NAME),
                    null
            );
            System.out.println("Proc created successfully:");
            System.out.println(createProcResponse);
            System.out.println();

            // Execute the proc
            System.out.println("Executing proc...");
            ExecuteProcResponse executeProcResponse = hDb.executeProc(
                    PROC_NAME,
                    null,
                    null,
                    Collections.singletonList(INPUT_TABLE),
                    null,
                    Collections.singletonList(OUTPUT_TABLE),
                    null
            );
            System.out.println("Proc executed successfully:");
            System.out.println(executeProcResponse);
            System.out.println("Check 'gpudb.log' or 'gpudb-proc.log' for execution information");
            System.out.println();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    } // end main
} // end class UdfTcManager