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
import com.viper.database.model.ForeignKey;
import com.viper.database.model.ForeignKeyReference;
import com.viper.database.model.Table;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestJDBCForeignKey extends AbstractTestCase {

    private final static String DATABASE_NAME = "test";
    private final static String DRIVER_NAME = "mysql";

    private final static String EXPECTED_META_FILENAME = "res:/com/viper/test/schema/TestJDBCForeignKey001.xml";
    private static final SQLDriver driver = new SQLDriver();

    private DatabaseSQLInterface dao = null;

    private String localTablename = "LocalTable";
    private String foreignTablename = "ForeignTable";

    @Before
    public void setUp() throws Exception {

        dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", dao);

        Databases databases = DatabaseMapper.read(Databases.class, EXPECTED_META_FILENAME);

        for (Database database : databases.getDatabases()) {
            dao.write(driver.dropDatabase(database));
        }

        SqlConverter.write(new DatabaseWriter(dao), DRIVER_NAME, databases);
    }

    @After
    public void tearDown() throws Exception {
        ((DatabaseInterface) dao).release();
    }

    // -------------------------------------------------------------------------
    @Test
    public void testCreateForeignKey() throws Exception {

        Databases databases = DatabaseMapper.read(Databases.class, EXPECTED_META_FILENAME);

        for (Database database : databases.getDatabases()) {

            Databases md = driver.load(dao, database.getName(), null);
            assertNotNull("Databases not loaded from JDBC", databases);
            assertTrue("Database count is not right", databases.getDatabases().size() > 0);

            Table localTable = DatabaseMgr.findTable(databases, database.getName(), localTablename);
            assertNotNull("table " + localTablename + " not found", localTable);

            Table foreignTable = DatabaseMgr.findTable(databases, database.getName(), foreignTablename);
            assertNotNull("table " + foreignTablename + " not found", foreignTable);

            assertTrue("must have at least one foreign key", localTable.getForeignKeys().size() > 0);

            for (ForeignKey foreignKey : localTable.getForeignKeys()) {
                assertFalse("Should not be a primary foreign key", foreignKey.getName().equalsIgnoreCase("PRIMARY"));

                String name = foreignKey.getName();
                dao.write(driver.dropForeignKey(database, localTable, foreignKey));

                md = driver.load(dao, database.getName(), null);

                ForeignKey foreignkey = DatabaseMgr.findForeignKey(md, database.getName(), localTable.getName(), name);
                assertNull("foreignKey " + name + " still exists", foreignkey);

                ForeignKeyReference foreignKeyReference = foreignKey.getForeignKeyReferences().get(0);
                assertNotNull("foreignKeyReference has no first foreignKey", foreignKeyReference);

                dao.write(driver.createForeignKey(database, localTable, foreignKey));

                md = driver.load(dao, database.getName(), null);

                foreignkey = DatabaseMgr.findForeignKey(md, database.getName(), localTable.getName(), name);
                assertNotNull("foreignKey " + name + " does not exists", foreignkey);
            }
        }
    }

    // -------------------------------------------------------------------------
    @Test
    public void testDropForeignKey() throws Exception {

        Databases databases = DatabaseMapper.read(Databases.class, EXPECTED_META_FILENAME);

        for (Database database : databases.getDatabases()) {

            Table localTable = DatabaseUtil.findOneItem(database.getTables(), "name", localTablename);
            assertNotNull("table " + localTablename + " not found", localTable);

            Table foreignTable = DatabaseUtil.findOneItem(database.getTables(), "name", foreignTablename);
            assertNotNull("table " + foreignTablename + " not found", foreignTable);

            for (ForeignKey foreignKey : localTable.getForeignKeys()) {
                assertFalse("Should not be a primary foreign key", foreignKey.getName().equalsIgnoreCase("PRIMARY"));

                String name = foreignKey.getName();
                dao.write(driver.dropForeignKey(database, localTable, foreignKey));

                Databases md = driver.load(dao, database.getName(), null);

                assertNull("foreignKey " + name + " still exists",
                        DatabaseMgr.findForeignKeyByLocalColumnName(md, database.getName(), localTable.getName(), name));
            }
        }
    }

    // -------------------------------------------------------------------------
    @Test
    public void testLoadDatabaseMetaInfo() throws Exception {

        Databases databases = DatabaseMapper.read(Databases.class, EXPECTED_META_FILENAME);

        for (Database database : databases.getDatabases()) {

            Table localTable = DatabaseUtil.findOneItem(database.getTables(), "name", localTablename);
            assertNotNull("table " + localTablename + " not found", localTable);

            Table foreignTable = DatabaseUtil.findOneItem(database.getTables(), "name", foreignTablename);
            assertNotNull("table " + foreignTablename + " not found", foreignTable);

            for (ForeignKey foreignKey : localTable.getForeignKeys()) {
                if (foreignKey.getName().equals("PRIMARY")) {
                    continue;
                }

                String name = foreignKey.getName();
                String tablepath = database.getName() + "." + localTable.getName();
            }
        }
    }
}
