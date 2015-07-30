/*
 * Nicholas Saney
 * 
 * Created: July 08, 2015
 * 
 * PsvSchema.java
 * PsvSchema class definition
 */

package chairosoft.psv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class PsvSchema
{
    public static class IndentingPrintWriter extends PrintWriter
    {
        private int indent = 0;
        private String currentTab = "";
        private void setTab() { currentTab = ""; for (int i = 0; i < this.indent; ++i) { currentTab += "\t"; } };
        public final void tabIn() { this.indent++; setTab(); }
        public final void tabOut() { this.indent--; setTab(); }
        
        public IndentingPrintWriter(File file) throws FileNotFoundException { super(file); }
        
        public void indentln() { this.indentln(""); }
        public void indentln(String s) { this.println(currentTab + s); }
    }
    
    public static final String keywords[] = 
    {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else", "extends", "false", "final", "finally", "float", 
        "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
        "new", "null", "package", "private", "protected", "public", "return", "short", "static", "strictfp", 
        "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", 
        "volatile", "while" 
    };

    public static boolean isJavaKeyword(String keyword) 
    {
        return (Arrays.binarySearch(keywords, keyword) >= 0);
    }
    
    public static String nonKeywordOf(String identifier)
    {
        if (isJavaKeyword(identifier)) { identifier += "_"; }
        return identifier;
    }
    
    public static String nonKeywordOf(String identifier, String defaultValue)
    {
        if (identifier == null || identifier.length() == 0) { return defaultValue; }
        return nonKeywordOf(identifier);
    }
    
    public static String getValidType(String typeName)
    {
        switch (typeName)
        {
            case "Boolean": return "boolean";
            case "Integer": return "int";
            default: return nonKeywordOf(typeName);
        }
    }
    
    public static String getColumnType(String typeName)
    {
        switch (typeName)
        {
            case "Boolean": return "ColumnType.BOOLEAN";
            case "Integer": return "ColumnType.INTEGER";
            case "String": return "ColumnType.STRING";
            default: return String.format("%s.TYPE", typeName);
        }
    }
    
    public static String getValueParseStatement(String typeName, String originalValueExpression, String columnValueExpression)
    {
        return String.format("%s.parseInto(%s, %s);", getColumnType(typeName), originalValueExpression, columnValueExpression);
    }
    
    public static final String ABSTRACT_SCHEMA_OBJECT_NAME = "AbstractSchemaObject";
    
    public static void main(String[] args)
        throws Exception
    {
        String fileLocation = args[0];
        File schemaFile = new File(fileLocation);
        
        System.out.printf("Reading from %s...", schemaFile);
        System.out.flush();
        PsvRecordSet schemaRecordSet = PsvRecordSet.readFrom(schemaFile, PsvSchemaRecord.EXPECTED_VALUE_COUNT);
        Map<String, List<PsvSchemaRecord>> psvSchemaRecordsByTableName = schemaRecordSet.records.stream()
            .map(psvRecord -> new PsvSchemaRecord(psvRecord))
            .collect(Collectors.groupingBy(psr -> psr.tableName));
        System.out.println(" done!");
        
        String genFileLocation = fileLocation + ".java";
        File genFile = new File(genFileLocation);
        
        System.out.printf("Writing to %s...", genFile);
        System.out.flush();
        try (IndentingPrintWriter out = new IndentingPrintWriter(genFile))
        {
            out.indentln("/*");
            out.indentln(" * This file is autogenerated. Any manual changes will ");
            out.indentln(" * be overwritten when the code is generated again.");
            out.indentln(" */");
            out.indentln("");
            
            String packageDeclaration = String.format("package %s;", args[1]);
            out.indentln(packageDeclaration);
            out.indentln("");
            out.indentln("import chairosoft.psv.*;");
            out.indentln("");
            out.indentln("public final class Schema");
            out.indentln("{");
            out.tabIn();
            {
                String abstractSchemaObjectDeclaration = String.format("public static abstract class %s", ABSTRACT_SCHEMA_OBJECT_NAME);
                out.indentln(abstractSchemaObjectDeclaration);
                out.indentln("{");
                out.tabIn();
                {
                    out.indentln("protected Row __row;");
                    out.indentln("public Row getRow() { this.syncRow(); return this.__row; }");
                    out.indentln("");
                    out.indentln("protected void syncRow() { }");
                    //String abstractSchemaObjectConstructor = String.format("protected %s() { this.__row = TODO; }", ABSTRACT_SCHEMA_OBJECT_NAME);
                    //out.indentln(abstractSchemaObjectConstructor);
                }
                out.tabOut();
                out.indentln("}");
                out.indentln("");
            }
            TreeSet<String> tableNames = new TreeSet<>(psvSchemaRecordsByTableName.keySet());
            for (String tableName : tableNames)
            {
                List<PsvSchemaRecord> records = psvSchemaRecordsByTableName.get(tableName);
                PsvSchemaRecord[] escapedRecords = records.stream()
                    .map(r -> new PsvSchemaRecord(
                        r.isExtendable, 
                        nonKeywordOf(r.tableName), 
                        nonKeywordOf(r.parentTableName, ABSTRACT_SCHEMA_OBJECT_NAME),
                        nonKeywordOf(r.columnName),
                        getValidType(r.type),
                        r.isPartOfPrimaryKey,
                        r.isRequired
                    ))
                    .toArray(PsvSchemaRecord[]::new);
                String[] commas = IntStream.range(0, escapedRecords.length)
                    .mapToObj(i -> i + 1 == escapedRecords.length ? "" : ",")
                    .toArray(String[]::new);
                PsvSchemaRecord schemaRecord = escapedRecords[0];
                String finalDescriptor = schemaRecord.isExtendable ? "" : " final";
                String className = schemaRecord.tableName;
                String parentName = schemaRecord.parentTableName; 
                boolean hasParent = !parentName.equals(ABSTRACT_SCHEMA_OBJECT_NAME);
                String classDeclaration = String.format("public static%s class %s extends %s", finalDescriptor, className, parentName);
                out.indentln(classDeclaration);
                out.indentln("{");
                out.tabIn();
                {
                    String parentReference = hasParent ? String.format("%s.TABLE", parentName) : "null";
                    String tableDeclarationStart = String.format("public static final Table TABLE = new Table(\"%s\", %s, %s, new Column[] {", className, schemaRecord.isExtendable, parentReference);
                    out.indentln(tableDeclarationStart);
                    out.tabIn();
                    for (int i = 0; i < escapedRecords.length; ++i)
                    {
                        PsvSchemaRecord escaped = escapedRecords[i];
                        if (escaped.columnName.length() == 0) { continue; }
                        String comma = commas[i];
                        String columnType = getColumnType(records.get(i).type);
                        String columnInstantiation = String.format("new Column(\"%s\", %s, %s, %s)%s", escaped.columnName, columnType, escaped.isPartOfPrimaryKey, escaped.isRequired, comma);
                        out.indentln(columnInstantiation);
                    }
                    out.tabOut();
                    out.indentln("});");
                    String typeDeclaration = String.format("public static final ReferenceColumnType TYPE = new ReferenceColumnType(%s.TABLE);", className);
                    out.indentln(typeDeclaration);
                    out.indentln("");
                }
                for (PsvSchemaRecord escaped : escapedRecords)
                {
                    if (escaped.columnName.length() == 0) { continue; }
                    String memberDeclaration = String.format("public %s %s;", escaped.type, escaped.columnName);
                    out.indentln(memberDeclaration);
                }
                {
                    out.indentln("");
                    out.indentln("@Override");
                    out.indentln("protected void syncRow()");
                    out.indentln("{");
                    out.tabIn();
                    {
                        out.indentln("base.syncRow();");
                    }
                    for (int i = 0; i < escapedRecords.length; ++i)
                    {
                        PsvSchemaRecord escaped = escapedRecords[i];
                        if (escaped.columnName.length() == 0) { continue; }
                        String originalValueExpression = String.format("this.%s", escaped.columnName);
                        String columnValueExpression = String.format("this.__row.getValue(\"%s\")", records.get(i).columnName);
                        String valueParseExpression = getValueParseStatement(escaped.type, originalValueExpression, columnValueExpression);
                        out.indentln(valueParseExpression);
                    }
                    out.tabOut();
                    out.indentln("}");
                }
                out.tabOut();
                out.indentln("}");
                out.indentln("");
            }
            out.tabOut();
            out.indentln("}");
        }
        System.out.println(" done!");
    }
}