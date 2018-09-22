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

package com.viper.database.dao;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;

import com.viper.database.annotations.Column;
import com.viper.database.annotations.Table;
import com.viper.database.dao.converters.ConverterUtils;
import com.viper.database.dao.converters.Converters;
import com.viper.database.dao.drivers.SQLConversionTables;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Cell;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.EnumItem;
import com.viper.database.model.Param;
import com.viper.database.model.Row;

public final class DatabaseJDBC implements DatabaseInterface, DatabaseSQLInterface {

    private final static Logger log = Logger.getLogger(DatabaseJDBC.class.getName());

    private final static String QUOTE_NAME = "`";
    private final static String QUOTE_NAME_H2 = "\"";
    private final static String QUOTE_VALUE = "'";
    private final static int MAX_COLUMN_NAME_LENGTH = 64;

    private final static SimpleDateFormat updaterDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final static Random random = new Random();
    private final static String RANDOM_CHARS = "ABCDEFGHIJKLMONPQRSTUVWXYZabcdefghijklmonpqrstuvwxyz0123456789";

    private String variant = "mysql";
    private DatabaseConnection dbc = new DatabaseConnection();
    private Map<Class, String> checksums = new HashMap<Class, String>();

    /**
     * Given the configuration map of parameters, get an instance of Database access
     * using the supplied configuration parameters. The configuration parameters are
     * defined in the DatabaseFactory class.
     * 
     * @param dbc
     *            the map of configuration parameters.
     * @return new Database instance.
     */
    public final static synchronized DatabaseInterface getInstance(DatabaseConnection dbc) {
        return new DatabaseJDBC(dbc);
    }

    private DatabaseJDBC(DatabaseConnection dbc) {
        this.dbc = dbc;

        String driverClass = dbc.getDriver();
        if (driverClass == null || driverClass.trim().length() == 0) {
            driverClass = "com.mysql.jdbc.Driver";
        }
        dbc.setDriver(driverClass);

        log.info("Starting: initDatabases: " + dbc.getPackageNames());

        String databaseUrl = dbc.getDatabaseUrl();
        if (databaseUrl.toLowerCase().contains("h2:")) {
            variant = "h2";
        }

        if (dbc.isCreateDatabase()) {
            for (String packagename : dbc.getPackageNames()) {
                createDatabase(packagename);
            }
        }

        for (String packagename : dbc.getPackageNames()) {
            initializeDatabase(packagename);
        }

        log.info("Leaving: initDatabases:");
    }

    private Connection getConnection() throws Exception {

        // private final static String DEFAULT_PROVIDER_URL =
        // "file:///tmp/JNDI";
        // private final static String DEFAULT_FACTORY_CLASSNAME =
        // "com.sun.jndi.fscontext.RefFSContextFactory";
        // Hashtable<String, String> env = new Hashtable<String, String>();
        // env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_FACTORY_CLASSNAME);
        // env.put(Context.PROVIDER_URL, DEFAULT_PROVIDER_URL);

        DataSource source = ConnectionFactory.getDataSource(dbc);

        Connection connection = source.getConnection();
        connection.setAutoCommit(true);

        return connection;
    }

