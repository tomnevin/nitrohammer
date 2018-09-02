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
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.Table;
import com.viper.database.model.TableType;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLColumn extends AbstractTestCase {

    private final static String vendor = "mysql";
    private static final SQLDriver driver = new SQLDriver();

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    @Test
    public void testAddColumn() throws Exception {
        DatabaseConnection dbc = new DatabaseConnection();
        dbc.setVendor(vendor);

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName("classroom");
        table.setCharsetName("latin1");
        table.setCollationName("latin1_german1_c");

        Column column = new Column();
        column.setName("id");
        column.setJavaType("int");
        column.setRequired(true);
        column.setPrimaryKey(true);
        column.setPersistent(true);
        table.getColumns().add(column);

        String sql = driver.addColumn(database, table, column);

        assertNotNull("addColumn", sql);
        assertEquals("addColumn", "alter table education.classroom add column (id int not null primary key)", sql.trim());
    }

    @Test
    public void testAddDefaultColumn() throws Exception {
        DatabaseConnection dbc = new DatabaseConnection();
        dbc.setVendor(vendor);

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName("classroom");

        Column column = new Column();
        column.setName("id");
        table.getColumns().add(column);

        String sql = driver.addColumn(database, table, column);

        assertNotNull("addColumn", sql);
        assertEquals("addColumn", "alter table education.classroom add column (id varchar(255))", sql.trim());
    }

    @Test
    public void testRenameColumn() throws Exception {
        DatabaseConnection dbc = new DatabaseConnection();
        dbc.setVendor(vendor);

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName("classroom");
        table.setCharsetName("latin1");
        table.setCollationName("latin1_german1_c");
        table.setTableType(TableType.TABLE);

        Column column = new Column();
        column.setName("id");
        column.setJavaType("int");
        column.setRequired(true);
        column.setPrimaryKey(true);
        column.setPersistent(true);

        table.getColumns().add(column);

        String sql = driver.renameColumn(database, table, column, "newId");

        assertNotNull("renameColumn", sql);
        assertEquals("renameColumn", "alter table education.classroom change column id newId int", sql.trim());
    }

    @Test
    public void testDropColumn() throws Exception {
        DatabaseConnection dbc = new DatabaseConnection();
        dbc.setVendor(vendor);

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName("classroom");
        table.setCharsetName("latin1");
        table.setCollationName("latin1_german1_c");
        table.setTableType(TableType.TABLE);

        Column column = new Column();
        column.setName("id");
        column.setJavaType("int");
        column.setRequired(true);
        column.setPrimaryKey(true);
        column.setPersistent(true);
        table.getColumns().add(column);

        String sql = driver.dropColumn(database, table, column);

        assertNotNull("dropColumn", sql);
        assertEquals("dropColumn", "alter table education.classroom drop column id", sql.trim());
    }

    @Test
    public void testColumnAppendColumnTypeByte() throws Exception {
        baseColumnAppendColumnType("byte", 10, 3, "tinyint(10)");
        baseColumnAppendColumnType("byte", null, 3, "tinyint");
        baseColumnAppendColumnType("Byte", 10, 3, "tinyint(10)");
        baseColumnAppendColumnType("Byte", null, 3, "tinyint");
    }

    @Test
    public void testColumnAppendColumnTypeShort() throws Exception {
        baseColumnAppendColumnType("short", 10, 3, "smallint");
        baseColumnAppendColumnType("short", null, 3, "smallint");
        baseColumnAppendColumnType("Short", 10, 3, "smallint");
        baseColumnAppendColumnType("Short", null, 3, "smallint");
    }

    @Test
    public void testColumnAppendColumnTypeInteger() throws Exception {
        baseColumnAppendColumnType("int", 10, 3, "int(10)");
        baseColumnAppendColumnType("int", null, 3, "int");
        baseColumnAppendColumnType("Integer", 10, 3, "int(10)");
        baseColumnAppendColumnType("Integer", null, 3, "int");
    }

    @Test
    public void testColumnAppendColumnTypeLong() throws Exception {
        baseColumnAppendColumnType("long", 10, 3, "bigint");
        baseColumnAppendColumnType("long", null, 3, "bigint");
        baseColumnAppendColumnType("Long", 10, 3, "bigint");
        baseColumnAppendColumnType("Long", null, 3, "bigint");
    }

    @Test
    public void testColumnAppendColumnTypeFloat() throws Exception {
        baseColumnAppendColumnType("float", 10, 3, "float(10,3)");
        baseColumnAppendColumnType("float", null, 3, "float");
        baseColumnAppendColumnType("float", null, null, "float");
        baseColumnAppendColumnType("Float", 10, 3, "float(10,3)");
        baseColumnAppendColumnType("Float", null, 3, "float");
        baseColumnAppendColumnType("Float", null, null, "float");
    }

    @Test
    public void testColumnAppendColumnTypeDouble() throws Exception {
        baseColumnAppendColumnType("double", 10, 3, "double(10,3)");
        baseColumnAppendColumnType("double", null, 3, "double");
        baseColumnAppendColumnType("double", null, null, "double");
        baseColumnAppendColumnType("Double", 10, 3, "double(10,3)");
        baseColumnAppendColumnType("Double", null, 3, "double");
        baseColumnAppendColumnType("Double", null, null, "double");
    }

    @Test
    public void testColumnAppendColumnTypeChar() throws Exception {
        baseColumnAppendColumnType("char", 10, 3, "char");
        baseColumnAppendColumnType("char", null, null, "char");
        baseColumnAppendColumnType("char", 255, null, "char");
        baseColumnAppendColumnType("Character", 10, 3, "char");
        baseColumnAppendColumnType("Character", null, null, "char");
        baseColumnAppendColumnType("Character", 255, null, "char");
    }

    @Test
    public void testColumnAppendColumnTypeString() throws Exception {
        baseColumnAppendColumnType("String", 10, 3, "varchar(10)");
        baseColumnAppendColumnType("String", null, null, "varchar(255)");
        baseColumnAppendColumnType("String", 255, null, "varchar(255)");
    }

    @Test
    public void testColumnAppendColumnTypeBoolean() throws Exception {
        baseColumnAppendColumnType("boolean", 10, 3, "tinyint");
        baseColumnAppendColumnType("boolean", null, null, "tinyint");
        baseColumnAppendColumnType("Boolean", 10, 3, "tinyint");
        baseColumnAppendColumnType("Boolean", null, null, "tinyint");
    }

    @Test
    public void testColumnAppendColumnTypeDate() throws Exception {
        baseColumnAppendColumnType("java.util.Date", 10, 3, "date");
        baseColumnAppendColumnType("java.util.Date", null, null, "date");
    }

    @Test
    public void testColumnAppendColumnTypeSqlDate() throws Exception {
        baseColumnAppendColumnType("java.sql.Date", 10, 3, "date");
        baseColumnAppendColumnType("java.sql.Date", null, null, "date");
    }

    @Test
    public void testColumnAppendColumnTypeSqlTime() throws Exception {
        baseColumnAppendColumnType("java.sql.Time", 10, 3, "time");
        baseColumnAppendColumnType("java.sql.Time", null, null, "time");
    }

    @Test
    public void testColumnAppendColumnTypeSqlTimestamp() throws Exception {
        baseColumnAppendColumnType("java.sql.Timestamp", 10, 3, "timestamp");
        baseColumnAppendColumnType("java.sql.Timestamp", null, null, "timestamp");
    }

    @Test
    public void testColumnAppendColumnTypeBigInteger() throws Exception {
        baseColumnAppendColumnType("java.math.BigInteger", 10, 3, "bigint(10)");
        baseColumnAppendColumnType("java.math.BigInteger", null, 3, "bigint");
    }

    @Test
    public void testColumnAppendColumnTypeBigDecimal() throws Exception {
        baseColumnAppendColumnType("java.math.BigDecimal", 10, 3, "decimal(10,3)");
        baseColumnAppendColumnType("java.math.BigDecimal", null, 3, "decimal");
        baseColumnAppendColumnType("java.math.BigDecimal", null, null, "decimal");
    }

    @Test
    public void testColumnAppendColumnTypeArray() throws Exception {
        baseColumnAppendColumnType("java.sql.Array", 10, 3, "varchar(10)");
        baseColumnAppendColumnType("java.sql.Array", null, null, "varchar(255)");
    }

    @Test
    public void testColumnAppendColumnTypeBlob() throws Exception {
        baseColumnAppendColumnType("java.sql.Blob", 10, 3, "blob");
        baseColumnAppendColumnType("java.sql.Blob", null, null, "blob");
    }

    @Test
    public void testColumnAppendColumnTypeClob() throws Exception {
        baseColumnAppendColumnType("java.sql.Clob", 10, 3, "clob");
        baseColumnAppendColumnType("java.sql.Clob", null, null, "clob");
    }

    @Test
    public void testColumnAppendColumnTypeNClob() throws Exception {
        baseColumnAppendColumnType("java.sql.NClob", 10, 3, "longtext");
        baseColumnAppendColumnType("java.sql.NClob", null, null, "longtext");
    }

    @Test
    public void testColumnAppendColumnTypeRef() throws Exception {
        baseColumnAppendColumnType("java.sql.Ref", 10, 3, "varchar(10)");
        baseColumnAppendColumnType("java.sql.Ref", null, null, "varchar(255)");
    }

    @Test
    public void testColumnAppendColumnTypeStruct() throws Exception {
        baseColumnAppendColumnType("java.sql.Struct", 10, 3, "blob");
        baseColumnAppendColumnType("java.sql.Struct", null, null, "blob");
    }

    // -------------------------------------------------------------------------

    public void baseColumnAppendColumnType(String javaType, Integer size, Integer decimalSize, String expected) throws Exception {

        Database database = new Database();
        database.setName("education");

        Table table = new Table();
        table.setName("classroom");

        Column column = new Column();
        column.setName("id");
        column.setJavaType(javaType);
        if (size != null) {
            column.setSize((long) size);
        }
        column.setDecimalSize(decimalSize);

        String sql = driver.addColumn(database, table, column);
        
        assertNotNull("sql read for addColumn is null.", sql);
        assertTrue(getCallerMethodName() + ", " + sql + " vs " + expected, sql.contains(expected));
    }
}
