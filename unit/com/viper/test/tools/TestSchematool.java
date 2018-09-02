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

package com.viper.test.tools;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.database.CustomXPathFunctions;
import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.utils.JEXLUtil;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSchematool extends AbstractTestCase {

    private final static String DATABASE_NAME = "test";

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    @Test
    public void testSchematoolExport() throws Exception {

        DatabaseSQLInterface writer = (DatabaseSQLInterface)DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("JDBCDriver should not be null", writer);

        SQLDriver driver = new SQLDriver();

        Databases databases = driver.load(writer, null, null);

        String outfile = "build/xml/test/" + DATABASE_NAME + ".xml";
        System.out.println("-export: outfile=" + outfile);

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", DATABASE_NAME);
        DatabaseMapper.writeDatabase(outfile, database);
    }

    @Test
    public void testColumnSize() throws Exception {
        Column column = new Column();
        column.setSize(10L);
        column.setDecimalSize(10);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", new Column());
        map.put("b", column);
        map.put("java", CustomXPathFunctions.class);

        Object value4 = JEXLUtil.getInstance().eval("b.decimalSize", map);
        assertEquals("testColumnSize.4: b.decimalSize", 10, value4);

        Object value3 = JEXLUtil.getInstance().eval("java.toSize(b)", map);
        assertEquals("testColumnSize.3: java.toSize(b)", 10L, value3);

        Object value2 = JEXLUtil.getInstance().eval("not empty(java.toSize(b))", map);
        assertEquals("testColumnSize.2: not empty(java.toSize(b))", true, value2);

        Object value1 = JEXLUtil.getInstance().eval("empty(java.toSize(a))", map);
        assertEquals("testColumnSize.1: empty(java.toSize(a))", true, value1);
    }
}
