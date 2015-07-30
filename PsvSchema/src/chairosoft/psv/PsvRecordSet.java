/*
 * Nicholas Saney
 * 
 * Created: July 08, 2015
 * 
 * PsvRecordSet.java
 * PsvRecordSet class definition
 */

package chairosoft.psv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PsvRecordSet
{
    // fields
    public final PsvRecord headerRecord;
    public final ArrayList<PsvRecord> records;
    
    // constructor
    public PsvRecordSet(PsvRecord _headerRecord, ArrayList<PsvRecord> _records)
    {
        this.headerRecord = _headerRecord;
        this.records = _records;
    }
    
    // static methods
    public static PsvRecordSet readFrom(File file)
    {
        return PsvRecordSet.readFrom(file, 0);
    }
    
    public static PsvRecordSet readFrom(File file, int expectedValueCount)
    {
        PsvRecord headerRecord = null;
        ArrayList<PsvRecord> records = new ArrayList<>();
        int lineNumber = 1;
        try (Scanner scanner = new Scanner(file))
        {
            String headerLine = "";
            if (scanner.hasNextLine())
            {
                headerLine = scanner.nextLine();
            }
            else
            {
                throw new IllegalArgumentException("PSV file must have at least one line (a header line).");
            }
            headerRecord = new PsvRecord(headerLine, expectedValueCount);
            ++lineNumber;
            
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                PsvRecord record = new PsvRecord(line, headerRecord.values.length);
                records.add(record);
                ++lineNumber;
            }
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (Exception ex)
        {
            String message = String.format("Error in line %s of file %s.", lineNumber, file);
            throw new IllegalStateException(message, ex);
        }
        
        return new PsvRecordSet(headerRecord, records);
    }
    
    // instance methods
    public void writeTo(Writer writer)
        throws IOException
    {
        this.headerRecord.writeTo(writer);
        for (PsvRecord record : this.records)
        {
            record.writeTo(writer);
        }
    }
}