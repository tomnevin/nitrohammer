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

package com.viper.test.schema;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.DatabaseWriter;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.managers.DatabaseMgr;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.Table;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestJDBCColumn extends AbstractTestCase {

	private static final String TEST_FILENAME = "res:/com/viper/test/schema/TestJDBCColumn.xml";
	private static final String DatabaseName = "test";
	private static final String TableName = "classroom";
	private static final String ColumnName = "teacher";

	private static final SQLDriver driver = new SQLDriver();

	private static Databases databases = null;
	private static DatabaseSQLInterface dao = null;

	static {
		Logger.getGlobal().setLevel(Level.INFO);
	}

	@BeforeClass
	public static void setUp() throws Exception {

		dao = (DatabaseSQLInterface)DatabaseFactory.getInstance(DatabaseName);
		assertNotNull("JDBCDriver should not be null", dao);

		if (databases == null) {
			databases = DatabaseMapper.read(Databases.class, TEST_FILENAME);
			assertNotNull("Databases should not be null", databases);
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
	    ((DatabaseInterface)dao).release();
	}

	// -------------------------------------------------------------------------
	@Test
	public void testCreateColumn() throws Exception {

		SqlConverter.write(new DatabaseWriter(dao), "mysql", databases);

		Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", DatabaseName);
		assertNotNull("Database not created " + TEST_FILENAME, database);
		assertNotNull("Database name is null " + TEST_FILENAME, database.getName());

		// check table/column exists in file
		Table table = DatabaseUtil.findOneItem(database.getTables(), "name", TableName);
		assertNotNull("table " + TableName + " not found", table);
		assertNotNull("table name is null ", table.getName());

		Column column = DatabaseUtil.findOneItem(table.getColumns(), "name", ColumnName);
		assertNotNull("column " + ColumnName + " not found", column);
		assertNotNull("column name is null ", column.getName());

		// drop the column, if necessary.
		dao.write(driver.dropColumn(database, table, column));

		Databases md = driver.load(dao, DatabaseName, TableName);

		Column dropColumn = DatabaseMgr.findColumn(md, DatabaseName, TableName, ColumnName);
		assertNull("column " + ColumnName + " still exists", dropColumn);

		dao.write(driver.addColumn(database, table, column));

		md = driver.load(dao, DatabaseName, TableName);

		Column addColumn = DatabaseMgr.findColumn(md, DatabaseName, TableName, ColumnName);
		assertNotNull("column " + ColumnName + " does not exists", addColumn);
	}

	// -------------------------------------------------------------------------
	@Test
	public void testDropColumn() throws Exception {

		SqlConverter.write(new DatabaseWriter(dao), "mysql", databases);

		Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", DatabaseName);
		assertNotNull("Database not created " + TEST_FILENAME, database);
		assertNotNull("Database name is null " + TEST_FILENAME, database.getName());

		Table table = DatabaseUtil.findOneItem(database.getTables(), "name", TableName);
		assertNotNull("table " + TableName + " not found", table);
		assertNotNull("table name is null ", table.getName());

		Column column = DatabaseUtil.findOneItem(table.getColumns(), "name", ColumnName);
		assertNotNull("column " + ColumnName + " not found", column);
		assertNotNull("column name is null ", column.getName());

		Databases md = driver.load(dao, DatabaseName, TableName);
		assertNotNull("column " + ColumnName + " does not exists", DatabaseMgr.findColumn(md, DatabaseName, TableName, ColumnName));

		dao.write(driver.dropColumn(database, table, column));

		md = driver.load(dao, DatabaseName, TableName);
		assertNull("column " + ColumnName + " still exists", DatabaseMgr.findColumn(md, database.getName(), table.getName(), ColumnName));
	}

	// -------------------------------------------------------------------------
	@Test
	public void testRenameColumn() throws Exception {

		SqlConverter.write(new DatabaseWriter(dao), "mysql", databases);

		String newcolumnname = "newteacher";

		Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", DatabaseName);
		assertNotNull("Database not created " + TEST_FILENAME, database);
		assertNotNull("Database name is null " + TEST_FILENAME, database.getName());

		Table table = DatabaseUtil.findOneItem(database.getTables(), "name", TableName);
		assertNotNull("table " + TableName + " not found", table);
		assertNotNull("table " + TableName + " name is null", table.getName());

		Column column = DatabaseUtil.findOneItem(table.getColumns(), "name", ColumnName);
		assertNotNull("column " + ColumnName + " not found", column);

		Databases md = driver.load(dao, DatabaseName, TableName);
		assertNotNull("column " + ColumnName + " does not exists", DatabaseMgr.findColumn(md, DatabaseName, TableName, ColumnName));

		dao.write(driver.renameColumn(database, table, column, newcolumnname));

		md = driver.load(dao, DatabaseName, TableName);
		assertNotNull("new column " + newcolumnname + " does not exists", DatabaseMgr.findColumn(md, DatabaseName, TableName, newcolumnname));
		assertNull("old column " + ColumnName + " still exists", DatabaseMgr.findColumn(md, DatabaseName, TableName, ColumnName));
	}

	// -------------------------------------------------------------------------
	@Test
	public void testLoadDatabaseMetaInfo() throws Exception {

		SqlConverter.write(new DatabaseWriter(dao), "mysql", databases);

		Databases items = driver.load(dao, DatabaseName, TableName);

		Column column = DatabaseMgr.findColumn(items, DatabaseName, TableName, ColumnName);
		assertNotNull("column " + ColumnName + " not found", column);

		// Standard parameters
		assertEquals("name", ColumnName, column.getName());
		assertEquals("size", 32L, column.getSize());
		assertEquals("decimal_digits", 0, column.getDecimalSize());
		assertEquals("is-required", false, column.isRequired());
		assertEquals("remarks", "this is the teachers column", column.getDescription());
	}
}
