import com.kinetica.*;

import java.io.File;
import java.util.Scanner;

/* *****************************************************************************
*                                                                              *
* Kinetica UDF Table Copy Example                                              *
* -------------------------------------------------------------------------    *
* This UDF first compares the given CSV file's rank and tom number values      *
* to the ranks and toms processing the UDF. If the values in the CSV file      *
* match the ranks and toms found in the request info map, the associated       *
* data with the matching rank/tom is copied from the input table to the        *
* output table.                                                                *
*                                                                              *
***************************************************************************** */

public class UdfTcJavaProc
{
    public static void main(String[] args)
    {
        // Instantiate a handle to the ProcData object
        ProcData procData = ProcData.get();

        // Initialize boolean that's switched to true if a match is found
        boolean foundMatch = false;

        // Retrieve rank and TOM from this UDF's request info map; together
        // these two numbers uniquely identify this instance of the UDF
        final String procRankNum = procData.getRequestInfo().get("rank_number");
        final String procTomNum = procData.getRequestInfo().get("tom_number");
        try
        {
            // Read a CSV file (skipping the header) and assign the file's
            // values to variables that will be checked against the rank and tom
            // in this proc instance
            Scanner scanner = new Scanner(new File("rank_tom.csv"));
            scanner.nextLine();
            while (scanner.hasNextLine())
            {
                String[] row = scanner.nextLine().split(",", -1);
                final String fileRankNum = row[0];
                final String fileTomNum = row[1];

                // Check if this proc instance's rank and tom numbers match the
                // file's values
                if (procRankNum.equals(fileRankNum) && procTomNum.equals(fileTomNum))
                {
                    System.out.println("Match found; processing now...");

                    // Loop through input and output tables (assume the same
                    // number)
                    for (int i = 0; i < procData.getInputData().getTableCount(); i++)
                    {
                        ProcData.InputTable inputTable = procData.getInputData().getTable(i);
                        ProcData.OutputTable outputTable = procData.getOutputData().getTable(i);
                        outputTable.setSize(inputTable.getSize());

                        // Loop through columns in the input and output
                        // tables (assume the same number and types)
                        for (int j = 0; j < inputTable.getColumnCount(); j++)
                        {
                            ProcData.InputColumn inputColumn = inputTable.getColumn(j);
                            ProcData.OutputColumn outputColumn = outputTable.getColumn(j);

                            // For each record, copy the data from the input
                            // column to the output column
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
                    }
                    foundMatch = true;
                    break;
                }
            }
            // If no matches exist, don't copy any values
            if (!foundMatch)
                System.out.println("No rank or tom matches");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        // Inform Kinetica that the proc has finished successfully
        procData.complete();
    }
}
