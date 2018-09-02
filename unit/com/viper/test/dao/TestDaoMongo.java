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
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.Predicate;
import com.viper.database.utils.DatabaseRegistry;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.demo.unit.model.Organization;
import com.viper.demo.unit.model.User;

public class TestDaoMongo extends AbstractTestCase {

	private final static Logger log = Logger.getLogger(TestDaoMongo.class.getName());
	private final static String DATABASE_NAME = "test-mongo";

	@BeforeClass
	public static void initializeClass() throws Exception {

		DatabaseRegistry.getInstance();
	}

	@Test
	public void testCreate() throws Exception {

		Organization expected = RandomBean.getRandomBean(Organization.class, 101);
		expected.setId(0);

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		database.insert(expected);

		Assert.assertTrue(getCallerMethodName() + " - the organization id not set: " + expected.getId(), expected.getId() > 0);
	}

	@Test
	public void testCreateCollection() throws Exception {

		List<Organization> organizations = RandomBean.getRandomBeans(Organization.class, 100, 102);

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		database.insertAll(organizations);

		for (Organization organization : organizations) {
			Assert.assertNotNull(getCallerMethodName() + " - the organization id not set: " + organization.getId(), organization.getId());
		}
	}

	@Test
	public void testPrimaryKey() throws Exception {

		List<Organization> organizations = RandomBean.getRandomBeans(Organization.class, 100, 103);

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		database.insertAll(organizations);

		for (Organization organization : organizations) {
			Assert.assertNotNull(getCallerMethodName() + " - no orgaization id:", organization.getId());
			Assert.assertTrue(getCallerMethodName() + " - orgaization id is zero:", organization.getId() != 0);
		}

		Organization expected = organizations.get(50);

		Organization actual = database.query(Organization.class, "id", expected.getId());

		Assert.assertNotNull(getCallerMethodName() + " - could not find Organization (is null):" + expected.getId(), actual);
		Assert.assertEquals(getCallerMethodName() + " - could not find Organization( name dont match): " + expected.getId(), expected.getName(), actual.getName());
	}

	@Test
	public void testQuery() throws Exception {

		List<Organization> organizations = RandomBean.getRandomBeans(Organization.class, 100, 104);

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		database.insertAll(organizations);

		Organization expected = organizations.get(50);

		Organization actual = database.query(Organization.class, "name", expected.getName());
		Assert.assertNotNull(getCallerMethodName() + " - could not find Organization", actual);
		Assert.assertEquals(getCallerMethodName() + " - could not find Organization2", expected.getName(), actual.getName());
	}

	@Test
	public void testList() throws Exception {

		List<Organization> organizations = RandomBean.getRandomBeans(Organization.class, 100, 106);

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		database.insertAll(organizations);

		Organization expected = organizations.get(75);

		Collection<Organization> actual = database.queryList(Organization.class, "id", expected.getId());
		Assert.assertNotNull(getCallerMethodName() + " null - could not find Organization", actual);
		Assert.assertEquals(getCallerMethodName() + " size - could not find Organization", 1, actual.size());
	}

	@Test
	public void testListAll() throws Exception {

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);

		Collection<Organization> organizations = database.queryAll(Organization.class);
		Assert.assertNotNull(getCallerMethodName() + " null - could not find Organization", organizations);
		Assert.assertTrue(getCallerMethodName() + " size - could not find Organization: " + organizations.size(), organizations.size() >= 100);
	}

	@Test
	public void testQueryUser() throws Exception {

		User expected = RandomBean.getRandomBean(User.class, 1);

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		database.insert(expected);

		User actual = database.query(User.class, "username", expected.getUsername());

		Assert.assertNotNull(getCallerMethodName() + " - could not find User", actual);
		Assert.assertEquals(getCallerMethodName() + " - could not find User.name", expected.getName(), actual.getName());
		Assert.assertEquals(getCallerMethodName() + " - could not find User.friends.size", expected.getFriends().size(), actual.getFriends().size());
		Assert.assertEquals(getCallerMethodName() + " - could not find User.friends", expected.getFriends(), actual.getFriends());
	}

	@Test
	public void testQueryExpression() throws Exception {

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);
		
		List<Organization> organizations = database.queryList(Organization.class, new Predicate<Organization>() {
			@Override
			public boolean apply(Organization e) {
				return e.getId() == expected.get(0).getId();
			}
		});
		Assert.assertNotNull("testQueryExpression null - could not find Organization", organizations);
		Assert.assertTrue("testQueryExpression size - could not find Organization: " + organizations.size(), organizations.size() == 1);
	}

	@Test
	public void testUpdateAll() throws Exception {

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);

		Collection<Organization> organizations = database.queryAll(Organization.class);
		Assert.assertNotNull("testUpdateAll null - could not find Organization", organizations);
		Assert.assertEquals("testUpdateAll size - could not find Organization: " + organizations.size(), expected.size(), organizations.size());
	}

	@Test
	public void testSize() throws Exception {

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);

		long size = database.size(Organization.class);
		Assert.assertEquals("testSize - could not find Organization: " + expected.size(), size, expected.size());
	}

	@Test
	public void testUniqueValues() throws Exception {

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		List<Organization> expected = RandomBean.getRandomBeans(Organization.class, 100, 107);
		database.deleteAll(Organization.class);
		database.insertAll(expected);

		List<Object> items = database.uniqueValues(Organization.class, "name");
		Assert.assertNotNull("testUniqueValues - could not find Organization: " + items.size(), items);
		Assert.assertTrue("testUniqueValues - could not find Organization: " + items.size(), items.size() > 0);
	}

	@Test
	public void testListDatabases() throws Exception {

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		List<String> items = database.listDatabases();

		for (String item : items) {
			log.info(getCallerMethodName() + " database Name=" + item);
		}

		Assert.assertTrue(getCallerMethodName() + " - number of database must be > 0: " + items.size(), items.size() > 0);
		Assert.assertTrue(getCallerMethodName() + " - test: ", items.contains("test"));
	}

	@Test
	public void testListTables() throws Exception {

		DatabaseInterface database = DatabaseFactory.getInstance(DATABASE_NAME);
		List<String> items = database.listTables(DATABASE_NAME);

		for (String item : items) {
			log.info(getCallerMethodName() + ": table Name=" + item);
		}

		Assert.assertTrue(getCallerMethodName() + " - number of tables must be > 0: " + items.size(), items.size() > 0);
	}
}