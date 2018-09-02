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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.demo.beans.model.Bean;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;
import com.viper.demo.unit.model.Activity;
import com.viper.demo.unit.model.Civilization;
import com.viper.demo.unit.model.Employee;
import com.viper.demo.unit.model.LogicalBean;
import com.viper.demo.unit.model.Types;
import com.viper.demo.unit.model.User;
   
public class TestRandomBean extends AbstractTestCase {

    @Rule
    public MethodRule benchmarkRule = new BenchmarkRule();

    private final static String DELIMITERS = "\\s*[<>,]\\s*";

    @BeforeClass
    public static void initializeClass() throws Exception {

        Logger.getGlobal().setLevel(Level.INFO);
    }

    @Test
    public void testSplit1() throws Exception {

        String str = "List<String>";
        assertEquals(getCallerMethodName() + " split(<>,) => " + str, 2, str.split(DELIMITERS).length);
    }

    @Test
    public void testSplit2() throws Exception {

        String str = "List<String, Object>";
        assertEquals(getCallerMethodName() + " split(<>,) => " + str, 3, str.split(DELIMITERS).length);
    }

    @Test
    public void testParseTypes1() throws Exception {

        String type = "List<String, Object>";
        String tokens[] = RandomBean.parseTypes(type);
        assertEquals(getCallerMethodName() + " split(<>,) => " + type, 3, tokens.length);
        assertEquals(getCallerMethodName() + ", " + type, "List", tokens[0]);
        assertEquals(getCallerMethodName() + ", " + type, "String", tokens[1]);
        assertEquals(getCallerMethodName() + ", " + type, "Object", tokens[2]);
    }

    @Test
    public void testParseTypes2() throws Exception {

        String type = "List < String , Object >";
        String tokens[] = RandomBean.parseTypes(type);
        assertEquals(getCallerMethodName() + " split(<>,) => " + type, 3, tokens.length);
        assertEquals(getCallerMethodName() + ", " + type, "List", tokens[0]);
        assertEquals(getCallerMethodName() + ", " + type, "String", tokens[1]);
        assertEquals(getCallerMethodName() + ", " + type, "Object", tokens[2]);
    }

    @Test
    public void testRandomLatLon1() throws Exception {

        String[] str = new String[] { "latlon", "45.0N 35.0W 10.0 10.0" };
        List<String> results = RandomBean.randomLatLon(str);

        assertNotNull(getCallerMethodName() + " latlon => " + str + ", " + results.get(0), results.get(0));
        assertNotNull(getCallerMethodName() + " latlon => " + str + ", " + results.get(1), results.get(1));
    }
    
    @Test
    public void testRandomActivity() throws Exception {

        Activity bean = RandomBean.getRandomBean(Activity.class, 100);

        BeanInfo info = Introspector.getBeanInfo(Activity.class);
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();

            Object actual = readMethod.invoke(bean);
            assertNotNull(getCallerMethodName() + " (" + propertyDescriptor.getName() + ") " + actual, actual);
        }
    }
    
    @Test
    public void testRandomCivilization() throws Exception {

        Civilization bean = RandomBean.getRandomBean(Civilization.class, 100);

        BeanInfo info = Introspector.getBeanInfo(Civilization.class);
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();

            Object actual = readMethod.invoke(bean);
            assertNotNull(getCallerMethodName() + " (" + propertyDescriptor.getName() + ") " + actual, actual);
        }
    }
    
    @Test
    public void testRandomEmployee() throws Exception {

        Employee employee = RandomBean.getRandomBean(Employee.class, 100);

        BeanInfo info = Introspector.getBeanInfo(Employee.class);
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();

            Object actual = readMethod.invoke(employee);
            assertNotNull(getCallerMethodName() + " (" + propertyDescriptor.getName() + ") " + actual, actual);
        }
    }
    
    @Test
    public void testLogicalBean() throws Exception {

        LogicalBean bean = RandomBean.getRandomBean(LogicalBean.class, 1);

        BeanInfo info = Introspector.getBeanInfo(LogicalBean.class);
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();

            Object actual = readMethod.invoke(bean);
            assertNotNull(getCallerMethodName() + " (" + propertyDescriptor.getName() + ") " + actual, actual);
        }
    }
    
    @Test
    public void testTypes() throws Exception {

        Types bean = RandomBean.getRandomBean(Types.class, 1);

        BeanInfo info = Introspector.getBeanInfo(Types.class);
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();

            Object actual = readMethod.invoke(bean);
            assertNotNull(getCallerMethodName() + " (" + propertyDescriptor.getName() + ") " + actual, actual);
        }
    }
    
    @Test
    public void testUser() throws Exception {

        User bean = RandomBean.getRandomBean(User.class, 1);

        BeanInfo info = Introspector.getBeanInfo(User.class);
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();

            Object actual = readMethod.invoke(bean);
            assertNotNull(getCallerMethodName() + " (" + propertyDescriptor.getName() + ") " + actual, actual);
        }
    }
    
    @Test
    public void testBean() throws Exception {

        Bean bean = RandomBean.getRandomBean(Bean.class, 1);

        BeanInfo info = Introspector.getBeanInfo(Bean.class);
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();

            Object actual = readMethod.invoke(bean);
            assertNotNull(getCallerMethodName() + " (" + propertyDescriptor.getName() + ") " + actual, actual);
        }
    }
}