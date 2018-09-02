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
import com.viper.database.model.Database;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLDatabase extends AbstractTestCase {

    private static final SQLDriver driver = new SQLDriver();

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    @Test
    public void testCreateDatabase() throws Exception {

        Database database = new Database();
        database.setName("DatabaseName");

        String sql = driver.createDatabase(database);

        assertNotNull("createDatabase", sql);
        assertEquals("createDatabase", "create database if not exists DatabaseName", sql.trim());
    }

    @Test
    public void testCreateDatabase02() throws Exception {

        Database database = new Database();
        database.setName("DatabaseName02");
        database.setCharsetName("latin1");
        database.setCollationName("latin1_german1_c");

        String sql = driver.createDatabase(database);

        assertNotNull("createDatabase", sql);
        assertEquals("createDatabase",
                "create database if not exists DatabaseName02 CHARACTER SET 'latin1' COLLATE 'latin1_german1_c'", sql.trim());
    }

    @Test
    public void testCreateDatabase03() throws Exception {

        Database database = new Database();
        database.setName("DatabaseName03");
        database.setCollationName("latin1_german1_c");

        String sql = driver.createDatabase(database);

        assertNotNull("createDatabase", sql);
        assertEquals("createDatabase", "create database if not exists DatabaseName03 COLLATE 'latin1_german1_c'", sql.trim());
    }

    @Test
    public void testDropDatabase01() throws Exception {

        Database database = new Database();
        database.setName("DatabaseName");
        database.setCollationName("latin1_german1_c");

        String sql = driver.dropDatabase(database);

        assertEquals("dropDatabase", "drop database if exists DatabaseName", sql.trim());
    }
}