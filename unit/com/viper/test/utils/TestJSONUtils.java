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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.database.dao.converters.Converters;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;
import com.viper.demo.unit.model.Activity;

public class TestJSONUtils extends AbstractTestCase {

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    @BeforeClass
    public static void initializeClass() throws Exception {

        Logger.getGlobal().setLevel(Level.INFO);
    }

    @Test
    public void testJSONMap() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", 25);
        map.put("bar", "Alpha");

        assertEquals("{\"bar\":\"Alpha\",\"foo\":25}", Converters.convert(String.class, map));
    }

    @Test
    public void testJSONArrayGetInt() throws Exception {

        String str = "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]";

        JSONArray favorites = new JSONArray(str);

        assertEquals(getCallerMethodName(), favorites.length(), 10);

        for (int i = 0; i < favorites.length(); i++) {
            assertEquals(getCallerMethodName(), favorites.getInt(i), i);
        }
    }

    @Test
    public void testJSONActivity() throws Exception {

        Activity activity1 = RandomBean.getRandomBean(Activity.class, 123);

        String str = Converters.convert(String.class, activity1);
        assertNotNull(getCallerMethodName(), str);

        Activity activity2 = Converters.convert(Activity.class, str);
        assertBeanEquals(getCallerMethodName(), activity1, activity2);
    }
}
