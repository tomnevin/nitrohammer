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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.utils.DatabaseRegistry;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestDatabaseFactory extends AbstractTestCase {

    @BeforeClass
    public static void initializeClass() throws Exception {

        Logger.getGlobal().setLevel(Level.INFO);

        DatabaseRegistry.getInstance();
    }

    @Test
    public void testOpenFactory1() throws Exception {

        utilOpenFactory(getCallerMethodName(), 1);
    }

    @Test
    public void testOpenFactory1000() throws Exception {

        utilOpenFactory(getCallerMethodName(), 1000);
    }

    @Test
    public void testOpenFactory10000() throws Exception {

        utilOpenFactory(getCallerMethodName(), 10000);
    }

    private void utilOpenFactory(String msg, int iterations) throws Exception {

        DatabaseFactory.releaseAll();

        for (int i = 0; i < iterations; i++) {
            DatabaseInterface dao = DatabaseFactory.getInstance("test");

            Assert.assertNotNull(msg, dao);
        }
    }
}