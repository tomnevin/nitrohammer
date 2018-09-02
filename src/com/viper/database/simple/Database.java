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

package com.viper.database.simple;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.viper.database.dao.drivers.SQLConversionTables;
import com.viper.database.security.Encryptor;

public class Database {

	protected Connection connection;

	/**
	 * Open a connection to the database.
	 * 
	 * @param driver
	 *            The database driver to use.
	 * @param url
	 *            The database connection URL to use.
	 * @throws Exception
	 *             Thrown if an error occurs while connecting.
	 */
	public void connect(String driver, String url) throws Exception {
		try {
			Class.forName(driver).newInstance();
			connection = DriverManager.getConnection(url);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	/**
	 * Open a connection to the database.
	 * 
	 * @param driver
	 *            The database driver to use.
	 * @param url
	 *            The database connection URL to use.
	 * @param username
	 * @param password
	 * @throws Exception
	 *             Thrown if an error occurs while connecting.
	 */
	public void connect(String driver, String url, String username, String password) throws Exception {
		try {
			Class.forName(driver).newInstance();

			String decPassword = new Encryptor().decryptPassword(password);
			connection = DriverManager.getConnection(url, username, decPassword);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	/**
	 * Called to close the database.
	 * 
	 * @throws Exception
	 *             Thrown if the connection cannot be closed.
	 */
	public void close() throws Exception {
		try {
			connection.close();
		} catch (SQLException e) {
			throw (new Exception(e));
		}
	}

	/**
	 * Check to see if the specified type is numeric.
	 * 
	 * @param type
	 *            The type to check.
	 * @return Returns true if the type is numeric.
	 */
	public boolean isNumeric(int type) {
		if (type == java.sql.Types.BIGINT || type == java.sql.Types.DECIMAL || type == java.sql.Types.DOUBLE
				|| type == java.sql.Types.FLOAT || type == java.sql.Types.INTEGER || type == java.sql.Types.NUMERIC
				|| type == java.sql.Types.SMALLINT || type == java.sql.Types.TINYINT) {
			return true;
		}
		return false;

	}

	/**
	 * Generate the DROP statement for a table.
	 * 
	 * @param database
	 * @param table
	 *            The name of the table to drop.
	 * @return The SQL to drop a table.
	 */
	public String generateDrop(String database, String table) {
		StringBuffer result = new StringBuffer();
		result.append("DROP TABLE ");
		result.append(toTableName(database, table));
		result.append(";\n");
		return result.toString();
	}

	/**
	 * Generate the create statement to create the specified table.
	 * 
	 * @param database
	 * @param table
	 *            The table to generate a create statement for.
	 * @return The create table statement.
	 * @throws Exception
	 *             If a database error occurs.
	 */
	public String generateCreate(String database, String table) throws Exception {
		StringBuffer result = new StringBuffer();

		List<Row> columns = listColumnInfo(table);

		result.append("CREATE TABLE ");
		result.append(toTableName(database, table));
		result.append(" ( ");

		for (int i = 0; i < columns.size(); i++) {
			if (i != 0) {
				result.append(',');
			}
			Row column = columns.get(i);

			String dataType = (String) column.getValue("type_name");

			result.append(column.getValue("column_name"));
			result.append(' ');
			result.append(dataType);

			int precision = column.getInt("column_size");
			int scale = column.getInt("decimal_digits");
			if (precision < 65535) {
				result.append('(');
				result.append(precision);
				if (scale > 0) {
					result.append(',');
					result.append(scale);
				}
				result.append(") ");
			} else {
				result.append(' ');
			}

			if (column.getValue("is_signed") != null && !(Boolean) column.getValue("is_signed")) {
				result.append("UNSIGNED ");
			}

			if (((String) column.getValue("is_nullable")).equalsIgnoreCase("no")) {
				result.append("NOT NULL ");
			} else {
				result.append("NULL ");
			}
			if (((String) column.getValue("is_autoincrement")).equalsIgnoreCase("yes")) {
				result.append(" auto_increment");
			}
		}

		DatabaseMetaData dbm = connection.getMetaData();
		ResultSet primary = dbm.getPrimaryKeys(null, null, table);
		boolean first = true;
		while (primary.next()) {
			if (first) {
				first = false;
				result.append(',');
				result.append("PRIMARY KEY(");
			} else {
				result.append(",");
			}
			result.append(primary.getString("COLUMN_NAME"));
		}

		if (!first) {
			result.append(')');
		}

		result.append(" ); ");

		return result.toString();
	}

	/**
	 * Execute a SQL query and return a ResultSet.
	 * 
	 * @param sql
	 *            The SQL query to execute.
	 * @return The ResultSet generated by the query.
	 * @throws Exception
	 *             If a database error occurs.
	 */
	public ResultSet executeQuery(String sql) throws Exception {
		Statement stmt = null;

		try {
			stmt = connection.createStatement();
			return stmt.executeQuery(sql);
		} catch (SQLException e) {
			throw (new Exception(sql, e));
		}
	}

	/**
	 * 
	 * @param sql
	 *            The SQL query to execute.
	 * @return
	 * @throws Exception
	 */

	public List<Row> executeQueryRows(String sql) throws Exception {
		List<Row> rows = new ArrayList<Row>();
		ResultSet rs = null;
		try {
			rs = executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			List<String> columnNames = new ArrayList<String>(rsmd.getColumnCount());
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				columnNames.add(rsmd.getColumnName(i + 1).toLowerCase());
			}
			while (rs.next()) {
				Row row = new Row();
				rows.add(row);
				for (int i = 0; i < rsmd.getColumnCount(); i++) {
					row.setValue(columnNames.get(i), rs.getObject(i + 1));
				}
			}
		} catch (SQLException e) {
			throw new Exception(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		return rows;
	}

	/**
	 * 
	 * @param rows1
	 * @param rows2
	 * @param columnName1
	 * @param columnName2
	 * @return
	 * @throws Exception
	 */

	public List<Row> join(List<Row> rows1, List<Row> rows2, String columnName1, String columnName2) throws Exception {
		List<Row> rows = new ArrayList<Row>();
		for (Row row1 : rows1) {
			Object value1 = row1.getValue(columnName1);
			for (Row row2 : rows2) {
				Object value2 = row2.getValue(columnName2);
				if ((value1 == null && value2 == null) || (value1 != null && value1.equals(value2))) {
					Row row = new Row();
					row.putAll(row1);
					row.putAll(row2);
					rows.add(row);
				}
			}
		}
		return rows;
	}

	/**
	 * Execute a INSERT, DELETE, UPDATE, or other statement that does not return a
	 * ResultSet.
	 * 
	 * @param sql
	 *            The query to execute.
	 * @throws Exception
	 *             If a database error occurs.
	 */
	public void execute(String sql) throws Exception {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			throw (new Exception(sql, e));
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * Execute a INSERT, DELETE, UPDATE, or other statement that does not return a
	 * ResultSet.
	 * 
	 * @throws Exception
	 *             If a database error occurs.
	 */
	public void commit() throws Exception {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw (new Exception(e));
		}
	}

	/**
	 * Get a list of all tables in the database.
	 * 
	 * @return A list of all tables in the database.
	 * @throws Exception
	 *             If a database error occurs.
	 */
	public List<String> listTables() throws Exception {
		List<String> result = new ArrayList<String>();
		ResultSet rs = null;

		try {
			DatabaseMetaData dbm = connection.getMetaData();

			String types[] = { "TABLE" };
			rs = dbm.getTables(null, null, "%", null);

			while (rs.next()) {
				String str = rs.getString("TABLE_NAME");
				result.add(str);
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	/**
	 * Determine if a table exists.
	 * 
	 * @param table
	 *            The name of the table.
	 * @return True if the table exists.
	 * @throws Exception
	 *             A database error occurred.
	 */
	public boolean tableExists(String table) throws Exception {
		boolean result = false;
		ResultSet rs = null;

		try {
			DatabaseMetaData dbm = connection.getMetaData();

			String types[] = { "TABLE" };
			rs = dbm.getTables(null, null, table, null);
			result = rs.next();
			rs.close();
		} catch (SQLException e) {
			throw (new Exception(e));
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	/**
	 * Get a list of all of the columns on a table.
	 * 
	 * @param table
	 *            The table to check.
	 * @return A list of all of the columns.
	 * @throws Exception
	 *             If a database error occurs.
	 */
	public List<String> listColumns(String table) throws Exception {
		List<String> result = new ArrayList<String>();
		ResultSet rs = null;

		try {

			DatabaseMetaData dbm = connection.getMetaData();
			rs = dbm.getColumns(null, null, table, "%");
			while (rs.next()) {
				result.add(rs.getString("COLUMN_NAME").toLowerCase());
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	/**
	 * Get a list of all of the columns on a table.
	 * 
	 * @param tablename
	 *            The table to check.
	 * @return A list of all of the columns.
	 * @throws Exception
	 *             If a database error occurs.
	 */

	public List<Row> listColumnInfo(String tablename) throws Exception {
		List<Row> result = new ArrayList<Row>();
		ResultSet rs = null;

		try {
			DatabaseMetaData dbm = connection.getMetaData();
			rs = dbm.getColumns(null, null, tablename, null);
			ResultSetMetaData rsmd = rs.getMetaData();
			List<String> columnNames = new ArrayList<String>(rsmd.getColumnCount());
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				columnNames.add(rsmd.getColumnName(i + 1).toLowerCase());
			}
			while (rs.next()) {
				Row row = new Row();
				result.add(row);
				for (int i = 0; i < rsmd.getColumnCount(); i++) {
					row.setValue(columnNames.get(i), rs.getObject(i + 1));
				}
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	/**
	 * Create a prepared statement.
	 * 
	 * @param sql
	 *            The SQL of the prepared statement.
	 * @return The PreparedStatement that was created.
	 * @throws Exception
	 *             If a database error occurs.
	 */
	public PreparedStatement prepareStatement(String sql) throws Exception {
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			throw (new Exception(e));
		}
		return statement;
	}

	private String toTableName(String database, String table) {
		if (database == null) {
			return table;
		}
		return database + "." + table;
	}
}
