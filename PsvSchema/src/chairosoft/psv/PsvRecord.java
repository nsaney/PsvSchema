/*
 * Nicholas Saney
 * 
 * Created: July 08, 2015
 * 
 * PsvRecord.java
 * PsvRecord class definition
 */

package chairosoft.psv;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class PsvRecord
{
    // constants
    public static final String VALUE_DELIMITER_REGEX = "\\|";
    public static final char VALUE_DELIMITER = '|';
    public static final char LINE_DELIMITER = '\n';
    
    // fields
    public final String[] values;
    
    // constructors
    public PsvRecord(int size)
    {
        this.values = new String[size];
    }
    
    public PsvRecord(List<String> valuesList)
    {
        this(valuesList.size());
        valuesList.toArray(this.values);
    }
    
    public PsvRecord(String[] _values)
    {
        this.values = Arrays.copyOf(_values, _values.length);
    }
    
    public PsvRecord(Stream<String> _values)
    {
        this.values = _values.toArray(String[]::new);
    }
    
    public PsvRecord(String rawLine)
    {
        this(rawLine, 0);
    }
    
    public PsvRecord(String rawLine, int expectedValueCount)
    {
        String[] resultValues = rawLine.split(VALUE_DELIMITER_REGEX, -1);
        
        if (expectedValueCount > 0 && resultValues.length != expectedValueCount)
        {
            String message = String.format("Line did not have expected number of values. Got %s, expected %s.", resultValues.length, expectedValueCount);
            throw new IllegalArgumentException(message);
        }
        
        this.values = resultValues;
    }
    
    // instance methods
    public void writeTo(Writer writer)
        throws IOException
    {
        if (this.values.length == 0) { return; }
        
        writer.write(this.values[0]);
        for (int i = 1; i < this.values.length; ++i)
        {
            writer.write(VALUE_DELIMITER);
            writer.write(this.values[i]);
        }
        
        writer.write(LINE_DELIMITER);
    }
}