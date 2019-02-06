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

package com.viper.database.utils.junit;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;

import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import com.viper.database.annotations.Column;
import com.viper.database.dao.DatabaseUtil;

@BenchmarkMethodChart()
@BenchmarkHistoryChart(labelWith = LabelType.RUN_ID, maxRuns = 100)
public class AbstractTestCase extends Assert {

    public static double PRECISION = 0.0001;

    public static String getCallerMethodName() throws Exception {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    // --------------------------------------------------------------------------
    // Used to compare objects in JUnit testing
    // --------------------------------------------------------------------------
    public static void assertEqualProperties(Object bean1, Object bean2) throws Exception {
        Method methods[] = bean1.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.startsWith("get")) {
                Object value1 = methods[i].invoke(bean1);
                Object value2 = methods[i].invoke(bean2);
                assertEquals(methodName, value1, value2);
            } else if (methodName.startsWith("is")) {
                Object value1 = methods[i].invoke(bean1);
                Object value2 = methods[i].invoke(bean2);
                assertEquals(methodName, value1, value2);
            }
        }
    }

    public void assertNotEmpty(String message, String str) {
        assertNotNull(message, str);
        assertTrue(message, str.length() > 0);
    }

    public static void assertEqualsIgnoreCase(String expected, String actual) {
        assertEqualsIgnoreCase("", expected, actual);
    }

    public static void assertEqualsIgnoreCase(String message, String expected, String actual) {
        assertEquals(message, expected.toLowerCase(), actual.toLowerCase());
    }

    public static void assertEqualsSorta(String expected, String actual) {
        try {
            Assert.assertEquals(escape(clean(expected)), escape(clean(actual)));
        } catch (Error e) {
            System.out.println("expect: " + clean(expected));
            System.out.println("actual: " + clean(actual));
            throw e;
        }
    }

    public static void assertEqualsSorta(String message, String expected, String actual) {
        try {
            Assert.assertEquals(message, escape(clean(expected)), escape(clean(actual)));
        } catch (Error e) {
            System.out.println(message);
            System.out.println("expect: " + clean(expected));
            System.out.println("actual: " + clean(actual));
            throw e;
        }
    }

    public static void assertEqualsIgnoreWhiteSpace(String message, String expected, String actual) {
        try {
            Assert.assertEquals(message, escape(clean(expected)), escape(clean(actual)));
        } catch (Error e) {
            System.out.println(message);
            System.out.println("expect: " + clean(expected));
            System.out.println("actual: " + clean(actual));
            throw e;
        }
    }

    /**
     * Asserts that two Strings are equal.
     * 
     * @param message
     * @param expected
     * @param actual
     */
    public static void assertEquals(String message, String expected, String actual) {
        try {
            Assert.assertEquals(message, expected, actual);
        } catch (Error e) {
            System.out.println(message);
            System.out.println("expect: " + expected);
            System.out.println("actual: " + actual);
            throw e;
        }
    }

    public static void assertEquals(String expected, String actual) {
        try {
            Assert.assertEquals(expected, actual);
        } catch (Error e) {
            System.out.println("expect: " + expected);
            System.out.println("actual: " + actual);
            throw e;
        }
    }

    // -------------------------------------------------------------------------

    public static void assertBetween(String message, int minimum, int maximum, int actual) {
        try {
            Assert.assertTrue(actual >= minimum && actual <= maximum);
        } catch (Error e) {
            System.out.println(message + " was " + actual + " but was expected to be between " + minimum + " and " + maximum);
            throw e;
        }
    }

    public static void assertBetween(String message, long minimum, long maximum, long actual) {
        try {
            Assert.assertTrue(actual >= minimum && actual <= maximum);
        } catch (Error e) {
            System.out.println(message + " was " + actual + " but was expected to be between " + minimum + " and " + maximum);
            throw e;
        }
    }

    public static void assertBetween(String message, float minimum, float maximum, float actual) {
        try {
            Assert.assertTrue(actual >= minimum && actual <= maximum);
        } catch (Error e) {
            System.out.println(message + " was " + actual + " but was expected to be between " + minimum + " and " + maximum);
            throw e;
        }
    }

    public static void assertBetween(String message, double minimum, double maximum, double actual) {
        try {
            Assert.assertTrue(actual >= minimum && actual <= maximum);
        } catch (Error e) {
            System.out.println(message + " was " + actual + " but was expected to be between " + minimum + " and " + maximum);
            throw e;
        }
    }

    public static String escape(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String pretty(String str) {
        return str.replace('\t', ' ').replace(">", ">\n");
    }

    public static String clean(String str) {
        return removeExtraWhiteSpace(str);
    }

    public static String removeWhiteSpace(String str) {
        return (str == null) ? null : str.replaceAll("\\s+", "");
    }

    public static String removeExtraWhiteSpace(String str) {
        return (str == null) ? null : str.replaceAll("\\s+", " ");
    }

    public static String removeLines(String str) {
        if (str == null) {
            return null;
        }
        return str.trim().replaceAll("\\s*[\\n\\r]", "").replaceAll("[\\n\\r]", "");
    }

    public static <T> void assertBeanEquals(String msg, T expected, T actual) throws Exception {
        assertNotNull(msg + ", expected is null", expected);
        assertNotNull(msg + ", actual is null", actual);

        BeanInfo info = Introspector.getBeanInfo(expected.getClass());
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();

            Object e = readMethod.invoke(expected);
            Object a = readMethod.invoke(actual);

            Column columnA = readMethod.getAnnotation(Column.class);
            double precision = PRECISION;
            if (columnA != null) {
                precision = 1.0 / Math.pow(10, columnA.decimalSize());
            }

            if (columnA != null && !columnA.persistent()) {
                continue;
            }

            if (columnA != null && !columnA.optional()) {
                continue;
            }

            assertTrue(msg + " (" + propertyDescriptor.getName() + ") [" + e + "] vs [ " + a + "] " + precision,
                    equals(e, a, precision));
        }
    }

    public static <T> void assertBeansEquals(String msg, List<T> expected, List<T> actual) throws Exception {
        assertNotNull(msg + ", expected is null", expected);
        assertNotNull(msg + ", actual is null", actual);
        assertEquals(msg + ", sizes not equal", expected.size(), actual.size());

    }

    public static <T> boolean equals(T bean1, T bean2, double precision) {
        if (bean1 == null && bean2 == null) {
            return true;
        }
        if (bean1 == null || bean2 == null) {
            return false;
        }
        if (Double.class.isInstance(bean1)) {
            return equals((Double) bean1, (Double) bean2, precision);
        }
        if (Float.class.isInstance(bean1)) {
            return equals((Float) bean1, (Float) bean2, (float) precision);
        }
        if (BigDecimal.class.isInstance(bean1)) {
            return equals((BigDecimal) bean1, (BigDecimal) bean2, precision);
        }
        if (bean1.getClass().isPrimitive() || bean1.getClass().equals(String.class)) {
            return bean1.equals(bean2);

        } else if (Date.class.isInstance(bean1) && Date.class.isInstance(bean2)) {
            return equals((Date) bean1, (Date) bean2);
        }

        String[] excludedFields = new String[0];
        if (DatabaseUtil.isTableClass(bean1.getClass())) {
            excludedFields = new String[] { DatabaseUtil.getPrimaryKeyName(bean1.getClass()) };
        }
        return EqualsBuilder.reflectionEquals(bean1, bean2, false, bean1.getClass(), excludedFields);
    }

    public static <T> boolean contains(List<T> beans, T bean, double precision) {
        for (T b : beans) {
            if (equals(b, bean, precision)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equals(Double v1, Double v2, double precision) {
        System.err.println("Equals-Double: " + v1 + ", " + v2 + ", " + precision);
        return (Math.abs(v2 - v1) < precision);
    }

    public static boolean equals(BigDecimal v1, BigDecimal v2, double precision) {
        System.err.println("Equals-BigDecimal: " + v1 + ", " + v2 + ", " + precision);
        return (Math.abs(v2.doubleValue() - v1.doubleValue()) < precision);
    }

    public static boolean equals(Float v1, Float v2, float precision) {
        return (Math.abs(v2 - v1) < precision);
    }

    public static boolean equals(Date v1, Date v2) {
        long precision = 24 * 3600 * 1000;
        System.err.println("Equals-Date: " + v1.getTime() + ", " + v2.getTime() + ", " + precision);
        return Math.abs(v1.getTime() - v2.getTime()) < precision;
    }

    public static <T> void assertContains(String msg, List<T> expected, T actual) throws Exception {
        assertNotNull(msg + ", expected is null", expected);
        assertNotNull(msg + ", actual is null", actual);
        assertTrue(msg + ", cant find item: " + actual.toString(), contains(expected, actual, 0.0));
    }

    public static <T> void assertContains(String msg, List<T> expected, List<T> actual) throws Exception {
        assertNotNull(msg + ", expected is null", expected);
        assertNotNull(msg + ", actual is null", actual);
        for (T a : actual) {
            assertContains(msg + ", ( list= " + expected + "), (actual=" + a + ")", expected, a);
        }
    }

    public static <T> void assertBeanContained(String msg, List<T> expected, T actual) throws Exception {
        assertNotNull(msg + ", expected is null", expected);
        assertNotNull(msg + ", actual is null", actual);
        assertContains(msg + "(" + actual + ")", expected, actual);
    }

    public static <T> void assertBeansContained(String msg, List<T> expected, List<T> actual) throws Exception {
        assertNotEmpty(msg + ", expected is null", expected);
        assertNotEmpty(msg + ", actual is null", actual);
        for (T e : expected) {
            assertContains(msg + "(" + e + ")", actual, e);
        }
    }

    public static <T> void assertNotEmpty(String msg, Collection<T> actual) throws Exception {
        assertNotNull(msg + ", is null", actual);
        assertTrue(msg + ", size must be greater then zero " + actual.size(), actual.size() >= 1);
    }

    public static <T> void assertNotEmpty(String msg, Collection<T> actual, int minSize) throws Exception {
        assertNotNull(msg + ", is null", actual);
        assertTrue(msg + ", size " + actual.size() + " vs " + minSize, actual.size() >= minSize);
    }

    public static void assertEquals(String msg, BigDecimal v1, BigDecimal v2, double precision) {
        assertTrue(msg + ":" + v1 + " vs " + v2, equals(v1, v2, precision));
    }
}
