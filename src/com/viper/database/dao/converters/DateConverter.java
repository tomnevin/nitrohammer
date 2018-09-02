/*
 * -----------------------------------------------------------------------------
 *                      VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * MIT License
 * 
 * Copyright (c) #{classname}.java #{util.YYYY()} Viper Software Services
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

package com.viper.database.dao.converters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.viper.database.annotations.Column;

public final class DateConverter implements ConverterInterface {

    private final static String TimePattern = "HH:mm:ss.SSS";
    private final static String DatePattern = "yyyy-MM-dd";
    private final static String TimestampPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final static List<String> patterns = new ArrayList<String>();
    {
        patterns.add(TimestampPattern);
        patterns.add("yyyy-MM-dd HH:mm:ss.S");
        patterns.add("yyyy-MM-dd hh:mm:ss");
        patterns.add(TimePattern);
        patterns.add("HH:mm:ss");
        patterns.add(DatePattern);
    }

    public static void addPattern(String pattern) {
        if (!patterns.contains(pattern)) {
            patterns.add(pattern);
        }
    }

    private Class<?> defaultType = null;

    public DateConverter(Class<?> defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public Class<?> getDefaultType() {
        return defaultType;
    }

    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {

        if (java.util.Date.class.isAssignableFrom(toType)) {
            return convertToDate(fromValue);

        } else if (fromValue instanceof Date) {
            return convertFromDate(toType, (Date) fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof java.util.Date) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }
        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    public final static Date convertFromString(String fromValue) throws Exception {
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                Date date = sdf.parse(fromValue);
                if (date != null) {
                    return date;
                }
            } catch (Exception ex) {
                ; // Intentional
            }
        }

        throw new Exception("Unhandled conversion convertFromString " + fromValue + " to Date.");
    }

    public Date convertToDate(Object fromValue) throws Exception {

        if (fromValue.getClass().isAssignableFrom(boolean.class)) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Date.");

        } else if (fromValue.getClass().isAssignableFrom(byte.class)) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Date.");

        } else if (fromValue.getClass().isAssignableFrom(int.class)) {
            return newInstance((int) fromValue);

        } else if (fromValue.getClass().isAssignableFrom(double.class)) {
            return newInstance((long) ((double) fromValue * 1000.0));

        } else if (fromValue.getClass().isAssignableFrom(short.class)) {
            return newInstance((short) fromValue);

        } else if (fromValue.getClass().isAssignableFrom(float.class)) {
            return newInstance((long) ((float) fromValue * 1000.0));

        } else if (fromValue.getClass().isAssignableFrom(long.class)) {
            return newInstance((long) fromValue);

        } else if (fromValue instanceof Byte) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Date.");

        } else if (fromValue instanceof BigDecimal) {
            long val = (long) (((BigDecimal) fromValue).doubleValue() * 1000.0);
            return newInstance(val);

        } else if (fromValue instanceof BigInteger) {
            return newInstance(((BigInteger) fromValue).longValue());

        } else if (fromValue instanceof Blob) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Date.");

        } else if (fromValue instanceof Boolean) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Date.");

        } else if (fromValue instanceof Clob) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Date.");

        } else if (fromValue instanceof Double) {
            return newInstance((long) ((Double) fromValue * 1000.0));

        } else if (fromValue instanceof Integer) {
            return newInstance((Integer) fromValue);

        } else if (fromValue instanceof Long) {
            return newInstance((Long) fromValue);

        } else if (fromValue instanceof Short) {
            return newInstance((Short) fromValue);

        } else if (fromValue instanceof String) {
            return convertFromString((String) fromValue);

        } else if (fromValue instanceof java.sql.Date) {
            return (Date) fromValue;

        } else if (fromValue instanceof java.sql.Time) {
            return (Date) fromValue;

        } else if (fromValue instanceof java.sql.Timestamp) {
            return (Date) fromValue;

        } else if (fromValue instanceof java.util.Date) {
            return (Date) fromValue;

        }

        throw new Exception("Unhandled conversion from " + fromValue + " " + fromValue.getClass() + " to Date.");
    }

    public <T> T convertFromDate(Class<T> targetType, Date fromValue) throws Exception {

        if (targetType.equals(Byte.class)) {
            return null; // TODO

        } else if (targetType.equals(BigDecimal.class)) {
            return targetType.cast(BigDecimal.valueOf(fromValue.getTime() / 1000.0));

        } else if (targetType.equals(Blob.class)) {
            return null; // TODO

        } else if (targetType.equals(Boolean.class)) {
            return null; // TODO

        } else if (targetType.equals(BigInteger.class)) {
            return targetType.cast(BigInteger.valueOf(fromValue.getTime()));

        } else if (targetType.equals(Date.class)) {
            return targetType.cast(fromValue);

        } else if (targetType.equals(Clob.class)) {
            return null; // TODO

        } else if (targetType.equals(Double.class)) {
            return targetType.cast(Double.valueOf(fromValue.getTime() / 1000.0));

        } else if (targetType.equals(Float.class)) {
            return targetType.cast(Float.valueOf(fromValue.getTime() / 1000.0F));

        } else if (targetType.equals(Integer.class)) {
            int val = (int) (fromValue.getTime() / 1000);
            return targetType.cast(Integer.valueOf(val));

        } else if (targetType.equals(Long.class)) {
            return targetType.cast(Long.valueOf(fromValue.getTime()));

        } else if (targetType.equals(Short.class)) {
            short val = (short) (fromValue.getTime() / 1000);
            return targetType.cast(Short.valueOf(val));

        } else if (targetType.equals(String.class)) {

            if (fromValue instanceof java.sql.Timestamp) {
                SimpleDateFormat sdf = new SimpleDateFormat(TimestampPattern);
                return targetType.cast(sdf.format(fromValue));

            } else if (fromValue instanceof java.sql.Time) {
                SimpleDateFormat sdf = new SimpleDateFormat(TimePattern);
                return targetType.cast(sdf.format(fromValue));

            } else if (fromValue instanceof java.sql.Date) {
                SimpleDateFormat sdf = new SimpleDateFormat(DatePattern);
                return targetType.cast(sdf.format(fromValue));

            }
            SimpleDateFormat sdf = new SimpleDateFormat(TimestampPattern);
            return targetType.cast(sdf.format(fromValue));
        }

        throw new Exception("Unhandled conversion from Date to " + fromValue + ".");
    }

    private java.util.Date newInstance(long val) throws Exception {
        if (java.sql.Date.class.isAssignableFrom(defaultType)) {
            return new java.sql.Date(val);
        }
        if (java.sql.Time.class.isAssignableFrom(defaultType)) {
            return new java.sql.Time(val);
        }
        if (java.sql.Timestamp.class.isAssignableFrom(defaultType)) {
            return new java.sql.Timestamp(val);
        }
        if (java.util.Date.class.isAssignableFrom(defaultType)) {
            return new java.util.Date(val);
        }
        throw new Exception("Unhandled new instance of Date " + defaultType + ".");
    }
}
