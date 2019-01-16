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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.viper.database.annotations.Table;
import com.viper.database.filters.Predicate;
import com.viper.database.filters.SortByOrder;
import com.viper.database.model.ColumnParam;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.LimitParam;
import com.viper.database.model.Row;

public class MemoryAdapter implements DatabaseSQLInterface {

    private final static Logger log = Logger.getLogger(MemoryAdapter.class.getName());

    private long TIMEOUT = 1 * 60 * 1000;
    private int NUMBER_OF_ROWS = 10000;

    private static final Map<Class, Long> accessTime = new HashMap<Class, Long>();
    private static final Map<Class, List> cache = new HashMap<Class, List>();

    private DatabaseInterface dao = null;
    private DatabaseConnection dbc = null;

    public MemoryAdapter(DatabaseInterface dao, DatabaseConnection dbc) {
        this.dao = dao;
        this.dbc = dbc;

        TIMEOUT = dbc.getCacheTimeout();
        NUMBER_OF_ROWS = dbc.getNumberOfRowsLimit();
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

    public void refresh() {
        accessTime.clear();
        cache.clear();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> long size(Class<T> clazz) throws Exception {
        if (!isCached(clazz)) {
            return dao.size(clazz);
        }
        load(clazz, false);
        return getCache(clazz).size();
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
        return dao.listColumns(clazz);
    }

    /**
     * {@inheritDoc}
     * 
     * Note: currently this method is not implemented.
     */
    @Override
    public <T> boolean hasChanged(Class<T> clazz) {
        return dao.hasChanged(clazz);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void create(Class<T> tableClass) throws Exception {
        dao.create(tableClass);
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
        if (!isCached(clazz)) {
            return dao.query(clazz, pairs);
        }
        load(clazz, false);
        T bean = (T) filter(getCache(clazz), pairs);
        return bean;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Object... pairs) throws Exception {
        if (!isCached(clazz)) {
            return dao.queryList(clazz, pairs);
        }
        load(clazz, false);
        return filterList(getCache(clazz), pairs);
    }

    /**
     * {@inheritDoc}
     * 
     */
    // CALLS TO HERE ARE NOT CACHED.
    public <T> List<T> queryList(Class<T> clazz, Map<String, String> parameters) throws Exception {
        return dao.queryList(clazz, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Predicate<T> filter, List<ColumnParam> columnParams,
            LimitParam limitParam, Map<String, String> parameters) throws Exception {

        if (!isCached(clazz)) {
            return dao.queryList(clazz, filter, columnParams, limitParam, parameters);
        }

        load(clazz, false);

        List<T> beans = getCache(clazz);
        List<T> filteredBeans = DatabaseUtil.applyFilter(beans, filter);
        List<T> groupedBeans = DatabaseUtil.applyGroupBy(filteredBeans, columnParams);
        Collections.sort(groupedBeans, new SortByOrder(columnParams));
        return groupedBeans;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryAll(Class<T> clazz) throws Exception {
        if (!isCached(clazz)) {
            return dao.queryAll(clazz);
        }
        load(clazz, false);
        return new ArrayList<T>(getCache(clazz));
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
        dao.insert(bean);

        if (!isCached(bean.getClass())) {
            load(bean.getClass(), true);
        }
        return bean;
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
        dao.insertAll(beans);

        if (!isCached(beans.get(0).getClass())) {
            load(beans.get(0).getClass(), true);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(T bean) throws Exception {
        dao.delete(bean);

        if (!isCached(bean.getClass())) {
            getCache(bean.getClass()).remove(bean);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void deleteAll(Class<T> clazz) throws Exception {
        dao.deleteAll(clazz);

        if (!isCached(clazz)) {
            getCache(clazz).clear();
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(Class<T> clazz, Object... keyValue) throws Exception {
        dao.delete(clazz, keyValue);

        if (!isCached(clazz)) {
            load(clazz, true);
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<Object> uniqueValues(Class<T> clazz, String fieldname) throws Exception {
        return dao.uniqueValues(clazz, fieldname);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Row> readRows(String sql) throws Exception {
        if (!(dao instanceof DatabaseSQLInterface)) {
            return new ArrayList<Row>();
        }
        return ((DatabaseSQLInterface) dao).readRows(sql);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> querySQL(Class<T> clazz, String sql) throws Exception {
        if (!(dao instanceof DatabaseSQLInterface)) {
            return new ArrayList<T>();
        }
        return ((DatabaseSQLInterface) dao).querySQL(clazz, sql);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void write(String sql) throws Exception {
        if (!(dao instanceof DatabaseSQLInterface)) {
            return;
        }
        ((DatabaseSQLInterface) dao).write(sql);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void write(List<String> sql) throws Exception {
        if (!(dao instanceof DatabaseSQLInterface)) {
            return;
        }
        ((DatabaseSQLInterface) dao).write(sql);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> executeQuery(Class<T> clazz, String sql, Object... params) throws Exception {
        if (!(dao instanceof DatabaseSQLInterface)) {
            return new ArrayList<T>();
        }
        return ((DatabaseSQLInterface) dao).executeQuery(clazz, sql, params);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public int executeUpdate(String sql, Object... params) throws Exception {
        if (!(dao instanceof DatabaseSQLInterface)) {
            return -1;
        }
        return ((DatabaseSQLInterface) dao).executeUpdate(sql, params);
    }

    private final synchronized <T> void load(Class<T> clazz, boolean force) throws Exception {
        if (isCacheInvalid(clazz, force)) {
            List<T> beans = dao.queryAll(clazz);

            log.fine("CACHE RELOADED: " + clazz.getName() + "," + beans.size());

            cache.put(clazz, beans);
            accessTime.put(clazz, System.currentTimeMillis());
        }
    }

    private final <T> boolean isCacheInvalid(Class<T> clazz, boolean force) {
        boolean invalid = (!cache.containsKey(clazz) || force || isTimedOut(clazz) || dao.hasChanged(clazz));
        if (invalid) {
            log.fine("CACHE RELOADING: " + clazz.getName() + "," + force + ":" + isTimedOut(clazz));
        }
        return invalid;
    }

    private final <T> boolean isTimedOut(final Class<T> clazz) {
        long toe = System.currentTimeMillis();
        Long last = accessTime.get(clazz);

        return (last == null || (TIMEOUT > 0 && (toe - last) > TIMEOUT));
    }

    private final <T> T filter(List<T> beans, Object[] pairs) {
        for (T bean : beans) {
            if (DatabaseUtil.isMatch(bean, pairs)) {
                return bean;
            }
        }
        return null;
    }

    private final <T> List<T> filterList(List<T> beans, Object[] pairs) {
        List<T> subset = new ArrayList<T>();
        for (T bean : beans) {
            if (DatabaseUtil.isMatch(bean, pairs)) {
                subset.add(bean);
            }
        }
        return subset;
    }

    private <T> boolean isCached(Class<T> clazz) throws Exception {
      
        Table table = DatabaseUtil.getTableAnnotation(clazz);
        if (table.isLargeTable()) {
            return false;
        }
        if ("bean".equalsIgnoreCase(table.tableType())) {
            return false;
        }
//        if (dao.size(clazz) > 100000) {
//            return false;
//        }
        return true;
    }

    private <T> List<T> getCache(Class<T> clazz) throws Exception {
        if (!cache.containsKey(clazz)) {
            cache.put(clazz, new ArrayList<T>());
        }
        return cache.get(clazz);
    }
}
