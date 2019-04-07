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

import java.io.FileWriter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.DatabaseWriter;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.DatabaseConnections;
import com.viper.database.model.Databases;
import com.viper.database.model.Table;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestSQLImporterExporter extends AbstractTestCase {

    private final static SqlConverter sqlManager = new SqlConverter();
    private static final SQLDriver driver = new SQLDriver();

    private static DatabaseConnection dbc = null;
    private static DatabaseSQLInterface dao = null;

    @BeforeClass
    public static void setUp() throws Exception {
        DatabaseConnections dbcs = DatabaseMapper.readConnections("res:/databases.xml");
        assertNotNull("Database connections(databases.xml) empty", dbcs);

        dbc = DatabaseUtil.findOneItem(dbcs.getConnections(), "name", "test");
        assertNotNull("Database connection (test) not found", dbc);

        dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(dbc);
        assertNotNull("JDBCDriver should not be null", dao);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ((DatabaseInterface) dao).release();
    }

    @Test
    public void testLoadMetaData() throws Exception {

        Databases databases = driver.load(dao, null, null);
        assertNotNull(getCallerMethodName(), databases);
        assertTrue(getCallerMethodName(), databases.getDatabases().size() > 0);
    }

    @Test
    public void testActivity() throws Exception {
        String filename = "Activity.xml";
        runFile(filename);

        Databases databases = driver.load(dao, null, null);

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");
        assertNotNull("Database does not exists: " + database.getName(), database);

        boolean tableExists = DatabaseUtil.findOneItem(database.getTables(), "name", "Activity") != null;
        assertTrue("Table doesn't exists: " + filename, tableExists);
    }

    @Test
    public void testDatabaseMetaDataTypeInfo() throws Exception {
        String filename = "DatabaseMetaDataTypeInfo.xml";

        runFile(filename);

        Databases databases = driver.load(dao, null, null);

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");
        assertNotNull("Database does not exists: " + database.getName(), database);

        boolean tableExists = DatabaseUtil.findOneItem(database.getTables(), "name", "metadatatypeinfo") != null;
        assertTrue("Table doesn't exists: " + filename, tableExists);
    }

    @Test
    public void testEmployee() throws Exception {
        String filename = "Employee.xml";

        runFile(filename);

        Databases databases = driver.load(dao, null, null);

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");
        assertNotNull("Database does not exists: " + database.getName(), database);

        boolean tableExists = DatabaseUtil.findOneItem(database.getTables(), "name", "employee") != null;
        assertTrue("Table doesn't exists: " + filename, tableExists);
    }

    @Test
    public void testProjectSchema() throws Exception {
        String filename = "project-schema.xml";

        runFile(filename);

        Databases databases = driver.load(dao, null, null);

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");
        assertNotNull("Database does not exists: " + database.getName(), database);

        boolean tableExists1 = DatabaseUtil.findOneItem(database.getTables(), "name", "civilization") != null;
        boolean tableExists2 = DatabaseUtil.findOneItem(database.getTables(), "name", "people") != null;

        assertTrue("Table CIVILIZATION doesn't exists: " + filename, tableExists1);
        assertTrue("Table PEOPLE doesn't exists: " + filename, tableExists2);
    }

    @Test
    public void testStates() throws Exception {
        String filename = "states.xml";

        runFile(filename);

        Databases databases = driver.load(dao, null, null);

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");
        assertNotNull("Database does not exists: " + database.getName(), database);

        boolean tableExists = DatabaseUtil.findOneItem(database.getTables(), "name", "states") != null;
        assertTrue("Table doesn't exists: " + filename, tableExists);
    }

    @Test
    public void testTypes() throws Exception {
        String filename = "testtypes-schema.xml";

        runFile(filename);

        Databases databases = driver.load(dao, null, null);
        assertTrue("Database length is zero: ", databases.getDatabases().size() > 0); 
        for (Database database : databases.getDatabases()) {
            System.out.println("Database: " + database.getName() + "," + database.getTables().size());
        }

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test"); 
        assertNotNull("Database does not exists: " + database.getName(), database);
        assertEquals("Database does not exists: " + database.getName(), "test", database.getName());
        assertTrue("Tables lengthis zero: ", database.getTables().size() > 0);

        boolean tableExists = DatabaseUtil.findOneItem(database.getTables(), "name", "TYPES") != null;
        for (Table table : database.getTables()) {
            System.out.println("Table: " + table.getName());
        }
        assertTrue("Table doesn't exists: " + filename, tableExists);
    }

    private void runFile(String filename) throws Exception {
        String inFilename = "etc/model/test/" + filename;
        String outFilename = "build/" + filename.replace(".xml", ".sql");

        Databases databases = DatabaseMapper.read(Databases.class, inFilename);

        sqlManager.write(new FileWriter(outFilename), dbc.getVendor(), databases);
        sqlManager.write(new DatabaseWriter(dao), dbc.getVendor(), databases);
    }
}
