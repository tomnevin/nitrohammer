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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import com.viper.database.dao.converters.Converters;
import com.viper.database.dao.converters.NumberConverter;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.demo.beans.model.enums.MyColor;
import com.viper.demo.beans.model.enums.NamingField;
import com.viper.demo.unit.model.Activity;
import com.viper.demo.unit.model.Employee;

public class TestConverters extends AbstractTestCase {

    private static final double DECIMAL_TOLERANCE = 0.0000001;

    public enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    }

    // Is Assignable
    @Test
    public void testIsAssignable() throws Exception {

        assertFalse("testByteIsAssignable1", Byte.class.isAssignableFrom(byte.class));
        assertFalse("testByteIsAssignable2", byte.class.isAssignableFrom(Byte.class));

        assertFalse("testDateIsAssignable1", java.sql.Date.class.isAssignableFrom(java.util.Date.class));
        assertFalse("testTimeIsAssignable1", java.sql.Time.class.isAssignableFrom(java.util.Date.class));
        assertFalse("testTimestampIsAssignable1", java.sql.Timestamp.class.isAssignableFrom(java.util.Date.class));

        assertTrue("testDateIsAssignable2", java.util.Date.class.isAssignableFrom(java.sql.Date.class));
        assertTrue("testTimeIsAssignable2", java.util.Date.class.isAssignableFrom(java.sql.Time.class));
        assertTrue("testTimestampIsAssignable2", java.util.Date.class.isAssignableFrom(java.sql.Timestamp.class));

        byte bytes[] = RandomBean.randomBytes(30, 200);
        Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);
        assertFalse("testIsInstance1", blob.getClass().isInstance(Blob.class));
        assertFalse("testIsInstance2", Blob.class.isInstance(blob.getClass()));
        assertTrue("testIsInstance2", Blob.class.isAssignableFrom(blob.getClass()));
    }

    @Test
    public void testLookupConverter() throws Exception {

        assertNotNull(getCallerMethodName(), Converters.lookup(String.class, (int) 1));
        assertNotNull(getCallerMethodName(), Converters.lookup(Double.class, (int) 1));
        assertNotNull(getCallerMethodName(), Converters.lookup(Long.class, (int) 1));
    }

    // Primitives

    @Test
    public void testBigDecimalConverter() throws Exception {

        BigDecimal bigDecimal = BigDecimal.valueOf(10.0);
        assertEquals(getCallerMethodName(), "10.0", Converters.convert(String.class, bigDecimal));
        assertEquals(getCallerMethodName(), bigDecimal, Converters.convert(BigDecimal.class, "10.0"), DECIMAL_TOLERANCE);
        assertEquals(getCallerMethodName(), bigDecimal, Converters.convert(BigDecimal.class, 10.0), DECIMAL_TOLERANCE);
        assertEquals(getCallerMethodName(), bigDecimal, Converters.convert(BigDecimal.class, 10L), DECIMAL_TOLERANCE);
    }

    @Test
    public void testNumberToBigIntegerConverter() throws Exception {

        BigInteger bigInteger = BigInteger.valueOf(10);
        assertEquals(getCallerMethodName(), "10", Converters.convert(String.class, bigInteger));
        assertEquals(getCallerMethodName(), bigInteger, Converters.convert(BigInteger.class, "10"));
        assertEquals(getCallerMethodName(), bigInteger, Converters.convert(BigInteger.class, 10.0));
        assertEquals(getCallerMethodName(), bigInteger, Converters.convert(BigInteger.class, 10L));
    }

    @Test
    public void tesBigIntegerToNumberConverter() throws Exception {

        assertEquals(getCallerMethodName(), "10", Converters.convert(String.class, BigInteger.valueOf(10)));
        assertEquals(getCallerMethodName(), 10.0, (double) Converters.convert(double.class, BigInteger.valueOf(10)), 0.0);
        assertEquals(getCallerMethodName(), 10.0F, (float) Converters.convert(float.class, BigInteger.valueOf(10)), 0.0);
        assertEquals(getCallerMethodName(), 10L, (long) Converters.convert(long.class, BigInteger.valueOf(10)));
        assertEquals(getCallerMethodName(), 10, (int) Converters.convert(int.class, BigInteger.valueOf(10)));
        assertEquals(getCallerMethodName(), (short) 10, (short) Converters.convert(short.class, BigInteger.valueOf(10)));
    }

    @Test
    public void testBooleanConverter() throws Exception {

        assertEquals(getCallerMethodName(), "true", Converters.convert(String.class, true));
        assertEquals(getCallerMethodName(), "false", Converters.convert(String.class, false));
        assertEquals(getCallerMethodName(), 1, (int) Converters.convert(int.class, true));
        assertEquals(getCallerMethodName(), 0, (int) Converters.convert(int.class, false));
        assertEquals(getCallerMethodName(), 1L, (long) Converters.convert(long.class, true));
        assertEquals(getCallerMethodName(), 0L, (long) Converters.convert(long.class, false));
        assertEquals(getCallerMethodName(), (short) 1, (short) Converters.convert(short.class, true));
        assertEquals(getCallerMethodName(), (short) 0, (short) Converters.convert(short.class, false));
        assertEquals(getCallerMethodName(), 1.0, (double) Converters.convert(double.class, true), 0.0);
        assertEquals(getCallerMethodName(), 0.0, (double) Converters.convert(double.class, false), 0.0);
        assertEquals(getCallerMethodName(), 1.0F, (float) Converters.convert(float.class, true), 0.0);
        assertEquals(getCallerMethodName(), 0.0F, (float) Converters.convert(float.class, false), 0.0);
        assertEquals(getCallerMethodName(), (byte) 1, (byte) Converters.convert(byte.class, true), 0.0);
        assertEquals(getCallerMethodName(), (byte) 0, (byte) Converters.convert(byte.class, false), 0.0);

        assertEquals(getCallerMethodName(), false, Converters.convert(boolean.class, "false"));
        assertEquals(getCallerMethodName(), true, Converters.convert(boolean.class, "true"));
        assertEquals(getCallerMethodName(), false, Converters.convert(boolean.class, 0));
        assertEquals(getCallerMethodName(), true, Converters.convert(boolean.class, 1));
        assertEquals(getCallerMethodName(), false, Converters.convert(boolean.class, 0));
        assertEquals(getCallerMethodName(), true, Converters.convert(boolean.class, 1L));
        assertEquals(getCallerMethodName(), false, Converters.convert(boolean.class, 0L));
        assertEquals(getCallerMethodName(), true, Converters.convert(boolean.class, (short) 1));
        assertEquals(getCallerMethodName(), false, Converters.convert(boolean.class, (short) 0));
        assertEquals(getCallerMethodName(), true, Converters.convert(boolean.class, 1.0));
        assertEquals(getCallerMethodName(), false, Converters.convert(boolean.class, 0.0));
        assertEquals(getCallerMethodName(), true, Converters.convert(boolean.class, 1.0F));
        assertEquals(getCallerMethodName(), false, Converters.convert(boolean.class, 0.0F));
        assertEquals(getCallerMethodName(), true, Converters.convert(boolean.class, (byte) 1));
        assertEquals(getCallerMethodName(), false, Converters.convert(boolean.class, (byte) 0));
    }

    @Test
    public void testByteConverter() throws Exception {

        assertEquals(getCallerMethodName(), "8F", Converters.convert(String.class, (byte) 0x8F));
        assertEquals(getCallerMethodName(), "FF", Converters.convert(String.class, (byte) 0xFF));
        assertEquals(getCallerMethodName(), (byte) 0x8F, (byte) Converters.convert(byte.class, "8F"));
        assertEquals(getCallerMethodName(), (byte) 0xFF, (byte) Converters.convert(byte.class, "FF"));
    }

    @Test
    public void testCharConverter() throws Exception {

        assertEquals(getCallerMethodName(), "A", Converters.convert(String.class, 'A'));
        assertEquals(getCallerMethodName(), 'A', (char) Converters.convert(char.class, "A"));

        assertEquals(getCallerMethodName(), "A", Converters.convert(String.class, 'A'));
        assertEquals(getCallerMethodName(), 'A', (char) Converters.convert(Character.class, "A"));
    }

    @Test
    public void testDoubleConverter() throws Exception {

        assertEquals(getCallerMethodName(), "10.0", Converters.convert(String.class, 10.0));
        assertEquals(getCallerMethodName(), 10.0, Converters.convert(double.class, "10"), 0.0001);
        assertEquals(getCallerMethodName(), 10.0, Converters.convert(double.class, 10.0), 0.0001);
        assertEquals(getCallerMethodName(), 10.0, Converters.convert(double.class, 10L), 0.0001);
    }

    @Test
    public void testFloatConverter() throws Exception {

        assertEquals(getCallerMethodName(), "10.0", Converters.convert(String.class, 10.0));
        assertEquals(getCallerMethodName(), 10.0F, Converters.convert(float.class, "10"), 0.0001F);
        assertEquals(getCallerMethodName(), 10.0F, Converters.convert(float.class, 10.0), 0.0001F);
        assertEquals(getCallerMethodName(), 10.0F, Converters.convert(float.class, 10L), 0.0001F);
        assertEquals(getCallerMethodName(), Float.valueOf(10.0F), Converters.convert(Float.class, 10.0F), 0.0001F);
        assertEquals(getCallerMethodName(), Float.valueOf(10.0F), Converters.convert(Float.class, 10), 0.0001F);
    }

    @Test
    public void testXToIntegerConverter() throws Exception {

        Integer empty = null;
        assertEquals(getCallerMethodName(), 10, (int) Converters.convert(int.class, "10"));
        assertEquals(getCallerMethodName(), 10, (int) Converters.convert(int.class, 10.0));
        assertEquals(getCallerMethodName(), 10, (int) Converters.convert(int.class, 10L));
        assertEquals(getCallerMethodName(), 0, (int) Converters.convert(int.class, empty));
    }

    @Test
    public void testNumberToIntegerConverter() throws Exception {
        assertEquals(getCallerMethodName(), new Integer(10),
                (Integer) NumberConverter.convertNumberToInteger(int.class, new Integer(10)));
    }

    @Test
    public void testIntegerToXConverter() throws Exception {
        assertEquals(getCallerMethodName(), "10", Converters.convert(String.class, 10));
        assertEquals(getCallerMethodName(), 10.0, Converters.convert(double.class, 10), 0.0);
        assertEquals(getCallerMethodName(), 10L, (long) Converters.convert(long.class, 10));
    }

    @Test
    public void testIntToStringConverter() throws Exception {
        assertEquals(getCallerMethodName(), "10", Converters.convert(String.class, 10));
        assertEquals(getCallerMethodName(), "-123", Converters.convert(String.class, -123));
    }

    @Test
    public void testStringToIntConverter() throws Exception {
        assertEquals(getCallerMethodName(), 10, (int) Converters.convert(int.class, "10"));
        assertEquals(getCallerMethodName(), -123, (int) Converters.convert(int.class, "-123"));
    }

    @Test
    public void testShortToStringConverter() throws Exception {
        assertEquals(getCallerMethodName(), "10", Converters.convert(String.class, (short) 10));
        assertEquals(getCallerMethodName(), "-123", Converters.convert(String.class, (short) -123));
    }

    @Test
    public void testStringToShortConverter() throws Exception {
        assertEquals(getCallerMethodName(), (short) 10, (short) Converters.convert(short.class, "10"));
        assertEquals(getCallerMethodName(), (short) -123, (short) Converters.convert(short.class, "-123"));

        assertEquals(getCallerMethodName(), Short.valueOf((short) 10), (Short) Converters.convert(Short.class, "10"));
        assertEquals(getCallerMethodName(), Short.valueOf((short) -123), (Short) Converters.convert(Short.class, "-123"));
    }

    @Test
    public void testDoubleToIntConverter() throws Exception {
        assertEquals(getCallerMethodName(), 10, (int) Converters.convert(int.class, 10.0));
        assertEquals(getCallerMethodName(), -123, (int) Converters.convert(int.class, -123.0));
    }

    @Test
    public void testLongToIntConverter() throws Exception {
        assertEquals(getCallerMethodName(), 10, (int) Converters.convert(int.class, 10L));
        assertEquals(getCallerMethodName(), -10, (int) Converters.convert(int.class, -10L));
    }

    @Test
    public void testLongConverter() throws Exception {

        assertEquals(getCallerMethodName(), "10", Converters.convert(String.class, 10L));
        assertEquals(getCallerMethodName(), 10L, (long) Converters.convert(long.class, "10"));
        assertEquals(getCallerMethodName(), 10L, (long) Converters.convert(long.class, 10.0));
        assertEquals(getCallerMethodName(), 10L, (long) Converters.convert(long.class, 10L));
    }

    @Test
    public void testShortConverter() throws Exception {

        assertEquals(getCallerMethodName(), "10", Converters.convert(String.class, 10));
        assertEquals(getCallerMethodName(), 10, (short) Converters.convert(short.class, "10"));
        assertEquals(getCallerMethodName(), 10, (short) Converters.convert(short.class, 10.0));
        assertEquals(getCallerMethodName(), 10, (short) Converters.convert(short.class, 10L));
    }

    @Test
    public void testStringConverter() throws Exception {

        assertEquals(getCallerMethodName(), "A", Converters.convert(String.class, "A"));
        assertEquals(getCallerMethodName(), "A", Converters.convert(String.class, "A"));

        assertEquals(getCallerMethodName(), "A", Converters.convert(String.class, "A"));
        assertEquals(getCallerMethodName(), "A", Converters.convert(String.class, "A"));
    }

    // Misc.

    @Test
    public void testDateConverter() throws Exception {

        long epochTime = 1433030276000L;
        java.util.Date date = new java.util.Date(epochTime); // 05/30/2015
        assertEquals(getCallerMethodName(), epochTime, (long) Converters.convert(Long.class, date));
        assertEquals(getCallerMethodName(), date, Converters.convert(java.util.Date.class, epochTime));

        String dateStr = "2015-05-30T16:57:56.000-0700";
        assertEquals(getCallerMethodName(), dateStr, Converters.convert(String.class, date));
        assertEquals(getCallerMethodName(), date, Converters.convert(java.util.Date.class, dateStr));
    }

    @Test
    public void testCalendarConverter() throws Exception {

        long epochTime = 1433030276L;
        Calendar date = Calendar.getInstance(); // 05/30/2015
        date.setTimeInMillis(epochTime);
        assertEquals(getCallerMethodName(), epochTime, (long) Converters.convert(Long.class, date));
        assertEquals(getCallerMethodName(), date.getTimeInMillis(),
                Converters.convert(Calendar.class, epochTime).getTimeInMillis());
    }

    // @Test
    // public void testFileConverter() throws Exception {
    // throw new Exception("testFileConverter not implemented");
    // }

    @Test
    public void testSqlDateConverter() throws Exception {

        java.sql.Date date = new java.sql.Date(1433030276); // 05/30/2015
        long epochTime = 1433030276L;
        assertEquals(getCallerMethodName(), epochTime, (long) Converters.convert(Long.class, date));
        assertEquals(getCallerMethodName(), date, Converters.convert(java.sql.Date.class, epochTime));
    }

    @Test
    public void testSqlTimeConverter() throws Exception {

        java.sql.Time date = new java.sql.Time(1433030276); // 05/30/2015
        long epochTime = 1433030276L;
        assertEquals(getCallerMethodName(), epochTime, (long) Converters.convert(Long.class, date));
        assertEquals(getCallerMethodName(), date, Converters.convert(java.sql.Time.class, epochTime));
    }

    @Test
    public void testSqlTimestampConverter() throws Exception {

        java.sql.Timestamp date = new java.sql.Timestamp(1433030276); // 05/30/2015
        long epochTime = 1433030276L;
        assertEquals(getCallerMethodName(), epochTime, (long) Converters.convert(Long.class, date));
        assertEquals(getCallerMethodName(), date, Converters.convert(java.sql.Timestamp.class, epochTime));
    }

    @Test
    public void testURLConverter() throws Exception {

        String urlStr = "http://www.google.com";
        URL url = new URL(urlStr);

        assertEquals(getCallerMethodName(), urlStr, Converters.convert(String.class, url));
        assertEquals(getCallerMethodName(), url, Converters.convert(URL.class, urlStr));
    }

    @Test
    public void testListConverter() throws Throwable {

        String expectedStr = "[\"tom@viper.com\",\"john@viper.com\",\"bill@viper.com\"]";
        List<String> expected = new ArrayList<String>();
        {
            expected.add("tom@viper.com");
            expected.add("john@viper.com");
            expected.add("bill@viper.com");
        }

        List<String> actual = Converters.convertToList(String.class, expectedStr);

        String actualStr = Converters.convertFromList(expected);

        assertEquals(getCallerMethodName(), actual.size(), expected.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(getCallerMethodName() + ":" + i, expected.get(i), actual.get(i));
        }

        assertEquals(getCallerMethodName(), expected, actual);
        assertEquals(getCallerMethodName(), expectedStr, actualStr);

        // assertEquals(getCallerMethodName(), expectedStr,
        // actualStrConverters.convert(String.class, expected));
    }

    @Test
    public void testEmptyListConverter() throws Throwable {

        String expectedStr = "[]";
        List<String> expected = new ArrayList<String>();

        List<String> actual = Converters.convertToList(String.class, expectedStr);

        String actualStr = Converters.convertFromList(expected);

        assertEquals(getCallerMethodName(), actual.size(), expected.size());
        assertEquals(getCallerMethodName(), expected, actual);
        assertEquals(getCallerMethodName(), expectedStr, actualStr);

    }

    @Test
    public void testListBeanConverter() throws Throwable {

        List<Employee> beans = RandomBean.getRandomBeans(Employee.class, 1, 1);

        String actual = Converters.convertFromList(beans);

        List<Employee> beans1 = Converters.convertToList(Employee.class, actual);

        String expected = Converters.convertFromList(beans1);

        assertEquals(getCallerMethodName(), expected, actual);
    }

    // Arrays

    @Test
    public void testArrayInteger1Converter() throws Exception {

        Integer a[] = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        String value = "1,2,3,4,5,6,7,8,9";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(Integer[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testArrayBoolean1Converter() throws Exception {

        Boolean a[] = new Boolean[] { true, false, true };
        String value = "true,false,true";
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(Boolean[].class, value));
    }

    @Test
    public void testArrayCharacterConverter() throws Exception {

        Character a[] = new Character[] { 'A', 'B', 'C' };
        String value = "ABC";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(Character[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testArrayDouble1Converter() throws Exception {

        Double a[] = new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0 };
        String value = "1.0,2.0,3.0,4.0,5.0";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(Double[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testStringFloatArrayConverter() throws Exception {

        Float a[] = new Float[] { 1.0F, 2.0F, 3.0F, 4.0F, 5.0F };
        String value = "1.0,2.0,3.0,4.0,5.0";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(Float[].class, value));
    }

    @Test
    public void testFloatArrayStringConverter() throws Exception {

        Float a[] = new Float[] { 1.0F, 2.0F, 3.0F, 4.0F, 5.0F };
        String value = "1.0,2.0,3.0,4.0,5.0";
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testArrayShort1Converter() throws Exception {

        Short a[] = new Short[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        String value = "1,2,3,4,5,6,7,8,9";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(Short[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testArrayLong1Converter() throws Exception {

        Long a[] = new Long[] { 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L };
        String value = "1,2,3,4,5,6,7,8,9";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(Long[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    // registerArrayConverter(BigDecimal.class, new BigDecimalConverter(),
    // throwException,
    // defaultArraySize);
    // registerArrayConverter(BigInteger.class, new BigIntegerConverter(),
    // throwException,
    // defaultArraySize);

    @Test
    public void testArrayStringConverter() throws Exception {

        String a[] = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" };
        String value = "1,2,3,4,5,6,7,8,9";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(String[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testArrayIntegerConverter() throws Exception {

        int a[] = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        String value = "1,2,3,4,5,6,7,8,9";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(int[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testArrayBooleanConverter() throws Exception {

        boolean a[] = new boolean[] { true, false, true };
        String value = "true,false,true";
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(boolean[].class, value));
    }

    @Test
    public void testArrayByteConverter() throws Exception {

        String value2 = "7F,-45,28,-127";
        Byte a2[] = new Byte[] { 127, -45, 28, -127 };
        assertArrayEquals(getCallerMethodName(), a2, Converters.convert(Byte[].class, value2));
        assertEquals(getCallerMethodName(), value2, Converters.convert(String.class, a2));

        String value1 = "7FD31C81";
        byte a1[] = new byte[] { 0x7F, (byte) 0xD3, 0x1C, (byte) 0x81 };
        assertArrayEquals(getCallerMethodName(), a1, Converters.convert(byte[].class, value1));
        assertEquals(getCallerMethodName(), value1, Converters.convert(String.class, a1));

    }

    @Test
    public void testArrayCharConverter() throws Exception {

        char a[] = new char[] { 'A', 'B', 'C' };
        String value = "ABC";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(char[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testArrayDoubleConverter() throws Exception {

        double a[] = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0 };
        String value = "1.0,2.0,3.0,4.0,5.0";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(double[].class, value), 0.001);
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testStringAndArrayFloatConverter() throws Exception {

        float a[] = new float[] { 1.0F, 2.0F, 3.0F, 4.0F, 5.0F };
        String value = "1.0,2.0,3.0,4.0,5.0";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(float[].class, value), 0.001F);
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testStringAndShortArrayConverter() throws Exception {

        short a[] = new short[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        String value = "1,2,3,4,5,6,7,8,9";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(short[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    @Test
    public void testStringAndLongArrayConverter() throws Exception {

        long a[] = new long[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        String value = "1,2,3,4,5,6,7,8,9";
        assertArrayEquals(getCallerMethodName(), a, Converters.convert(long[].class, value));
        assertEquals(getCallerMethodName(), value, Converters.convert(String.class, a));
    }

    // registerArrayConverter(String.class, new StringConverter(),
    // throwException,
    // defaultArraySize);
    //
    // // Other
    // registerArrayConverter(Class.class, new ClassConverter(), throwException,
    // defaultArraySize);
    // registerArrayConverter(java.util.Date.class, new DateConverter(),
    // throwException,
    // defaultArraySize);
    // registerArrayConverter(Calendar.class, new DateConverter(),
    // throwException,
    // defaultArraySize);
    // registerArrayConverter(File.class, new FileConverter(), throwException,
    // defaultArraySize);
    // registerArrayConverter(java.sql.Date.class, new SqlDateConverter(),
    // throwException,
    // defaultArraySize);
    // registerArrayConverter(java.sql.Time.class, new SqlTimeConverter(),
    // throwException,
    // defaultArraySize);
    // registerArrayConverter(Timestamp.class, new SqlTimestampConverter(),
    // throwException,
    // defaultArraySize);
    // registerArrayConverter(URL.class, new URLConverter(), throwException,
    // defaultArraySize);

    @Test
    public void testEnumDayConverter() throws Exception {

        String dayStr = "WEDNESDAY";
        Day day = Day.WEDNESDAY;

        // Converters.register(Day.class, new EnumConverter());
        assertEquals(getCallerMethodName(), dayStr, Converters.convert(String.class, day));
        assertEquals(getCallerMethodName(), day, Converters.convert(Day.class, dayStr));
    }

    @Test
    public void testEnumNamingField() throws Exception {

        String str = "A.1";
        NamingField namingField = NamingField.A_1;

        assertEquals(getCallerMethodName(), namingField.value(), Converters.convert(NamingField.class, str).value());
        assertEquals(getCallerMethodName(), str, Converters.convert(String.class, namingField));
    }

    @Test
    public void testActivityConverter1() throws Exception {

        Activity expected = RandomBean.getRandomBean(Activity.class, 1);

        String json = Converters.convert(String.class, expected);
        assertBeanEquals(getCallerMethodName(), expected, Converters.convert(expected.getClass(), json));
    }

    @Test
    public void testActivityListConverter() throws Exception {

        List<Activity> activity = new ArrayList<Activity>();
        activity.add(RandomBean.getRandomBean(Activity.class, 1));
        activity.add(RandomBean.getRandomBean(Activity.class, 2));
        activity.add(RandomBean.getRandomBean(Activity.class, 3));

        String json = Converters.convert(String.class, activity);

        assertBeansEquals(getCallerMethodName(), activity, Arrays.asList(Converters.convert(Activity[].class, json)));
    }

    @Test
    public void testMyColorConverter() throws Exception {

        String str = "BLUE";
        MyColor myColor = MyColor.BLUE;

        assertEquals(getCallerMethodName(), str, Converters.convert(String.class, myColor));
        assertEquals(getCallerMethodName(), myColor.toString(), Converters.convert(MyColor.class, str).toString());
    }

    @Test
    public void testActivityConverter() throws Exception {
        String expectedStr = "{\"lastModifiedAt\":1510121631815,\"color\":\"ygx\",\"isvisibletotcmapping\":false,\"description\":\"geregnkbgqukccadckcyebafzgibfsamdzqqfdipioywl\",\"earningTypeId\":2,\"organizationId\":3,\"activityCategoryId\":163,\"sourceMeasureId\":445,\"isDeleted\":false,\"isRequestable\":true,\"maxDurationThreshold\":437,\"modifiedBy\":\"rrigkostwwtrhlfzsjodhwpnfftevpo\",\"id\":0,\"isUnavailability\":true,\"isQueueHopping\":false,\"isUsedInShift\":false,\"adherenceTolerance\":-28182,\"isusedincalendarevent\":false,\"isusedinshiftevent\":false,\"isPaid\":false,\"name\":\"pr\",\"isTimeOff\":true,\"isOut\":false,\"istimeoffwithallotment\":true,\"colorCode\":\"ls\"}";
        Activity expected = Converters.convert(Activity.class, expectedStr);

        assertEquals(getCallerMethodName(), expectedStr.length(), Converters.convert(String.class, expected).length());
    }

    @Test
    public void testBlobConverter() throws Exception {

        byte expectedBytes[] = RandomBean.randomBytes(30, 200);
        Blob expectedBlob = new javax.sql.rowset.serial.SerialBlob(expectedBytes);

        assertArrayEquals(getCallerMethodName(), expectedBytes, Converters.convert(byte[].class, expectedBlob));
        assertEquals(getCallerMethodName(), expectedBlob,
                Converters.convert(javax.sql.rowset.serial.SerialBlob.class, expectedBytes));
    }

    @Test
    public void testClobConverter() throws Exception {

        char expectedChars[] = RandomBean.randomChars(30, 200);
        Clob expectedClob = new javax.sql.rowset.serial.SerialClob(expectedChars);

        assertArrayEquals(getCallerMethodName(), expectedChars, Converters.convert(char[].class, expectedClob));
        assertEquals(getCallerMethodName(), expectedClob,
                Converters.convert(javax.sql.rowset.serial.SerialClob.class, expectedChars));
    }
}
