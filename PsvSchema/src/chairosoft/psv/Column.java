/*
 * Nicholas Saney
 * 
 * Created: July 11, 2015
 * 
 * Column.java
 * Column class definition
 */

package chairosoft.psv;

public class Column
{
    // fields
    public final String name;
    public final ColumnType type;
    public final boolean isPartOfPrimaryKey;
    public final boolean isRequired;
    
    // constructor
    public Column(String _name, ColumnType _type, boolean _isPartOfPrimaryKey, boolean _isRequired)
    {
        this.name = _name;
        this.type = _type;
        this.isPartOfPrimaryKey = _isPartOfPrimaryKey;
        this.isRequired = _isRequired;
    }
}