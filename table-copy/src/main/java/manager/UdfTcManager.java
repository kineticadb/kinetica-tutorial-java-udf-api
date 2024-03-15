package com.kinetica;

import com.gpudb.*;
import com.gpudb.protocol.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;

public class UdfTcManager
{
    static Integer MAX_RECORDS = 10000;

    static String CSV_FILE = "rank_tom.csv";
    static String PROC_NAME = "UdfTcJavaProc";
    static String PROC_PATH = "com.kinetica." + PROC_NAME;
    static String PROC_JAR_FILE = "kinetica-udf-table-copy-proc-7.2.0.jar";
    static String PROC_API_ROOT = "/opt/gpudb/udf/api/java/proc-api/";
    static String PROC_API_7200_FILE = PROC_API_ROOT + "kinetica-proc-api-7.2.0.0-jar-with-dependencies.jar";
    static String PROC_API_FILE = PROC_API_ROOT + "kinetica-proc-api.jar";

    static Random rand = new Random();

    public static class InTable extends RecordObject
    {
        @RecordObject.Column(order=0, properties = {"int16", "primary_key"})
        public Integer id;
        @RecordObject.Column(order=1)
        public Float x;
        @RecordObject.Column(order=2)
        public Float y;

        public InTable() {}
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
    }

    public static void main(String[] args) throws Exception
    {
    	if (args.length < 4)
    	{
    		usage();
    		System.exit(1);
    	}

    	String task = args[0];
        String url = args[1];
        String username = args[2];
        String password = args[3];
        String schema = (args.length > 4) ? args[4] : null;

        String inputTable = ((schema == null) ? "" : schema + ".") + "udf_tc_java_in_table";
        String outputTable = ((schema == null) ? "" : schema + ".") + "udf_tc_java_out_table";

        GPUdbBase.Options options = new GPUdbBase.Options();
        options.setUsername(username);
        options.setPassword(password);
        GPUdb kinetica = new GPUdb(url, options);

        switch (task)
        {
            case "init":
                init(kinetica, schema, inputTable, outputTable);
                break;
            case "exec":
                exec(kinetica, inputTable, outputTable);
                break;
            default:
                usage();
                System.exit(2);
        }
    }

    public static void usage()
    {
    	System.err.println("Usage:  java -jar kinetica-udf-table-copy-manager-jar-with-dependencies.jar <task> <url> <user> <pass> [<schema>]");
    	System.err.println("Where:");
    	System.err.println("        task   = UDF management task to run; one of:");
    	System.err.println("                 init - initialize the UDF environment");
    	System.err.println("                 exec - run the UDF");
    	System.err.println("        url    = Kinetica connection URL");
    	System.err.println("        user   = Kinetica connection username");
    	System.err.println("        pass   = Kinetica connection password");
    	System.err.println("        schema = Optional schema to put database objects into; requires admin access");
    }

    public static void init(GPUdb kinetica, String schema, String inputTable, String outputTable) throws GPUdbException
    {
        System.out.println();
        System.out.println("JAVA UDF TABLE COPY INITIALIZATION");
        System.out.println("==================================");
        System.out.println();

        if (schema != null)
        {
            // Create the Java UDF tutorial schema if it doesn't exist
            Map<String, String> createSchemaOptions = GPUdb.options(
                CreateSchemaRequest.Options.NO_ERROR_IF_EXISTS, CreateSchemaRequest.Options.TRUE
            );
            kinetica.createSchema(schema, createSchemaOptions);
        }

        // Create input data table
        kinetica.clearTable(inputTable, null, GPUdb.options("no_error_if_not_exists", "true"));
        String inTableId = RecordObject.createType(InTable.class, kinetica);
        kinetica.createTable(inputTable, inTableId, null);
        System.out.println("Input table successfully created:");
        ShowTableResponse showInputTable = kinetica.showTable(inputTable, null);
        System.out.println(showInputTable.getTableNames().get(0) + " with type id " + showInputTable.getTypeIds().get(0));

        // Insert randomly-generated data into the input table
        Map<String, Double> idOpts = new HashMap<String, Double>() {{put("min", 0.0);put("interval", 1.0);}};
        Map<String, Map<String, Double>> options = new HashMap<String, Map<String, Double>>() {{put("id", idOpts);}};
        kinetica.insertRecordsRandom(inputTable, MAX_RECORDS, options);
        GetRecordsResponse getRecordsResponse = kinetica.getRecords(inputTable, 0, GPUdbBase.END_OF_SET, null);
        System.out.println("Number of records inserted into the input table: " + getRecordsResponse.getTotalNumberOfRecords());
        System.out.println();

        // Create output data table
        kinetica.clearTable(outputTable, null, GPUdb.options("no_error_if_not_exists", "true"));
        String outTableId = RecordObject.createType(OutTable.class, kinetica);
        kinetica.createTable(outputTable, outTableId, null);
        System.out.println("Output table successfully created:");
        ShowTableResponse showOutputTable = kinetica.showTable(outputTable, null);
        System.out.println(showOutputTable.get(0) + " with type id " + showOutputTable.get(3));
    }

    public static void exec(GPUdb kinetica, String inputTable, String outputTable) throws Exception
    {

        System.out.println();
        System.out.println("JAVA UDF TABLE COPY EXECUTION");
        System.out.println("=============================");
        System.out.println();

        System.out.println("Reading in the proc JAR and 'rank_tom.csv' file as bytes...");
        System.out.println();

        // For the given files, add them to a byte array and put them in a
        // map
        Map<String, ByteBuffer> filesMap = new HashMap<>();
        for (String fileName : Arrays.asList(CSV_FILE, PROC_JAR_FILE))
        {
            byte [] fileAsBytes = Files.readAllBytes(new File(fileName).toPath());
            ByteBuffer fileByteBuffer = ByteBuffer.wrap(fileAsBytes);
            filesMap.put(fileName, fileByteBuffer);
        }

        if (kinetica.hasProc(PROC_NAME, null).getProcExists())
            kinetica.deleteProc(PROC_NAME, null);

        // Create the proc, covering the bases for which UDF API might be
        //   installed on the server; if known, just include it in the classpath
        System.out.println("Registering distributed proc...");
        CreateProcResponse createProcResponse = kinetica.createProc(
                PROC_NAME,
                "distributed",
                filesMap,
                "java",
                Arrays.asList(
                        "-cp",
                        PROC_JAR_FILE + ":" +
                            PROC_API_7200_FILE + ":" +
                            PROC_API_FILE,
                        PROC_PATH
                ),
                null
        );
        System.out.println("Proc created successfully:");
        System.out.println(createProcResponse);
        System.out.println();

        // Execute the proc
        System.out.println("Executing proc...");
        ExecuteProcResponse executeProcResponse = kinetica.executeProc(
                PROC_NAME,
                null,
                null,
                Collections.singletonList(inputTable),
                null,
                Collections.singletonList(outputTable),
                null
        );
        System.out.println("Proc executed successfully:");
        System.out.println(executeProcResponse);
        System.out.println("Check the system log or 'gpudb.log' for execution information");
        System.out.println();
    }
}
