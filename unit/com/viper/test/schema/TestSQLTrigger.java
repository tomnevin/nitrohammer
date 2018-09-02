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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.JavaNamingMethodType;
import com.viper.database.model.Table;
import com.viper.database.model.Trigger;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLTrigger extends AbstractTestCase {

    private final static String META_FILENAME = "res:/com/viper/test/schema/MetaDatabaseManagerExporter001.xml";
    private final static String tablename = "basic_table";
    private final static String triggername = "trigger01";

    private static final SQLDriver driver = new SQLDriver();

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    protected Databases startup() throws Exception {

        Databases databases = DatabaseMapper.read(Databases.class, META_FILENAME);
        assertNotNull("Database empty for " + META_FILENAME, databases);

        return databases;
    }

    protected void finish() throws Exception {
    }

    @Test
    public void testMetaTrigger01() throws Exception {
        Databases databases = startup();

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        assertEquals("Database name should be test ", "test", database.getName());
        assertEquals("Database charset-name ", "utf8", database.getCharsetName());
        assertEquals("Database collation-name ", "unknown", database.getCollationName());
        assertEquals("Database default-java-naming-method ", JavaNamingMethodType.NOCHANGE,
                database.getDefaultJavaNamingMethod());
        assertEquals("Database filename ", "build/src/com/viper/test.java", database.getFilename());
        assertEquals("Database version ", "3.2", database.getVersion());
        assertEquals("Database should have (x) procedures", 1, database.getProcedures().size());
        assertEquals("Database should have (x) tables ", 2, database.getTables().size());

        Trigger trigger = DatabaseUtil.findOneItem(database.getTriggers(), "name", triggername);
        assertNotNull(triggername + " trigger not found", trigger);

        List<Trigger> list = database.getTriggers();
        assertNotNull("triggers not found", list);

        assertEquals("number of triggers mismatched", 1, list.size());
        finish();
    }

    @Test
    public void testCreateTrigger01() throws Exception {
        Databases databases = startup();

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
        assertNotNull(tablename + " table not found", table);

        Trigger trigger = DatabaseUtil.findOneItem(database.getTriggers(), "name", triggername);
        assertNotNull(triggername + " trigger  not found", trigger);

        String sql = driver.createTrigger(database, trigger);

        assertNotNull("create Trigger", sql);
        assertEquals("create trigger " + triggername + " before delete on test." + tablename
                + " for each row insert table (a, b, c) values (0, 1, 2)", sql.trim());

        finish();
    }

    @Test
    public void testDropTrigger01() throws Exception {
        Databases databases = startup();

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Trigger trigger = DatabaseUtil.findOneItem(database.getTriggers(), "name", triggername);
        assertNotNull(triggername + " trigger  not found", trigger);

        String sql = driver.dropTrigger(database,  trigger);

        assertNotNull("dropTrigger", sql);
        assertEquals("drop trigger test." + triggername, sql.trim());

        finish();
    }
}
