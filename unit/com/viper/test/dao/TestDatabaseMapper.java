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

package com.viper.test.dao;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

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
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestDatabaseMapper extends AbstractTestCase {

    private final static String DATABASE_NAME = "test-mapper";
    private final static String DRIVER_NAME = "mysql";

    @Rule
    public TestRule benchmarkRule = new BenchmarkRule();

    @BeforeClass
    public static void initializeClass() throws Exception {
        Logger.getGlobal().setLevel(Level.INFO);
    }

    @Test
    public void testImportTable() throws Exception {

        Database database = new Database();
        // TODO fill in database object.

        DatabaseInterface dao = DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", dao);

        DatabaseMapper.importTable(dao, database);

        // TODO Check Table in database

        dao.release();
    }

    @Test
    public void testReadWriteConnections1() throws Exception {

        DatabaseConnections dbc1 = DatabaseMapper.readConnections("res:/databases.xml");
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc1);
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc1.getConnections());
        assertTrue("Databases.xml: connections size: " + dbc1.getConnections().size(),
                dbc1.getConnections().size() > 0);

        DatabaseMapper.writeConnections("./build/databases-test.xml", dbc1);

        DatabaseConnections dbc2 = DatabaseMapper.readConnections("./build/databases-test.xml");
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc2);
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc2.getConnections());
        assertTrue("Databases.xml: connections size: " + dbc2.getConnections().size(),
                dbc2.getConnections().size() > 0);

        assertEquals("Databases.xml: dbc1 and dbc2 connections size: ", dbc1.getConnections().size(),
                dbc2.getConnections().size());
    }

    @Test
    public void testWriteReadConnections1() throws Exception {

        String filename = "./build/databases-test2.xml";
        DatabaseConnections dbc1 = new DatabaseConnections();

        DatabaseConnection dbc1A = new DatabaseConnection();
        dbc1A.setName("TestName1");
        dbc1.getConnections().add(dbc1A);

        DatabaseMapper.writeConnections(filename, dbc1);

        DatabaseConnections dbc2 = DatabaseMapper.readConnections(filename);
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc2);
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc2.getConnections());
        assertTrue("Databases.xml: connections size: " + dbc2.getConnections().size(),
                dbc2.getConnections().size() > 0);

        assertEquals("Databases.xml: dbc1 and dbc2 connections size: ", dbc1.getConnections().size(),
                dbc2.getConnections().size());
    }

    @Test
    public void testReadWriteDatabase1() throws Exception {

        Database database1 = DatabaseMapper.readDatabase("./etc/model/beans/bean.xml");
        assertNotNull(getCallerMethodName() + " : database1 were not found ", database1);
        assertEquals("Databases.xml: database1 size: " + database1.getTables().size(), 1, database1.getTables().size());

        DatabaseMapper.writeDatabase("./build/bean-test.xml", database1);

        Database database2 = DatabaseMapper.readDatabase("./build/bean-test.xml");
        assertNotNull(getCallerMethodName() + " : connections were not found ", database2);
        assertEquals("Databases.xml: connections size: " + database2.getTables().size(), 1,
                database2.getTables().size());

        assertEquals("Databases.xml: dbc1 and dbc2 connections size: ", database1.getTables().size(),
                database2.getTables().size());
    }

    @Test
    public void testReadWriteGeneric1() throws Exception {

        DatabaseConnections dbc1 = DatabaseMapper.read(DatabaseConnections.class, "res:/databases.xml");
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc1);
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc1.getConnections());
        assertTrue("Databases.xml: connections size: " + dbc1.getConnections().size(),
                dbc1.getConnections().size() > 0);

        DatabaseMapper.write("./build/databases-test.xml", dbc1, null);

        DatabaseConnections dbc2 = DatabaseMapper.read(DatabaseConnections.class, "./build/databases-test.xml");
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc2);
        assertNotNull(getCallerMethodName() + " : connections were not found ", dbc2.getConnections());
        assertTrue("Databases.xml: connections size: " + dbc2.getConnections().size(),
                dbc2.getConnections().size() > 0);

        assertEquals("Databases.xml: dbc1 and dbc2 connections size: ", dbc1.getConnections().size(),
                dbc2.getConnections().size());
    }

    @Test
    public void testFindConnection1() throws Exception {

        String name = "mysql";
        DatabaseConnections connections = DatabaseMapper.readConnections("res:/databases.xml");
        System.err.println("Databases.xml: connections size: " + connections.getConnections().size() + ":" + name);

        DatabaseConnection connection = DatabaseUtil.findOneItem(connections.getConnections(), "name", name);

        assertNotNull(getCallerMethodName() + " : connections were not found ", connections.getConnections());
        assertTrue(getCallerMethodName() + " : not enought connections were not found ",
                connections.getConnections().size() > 0);
        assertNotNull(getCallerMethodName() + " : connection was not found ", connection);
    }

    @Test
    public void testFindConnection2() throws Exception {

        String name = "test";
        DatabaseConnections connections = DatabaseMapper.readConnections("res:/databases.xml");
        System.err.println("Databases.xml: connections size: " + connections.getConnections().size() + ":" + name);

        assertNotNull(getCallerMethodName() + " : connections were not found ", connections.getConnections());
        assertTrue(getCallerMethodName() + " : not enought connections were not found ",
                connections.getConnections().size() > 0);

        for (DatabaseConnection c : connections.getConnections()) {
            System.err.println("testFindConnection2: " + c.getName());
        }

        DatabaseConnection connection = DatabaseUtil.findOneItem(connections.getConnections(), "name", name);

        assertNotNull(getCallerMethodName() + " : connection was not found ", connection);
    }

    @Test
    public void testMetaTable() throws Exception {

        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", dao);

        SQLDriver driver = new SQLDriver();
        assertNotNull("Driver should not be null: " + DRIVER_NAME, driver);

        Databases databases1 = DatabaseMapper.readDatabases("res:/com/viper/test/dao/TestMetaConverter.xml");
        SqlConverter.write(new DatabaseWriter(dao), DRIVER_NAME, databases1);

        Databases databases2 = driver.load(dao, "meta", null);
        assertNotNull(getCallerMethodName() + ", databases2 should not be null", databases2);
        assertNotNull(getCallerMethodName() + ", databases1 should not be null", databases1);
        assertEquals(getCallerMethodName() + ", wrong number databases", databases1.getDatabases().size(),
                databases2.getDatabases().size());

        DatabaseMapper.writeDatabases("./build/TestMetaConverter.xml", databases2);

        Databases expected = DatabaseMapper.readDatabases("res:/com/viper/test/dao/TestMetaConverter.xml");
        Databases actual = DatabaseMapper.readDatabases("./build/TestMetaConverter.xml");

        assertNotNull(getCallerMethodName() + ", expected should not be null", expected);
        assertNotNull(getCallerMethodName() + ", actual should not be null", actual);
        assertEquals(getCallerMethodName() + ", wrong number databases", expected.getDatabases().size(),
                actual.getDatabases().size());

        ((DatabaseInterface) dao).release();
    }

    @Test
    public void testReadConnectionPerf1() throws Exception {

        utilReadConnectionPerf(getCallerMethodName(), "res:/databases.xml", 1);
    }

    @Test
    public void testReadConnectionPerf1000() throws Exception {

        utilReadConnectionPerf(getCallerMethodName(), "res:/databases.xml", 1000);
    }

    @Test
    public void testReadConnectionPerf10000() throws Exception {

        utilReadConnectionPerf(getCallerMethodName(), "res:/databases.xml", 10000);
    }

    private void utilReadConnectionPerf(String msg, String filename, int iterations) throws Exception {

        DatabaseFactory.releaseAll();
        
        for (int i = 0; i < iterations; i++) {
            DatabaseConnections connections = DatabaseMapper.readConnections(filename);

            assertNotNull(msg, connections);
            assertNotNull(msg, connections.getConnections());
            assertTrue(msg, connections.getConnections().size() > 0);
        }
    }
}