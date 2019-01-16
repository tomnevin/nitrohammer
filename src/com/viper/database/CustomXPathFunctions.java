/*
 * -----------------------------------------------------------------------------
 *                      VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * MIT License
 * 
 * Copyright (c) #{classname}.html #{util.YYYY()} Viper Software Services
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 *
 * -----------------------------------------------------------------------------
 */

package com.viper.database;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.viper.database.dao.drivers.SQLConversionTables;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.EnumItem;
import com.viper.database.model.ForeignKey;
import com.viper.database.model.ForeignKeyReference;
import com.viper.database.model.IdMethodType;
import com.viper.database.model.Index;
import com.viper.database.model.Table;

public final class CustomXPathFunctions {

    private final static List<String> DateList = new ArrayList<String>();

    static {
        DateList.add("java.util.Date");
        DateList.add("java.sql.Date");
        DateList.add("java.sql.Time");
        DateList.add("java.sql.Timestamp");
        DateList.add("Date");
        DateList.add("Time");
        DateList.add("Timestamp");
    }

    private final static Map<String, String> ReservedWords = new HashMap<String, String>();

    static {
        ReservedWords.put("interface", "interfce");
        ReservedWords.put("class", "clazz");
    }

    public final static Long toSize(Column column) {
        return (column != null) ? column.getSize() : null;
    }

    public final static String toClass(Column column) {
        return column.getJavaType() + ".class";
    }

    public final static String toGenericClass(Column column) {
        String gtype = "null";
        if (column.getGenericType() != null && column.getGenericType().length() > 0) {
            gtype = column.getGenericType() + ".class";
        }
        return gtype;
    }

    public final static boolean isDecimal(Column column) {
        return "double".equalsIgnoreCase(column.getJavaType()) || "float".equalsIgnoreCase(column.getJavaType());
    }

    public final static String toGenericType(Column column) {
        String gtype = "";
        if (column.getGenericType() != null && column.getGenericType().length() > 0) {
            gtype = "<" + column.getGenericType() + ">";
        }
        return gtype;
    }

    public final static String getParent(String name) {
        int index = name.lastIndexOf('.');
        return (index == -1) ? name : name.substring(0, index);
    }

    public final static String getChild(String name) {
        int index = name.lastIndexOf('.');
        return (index == -1) ? name : name.substring(index + 1);
    }

    public final static String toJavaType(Column column) {
        String classname = column.getJavaType();
        if (isEnum(column)) {
            classname = getChild(classname);
        }
        if (column.getGenericType() != null && !column.getGenericType().isEmpty()) {
            classname = classname + "<" + column.getGenericType() + ">";
        }
        return classname;
    }

    public final static String toEnumType(Column column) {
        String classname = column.getJavaType();
        if (isEnum(column)) {
            classname = getChild(classname);
        }
        if (column.getGenericType() != null && !column.getGenericType().isEmpty()) {
            classname = classname + "<" + column.getGenericType() + ">";
        }
        return classname;
    }

    public final static String toJavaClassName(Column column) {
        String classname = column.getJavaType();
        if (isEnumType(column)) {
            classname = getChild(classname);
        }
        if (column.getGenericType() != null && !column.getGenericType().isEmpty()) {
            classname = classname + "<" + column.getGenericType() + ">";
        }
        return classname;
    }

    public final static String toJavaClass(Database database, Table table, Column column) {
        String classname = column.getJavaType();
        if (isEnumType(column)) {
            classname = getChild(classname);
        }
        return toClassName(classname);
    }

    public final static boolean isArray(Database database, Table table, Column column) {
        String name = toJavaClass(database, table, column);

        return (name != null && name.startsWith("["));
    }

