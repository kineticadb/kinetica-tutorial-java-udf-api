package com.kinetica;

import java.io.File;
import java.util.Scanner;

/* *****************************************************************************
*                                                                              *
* Kinetica UDF Table Copy Example                                              *
* -------------------------------------------------------------------------    *
* This distributed UDF will be run by a process on each rank/TOM pair in the   *
* database.  If the rank/TOM pair associated with a process is found in the    *
* rank_tom.csv provided to the UDF, that process will copy all of the data in  *
* the specified input tables located on its rank/TOM to the specified output   *
* tables, which will be co-located on the same rank/TOM.                       *
*                                                                              *
* For example, given the following setup:                                      *
*                                                                              *
* * rank_tom.csv contains rank 2, TOM 1                                        *
* * input table name is udf.in                                                 *
* * output table name is udf.out                                               *
*                                                                              *
* The UDF will copy only the data from udf.in that resides on TOM 1 of rank 2  *
* to udf.out.                                                                  *
*                                                                              *
***************************************************************************** */

public class UdfTcJavaProc
{
    public static void main(String[] args)
    {
        // Instantiate a handle to the ProcData object
        ProcData procData = ProcData.get();

        // Initialize boolean that determines whether the current rank/TOM
        //   proceess has found itself in the given rank_tom.csv
        boolean foundMatch = false;

        // Retrieve rank and TOM from this UDF's request info map; together
        // these two numbers uniquely identify this process of the UDF
        final String procRankNum = procData.getRequestInfo().get("rank_number");
        final String procTomNum = procData.getRequestInfo().get("tom_number");
        try
        {
            // Read the CSV file (skipping the header) and extract the file's
            // rank/TOM pairs to determine whether any refer to this process
            Scanner scanner = new Scanner(new File("rank_tom.csv"));
            scanner.nextLine();
            while (scanner.hasNextLine())
            {
                String[] row = scanner.nextLine().split(",", -1);
                final String fileRankNum = row[0];
                final String fileTomNum = row[1];

                // Check if this proc instance's rank/TOM match the file values
                if (procRankNum.equals(fileRankNum) && procTomNum.equals(fileTomNum))
                {
                    // Loop through the given input tables
                    for (int i = 0; i < procData.getInputData().getTableCount(); i++)
                    {
                        ProcData.InputTable inputTable = procData.getInputData().getTable(i);
                        ProcData.OutputTable outputTable = procData.getOutputData().getTable(i);
                        outputTable.setSize(inputTable.getSize());

                        System.out.println(
                                "Copying <" + inputTable.getSize() + "> records " +
                                "of <" + inputTable.getColumnCount() + "> columns " +
                                "on rank/TOM <" + fileRankNum + "/" + fileTomNum + "> " +
                                "from <" + inputTable.getName() + "> to <" + outputTable.getName() + ">"
                        );

                        // Loop through the columns in the given input tables
                        for (int j = 0; j < inputTable.getColumnCount(); j++)
                        {
                            ProcData.InputColumn inputColumn = inputTable.getColumn(j);
                            ProcData.OutputColumn outputColumn = outputTable.getColumn(j);

                            // For each record on this rank/TOM, copy the data
                            // from the input column to the output column
                            for (long k = 0; k < inputTable.getSize(); k++)
                            {
                                switch (inputColumn.getType())
                                {
                                	case BOOLEAN: outputColumn.appendBoolean(inputColumn.getBoolean(k)); break;
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
                                    case ULONG: outputColumn.appendBigInteger(inputColumn.getBigInteger(k)); break;
                                    case UUID: outputColumn.appendUUID(inputColumn.getUUID(k)); break;
                                    default:
                                        throw new RuntimeException("Unhandled column type <" + inputColumn.getType() + ">");
                                }
                            }
                        }
                    }
                    foundMatch = true;
                    break;
                }
            }
            // If no matches exist, don't copy any values
            if (!foundMatch)
                System.out.println(
                        "This rank/TOM <" + procRankNum + "/" + procTomNum + "> not present in rank_tom.csv"
                );

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        // Inform Kinetica that the proc has finished successfully
        procData.complete();
    }
}
