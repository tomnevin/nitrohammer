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

package com.viper.test;

import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.DatabaseWriter;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.Row;
import com.viper.database.model.Table;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;
import com.viper.demo.unit.model.Employee;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBasicSimulation extends AbstractTestCase {

    private final static String DATABASE_NAME = "test";

    @Rule
    public TestRule benchmarkRule = new BenchmarkRule();

    @Test
    public void testSimulationRoundTrip() throws Exception {
        String databaseFilename = "etc/model/test/Employee.xml";

        Databases databases = DatabaseMapper.read(Databases.class, databaseFilename);
        assertNotNull("DatabasesMgr.importFile return null", databases);

        DatabaseInterface writer = DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", writer);

        List<Employee> list = RandomBean.getRandomBeans(Employee.class, 1, 10);
        writer.insertAll(list);
    }

    @Test
    public void testPerformanceInsertNoIndex100000_1() throws Exception {
        performSimulationInsert(getCallerMethodName(), "PERFORMANCE_NO_INDEX", 1, 1);
    }

    @Test
    public void testPerformanceInsertNoIndex100000_16() throws Exception {
        performSimulationInsert(getCallerMethodName(), "PERFORMANCE_NO_INDEX", 1, 16);
    }

    @Test
    public void testPerformanceInsertNoIndex100000_128() throws Exception {
        performSimulationInsert(getCallerMethodName(), "PERFORMANCE_NO_INDEX", 1, 128);
    }

    @Test
    public void testPerformanceInsertNoIndex100000_1024() throws Exception {
        performSimulationInsert(getCallerMethodName(), "PERFORMANCE_NO_INDEX", 1, 1024);
    }

    @Test
    public void testPerformanceInsertNoIndex1000000_128() throws Exception {
        performSimulationInsert(getCallerMethodName(), "PERFORMANCE_NO_INDEX", 1, 128);
    }

    @Test
    public void testPerformanceInsert1Index100000_128() throws Exception {
        performSimulationInsert(getCallerMethodName(), "PERFORMANCE_1_INDEX", 1, 128);
    }

    @Test
    public void testPerformanceQueryNoIndex10() throws Exception {
        performSimulationQuery(getCallerMethodName(), "PERFORMANCE_NO_INDEX", 1);
    }

    @Test
    public void testPerformanceQuery1Index10() throws Exception {
        performSimulationQuery(getCallerMethodName(), "PERFORMANCE_1_INDEX", 1);
    }

    @Test
    public void testSimulationManager() throws Exception {
        String filename = "res:/com/viper/test/TestBasicSimulation.xml";

        Database database = DatabaseMapper.read(Database.class, filename);
        assertNotNull("testSimulationManager.DatabaseSimulationBean", database);
        assertNotNull("testSimulationManager.DatabaseSimulationBean", database);
        assertEquals("DatabaseSimulationBean.filename", "com/viper/test/TestBasicSimulation.xml", database.getFilename());
        assertEquals("DatabaseSimulationBean.name", "test", database.getName());
        assertNotNull("DatabaseSimulationBean.tables", database.getTables());
        assertEquals("DatabaseSimulationBean.tables.size", 2, database.getTables().size());

        Table table = database.getTables().get(0);
        assertNotNull("TableSimulationBean.table", table);
        assertEquals("TableSimulationBean.name", "PERFORMANCE_NO_INDEX", table.getName());

        List<Column> cells = table.getColumns();
        assertNotNull("RowSimulationBean.cells", cells);
        assertEquals("CellSimulationInterface.cells.size", 25, cells.size());

        Column cell = cells.get(0);
        assertNotNull("CellSimulationInterface.cell", cell);
        assertEquals("CellSimulationInterface.cell[0].name", "ID", cell.getName());
    }

    @Test
    public void testBasicSimulation() throws Exception {

        Database database = DatabaseMapper.read(Database.class, "etc/model/test/Employee.xml");
        assertNotNull("MetaDatabaseManager.importFile return null", database);

        DatabaseMapper.writeDatabase("build/EmployeeSimulation2.xml", database);

        Database database2 = DatabaseMapper.readDatabase("build/EmployeeSimulation2.xml");
        assertNotNull("generated file does not have database node", database2);
        assertEquals("generated file does not have enough table nodes", database2.getTables().size(), 1);
        assertEquals("generated file does not have enough row nodes", database2.getTables().get(0).getRows().size(), 4);
        // assertEquals("generated file does not have enough cell nodes",
        // doc.getDocumentElement(), "/database/table/row/cell", 4 * 7);
    }

    private void performSimulationInsert(String methodName, String tablename, int iterations, int pageSize) throws Exception {

        System.out.println("STARTING: " + methodName + ", " + tablename + "," + iterations);

        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", dao);

        String filename = "res:/com/viper/test/TestBasicSimulation.xml";
        Database database = DatabaseMapper.read(Database.class, filename);
        assertNotNull("Database not found: " + filename, database);

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);

        SQLDriver driver = new SQLDriver();
        SqlConverter.write(new DatabaseWriter(dao), "mysql", database, table);

        table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
        table.getRows().clear();

        for (int i = 0; i < iterations; i++) {
            // Row row = RandomBean.getRandomRow();
            Row row = null;
            table.getRows().add(row);
            if ((i % pageSize) == (pageSize - 1) || i == (iterations - 1)) {
                driver.insertRows(database, table, table.getRows(), 0, table.getRows().size());
                table.getRows().clear();
            }
        }

        System.out.println("ENDING: " + methodName + ", " + tablename + "," + iterations);
    }

    private void performSimulationQuery(String methodName, String tablename, int iterations) throws Exception {

        System.out.println("STARTING: " + methodName + ", " + tablename + "," + iterations);

        String filename = "res:/com/viper/test/TestBasicSimulation.xml";
        DatabaseInterface writer = DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", writer);

        Database database = DatabaseMapper.read(Database.class, filename);
        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
        if (table == null) {
            throw new Exception("Could not find table " + tablename);
        }

        Column queryColumn = null;
        for (Column column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                queryColumn = column;
            }
        }

        if (queryColumn == null) {
            throw new Exception("Could not find query column " + tablename);
        }

        // List<String> list =
        // FileUtil.readFileViaLines(queryColumn.getListFilename());
        // for (int i = 0; i < iterations; i++) {
        // String value = list.get((i % list.size()));
        // String sql = driver.load(databases, table, queryColumn, value);
        //
        // System.out.println(methodName + ": " + i + ": " + sql);
        // List<Row> rows = writer.readRows(sql);
        // assertTrue("Rows must be greater then 0:" + sql, rows.size() > 0);
        // }
        System.out.println("ENDING: " + methodName + ", " + tablename + "," + iterations);
    }
}
