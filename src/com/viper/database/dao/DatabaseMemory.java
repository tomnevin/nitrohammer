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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import com.viper.database.annotations.Column;
import com.viper.database.filters.Predicate;
import com.viper.database.model.ColumnParam;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.LimitParam;
import com.viper.database.utils.LimitedArrayList;

public class DatabaseMemory implements DatabaseInterface {

    private final static Logger log = Logger.getLogger(DatabaseMemory.class.getName());

    private final static Random random = new Random();
    private final static String RANDOM_CHARS = "ABCDEFGHIJKLMONPQRSTUVWXYZabcdefghijklmonpqrstuvwxyz0123456789";

    private final Map<String, List> cache = new HashMap<String, List>();
    private DatabaseConnection dbc = new DatabaseConnection();

    public static synchronized DatabaseInterface getInstance(DatabaseConnection dbc) {
        return new DatabaseMemory(dbc);
    }

    private DatabaseMemory(DatabaseConnection dbc) {
        this.dbc = dbc;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void release() {

    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> long size(Class<T> clazz) throws Exception {

        List list = cache.get(packKey(clazz));

        return (list == null) ? 0 : list.size();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<String> listDatabases() {
        List<String> items = new ArrayList<String>();
        for (String key : cache.keySet()) {
            items.add(unpackDatabase(key));
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
        for (String key : cache.keySet()) {
            items.add(unpackTable(key));
        }
        return items;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<String> listColumns(Class<T> clazz) {
        try {
            List<String> packagenames = dbc.getPackageNames();
            if (packagenames == null || packagenames.size() == 0) {
                throw new Exception("Package name for " + clazz.getName() + " is not defined.");
            }
            return DatabaseUtil.getColumnNames(clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<String>();
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
    public <T> void create(Class<T> tableClass) throws Exception {

    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void createDatabase(String packagename) {

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
    public <T> T query(Class<T> clazz, Object... pairs) throws Exception {

        List<T> beans = queryAll(clazz);
        return DatabaseUtil.findOneItem(beans, (String) pairs[0], pairs[1]);
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
    public <T> List<T> queryList(Class<T> clazz, Object... pairs) throws Exception {

        List<T> beans = queryAll(clazz);
        return DatabaseUtil.findAllItems(beans, pairs);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Predicate<T> filter, List<ColumnParam> columnParams,
            LimitParam limitParam, Map<String, String> parameters) throws Exception {

        List<T> beans = queryAll(clazz);
        return DatabaseUtil.applyFilter(beans, filter);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryAll(Class<T> clazz) throws Exception {

        String key = packKey(clazz);
        return (List<T>) cache.get(key);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> T update(T bean) throws Exception {
        updateInternal(bean);
        return bean;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> T insert(T bean) throws Exception {
        updateInternal(bean);
        return bean;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void insertAll(List<T> beans) throws Exception {
        for (T bean : beans) {
            updateInternal(bean);
        }
    }

    private <T> void updateInternal(T bean) throws Exception {

        String key = packKey(bean.getClass());
        List<T> list = (List<T>) cache.get(key);
        if (list == null) {
            list = createTable();

            cache.put(key, list);
        }

        if (!isValidPrimaryKeyValue(bean)) {
            Column primaryKeyColumn = getPrimaryKey(bean.getClass());
            if ("assigned".equalsIgnoreCase(primaryKeyColumn.idMethod())) {
                Method method = toMethod(bean.getClass(), primaryKeyColumn.field());
                DatabaseUtil.setValue(bean, primaryKeyColumn.field(), generateKeyValue(method, primaryKeyColumn));

            } else { // autoincrement
                Object keyValue = list.size() + 1;
                DatabaseUtil.setValue(bean, primaryKeyColumn.field(), keyValue);
            }
        }

        String primaryKey = DatabaseUtil.getPrimaryKeyName(bean.getClass());
        Object value = DatabaseUtil.getPrimaryKeyValue(bean);
        int current = DatabaseUtil.indexOf(list, primaryKey, value);
        if (current == -1) {
            list.add(bean);
        } else {
            list.set(current, bean);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(T bean) throws Exception {

        String key = packKey(bean.getClass());
        List<T> list = cache.get(key);

        if (list == null) {
            list = createTable();
            cache.put(key, list);
        }

        list.remove(bean);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void deleteAll(Class<T> clazz) throws Exception {

        String key = packKey(clazz);
        cache.remove(key);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(Class<T> clazz, Object... keyValue) throws Exception {

        String key = packKey(clazz);
        List<T> list = (List<T>) cache.get(key);
        delete(clazz, DatabaseUtil.findAllItems(list, keyValue));

    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<Object> uniqueValues(Class<T> clazz, String fieldname) throws Exception {

        List<T> list = queryAll(clazz);
        List<Object> values = new ArrayList<Object>();
        for (T bean : list) {
            Object value = DatabaseUtil.getValue(bean, fieldname);
            if (value != null && !values.contains(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private <T> boolean isValidPrimaryKeyValue(T bean) {
        try {
            Column primaryKeyColumn = getPrimaryKey(bean.getClass());
            Object value = DatabaseUtil.getValue(bean, primaryKeyColumn.field());
            if (value == null) {
                return false;
            } else if (value instanceof Integer && (Integer) value <= 0) {
                return false;
            } else if (value instanceof Long && (Long) value <= 0) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.throwing("isValidPrimaryKeyValue", "", ex);
        }
        return false;
    }

    private <T> Column getPrimaryKey(Class<T> clazz) throws Exception {
        Method[] fields = clazz.getMethods();
        if (fields != null) {
            for (Method field : fields) {
                Column column = field.getAnnotation(Column.class);
                if (column != null && column.primaryKey()) {
                    return column;
                }
            }
        }
        throw new Exception("No primary key defined for : " + clazz);
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
        System.err.println("Unable to find column with field name of " + fieldname + " in class " + clazz.getName());
        return null;
    }

    private Object generateKeyValue(Method method, Column column) {
        String typeName = method.getReturnType().getName();
        if ("String".equals(typeName)) {
            StringBuilder buf = new StringBuilder();
            int size = (column.size() <= 0) ? 8 : (int) column.size();
            for (int i = 0; i < size; i++) {
                int index = (int) (random.nextDouble() * RANDOM_CHARS.length());
                buf.append(RANDOM_CHARS.charAt(index));
            }
            return buf.toString();
        }
        return random.nextInt();
    }

    private String packKey(Class clazz) throws Exception {
        return packKey(DatabaseUtil.getDatabaseName(clazz), DatabaseUtil.getTableName(clazz));
    }

    private String packKey(String databaseName, String tableName) {
        return databaseName.toLowerCase() + "." + tableName.toLowerCase();
    }

    private String unpackDatabase(String key) {
        return key.substring(0, key.indexOf("."));
    }

    private String unpackTable(String key) {
        return key.substring(key.indexOf(".") + 1);
    }

    private <T> List<T> createTable() {
        int limit = dbc.getNumberOfRowsLimit();
        return new LimitedArrayList<T>(limit);
    }
}
