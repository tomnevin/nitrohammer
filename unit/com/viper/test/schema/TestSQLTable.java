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

import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.EngineType;
import com.viper.database.model.RowFormatType;
import com.viper.database.model.Table;
import com.viper.database.model.TableType;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLTable extends AbstractTestCase {

    private static final SQLDriver driver = new SQLDriver();

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    @Test
    public void testCreateTableComplete() throws Exception {
        
        String expectedSql = "create table if not exists education.testComplete "
                + "ENGINE=MEMORY checksum=1 COMMENT 'Comment for testComplete' "
                + "PACK_KEYS=alpha PASSWORD=password123 delay_key_write=1 "
                + "ROW_FORMAT=COMPRESSED UNION=gamma DATA_DIRECTORY 'C:/temp' CHARACTER SET latin1 COLLATE latin1_german1_c";

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName("testComplete");
        table.setCharsetName("latin1");
        table.setCollationName("latin1_german1_c");
        table.setTableType(TableType.TABLE);

        table.setDataDirectory("C:/temp");
        table.setDelayKeyWrite(true);
        table.setDescription("Comment for testComplete");
        table.setEngine(EngineType.MEMORY);
        table.setHasChecksum(true);
        table.setMaximumRows(10000);
        table.setMinimumRows(10);
        table.setPackKeys("alpha");
        table.setPassword("password123");
        table.setRaidChunks("alpha");
        table.setRaidChunkSize(10000);
        table.setRaidType("beta");
        table.setRowFormat(RowFormatType.COMPRESSED);
        table.setUnion("gamma");

        String sql = driver.createTable(database, table);

        assertNotNull("testCreateTableComplete", sql);
        assertEquals("testCreateTableComplete", expectedSql, sql.trim());
    }

    @Test
    public void testCreateTableDefault() throws Exception {
        String expectedSql = "create table if not exists education.classroomDefault (id int not null primary key, "
                + "teacher varchar(32)) CHARACTER SET latin1 COLLATE latin1_german1_c";
        internalTestCreateTable("testCreateTableDefault", "classroomDefault", TableType.TABLE, expectedSql);
    }

    @Test
    public void testCreateTableAlias() throws Exception {
        String expectedSql = "create table if not exists education.classroomAlias (id int not null primary key, "
                + "teacher varchar(32)) CHARACTER SET latin1 COLLATE latin1_german1_c";
        internalTestCreateTable("testCreateTableAlias", "classroomAlias", TableType.ALIAS, expectedSql);
    }

    @Test
    public void testCreateTableBean() throws Exception {
        String expectedSql = "create table if not exists education.classroomBean (id int not null primary key, "
                + "teacher varchar(32)) CHARACTER SET latin1 COLLATE latin1_german1_c";
        internalTestCreateTable("testCreateTableBean", "classroomBean", TableType.BEAN, expectedSql);
    }

    @Test
    public void testCreateTableData() throws Exception {
        String expectedSql = "create table if not exists education.classroomData (id int not null primary key, "
                + "teacher varchar(32)) CHARACTER SET latin1 COLLATE latin1_german1_c";
        internalTestCreateTable("testCreateTableData", "classroomData", TableType.DATA, expectedSql);
    }

    @Test
    public void testCreateTableGlobalTemporary() throws Exception {
        String expectedSql = "create temporary table if not exists education.classroomGlobalTemporary (id int not null primary key, "
                + "teacher varchar(32)) CHARACTER SET latin1 COLLATE latin1_german1_c";
        internalTestCreateTable("testCreateTableGlobalTemporary", "classroomGlobalTemporary", TableType.GLOBAL_TEMPORARY,
                expectedSql);
    }

    @Test
    public void testCreateTableLocalTemporary() throws Exception {
        String expectedSql = "create temporary table if not exists education.classroomLocalTemporary (id int not null primary key, "
                + "teacher varchar(32)) CHARACTER SET latin1 COLLATE latin1_german1_c";
        internalTestCreateTable("testCreateTableLocalTemporary", "classroomLocalTemporary", TableType.LOCAL_TEMPORARY, expectedSql);
    }

    @Test
    public void testCreateTableSynonym() throws Exception {
        String expectedSql = "create table if not exists education.classroomSynonym (id int not null primary key, "
                + "teacher varchar(32)) CHARACTER SET latin1 COLLATE latin1_german1_c";
        internalTestCreateTable("testCreateTableSynonym", "classroomSynonym", TableType.SYNONYM, expectedSql);
    }

    @Test
    public void testCreateTable() throws Exception {
        String expectedSql = "create table if not exists education.classroom (id int not null primary key, "
                + "teacher varchar(32)) CHARACTER SET latin1 COLLATE latin1_german1_c";
        internalTestCreateTable("testCreateTable", "classroom", TableType.TABLE, expectedSql);
    }

    @Test
    public void testCreateTableView() throws Exception {
        String expectedSql = "create or replace view education.classroomView (id," + "teacher) as select * from alpha";
        internalTestCreateTable("testCreateTableView", "classroomView", TableType.VIEW, expectedSql);
    }

    @Test
    public void testDropTable01() throws Exception {

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName("classroom");
        table.setCharsetName("latin1");
        table.setCollationName("latin1_german1_c");
        table.setTableType(TableType.TABLE);

        String sql = driver.dropTable(database, table);

        assertNotNull("dropTable", sql);
        assertEquals("dropTable", "drop table if exists education.classroom", sql.trim());
    }

    @Test
    public void testRenameTable01() throws Exception {

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName("classroom");
        table.setCharsetName("latin1");
        table.setCollationName("latin1_german1_c");
        table.setTableType(TableType.TABLE);

        String sql = driver.renameTable(database, table, "room");

        assertNotNull("renameTable", sql);
        assertEquals("renameTable", "rename table education.classroom to education.room", sql.trim());
    }

    @Test
    public void testTruncateTable01() throws Exception {

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName("classroom");
        table.setCharsetName("latin1");
        table.setCollationName("latin1_german1_c");
        table.setTableType(TableType.TABLE);

        String sql = driver.truncateTable(database, table);

        assertNotNull("truncateTable", sql);
        assertEquals("truncateTable", "truncate table education.classroom", sql.trim());
    }

    @Test
    public void testAlterTable() throws Exception {

        String expectedSql = "alter table test.classroom1 if not exists CHARACTER SET latin1 COLLATE latin1_german1_c";

        Database database = new Database();
        database.setName("test");

        Table fromTable = new Table();
        fromTable.setName("classroom1");
        fromTable.setCharsetName("latin1");
        fromTable.setCollationName("latin1_german1_c");
        fromTable.setTableType(TableType.TABLE);

        Table toTable = new Table();
        toTable.setName("classroom1");
        toTable.setCharsetName("latin1");
        toTable.setCollationName("latin1_german1_c");
        toTable.setTableType(TableType.TABLE);

        String sql = driver.alterTable(database, fromTable, toTable);

        assertNotNull("testAlterTable", sql);
        assertEquals("testAlterTable", expectedSql, sql.trim());
    }

    @Test
    public void testAlterTable002() throws Exception {

        String expectedSQL = "alter table test.classroom if not exists drop column teacher, add column (student varchar(255)), modify column (number varchar(255))";

        Database database = new Database();
        database.setName("test");

        Table fromTable = new Table();
        fromTable.setName("classroom");
        fromTable.setTableType(TableType.TABLE);

        Column column = new Column();
        column.setName("number");
        fromTable.getColumns().add(column);

        column = new Column();
        column.setName("teacher");
        fromTable.getColumns().add(column);

        Table toTable = new Table();
        toTable.setName("classroom");
        toTable.setTableType(TableType.TABLE);

        column = new Column();
        column.setName("number");
        toTable.getColumns().add(column);

        column = new Column();
        column.setName("student");
        toTable.getColumns().add(column);
        
        System.out.println("AlterTable002: before SQL driver.");

        String sql = driver.alterTable(database, fromTable, toTable);
        
        System.out.println("AlterTable002: sql=" + sql);

        assertNotNull("testAlterTable002", sql);
        assertEquals("testAlterTable002", expectedSQL, sql.trim());
    }
    
    @Test
    public void testAlterTable003() throws Exception {

        String expectedSQL = "alter table test.classroom if not exists modify column (number varchar(255))";

        Database database = new Database();
        database.setName("test");

        Table fromTable = new Table();
        fromTable.setName("classroom");
        fromTable.setTableType(TableType.TABLE);

        Column column = new Column();
        column.setName("number");
        fromTable.getColumns().add(column);

        Table toTable = new Table();
        toTable.setName("classroom");
        toTable.setTableType(TableType.TABLE);

        column = new Column();
        column.setName("number");
        toTable.getColumns().add(column);
        
        System.out.println("AlterTable003: before SQL driver.");

        String sql = driver.alterTable(database, fromTable, toTable);
        
        System.out.println("AlterTable003: sql=" + sql);

        assertNotNull("testAlterTable003", sql);
        assertEquals("testAlterTable003", expectedSQL, sql.trim());
    }
    
    @Test
    public void testAlterTable004() throws Exception {

        String expectedSQL = "alter table test.classroom if not exists modify column (number varchar(32))";

        Database database = new Database();
        database.setName("test");

        Table fromTable = new Table();
        fromTable.setName("classroom");
        fromTable.setTableType(TableType.TABLE);

        Column column = new Column();
        column.setName("number");
        fromTable.getColumns().add(column);

        Table toTable = new Table();
        toTable.setName("classroom");
        toTable.setTableType(TableType.TABLE);

        column = new Column();
        column.setName("number");
        column.setSize(32L);
        toTable.getColumns().add(column);
        
        System.out.println("AlterTable004: before SQL driver.");

        String sql = driver.alterTable(database, fromTable, toTable);
        
        System.out.println("AlterTable004: sql=" + sql);

        assertNotNull("testAlterTable004", sql);
        assertEquals("testAlterTable004", expectedSQL, sql.trim());
    }


    // -------------------------------------------------------------------------
    private void internalTestCreateTable(String testname, String tablename, TableType tableType, String expectedSql)
            throws Exception {

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName(tablename);
        table.setCharsetName("latin1");
        table.setCollationName("latin1_german1_c");
        table.setTableType(tableType);
        table.setSqlSelect("select * from alpha");

        Column column = new Column();
        column.setName("id");
        column.setJavaType("int");
        column.setRequired(true);
        column.setPrimaryKey(true);
        column.setPersistent(true);
        table.getColumns().add(column);

        column = new Column();
        column.setName("teacher");
        column.setJavaType("String");
        column.setRequired(false);
        column.setSize(32L);
        column.setPersistent(true);
        table.getColumns().add(column);

        column = new Column();
        column.setName("roomname");
        column.setJavaType("String");
        column.setRequired(false);
        column.setSize(32L);
        column.setPersistent(false);
        table.getColumns().add(column);

        column = new Column();
        column.setName("student");
        column.setJavaType("String");
        column.setRequired(false);
        column.setSize(32L);
        column.setPersistent(false);
        table.getColumns().add(column);

        String sql = driver.createTable(database, table);

        assertNotNull(testname, sql);
        assertEquals(testname, expectedSql, sql.trim());
    }
}
