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

import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.Row;
import com.viper.database.model.Table;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.demo.mysql.information_schema.model.Columns;
import com.viper.demo.mysql.information_schema.model.InnodbLocks;
import com.viper.demo.mysql.information_schema.model.Tables;

public class TestJDBC extends AbstractTestCase {

    private static final String DATABASE_NAME = "test";
    private static final SQLDriver driver = new SQLDriver();

    @Test
    public void testStatmentIsClosed() throws Exception {
        System.out.println("testStatmentIsClosed: connecting to database ");

        DatabaseInterface writer = DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", writer);

        writer.release();
    }

    @Test
    public void testLoadDatabases() throws Exception {

        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("DatabaseInterface should not be null", dao);

        List<Database> actual = driver.loadDatabases(dao, DATABASE_NAME);

        assertNotNull("databases.getDatabase() should not be null", actual);
        assertEquals("databases.getDatabase().size() should not be zero", 1, actual.size());
    }

    @Test
    public void testLoadTables() throws Exception {

        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("DatabaseInterface should not be null", dao);

        List<Table> actual = driver.loadTables(dao, DATABASE_NAME, null);

        assertNotNull("databases.getDatabase() should not be null", actual);
        assertTrue("databases.getDatabase().size() should not be zero", actual.size() > 0);
    }

    @Test
    public void testLoadColumns() throws Exception {

        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("DatabaseInterface should not be null", dao);

        Databases databases = driver.load(dao, DATABASE_NAME, null);
        assertNotNull("databases.getDatabase() should not be null", databases.getDatabases());
        assertEquals("databases.getDatabase().size() should not be zero", 1, databases.getDatabases().size());

        for (Table table : databases.getDatabases().get(0).getTables()) {
            assertTrue("databases.getDatabase().size() should not be zero", table.getColumns().size() > 0);
        }
    }

    @Test
    public void testReadColumns() throws Exception {

        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("DatabaseInterface should not be null", dao);

        List<Row> rows = dao.readRows("select * from information_schema.columns");
        assertNotNull("rows should not be null", rows);
        assertTrue("rows.size() should not be zero", rows.size() > 0);
    }

    @Test
    public void testReadColumnBeans() throws Exception {

        DatabaseInterface dao = DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("DatabaseInterface should not be null", dao);

        List<Columns> columns = dao.queryAll(Columns.class);
        assertNotNull("rows should not be null", columns);
        assertTrue("rows.size() should not be zero", columns.size() > 0);

        dao.release();
    }

    @Test
    public void testReadInnodbLocksBeans() throws Exception {

        DatabaseInterface dao = DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("DatabaseInterface should not be null", dao);

        List<InnodbLocks> columns = dao.queryAll(InnodbLocks.class);
        assertNotNull("rows should not be null", columns);
        // assertTrue("rows.size() should not be zero", columns.size() > 0);

        dao.release();
    }

    @Test
    public void testReadTablesBeans() throws Exception {

        DatabaseInterface dao = DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("DatabaseInterface should not be null", dao);

        List<Tables> columns = dao.queryAll(Tables.class);
        assertNotNull("rows should not be null", columns);
        assertTrue("rows.size() should not be zero", columns.size() > 0);

        dao.release();
    }
}
