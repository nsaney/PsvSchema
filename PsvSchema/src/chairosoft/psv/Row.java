/*
 * Nicholas Saney
 * 
 * Created: July 12, 2015
 * 
 * Row.java
 * Row class definition
 */

package chairosoft.psv;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class Row
{
    // instance fields
    public final Table table;
    protected final ColumnValue[] values;
    public final ColumnValue getValue(int i) { return this.values[i]; }
    protected final Map<String, ColumnValue> valuesByColumnName = new HashMap<>();
    public final ColumnValue getValue(String columnName) { return this.valuesByColumnName.get(columnName); }
    protected final Key key;
    
    // constructor
    public Row(Table _table, ColumnValue[] _values)
    {
        this.table = _table;
        this.values = _values;
        if (this.table.columns.length != this.values.length)
        {
            String message = String.format("Number of columns (%s) does not match number of values (%s) given to Row.", this.table.columns.length, this.values.length);
            throw new IllegalStateException(message);
        }
        IntStream.range(0, this.values.length)
            .forEach(i -> this.valuesByColumnName.put(this.table.columns[i].name, this.values[i]));
        ColumnValue[] keyValues = IntStream.range(0, this.values.length)
            .filter(i -> this.table.columns[i].isPartOfPrimaryKey)
            .mapToObj(i -> this.values[i])
            .toArray(ColumnValue[]::new);
        Key _key = null;
        if (keyValues.length < this.values.length)
        {
            _key = new Key(this.table, keyValues);
        }
        else if (this instanceof Key)
        {
            _key = (Key)this;
        }
        else 
        {
            String message = String.format("Invalid key. Row values count = %s, key values count = %s, type = %s.", this.values.length, keyValues.length, this.getClass());
            throw new IllegalStateException(message);
        }
        this.key = _key;
    }
    
    // instance methods
    public String getPsvIdentifier() 
    {
        return this.table.getPsvIdentifierFromRow(this);
    }
    
    public PsvRecord toPsvRecord()
    {
        Stream<String> psvRecordValues = Stream.of(this.values)
            .map(value -> value.getStringValue());
        PsvRecord result = new PsvRecord(psvRecordValues);
        return result;
    }
    
    @Override
    public int hashCode()
    {
        return this.key.hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Row)) { return false; }
        return this.equals((Row)o);
    }
    
    public boolean equals(Row that) 
    {
        return this.key.equals(that.key); 
    }
    
    
    // static inner class
    public static class Key extends Row
    {
        // constructor
        public Key(Table _table, ColumnValue[] _values) { super(_table, _values); }
        
        // instance methods
        @Override
        public int hashCode()
        {
            int result = IntStream.range(0, this.values.length)
                .reduce(0, (partialResult, i) -> 37 * partialResult + this.values[i].hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Key)) { return false; }
            return this.equals((Key)o);
        }
        
        public boolean equals(Key that)
        {
            boolean result = IntStream.range(0, this.values.length)
                .allMatch(i -> this.values[i].equals(that.values[i]));
            return result;
        }
    }
    
}