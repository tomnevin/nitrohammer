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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.Databases;
import com.viper.database.model.User;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLUser extends AbstractTestCase {

    private static final SQLDriver driver = new SQLDriver();

	@Rule
	public MethodRule benchmarkRule = new BenchmarkRule();

	protected Databases startup() throws Exception {

		String META_FILENAME = "res:/com/viper/test/schema/MetaDatabaseManagerExporter001.xml";

		Databases databases = DatabaseMapper.read(Databases.class, META_FILENAME);
		assertNotNull("Database empty for " + META_FILENAME, databases);

		return databases;
	}

	protected void finish(DatabaseConnection dbc) throws Exception {
	}

	@Test
	public void testMetaUser() throws Exception {
		Databases databases = startup();

		List<User> users = databases.getUsers();
		assertNotNull("users not found", users);

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", "demo");

		assertNotNull("demo user not found", user);
		assertEquals("number of users mismatched", 1, users.size());
	}

	@Test
	public void testCreateUser() throws Exception {
		Databases databases = startup();

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", "demo");
		assertNotNull("demo user not found", user);

		String sql = driver.createUser(user);

		assertNotNull("create user", sql);
		assertEquals("create user 'demo'@'localhost'", sql.trim());
	}

	@Test
	public void testDropUser() throws Exception {
		Databases databases = startup();

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", "demo");
		assertNotNull("demo user not found", user);

		String sql = driver.dropUser(user);

		assertNotNull("drop user", sql);
		assertEquals("drop user 'demo'@'localhost'", sql.trim());
	}
}