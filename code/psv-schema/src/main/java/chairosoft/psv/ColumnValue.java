/*
 * Nicholas Saney
 * 
 * Created: July 13, 2015
 * 
 * ColumnValue.java
 * ColumnValue class definition
 */

package chairosoft.psv;

public abstract class ColumnValue
{
    // constructors
    protected ColumnValue() { }
    
    protected ColumnValue(String _value)
    {
        this.setStringValue(_value);
    }
    
    // instance methods
    public abstract String getStringValue();
    public abstract void setStringValue(String updatedValue);
    @Override public abstract int hashCode();
    @Override public abstract boolean equals(Object o);
    
    // static inner classes
    public static class StringColumnValue extends ColumnValue
    {
        // fields
        public String value;
        
        // constructors
        public StringColumnValue(String _value)
        {
            super(_value);
        }
        
        // instance methods
        @Override public final String getStringValue() { return this.value; }
        @Override public final void setStringValue(String updatedValue) { this.value = updatedValue; }
        @Override public int hashCode() { return this.value.hashCode(); }
        @Override public boolean equals(Object o) { return (o instanceof StringColumnValue) && ((StringColumnValue)o).value.equals(this.value); }
    }
    
    public static class IntegerColumnValue extends ColumnValue
    {
        // fields
        public int value;
        
        // constructors
        public IntegerColumnValue(int _value)
        {
            this.value = _value;
        }
        
        public IntegerColumnValue(String _value)
        {
            super(_value);
        }
        
        // instance methods
        @Override public final String getStringValue() { return Integer.toString(this.value); }
        @Override public final void setStringValue(String updatedValue) { this.value = Integer.parseInt(updatedValue); }
        @Override public int hashCode() { return this.value; }
        @Override public boolean equals(Object o) { return (o instanceof IntegerColumnValue) && ((IntegerColumnValue)o).value == this.value; }
    }
    
    public static class BooleanColumnValue extends ColumnValue
    {
        // fields
        public boolean value;
        
        // constructors
        public BooleanColumnValue(boolean _value)
        {
            this.value = _value;
        }
        
        public BooleanColumnValue(String _value)
        {
            super(_value);
        }
        
        // instance methods
        @Override public final String getStringValue() { return Boolean.toString(this.value); }
        @Override public final void setStringValue(String updatedValue) { this.value = Boolean.parseBoolean(updatedValue); }
        @Override public int hashCode() { return this.value ? 0 : 1; }
        @Override public boolean equals(Object o) { return (o instanceof BooleanColumnValue) && ((BooleanColumnValue)o).value == this.value; }
    }
    
    public static class ReferenceColumnValue extends ColumnValue
    {
        // fields
        public final Table table;
        public Row value;
        
        // constructors
        public ReferenceColumnValue(Table _table, Row _value)
        {
            this.table = _table;
            this.value = _value;
        }
        
        public ReferenceColumnValue(Table _table, String _value)
        {
            this.table = _table;
            this.setStringValue(_value);
        }
        
        // instance methods
        @Override public final String getStringValue() { return this.value.getPsvIdentifier(); }        
        @Override public final void setStringValue(String updatedValue) { this.value = this.table.getRowFromPsvIdentifier(updatedValue); }        
        @Override public int hashCode() { return this.value.hashCode(); }        
        @Override public boolean equals(Object o) { return (o instanceof ReferenceColumnValue) && ((ReferenceColumnValue)o).value.equals(this.value); }
    }
}