    private void initializeDatabase(String packagename) {
        log.info("initializeDatabase: " + packagename);

        Connection connection = null;
        Map<String, String> cache = new HashMap<String, String>();

        try {
            List<Class> classes = DatabaseUtil.listDatabaseTableClasses(packagename, null);
            for (Class clazz : classes) {
                try {
                    Table table = DatabaseUtil.getTableAnnotation(clazz);
                    if (table.isSchemaUpdatable()) {
                        if (connection == null) {
                            connection = getConnection();

                            listTables(cache, connection, toAliasSchemaName(clazz));
                        }

                        constructDBTable(cache, connection, clazz);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    log.throwing("", "Problems Creating Table: " + clazz, ex);
                }
            }
        } finally {
            release(connection);
        }
    }

    private void release(Connection connection) {
        try {
            if (connection != null) {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
                connection.close();
            }
        } catch (Exception e) {
            log.throwing("Can't close connection ", "", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @note currently this method is not implemented.
     */
    @Override
    public void release() {

        try {
            ConnectionFactory.release();
        } catch (Exception e) {
            log.throwing("Can't close connection ", "", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public <T> long size(Class<T> clazz) throws Exception {

        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from ");
        sql.append(toFullName(clazz));

        ResultSet rs = null;
        Statement stmt = null;
        Connection connection = null;

        try {
            log.fine("read: sql=" + sql);

            connection = getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql.toString());
            rs.next();

            return rs.getLong(1);

        } catch (SQLException e) {
            throw new Exception(sql.toString(), e);

        } finally {
            close(rs);
            close(stmt);
            release(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<String> listDatabases() {
        List<String> items = new ArrayList<String>();

        Connection connection = null;
        try {
            connection = getConnection();
            items = listDatabases(null, connection);
        } catch (Exception ex) {
            log.throwing("ERROR: listDatabases", "", ex);
        } finally {
            release(connection);
        }
        return items;
    }

    private List<String> listDatabases(Map<String, String> cache, Connection connection) {
        List<String> items = new ArrayList<String>();
        ResultSet rs = null;

        try {
            DatabaseMetaData dmd = connection.getMetaData();

            if (!"h2".equalsIgnoreCase(variant)) {
                rs = dmd.getCatalogs();
                while (rs.next()) {
                    String dbname = rs.getString("TABLE_CAT");
                    items.add(dbname);
                    if (cache != null) {
                        cache.put(dbname.toLowerCase(), dbname);
                    }
                }
            } else {
                rs = dmd.getSchemas();
                while (rs.next()) {
                    String dbname = rs.getString("TABLE_SCHEM");
                    items.add(dbname);
                    if (cache != null) {
                        cache.put(dbname.toLowerCase(), dbname);
                    }
                }
            }
        } catch (Exception ex) {
            log.throwing("ERROR: listDatabases: ", "", ex);
        } finally {
            close(rs);
        }
        return items;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<String> listTables(String databasename) {
        List<String> items = new ArrayList<String>();
        Connection connection = null;
        try {
            connection = getConnection();
            items = listTables(null, connection, databasename);
        } catch (Exception ex) {
            log.throwing("ERROR: listTables: databasename=" + databasename, "", ex);
        } finally {
            release(connection);
        }
        return items;
    }

    private List<String> listTables(Map<String, String> cache, Connection connection, String databasename) {
        List<String> items = new ArrayList<String>();
        ResultSet rs = null;

        String schemaname = toAliasSchemaName(databasename);

        try {
            DatabaseMetaData dmd = connection.getMetaData();
            if (!"h2".equalsIgnoreCase(variant)) {
                rs = dmd.getTables(schemaname, null, "%", null);
            } else {
                rs = dmd.getTables(null, schemaname, "%", null);
            }
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                items.add(tableName);

                if (cache != null) {
                    String name = schemaname.toLowerCase() + "." + tableName.toLowerCase();
                    cache.put(name, tableName);
                }
            }
        } catch (Exception ex) {
            log.throwing("ERROR: listTables: databasename=" + databasename, "", ex);
        } finally {
            close(rs);
        }
        return items;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<String> listColumns(Class<T> clazz) {
        List<String> items = new ArrayList<String>();
        Connection connection = null;

        try {
            connection = getConnection();
            items = listColumns(connection, clazz);
        } catch (Exception ex) {
            log.throwing("ERROR: listColumns: class " + clazz.getName(), "", ex);
        } finally {
            release(connection);
        }
        return items;
    }

    private <T> List<String> listColumns(Connection connection, Class<T> clazz) {
        List<String> items = new ArrayList<String>();
        ResultSet rs = null;

        String databasename = toAliasSchemaName(clazz);
        String tablename = DatabaseUtil.getTableName(clazz);

        try {
            DatabaseMetaData dmd = connection.getMetaData();
            if (!"h2".equalsIgnoreCase(variant)) {
                rs = dmd.getColumns(databasename, null, tablename, "%");
            } else {
                rs = dmd.getColumns(null, databasename, tablename, "%");
            }
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME").toString().toLowerCase();
                items.add(columnName);
            }
        } catch (Exception ex) {
            log.throwing("ERROR: listColumns: databasename=" + databasename + ", tablename=" + tablename, "", ex);
        } finally {
            close(rs);
        }
        return items;
    }

    /**
     * {@inheritDoc}
     * 
     * Note: currently this method is not implemented.
     */
    @Override
    public <T> boolean hasChanged(Class<T> clazz) {
        boolean changed = false;
        try {
            String checksum = getChecksum(clazz);
            if (checksum == null) {
                changed = true;
            } else {

                String current = checksums.get(clazz);
                checksums.put(clazz, checksum);

                if (current == null) {
                    changed = true;
                } else {
                    changed = (!checksum.equals(current));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (changed) {
            log.info("FYI: Table change status, changed=" + changed + "," + clazz.getName());
        }
        return changed;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void createDatabase(String packagename) {

        List<Class> classes = DatabaseUtil.listDatabaseTableClasses(packagename, null);
        for (Class clazz : classes) {
            try {
                create(clazz);
            } catch (Exception ex) {
                log.throwing("", "Problems Creating Table: " + clazz, ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void create(Class<T> tableClass) throws Exception {
        Connection connection = null;

        try {
            connection = getConnection();
            Map<String, String> cache = new HashMap<String, String>();

            constructDBTable(cache, connection, tableClass);
        } finally {
            release(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> T query(Class<T> clazz, Object... pairs) throws Exception {

        String sql = buildSQL(clazz, pairs);

        List<T> items = read(clazz, sql);
        return (items == null || items.size() == 0) ? null : items.get(0);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Object... pairs) throws Exception {
        return read(clazz, buildSQL(clazz, pairs));
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Predicate<T> filter) throws Exception {
        List<T> results = new ArrayList<T>();

        int pageno = 0;
        int limit = 50000;
        while (true) {
            List<T> beans = queryList(clazz, PAGENO_KEY, pageno, PAGESIZE_KEY, limit);
            for (T bean : beans) {
                if (filter.apply(bean)) {
                    results.add(bean);
                }
            }
            if (beans.size() < limit) {
                break;
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryAll(Class<T> clazz) throws Exception {
        return queryList(clazz);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> T update(T bean) throws Exception {
        return insert(bean);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> T insert(T bean) throws Exception {
        if (bean == null) {
            return bean;
        }

        Connection connection = null;
        try {
            connection = getConnection();
            List<String> columns = addDefaultColumns(connection, bean);

            updateInternal(connection, bean, columns);
            return bean;
        } finally {
            release(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void insertAll(List<T> beans) throws Exception {
        if (beans == null || beans.size() == 0) {
            return;
        }

        Connection connection = null;
        try {
            connection = getConnection();

            List<String> columns = addDefaultColumns(connection, beans.get(0));

            for (T bean : beans) {
                updateInternal(connection, bean, columns);
            }
        } finally {
            release(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> executeQuery(Class<T> clazz, String sql, Object... params) throws Exception {
        return new ArrayList();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public int executeUpdate(String sql, Object... params) throws Exception {
        return 0;
    }

    private <T> void updateInternal(Connection connection, T bean, List<String> columnNames) throws Exception {

        if (DatabaseUtil.isValidPrimaryKeyValue(bean)) {
            if (write(connection, updateSQL(bean, columnNames), Statement.NO_GENERATED_KEYS) != null) {
                return;
            }
        }

        Column assignedColumn = DatabaseUtil.getAssignedColumn(bean.getClass());
        if (assignedColumn != null) {
            Method method = toMethod(bean.getClass(), assignedColumn.field());
            DatabaseUtil.setValue(bean, assignedColumn.field(), generateKeyValue(method, assignedColumn));
        }

        Column autoincrementColumn = DatabaseUtil.getAutoIncrementColumn(bean.getClass());
        if (autoincrementColumn != null) {
            Object keyValue = write(connection, insertSQL(bean, columnNames), Statement.RETURN_GENERATED_KEYS);
            if (keyValue == null) {
                log.info("Failed to insert bean: " + DatabaseUtil.getDatabaseName(bean.getClass()) + "."
                        + DatabaseUtil.getTableName(bean.getClass()));
            }
            DatabaseUtil.setValue(bean, autoincrementColumn.field(), keyValue);

        } else {

            write(connection, insertSQL(bean, columnNames), Statement.NO_GENERATED_KEYS);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(T bean) throws Exception {

        List<Column> primaryKeyColumns = DatabaseUtil.getPrimaryKeyColumns(bean.getClass());
        if (primaryKeyColumns.size() == 0) {
            throw new Exception("No primary key defined for : " + bean.getClass());
        }

        StringBuilder buf = new StringBuilder();
        if (!"h2".equalsIgnoreCase(variant)) {
            buf.append("delete ignore from ");
        } else {
            buf.append("delete from ");
        }
        buf.append(toFullName(bean.getClass()));

        List<Object> pairs = new ArrayList<Object>();
        for (Column primaryKeyColumn : primaryKeyColumns) {
            Object rowKeyValue = DatabaseUtil.getValue(bean, primaryKeyColumn.field());
            pairs.add(primaryKeyColumn.field());
            pairs.add(rowKeyValue);
        }

        buf.append(buildWhereClause(bean.getClass(), pairs.toArray()));

        Connection connection = null;
        try {
            connection = getConnection();

            write(connection, buf.toString(), Statement.NO_GENERATED_KEYS);

        } finally {
            release(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void deleteAll(Class<T> clazz) throws Exception {

        Connection connection = null;
        try {
            connection = getConnection();
            write(connection, "delete from " + toFullName(clazz), Statement.NO_GENERATED_KEYS);

        } finally {
            release(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(Class<T> clazz, Object... keyValue) throws Exception {

        StringBuilder buf = new StringBuilder();
        buf.append("delete from ");
        buf.append(toFullName(clazz));
        buf.append(" ");
        buf.append(buildWhereClause(clazz, keyValue));

        Connection connection = null;
        try {
            connection = getConnection();

            write(connection, buf.toString(), Statement.NO_GENERATED_KEYS);

        } finally {
            release(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<Object> uniqueValues(Class<T> clazz, String fieldname) throws Exception {

        StringBuilder buf = new StringBuilder();
        buf.append("select distinct " + fieldname + " from ");
        buf.append(toFullName(clazz));
        buf.append(" order by " + fieldname);

        List<T> list = read(clazz, buf.toString());

        List<Object> values = new ArrayList<Object>();
        for (T bean : list) {
            Object value = DatabaseUtil.getValue(bean, fieldname);
            if (value != null && !values.contains(value)) {
                values.add(value);
            }
        }
        return values;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void write(String sql) throws Exception {

        Connection connection = null;
        try {
            connection = getConnection();

            write(connection, sql, Statement.NO_GENERATED_KEYS);

        } finally {
            release(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void write(List<String> items) throws Exception {

        Connection connection = getConnection();
        for (String sql : items) {
            try {
                write(connection, sql, Statement.NO_GENERATED_KEYS);
            } catch (Exception ex) {
                log.info("Database: " + ex + ": " + sql);
            }
        }
        release(connection);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> querySQL(Class<T> clazz, String whereClause) throws Exception {

        String sql = buildSQL(clazz, new String[] { "where", whereClause });

        return read(clazz, sql);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Row> readRows(String sql) throws Exception {

        List<Row> list = new ArrayList<Row>();
        Statement stmt = null;
        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            sql = convertSqlSchemaName(sql);

            log.fine("readRows: sql=" + sql);

            rs = stmt.executeQuery(sql);

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            while (rs.next()) {
                Row bean = new Row();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    String columnType = rsmd.getColumnTypeName(i);
                    String value = rs.getString(i);
                    bean.getCells().add(newCell(columnName, columnType, value));
                }
                list.add(bean);
            }
        } finally {
            close(rs);
            close(stmt);
            release(conn);
        }
        return list;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Row> readMetaRows(String metaName) throws Exception {

        List<Row> list = new ArrayList<Row>();
        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = getConnection();

            // System.out.println("**** read: metaName=" + metaName);

            DatabaseMetaData dmd = conn.getMetaData();
            if ("primarykeys".equalsIgnoreCase(metaName)) {
                rs = dmd.getPrimaryKeys(null, null, "%");
            } else if ("attributes".equalsIgnoreCase(metaName)) {
                rs = dmd.getAttributes(null, null, "%", "%");
            } else if ("BestRowIdentifier".equalsIgnoreCase(metaName)) {
                rs = dmd.getBestRowIdentifier(null, null, null, 0, false);
            } else if ("Catalogs".equalsIgnoreCase(metaName)) {
                rs = dmd.getCatalogs();
            } else if ("CrossReference".equalsIgnoreCase(metaName)) {
                rs = dmd.getCrossReference(null, null, "%", null, null, "%");
            } else if ("SupportsConvert".equalsIgnoreCase(metaName)) {
                List<Row> types = readMetaRows("TypeInfo");
                for (Row type1 : types) {
                    for (Row type2 : types) {
                        int typeName1 = Integer.parseInt(DatabaseUtil.findValue(type1.getCells(), "data_type"));
                        int typeName2 = Integer.parseInt(DatabaseUtil.findValue(type2.getCells(), "data_type"));
                        boolean result = dmd.supportsConvert(typeName1, typeName2);
                        Row item = new Row();
                        item.getCells().add(newCell("fromType", null, typeName1));
                        item.getCells().add(newCell("toType", null, typeName2));
                        item.getCells().add(newCell("supported", null, result));
                        list.add(item);
                    }
                }
                return list;
            } else if ("TypeInfo".equalsIgnoreCase(metaName)) {
                rs = dmd.getTypeInfo();
            } else if ("TableTypes".equalsIgnoreCase(metaName)) {
                rs = dmd.getTableTypes();
            }

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            while (rs.next()) {
                Row bean = new Row();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    Object value = rs.getObject(i);
                    bean.getCells().add(newCell(columnName, null, value));
                }
                list.add(bean);
            }
        } finally {
            close(rs);
            release(conn);
        }
        return list;
    }

    private Cell newCell(String name, String columnType, Object value) {
        Cell cell = new Cell();
        cell.setName((name == null) ? "" : name.toLowerCase());
        cell.setType(columnType);
        cell.setValue((value == null) ? null : value.toString());
        return cell;
    }

    /**
     * Available only for DatabaseJDBC, read from sql string into resultSet.
     * 
     * @param sql
     * @return
     * @throws Exception
     */
    public ResultSet readResultSet(String sql) throws Exception {
        Connection connection = null;
        try {

            sql = convertSqlSchemaName(sql);

            log.fine("read: sql=" + sql);

            connection = getConnection();
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(sql);

        } catch (SQLException e) {
            throw new Exception(sql, e);
        }
    }

    private <T> List<T> read(Class<T> clazz, String sql) throws Exception {

        List<T> list = new ArrayList<T>();
        Statement stmt = null;
        Connection conn = null;

        sql = convertSqlSchemaName(sql);

        log.fine("read: sql=" + sql);

        try {

            conn = getConnection();
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            Class[] fieldClazzs = new Class[columnCount];
            Column[] fieldColumns = new Column[columnCount];
            String[] propertyNames = new String[columnCount];

            T extra = clazz.newInstance();

            for (int i = 0; i < columnCount; i++) {
                String columnName = rsmd.getColumnName(i + 1);
                fieldColumns[i] = DatabaseUtil.getColumnAnnotation(clazz, columnName);
                fieldClazzs[i] = toPropertyType(extra, columnName);

                if (fieldClazzs[i] == null) {
                    log.info("DatabaseJDBC: (column) " + columnName + " (not found in bean) "
                            + extra.getClass().getName());
                }

                propertyNames[i] = DatabaseUtil.toPropertyName(clazz, columnName);
            }

            while (rs.next()) {
                T bean = clazz.newInstance();

                for (int i = 1; i <= columnCount; i++) {

                    Class fieldClazz = fieldClazzs[i - 1];
                    Column fieldColumn = fieldColumns[i - 1];
                    String propertyName = propertyNames[i - 1];
                    Object inputValue = rs.getObject(i);

                    try {
                        if (fieldClazz == null) {
                            DatabaseUtil.set(bean, propertyName, 0);

                        } else if (fieldClazz.isPrimitive() && inputValue == null) {
                            DatabaseUtil.set(bean, propertyName, 0);

                        } else if (fieldColumn.converter() != null && !fieldColumn.converter().isEmpty()) {
                            Object value = DatabaseUtil.convert(fieldColumn, fieldClazz, inputValue);
                            DatabaseUtil.set(bean, propertyName, value);

                        } else if (fieldClazz.isAssignableFrom(String.class) && !isEmpty(fieldColumn.converter())) {
                            Object value = DatabaseUtil.convert(fieldColumn, fieldClazz, inputValue);
                            DatabaseUtil.set(bean, propertyName, value);

                        } else if (java.util.List.class.isAssignableFrom(fieldClazz)
                                && !fieldColumn.genericType().isEmpty()) {
                            Object value = Converters.convertToList(toClass(fieldColumn.genericType()), inputValue);
                            DatabaseUtil.set(bean, propertyName, value);

                        } else {
                            Object value = Converters.convert(fieldClazz, inputValue);
                            DatabaseUtil.set(bean, propertyName, value);
                        }

                    } catch (Throwable ex) {
                        log.info("ERROR: JDBC field read: " + clazz.getName() + "," + rsmd.getColumnName(i) + ","
                                + fieldClazz + "," + inputValue + "," + ex);
                        ex.printStackTrace();
                    }
                }

                // When reading beans update the fields which maybe calculated or for whatever
                // reason the user deems needs to be altered.
                DatabaseUtil.callGenerator(bean);
                list.add(bean);
            }
            rs.close();

        } catch (Exception ex) {
            throw new Exception("Read sql failed: " + dbc.getDatabaseUrl() + "," + sql, ex);

        } finally {
            close(stmt);
            release(conn);
        }
        return list;
    }

    private Object write(Connection conn, String sql, int autoGeneratedKeys) throws Exception {

        if (sql == null || sql.isEmpty()) {
            new Exception().printStackTrace();
            return null;
        }

        Object key = null;
        Statement stmt = null;

        try {

            sql = convertSqlSchemaName(sql);

            log.fine("write: SQL=" + sql);

            stmt = conn.createStatement();
            int result = stmt.executeUpdate(sql, autoGeneratedKeys);
            if (result != 0) {
                if (autoGeneratedKeys == Statement.RETURN_GENERATED_KEYS) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    try {
                        while (rs.next()) {
                            key = rs.getLong(1);
                            break;
                        }
                    } catch (Throwable t) {
                        log.throwing("ERROR: get updated primary key.", "DatabaseJDBC.write", t);
                    }
                    rs.close();
                } else {
                    key = "";
                }
            }

        } catch (Throwable ex) {
            System.err.println("ERROR.write: " + ex + ": SQL=" + sql);
            throw ex;

        } finally {
            close(stmt);
        }
        return key;
    }

    private <T> String buildSQL(Class<T> clazz, Object[] pairs) throws Exception {

        StringBuilder buf = new StringBuilder();
        Table table = DatabaseUtil.getTableAnnotation(clazz);
        if (table != null && table.sqlSelect() != null && !table.sqlSelect().trim().isEmpty()) {
            buf.append(table.sqlSelect());
            buf.append(buildWhereClause(clazz, pairs));

        } else {
            buf.append("select * from ");
            buf.append(toFullName(clazz));
            buf.append(buildWhereClause(clazz, pairs));
        }

        return buf.toString();
    }

    /**
     * Creates a select statement for SQL that can do multiple where and selections
     * based on the keyValue pairs.
     * 
     * @param <T>
     * 
     * @param tableName
     * @param keyValue
     * @return
     * @throws Exception
     */
    private <T> String buildWhereClause(Class<T> clazz, Object[] keyValue) throws Exception {

        long pageno = -1;
        long pagesize = -1;

        StringBuilder buf = new StringBuilder();

        if (keyValue != null && (keyValue.length % 2) == 0) {
            for (int i = 0; i < keyValue.length; i = i + 2) {
                String key = "" + keyValue[i];
                Object value = keyValue[i + 1];

                // TODO FIX this should handle any time, int, long string.
                if (PAGENO_KEY.equalsIgnoreCase(key)) {
                    pageno = new Long((int) value);
                    continue;
                }
                if (PAGESIZE_KEY.equalsIgnoreCase(key)) {
                    pagesize = new Long((int) value);
                    continue;
                }

                if ("where".equalsIgnoreCase(key)) {
                    buf.append((String) value);
                    continue;
                }

                Column column = DatabaseUtil.getColumnAnnotation(clazz, key);
                if (column == null) {
                    continue;
                }

                if (buf.length() == 0) {
                    buf.append(" where ");
                } else {
                    buf.append(" and ");
                }

                buf.append(toFieldName(column.field()));
                buf.append(" = ");
                buf.append(toFieldValue(column, value));
            }
        }

        if (pagesize > 0) {
            buf.append(" limit ");
            buf.append(pagesize);
            buf.append(" offset ");
            buf.append(pageno * pagesize);
        }
        return buf.toString();
    }

    private <T> void constructDBTable(Map<String, String> cache, Connection connection, Class<T> clazz)
            throws Exception {

        log.info("constructDBTable: checking " + toFullName(clazz));

        String databaseName = toAliasSchemaName(clazz);
        if (!isDatabaseCreated(cache, clazz)) {
            log.info("constructDBTables: database is NOT already created " + databaseName);
            if (!"h2".equalsIgnoreCase(variant)) {
                write(connection, "create database if not exists " + databaseName, Statement.NO_GENERATED_KEYS);
            } else {
                write(connection, "create schema if not exists " + databaseName, Statement.NO_GENERATED_KEYS);
            }
        }

        if (!isCreateTableType(clazz)) {
            return;
        }

        if (!isTableCreated(cache, clazz)) {

            log.info("constructDBTables: create table " + toFullName(clazz));

            // generate Table create SQL
            StringBuilder sql = new StringBuilder();
            sql.append("create table if not exists " + toFullName(clazz));
            sql.append(" (");

            boolean isFirst = true;

            Method[] methods = clazz.getMethods();
            if (methods != null) {
                for (Method method : methods) {
                    Column column = method.getAnnotation(Column.class);
                    if (column == null) {
                        continue;
                    }

                    if (!column.persistent()) {
                        continue;
                    }

                    String columnName = column.field();

                    if (!isFirst) {
                        sql.append(",");
                    }
                    isFirst = false;

                    sql.append(toFieldName(columnName));
                    sql.append(" ");
                    sql.append(getColumnType(method, column));

                    if (column.primaryKey()) {
                        if (method.getReturnType().isPrimitive()) {
                            sql.append(" not null auto_increment primary key");
                        } else {
                            sql.append(" not null primary key");
                        }
                        // } else if (column.isUnique()) {
                        // sql.append(" unique key");
                    }
                }
            }
            sql.append(")");

            // write the SQL
            write(connection, sql.toString(), Statement.NO_GENERATED_KEYS);
        }

        Table table = DatabaseUtil.getTableAnnotation(clazz);
        if (table.seedFilename() != null && !table.seedFilename().isEmpty()) {
            if (this.size(clazz) == 0) {

                log.info("constructDBTables: import table " + table.seedFilename() + ", " + toFullName(clazz));
                DatabaseMapper.importTableByFile(this, table.seedFilename(), clazz);
            }
        }
    }

    private <T> List<String> addDefaultColumns(Connection connection, T bean) throws Exception {

        if (bean == null) {
            return null;
        }
        Table table = bean.getClass().getAnnotation(Table.class);
        if (table == null) {
            return null;
        }
        List<String> columnNames = listColumns(connection, bean.getClass());
        if (!dbc.isCreateDatabase()) {
            return columnNames;
        }

        Method[] methods = bean.getClass().getMethods();
        if (methods == null) {
            return columnNames;
        }
        for (Method method : methods) {
            String columnName = null;
            Column column = method.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            columnName = column.field();
            if (columnName == null) {
                continue;
            }

            if (columnNames.contains(columnName.toLowerCase())) {
                continue;
            }

            addDefaultColumn(connection, bean.getClass(), column);

            columnNames.add(columnName.toLowerCase());
        }
        return columnNames;
    }

    private <T> void addDefaultColumn(Connection connection, Class<T> clazz, Column column) throws Exception {

        StringBuilder sql = new StringBuilder();
        sql.append("alter table " + toFullName(clazz));
        sql.append(" add ");
        sql.append(toFieldName(column.field()));
        sql.append(" TEXT"); // TODO fix this, pull from column type.

        // write the SQL
        write(connection, sql.toString(), Statement.NO_GENERATED_KEYS);
    }

    private <T> boolean isDatabaseCreated(Map<String, String> cache, Class<T> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        return (cache.get(table.databaseName().toLowerCase()) != null);
    }

    private <T> boolean isTableCreated(Map<String, String> cache, Class<T> clazz) {

        Table table = clazz.getAnnotation(Table.class);
        String name = toFullName(clazz);
        return cache.containsKey(name);
    }

    private <T> boolean isCreateTableType(Class<T> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        return (!"bean".equalsIgnoreCase(table.tableType()) && !"viewapp".equalsIgnoreCase(table.tableType()));
    }

    // INSERT INTO table (a,b,c) VALUES (1,2,3)
    // ON DUPLICATE KEY UPDATE c=c+1;

    private <T> String insertSQL(T bean, List<String> columnNames) throws Exception {

        Method[] methods = bean.getClass().getMethods();
        if (methods == null || methods.length == 0) {
            return null;
        }

        StringBuilder names = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder updates = new StringBuilder();

        for (Method method : methods) {
            Column column = method.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            if (!columnNames.contains(column.field().toLowerCase())) {
                continue;
            }
            if (!column.persistent()) {
                continue;
            }

            Object value = DatabaseUtil.getValue(bean, column.field());

            // Need to set to null, in case there is a value in there already.
            if (value == null) {
                if (java.util.Date.class.isInstance(method.getReturnType())) {
                    if (!column.primaryKey() || !"autoincrement".equalsIgnoreCase(column.idMethod())) {
                        append(names, toFieldName(column.field()), ',');
                        append(values, "now()", ',');
                    }
                    append(updates, toFieldName(column.field()), "now()", ',');
                } else if (!column.required()) {
                    if (!column.primaryKey() || !"autoincrement".equalsIgnoreCase(column.idMethod())) {
                        append(names, toFieldName(column.field()), ',');
                        append(values, "null", ',');
                    }
                    append(updates, toFieldName(column.field()), "null", ',');
                } else {
                    System.err.println("WARN: value is null, column is required: " + bean.getClass().getName() + ","
                            + column.field());
                }
            } else {
                if (!column.primaryKey() || !"autoincrement".equalsIgnoreCase(column.idMethod())) {
                    append(names, toFieldName(column.field()), ',');
                    append(values, toFieldValue(column, value), ',');
                }
                append(updates, toFieldName(column.field()), toFieldValue(column, value), ',');
            }
        }

        // INSERT [LOW_PRIORITY | DELAYED | HIGH_PRIORITY] [IGNORE]
        // [INTO] tbl_name
        // [PARTITION (partition_name,...)]
        // [(col_name,...)]
        // {VALUES | VALUE} ({expr | DEFAULT},...),(...),...

        StringBuilder buf = new StringBuilder();
        buf.append("insert into ");
        buf.append(toFullName(bean.getClass()));
        buf.append(" (");
        buf.append(names.toString());
        buf.append(") values (");
        buf.append(values.toString());
        buf.append(") ON DUPLICATE KEY UPDATE ");
        buf.append(updates.toString());

        return buf.toString();
    }

    private <T> String updateSQL(T bean, List<String> columnNames) throws Exception {

        StringBuilder buf1 = new StringBuilder();

        Method[] methods = bean.getClass().getMethods();
        if (methods == null) {
            return null;
        }

        for (Method method : methods) {
            Column column = method.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            String columnName = column.field();
            if (!columnNames.contains(columnName.toLowerCase())) {
                continue;
            }

            Object value = DatabaseUtil.getValue(bean, columnName);

            if (!column.persistent()) {
                continue;
            }

            // Need to set to null, in case there is a value in there already.
            if (value != null) {
                append(buf1, toFieldName(columnName), toFieldValue(column, value), ',');

            } else if (method.getReturnType().isInstance(java.util.Date.class)) {
                append(buf1, toFieldName(columnName), "now()", ',');
            }
        }

        StringBuilder buf = new StringBuilder();
        List<Column> primaryKeyColumns = DatabaseUtil.getPrimaryKeyColumns(bean.getClass());
        if (primaryKeyColumns.size() == 0) {
            buf.append("update ");
            buf.append(toFullName(bean.getClass()));
            buf.append(" set ");
            buf.append(buf1.toString());

        } else {
            List<Object> pairs = new ArrayList<Object>();
            for (Column primaryKeyColumn : primaryKeyColumns) {
                Object primaryKeyValue = DatabaseUtil.getValue(bean, primaryKeyColumn.field());
                pairs.add(primaryKeyColumn.field());
                pairs.add(primaryKeyValue);
            }

            // UPDATE [LOW_PRIORITY] [IGNORE] table_reference
            // SET col_name1={expr1|DEFAULT} [, col_name2={expr2|DEFAULT}] ...
            // [WHERE where_condition]
            // ON DUPLICATE KEY UPDATE c=3;

            buf.append("update ");
            buf.append(toFullName(bean.getClass()));
            buf.append(" set ");
            buf.append(buf1.toString());
            buf.append(" ");
            buf.append(buildWhereClause(bean.getClass(), pairs.toArray()));
        }

        return buf.toString();
    }

    private <T> String convertSqlSchemaName(String sql) {
        for (Param param : dbc.getSchemaAlias()) {
            String original = "#{" + param.getName() + "}";
            String actual = param.getValue();
            if (sql.indexOf(original) != -1) {
                return sql.replace(original, actual);
            }
        }
        return sql;
    }

    private <T> String toAliasSchemaName(Class<T> clazz) {
        String databasename = DatabaseUtil.getDatabaseName(clazz);
        String schema = DatabaseUtil.getValue(dbc.getSchemaAlias(), databasename);
        return (schema == null) ? databasename : schema;
    }

    private <T> String toAliasSchemaName(String databasename) {
        String schema = DatabaseUtil.getValue(dbc.getSchemaAlias(), databasename);
        return (schema == null) ? databasename : schema;
    }

    // Temporary see the Memory adapter, add this to interface, or to a utility or
    // something should not be public.
    private <T> String toFullName(Class<T> clazz) {

        String tablename = DatabaseUtil.getTableName(clazz);
        String schema = toAliasSchemaName(clazz);
        if (schema != null && schema.length() > 0) {
            tablename = toFieldName(schema) + '.' + toFieldName(tablename);
        } else {
            tablename = toFieldName(tablename);
        }
        return toCase(tablename);
    }

    private String getColumnType(Method field, Column annotation) throws Exception {
        Class type = field.getReturnType();
        long length = annotation.size();
        int decimal = annotation.decimalSize();

        String columnType = SQLConversionTables.getDataTypeString(type);
        if (annotation.dataType().length() > 0) {
            // for example if a varchar use the predefined data type definition
            // which is varchar(<size>), etc. there could be a bug using the data type
            // since we assume the user did not use varchar(<size>) when specifying data
            // type.
            // Rethink this part, works most of the time.
            if (!columnType.startsWith(annotation.dataType())) {
                columnType = annotation.dataType();
            }
            // HACK: clob is not allowed data type for MySql.
            if ("clob".equalsIgnoreCase(columnType)) {
                columnType = "text";
            }
        }
        if (columnType == null) {
            log.warning("WARN: Unhandled Column Type: " + type);
            columnType = "text";
        }
        if (columnType.startsWith("varchar")) {
            if (length > 512) {
                columnType = "text";
            } else if (length <= 0) {
                columnType = "text";
            }
        }

        if (columnType.equals("enum")) {
            if (annotation.enumValue() != null && annotation.enumValue().length > 0) {
                columnType = columnType + " " + toEnumString(annotation.enumValue());
            }
        }

        if (length <= 0) {
            columnType = columnType.replace("(<size>)", "");
            columnType = columnType.replace("(<size>,<decimal>)", "");
        }
        // Could cause issues, ?S/B? length is zero, and decimal < 32, and should be for
        // numerics not strings.
        if (length <= decimal) {
            length = 32 - decimal;
        }
        columnType = columnType.replace("<size>", Long.toString(length));
        columnType = columnType.replace("<decimal>", Integer.toString(decimal));
        return columnType;
    }

    private String toEnumString(String[] items) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        for (String item : items) {
            if (buf.length() > 1) {
                buf.append(",");
            }
            buf.append("'");
            buf.append(item);
            buf.append("'");
        }
        buf.append(")");
        return buf.toString();
    }

    private String toFieldName(String name) {
        if (name == null) {
            return "";
        }
        if (name.length() > MAX_COLUMN_NAME_LENGTH) {
            name = name.substring(0, MAX_COLUMN_NAME_LENGTH);
        }
        if ("h2".equalsIgnoreCase(variant)) {
            if (isReservedWord(name)) {
                return QUOTE_NAME_H2 + name.toUpperCase() + QUOTE_NAME_H2;
            } else {
                return name;
            }
        }
        if (containsSpecialCharacters(name) || isReservedWord(name)) {
            return QUOTE_NAME + name + QUOTE_NAME;
        }
        return name;
    }

    private boolean containsSpecialCharacters(String str) {
        return (str != null && str.indexOf('-') != -1);
    }

    private boolean isReservedWord(String str) {
        return ArrayUtils.contains(SQLDriver.RESERVED_WORDS, str.toUpperCase());
    }

    // Use Converters.
    private <T> String toFieldValue(Column column, Object value) throws Exception {
        int size = (int) column.size();

        if (value == null) {
            return "NULL";
        }
        if (value instanceof Long && "timestamp".equalsIgnoreCase(column.dataType())) {
            return escape(toUpdateDateTime((long) value));
        }
        if (value instanceof String) {
            return escape(toLimitedString((String) value, size));
        }
        if (value instanceof Character) {
            return escape(value.toString());
        }
        if (value.getClass().isAssignableFrom(char.class)) {
            return escape(value.toString());
        }
        if (value instanceof java.sql.Blob) {
            java.sql.Blob blob = (java.sql.Blob) value;
            return toHex(blob.getBytes(1L, (int) blob.length()));
        }
        if (value instanceof java.sql.Clob) {
            java.sql.Clob clob = (java.sql.Clob) value;
            return escape(clob.getSubString(0L, (int) clob.length()));
        }
        if (value instanceof boolean[]) {
            return escape(Arrays.toString((boolean[]) value));
        }
        if (value instanceof byte[]) {
            return toHex((byte[]) value);
        }
        if (value instanceof char[]) {
            return escape(new String((char[]) value));
        }
        if (value instanceof double[]) {
            return escape(Arrays.toString((double[]) value));
        }
        if (value instanceof float[]) {
            return escape(Arrays.toString((float[]) value));
        }
        if (value instanceof int[]) {
            return escape(Arrays.toString((int[]) value));
        }
        if (value instanceof long[]) {
            return escape(Arrays.toString((long[]) value));
        }
        if (value instanceof short[]) {
            return escape(Arrays.toString((short[]) value));
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value.getClass().isPrimitive()) {
            return (value.toString().isEmpty()) ? "0" : toLimitedString(value.toString(), size);
        }
        if (value.getClass().isEnum()) {
            String v = value.toString();
            if (value instanceof EnumItem) {
                v = ((EnumItem) value).getName();
                if (v == null || v.trim().length() == 0) {
                    v = value.toString();
                }
            }
            return QUOTE_VALUE + toLimitedString(v, size) + QUOTE_VALUE;
        }
        if (value instanceof List) {
            return escape(toLimitedString(convertToString(column, value), size));
        }
        if (value instanceof Map) {
            return escape(toLimitedString(convertToString(column, value), size));
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof java.util.Date) {
            return QUOTE_VALUE + formatter.format((java.util.Date) value) + QUOTE_VALUE;
        }
        if (value.getClass().isArray()) {
            return escape(ConverterUtils.writeJsonFromArray((Object[]) value));
        }
        if ("json".equalsIgnoreCase(column.dataType())) {
            String json = ConverterUtils.writeJson(value);
            if (json == null || json.length() == 0) {
                return QUOTE_VALUE + QUOTE_VALUE;
            }
            return escape(json);
        }
        String json = ConverterUtils.writeJson(value);
        if (json == null || json.length() == 0) {
            return QUOTE_VALUE + QUOTE_VALUE;
        }
        return escape(json);
    }

    private String escape(String str) throws Exception {
        if (str == null || str.length() == 0 || "[]".equals(str)) {
            return "NULL";
        }
        str = escape(str, "\\");
        str = escape(str, "'");
        str = escape(str, "\"");
        return QUOTE_VALUE + str + QUOTE_VALUE;
    }

    private String escape(String str, String escapeChar) throws Exception {

        if ("h2".equalsIgnoreCase(variant)) {
            return str.replace(escapeChar, "'" + escapeChar);
        }
        return str.replace(escapeChar, "\\" + escapeChar);
    }

    private void append(StringBuilder buf, String name, String value, char separator) {
        if (buf.length() > 0) {
            buf.append(separator);
        }
        buf.append(name);
        buf.append('=');
        buf.append(value);
    }

    private void append(StringBuilder buf, String value, char separator) {
        if (buf.length() > 0) {
            buf.append(separator);
        }
        buf.append(value);
    }

    private <T> Method toMethod(Class<T> clazz, String fieldname) throws Exception {
        Method[] methods = clazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column == null || column.field() == null) {
                    continue;
                }
                if (fieldname.equalsIgnoreCase(column.field())) {
                    return method;
                }
            }
        }
        System.err.println("Unable to find method with field name of " + fieldname + " in class " + clazz.getName());
        return null;
    }

    private String toLimitedString(String str, int size) {
        if (size > 0 && str.length() > size) {
            return str.substring(0, size);
        }
        return str;
    }

    private <T> String convertToString(Column column, Object value) {
        try {
            return Converters.convert(String.class, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String toHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "NULL";
        }
        StringBuilder buf = new StringBuilder();
        // buf.append("0x");
        for (byte b : bytes) {
            buf.append(String.format("%02X", b));
        }
        return "X'" + buf.toString() + "'";
    }

    private <T> Class toPropertyType(T bean, String fieldname) {
        String propertyName = fieldname;
        try {
            Column column = DatabaseUtil.getColumnAnnotation(bean.getClass(), fieldname);
            if (column != null) {
                propertyName = column.name();
            }
            return PropertyUtils.getPropertyType(bean, propertyName);
        } catch (Exception ex) {
            log.severe(
                    "getType failed for " + bean.getClass() + "." + fieldname + "." + propertyName + ": ERROR " + ex);
        }
        return null;
    }

    private Object generateKeyValue(Method method, Column column) {
        String typeName = method.getReturnType().getName();
        if ("String".equals(typeName)) {
            StringBuilder buf = new StringBuilder();
            long size = (column.size() <= 0) ? 8 : column.size();
            for (int i = 0; i < size; i++) {
                int index = (int) (random.nextDouble() * RANDOM_CHARS.length());
                buf.append(RANDOM_CHARS.charAt(index));
            }
            return buf.toString();
        } else if ("Long".equalsIgnoreCase(typeName)) {
            random.nextLong();
        }
        return random.nextInt();
    }

    private void close(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException se) {
            // Intentional
        }
    }

    private void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception ex) {
                log.throwing("ERROR: closing result set.", "", ex);
            }
        }
    }

    private final <T> String getChecksum(Class<T> clazz) throws Exception {
        List<Row> rows = readRows("checksum table " + toFullName(clazz));
        for (Row row : rows) {
            for (Cell cell : row.getCells()) {
                if ("checksum".equalsIgnoreCase(cell.getName())) {
                    return cell.getValue();
                }
            }
        }
        return null;
    }

    private boolean isEmpty(String str) {
        return (str == null || str.isEmpty());
    }

    private Class toClass(String classname) throws Exception {
        if (classname == null) {
            return null;
        }
        if (classname.indexOf('.') == -1) {
            classname = "java.lang." + classname;
        }
        return Class.forName(classname);
    }

    private String toUpdateDateTime(long datetime) {
        return updaterDateTime.format(new Date(datetime));
    }

    private String toCase(String name) {
        if (name == null || dbc.getNameCase() == null) {
            return name;
        }
        switch (dbc.getNameCase()) {
        case UPPER:
            return name.toUpperCase();
        case LOWER:
            return name.toLowerCase();
        default:
            return name;
        }
    }
}
