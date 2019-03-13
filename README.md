# Kinetica Java UDF API Tutorial #

This project contains the **7.0** version of the **Java UDF API Tutorial**.

This guide exists on-line at:  [Kinetica Java UDF API Tutorial](http://www.kinetica.com/docs/udf/java/tutorial.html)

More information can be found at:  [Kinetica Documentation](http://www.kinetica.com/docs/index.html)

-----

The following guide provides step-by-step instructions to get started writing
and running UDFs in *Java*. This particular example is a simple distributed
*UDF* that copies data from one table to another using a CSV configuration file
to determine from which processing node to copy data. Note that only copying
data from some processing nodes typically would not have "real" applications and
this exercise is purely to demonstrate the many facets of the *UDF* API.

## Contents

* [References](#references)
* [Prerequisites](#prerequisites)
* [API Download and Installation](#api-download-and-installation)
* [Development](#development)
* [Deployment](#deployment)
* [Execution Detail](#execution-detail)


## References

* [Java UDF Reference](https://www.kinetica.com/docs/udf/java/writing.html)
  -- detailed description of the entire *UDF* API
* [Running UDFs](https://www.kinetica.com/docs/udf/java/running.html)
  -- detailed description on running *Java* UDFs
* [Example UDFs](https://www.kinetica.com/docs/udf/java/examples.html)
  -- example *UDFs* written in *Java*


## Prerequisites

The general prerequisites for using *UDFs* in *Kinetica* can be found on
the [UDF Implementation](https://www.kinetica.com/docs/udf/index.html) page.


### Data Files

There are four files associated with the *Java UDF* tutorial:

* a management file, ``UdfTcManager.java``, that creates the input and output
  tables and creates the proc and executes it
* a UDF, ``UdfTcJavaProc.java``, that contains a table copying example
* a CSV input file, ``rank_tom.csv``
* an [Avro-shaded native Java API JAR file](http://files.kinetica.com/nexus/content/repositories/releases/com/gpudb/gpudb-api/7.0.0.0/gpudb-api-7.0.0.0-avroshaded.jar)
  that is used to compile the *UDF* management file


### Software

* *Java* 1.7 (or greater)

**NOTE:** The location of ``java`` should be placed in the ``PATH`` environment
   variable and the ``JAVA_HOME`` should be set. If it is not, you'll need to   
   use the full path to ``java`` executables in the relevant instructions below.

* *Maven*
* *Python* 2.7 (or greater) or ``pip``

**NOTE:** The locations of ``python`` and ``pip`` should be placed in the
   ``PATH`` environment variable. If they are not, you'll need to use the full
   path to the ``python`` and ``pip`` executables in the relevant instructions
   below. Also, administrative access will most likely be required when
   installing the *Python* packages.


## API Download and Installation

The *Java UDF* tutorial requires local access to the *Java UDF* tutorial
repository, native *Java* API JAR, and the *Java UDF* API. The native *Python*
API must also be installed to use the *UDF* simulator (details found in
[Development](#development)).

In the desired directory, run the following but be sure to replace
``<kinetica-version>`` with the name of the installed Kinetica version, e.g.,
``v7.0``:

     git clone -b release/<kinetica-version> --single-branch https://github.com/kineticadb/kinetica-tutorial-java-udf-api.git

In the same directory, run the following but be sure to replace
``<kinetica-version>`` with the name of the installed Kinetica version, e.g.,
``v7.0``:

     git clone -b release/<kinetica-version> --single-branch https://github.com/kineticadb/kinetica-udf-api-java.git

In the same directory, run the following but be sure to replace
``<kinetica-version>`` with the name of the installed Kinetica version, e.g.,
``v7.0``:

     git clone -b release/<kinetica-version> --single-branch https://github.com/kineticadb/kinetica-api-python.git

In the same directory, run the following:

     wget http://files.kinetica.com/nexus/content/repositories/releases/com/gpudb/gpudb-api/7.0.0.0/gpudb-api-7.0.0.0-avroshaded.jar

Change directory into the newly downloaded native *Python* API repository:

    cd kinetica-api-python/

In the root directory of the repository, install the Kinetica API:

    sudo python setup.py install

Change directory into the *Java UDF* API directory:

    cd ../kinetica-udf-api-java/proc-api/

Install the *Java UDF* API:

    mvn clean package
    mvn install

Change directory into the *Java UDF* tutorial directory:

    cd ../kinetica-tutorial-java-udf-api


## Development

Refer to the [Java UDF API Reference](https://www.kinetica.com/docs/udf/java/writing.html)
page to begin writing your own *UDF(s)*, or use the *UDF* already provided with
the *Java UDF* tutorial repository. The steps below outline using the
[UDF Simulator](https://www.kinetica.com/docs/udf/simulating_udfs.html) with
the *UDF* included with the *Java UDF* tutorial repository. The *UDF* simulator
simulates the mechanics of ``executeProc()`` without actually calling it in the
database; this is useful for developing *UDFs* piece-by-piece, so you can test
incrementally without any database memory ramifications.

Compile the Proc file and create a JAR:

    javac -cp ../kinetica-udf-api-java/proc-api/target/kinetica-proc-api-1.0-jar-with-dependencies.jar UdfTcJavaProc.java
    jar -cvf UdfTcJavaProc.jar UdfTcJavaProc*.class

Compile the Manager file and create a JAR:

    javac -cp ../gpudb-api-7.0.0.0-avroshaded.jar UdfTcManager.java
    jar -cvf UdfTcManager.jar UdfTcManager*.class

Run the *UDF* manager JAR with the ``init`` option, specifying the database
host and optional port (if non-default):

    java -cp '../gpudb-api-7.0.0.0-avroshaded.jar:UdfTcManager.jar' UdfTcManager "init" [<kinetica-host> [<kinetica-port>]]

In the native *Python* API directory, run the *UDF* simulator in ``execute``
mode with the following options to simulate running the *UDF*, where ``-i``
is the UDF input table, ``-o`` is the UDF output table, and ``-K`` is the
*Kinetica* URL (using the appropriate values for your environment).
Username (``-U``) & password (``-P``) can be specified, if your instance
requires authentication:

     python ../kinetica-api-python/examples/udfsim.py execute -d \
         -i udf_tc_java_in_table -o udf_tc_java_out_table \
         -K http://<kinetica-host>:<kinetica-port> \
         [-U <kinetica-user> -P <kinetica-pass>]

For instance:

     python ../kinetica-api-python/examples/udfsim.py execute -d \
         -i udf_tc_java_in_table -o udf_tc_java_out_table \
         -K http://127.0.0.1:9191 \
         -U admin -P admin123

Copy & execute the ``export`` command output by the previous command; this will
prepare the execution environment for simulating the UDF:

     export KINETICA_PCF=/tmp/udf-sim-control-files/kinetica-udf-sim-icf-xMGW32

**IMPORTANT:**  The ``export`` command shown above is an *example* of what the
      ``udfsim.py`` script will output--it should **not** be copied to the
      terminal in which this example is being run.  Make sure to copy & execute
      the **actual** command output by ``udfsim.py`` in the previous step.

Run the *UDF*:

     java -cp '../kinetica-udf-api-java/proc-api/target/kinetica-proc-api-1.0-jar-with-dependencies.jar:UdfTcJavaProc.jar' UdfTcJavaProc

Run the *UDF Simulator* in ``output`` mode to output the results to
*Kinetica* (use the dry run flag ``-d`` to avoid writing to *Kinetica*),
ensuring you replace the *Kinetica* URL and port with the appropriate values.
The ``results`` map will be returned (even if there's nothing in it) as well
as the amount of records that were (or will be in the case of a dry run)
added to the given output table:

     python ../kinetica-api-python/examples/udfsim.py output \
         -K http://<kinetica-host>:<kinetica-port> \
         [-U <kinetica-user> -P <kinetica-pass>]

For instance:

     python ../kinetica-api-python/examples/udfsim.py output \
         -K http://127.0.0.1:9191 \
         -U admin -P admin123

This should output the following:

     No results
     Output:

     udf_tc_java_out_table: 10000 records

Clean the control files output by the *UDF* simulator:

     python ../kinetica-api-python/examples/udfsim.py clean

**IMPORTANT:** The ``clean`` command is only necessary if data was output to
      *Kinetica*; otherwise, the *UDF* simulator can be re-run as many times as
      desired without having to clean the output files and enter another export
      command.


## Deployment

If satisfied after testing your *UDF* with the *UDF* simulator, the *UDF* can
be created and executed using the official *UDF* endpoints: ``/create/proc``
and ``/execute/proc`` (respectively).

Optionally, run the *UDF* manager JAR with the ``init`` option to reset the
example tables:

    java -cp '../gpudb-api-7.0.0.0-avroshaded.jar:UdfTcManager.jar' UdfTcManager "init" [<kinetica-host> [<kinetica-port>]]

Run the *UDF* manager JAR with the ``exec`` option:

    java -cp '../gpudb-api-7.0.0.0-avroshaded.jar:UdfTcManager.jar' UdfTcManager "exec" [<kinetica-host> [<kinetica-port>]]


## Execution Detail

As mentioned previously, this section details a simple distributed *UDF* that
copies data from one table to another. While the table copy *UDF* can run
against multiple tables, the example run will use a single table,
``udf_tc_java_in_table``, as input and a similar table,
``udf_tc_java_out_table``, for output.

The input table will contain one *int16* column (``id``) and two *float*
columns (``x`` and ``y``). The ``id`` column will be an ordered integer field,
with the first row containing ``1``, the second row containing ``2``, etc. Both
*float* columns will contain 10,000 pairs of randomly-generated numbers:

  +------+-----------+-----------+
  | id   | x         | y         |
  +======+===========+===========+
  | 1    | 2.57434   | -3.357401 |
  +------+-----------+-----------+
  | 2    | 0.0996761 | 5.375546  |
  +------+-----------+-----------+
  | ...  | ...       | ...       |
  +------+-----------+-----------+

The output table will also contain one *int16* column (``id``) and two *float*
columns (``a`` and ``b``). No data is inserted:

  +------+-----------+-----------+
  | id   | a         | b         |
  +======+===========+===========+
  |      |           |           |
  +------+-----------+-----------+

The *UDF* will first read from a given CSV file to determine from which
processing node container and processing node to copy data:

    rank_num,tom_num
    1,0
    2,0

The ``tom_num`` column values refer to processing nodes that contains some of
the many shards of data inside the database. The ``rank_num`` column values
refer to processing node containers that hold some of the processing nodes for
the database. For example, the given CSV file determines that the data from
``udf_tc_py_in_table`` on processing node container ``1``, processing node ``0``
and processing node container ``2``, processing node ``0`` will be copied to
``udf_tc_py_out_table``.

Once the *UDF* is executed, a *UDF* instance (OS process) is spun up for each
processing node to execute the given code against its assigned processing node.
The *UDF* then determines if the processing node container/processing node pair
it's currently running on matches one of the pairs of values in the CSV file. If
there is a match, the *UDF* will loop through the input tables, match the output
tables' size to the input tables', and copy the appropriate data from the input
tables to the output tables. If there isn't a match, the code will complete.


### Initialization (UdfTcManager.java init mode)

The *init* mode calls the ``init()`` method of the ``UdfTcManager.java`` file.
This method will create an input type and table for the *UDF* to copy data from
and an output type and table to copy data to. Sample data will also be generated
and placed in the input table.

To create tables using the *Java* API, a [type](https://www.kinetica.com/docs/concepts/types.html)
needs to be defined in the system first.  The type is a class, extended from
[RecordObject](https://www.kinetica.com/docs/api/java/com/gpudb/RecordObject.html),
using annotations to describe which class instance variables are fields (i.e.
columns), what type they are, and any special handling they should receive.  
Each field consists of a name and a data type:

```java
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
```

To interact with *Kinetica*, you must first instantiate an object of the
``GPUdb`` class while providing the connection URL, including the host and port
of the database server:

```java
GPUdb hDb = new GPUdb("http://" + DATABASE_HOST + ":" + DATABASE_PORT);
```

The ``InTable`` type and table are created, but the table is removed first if it
already exists. Then the table creation is verified using ``showTable()``:

```java
hDb.clearTable(INPUT_TABLE, null, GPUdb.options("no_error_if_not_exists", "true"));
String inTableId = RecordObject.createType(InTable.class, hDb);
hDb.createTable(INPUT_TABLE, inTableId, null);
System.out.println("Input table successfully created:");
ShowTableResponse showInputTable = hDb.showTable(INPUT_TABLE, null);
System.out.println(showInputTable.getTableNames().get(0) + "with type id " + showInputTable.getTypeIds().get(0));
```

Next, sample data is generated and inserted into the new input table:

```java
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
```

Lastly, an ``OutTable`` type and table are created, but the table is removed
first if it already exists. Then the table creation is verified using
``showTable()``:

```java
hDb.clearTable(OUTPUT_TABLE, null, GPUdb.options("no_error_if_not_exists", "true"));
String outTableId = RecordObject.createType(OutTable.class, hDb);
hDb.createTable(OUTPUT_TABLE, outTableId, null);
System.out.println("Output table successfully created:");
ShowTableResponse showOutputTable = hDb.showTable(OUTPUT_TABLE, null);
System.out.println(showOutputTable.get(0) + " with type id " + showOutputTable.get(3));
```


### UDF (UdfTcJavaProc.java)

First, instantiate a handle to the ``ProcData`` class:

```java
ProcData procData = ProcData.get();
```

Initialize a boolean that will be switched to ``true`` if a rank/TOM pair-CSV
file value match is found:

```java
boolean foundMatch = false;
```

Retrieve each pair of uniquely-identifying rank/TOM pairs from the CSV file
containing the list of processing nodes whose data should be copied by the UDF:

```java
final String procRankNum = procData.getRequestInfo().get("rank_number");
final String procTomNum = procData.getRequestInfo().get("tom_number");
```

Then, the CSV file mentioned in [Data Files](#data-files) is read (skipping the
header):

```java
Scanner scanner = new Scanner(new File("rank_tom.csv"));
scanner.nextLine();
while (scanner.hasNextLine())
```

Compare the rank and TOM of the current UDF instance's processing node to each
rank/TOM pair in the file to determine if the current UDF instance should copy
the data on its corresponding processing node:

```java
String[] row = scanner.nextLine().split(",", -1);
final String fileRankNum = row[0];
final String fileTomNum = row[1];

if (procRankNum.equals(fileRankNum) && procTomNum.equals(fileTomNum))
```

For each input and output table found in the ``inputData`` and ``outputData``
objects (respectively), set the output tables' size to the input tables' size.
This will allocate enough memory to copy all input records to the output
table:

```java
ProcData.InputTable inputTable = procData.getInputData().getTable(i);
ProcData.OutputTable outputTable = procData.getOutputData().getTable(i);
outputTable.setSize(inputTable.getSize());
```

For each input column in the input table(s) and for each output column in the
output table(s), copy the input columns' values to the output columns:

```java
for (int j = 0; j < inputTable.getColumnCount(); j++)
{
    ProcData.InputColumn inputColumn = inputTable.getColumn(j);
    ProcData.OutputColumn outputColumn = outputTable.getColumn(j);

    for (long k = 0; k < inputTable.getSize(); k++)
    {
        switch (inputColumn.getType())
        {
            case BYTES: outputColumn.appendVarBytes(inputColumn.getVarBytes(k)); break;
            case CHAR1: outputColumn.appendChar(inputColumn.getChar(k)); break;
            case CHAR2: outputColumn.appendChar(inputColumn.getChar(k)); break;
            case CHAR4: outputColumn.appendChar(inputColumn.getChar(k)); break;
            case CHAR8: outputColumn.appendChar(inputColumn.getChar(k)); break;
            case CHAR16: outputColumn.appendChar(inputColumn.getChar(k)); break;
            case CHAR32: outputColumn.appendChar(inputColumn.getChar(k)); break;
            case CHAR64: outputColumn.appendChar(inputColumn.getChar(k)); break;
            case CHAR128: outputColumn.appendChar(inputColumn.getChar(k)); break;
            case CHAR256: outputColumn.appendChar(inputColumn.getChar(k)); break;
            case DATE: outputColumn.appendCalendar(inputColumn.getCalendar(k)); break;
            case DATETIME: outputColumn.appendCalendar(inputColumn.getCalendar(k)); break;
            case DECIMAL: outputColumn.appendBigDecimal(inputColumn.getBigDecimal(k)); break;
            case DOUBLE: outputColumn.appendDouble(inputColumn.getDouble(k)); break;
            case FLOAT: outputColumn.appendFloat(inputColumn.getFloat(k)); break;
            case INT: outputColumn.appendInt(inputColumn.getInt(k)); break;
            case INT8: outputColumn.appendByte(inputColumn.getByte(k)); break;
            case INT16: outputColumn.appendShort(inputColumn.getShort(k)); break;
            case IPV4: outputColumn.appendInet4Address(inputColumn.getInet4Address(k)); break;
            case LONG: outputColumn.appendLong(inputColumn.getLong(k)); break;
            case STRING: outputColumn.appendVarString(inputColumn.getVarString(k)); break;
            case TIME: outputColumn.appendCalendar(inputColumn.getCalendar(k)); break;
            case TIMESTAMP: outputColumn.appendLong(inputColumn.getLong(k)); break;
            default:
                throw new RuntimeException();
        }
    }
}    
```

If no matches were found, finish processing:

```java
if (!foundMatch)
    System.out.println("No rank or tom matches");
```

Call ``complete()`` to tell *Kinetica* the proc code is finished:

```java
procData.complete();
```


### Execution  (UdfTcManager.java exec mode)

The *exec* mode calls the ``exec()`` method of the ``UdfTcManager.java`` file.
This method will read files in as bytes, create a proc, and upload the files to
the proc. The method will then execute the proc.

To interact with *Kinetica*, you must first instantiate an object of the
``GPUdb`` class while providing the connection URL, including the host and port
of the database server. Ensure the host address and port are correct for your
setup:

```java
GPUdb hDb = new GPUdb("http://" + DATABASE_HOST + ":" + DATABASE_PORT);
```

To upload the ``UdfTcManager.jar`` and ``rank_tom.csv`` files to *Kinetica*,
they will first need to be read in as bytes and added to a file data map:

```java
Map<String, ByteBuffer> filesMap = new HashMap<>();
for (String fileName : Arrays.asList(CSV_FILE_NAME, PROC_JAR_FILE))
{
    byte [] fileAsBytes = Files.readAllBytes(new File(fileName).toPath());
    ByteBuffer fileByteBuffer = ByteBuffer.wrap(fileAsBytes);
    filesMap.put(fileName, fileByteBuffer);
}
```

After the files are placed in a data map, the distributed ``UdfTcJavaProc``
proc can be created in *Kinetica* and the files can be associated with it:

```java
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
```

**NOTE:**
   The proc requires the proper ``command`` and ``args`` to be executed, in
   this case, the assembled command line would be:

     ``java -cp /opt/gpudb/udf/api/java/proc-api/kinetica-proc-api-1.0-jar-with-dependencies.jar:UdfTcJavaProc.jar UdfTcJavaProc``

Finally, after the proc is created, it can be executed. The input table
and output table created in the *Initialization* section are passed in here:

```java
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
```
