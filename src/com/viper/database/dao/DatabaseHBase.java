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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.ValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.viper.database.dao.converters.ConverterUtils;
import com.viper.database.filters.Predicate;
import com.viper.database.model.ColumnParam;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.LimitParam;

public class DatabaseHBase implements DatabaseInterface {

    private final static Logger log = Logger.getLogger(DatabaseHBase.class.getName());

    private final static String QL_CONTENTS = "contents";

    private String hbaseParent = "/hbase";
    private Configuration config = null;

    /**
     * Given the database connection parameters, return a database (dao) object
     * for accessing the database. The return object is DatabaseInterface
     * implementation.
     * 
     * @param dbc
     *            the database connection parameetrs
     * @return the dao object for accessing the database
     * 
     * @throws Exception
     *             if failure to perform initialization.
     */

    public final static DatabaseInterface getInstance(DatabaseConnection dbc) throws Exception {
        return new DatabaseHBase(dbc);
    }

    /**
     * Given the database connection parameters, return a database (dao) object
     * for accessing the database. The return object is DatabaseInterface
     * implementation. This private routine, will setup the hbase configuration
     * parameters, and will optionally create and/or alter the necessary tables,
     * to match the Java bean model.
     * 
     * @param dbc
     *            the database connection parameters
     * 
     * @throws Exception
     *             if failure to perform initialization.
     */
    private DatabaseHBase(DatabaseConnection dbc) throws Exception {

        String home = System.getProperty("hadoop.home.dir");
        if (home == null) {
            home = System.getenv("HADOOP_HOME");
        }
        if (home == null) {
            System.err.println("Hadoop home is not defined.");
        }

        URI url = new URI(dbc.getDatabaseUrl());

        System.err.println("hbase.zookeeper.quorum:" + url.getHost());
        System.err.println("hbase.zookeeper.property.clientPort:" + url.getPort());

        config = HBaseConfiguration.create();
        config.setInt("timeout", 120000);
        config.set("hbase.zookeeper.quorum", url.getHost());
        config.set("hbase.zookeeper.property.clientPort", "" + url.getPort());
        config.set("zookeeper.znode.parent", hbaseParent);

        if (dbc.isCreateDatabase()) {
        	for (String packagename : dbc.getPackageNames()) {
        		createDatabase(packagename);
        	}
        }
    }

    /**
     * Utility routine to open an actual connection to the hbase database.
     * 
     * @return hbase database connection
     * @throws Exception
     *             problem with with the specified configuration.
     */
    private final Connection openConnection() throws Exception {
        if (config == null) {
            throw new Exception("ERROR :: DB CONFIG Object is NULL");
        }

        Connection connection = ConnectionFactory.createConnection(config);
        if (connection == null || connection.isAborted() || connection.isClosed()) {
            throw new Exception("HBASE: openconnection connection not opened.");
        }
        return connection;
    }

    /**
     * Close the hbase connection, necessary to prevent memory leaks.
     * 
     * @param connection
     *            the
     * @throws Exception
     *             close of connection has failed.
     */

    private final void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
    /**
     * {@inheritDoc}
     * 
     * Note: currently this method is not implemented.
     */
    @Override
    public final void release() {

    }

