/*
 * Nicholas Saney
 * 
 * Created: July 11, 2015
 * 
 * ColumnType.java
 * ColumnType class definition
 */

package chairosoft.psv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class ColumnType<V extends ColumnValue>
{
    // constants
    public static final ColumnType STRING = StringColumnType.SINGLETON;
    public static final ColumnType INTEGER = IntegerColumnType.SINGLETON;
    public static final ColumnType BOOLEAN = BooleanColumnType.SINGLETON;
    
    // static fields
    private static final ArrayList<ColumnType> typesById = new ArrayList<>();
    public static ColumnType getById(int typeId) { return ColumnType.typesById.get(typeId); }
    private static final HashMap<String, ColumnType> typesByName = new HashMap<>();
    public static ColumnType getByName(String typeName) { return ColumnType.typesByName.get(typeName); }
    
    // instance fields
    public final int id;
    { ColumnType.typesById.add(this); this.id = ColumnType.typesById.size() - 1; }
    public final String name;
    public final String psvItemRegex;
    
    // constructor
    protected ColumnType(String _name, String _psvItemRegex)
    {
        this.name = _name;
        this.psvItemRegex = _psvItemRegex;
        ColumnType oldType = ColumnType.typesByName.put(this.name, this);
        if (oldType != null)
        {
            String message = String.format("A type with the name \"%s\" already exists with id = %s. The new type has id = %s.", this.name, oldType.id, this.id);
            throw new IllegalArgumentException(message);
        }
    }
    
    // instance methods
    public abstract ColumnValue parseNew(String literal);
    public void parseInto(String literal, V targetColumnValue)
    {
        targetColumnValue.setStringValue(literal);
    }
    
    @Override
    public String toString()
    {
        return String.format("ColumnType[%s](%s)", this.id, this.name);
    }
    
    
    // static inner classes
    private static class StringColumnType extends ColumnType<ColumnValue.StringColumnValue>
    {
        // static fields
        public static final ColumnType SINGLETON = new StringColumnType();
        
        // constructor
        private StringColumnType() { super(String.class.getSimpleName(), Table.SIMPLE_KEY_ITEM_REGEX); }
        
        // instance methods
        @Override
        public ColumnValue parseNew(String literal) 
        {
            return new ColumnValue.StringColumnValue(literal); 
        }
    }
    
    private static class IntegerColumnType extends ColumnType<ColumnValue.IntegerColumnValue>
    {
        // static fields
        public static final ColumnType SINGLETON = new IntegerColumnType();
        
        // constructor
        private IntegerColumnType() { super(Integer.class.getSimpleName(), Table.SIMPLE_KEY_ITEM_REGEX); }
        
        // instance methods
        @Override
        public ColumnValue parseNew(String literal) 
        {
            return new ColumnValue.IntegerColumnValue(literal); 
        }
    }
    
    private static class BooleanColumnType extends ColumnType<ColumnValue.BooleanColumnValue>
    {
        // static fields
        public static final ColumnType SINGLETON = new BooleanColumnType();
        
        // constructor
        private BooleanColumnType() { super(Boolean.class.getSimpleName(), Table.SIMPLE_KEY_ITEM_REGEX); }
        
        // instance methods
        @Override
        public ColumnValue parseNew(String literal) 
        {
            return new ColumnValue.BooleanColumnValue(literal); 
        }
    }
    
    public static class ReferenceColumnType extends ColumnType<ColumnValue.ReferenceColumnValue>
    {
        // instance fields
        public final Table table;
        
        // constructor
        public ReferenceColumnType(Table _table)
        {
            super(_table.name, _table.getPsvIdentifierRegex());
            this.table = _table;
        }
        
        // instance methods
        @Override
        public ColumnValue parseNew(String literal) { return new ColumnValue.ReferenceColumnValue(this.table, literal); }
    }
}