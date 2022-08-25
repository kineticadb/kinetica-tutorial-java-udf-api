<h3 align="center" style="margin:0px">
	<img width="200" src="https://2wz2rk1b7g6s3mm3mk3dj0lh-wpengine.netdna-ssl.com/wp-content/uploads/2018/08/kinetica_logo.svg" alt="Kinetica Logo"/>
</h3>
<h5 align="center" style="margin:0px">
	<a href="https://www.kinetica.com/">Website</a>
	|
	<a href="https://docs.kinetica.com/7.1/">Docs</a>
	|
	<a href="https://docs.kinetica.com/7.1/api/">API Docs</a>
	|
	<a href="https://join.slack.com/t/kinetica-community/shared_invite/zt-1bt9x3mvr-uMKrXlSDXfy3oU~sKi84qg">Community Slack</a>   
</h5>
<p align = "center">
	<img src="https://img.shields.io/badge/tested-%3E=v7.1.7-green"></img>
	<img src="https://img.shields.io/badge/time-15 mins-blue"></img>
</p>

# Kinetica Java UDF API Tutorial #

This project contains the **7.1** version of the **Java UDF API Tutorial**.

This guide exists on-line at:  [Kinetica Java UDF API Tutorial](https://docs.kinetica.com/7.1/guides/udf_java_guide/)

More information can be found at:  [Kinetica Documentation](https://docs.kinetica.com/7.1/)

-----

The following guide provides step-by-step instructions to get started writing
and running UDFs in Java. This example is a simple distributed UDF that copies
data from one table to another using a CSV configuration file to determine on
which processing node(s) data will be copied.

Standard (non-replicated) tables have their data distributed across all
processing nodes, while replicated tables have all of their data on every
processing node.  In this example, we'll use a standard table and copy only the
portions of its data that reside on the nodes named in the CSV file.

Note that only copying data from some processing nodes typically would not have
"real" applications and this exercise is purely to demonstrate the many facets
of the UDF API.

## Contents

* [References](#references)
* [Prerequisites](#prerequisites)
* [API Download and Installation](#api-download-and-installation)
* [Development](#development)
* [Deployment](#deployment)
* [UDF Detail](#udf-detail)


## References

* [Java UDF Reference](https://docs.kinetica.com/7.1/udf/java/writing/)
  -- detailed description of the entire UDF API
* [Running UDFs](https://docs.kinetica.com/7.1/udf/java/running/)
  -- detailed description on running Java UDFs
* [Example UDFs](https://docs.kinetica.com/7.1/udf/java/examples/)
  -- example UDFs written in Java


## Prerequisites

The general prerequisites for using UDFs in Kinetica can be found on the
[UDF Implementation](https://docs.kinetica.com/7.1/udf/) page.


### Program Files

There are six files associated with the Java UDF tutorial:

* A UDF management program,
  [UdfTcManager.java](table-copy/src/main/java/manager/UdfTcManager.java),
  written using the Java API, which creates the input & output tables, and
  creates the UDF and executes it.
* A UDF,
  [UdfTcJavaProc.java](table-copy/src/main/java/udf/UdfTcJavaProc.java),
  written using the Java UDF API, which contains a table copying example.
* A CSV input file,
  [rank_tom.csv](table-copy/rank_tom.csv), used to
  identify which processing nodes should copy data.
* Three Project Object Model (POM) files:

  * The main [pom.xml](table-copy/pom.xml) file
    contained at the top-level of the project that is used to compile the
    two module JARs (the UDF manager and the UDF)
  * The UDF manager [pom.xml](table-copy/manager/pom.xml) file
    contained within the [manager](manager) sub-directory that is used to
    compile the UDF manager JAR
  * The UDF [pom.xml](table-copy/udf/pom.xml) file
    contained within the [udf](udf) sub-directory that is used to
    compile the UDF JAR


### Software

* *Java* 1.7 (or greater)

  **NOTE:**
     The location of ``java`` should be placed in the ``PATH`` environment
     variable and ``JAVA_HOME`` should be set. If it is not, you'll need to use
     the full path to ``java`` executables in the relevant instructions below.

* *Maven*
* *Python* 2.7 (or greater) or ``pip``

  **NOTE:**
     The locations of ``python`` and ``pip`` should be placed in the ``PATH``
     environment variable. If they are not, you'll need to use the full path to
     the ``python`` and ``pip`` executables in the relevant instructions below.
     Also, administrative access will most likely be required when installing
     the *Python* packages.


## API Download and Installation

The Java UDF tutorial requires local access to the Java UDF API & tutorial
repositories and the Java API. The native Python API must also be installed to
use the UDF simulator (details found in [Development](#development)).

* In the desired directory, run the following to download the Kinetica Java UDF
  tutorial repository:

      git clone -b release/v7.1 --single-branch https://github.com/kineticadb/kinetica-tutorial-java-udf-api.git

* In the same directory, run the following to download the Kinetica Java UDF
  API repository:

      git clone -b release/v7.1 --single-branch https://github.com/kineticadb/kinetica-udf-api-java.git

* In the same directory, run the following to download the Kinetica Python API
  repository:

      git clone -b release/v7.1 --single-branch https://github.com/kineticadb/kinetica-api-python.git

* Change directory into the newly downloaded native Python API repository:

      cd kinetica-api-python

* In the root directory of the repository, install the Python API:

      sudo python setup.py install

* Change directory into the Java UDF API directory:

      cd ../kinetica-udf-api-java/proc-api

* Install the Java UDF API:

      mvn clean package
      mvn install

* Change directory into the UDF tutorial root:

      cd ../..


## Development

The steps below outline using the
[UDF Simulator](https://docs.kinetica.com/7.1/udf/simulating_udfs/),
included with the Python API. The UDF Simulator simulates the mechanics of
[executeProc()](https://docs.kinetica.com/7.1/api/java/?com/gpudb/GPUdb.html#executeProc-java.lang.String-java.util.Map-java.util.Map-java.util.List-java.util.Map-java.util.List-java.util.Map-)
without actually calling it in the database; this is useful for developing UDFs
piece-by-piece and test incrementally, avoiding memory ramifications for the
database.


### Compile

The UDF files must be compiled into a JAR prior to usage; the files will need
to be re-compiled after making any changes to the UDF code. Re-compiling this
tutorial using the provided main ``pom.xml`` file will create two JARs: one for
the UDF itself and one for the manager.

To compile the example UDF & manager:

     cd kinetica-tutorial-java-udf-api/table-copy
     mvn clean package
     cd output

**IMPORTANT:**
   When working on your own UDFs, ensure that the Kinetica Java UDF API is not
   bundled with your UDF JAR; otherwise, there could be a compilation target
   platform conflict with the UDF API on the Kinetica server.


### Test

A UDF can be tested using the UDF Simulator in the native Python API
repository without writing anything to the database.

* Run the UDF manager JAR with the ``init`` option, specifying the database URL
  and a username & password:

      java -jar kinetica-udf-table-copy-manager-7.1.2-jar-with-dependencies.jar init <url> <username> <password>

* In the native Python API directory, run the UDF Simulator in ``execute``
  mode with the following options to simulate running the UDF:
   
      python ../../../kinetica-api-python/examples/udfsim.py execute -d \
         -i [<schema>.]<input-table> -o [<schema>.]<output-table> \
         -K <url> -U <username> -P <password>

  Where:

  * ``-i`` - schema-qualified UDF input table
  * ``-o`` - schema-qualified UDF output table
  * ``-K`` - Kinetica URL
  * ``-U`` - Kinetica username
  * ``-P`` - Kinetica password

  For instance:

      python ../../../kinetica-api-python/examples/udfsim.py execute -d \
         -i udf_tc_java_in_table -o udf_tc_java_out_table \
         -K http://127.0.0.1:9191 -U admin -P admin123

* Copy & execute the ``export`` command output by the previous command; this
  will prepare the execution environment for simulating the UDF:

      export KINETICA_PCF=/tmp/udf-sim-control-files/kinetica-udf-sim-icf-xMGW32

  **IMPORTANT:**
      The ``export`` command shown above is an *example* of what the
      ``udfsim.py`` script will output--it should **not** be copied to the
      terminal in which this example is being run.  Make sure to copy & execute
      the **actual** command output by ``udfsim.py`` in the previous step.

* Run the UDF:

      java -jar kinetica-udf-table-copy-proc-7.1.2-jar-with-dependencies.jar

* Run the UDF Simulator in ``output`` mode to output the results to
  Kinetica (use the dry run flag ``-d`` to avoid writing to Kinetica),
  The ``results`` map will be returned (even if there's nothing in it) as well
  as the number of records that were (or will be in the case of a dry run)
  added to the given output table:

      python ../../../kinetica-api-python/examples/udfsim.py output \
         -K <url> -U <username> -P <password>

  For instance:

      python ../../../kinetica-api-python/examples/udfsim.py output \
         -K http://127.0.0.1:9191 -U admin -P admin123

  This should output the following:

      No results
      Output:

      udf_tc_java_out_table: 10000 records

* Clean the control files output by the UDF Simulator:

      python ../../../kinetica-api-python/examples/udfsim.py clean

  **IMPORTANT:**
      The ``clean`` command is only necessary if data was output to Kinetica;
      otherwise, the UDF Simulator can be re-run as many times as desired
      without having to clean the output files and enter another export command.


## Deployment

If satisfied after testing your UDF with the UDF Simulator or if you want to see
your UDF in action, the UDF can be created and executed using the UDF methods
[createProc()](https://docs.kinetica.com/7.1/api/java/?com/gpudb/GPUdb.html#createProc-java.lang.String-java.lang.String-java.util.Map-java.lang.String-java.util.List-java.util.Map-)
and
[executeProc()](https://docs.kinetica.com/7.1/api/java/?com/gpudb/GPUdb.html#executeProc-java.lang.String-java.util.Map-java.util.Map-java.util.List-java.util.Map-java.util.List-java.util.Map-)
(respectively).

* Run the UDF manager JAR with the ``init`` option to reset the example
  tables:

      java -jar kinetica-udf-table-copy-manager-7.1.2-jar-with-dependencies.jar init <url> <username> <password>

* Run the UDF manager JAR with the ``exec`` option to run the example:

      java -jar kinetica-udf-table-copy-manager-7.1.2-jar-with-dependencies.jar exec <url> <username> <password>

* Verify the results, using a SQL client (KiSQL), Kinetica Workbench, or other:

  * The ``udf_tc_java_in_table`` table is created in the user's default schema
    (``ki_home``, unless a different one was assigned during account creation)
  * A matching ``udf_tc_java_out_table`` table is created in the same schema
  * The ``udf_tc_java_in_table`` contains 10,000 records of random data
  * The ``udf_tc_java_out_table`` contains the correct amount of copied data
    from ``udf_tc_java_in_table``.
     
    On single-node installations, as is the case with *Developer Edition*, all
    data should be copied.  This is because single-node instances have a
    default configuration of 2 worker ranks with one TOM each, and the
    ``rank_tom.csv`` configuration file contains a reference to rank 1/TOM 0
    and rank 2/TOM 0, effectively naming both data TOMs to copy data from.
     
    In larger cluster configurations, only a fraction of the data in the input
    table will be stored on those two TOMs; so, the output table will contain
    that same fraction of the input table's data.
     
    The database logs should also show the portion of the data being copied:
     
        Copying <5071> records of <3> columns on rank/TOM <1/0> from <ki_home.udf_tc_java_in_table> to <ki_home.udf_tc_java_out_table>
        Copying <4929> records of <3> columns on rank/TOM <2/0> from <ki_home.udf_tc_java_in_table> to <ki_home.udf_tc_java_out_table>


## UDF Detail

As mentioned previously, this section details a simple distributed UDF that
copies data from one table to another. While the table copy UDF can run
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

The UDF will first read from a given CSV file to determine from which
processing node container (*rank*) and processing node (*TOM*) to copy data:

    rank_num,tom_num
    1,0
    2,0

The ``tom_num`` column values refer to processing nodes that contain the many
shards of data inside the database. The ``rank_num`` column values refer to
processing node containers that hold the processing nodes for the database. For
example, the given CSV file determines that the data from
``udf_tc_java_in_table`` on processing node container ``1``, processing node
``0`` and processing node container ``2``, processing node ``0`` will be copied
to ``udf_tc_java_out_table`` on those same nodes.

Once the UDF is executed, a UDF instance (OS process) is spun up for each
processing node to execute the UDF code against its assigned processing node's
data.  Each UDF process then determines if its corresponding processing node
container/processing node pair matches one of the pairs of values in the CSV
file. If there is a match, the UDF process will loop through the given input
tables and copy the data contained in that processing node from the input tables
to the output tables. If there isn't a match, no data will be copied by that
process.


### Initialization (UdfTcManager.java init)

The *init* option invokes the ``init()`` method of the ``UdfTcManager`` class.
This method will create the input table for the UDF to copy data from and the
output table to copy data to. Sample data will also be generated and inserted
into the input table.

To create tables using the Java API, a [type](https://docs.kinetica.com/7.1/concepts/types/)
needs to be defined in the system first.  The type is a class, extended from
[RecordObject](https://docs.kinetica.com/7.1/api/java/?com/gpudb/RecordObject.html),
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
    @RecordObject.Column(order=2)
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

To interact with Kinetica, you must first instantiate an object of the
``GPUdb`` class while providing the connection URL and username & password to
use for logging in. This database object is later passed to the ``init()`` and
``exec()`` methods:

```java
GPUdbBase.Options options = new GPUdbBase.Options();
options.setUsername(username);
options.setPassword(password);
GPUdb kinetica = new GPUdb(url, options);
```

The ``InTable`` type and table are created, but the table is removed first if it
already exists. Then the table creation is verified using ``showTable()``:

```java
kinetica.clearTable(inputTable, null, GPUdb.options("no_error_if_not_exists", "true"));
String inTableId = RecordObject.createType(InTable.class, kinetica);
kinetica.createTable(inputTable, inTableId, null);
System.out.println("Input table successfully created:");
ShowTableResponse showInputTable = kinetica.showTable(inputTable, null);
System.out.println(showInputTable.getTableNames().get(0) + " with type id " + showInputTable.getTypeIds().get(0));
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
kinetica.insertRecords(inputTable, allRecords, null);
GetRecordsResponse getRecordsResponse = kinetica.getRecords(inputTable, 0, GPUdbBase.END_OF_SET, null);
System.out.println("Number of records inserted into the input table: " + getRecordsResponse.getTotalNumberOfRecords());
```

Lastly, an ``OutTable`` type and table are created, but the table is removed
first if it already exists. Then the table creation is verified using
``showTable()``:

```java
kinetica.clearTable(outputTable, null, GPUdb.options("no_error_if_not_exists", "true"));
String outTableId = RecordObject.createType(OutTable.class, kinetica);
kinetica.createTable(outputTable, outTableId, null);
System.out.println("Output table successfully created:");
ShowTableResponse showOutputTable = kinetica.showTable(outputTable, null);
System.out.println(showOutputTable.get(0) + " with type id " + showOutputTable.get(3));
```


### UDF (UdfTcJavaProc.java)

The ``UdfTcJavaProc`` class is the UDF itself.  It does the work of copying the
input table data to the output table, based on the ranks & TOMs specified in the
given CSV file.

First, instantiate a handle to the ``ProcData`` class:

```java
ProcData procData = ProcData.get();
```

Retrieve the rank/TOM pair for this UDF process instance from the request info
map:

```java
final String procRankNum = procData.getRequestInfo().get("rank_number");
final String procTomNum = procData.getRequestInfo().get("tom_number");
```

Then, the CSV file mentioned in [Program Files](#program-files) is read
(skipping the header):

```java
Scanner scanner = new Scanner(new File("rank_tom.csv"));
scanner.nextLine();
```

Compare the rank and TOM of the current UDF instance's processing node to each
rank/TOM pair in the file to determine if the current UDF instance should copy
the data on its corresponding processing node:

```java
while (scanner.hasNextLine())
{
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

For each input column in the input table(s), copy the input columns' values to
the corresponding output table columns:

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
                throw new RuntimeException("Unhandled column type <" + inputColumn.getType() + ">");
        }
    }
}    
```

Call ``complete()`` to tell Kinetica the UDF is finished:

```java
procData.complete();
```


### Execution  (UdfTcManager.java exec)

The *exec* option invokes the ``exec()`` method of the ``UdfTcManager`` class.
This method will read files in as bytes, create a UDF, and upload the files to
the database. The method will then execute the UDF.

To upload the ``UdfTcManager.jar`` and ``rank_tom.csv`` files to Kinetica,
they will first need to be read in as bytes and added to a file data map:

```java
Map<String, ByteBuffer> filesMap = new HashMap<>();
for (String fileName : Arrays.asList(CSV_FILE, PROC_JAR_FILE))
{
    byte [] fileAsBytes = Files.readAllBytes(new File(fileName).toPath());
    ByteBuffer fileByteBuffer = ByteBuffer.wrap(fileAsBytes);
    filesMap.put(fileName, fileByteBuffer);
}
```

After the files are placed in a data map, the distributed ``UdfTcJavaProc``
UDF can be created in Kinetica and the files can be associated with it:

```java
CreateProcResponse createProcResponse = kinetica.createProc(
        PROC_NAME,
        "distributed",
        filesMap,
        "java",
        Arrays.asList(
                "-cp",
                PROC_JAR_FILE + ":" +
                    PROC_API_7100_FILE + ":" +
                    PROC_API_7101_FILE + ":" +
                    PROC_API_7102_FILE + ":" +
                    PROC_API_FILE,
                PROC_PATH
        ),
        null
);
```

**NOTE:**
   The Java UDF command line needs to reference:
   
   * ``java`` as the command to run
   * a classpath parameter including the uploaded UDF JAR and the server-side
     UDF API JAR (here, all 7.1 versions are included to cover any UDF build)
   * a parameter that is the fully-qualified class name of the UDF Java class

   in this case, the assembled command line would be:

    java -cp kinetica-udf-table-copy-proc-7.1.2.jar:<UDF API JARs> com.kinetica.UdfTcJavaProc

Finally, after the UDF is created, it can be executed. The input & output tables
created in the [Initialization](#initialization-udftcmanagerjava-init) section are passed in
here:

```java
ExecuteProcResponse executeProcResponse = kinetica.executeProc(
        PROC_NAME,
        null,
        null,
        Collections.singletonList(inputTable),
        null,
        Collections.singletonList(outputTable),
        null
);
```


## Support

For bugs, please submit an
[issue on Github](https://github.com/kineticadb/kinetica-udf-api-java/issues).

For support, you can post on
[stackoverflow](https://stackoverflow.com/questions/tagged/kinetica) under the
``kinetica`` tag or
[Slack](https://join.slack.com/t/kinetica-community/shared_invite/zt-1bt9x3mvr-uMKrXlSDXfy3oU~sKi84qg).


## Contact Us

* Ask a question on Slack:
  [Slack](https://join.slack.com/t/kinetica-community/shared_invite/zt-1bt9x3mvr-uMKrXlSDXfy3oU~sKi84qg)
* Follow on GitHub:
  [Follow @kineticadb](https://github.com/kineticadb) 
* Email us:  <support@kinetica.com>
* Visit:  <https://www.kinetica.com/contact/>