    /**
     * {@inheritDoc}
     * 
     * Note: currently this method is not implemented.
     */
    @Override
	public final <T> long size(Class<T> clazz) throws Exception {
        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     * Note: currently this method is not implemented.
     */
    @Override
    public final List<String> listDatabases() {
        List<String> items = new ArrayList<String>();
        return items;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public final List<String> listTables(String databaseName) {

        Admin admin = null;
        Connection connection = null;

        List<String> items = new ArrayList<String>();
        try {
            connection = openConnection();
            admin = connection.getAdmin();
            for (TableName tableName : admin.listTableNames()) {
                items.add(tableName.getNameAsString());
            }
        } catch (Exception ex) {
            log.throwing("", "Unable to listTableNames", ex);
        } finally {
            close(connection);
            close(admin);
        }
        return items;
    }

    /**
     * {@inheritDoc}
     * 
     * Note: currently this method is not implemented.
     */
    @Override
    public final <T> List<String> listColumns(Class<T> clazz) {
        List<String> items = new ArrayList<String>();
        return items;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Note: currently this method is not implemented.
     */
    @Override
    public <T> boolean hasChanged(Class<T> clazz) {
    	return true;
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void createDatabase(String packagename) throws Exception {

        List<Class<?>> classes = DatabaseUtil.listDatabaseTableClasses(packagename, null);
        for (Class<?> clazz : classes) {
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
    public final <T> void create(Class<T> clazz) throws Exception {

        String databasename = DatabaseUtil.getDatabaseName(clazz);
        String tablename = DatabaseUtil.getTableName(clazz);
        List<String> columns = DatabaseUtil.getColumnFamilyNames(clazz);

        Connection connection = null;
        Admin admin = null;

        try {
            connection = openConnection();
            admin = connection.getAdmin();

            TableName tableName = TableName.valueOf(tablename);
            HTableDescriptor descriptor = admin.getTableDescriptor(tableName);

            if (admin.tableExists(tableName)) {
                log.fine("Table " + tablename + " already exists.");
                for (String columnFamily : columns) {
                    if (descriptor.getFamily(Bytes.toBytes(columnFamily)) == null) {
                        log.fine("MISSING ColumnFamily: " + tablename + ", " + columnFamily);
                    }
                }

            } else {
                log.fine("Table " + tablename + " does NOT exist, creating it.");
                for (String columnFamily : columns) {
                    descriptor.addFamily(new HColumnDescriptor(Bytes.toBytes(columnFamily)));
                    log.fine("CREATING ColumnFamily: " + tablename + ", " + columnFamily);
                }
                admin.createTable(descriptor);
            }
        } finally {
            close(connection);
            close(admin);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public final <T> T query(Class<T> clazz, Object... keyValue) throws Exception {
        List<String> list = read(clazz, keyValue);
        if (list == null || list.size() == 0) {
            return null;
        }
        return ConverterUtils.readJson(list.get(0), clazz);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public final <T> List<T> queryAll(Class<T> clazz) throws Exception {
        List<String> list = read(clazz);
        return ConverterUtils.readJsonListToList(list, clazz);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    @Deprecated
    public final <T> List<T> queryList(Class<T> clazz, Object... keyValue) throws Exception {
        List<String> list = read(clazz, keyValue);
        return ConverterUtils.readJsonListToList(list, clazz);
    }

    /**
     * {@inheritDoc}
     * 
     */
    public <T> List<T> queryList(Class<T> tableClass, Map<String, String> parameters) throws Exception {
        return null; // TODO
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Predicate<T> filter, List<ColumnParam> columnParams, LimitParam limitParam, Map<String, String> parameters) throws Exception {

        List<T> results = new ArrayList<T>();

        List<T> beans = queryAll(clazz);
        for (T bean : beans) {
            if (filter.apply(bean)) {
                results.add(bean);
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public final <T> void delete(Class<T> clazz, Object... keyValue) throws Exception {

        String primaryKeyName = DatabaseUtil.getPrimaryKeyName(clazz);
        Connection connection = openConnection();
        Table table = openTable(connection, clazz);

        List<String> list = read(clazz, keyValue);
        for (String jstr : list) {
            Map<String, Object> map = ConverterUtils.readJsonToMap(jstr);
            Object primaryKeyValue = map.get(primaryKeyName);
            table.delete(new Delete(toBytes(primaryKeyValue.toString())));
        }
        close(table);
        close(connection);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public final <T> void delete(T bean) throws Exception {

        String primaryKeyName = DatabaseUtil.getPrimaryKeyName(bean.getClass());
        Connection connection = openConnection();
        Table table = openTable(connection, bean.getClass());

        String primaryKeyValue = DatabaseUtil.getString(bean, primaryKeyName);
        table.delete(new Delete(toBytes(primaryKeyValue)));

        close(table);
        close(connection);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public final <T> void deleteAll(Class<T> clazz) throws Exception {

        Connection connection = openConnection();
        Admin admin = connection.getAdmin();

        TableName tableName = getTableName(clazz);
        HTableDescriptor descriptor = admin.getTableDescriptor(tableName);

        admin.disableTable(tableName);
        admin.deleteTable(tableName);
        admin.createTable(descriptor);

        admin.close();
        connection.close();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> T update(T bean) throws Exception {
        return bean;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public final <T> T insert(T bean) throws Exception {

        String primaryKeyName = DatabaseUtil.getPrimaryKeyName(bean.getClass());
        List<String> columnFamilyNames = DatabaseUtil.getColumnFamilyNames(bean.getClass());
        List<String> qualifierNames = DatabaseUtil.getQualifierNames(bean.getClass());

        Connection connection = openConnection();
        Table table = openTable(connection, bean.getClass());

        Object primaryKeyValue = nextRowNumber(table, bean.getClass(), bean);
        DatabaseUtil.setValue(bean, primaryKeyName, primaryKeyValue);

        Put put = new Put(toBytes("" + primaryKeyValue));

        for (String familyName : columnFamilyNames) {
            for (String qualifierName : qualifierNames) {
                String qualifierValue = DatabaseUtil.getString(bean, qualifierName);
                put.add(toBytes(familyName), toBytes(qualifierName), toBytes(qualifierValue));
            }

            String json = ConverterUtils.writeJson(bean);
            put.add(toBytes(familyName), toBytes(QL_CONTENTS), toBytes(json));
        }

        table.put(put);

        close(table);
        close(connection);

        return bean;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public final <T> void insertAll(List<T> beans) throws Exception {

        if (beans == null || beans.size() == 0) {
            return;
        }

        Class clazz = beans.get(0).getClass();
        String primaryKeyName = DatabaseUtil.getPrimaryKeyName(clazz);
        List<String> columnFamilyNames = DatabaseUtil.getColumnFamilyNames(clazz);
        List<String> qualifierNames = DatabaseUtil.getQualifierNames(clazz);

        Connection connection = openConnection();
        Table table = openTable(connection, clazz);

        List<Put> puts = new ArrayList<Put>();

        for (T bean : beans) {
            Object primaryKeyValue = DatabaseUtil.getValue(bean, primaryKeyName);
            if (primaryKeyValue == null) {
                primaryKeyValue = nextRowNumber(table, clazz, bean);
            }
            DatabaseUtil.setValue(bean, primaryKeyName, primaryKeyValue);

            Put put = new Put(toBytes("" + primaryKeyValue));

            for (String familyName : columnFamilyNames) {
                for (String qualifierName : qualifierNames) {
                    String qualifierValue = DatabaseUtil.getString(bean, qualifierName);
                    put.add(toBytes(familyName), toBytes(qualifierName), toBytes(qualifierValue));
                }

                String json = ConverterUtils.writeJson(bean);
                put.add(toBytes(familyName), toBytes(QL_CONTENTS), toBytes(json));
            }
            puts.add(put);
        }

        table.put(puts);

        close(table);
        close(connection);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public final <T> List<Object> uniqueValues(Class<T> clazz, String fieldName) throws Exception {

        List<Object> list = new ArrayList<Object>();

        Connection connection = openConnection();
        Table table = openTable(connection, clazz);
        ResultScanner scanner = table.getScanner(new Scan());

        for (Result result : scanner) {
            for (KeyValue kv : result.raw()) {
                if (!QL_CONTENTS.equalsIgnoreCase(Bytes.toString(kv.getQualifier()))) {
                    continue;
                }
                Map<String, Object> map = ConverterUtils.readJsonToMap(Bytes.toString(kv.getValue()));
                Object value = map.get(fieldName);
                if (value != null && !list.contains(value)) {
                    list.add(value);
                }
            }
        }

        close(scanner);
        close(table);
        close(connection);
        return list;
    }

    /**
     * 
     * @param clazz
     * @param keyValue
     * @return
     * @throws Exception
     */
    private final <T> List<String> read(Class<T> clazz, Object... keyValue) throws Exception {

        List<String> list = new ArrayList<String>();

        Connection connection = openConnection();
        Table table = openTable(connection, clazz);

        Scan scan = new Scan();
        scan.setFilter(buildFilter(clazz, keyValue));

        ResultScanner scanner = table.getScanner(scan);
        for (Result result : scanner) {
            for (KeyValue kv : result.raw()) {
                if (!QL_CONTENTS.equalsIgnoreCase(Bytes.toString(kv.getQualifier()))) {
                    continue;
                }
                list.add(Bytes.toString(kv.getValue()));
            }
        }

        close(scanner);
        close(table);
        close(connection);

        return list;
    }

    /**
     * 
     * @param clazz
     * @param keyValue
     * @return
     * @throws Exception
     */
    private final <T> FilterList buildFilter(Class<T> clazz, Object... keyValue) throws Exception {
        FilterList list = new FilterList();

        String primaryKeyName = DatabaseUtil.getPrimaryKeyName(clazz);
        List<String> qualifierNames = DatabaseUtil.getQualifierNames(clazz);

        for (int i = 0; i < keyValue.length; i = i + 2) {
            String key = keyValue[i].toString();
            Object value = keyValue[i + 1];

            if (primaryKeyName.equals(key)) {
                list.addFilter(new RowFilter(CompareOp.EQUAL, new SubstringComparator(value.toString())));
            } else if (qualifierNames.contains(key)) {
                String fValue = "['\"]" + key + "['\"]:['\"]" + value + "['\"]";
                list.addFilter(new ValueFilter(CompareOp.EQUAL, new RegexStringComparator(fValue)));
            }
        }
        return list;
    }

    /**
     * 
     * @param table
     * @param clazz
     * @param bean
     * @return
     * @throws Exception
     */
    private final synchronized <T> Object nextRowNumber(Table table, Class clazz, T bean) throws Exception {
        Object rowKeyValue = DatabaseUtil.getValue(bean, DatabaseUtil.getPrimaryKeyName(clazz));
        if (rowKeyValue != null) {
            return rowKeyValue;
        }

        int rowNumber = 0;

        Scan scan = new Scan();
        scan.setFilter(new FirstKeyOnlyFilter());

        ResultScanner scanner = table.getScanner(scan);
        for (Result result : scanner) {
            int rowNum = Integer.parseInt(Bytes.toString(result.getRow()));
            if (rowNum > rowNumber) {
                rowNumber = rowNum;
            }
        }
        close(scanner);

        return rowNumber + 1;
    }

    /**
     * 
     * @param connection
     * @param clazz
     * @return
     * @throws Exception
     */
    private final <T> Table openTable(Connection connection, Class<T> clazz) throws Exception {
        String tablename = DatabaseUtil.getTableName(clazz);
        TableName tableName = TableName.valueOf(tablename);
        return connection.getTable(tableName);
    }

    /**
     * 
     * @param table
     * @throws Exception
     */
    private final void close(Table table) throws Exception {
        if (table != null) {
            table.close();
        }
    }

    /**
     * 
     * @param admin
     * @throws Exception
     */
    private final void close(Admin admin) {
        if (admin != null) {
            try {
                admin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * @param scanner
     * @throws Exception
     */
    private final void close(ResultScanner scanner) throws Exception {
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * 
     * @param clazz
     * @return
     * @throws Exception
     */
    private final TableName getTableName(Class clazz) throws Exception {
        String tablename = DatabaseUtil.getTableName(clazz);
        return TableName.valueOf(tablename);
    }

    /**
     * 
     * @param str
     * @return
     */
    private final byte[] toBytes(String str) {
        return Bytes.toBytes(str);
    }

}
