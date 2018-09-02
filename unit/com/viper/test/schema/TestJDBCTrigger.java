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

import java.util.List;

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
import com.viper.database.managers.DatabaseMgr;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.Trigger;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestJDBCTrigger extends AbstractTestCase {

	private final static String DATABASE_NAME = "test";
	private final static String DRIVER_NAME = "mysql";
	private final static String TEST_FILENAME_001 = "res:/com/viper/test/schema/TestJDBCTrigger001.xml";
	private final static String TRIGGER_A = "trigger01";

	private static final SQLDriver driver = new SQLDriver();
	private DatabaseSQLInterface dao = null;

	@Before
	public void setUp() throws Exception {

		dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
		assertNotNull("JDBCDriver should not be null", dao);

		Databases databases = DatabaseMapper.read(Databases.class, TEST_FILENAME_001);

		SqlConverter.write(new DatabaseWriter(dao), DRIVER_NAME, databases);
	}

	@After
	public void tearDown() throws Exception {
		((DatabaseInterface) dao).release();
	}

	@Test
	public void testCreateTrigger() throws Exception {
		internalTestCreateTrigger("testCreateTrigger", DATABASE_NAME, TRIGGER_A);
	}

	@Test
	public void testDropTrigger() throws Exception {

		internalTestCreateTrigger("testDropTrigger", DATABASE_NAME, TRIGGER_A);

		Databases databases = DatabaseMapper.read(Databases.class, TEST_FILENAME_001);

		for (Database database : databases.getDatabases()) {

			Trigger trigger = DatabaseUtil.findOneItem(database.getTriggers(), "name", TRIGGER_A);
			assertNotNull("trigger " + TRIGGER_A + " not found", trigger);

			dao.write(driver.dropTrigger(database,  trigger));

			List<Trigger> actual = driver.loadTriggers(dao, database.getName(), TRIGGER_A);

			Trigger actualTrigger = DatabaseUtil.findOneItem(actual, "name", TRIGGER_A);
			assertNotNull("trigger " + TRIGGER_A + " still exists", actualTrigger);
		}
	}

	@Test
	public void testRenameTrigger() throws Exception {

		String newtriggername = "new-trigger-name";

		Databases databases = DatabaseMapper.read(Databases.class, TEST_FILENAME_001);
		for (Database database : databases.getDatabases()) {

			for (Trigger trigger : database.getTriggers()) {
				dao.write(driver.dropTrigger(database, trigger));

				List<Trigger> actual = driver.loadTriggers(dao, database.getName(), trigger.getName());
				assertNull("trigger " + trigger.getName() + " still exists",
						DatabaseUtil.findOneItem(actual, "name", trigger.getName()));

				dao.write(driver.createTrigger(database,  trigger));

				actual = driver.loadTriggers(dao, database.getName(), trigger.getName());
				assertNotNull("trigger " + trigger.getName() + " does still exists",
						DatabaseUtil.findOneItem(actual, "name", trigger.getName()));

				dao.write(driver.renameTrigger(database,  trigger, newtriggername));

				actual = driver.loadTriggers(dao, database.getName(), trigger.getName());
				assertNull("trigger " + trigger.getName() + " still exists",
						DatabaseUtil.findOneItem(actual, "name", trigger.getName()));

				actual = driver.loadTriggers(dao, database.getName(), newtriggername);
				assertNotNull("trigger " + newtriggername + " does not exists",
						DatabaseUtil.findOneItem(actual, "name", trigger.getName()));
			}
		}
	}

	private void internalTestCreateTrigger(String testName, String databaseName, String triggerName) throws Exception {

		Databases databases = DatabaseMapper.read(Databases.class, TEST_FILENAME_001);
		for (Database database : databases.getDatabases()) {

			for (Trigger trigger : database.getTriggers()) {
				dao.write(driver.dropTrigger(database,  trigger));

				List<Trigger> actual = driver.loadTriggers(dao, database.getName(), trigger.getName());
				assertNull("trigger " + trigger.getName() + " still exists",
						DatabaseUtil.findOneItem(actual, "name", trigger.getName()));

				dao.write(driver.createTrigger(database,  trigger));

				actual = driver.loadTriggers(dao, database.getName(), trigger.getName());
				assertNotNull("trigger " + trigger.getName() + " not exists",
						DatabaseUtil.findOneItem(actual, "name", trigger.getName()));
			}
		}
	}
}
