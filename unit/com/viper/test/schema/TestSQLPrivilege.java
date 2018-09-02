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
import com.viper.database.model.Databases;
import com.viper.database.model.Privilege;
import com.viper.database.model.User;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLPrivilege extends AbstractTestCase {

    private static final SQLDriver driver = new SQLDriver();
	private final static String META_FILENAME = "res:/com/viper/test/schema/MetaDatabaseManagerExporter001.xml";

	@Rule
	public MethodRule benchmarkRule = new BenchmarkRule();

	@Test
	public void testMetaPrivilege01() throws Exception {
		Databases databases = DatabaseMapper.read(Databases.class, META_FILENAME);

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", "demo");
		assertNotNull("demo user not found", user);

		List<Privilege> privileges = databases.getPrivileges();
		assertNotNull("demo user privileges not found", privileges);

		assertEquals("number of demo user privileges mismatched", 1, privileges.size());
	}

	@Test
	public void testCreatePrivilege() throws Exception {
		Databases databases = DatabaseMapper.read(Databases.class, META_FILENAME);

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", "demo");
		assertNotNull("demo user not found", user);

		List<Privilege> privileges = databases.getPrivileges();
		assertNotNull("demo user privileges not found", privileges);

		assertEquals("number of demo user privileges mismatched", 1, privileges.size());

		Privilege privilege = privileges.get(0);

		String sql = driver.createPrivilege(user, privilege);

		assertNotNull("createPrivilege", sql);
		assertEquals("GRANT create ON * TO demo", sql.trim());
	}

	@Test
	public void testDropPrivilege() throws Exception {
		Databases databases = DatabaseMapper.read(Databases.class, META_FILENAME);

		User user = DatabaseUtil.findOneItem(databases.getUsers(), "name", "demo");
		assertNotNull("demo user not found", user);

		List<Privilege> privileges = databases.getPrivileges();
		assertNotNull("demo user privileges not found", privileges);
		assertEquals("number of demo user privileges mismatched", 1, privileges.size());

		Privilege privilege = privileges.get(0);

		String sql = driver.dropPrivilege(user, privilege);

		assertNotNull("dropPrivilege", sql);
		assertEquals("REVOKE create ON * FROM demo", sql.trim());
	}
}