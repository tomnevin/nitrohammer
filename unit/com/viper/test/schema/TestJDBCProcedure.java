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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.DatabaseWriter;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.Procedure;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestJDBCProcedure extends AbstractTestCase {

	private final static String DATABASE_NAME = "test";
	private final static String DRIVER_NAME = "mysql";

	private final static String FILENAME = "res:/com/viper/test/schema/TestJDBCProcedure001.xml";
	private final static String PROCEDURE_NAME = "sumit";

	private final static SqlConverter sqlManager = new SqlConverter();
	private static final SQLDriver driver = new SQLDriver();
	private static Databases databases = new Databases();
	private DatabaseSQLInterface dao = null;

	@Before
	public void setUp() throws Exception {

		dao = (DatabaseSQLInterface)DatabaseFactory.getInstance(DATABASE_NAME);
		assertNotNull("JDBCDriver should not be null", dao);

		if (databases.getDatabases().size() == 0) {
			Database database = DatabaseMapper.readDatabase(FILENAME);
			assertNotNull("Databases should not be null", database);
			
			databases.getDatabases().add(database);
		}

		sqlManager.write(new DatabaseWriter(dao), DRIVER_NAME, databases);
	}

	@After
	public void tearDown() throws Exception {
	    ((DatabaseInterface)dao).release();
	}

	@Test
	public void testCreateProcedure() throws Exception {

		for (Database database : databases.getDatabases()) {

			Procedure procedure = DatabaseUtil.findOneItem(database.getProcedures(), "name", PROCEDURE_NAME);
			assertNotNull("Procedure " + PROCEDURE_NAME + " not found.", database);

			dao.write(driver.dropProcedure(database, procedure));

			Databases md = driver.load(dao, database.getName(), null);

			database = DatabaseUtil.findOneItem(md.getDatabases(), "name", DATABASE_NAME);
			assertNotNull("Database test not found", database);

			Procedure dropProcedure = DatabaseUtil.findOneItem(database.getProcedures(), "name", procedure.getName());
			assertNull("procedure " + procedure.getName() + " still exists", dropProcedure);

			dao.write(driver.createProcedure(database, procedure));

			md = driver.load(dao, database.getName(), null);

			database = DatabaseUtil.findOneItem(md.getDatabases(), "name", DATABASE_NAME);
			assertNotNull("Database test not found", database);

			procedure = DatabaseUtil.findOneItem(database.getProcedures(), "name", PROCEDURE_NAME);
			assertNotNull("Procedures is null: " + PROCEDURE_NAME, procedure);
			assertEquals("Procedure " + PROCEDURE_NAME + " not created", PROCEDURE_NAME, procedure.getName());
		}
	}

	@Test
	public void testDropProcedure() throws Exception {

		for (Database database : databases.getDatabases()) {

			Procedure procedure = DatabaseUtil.findOneItem(database.getProcedures(), "name", PROCEDURE_NAME);
			assertNotNull("Procedure not found: " + PROCEDURE_NAME, procedure);

			dao.write(driver.dropProcedure(database, procedure));

			Databases md = driver.load(dao, database.getName(), null);

			database = DatabaseUtil.findOneItem(md.getDatabases(), "name", DATABASE_NAME);
			assertNotNull("Database not found: " + DATABASE_NAME, database);

			procedure = DatabaseUtil.findOneItem(database.getProcedures(), "name", PROCEDURE_NAME);
			assertNull("Procedure " + database.getName() + "." + PROCEDURE_NAME + " not dropped", procedure);
		}
	}

	@Test
	public void testLoadDatabaseMetaInfo() throws Exception {

		Databases databases = driver.load(dao, DATABASE_NAME, null);

		Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", DATABASE_NAME);
		assertNotNull("Database test not found", database);

		Procedure procedure = DatabaseUtil.findOneItem(database.getProcedures(), "name", PROCEDURE_NAME);
		assertNotNull("Procedures " + PROCEDURE_NAME + " not found", procedure);
	}

	@Test
	public void testLoadCustomInfo() throws Exception {

		Databases databases = driver.load(dao, DATABASE_NAME, null);

		Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", DATABASE_NAME);
		assertNotNull("Database test not found", database);

		Procedure procedure = DatabaseUtil.findOneItem(database.getProcedures(), "name", PROCEDURE_NAME);
		assertNotNull("Procedure " + PROCEDURE_NAME + " not found", procedure);

		Database database1 = DatabaseUtil.findOneItem(databases.getDatabases(), "name", database.getName());
		assertNotNull("Custom Procedure Info is null", database1);
		assertTrue("Custom Procedure Info is empty", database1.getProcedures().size() > 0);

		// TODO add asserts
	}
}
