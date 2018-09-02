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
import com.viper.database.model.Index;
import com.viper.database.model.IndexClassType;
import com.viper.database.model.IndexType;
import com.viper.database.model.Table;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestJDBCIndex extends AbstractTestCase {

    private final static String DATABASE_NAME = "test";
    private final static String DRIVER_NAME = "mysql";

    private final static String TEST_FILENAME_001 = "res:/com/viper/test/schema/TestJDBCIndex001.xml";
    private final static String TABLENAME_A = "INDEXTABLE";
    private final static String INDEXNAME_A = "index0001";

    private static final SQLDriver driver = new SQLDriver();

    private DatabaseSQLInterface dao = null;

    @Before
    public void setUp() throws Exception {

        dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", dao);

        driver.setIgnore(true);

        Databases databases = DatabaseMapper.read(Databases.class, TEST_FILENAME_001);

        SqlConverter.write(new DatabaseWriter(dao), DRIVER_NAME, databases);
    }

    @After
    public void tearDown() throws Exception {
        ((DatabaseInterface) dao).release();
    }

    @Test
    public void testCreateIndexUniqueBtree() throws Exception {
        internalTestCreateIndex("testCreateIndexUniqueBtree", TABLENAME_A, INDEXNAME_A, IndexClassType.UNIQUE, IndexType.BTREE);
    }

    @Test
    public void testCreateIndexFullText() throws Exception {
        internalTestCreateIndex("testCreateIndexFullText", TABLENAME_A, "index0002", IndexClassType.FULLTEXT, IndexType.DEFAULT);
    }

    // @Test
    // public void testCreateIndexSpatial() throws Exception {
    // internalTestCreateIndex("testCreateIndexSpatial", TABLENAME_A,
    // "index0003", IndexClassType.UNIQUE, IndexType.DEFAULT);
    // }

    @Test
    public void testCreateIndexUniqueHash() throws Exception {
        internalTestCreateIndex("testCreateIndexUniqueHash", "INDEXTABLE2", "index0010", IndexClassType.DEFAULT, IndexType.HASH);
    }

    @Test
    public void testDropIndex() throws Exception {

        Databases databases = DatabaseMapper.read(Databases.class, TEST_FILENAME_001);

        for (Database database : databases.getDatabases()) {

            Table table = DatabaseUtil.findOneItem(database.getTables(), "name", TABLENAME_A);
            assertNotNull("table " + TABLENAME_A + " not found", table);

            Index index = DatabaseUtil.findOneItem(table.getIndices(), "name", INDEXNAME_A);
            assertNotNull("index " + INDEXNAME_A + " not found", index);

            dao.write(driver.dropIndex(database, table, index));

            Databases md = driver.load(dao, database.getName(), table.getName());
            assertNull("index " + INDEXNAME_A + " still exists",
                    DatabaseMgr.findIndex(md, database.getName(), table.getName(), INDEXNAME_A));
        }
    }

    @Test
    public void testRenameIndex() throws Exception {

        String newindexname = "new-index-name";

        Databases databases = DatabaseMapper.read(Databases.class, TEST_FILENAME_001);

        for (Database database : databases.getDatabases()) {

            Table table = DatabaseUtil.findOneItem(database.getTables(), "name", TABLENAME_A);
            assertNotNull("table " + TABLENAME_A + " not found", table);

            Index index = DatabaseUtil.findOneItem(table.getIndices(), "name", INDEXNAME_A);
            assertNotNull("index " + INDEXNAME_A + " not found", index);

            dao.write(driver.dropIndex(database, table, index));

            Databases md = driver.load(dao, database.getName(), table.getName());
            assertNull("index " + INDEXNAME_A + " still exists",
                    DatabaseMgr.findIndex(md, database.getName(), table.getName(), INDEXNAME_A));

            dao.write(driver.createIndex(database, table, index));

            md = driver.load(dao, database.getName(), table.getName());
            assertNotNull("index " + INDEXNAME_A + " does not exists",
                    DatabaseMgr.findIndex(md, database.getName(), table.getName(), INDEXNAME_A));

            dao.write(driver.renameIndex(database, table, index, newindexname));

            md = driver.load(dao, database.getName(), table.getName());
            assertNull("index " + INDEXNAME_A + " does not exists",
                    DatabaseMgr.findIndex(md, database.getName(), table.getName(), INDEXNAME_A));
            assertNotNull("index " + newindexname + " does not exists",
                    DatabaseMgr.findIndex(md, database.getName(), table.getName(), newindexname));
        }
    }

    private void internalTestCreateIndex(String testName, String tablename, String indexName, IndexClassType indexClass,
            IndexType indexType) throws Exception {

        Databases databases = DatabaseMapper.read(Databases.class, TEST_FILENAME_001);

        for (Database database : databases.getDatabases()) {

            Table table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
            assertNotNull(testName + " table " + tablename + " not found", table);

            Index index = DatabaseUtil.findOneItem(table.getIndices(), "name", indexName);
            assertNotNull(testName + " index " + indexName + " not found", index);

            dao.write(driver.dropIndex(database, table, index));

            Databases md = driver.load(dao, database.getName(), table.getName());
            assertNull(testName + " index " + indexName + " still exists",
                    DatabaseMgr.findIndex(md, database.getName(), table.getName(), indexName));

            dao.write(driver.createIndex(database, table, index));

            md = driver.load(dao, database.getName(), table.getName());

            Index actualIndex = DatabaseMgr.findIndex(md, database.getName(), table.getName(), indexName);
            assertNotNull(testName + " index " + indexName + " does not exists", actualIndex);
            assertEquals(testName + " IndexClass", indexClass, actualIndex.getIndexClass());
            assertEquals(testName + " IndexType", indexType, actualIndex.getIndexType());
        }
    }
}
