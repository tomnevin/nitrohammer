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

import java.util.List;

import com.viper.database.model.Row;

/**
 * The following interface defines all routines in the database (dao) object. To
 * create a dao interface call the database factory.
 *
 */

public interface DatabaseSQLInterface extends DatabaseInterface {

	/**
	 * Given a sql query return a list of row objects
	 * 
	 * @param sql
	 *            the query string, select * from table
	 * @return the list of row objects, for the matches in the query,
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc. Note the returned list will NOT be null, will be valid list
	 *             of zero to N length.
	 */
	public List<Row> readRows(String sql) throws Exception;

	/**
	 * Given a SQL query return a list of bean objects, Let the caller of this
	 * method be fore warned, you must match the bean class to the expected result
	 * set.
	 * 
	 * @param sql
	 *            the query string, select * from table
	 * @return the list of bean objects, for the matches in the query,
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc. Note the returned list will NOT be null, will be valid list
	 *             of zero to N length.
	 */
	public <T> List<T> querySQL(Class<T> clazz, String sql) throws Exception;

	/**
	 * Given a meta data category query the databaseMetaData object and return a
	 * list of row objects
	 * 
	 * @param metaName
	 *            the category of metadata, see DatabaseMetaData.get<metaName>
	 * 
	 * @return the list of row objects, for the matches in the query,
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc. Note the returned list will NOT be null, will be valid list
	 *             of zero to N length.
	 */
	public List<Row> readMetaRows(String metaName) throws Exception;

	/**
	 * Write a sql update or insert into the database.
	 * 
	 * @param sql
	 *            the update or insert string
	 * @throws Exception
	 *             failure to write table data, no database, no table, bad
	 *             connection, etc.
	 */
	public void write(String sql) throws Exception;

	/**
	 * Write a list of sql update or insert into the database.
	 * 
	 * @param sql
	 *            the list of update or insert string
	 * @throws Exception
	 *             failure to write table data, no database, no table, bad
	 *             connection, etc.
	 */
	public void write(List<String> sql) throws Exception;

	/**
	 * Write a sql update or insert into the database.
	 * 
	 * @param sql
	 *            the update or insert string
	 * @throws Exception
	 *             failure to write table data, no database, no table, bad
	 *             connection, etc.
	 */
	public <T> List<T> executeQuery(Class<T> clazz, String sql, Object... params) throws Exception;

	/**
	 * Write a sql update or insert into the database.
	 * 
	 * @param sql
	 *            the update or insert string
	 * @throws Exception
	 *             failure to write table data, no database, no table, bad
	 *             connection, etc.
	 */
	public int executeUpdate(String sql, Object... params) throws Exception;

}
