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
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.DatabaseWriter;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Databases;
import com.viper.database.model.User;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestJDBCUser extends AbstractTestCase {

	private final static Logger log = Logger.getLogger(TestJDBCUser.class.getName());

	private final static String DATABASE_NAME = "test";
	private final static String DRIVER_NAME = "mysql";
	private final static String TEST_FILENAME_001 = "res:/com/viper/test/schema/TestJDBCUser001.xml";
	private final static String databaseName = "test";
	private final static String username = "demo";

	private static final SQLDriver driver = new SQLDriver();
	private DatabaseSQLInterface dao = null;
	private Databases databases = null;

	@Before
	public void initialize() throws Exception {

		dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
		assertNotNull("JDBCDriver should not be null", dao);

		databases = DatabaseMapper.readDatabases(TEST_FILENAME_001);

		SqlConverter.write(new DatabaseWriter(dao), DRIVER_NAME, databases);
	}

	public void finish() throws Exception {
		((DatabaseInterface) dao).release();
	}

	@Test
	public void testCreateUser() throws Exception {

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", username);
		assertNotNull("user " + username + " not found", user);

		dao.write(driver.dropUser(user));

		List<User> actual = driver.loadUsers(dao, username);
		assertEquals("user " + username + " still exists", 0, actual.size());

		dao.write(driver.createUser(user));

		actual = driver.loadUsers(dao, username);
		assertEquals("user " + username + " does not exists", 1, actual.size());

		finish();
	}

	@Test
	public void testDropUser() throws Exception {

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", username);
		assertNotNull("user " + username + " not found", user);

		dao.write(driver.dropUser(user));

		List<User> actual = driver.loadUsers(dao, username);
		assertEquals("user " + username + " still exists", 0, actual.size());

		finish();
	}

	@Test
	public void testLoadUsers() throws Exception {

		List<User> users = databases.getUsers();
		assertNotNull("LoadUsers is null", users);
		assertTrue("LoadUsers is not empty", users.size() > 0);

		log.info("Number of users: " + users.size());

		finish();
	}

	@Test
	public void testLoadStandardInfo() throws Exception {

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", username);
		assertNotNull("user " + username + " not found", user);

		assertNotNull("Standad User Info is null", databases.getUsers());
		assertTrue("Standard User Info is empty", databases.getUsers().size() > 0);

		finish();
	}

	@Test
	public void testLoadCustomInfo() throws Exception {

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", username);
		assertNotNull("User " + username + " not found", user);

		List<User> actual = driver.loadUsers(dao, username);
		assertEquals("user " + username + " does not exists", 1, actual.size());

		User item = actual.get(0);

		finish();
	}
}
