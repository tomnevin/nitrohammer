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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.ForeignKey;
import com.viper.database.model.ForeignKeyReference;
import com.viper.database.model.Table;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLForeignKey extends AbstractTestCase {

    private Databases databases = null;
    private static final SQLDriver driver = new SQLDriver();

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    @Before
    public void setUp() throws Exception {

        String META_FILENAME = "res:/com/viper/test/schema/MetaDatabaseManagerExporter001.xml";

        databases = DatabaseMapper.read(Databases.class, META_FILENAME);
        assertNotNull("Database not created from " + META_FILENAME, databases);
    }

    @Test
    public void testCreateForeignKey01() throws Exception {

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table table not found", table);

        ForeignKey foreignKey = DatabaseUtil.findOneItem(table.getForeignKeys(), "name", "local-key-name");
        assertNotNull("local-key-name foreignKey not found", foreignKey);

        List<ForeignKeyReference> references = foreignKey.getForeignKeyReferences();
        assertNotNull("ForeignKeyReference not found", references);

        String sql = driver.createForeignKey(database, table, foreignKey);

        assertNotNull("createForeignKey", sql);
        assertEquals(
                "createForeignKey",
                "alter table test.basic_table add constraint constrain0002 foreign key (`local-column-name`) references `foreign-catalog`.`foreign-table` (`foreign-column`)",
                sql.trim());
    }

    @Test
    public void testDropForeignKey01() throws Exception {

        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table table not found", table);

        ForeignKey foreignKey = DatabaseUtil.findOneItem(table.getForeignKeys(), "name", "local-key-name");
        assertNotNull("local-key-name foreignKey not found", foreignKey);

        List<ForeignKeyReference> references = foreignKey.getForeignKeyReferences();
        assertNotNull("ForeignKeyReference not found", references);

        String sql = driver.dropForeignKey(database, table, foreignKey);

        assertNotNull("dropForeignKey", sql);
        assertEquals("dropForeignKey", "alter table test.basic_table drop foreign key constrain0002", sql.trim());
    }
}
