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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;
import com.viper.demo.beans.model.enums.MyColor;

public class TestUtils extends AbstractTestCase {

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    @BeforeClass
    public static void initializeClass() throws Exception {

        Logger.getGlobal().setLevel(Level.INFO);
    }

    @Test
    public void isEquals() throws Exception {

        assertFalse(getCallerMethodName() + " long vs Long", long.class == java.lang.Long.class);
        assertFalse(getCallerMethodName() + " Long vs long", Long.class == long.class);
    }

    @Test
    public void isAssignableFromLong() throws Exception {

        assertFalse(getCallerMethodName() + " long vs Long", long.class.isAssignableFrom(java.lang.Long.class));
    }

    @Test
    public void isAssignableFromlong() throws Exception {

        assertFalse(getCallerMethodName() + " Long vs long", Long.class.isAssignableFrom(long.class));
    }

    @Test
    public void isAssignableFromArrayList() throws Exception {

        assertTrue(getCallerMethodName() + " List vs ArrayList ", List.class.isAssignableFrom(ArrayList.class));
    }

    @Test
    public void testInstanceOf() throws Exception {

        long a = 1L;
        Long b = 0L;
        assertTrue(getCallerMethodName() + " long instanceof Long", (b instanceof Long));

    }

    @Test
    public void testArrayList() throws Exception {

        List list = new ArrayList();
        assertTrue(getCallerMethodName() + " List ", List.class.isInstance(list));
        assertTrue(getCallerMethodName() + " ArrayList ", ArrayList.class.isInstance(list));

    }

    @Test
    public void testJavaNullEnum() throws Exception {

    	MyColor mycolor = null;
        assertNull(getCallerMethodName() + " MyColor ", mycolor);

    }
}
