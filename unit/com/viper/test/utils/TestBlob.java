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

package com.viper.test.utils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.viper.demo.beans.model.Bean2;
import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.utils.DatabaseRegistry;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
   
public class TestBlob extends AbstractTestCase {
    
    private final static String DatabaseInstanceName = System.getProperty("DATABASE_LOCATOR", "test");

    @BeforeClass
    public static void initializeClass() throws Exception {

        Logger.getGlobal().setLevel(Level.INFO);

        DatabaseRegistry.getInstance();
    }

    private DatabaseInterface createDatabase() throws Exception {
        // connection.setPackageName("com.viper.primefaces.model");
        DatabaseInterface database = DatabaseFactory.getInstance(DatabaseInstanceName);
        if (!DatabaseUtil.isDatabaseExist(database, "test")) {
            database.createDatabase("test");
        }
        if (!DatabaseUtil.isTableExist(database, "test", "bean2")) {
            database.create(Bean2.class);
        }
        return database;
    }

    @Test
    public void testCreate() throws Exception {

        Bean2 expected = RandomBean.getRandomBean(Bean2.class, 101);
        expected.setId(0);

        DatabaseInterface database = createDatabase();
        database.insert(expected);

        Assert.assertTrue("testCreate - the organization id not set: " + expected.getId(), expected.getId() > 0);
    }

    @Test
    public void testQuery() throws Exception {

        List<Bean2> organizations = RandomBean.getRandomBeans(Bean2.class, 100, 104);

        DatabaseInterface database = createDatabase();
        database.insertAll(organizations);

        Bean2 expected = organizations.get(50);
        Bean2 actual = database.query(Bean2.class, "id", expected.getId());
        
        Assert.assertNotNull("testQuery - could not find Bean2", actual);
        Assert.assertEquals("testQuery - could not find Bean2", expected.getId(), actual.getId());
        Assert.assertNotNull("testQuery - expected.dataBytes", expected.getDataBytes());
        Assert.assertNotNull("testQuery - actual.dataBytes", actual.getDataBytes());
        Assert.assertEquals("testQuery - actual.dataBytes.length", actual.getDataBytes().length, expected.getDataBytes().length);
        Assert.assertTrue("testQuery - actual.dataBytes.length:" + actual.getDataBytes().length, actual.getDataBytes().length > 0);
    }
}