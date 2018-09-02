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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.utils.DatabaseRegistry;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;
import com.viper.demo.persistence0.test.model.Organization;

public class TestDaoPersistence extends AbstractTestCase {

    @Rule
    public TestRule benchmarkRule = new BenchmarkRule();

    @BeforeClass
    public static void initializeClass() throws Exception {

        Logger.getGlobal().setLevel(Level.INFO);

        DatabaseRegistry.getInstance();
    }

    private DatabaseInterface getDatabase() throws Exception {
        
        return DatabaseFactory.getInstance("test-jta");
    }

    @Test
    public void testCreate() throws Exception {

        Organization organization = getSampleOrganization();

        DatabaseInterface database = getDatabase();
        database.deleteAll(Organization.class);
        database.insert(organization);

        assertNotNull("testCreate - the organization id was not set", organization.getId());
    }

    @Test
    public void testCreateCollection() throws Exception {

        List<Organization> organizations = getSampleOrganizations(100);
        DatabaseInterface database = getDatabase();
        database.deleteAll(Organization.class);
        database.insertAll(organizations);

        for (Organization organization : organizations) {
            assertNotNull("testCreateCollection - the organization id was not set", organization.getId());
        }
    }

    @Test
    public void testPrimaryKey() throws Exception {

        List<Organization> organizations = getSampleOrganizations(100);
        DatabaseInterface database = getDatabase();
        database.deleteAll(Organization.class);
        database.insertAll(organizations);

        int id = organizations.get(50).getId();

        Organization organization = database.query(Organization.class, "id", id);
        assertNotNull("testPrimaryKey - could not find Organization1", organization);
        assertEquals("testPrimaryKey - could not find Organization2", "Joe Johnson - 50", organization.getName());
    }

    @Test
    public void testPrimaryKeys() throws Exception {

        List<Organization> organizations = getSampleOrganizations(100);
        DatabaseInterface database = getDatabase();
        database.deleteAll(Organization.class);
        database.insertAll(organizations);

        int id = organizations.get(50).getId();
        String name = organizations.get(50).getName();

        Collection<Organization> organizations1 = database.queryList(Organization.class, "id", id, "name", name);
        assertNotNull("testPrimaryKeys - could not find Organization1", organizations1);
        assertEquals("testPrimaryKeys - could not find Organization2", 1, organizations1.size());
    }

    @Test
    public void testQuery() throws Exception {

        DatabaseInterface database = getDatabase();
        database.deleteAll(Organization.class);
        database.insertAll(getSampleOrganizations(100));

        Organization organization = database.query(Organization.class, "name", "Joe Johnson - 50");
        assertNotNull("testQuery - could not find Organization1", organization);
        assertEquals("testQuery - could not find Organization2", "Joe Johnson - 50", organization.getName());
    }

    @Test
    public void testList() throws Exception {

        DatabaseInterface database = getDatabase();
        database.deleteAll(Organization.class);
        database.insertAll(getSampleOrganizations(100));

        Collection<Organization> organizations = database.queryList(Organization.class, "name", "Joe Johnson - 50");
        assertNotNull("testList null - could not find Organization", organizations);
        assertEquals("testList size - could not find Organization", 1, organizations.size());
    }

    @Test
    public void testListAll() throws Exception {

        DatabaseInterface database = getDatabase();
        database.deleteAll(Organization.class);
        database.insertAll(getSampleOrganizations(100));

        Collection<Organization> organizations = database.queryAll(Organization.class);

        assertNotNull("testList null - could not find Organization", organizations);
        assertEquals("testList size - could not find Organization", 100, organizations.size());
    }

    private Organization getSampleOrganization() {
        Organization organization = new Organization();
        organization.setAddress("99 Main Street");
        organization.setName("Joe Johnson");
        organization.setPhone("999-888-7777");
        organization.setZipcode("94536");
        return organization;
    }

    private List<Organization> getSampleOrganizations(int size) {
        List<Organization> organizations = new ArrayList<Organization>();
        for (int i = 0; i < size; i++) {
            Organization organization = new Organization();
            organization.setAddress(i + " Main Street");
            organization.setName("Joe Johnson - " + i);
            organization.setPhone("999-888-7777");
            organization.setZipcode("" + i);

            organizations.add(organization);
        }
        return organizations;
    }
}
