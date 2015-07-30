/*
 * Nicholas Saney
 * 
 * Created: July 11, 2015
 * 
 * Table.java
 * Table class definition
 */

package chairosoft.psv;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class Table implements Iterable<Column>
{
    // constants
    public static final String KEY_LIST_SEPARATOR = "#";
    public static final String KEY_LIST_OPENER = "{";
    public static final String KEY_LIST_CLOSER = "}";
    public static final String SIMPLE_KEY_ITEM_REGEX = String.format("([^\\%s\\%s\\%s])", KEY_LIST_SEPARATOR, KEY_LIST_OPENER, KEY_LIST_CLOSER);
    
    // static fields
    private static final HashMap<String, Table> tablesByName = new HashMap<>();
    public static Table getTable(String tableName) { return Table.tablesByName.get(tableName); }
    
    // instance fields
    public final String name;
    public final boolean isExtendable;
    public final Table parent;
    public final boolean hasParent() { return this.parent != null; }
    protected final List<Table> children = new ArrayList<>();
    protected final Column[] columns;
    public final Column getColumn(int i) { return this.columns[i]; }
    protected final Column[] keyColumns;
    public final Map<Row.Key, Row> rowsByKey = new HashMap<>();
    protected final PsvRecord psvHeader;
    public final void writePsvHeaderTo(Writer writer) throws IOException { this.psvHeader.writeTo(writer); }
    private final boolean needsPsvIdTableName;
    private final boolean needsPsvIdListOpener;
    private final Pattern psvIdentifierPattern;
    
    // constructor
    public Table(String _name, boolean _isExtendable, Table _parent, Column[] _ownColumns)
    {
        this.name = _name;
        this.isExtendable = _isExtendable;
        this.parent = _parent;
        
        Column[] _columns = null;
        if (this.hasParent())
        {
            _columns = Stream.concat(Stream.of(this.parent.columns), Stream.of(_ownColumns))
                .toArray(Column[]::new);
            this.parent.children.add(this);
        }
        else
        {
            _columns = _ownColumns;
        }
        this.columns = _columns;
        this.keyColumns = Stream.of(this.columns)
            .filter(col -> col.isPartOfPrimaryKey)
            .toArray(Column[]::new);
        
        Stream<String> psvHeaderValues = Stream.of(this.columns)
            .map(col -> col.name);
        this.psvHeader = new PsvRecord(psvHeaderValues);
        
        this.needsPsvIdTableName = this.hasParent() || this.isExtendable;
        this.needsPsvIdListOpener = this.needsPsvIdTableName || this.keyColumns.length > 1;
        String psvIdentifierRegex = this.getPsvIdentifierRegex();
        this.psvIdentifierPattern = Pattern.compile(psvIdentifierRegex);
        
        Table.tablesByName.put(this.name, this);
    }
    
    // methods
    @Override public Iterator<Column> iterator() { return new ArrayIterator<Column>(this.columns); }
    @Override public String toString() { return this.name; }
    
    public String getPsvIdentifierFromRow(Row row)
    {
        if (this != row.table)
        {
            String message = String.format("Table (%s) does not match row's table (%s).", this.name, row.table.name);
            throw new IllegalStateException(message);
        }
        String tableName = this.needsPsvIdTableName ? this.name : "";
        String keyListOpener = this.needsPsvIdListOpener ? KEY_LIST_OPENER : "";
        String keyListCloser = this.needsPsvIdListOpener ? KEY_LIST_CLOSER : "";
        String keyList = Stream.of(row.key.values)
            .map(value -> value.getStringValue())
            .collect(Collectors.joining(KEY_LIST_SEPARATOR));
        String result = String.format("%s%s%s%s", tableName, keyListOpener, keyList, keyListCloser);
        return result;
    }
    
    public String getPsvIdentifierRegex()
    {
        String result = "";
        if (this.needsPsvIdTableName) { result += this.name; }
        if (this.needsPsvIdListOpener) { result += KEY_LIST_OPENER; }
        String innerResult = Stream.of(this.keyColumns)
            .map(column -> column.type.psvItemRegex)
            .collect(Collectors.joining(KEY_LIST_SEPARATOR));
        result += innerResult;
        if (this.needsPsvIdListOpener) { result += KEY_LIST_CLOSER; }
        return result;
    }
    
    public static class TableMatcher
    {
        public final Table table;
        public final Matcher matcher;
        public TableMatcher(Table t, Matcher m) { this.table = t; this.matcher = m; }
    }
    
    public TableMatcher matchingPsvIdentifierTableName(String psvIdentifier)
    {
        Matcher psvIdentifierMatcher = this.psvIdentifierPattern.matcher(psvIdentifier);
        if (psvIdentifierMatcher.matches()) { return new TableMatcher(this, psvIdentifierMatcher); }
        TableMatcher result = null;
        for (Table child : this.children)
        {
            result = child.matchingPsvIdentifierTableName(psvIdentifier);
            if (result != null) { break; }
        }
        return result;
    }
    
    public boolean matchesPsvIdentifierTableName(String psvIdentifier)
    {
        return this.matchingPsvIdentifierTableName(psvIdentifier) != null;
    }
    
    public Row getRowFromPsvIdentifier(String psvIdentifier)
    {
        TableMatcher tableMatcher = this.matchingPsvIdentifierTableName(psvIdentifier);
        if (tableMatcher == null)
        {
            String message = String.format("PSV identifier (%s) is not compatible with table type (%s).", psvIdentifier, this.name);
            throw new IllegalArgumentException(message);
        }
        
        // {v1#v2#{v3a#v3b}#v4}
        
        // TODO
        throw new UnsupportedOperationException();
    }
}