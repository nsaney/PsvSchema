/*
 * Nicholas Saney
 * 
 * Created: July 14, 2015
 * 
 * PsvSchemaRecord.java
 * PsvSchemaRecord class definition
 */

package chairosoft.psv;

public class PsvSchemaRecord
{
    // constants
    public static final int EXPECTED_VALUE_COUNT = 7;
    
    // fields
    // isExtendable|tableName|parentTableName|columnName|type|partOfPrimaryKey|required
    public final boolean isExtendable;
    public final String tableName;
    public final String parentTableName;
    public final String columnName;
    public final String type;
    public final boolean isPartOfPrimaryKey;
    public final boolean isRequired;
    
    // constructors
    public PsvSchemaRecord(PsvRecord psvRecord)
    {
        this.isExtendable = Boolean.parseBoolean(psvRecord.values[0]);
        this.tableName = psvRecord.values[1];
        this.parentTableName = psvRecord.values[2];
        this.columnName = psvRecord.values[3];
        this.type = psvRecord.values[4];
        this.isPartOfPrimaryKey = Boolean.parseBoolean(psvRecord.values[5]);
        this.isRequired = Boolean.parseBoolean(psvRecord.values[6]);
    }
    
    public PsvSchemaRecord(boolean _isExtendable, String _tableName, String _parentTableName, String _columnName, String _type, boolean _isPartOfPrimaryKey, boolean _isRequired)
    {
        this.isExtendable = _isExtendable;
        this.tableName = _tableName;
        this.parentTableName = _parentTableName;
        this.columnName = _columnName;
        this.type = _type;
        this.isPartOfPrimaryKey = _isPartOfPrimaryKey;
        this.isRequired = _isRequired;
    }
}