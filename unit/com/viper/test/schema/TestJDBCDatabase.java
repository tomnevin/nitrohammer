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
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestJDBCDatabase extends AbstractTestCase {

    private final static String DATABASE_NAME = "test";
    private final static String DatabaseName = "databasename";
    private final static SQLDriver driver = new SQLDriver("mysql");
    
    private DatabaseSQLInterface dao = null;

    @Before
    public void setUp() throws Exception {

        dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("DatabaseFactory should not be null", dao);
       
    }

    @After
    public void tearDown() throws Exception {
        ((DatabaseInterface)dao).release();
    }

    // -------------------------------------------------------------------------
    @Test
    public void testCreateDatabase() throws Exception {

        Database database = new Database();
        database.setName(DatabaseName);

        dao.write(driver.dropDatabase(database));

        Databases databases = driver.load(dao, DatabaseName, null);
        assertNull("database " + DatabaseName + " was NOT dropped", DatabaseUtil.findOneItem(databases.getDatabases(), "name", DatabaseName));

        dao.write(driver.createDatabase(database));

        databases = driver.load(dao, DatabaseName, null);
        assertNotNull("database " + DatabaseName + " was NOT created", DatabaseUtil.findOneItem(databases.getDatabases(), "name", DatabaseName));
    }

    // -------------------------------------------------------------------------
    @Test
    public void testDropDatabase() throws Exception {
        Database database = new Database();
        database.setName(DatabaseName);

        dao.write(driver.dropDatabase(database));

        Databases databases = driver.load(dao, DatabaseName, null);
        assertNull("database " + DatabaseName + " was NOT dropped", DatabaseUtil.findOneItem(databases.getDatabases(), "name", DatabaseName));

        dao.write(driver.createDatabase(database));

        databases = driver.load(dao, DatabaseName, null);
        assertNotNull("database DatabaseName was NOT created", DatabaseUtil.findOneItem(databases.getDatabases(), "name", DatabaseName));

        dao.write(driver.dropDatabase(database));

        databases = driver.load(dao, DatabaseName, null);
        assertNull("database " + DatabaseName + " was NOT dropped", DatabaseUtil.findOneItem(databases.getDatabases(), "name", DatabaseName));
    }

    // -------------------------------------------------------------------------
    @Test
    public void testLoadDatabaseMetaInfo() throws Exception {

        Database database = new Database();
        database.setName(DatabaseName);

        dao.write(driver.createDatabase(database));

        List<Database> databases = driver.loadDatabases(dao, DatabaseName);
        assertNotNull("Database not found:", databases);
        assertEquals("Database not found:", 1, databases.size());

        Database item = databases.get(0);
        assertNotNull("Database not found:", item);

        assertEquals("testLoadDatabaseMetaInfo: charsetName", "latin1", item.getCharsetName());
        assertEquals("testLoadDatabaseMetaInfo: collationName", "latin1_swedish_ci", item.getCollationName());
        assertEquals("testLoadDatabaseMetaInfo: catalog", "def", item.getCatalog());
    }
}
