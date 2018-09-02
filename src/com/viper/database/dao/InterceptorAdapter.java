/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.viper.database.annotations.Table;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.Row;

public class InterceptorAdapter implements DatabaseSQLInterface {

    private final static Logger log = Logger.getLogger(InterceptorAdapter.class.getName());

    private final static Map<Class<?>, DatabaseInterface> cache = new HashMap<Class<?>, DatabaseInterface>();

    private DatabaseInterface dao = null;
    private DatabaseConnection dbc = null;

    public InterceptorAdapter(DatabaseInterface dao, DatabaseConnection dbc) {
        this.dao = dao;
        this.dbc = dbc;
    }

    public DatabaseInterface getUnderlyingDaoLayer() {
        return dao;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void release() {
        dao.release();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> long size(Class<T> clazz) throws Exception {
        return loadAdapterClass(clazz).size(clazz);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<String> listDatabases() {
        return dao.listDatabases();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<String> listTables(String databasename) {
        return dao.listTables(databasename);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<String> listColumns(Class<T> clazz) {
        return loadAdapterClass(clazz).listColumns(clazz);
    }

    /**
     * {@inheritDoc}
     * 
     * Note: currently this method is not implemented.
     */
    @Override
    public <T> boolean hasChanged(Class<T> clazz) {
        return loadAdapterClass(clazz).hasChanged(clazz);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void create(Class<T> clazz) throws Exception {
        loadAdapterClass(clazz).create(clazz);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void createDatabase(String packagename) throws Exception {
        dao.createDatabase(packagename);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> T query(Class<T> clazz, Object... pairs) throws Exception {
        return loadAdapterClass(clazz).query(clazz, pairs);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Object... pairs) throws Exception {
        return loadAdapterClass(clazz).queryList(clazz, pairs);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Predicate<T> filter) throws Exception {
        return loadAdapterClass(clazz).queryList(clazz, filter);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryAll(Class<T> clazz) throws Exception {
        return loadAdapterClass(clazz).queryAll(clazz);
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
        return loadAdapterClass(bean.getClass()).insert(bean);
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
        Class<T> clazz = (Class<T>) beans.get(0).getClass();
        loadAdapterClass(clazz).insertAll(beans);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(T bean) throws Exception {
        loadAdapterClass(bean.getClass()).delete(bean);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void deleteAll(Class<T> clazz) throws Exception {
        loadAdapterClass(clazz).deleteAll(clazz);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(Class<T> clazz, Object... keyValue) throws Exception {
        loadAdapterClass(clazz).delete(clazz, keyValue);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<Object> uniqueValues(Class<T> clazz, String fieldname) throws Exception {
        return loadAdapterClass(clazz).uniqueValues(clazz, fieldname);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Row> readRows(String sql) throws Exception {
        return ((DatabaseSQLInterface) dao).readRows(sql);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> querySQL(Class<T> clazz, String sql) throws Exception {
        return ((DatabaseSQLInterface) loadAdapterClass(clazz)).querySQL(clazz, sql);

    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Row> readMetaRows(String metaName) throws Exception {
        return ((DatabaseSQLInterface) dao).readMetaRows(metaName);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void write(String sql) throws Exception {
        ((DatabaseSQLInterface) dao).write(sql);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void write(List<String> sql) throws Exception {
        ((DatabaseSQLInterface) dao).write(sql);
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

    private <T> DatabaseInterface loadAdapterClass(Class<T> clazz) {

        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        }

        DatabaseInterface wrapper = dao;

        try {
            Table table = DatabaseUtil.getTableAnnotation(clazz);
            if (table.queryClassName() != null && table.queryClassName().trim().length() > 0) {
                Class queryClazz = Class.forName(table.queryClassName());
                Constructor constructor = queryClazz.getDeclaredConstructor(DatabaseSQLInterface.class, DatabaseConnection.class);
                wrapper = (DatabaseInterface) constructor.newInstance(dao, dbc);
                cache.put(clazz, wrapper);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return wrapper;
    }
}
