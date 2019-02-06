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
import com.viper.database.model.EngineType;
import com.viper.database.model.Table;
import com.viper.database.model.TableType;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestJDBCTable extends AbstractTestCase {

    private final static String DATABASE_NAME = "test";
    private final static String DRIVER_NAME = "mysql";
    private final static String TEST_FILENAME = "res:/com/viper/test/schema/TestJDBCTable.xml";
    private final static String DatabaseName = "test";
    private final static String TableName = "classroom";

    private static final SQLDriver driver = new SQLDriver();
    private DatabaseSQLInterface dao = null;

    @Before
    public void setUp() throws Exception {

        dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", dao);

        Databases databases = DatabaseMapper.readDatabases(TEST_FILENAME);
        assertNotNull("failed to read : " + TEST_FILENAME, databases);

        SqlConverter.write(new DatabaseWriter(dao), DRIVER_NAME, databases);
    }

    @After
    public void tearDown() throws Exception {
        ((DatabaseInterface) dao).release();
    }

    @Test
    public void testCreateTable() throws Exception {

        Databases databases = DatabaseMapper.readDatabases(TEST_FILENAME);
        assertNotNull("Database not created " + TEST_FILENAME, databases);

        for (Database database : databases.getDatabases()) {
            Table table = DatabaseUtil.findOneItem(database.getTables(), "name", TableName);

            dao.write(driver.dropTable(database, table));

            dao.write(driver.createTable(database, table));
            assertNotNull("listTables", database.getTables());

            List<Table> actual = driver.loadTables(dao, DatabaseName, TableName);

            assertEquals("table " + TableName + " was NOT created", 1, actual.size());
        }
    }

    @Test
    public void testDropTable() throws Exception {

        Databases databases = DatabaseMapper.readDatabases(TEST_FILENAME);
        assertNotNull("Database not created " + TEST_FILENAME, databases);

        for (Database database : databases.getDatabases()) {
            Table table = DatabaseUtil.findOneItem(database.getTables(), "name", TableName);
            assertNotNull("Table not created " + TableName, table);

            dao.write(driver.dropTable(database, table));

            List<Table> actual = driver.loadTables(dao, DatabaseName, TableName);

            assertEquals("table " + TableName + " was NOT created", 1, actual.size());
        }
    }

    @Test
    public void testRenameTable() throws Exception {

        String newtablename = "newclassroom";

        Databases databases = DatabaseMapper.readDatabases(TEST_FILENAME);
        assertNotNull("Database not created " + TEST_FILENAME, databases);

        for (Database database : databases.getDatabases()) {
            Table table = DatabaseUtil.findOneItem(database.getTables(), "name", TableName);
            assertNotNull("Table exists " + TEST_FILENAME, table);

            dao.write(driver.dropTable(database, table));

            List<Table> actual = driver.loadTables(dao, DatabaseName, TableName);

            assertEquals("table " + TableName + " was NOT dropped", 0, actual.size());

            dao.write(driver.createTable(database, table));

            Table renameTable = DatabaseMgr.findTable(databases, DatabaseName, newtablename);
            if (renameTable != null) {
                dao.write(driver.dropTable(database, renameTable));
            }

            actual = driver.loadTables(dao, DatabaseName, TableName);
            assertEquals("table " + TableName + " was NOT created", 1, actual.size());

            driver.renameTable(database, table, newtablename);

            actual = driver.loadTables(dao, DatabaseName, TableName);
            assertEquals("table " + TableName + " was NOT renamed", 1, actual.size());

            actual = driver.loadTables(dao, DatabaseName, newtablename);
            assertEquals("table " + newtablename + " was NOT renamed", 1, actual.size());
        }
    }

    @Test
    public void testLoadDatabaseMetaInfo() throws Exception {

        Databases databases = DatabaseMapper.readDatabases(TEST_FILENAME);
        assertNotNull("Database not created " + TEST_FILENAME, databases);

        for (Database database : databases.getDatabases()) {
            Table table = DatabaseUtil.findOneItem(database.getTables(), "name", TableName);
            assertNotNull("Table exists " + TEST_FILENAME, table);

            dao.write(driver.dropTable(database, table));

            List<Table> actual = driver.loadTables(dao, DatabaseName, TableName);
            assertEquals("table " + TableName + " was NOT dropped", 0, actual.size());

            dao.write(driver.createTable(database, table));
        }

        Databases databases1 = driver.load(dao, DatabaseName, TableName);

        assertEquals("Database test is null", 1, databases1.getDatabases().size());
        assertEquals("table " + TableName + " not found", 1, databases1.getDatabases().get(0).getTables().size());
    }

    @Test
    public void testLoadCustomInfo() throws Exception {

        List<Table> actual = driver.loadTables(dao, DatabaseName, TableName);

        assertNotNull("table " + TableName + " not found", actual);
        assertEquals("table " + TableName + " not found", 1, actual.size());

        Table table = actual.get(0);

        // Standard parameters
        assertEquals("table_collation", "latin1_swedish_ci", table.getCollationName());
        assertNotNull("description", table.getDescription());
        assertEquals("engine", EngineType.INNODB, table.getEngine());
        assertEquals("name", TableName, table.getName());
        assertEquals("table-type", TableType.BASE_TABLE, table.getTableType());
    }

    @Test
    public void testInformationSchema() throws Exception {

        String databasename = "information_schema";

        Databases databases = driver.load(dao, databasename, null);

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", databasename);
        assertNotNull(databasename + " not found", database);

        assertNotNull("database " + databasename + " no tables found", database.getTables());
        assertTrue("database " + databasename + " zero tables found", database.getTables().size() > 0);

    }

    @Test
    public void testLoadMetaData() throws Exception {

        Databases databases = driver.load(dao, null, null);
        assertNotNull("MetaData not found for driver: ", databases);

    }
}
