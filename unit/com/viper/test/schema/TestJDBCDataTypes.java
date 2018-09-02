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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseJDBC;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.utils.FileUtil;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestJDBCDataTypes extends AbstractTestCase {

	private final static String DATABASE_NAME = "test";
    private static final SQLDriver driver = new SQLDriver();
    
    private static DatabaseInterface dao = null;

    @BeforeClass
    public static void setUp() throws Exception {

        Logger.getGlobal().setLevel(Level.FINE);
        Logger.getLogger(DatabaseJDBC.class.getName()).setLevel(Level.FINE);

        dao = DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", dao);

        Databases databases = driver.load((DatabaseSQLInterface)dao, DATABASE_NAME, null);
    }

    @AfterClass
    public static void tearDown() throws Exception {
    }

    // -------------------------------------------------------------------------
    // Simple Generic test - (rewrite too simplistic)
    // -------------------------------------------------------------------------
    @Test
    public void testSimpleValdiation() throws Exception {

        String expectedFilename = "res:/com/viper/test/schema/SchemaBasics001.xml";
        String actualFilename = "build/SchemaBasics001.xml";

        Database database = DatabaseMapper.readDatabase(expectedFilename);
        DatabaseMapper.writeDatabase(actualFilename, database);

        String expectContents = FileUtil.readFile(expectedFilename);
        String actualContents = FileUtil.readFile(actualFilename);

       // assertEqualsDom(expectedFilename + " vs " + actualFilename, expectContents, actualContents);
    }

    // -------------------------------------------------------------------------
    // Test all the defined database types (schema-tool database types)
    // -------------------------------------------------------------------------
    @Test
    public void testDataTypes() throws Exception {

        String databaseName = DatabaseUtil.getDatabaseName(JDBCDataTypesBean.class);
        String tableName = DatabaseUtil.getTableName(JDBCDataTypesBean.class);

        ((DatabaseSQLInterface)dao).write("drop table if exists " + databaseName + "." + tableName);

        dao.create(JDBCDataTypesBean.class);

        dao.insert(new JDBCDataTypesBean());

        JDBCDataTypesBean bean = RandomBean.getRandomBean(JDBCDataTypesBean.class, 101);
        assertNotNull("andomBean.getRandomBean should not be null", bean);

        dao.insert(bean);

        List<JDBCDataTypesBean> beans = dao.queryAll(JDBCDataTypesBean.class);
        assertNotNull("beans should not be null", beans);
        assertEquals("beans should not be size other then 2", 2, beans.size());
    }
}
