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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.Index;
import com.viper.database.model.Table;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLIndex extends AbstractTestCase {

    private static final SQLDriver driver = new SQLDriver();

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    protected Databases startup() throws Exception {

        String META_FILENAME = "res:/com/viper/test/schema/MetaDatabaseManagerExporter001.xml";
        return DatabaseMapper.read(Databases.class, META_FILENAME);
    }

    @Test
    public void testCreateIndex01() throws Exception {
        String tablename = "basic_table";
        String indexname = "indexname";
        String expectedSql = "create unique index " + indexname + " using btree on test." + tablename + " ( NAME asc )";
        internalTestCreateIndex("testCreateIndex01", tablename, indexname, expectedSql);
    }

    @Test
    public void testCreateIndex02() throws Exception {
        String tablename = "basic_table";
        String indexname = "indexname02";
        String expectedSql = "create unique index " + indexname + " using hash on test." + tablename + " ( NAME asc )";
        internalTestCreateIndex("testCreateIndex01", tablename, indexname, expectedSql);
    }

    @Test
    public void testDropIndex01() throws Exception {
        Databases databases = startup();

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        String indexname = "indexname";

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table table not found", table);

        Index index =DatabaseUtil.findOneItem(table.getIndices(), "name", indexname);
        assertNotNull(indexname + " index not found", index);

        String sql = driver.dropIndex(database, table, index);

        assertNotNull("dropIndex", sql);
        assertEquals("dropIndex", "alter table test.basic_table drop index " + indexname, sql.trim());
      
    }

    @Test
    public void testRenameIndex01() throws Exception {
        Databases databases = startup();

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");
        String indexname = "indexname";

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table table not found", table);

        Index index = DatabaseUtil.findOneItem(table.getIndices(), "name", indexname);
        assertNotNull(indexname + " index not found", index);

        String sql = driver.renameIndex(database, table, index, "room");

        assertNotNull("renameIndex", sql);
    }

    // -------------------------------------------------------------------------
    private void internalTestCreateIndex(String testname, String tablename, String indexname, String expectedSql) throws Exception {
        Databases databases = startup();

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
        assertNotNull(testname + ", " + tablename + " table not found", table);

        Index index = DatabaseUtil.findOneItem(table.getIndices(), "name", indexname);
        assertNotNull(testname + ", " + indexname + " index not found", index);

        String sql = driver.createIndex(database, table, index);

        assertNotNull(testname, sql);
        assertEquals(testname, expectedSql, sql.trim());
    }
}
