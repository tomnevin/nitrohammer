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
import com.viper.database.model.Table;
import com.viper.database.model.TableType;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLTableView extends AbstractTestCase {

    private static final SQLDriver driver = new SQLDriver();

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    protected Databases startup() throws Exception {

        String META_FILENAME = "res:/com/viper/test/schema/MetaDatabaseManagerExporter001.xml";

		Databases databases = DatabaseMapper.read(Databases.class, META_FILENAME);
        assertNotNull("Database empty for " + META_FILENAME, databases);

        return databases;
    }

    @Test
    public void testCreateTableView01() throws Exception {

        Databases databases = startup();
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "assignments");
        assertNotNull("assignments table not found", table);
        assertEquals("asignments table must be table view", TableType.VIEW, table.getTableType());

        String sql = driver.createTable(database, table);

        assertNotNull("createView", sql);
        assertEqualsIgnoreWhiteSpace("createView", "create or replace view test.assignments "
                + "(id,lessonid,active,due_date) as "
                + "select concat(h.teacher, h.roomname, h.lessonid) as id, l.cube, l.grade, "
                + "l.subject, l.skill, l.title, h.teacher, h.roomname, h.lessonid, h.active, "
                + "h.due_date from homework h left outer join lesson l on h.lessonid=l.id group " + "by l.id order by l.id",
                sql.trim());
    }

    @Test
    public void testDropTableView01() throws Exception {

        Databases databases = startup();
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "assignments");
        assertNotNull("assignments table not found", table);
        assertEquals("asignments table must be table view", TableType.VIEW, table.getTableType());

        String sql = driver.dropTable(database, table);

        assertNotNull("dropView", sql);
        assertEquals("dropView", "drop view if exists test.assignments", sql.trim());
    }

    @Test
    public void testRenameTableView01() throws Exception {

        Databases databases = startup();
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "assignments");
        assertNotNull("assignments table not found", table);
        assertEquals("asignments table must be table view", TableType.VIEW, table.getTableType());

        String sql = driver.renameTable(database, table, "test");

        assertNotNull("renameView", sql);
        assertEqualsIgnoreWhiteSpace("renameView", "rename view test.assignments to test", sql.trim());
    }

    @Test
    public void testAlterTableView01() throws Exception {

        Databases databases = startup();
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "assignments");
        assertNotNull("assignments table not found", table);
        assertEquals("asignments table must be table view", TableType.VIEW, table.getTableType());

        String sql = driver.alterTable(database, table, table);

        assertNotNull("alterView", sql);
        assertEqualsIgnoreWhiteSpace("alterView", "create or replace view test.assignments "
                + "(id,lessonid,active,due_date) as "
                + "select concat(h.teacher, h.roomname, h.lessonid) as id, l.cube, l.grade, "
                + "l.subject, l.skill, l.title, h.teacher, h.roomname, h.lessonid, h.active, "
                + "h.due_date from homework h left outer join lesson l on h.lessonid=l.id group " + "by l.id order by l.id",
                sql.trim());
    }
}
