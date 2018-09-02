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

package com.viper.test;

import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestDatabaseMetaData extends AbstractTestCase {

	private final static String DATABASE_NAME = "test";
	
	private final static SQLDriver driver = new SQLDriver();

	@Test
	public void testDatabaseDriver() throws Exception {

		DatabaseSQLInterface dao = (DatabaseSQLInterface)DatabaseFactory.getInstance(DATABASE_NAME);
		assertNotNull("JDBCDriver should not be null", dao);

		assertTrue("Meta.getAttributes", dao.readMetaRows("attributes").size() >= 0);
		assertTrue("Meta.getCrossReference", dao.readMetaRows("crossreference").size() >= 0);
		// assertTrue("Meta.getPrimaryKeys", dao.readMetaRows("primarykeys").size() >= 0);
		assertTrue("Meta.getTriggerInfo", dao.readMetaRows("triggerinfo").size() >= 0);
		assertTrue("Meta.getTypeInfo", dao.readMetaRows("typeinfo").size() >= 0);
        assertTrue("Meta.getTableTypes", dao.readMetaRows("TableTypes").size() >= 0);
		assertTrue("Meta.getBestRowIdentifier", dao.readMetaRows("BestRowIdentifier").size() >= 0);
		assertTrue("Meta.getSupportsConvertByType", dao.readMetaRows("SupportsConvert").size() >= 0);

		((DatabaseInterface)dao).release();
	}

	@Test
	public void testListDatabases() throws Exception {

		DatabaseSQLInterface dao = (DatabaseSQLInterface)DatabaseFactory.getInstance(DATABASE_NAME);
		assertNotNull("JDBCDriver should not be null", dao);

		Databases databases =  driver.load(dao, null, null);

		assertNotNull("database (mysql) not found", DatabaseUtil.findOneItem(databases.getDatabases(), "name", "mysql"));
		assertNotNull("database (information_schema) not found", DatabaseUtil.findOneItem(databases.getDatabases(), "name", "information_schema"));
		assertNotNull("database (test) not found", DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test"));

		for (Database database : databases.getDatabases()) {
			System.out.println("Database: " + database.getName());
		}

		((DatabaseInterface)dao).release();
	}

	@Test
	public void testListTables() throws Exception {

		DatabaseSQLInterface dao = (DatabaseSQLInterface)DatabaseFactory.getInstance(DATABASE_NAME);
		assertNotNull("JDBCDriver should not be null", dao);

		Databases databases = driver.load(dao, null, null);

		Database infoDatabase = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "information_schema");
		assertNotNull("database (information_schema) not found", infoDatabase);

		assertNotNull("table (information_schema.tables) not found", DatabaseUtil.findOneItem(infoDatabase.getTables(), "name", "tables"));

		((DatabaseInterface)dao).release();
	}
}
