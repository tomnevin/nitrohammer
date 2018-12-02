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

package com.viper.test.dao;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.filters.Predicate;
import com.viper.database.utils.DatabaseRegistry;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.demo.unit.model.Organization;
import com.viper.demo.unit.model.User;

public class TestDaoMemory extends AbstractTestCase {

	@BeforeClass
	public static void initializeClass() throws Exception {

		Logger.getGlobal().setLevel(Level.INFO);

		DatabaseRegistry.getInstance();
	}

	private DatabaseInterface getDatabase() throws Exception {
		// connection.setPackageName("com.viper.primefaces.model");
		DatabaseInterface database = DatabaseFactory.getInstance("test-memory");

		if (!DatabaseUtil.isTableExist(database, "test", "organization")) {
			database.create(Organization.class);
		}
		return database;
	}

	@Test
	public void testCreate() throws Exception {

		Organization expected = RandomBean.getRandomBean(Organization.class, 101);
		expected.setId(0);

		DatabaseInterface database = getDatabase();
		database.insert(expected);

		Assert.assertTrue("testCreate - the organization id not set: " + expected.getId(), expected.getId() > 0);
	}

	@Test
	public void testCreateCollection() throws Exception {

		List<Organization> organizations = RandomBean.getRandomBeans(Organization.class, 100, 102);

		DatabaseInterface database = getDatabase();
		database.insertAll(organizations);

		for (Organization organization : organizations) {
			Assert.assertNotNull("testCreateCollection - the organization id not set: " + organization.getId(), organization.getId());
		}
	}

	@Test
	public void testPrimaryKey() throws Exception {

		List<Organization> organizations = RandomBean.getRandomBeans(Organization.class, 100, 103);

		DatabaseInterface database = getDatabase();
		database.insertAll(organizations);

		Organization expected = organizations.get(50);
		Assert.assertNotNull("testPrimaryKey - could not find expected Organization", expected);

		Organization actual = database.query(Organization.class, "id", expected.getId());
		Assert.assertNotNull("testPrimaryKey - could not find Organization1: " + expected.getId(), actual);
		Assert.assertEquals("testPrimaryKey - id does not match", expected.getId(), actual.getId());
		Assert.assertEquals("testPrimaryKey - name does not match", expected.getName(), actual.getName());
	}

	@Test
	public void testQuery() throws Exception {

		List<Organization> organizations = RandomBean.getRandomBeans(Organization.class, 100, 104);

		DatabaseInterface database = getDatabase();
		database.insertAll(organizations);

		Organization expected = organizations.get(50);

		Organization actual = database.query(Organization.class, "name", expected.getName());
		Assert.assertNotNull("testQuery - could not find Organization", actual);
		Assert.assertEquals("testQuery - could not find Organization2", expected.getName(), actual.getName());
	}

	@Test
	public void testList() throws Exception {

		List<Organization> organizations = RandomBean.getRandomBeans(Organization.class, 100, 106);

		DatabaseInterface database = getDatabase();
		database.insertAll(organizations);

		Organization expected = organizations.get(75);

		Collection<Organization> actual = database.queryList(Organization.class, "id", expected.getId());
		Assert.assertNotNull("testList null - could not find Organization", actual);
		Assert.assertEquals("testList size - could not find Organization", 1, actual.size());
	}

	@Test
	public void testListAll() throws Exception {

		DatabaseInterface database = getDatabase();
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);

		Collection<Organization> organizations = database.queryAll(Organization.class);
		Assert.assertNotNull("testList null - could not find Organization", organizations);
		Assert.assertTrue("testList size - could not find Organization: " + organizations.size(), organizations.size() >= 100);
	}

	@Test
	public void testQueryUser() throws Exception {

		User expected = RandomBean.getRandomBean(User.class, 1);

		DatabaseInterface database = getDatabase();
		database.insert(expected);

		User actual = database.query(User.class, "username", expected.getUsername());

		Assert.assertNotNull("testQuery - could not find User", actual);
		Assert.assertEquals("testQuery - could not find User.name", expected.getName(), actual.getName());
		Assert.assertEquals("testQuery - could not find User.friends.size", expected.getFriends().size(), actual.getFriends().size());
		Assert.assertEquals("testQuery - could not find User.friends", expected.getFriends(), actual.getFriends());
	}

	@Test
	public void testQueryExpression() throws Exception {

		DatabaseInterface database = getDatabase();
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);

		List<Organization> organizations = database.queryList(Organization.class, new Predicate<Organization>() {
		    @Override
            public boolean apply(Organization organization) {
                return organization.getId() == expected.get(0).getId();
            }
            @Override
            public String toSQL() {
                return null;
            }
		}, null, null);

		Assert.assertNotNull("testQueryExpression null - could not find Organization", organizations);
		Assert.assertTrue("testQueryExpression size - could not find Organization: " + organizations.size(), organizations.size() == 1);
	}

	@Test
	public void testInsertAll() throws Exception {

		DatabaseInterface database = getDatabase();
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);

		Collection<Organization> organizations = database.queryAll(Organization.class);
		Assert.assertNotNull("testUpdateAll null - could not find Organization", organizations);
		Assert.assertEquals("testUpdateAll size - could not find Organization: ", expected.size(), organizations.size());
	}

	@Test
	public void testSize() throws Exception {

		DatabaseInterface database = getDatabase();
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);

		long size = database.size(Organization.class);
		Assert.assertEquals("testSize - could not find Organization: " + expected.size(), expected.size(), size);
	}

	@Test
	public void testUniqueValues() throws Exception {

		DatabaseInterface database = getDatabase();
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);

		List<Object> items = database.uniqueValues(Organization.class, "name");
		Assert.assertNotNull("testUniqueValues - could not find Organization: " + items.size(), items);
		Assert.assertTrue("testUniqueValues - could not find Organization: " + items.size(), items.size() > 0);
	}

	@Test
	public void testListDatabases() throws Exception {

		DatabaseInterface database = getDatabase();
		List<String> items = database.listDatabases();

		for (String item : items) {
			System.out.println("testListDatabases: database Name=" + item);
		}

		Assert.assertTrue("testListDatabases - number of database must be > 0: " + items.size(), items.size() > 0);
		Assert.assertTrue("testListDatabases - test: ", items.contains("test"));
	}

	@Test
	public void testListTables() throws Exception {

		DatabaseInterface database = getDatabase();
		List<String> items = database.listTables("test");

		for (String item : items) {
			System.out.println("testListTables: table Name=" + item);
		}

		Assert.assertTrue("testListTables - number of tables must be > 0: " + items.size(), items.size() > 0);
	}

	@Test
	public void testListColumns() throws Exception {

		DatabaseInterface database = getDatabase();
		List<String> items = database.listColumns(Organization.class);

		for (String item : items) {
			System.out.println("testListColumns: column Name=" + item);
		}

		Assert.assertTrue("testListColumns - number of columns must be > 0: " + items.size(), items.size() > 0);
	}
}
