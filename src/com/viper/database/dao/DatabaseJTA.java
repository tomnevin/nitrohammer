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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.sql.DataSource;

import com.viper.database.filters.Predicate;
import com.viper.database.model.ColumnParam;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.LimitParam;
import com.viper.database.model.Row;

public class DatabaseJTA implements DatabaseInterface, DatabaseSQLInterface {

    private final static Logger log = Logger.getLogger(DatabaseJTA.class.getName());

    public final static String DEFAULT_PROVIDER_URL = "file:///tmp/JNDI";
    public final static String DEFAULT_FACTORY_CLASSNAME = "com.sun.jndi.fscontext.RefFSContextFactory";

    private EntityManager em = null;
    private DatabaseConnection dbc = new DatabaseConnection();

    public final static synchronized DatabaseInterface getInstance(DatabaseConnection dbc) {
        return new DatabaseJTA(dbc);
    }

    private DatabaseJTA(DatabaseConnection dbc) {
        this.dbc = dbc;
    }

    private final synchronized EntityManager getSession() {
        if (em == null) {
            String persistenceUnit = dbc.getDatasource();
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit);
            em = emf.createEntityManager();
        }
        return em;
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
        return 0;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<String> listDatabases() {
        List<String> items = new ArrayList<String>();
        return items;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<String> listTables(String databasename) {
        List<String> items = new ArrayList<String>();
        return items;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<String> listColumns(Class<T> clazz) {
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
    public <T> List<Object> uniqueValues(Class<T> tableClass, String fieldName) throws Exception {
        // TODO Auto-generated method stub
        return null;
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
    public <T> T update(T bean) throws Exception {
        return insert(bean);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> T insert(T bean) throws Exception {
        EntityManager s = getSession();
        try {
            s.getTransaction().begin();
            s.persist(bean);
            s.refresh(bean);
            s.flush();
            s.getTransaction().commit();
            return bean;
        } catch (Exception ex) {
            rollback(s);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void insertAll(List<T> beans) throws Exception {
        EntityManager s = getSession();
        try {
            s.getTransaction().begin();
            for (T row : beans) {
                s.persist(row);
            }
            s.getTransaction().commit();
        } catch (Exception ex) {
            rollback(s);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> T query(Class<T> clazz, Object... pairs) throws Exception {
        try {
            String ejbsql = "select e from " + toTableName(clazz) + " as e" + where(pairs);
            EntityManager s = getSession();
            return clazz.cast(setParameters(s.createQuery(ejbsql), pairs).getSingleResult());
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Object... pairs) throws Exception {
        EntityManager s = getSession();
        String ejbsql = "select e from " + toTableName(clazz) + " as e" + where(pairs);
        return setParameters(s.createQuery(ejbsql), pairs).getResultList();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryList(Class<T> clazz, Predicate<T> filter,  List<ColumnParam> columnParams, LimitParam limitParam) throws Exception {

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> queryAll(Class<T> clazz) throws Exception {
        EntityManager s = getSession();
        String ejbsql = "select e from " + toTableName(clazz) + " as e";
        return s.createQuery(ejbsql).getResultList();
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void write(String sql) throws Exception {

    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void write(List<String> sql) throws Exception {

    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public List<Row> readRows(String sql) throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> List<T> querySQL(Class<T> clazz, String sql) throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(T obj) throws Exception {
        EntityManager s = getSession();
        try {
            s.getTransaction().begin();
            s.remove(obj);
            s.getTransaction().commit();
        } catch (Exception ex) {
            rollback(s);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void delete(Class<T> clazz, Object... keyValue) throws Exception {
        if (keyValue == null) {
            return;
        }
        EntityManager s = getSession();
        try {
            s.getTransaction().begin();
            String ejbsql = "select e from " + toTableName(clazz) + " as e" + where(keyValue);
            List<T> beans = setParameters(s.createQuery(ejbsql), keyValue).getResultList();
            for (T row : beans) {
                s.remove(row);
            }
            s.getTransaction().commit();
        } catch (Exception ex) {
            rollback(s);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> void deleteAll(Class<T> clazz) throws Exception {
        delete(queryAll(clazz));
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

    // -------------------------------------------------------------------------
    private Query setParameters(Query query, Object[] pairs) {
        if (pairs == null) {
            return query;
        }
        for (int index = 0; index < pairs.length; index++) {
            String name = (String) pairs[index];
            if ("limit".equalsIgnoreCase(name)) {
                query.setFirstResult((Integer) pairs[++index]);
                query.setMaxResults((Integer) pairs[++index]);
            } else if ("cacheable".equalsIgnoreCase(name)) {
                Object value = pairs[++index];
                // query.setCacheable((Boolean) value);
            } else if (pairs[index + 1] instanceof java.util.Date) {
                query.setParameter(name, (java.util.Date) pairs[++index], TemporalType.DATE);
            } else if (pairs[index + 1] instanceof Date) {
                query.setParameter(name, (Date) pairs[++index], TemporalType.DATE);
            } else if (pairs[index + 1] instanceof Time) {
                query.setParameter(name, (Time) pairs[++index], TemporalType.TIME);
            } else if (pairs[index + 1] instanceof Timestamp) {
                query.setParameter(name, (Timestamp) pairs[++index], TemporalType.TIMESTAMP);
            } else {
                query.setParameter(name, pairs[++index]);
            }
        }
        return query;
    }

    private PreparedStatement setParameters(PreparedStatement stmt, String[] parameters) throws Exception {
        if (parameters == null) {
            return stmt;
        }
        int index = 1;
        for (String parameter : parameters) {
            stmt.setString(index, parameter);
            index = index + 1;
        }
        return stmt;
    }

    private Connection getConnection() throws Exception {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_FACTORY_CLASSNAME);
        env.put(Context.PROVIDER_URL, DEFAULT_PROVIDER_URL);

        String persistenceUnit = dbc.getDatasource();

        Context ctx = new InitialContext(env); // TODO (properties);
        DataSource ds = (DataSource) ctx.lookup(persistenceUnit);
        return ds.getConnection();
    }

    private String where(Object pairs[]) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < pairs.length; i = i + 2) {
            if (i == 0) {
                buf.append(" where ");
            }
            buf.append("e.");
            buf.append(pairs[i]);
            buf.append("=:");
            buf.append(pairs[i]);
        }
        return buf.toString();
    }

    private void rollback(EntityManager s) {
        if (s.getTransaction() != null && s.getTransaction().isActive()) {
            s.getTransaction().rollback();
        }
    }

    private String toTableName(Class clazz) {
        return clazz.getName();
    }

}
