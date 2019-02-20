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

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.converters.Converters;
import com.viper.database.utils.JSONUtil; 

public class RestService implements RestServiceInterface {

    private static final String getDatabaseName() throws Exception {
        return com.viper.database.utils.ResourceUtil.getResource("DATABASE_LOCATOR", "test");
    }

    @Override
    public final <T> Response query(Class<T> clazz, Locale locale, List<String> whereClause) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        T bean = database.query(clazz, (Object[]) whereClause.toArray());

        return Response.ok(JSONUtil.toJSON(bean), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public final <T> Response query(Class<T> clazz, Locale locale, String key, String value) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        T bean = database.query(clazz, key, value);

        return Response.ok(JSONUtil.toJSON(bean), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public final <T> Response queryList(Class<T> clazz, Locale locale, String key, String value) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        List<T> beans = database.queryList(clazz, key, value);

        return Response.ok(JSONUtil.toJSON(beans), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response queryList(Class<T> clazz, Locale locale, MultivaluedMap<String, String> queryParams) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public final <T> Response queryAll(Class<T> clazz, Locale locale) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        List<T> beans = database.queryAll(clazz);

        return Response.ok(JSONUtil.toJSON(beans), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public final <T> Response update(Class<T> clazz, String request) throws Exception {

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
    public final <T> Response createItem(Class<T> clazz, String request) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        T bean = Converters.convert(clazz, request);

        database.insert(bean);

        return Response.ok(JSONUtil.toJSON(bean), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public final <T> Response createItems(Class<T> clazz, String request) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        List<T> beans = Converters.convertToList(clazz, request);

        database.insertAll(beans);

        return Response.ok(JSONUtil.toJSON(beans), MediaType.APPLICATION_JSON).build();
    }

    @Override
    public final <T> Response deleteItem(Class<T> clazz, String key, String value) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        database.delete(clazz, key, value);

        return Response.ok().build();
    }

    @Override
    public final <T> Response deleteItem(Class<T> clazz, String request) throws Exception {

        String DatabaseInstanceName = getDatabaseName();
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);

        T bean = Converters.convert(clazz, request);

        database.delete(bean);

        return Response.ok().build();
    }

//    public final <T> Response dataGetResponse(Class<T> clazz, Locale locale, MultivaluedMap<String, String> queryParams)
//            throws Exception {
//
//        String DatabaseInstanceName = getDatabaseName();
//
//        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DatabaseInstanceName);
//
//        List<T> beans;
//        if (queryParams == null) {
//            beans = dao.queryAll(clazz);
//        } else {
//            beans = dao.querySQL(clazz, AgTablesUtil.toWhereClause(queryParams, clazz));
//        }
//
//        HeaderDef headerDef = AgTablesUtil.data(locale, clazz, beans);
//
//        return Response.ok(JSONUtil.toJSON(headerDef), MediaType.APPLICATION_JSON).build();
//    }
//
//    public final <T> Response dataPostResponse(Class<T> clazz, Locale locale, MultivaluedMap<String, String> queryParams)
//            throws Exception {
//
//        String DatabaseInstanceName = getDatabaseName();
//
//        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DatabaseInstanceName);
//
//        List<T> beans;
//        if (queryParams == null) {
//            beans = dao.queryAll(clazz);
//        } else {
//            beans = dao.querySQL(clazz, AgTablesUtil.toWhereClause(queryParams, clazz));
//        }
//
//        HeaderDef headerDef = AgTablesUtil.data(locale, clazz, beans);
//
//        return Response.ok(JSONUtil.toJSON(headerDef), MediaType.APPLICATION_JSON).build();
//    }
//
//    public final <T> Response dataTableResponse(Class<T> clazz, Locale locale) throws Exception {
//
//        HeaderDef headerDef = AgTablesUtil.header(locale, clazz);
//
//        return Response.ok(JSONUtil.toJSON(headerDef), MediaType.APPLICATION_JSON).build();
//    }
//
//    public final <T> Response tableResponse(Class<T> clazz, Locale locale, MultivaluedMap<String, String> queryParams)
//            throws Exception {
//
//        String DatabaseInstanceName = getDatabaseName();
//
//        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DatabaseInstanceName);
//
//        List<T> beans;
//        if (queryParams == null) {
//            beans = dao.queryAll(clazz);
//        } else {
//            beans = dao.querySQL(clazz, AgTablesUtil.toWhereClause(queryParams, clazz));
//        }
//
//        HeaderDef headerDef = AgTablesUtil.table(locale, clazz, beans);
//
//        return Response.ok(JSONUtil.toJSON(headerDef), MediaType.APPLICATION_JSON).build();
//    }
//
//    public final <T> Response dataResponses(Class<T> clazz, Locale locale, String key, String value) throws Exception {
//
//        String DatabaseInstanceName = getDatabaseName();
//        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);
//
//        List<T> beans = AgTablesUtil.getBeansByForeignKeys(database, clazz.getName(), clazz, key, value);
//
//        Form form = AgTablesUtil.form(locale, clazz.getName(), beans);
//
//        return Response.ok(JSONUtil.toJSON(form), MediaType.APPLICATION_JSON).build();
//    }
//
//    public final <T> Response dataResponses(Class<T> clazz, Locale locale) throws Exception {
//
//        String DatabaseInstanceName = getDatabaseName();
//        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);
//
//        List<T> beans = database.queryAll(clazz);
//
//        Form form = AgTablesUtil.form(locale, clazz.getName(), beans);
//
//        return Response.ok(JSONUtil.toJSON(form), MediaType.APPLICATION_JSON).build();
//    }
}