    private final static String toClassName(String classname) {
        try {
            String name = SQLConversionTables.getJavaClassName(classname);
            if (name != null) {
                return name;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "java.lang.String";
    }

    public final static String toHibernateType(Column column) {
        return toJavaType(column);
    }

    public final static String toJavaFX(Column column) {
        return toJavaType(column);
    }

    public final static boolean isJavaFX(Column column) {
        return (toJavaFX(column) != null);
    }

    public final static String toBaseName(Class clazz) {
        String name = clazz.getName();
        return (name.indexOf('.') == -1) ? name : name.substring(name.lastIndexOf('.') + 1);
    }

    public final static String toPropertyName(Column column) {
        if (column == null) {
            return "name";
        }
        String name = column.getJavaType();
        if (name.endsWith("String")) {
            name = "String";
        } else if (name.endsWith("Integer")) {
            name = "Integer";
        } else if (name.endsWith("Boolean")) {
            name = "Boolean";
        } else if (name.endsWith("Float")) {
            name = "Float";
        } else if (name.endsWith("Double")) {
            name = "Double";
        } else if (name.endsWith("byte[]")) {
            name = "Byte";
        }
        if (name.equals("int")) {
            name = "Integer";
        } else {
            name = toJavaNameFromDBName(name, true);
        }
        return (name.indexOf('.') == -1) ? name : name.substring(name.lastIndexOf('.') + 1);
    }

    public final static String toEnumDefinition(Column column) {
        StringBuffer buf = new StringBuffer();
        if (column.getEnumValues() != null) {
            for (EnumItem item : column.getEnumValues()) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                if (item.getValue() == null || item.getValue().trim().length() == 0) {
                    buf.append("UNKNOWN");
                } else {
                    buf.append(item.getValue());
                }
                buf.append('(');
                buf.append('"');
                if (item.getValue() == null || item.getValue().trim().length() == 0) {
                    ;
                } else if (item.getName() != null && item.getName().length() > 0) {
                    buf.append(item.getName());
                } else {
                    buf.append(item.getValue());
                }
                buf.append('"');
                buf.append(')');
            }
        }
        return buf.toString();
    }

    public final static String toDynamicEnumDefinition(Column column) {
        StringBuffer buf = new StringBuffer();
        if (column.getEnumValues() != null) {
            buf.append("\n");
            for (EnumItem item : column.getEnumValues()) {
                buf.append("    public static final " + toEnumType(column) + " " + getName(item));
                buf.append(" = new " + toEnumType(column) + "(\"" + getValue(item) + "\");\n");
            }
        }
        return buf.toString();
    }

    private static final String getValue(EnumItem item) {
        return (item.getName() == null || item.getName().trim().isEmpty()) ? item.getValue() : item.getName();
    }

    private static final String getName(EnumItem item) {
        return (item.getValue() == null || item.getValue().trim().isEmpty()) ? "EXTRA" : item.getValue();
    }

    public final static boolean isEnumType(Column column) {
        return ("enum".equalsIgnoreCase(column.getDataType())
                || column.getEnumValues() != null && column.getEnumValues().size() > 0);
    }

    public final static boolean isEnum(Column column) {
        return (column.getEnumValues() != null && column.getEnumValues().size() > 0);
    }

    public final static boolean hasEnums(Table table) {
        for (Column column : table.getColumns()) {
            if (isEnum(column)) {
                return true;
            }
        }
        return false;
    }

    public final static String toGeneratorClass(IdMethodType idMethod) {
        if (idMethod == IdMethodType.NATIVE) {
            return "native";
        } else if (idMethod == IdMethodType.AUTOINCREMENT) {
            return "org.hibernate.id.IncrementGenerator";
        } else if (idMethod == IdMethodType.ASSIGNED) {
            return "assigned";
        } else if (idMethod == IdMethodType.NONE) {
            return "none";
        }
        return "none";
    }

    public final static String capitalizeFirstLetter(String original) {
        if (original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    public final static String toGetter(Column column) {
        try {
            String name = capitalizeFirstLetter(column.getName());
            String getterName = "get" + name;
            if (column.getJavaType().equals(Boolean.class)) {
                getterName = "is" + name;
            } else if (column.getJavaType().equalsIgnoreCase("boolean")) {
                getterName = "is" + name;
            }
            return getterName;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public final static Method getDeclaredMethod(Class clazz, String methodName) {
        try {
            return clazz.getDeclaredMethod(methodName);
        } catch (Exception ex) {
        }
        return null;
    }

    public final static String duplicateMethodCount(Method methods[], Method method) {
        int counter = 0;
        for (Method m : methods) {
            if (m.getName().equals(method.getName())) {
                counter = counter + 1;
            }
            if (m == method) {
                break;
            }
        }
        return (counter <= 1) ? "" : Integer.toString(counter);
    }

    // -------------------------------------------------------------------------
    //
    // -------------------------------------------------------------------------

    public final static String toJavaNameFromDBName(String name, boolean firstUpperCase) {

        if (ReservedWords.get(name.toLowerCase()) != null) {
            return ReservedWords.get(name.toLowerCase());
        }

        char prev = (firstUpperCase) ? 'a' : 'A';
        StringBuffer buf = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                if (Character.isJavaIdentifierStart(c) == false) {
                    c = '_';
                } else {
                    c = Character.toUpperCase(c);
                }
            } else {
                if (Character.isJavaIdentifierPart(c) == false) {
                    c = '_';
                }
            }
            if (c == '_') {
                // Skip the dash or underscore.
            } else if (prev == '_') {
                buf.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(prev)) {
                buf.append(Character.toLowerCase(c));
            } else {
                buf.append(c);
            }
            prev = c;
        }
        return buf.toString();
    }

    // -------------------------------------------------------------------------
    //
    // -------------------------------------------------------------------------

    public final static String toEnglishFromDBName(String name, boolean firstUpperCase) {

        char prev = (firstUpperCase) ? 'a' : 'A';
        StringBuffer buf = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                c = Character.toUpperCase(c);
            } else {
                if (Character.isJavaIdentifierPart(c) == false) {
                    c = ' ';
                }
            }
            if (c == '_') {
                c = ' ';
                buf.append(' ');
            } else if (prev == ' ') {
                buf.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(prev)) {
                buf.append(Character.toLowerCase(c));
            } else {
                buf.append(c);
            }
            prev = c;
        }
        return buf.toString();
    }

    public final static String toJavaName(Table table) {
        if (table.getJavaName() != null && table.getJavaName().length() > 0) {
            return table.getJavaName();
        }
        if (table.getName() != null && table.getName().length() > 0) {
            return toJavaNameFromDBName(table.getName(), true);
        }
        return "JavaName";
    }

    public final static String toJavaName(Column column) {
        if (column.getName() != null && column.getName().length() > 0) {
            return toJavaNameFromDBName(column.getName(), true);
        }
        return "JavaName";
    }

    public final static String toJavaName(ForeignKey foreignKey) {
        if (foreignKey.getName() != null && foreignKey.getName().length() > 0) {
            return toJavaNameFromDBName(foreignKey.getName(), true);
        }
        if (foreignKey.getForeignTable() != null && foreignKey.getForeignTable().length() > 0) {
            return toJavaNameFromDBName(foreignKey.getForeignTable(), true);
        }
        return "JavaName";
    }

    public final static String toJavaNameFromField(String fieldname) {
        if (fieldname != null && fieldname.length() > 0) {
            return toJavaNameFromDBName(fieldname, true);
        }
        return "JavaName";
    }

    public final static String toBaseClass(Table table) {
        if (table.getBaseClass() != null && table.getBaseClass().length() > 0) {
            return " extends " + table.getBaseClass();
        }
        return "";
    }

    public final static String toFinal(Table table) {
        if (table.isIsFinal()) {
            return " final ";
        }
        return "";
    }

    public final static String toDefaultValue(Column column) {
        if (column.getDefaultValue() == null || column.getDefaultValue().length() == 0) {
            return "";
        }
        if (column.getJavaType().equals("String")) {
            return " = \"" + column.getDefaultValue() + "\"";
        }
        if (isEnumType(column)) {
            if (validEnumType(column.getEnumValues(), column.getDefaultValue())) {
                return " = " + column.getJavaType() + "." + column.getDefaultValue();
            }
        }
        // check for primitive instead
        if (column.getJavaType().equals("int")) {
            return " = " + column.getDefaultValue() + "";
        }
        if (column.getJavaType().equals("boolean")) {
            return " = " + column.getDefaultValue() + "";
        }
        if (column.getJavaType().equals("double")) {
            return " = " + column.getDefaultValue() + "";
        }
        if (column.getJavaType().equals("float")) {
            return " = " + column.getDefaultValue() + "";
        }
        return "";
    }

    public final static String toDisplayDataType(Column column) {
        if ("timestamp".equalsIgnoreCase(column.getDataType())) {
            return "data-type=\"inline-timestamp\"";
        }
        return "";
    }

    private final static boolean validEnumType(List<EnumItem> items, String value) {
        for (EnumItem item : items) {
            if (item.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public final static String toStringValue(Table table) {
        StringBuilder buf = new StringBuilder();
        for (Column column : table.getColumns()) {
            if (column.isPersistent()) {
                if (buf.length() > 0) {
                    buf.append(" + \", \" + ");
                } else {
                    buf.append("\"\" + ");
                }
                if (column.getJavaType().contains("[]")) {
                    buf.append("java.util.Arrays.toString(" + toJavaVariableName(column) + ")");
                } else {
                    buf.append(toJavaVariableName(column));
                }
            }
        }
        if (buf.length() == 0) {
            buf.append("\"\"");
        }
        return buf.toString();
    }

    public final static String toPrimaryKeyVariableName(Table table) {
        if (table.getColumns().size() > 0) {
            for (Column column : table.getColumns()) {
                if (column.isPrimaryKey()) {
                    return toJavaVariableName(column);
                }
            }
            return toJavaVariableName(table.getColumns().get(0));
        }
        return "<" + table.getName() + ">";
    }

    public final static String toPrimaryKeyName(Table table) {
        if (table.getColumns().size() > 0) {
            for (Column column : table.getColumns()) {
                if (column.isPrimaryKey()) {
                    return toJavaName(column);
                }
            }
            return toJavaName(table.getColumns().get(0));
        }
        return "<" + table.getName() + ">";
    }

    public final static String toJavaVariableName(Column column) {
        return Introspector.decapitalize(toJavaName(column));
    }

    public final static String toJavaVariableName(ForeignKey foreignKey) {
        return Introspector.decapitalize(toJavaName(foreignKey));
    }

    public final static String toJavaVariableNameFromField(String name) {
        return Introspector.decapitalize(toJavaNameFromField(name));
    }

    public final static boolean isCustomObject(Column column) {
        return (column.getJavaType().startsWith("com.") && !"enum".equalsIgnoreCase(column.getDataType())
                && column.getEnumValues().size() == 0);
    }

    public final static boolean isList(Column column) {
        return (column.getJavaType().equals("List") || column.getJavaType().equals("java.util.List")
                || column.getJavaType().equals("ArrayList") || column.getJavaType().equals("java.util.ArrayList"));
    }

    public final static boolean isMap(Column column) {
        return (column.getJavaType().equals("Map") || column.getJavaType().equals("java.util.Map")
                || column.getJavaType().equals("HashMap") || column.getJavaType().equals("java.util.HashMap"));
    }

    public final static String toGeneratorClass(Column column) {
        IdMethodType idMethod = column.getIdMethod();
        if (idMethod == IdMethodType.NATIVE) {
            return "native";
        } else if (idMethod == IdMethodType.AUTOINCREMENT) {
            return "org.hibernate.id.IncrementGenerator";
        } else if (idMethod == IdMethodType.ASSIGNED) {
            return "assigned";
        } else if (idMethod == IdMethodType.NONE) {
            return "none";
        }
        return "none";
    }

    public final static String toTableAnnotation(String databaseName, Table table) {

        StringBuffer buf = new StringBuffer();
        append(buf, "databaseName", databaseName);
        append(buf, "name", table.getName());
        append(buf, "tableName", table.getTableName());
        append(buf, "tableType", table.getTableType().toString().toLowerCase());
        append(buf, "iterations", table.getIterations());
        append(buf, "isSchemaUpdatable", table.isIsSchemaUpdatable());
        append(buf, "isLargeTable", table.isIsLargeTable());
        append(buf, "isReportTable", table.isIsReportTable());
        append(buf, "isMonitorChanges", table.isIsMonitorChanges()); 

        if (!isEmpty(table.getValidator())) {
            append(buf, "validator", table.getValidator().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getSqlSelect())) {
            append(buf, "sqlSelect", table.getSqlSelect().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getSqlUpdate())) {
            append(buf, "sqlUpdate", table.getSqlUpdate().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getSqlInsert())) {
            append(buf, "sqlInsert", table.getSqlInsert().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getSqlDelete())) {
            append(buf, "sqlDelete", table.getSqlDelete().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getSqlSize())) {
            append(buf, "sqlSize", table.getSqlSize().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getQueryClassName())) {
            append(buf, "queryClassName", table.getQueryClassName().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getConverter())) {
            append(buf, "converter", table.getConverter().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getBeanGenerator())) {
            append(buf, "beanGenerator", table.getBeanGenerator().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getSqlGenerator())) {
            append(buf, "sqlGenerator", table.getSqlGenerator().replaceAll("(\\r|\\n)", " "));
        }
        if (!isEmpty(table.getSeedFilename())) {
            append(buf, "seedFilename", table.getSeedFilename().replaceAll("(\\r|\\n)", " "));
        }

        return buf.toString();
    }

    private final static boolean isEmpty(List items) {
        return (items == null || items.size() == 0);
    }

    private final static boolean isEmpty(String str) {
        return (str == null || str.trim().length() == 0);
    }

    public final static String toColumnAnnotation(Table table, Column column) {

        StringBuffer buf = new StringBuffer();
        if (!isEmpty(column.getField())) {
            append(buf, "field", column.getField());
        } else {
            append(buf, "field", column.getName());
        }
        append(buf, "name", toJavaVariableName(column));
        if (!isEmpty(column.getGenericType())) {
            append(buf, "genericType", column.getGenericType());
        }
        if (!isEmpty(column.getJavaType())) {
            append(buf, "javaType", column.getJavaType());
        }
        if (!isEmpty(column.getTableName())) {
            append(buf, "tableName", column.getTableName());
        }
        append(buf, "logicalType", column.getLogicalType());
        append(buf, "dataType", column.getDataType());

        if (column.isPrimaryKey()) {
            append(buf, "primaryKey", true);
            if (column.getIdMethod() != null) {
                append(buf, "idMethod", column.getIdMethod().value());
            }
        }
        if (column.isNaturalKey()) {
            append(buf, "naturalKey", true);
        } else if (column.isUnique()) {
            append(buf, "unique", true);
        }
        if (!column.isPersistent()) {
            append(buf, "persistent", false);
        }
        if (!column.isOptional()) {
            append(buf, "optional", false);
        }
        if (column.isRequired()) {
            append(buf, "required", true);
        }
        if (column.isIsNullable()) {
            append(buf, "isNullable", true);
        }
        if (column.isSecure()) {
            append(buf, "secure", true);
        }
        if (column.getSize() > 0) {
            if (append(buf, "size", column.getSize())) {
                buf.append("L");
            }
        }
        if (column.getOrder() > 0) {
            append(buf, "order", column.getOrder());
        }
        if (column.getEnumValues() != null && column.getEnumValues().size() > 0) {
            append(buf, "enumValue", toList(column.getEnumValues()));
        }
        append(buf, "decimalSize", column.getDecimalSize());
        append(buf, "validator", column.getValidator());
        append(buf, "converter", column.getConverter());
        append(buf, "renderer", column.getRenderer());
        append(buf, "options", column.getOptions());
        append(buf, "defaultValue", column.getDefaultValue());
        append(buf, "minimumValue", column.getMinimumValue());
        append(buf, "maximumValue", column.getMaximumValue());
        append(buf, "valuesClassname", column.getValuesClassname());
        append(buf, "columnVisibilty", column.getColumnVisibility().value());
        append(buf, "validationMessage", column.getValidationMessage());
        append(buf, "pattern", column.getPattern());

        return buf.toString();
    }

    private final static List<String> toList(List<EnumItem> items) {
        if (items == null || items.size() == 0) {
            return new ArrayList<String>();
        }

        List<String> list = new ArrayList<String>(items.size());
        for (EnumItem item : items) {
            list.add(item.getValue());
        }
        Collections.sort(list);
        return list;
    }

    public final static String toIndexAnnotation(Table table, Index index) {

        List<String> columnNames = listColumnNames(table, index);

        StringBuffer buf = new StringBuffer();
        append(buf, "name", index.getName());
        append(buf, "columns", columnNames);
        append(buf, "indexClass", index.getIndexClass().toString());
        append(buf, "indexType", index.getIndexType().toString());
        append(buf, "primary", index.isPrimary());
        append(buf, "editable", index.isEditable());
        return buf.toString();
    }

    public final static List<String> listColumnNames(Table table, Index index) {
        List<String> columnNames = new ArrayList<String>();

        for (Column column : table.getColumns()) {
            if (index.getName().equalsIgnoreCase(column.getIndexName())) {
                columnNames.add(column.getName());
            }
        }

        return columnNames;
    }

    public final static List<Column> listSortedColumns(Table table) {
        List<Column> columns = new ArrayList<Column>();
        columns.addAll(table.getColumns());
        Collections.sort(columns, new ColumnNameComparator());
        return columns;
    }

    public static class ColumnNameComparator implements Comparator<Column> {
        @Override
        public int compare(Column o1, Column o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public final static String toForeignKeyAnnotation(ForeignKey foreignKey) {

        StringBuffer buf = new StringBuffer();
        append(buf, "name", foreignKey.getName());
        append(buf, "constraintName", foreignKey.getConstraintName());
        append(buf, "localDatabase", foreignKey.getLocalDatabase());
        append(buf, "localColumns", toLocalColumns(foreignKey.getForeignKeyReferences()));
        append(buf, "foreignDatabase", foreignKey.getForeignDatabase());
        append(buf, "foreignTable", foreignKey.getForeignTable());
        append(buf, "foreignColumns", toForeignColumns(foreignKey.getForeignKeyReferences()));
        append(buf, "unique", foreignKey.isUnique());
        return buf.toString();
    }

    public final static String toInterface(Table table) {

        StringBuffer buf = new StringBuffer();
        buf.append(" implements java.io.Serializable");
        if (table.getInterface() != null && table.getInterface().length() > 0) {
            buf.append(", ");
            buf.append(table.getInterface());
        }
        return buf.toString();
    }

    public final static String toJohnzonAnnotation(Column column) {
        if (column.getJavaType() != null) {
            if (DateList.contains(column.getJavaType())) {
                return "@org.apache.johnzon.mapper.JohnzonConverter(org.apache.johnzon.mapper.converter.TimestampAdapter.class)";
            }
        }
        return "";
    }

    private final static String toString(List<String> columns, boolean inQuotes) {
        StringBuilder buf = new StringBuilder();
        for (String column : columns) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            if (inQuotes) {
                buf.append('"');
                buf.append(column);
                buf.append('"');
            } else {
                buf.append(column);
            }
        }
        return buf.toString();
    }

    private final static String toLocalColumns(List<ForeignKeyReference> columns) {
        StringBuilder buf = new StringBuilder();
        for (ForeignKeyReference column : columns) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(column.getLocalColumn());
        }
        return buf.toString();
    }

    private final static String toForeignColumns(List<ForeignKeyReference> columns) {
        StringBuilder buf = new StringBuilder();
        for (ForeignKeyReference column : columns) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(column.getForeignColumn());
        }
        return buf.toString();
    }

    public final static String toLine(String token, String line) {
        if (line != null && line.length() > 0) {
            return token + line;
        }
        return "";
    }

    private final static boolean append(StringBuffer buf, String name, Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String && ((String) value).trim().length() == 0) {
            return false;
        }
        if (buf.length() > 0) {
            buf.append(", ");
        }
        buf.append(name);
        buf.append(" = ");
        if (value instanceof List) {
            buf.append("{");
            buf.append(toString((List) value, true));
            buf.append("}");

        } else if (value instanceof String) {
            buf.append("\"");
            buf.append(value.toString());
            buf.append("\"");
        } else {
            buf.append(value.toString());
        }
        return true;
    }
}
