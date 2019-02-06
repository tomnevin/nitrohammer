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

package com.viper.rest.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.viper.database.annotations.Column;
import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.converters.Converters;
import com.viper.database.model.Database;
import com.viper.database.rest.model.ColumnDef;
import com.viper.database.rest.model.ComponentDef;
import com.viper.database.rest.model.Form;
import com.viper.database.rest.model.HeaderDef;
import com.viper.database.rest.model.TableDef;
import com.viper.database.utils.FileUtil;
import com.viper.database.utils.JSONUtil;
import com.viper.rest.LocaleUtil; 

public class FormRestService implements RestServiceInterface {

    private static final String DependencyDirectory = "res:/templates/";
    private static Map<String, Database> cache = new HashMap<String, Database>();
    private static Map<String, String> cache1 = new HashMap<String, String>();

    private static final String getDatabaseName() throws Exception {
        return com.viper.database.utils.ResourceUtil.getResource("DATABASE_LOCATOR", "test");
    }

    @Override
    public <T> Response query(Class<T> clazz, Locale locale, List<String> whereClause) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        T bean = database.query(clazz, (Object[]) whereClause.toArray());

        return Response.ok(JSONUtil.toJSON(bean), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response query(Class<T> clazz, Locale locale, String key, String value) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        T bean = database.query(clazz, key, value);

        return Response.ok(JSONUtil.toJSON(bean), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response queryList(Class<T> clazz, Locale locale, String key, String value) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        List<T> beans = getBeansByForeignKeys(database, clazz.getName(), clazz, key, value);

        Form form =  form(locale, clazz.getName(), beans);

        return Response.ok(JSONUtil.toJSON(form), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response queryList(Class<T> clazz, Locale locale, MultivaluedMap<String, String> queryParams) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Response queryAll(Class<T> clazz, Locale locale) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        List<T> beans = database.queryAll(clazz);

        Form form =  form(locale, clazz.getName(), beans);

        return Response.ok(JSONUtil.toJSON(form), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response update(Class<T> clazz, String request) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        T bean = Converters.convert(clazz, request);

        database.insert(bean);

        return Response.ok(JSONUtil.toJSON(bean), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response update(Class<T> clazz, MultivaluedMap<String, String> queryParams) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Response createItem(Class<T> clazz, String request) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        T bean = Converters.convert(clazz, request);

        database.insert(bean);

        return Response.ok(JSONUtil.toJSON(bean), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response createItems(Class<T> clazz, String request) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        List<T> beans = Converters.convertToList(clazz, request);

        database.insertAll(beans);

        return Response.ok(JSONUtil.toJSON(beans), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response deleteItem(Class<T> clazz, String key, String value) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        database.delete(clazz, key, value);

        return Response.ok().build();
    }

    @Override
    public <T> Response deleteItem(Class<T> clazz, String request) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        T bean = Converters.convert(clazz, request);

        database.delete(bean);

        return Response.ok().build();
    }

    public static final <T> HeaderDef header(Locale locale, Class<T> clazz) throws Exception {

        ResourceBundle resources = LocaleUtil.getBundle(locale);

        String tablename = DatabaseUtil.getTableName(clazz);
        List<Column> columns = DatabaseUtil.getColumnAnnotations(clazz);

        HeaderDef headers = loadTables(makeTableFilename(tablename));

        // If no definition provided use all the columns for the table.
        boolean useAllColumns = false;
        if (headers.getColumnDefs().size() == 0) {
            useAllColumns = true;
        }
        List<ColumnDef> columnDefs = headers.getColumnDefs();

        for (Column column : columns) {
            String columnName = toFullColumnName(clazz, column);
            String headerName = LocaleUtil.getString(resources, column.field());

            ColumnDef columnDef = findColumnDef(columnDefs, columnName);
            if (useAllColumns) {
                columnDef = new ColumnDef();
                columnDef.setHeaderName(headerName);
                columnDef.setField(columnName);
                columnDefs.add(columnDef);

            } else if (columnDef != null) {
                columnDef.setHeaderName(headerName);
            }
        }
        return headers;
    }

    public static final <T> HeaderDef data(Locale locale, Class<T> clazz, List<T> beans) throws Exception {

        HeaderDef data = new HeaderDef();

        data.getRowData().addAll(beans);

        // figure out the actual number of rows in table.
        data.setLastRow(beans.size());
        return data;
    }

    public final static <T> HeaderDef table(Locale locale, Class<T> clazz, List<T> beans) throws Exception {

        HeaderDef table = header(locale, clazz);

        table.getRowData().addAll(beans);

        // figure out the actual number of rows in table.
        table.setLastRow(beans.size());
        table.getRowData().addAll(beans);

        // figure out the actual number of rows in table.
        table.setLastRow(beans.size());

        return table;
    }

    public final static <T> Form form(Locale locale, String formname, List<T> beans) throws Exception {

        Form root = new Form();
        root.setTitle(formname);

        for (T bean : beans) {

            List<Column> columns = DatabaseUtil.getColumnAnnotations(bean.getClass());
            for (Column column : columns) {

                String classname = bean.getClass().getName();
                String primnarykeyname = DatabaseUtil.getPrimaryKeyName(bean.getClass());
                Object primnarykeyvalue = DatabaseUtil.getPrimaryKeyValue(bean);
                String databasename = DatabaseUtil.getDatabaseName(bean.getClass());
                String tablename = DatabaseUtil.getTableName(bean.getClass());
                String columnName = toFullColumnName(bean.getClass(), column);
                String value = DatabaseUtil.getString(bean, column.name());
                Object obj = DatabaseUtil.get(bean, column.name());
                String url = "classes/" + formname + "/js/datatable/form";

                ComponentDef component = new ComponentDef();

                component.setKey(columnName);
                component.setHidden(false);
                component.setDefaultValue(value);
                component.setType(toComponentType(column));
                component.setLabel(columnName);
                component.setPlaceHolder("{prompt}");
                component.setMaximumLength(column.size());
                component.setMinimumValue(column.minimumValue());
                component.setMaximumValue(column.maximumValue());
                component.setRequired(column.required());
                component.setPersistent(column.persistent());
                component.setLogicalType(column.logicalType());
                component.setDataType(column.dataType());
                component.setValidate(column.validator());
                component.setAutocomplete("on");
                component.setChecked("");
                component.setPattern("");
                component.setCellRenderer("");
                component.setFilter("");

                if (obj != null && obj.getClass().isEnum()) {
                    Object[] enumList = obj.getClass().getEnumConstants();
                    component.setOptions(toString(enumList));
                }

                root.getComponents().add(component);

                TableDef table = findByClassname(root.getTables(), classname);
                if (table == null) {
                    table = new TableDef();

                    table.setClassname(classname);
                    table.setDatabasename(databasename);
                    table.setTablename(tablename);
                    table.setUrl(url);
                    table.setPrimaryKey(primnarykeyname);
                    table.setPrimaryValue(primnarykeyvalue);

                    root.getTables().add(table);
                }
            }
        }
        return root;
    }

    public final static <T> String toWhereClause(MultivaluedMap<String, String> formParams, Class<T> clazz) {

        StringBuilder whereClause = new StringBuilder();

        if (formParams.size() > 0) {
            if (formParams.containsKey("startRow") && formParams.containsKey("endRow")) {
                int startRow = Integer.parseInt(formParams.getFirst("startRow"));
                int endRow = Integer.parseInt(formParams.getFirst("endRow"));
                int length = endRow - startRow;
                whereClause.append(" limit ");
                whereClause.append(Integer.toString(startRow));
                whereClause.append(", ");
                whereClause.append(Integer.toString(length));
            }
        }

        List<String> searchRequest = formParams.get("search");
        if (searchRequest != null) {

            boolean isFirst = true;
            List<Column> columns = DatabaseUtil.getColumnAnnotations(clazz);
            for (Column column : columns) {
                if (column.naturalKey() || column.unique()) {
                    if (whereClause.length() == 0) {
                        whereClause.append(" where match (");
                    }
                    whereClause.append(column.field());
                    isFirst = false;
                }
            }

            if (!isFirst) {
                whereClause.append(") against ");
                whereClause.append(" ('");
                whereClause.append(searchRequest);
                whereClause.append(" ')");
            }
        }
 

        return whereClause.toString();
    }

    public final static <T> List getBeansByForeignKeys(DatabaseInterface dao, String formname, Class<T> tableClazz, String key,
            String value) throws Exception {

        Database database = loadForm(makeDependenciesFilename(formname));

        return DatabaseUtil.getBeansByForeignKeys(dao, database, tableClazz, key, value);

    }

    private final static String makeDependenciesFilename(String formname) {
        return DependencyDirectory + "dependencies/" + formname.toLowerCase() + ".dependencies.xml";
    }

    private final static String makeTableFilename(String formname) {
        String filename = DependencyDirectory + "tables/" + formname.toLowerCase() + ".json";
        return filename;
    }

    private final static HeaderDef loadTables(String filename) {
        try {
            if (cache1.containsKey(filename)) {
                return JSONUtil.fromJSON(HeaderDef.class, cache1.get(filename));
            }

            String str = FileUtil.readFile(filename);
            cache1.put(filename, str);

            return JSONUtil.fromJSON(HeaderDef.class, str);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new HeaderDef();
    }

    private final static Database loadForm(String filename) {
        if (cache.containsKey(filename)) {
            return cache.get(filename);
        }

        Database database = null;
        try {

            database = DatabaseMapper.readDatabase(filename);

            cache.put(filename, database);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return database;
    }

    public final static <T> String getPrimaryKeyName(Class<T> clazz) {

        List<Column> columns = DatabaseUtil.getColumnAnnotations(clazz);
        if (columns != null && columns.size() > 0) {
            for (Column column : columns) {
                if (column.primaryKey()) {
                    return column.name();
                }
            }
            return columns.get(0).name();
        }
        return null;
    }

    private static final TableDef findByClassname(List<TableDef> tables, String value) {
        for (TableDef table : tables) {
            if (table.getClassname().equalsIgnoreCase(value)) {
                return table;
            }
        }
        return null;
    }

    private final static String toType(String javatype) {
        if (javatype.toLowerCase().contains("date") || javatype.toLowerCase().contains("time")) {
            return "date";
        }
        if (javatype.toLowerCase().contains("int")) {
            return "num";
        }
        if (javatype.toLowerCase().contains("float")) {
            return "num";
        }
        if (javatype.toLowerCase().contains("double")) {
            return "num";
        }
        return "string";
    }

    // DataTypeMapping.put("array", null);
    // DataTypeMapping.put("bit", new ConversionType("Boolean", "boolean"));
    // DataTypeMapping.put("boolean", new ConversionType("Boolean", "boolean"));
    // DataTypeMapping.put("bigint", new ConversionType("Long", "long"));
    // DataTypeMapping.put("binary", new ConversionType("Byte[]", "byte[]"));
    // DataTypeMapping.put("blob", new ConversionType("java.sql.Blob",
    // "java.sql.Blob"));
    // DataTypeMapping.put("char", new ConversionType("String", "String"));
    // DataTypeMapping.put("clob", new ConversionType("java.sql.Clob",
    // "java.sql.Clob"));
    // DataTypeMapping.put("datalink", null);
    // DataTypeMapping.put("date", new ConversionType("Long", "long"));
    // DataTypeMapping.put("datetime", new ConversionType("Long", "long"));
    // DataTypeMapping.put("decimal", new ConversionType("Double", "double"));
    // DataTypeMapping.put("distinct", null);
    // DataTypeMapping.put("double", new ConversionType("Double", "double"));
    // DataTypeMapping.put("enum", new ConversionType("enum", "enum"));
    // DataTypeMapping.put("float", new ConversionType("Float", "float"));
    // DataTypeMapping.put("int", new ConversionType("Integer", "int"));
    // DataTypeMapping.put("integer", new ConversionType("Integer", "int"));
    // DataTypeMapping.put("longblob", new ConversionType("Byte[]", "byte[]"));
    // DataTypeMapping.put("longnvarchar", new ConversionType("String", "String"));
    // DataTypeMapping.put("longtext", new ConversionType("String", "String"));
    // DataTypeMapping.put("longvarchar", new ConversionType("String", "String"));
    // DataTypeMapping.put("longvarbinary", new ConversionType("Byte[]", "byte[]"));
    // DataTypeMapping.put("mediumint", new ConversionType("Integer", "int"));
    // DataTypeMapping.put("nchar", new ConversionType("String", "String"));
    // DataTypeMapping.put("null", null);
    // DataTypeMapping.put("numeric", null);
    // DataTypeMapping.put("nvarchar", new ConversionType("String", "String"));
    // DataTypeMapping.put("nclob", new ConversionType("String", "String"));
    // DataTypeMapping.put("other", null);
    // DataTypeMapping.put("object", new ConversionType("Object", "Object"));
    // DataTypeMapping.put("real", new ConversionType("Double", "double"));
    // DataTypeMapping.put("ref", null);
    // DataTypeMapping.put("rowid", null);
    // DataTypeMapping.put("set", new ConversionType("Object", "Object"));
    // DataTypeMapping.put("smallint", new ConversionType("Integer", "int"));
    // DataTypeMapping.put("sqlxml", null);
    // DataTypeMapping.put("struct", null);
    // DataTypeMapping.put("time", new ConversionType("Long", "long"));
    // DataTypeMapping.put("timestamp", new ConversionType("Long", "long"));
    // DataTypeMapping.put("tinyint", new ConversionType("Integer", "int"));
    // DataTypeMapping.put("text", new ConversionType("String", "String"));
    // DataTypeMapping.put("varchar", new ConversionType("String", "String"));
    // DataTypeMapping.put("varbinary", new ConversionType("Byte[]", "byte[]"));

    private final static String toComponentType(Column column) {
        if (column.logicalType() != null && column.logicalType().length() > 0) {

            if (column.logicalType().contains("password")) {
                return "password";
            }
            if (column.logicalType().contains("password")) {
                return "password";
            }
            if (column.logicalType().contains("color")) {
                return "color";
            }
            if (column.logicalType().contains("email")) {
                return "email";
            }
            if (column.logicalType().contains("phone")) {
                return "tel";
            }
            if (column.logicalType().contains("url")) {
                return "url";
            }
            if (column.logicalType().contains("month")) {
                return "month";
            }
            if (column.logicalType().contains("week")) {
                return "week";
            }

            // number
            // range
            // search

        }
        String datatype = column.dataType();
        if (datatype.toLowerCase().contains("date")) {
            return "date";
        }
        if (datatype.toLowerCase().contains("time")) {
            return "time";
        }
        if (datatype.toLowerCase().contains("timestamp")) {
            return "datetime-local";
        }
        if (datatype.toLowerCase().contains("enum")) {
            return "combobox";
        }
        if (datatype.toLowerCase().contains("boolean")) {
            return "checkbox";
        }
        if (datatype.toLowerCase().contains("radio")) {
            return "radio";
        }

        return "text";
    }

    private final static <T> String toFullColumnName(Class<T> clazz, Column column) {

        return clazz.getSimpleName() + "." + column.field();
    }

    private final static String toString(Object[] values) {
        StringBuilder buf = new StringBuilder();

        for (Object value : values) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(value.toString());
        }
        return buf.toString();
    }

    private final static ColumnDef findColumnDef(List<ColumnDef> columnDefs, String columnName) {
        for (ColumnDef columnDef : columnDefs) {
            if (columnName.equalsIgnoreCase(columnDef.getField())) {
                return columnDef;
            }
        }
        return null;
    }

}
