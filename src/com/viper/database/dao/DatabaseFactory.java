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
import java.util.HashMap;
import java.util.Map;

import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.DatabaseConnections;

/**
 * Factory to generate the database interface object (DAO).
 * 
 * Supported types of databases:
 * <ol>
 * <li>Mongo</li>
 * <li>jdbc</li>
 * <li>hbase</li>
 * <li>jta</li>
 * <li>memory</li>
 * </ol>
 *
 */

public class DatabaseFactory {

	private final static Map<String, DatabaseInterface> cache = new HashMap<String, DatabaseInterface>();
	
	private final static String DEFAULT_CONNECTION_FILENAME = "res:/databases.xml";
	private final static String CONNECTION_FILENAME_PROPERTY = "DATABASE_CONNECTION_FILENAME";
	
	private final static String getConnectionFilename() {
		// TODO use ResourceUtil.getResource
		String filename = System.getenv(CONNECTION_FILENAME_PROPERTY);
		if (filename == null || filename.length() == 0) {
			filename = System.getProperty(CONNECTION_FILENAME_PROPERTY);
		}
		if (filename == null || filename.length() == 0) {
            filename = DEFAULT_CONNECTION_FILENAME;
        }
		return filename;
	}

	/**
	 * Given the database connection name, open a database (dao) object, of the
	 * appropriate database type.
	 * 
	 * The connection name must be in a list of databases connections in the jar
	 * file root directory, under the filename databases.xml
	 * 
	 * Note: An added feature the MemoryAdapter this adds an intercepter to the
	 * existing DAO engines, which will cache the data. The cache can have a
	 * time out, and a size limit. (TODO add a refresh cache method which will
	 * force cache be refreshed on next access. Use of memory adapter is
	 * triggered by the cache timeout parameter and/or the number of rows limit
	 * parameter.
	 * 
	 * @param connectionName
	 *            the name of the connection, specified in the configuration
	 *            file of connections.
	 * 
	 * @return the database interface (dao) object
	 * 
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad
	 *             connection, etc.
	 */
	public final static DatabaseInterface getInstance(String connectionName) throws Exception {
		return getInstance(getConnectionFilename(), connectionName);
	}

	/**
	 * Given the database connection name, open a database (dao) object, of the
	 * appropriate database type.
	 * 
	 * The connection name must be in a list of databases connections in the jar
	 * file root directory, under the filename databases.xml
	 * 
	 * @param filename
	 *            the name of the database configuration file (XML), which
	 *            contains the list of database connections.
	 * @param connectionName
	 *            the name of the connection, specified in the configuration
	 *            file of connections.
	 * 
	 * @return the database (dao) object
	 * 
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad
	 *             connection, etc.
	 */
	public final static DatabaseInterface getInstance(String filename, String connectionName) throws Exception {

		DatabaseConnection connection = getDatabaseConnection(filename, connectionName);
		if (connection == null) {
			throw new Exception("Can't find connection in databases.xml:" + filename + "," + connectionName);
		}

		return getInstance(connection);
	}

	/**
	 * Given the database connection name, and the filename of database
	 * connections, return the named database connection object.
	 * 
	 * The connection name must be in a list of databases connections in the jar
	 * file root directory, under the filename databases.xml
	 * 
	 * @param filename
	 *            the name of the database configuration file (XML), which
	 *            contains the list of database connections.
	 * @param connectionName
	 *            the name of the connection, specified in the configuration
	 *            file of connections.
	 * 
	 * @return the database connection object.
	 * 
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad
	 *             connection, etc.
	 */
	public final static DatabaseConnection getDatabaseConnection(String connectionName) throws Exception {

		DatabaseConnections connections = DatabaseMapper.readConnections(getConnectionFilename());

		return DatabaseUtil.findOneItem(connections.getConnections(), "name", connectionName);
	}

	/**
	 * Given the database connection name, and the filename of database connections,
	 * return the named database connection object.
	 * 
	 * The connection name must be in a list of databases connections in the jar
	 * file root directory, under the filename databases.xml
	 * 
	 * @param filename
	 *            the name of the database configuration file (XML), which contains
	 *            the list of database connections.
	 * @param connectionName
	 *            the name of the connection, specified in the configuration file of
	 *            connections.
	 * 
	 * @return the database connection object.
	 * 
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc.
	 */
	public final static DatabaseConnection getDatabaseConnection(String filename, String connectionName)
			throws Exception {

		if (filename == null) {
			filename = getConnectionFilename();
		}

		DatabaseConnections connections = DatabaseMapper.readConnections(filename);
	
		return DatabaseUtil.findOneItem(connections.getConnections(), "name", connectionName);
	}

	/**
	 * Given the database connection parameters, open a database (dao) object,
	 * of the appropriate database type.
	 * 
	 * @param dbc
	 *            the database connection object
	 * 
	 * @return the database (dao) object
	 * 
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad
	 *             connection, etc.
	 */
	public final static synchronized DatabaseInterface getInstance(DatabaseConnection dbc) throws Exception {

		DatabaseInterface database = cache.get(dbc.getName());
		if (database != null) {
			return database;
		}

		if (dbc.getSsh() != null && dbc.getSsh().getLocalPort() != 0) {
			new DatabaseTunnel().doSshTunnel(dbc);
		}

		String url = dbc.getDatabaseUrl();
		if (url == null) {
			throw new Exception("Unknown database to be used: " + dbc.getName());
		}
		if (url.startsWith("mongodb:")) {
			database = getInstance("com.viper.database.dao.DatabaseMongoDB", dbc);

		} else if (url.startsWith("jdbc:")) {
			database = getInstance("com.viper.database.dao.DatabaseJDBC", dbc);

		} else if (url.startsWith("hbase:")) {
			database = getInstance("com.viper.database.dao.DatabaseHBase", dbc);

		} else if (url.startsWith("jta:")) {
			database = getInstance("com.viper.database.dao.DatabaseJTA", dbc);

		} else if (url.startsWith("mem:")) {
			database = getInstance("com.viper.database.dao.DatabaseMemory", dbc);

		} else {
			throw new Exception("Unknown database to be used: " + url);
		}

		if (useMemoryAdapter(dbc)) {
			database = new MemoryAdapter(database, dbc);
		}

		if (useInterceptorAdapter(dbc)) {
			database = new InterceptorAdapter(database, dbc);
		}

		cache.put(dbc.getName(), database);
		return database;
	}

	private final static DatabaseInterface getInstance(String classname, DatabaseConnection dbc) throws Exception {
		Class clazz = Class.forName(classname);
		Method method = clazz.getDeclaredMethod("getInstance", DatabaseConnection.class);
		return (DatabaseInterface) method.invoke(null, dbc);
	}

	private final static boolean useMemoryAdapter(DatabaseConnection dbc) {
		return (dbc.getCacheTimeout() > 0 || dbc.getNumberOfRowsLimit() > 0);
	}

	private final static boolean useInterceptorAdapter(DatabaseConnection dbc) {
		return true;
	}
	
	
	public final static synchronized void release(String name) throws Exception {

        DatabaseInterface database = cache.get(name);
        if (database != null) {
            cache.remove(name);
            database.release();
        }
	}
}
