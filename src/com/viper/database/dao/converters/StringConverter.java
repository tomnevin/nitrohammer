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
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;

import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;

import com.viper.database.annotations.Column;

public final class StringConverter implements ConverterInterface {

    private final static Mapper mapper = new MapperBuilder().build();
    private static final String DELIMITER = ",";

    private Class<?> defaultType = null;

    public StringConverter(Class<?> defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public Class<?> getDefaultType() {
        return defaultType;
    }

    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {

        if (fromValue == null) {
            return null;
        }

        if (toType.equals(String.class)) {
            if (fromValue.getClass().isAssignableFrom(boolean[].class)) {
                return convertArrayToString((boolean[]) fromValue, DELIMITER);
            }
            if (fromValue.getClass().isAssignableFrom(Boolean[].class)) {
                return convertArrayToString((Boolean[]) fromValue, DELIMITER);
            }
            if (fromValue.getClass().isAssignableFrom(byte[].class)) {
                return convertToHexString(fromValue);
            }
            if (fromValue.getClass().isAssignableFrom(Byte[].class)) {
                return convertToHexString(fromValue);
            }
            if (fromValue.getClass().isAssignableFrom(float[].class)) {
                return convertArrayToString((float[]) fromValue, DELIMITER);
            }
            if (fromValue.getClass().isAssignableFrom(double[].class)) {
                return convertArrayToString((double[]) fromValue, DELIMITER);
            }
            if (fromValue.getClass().isAssignableFrom(char[].class)) {
                return convertArrayToString((char[]) fromValue);
            }
            if (fromValue.getClass().isAssignableFrom(Character[].class)) {
                return convertArrayToString((Character[]) fromValue);
            }
            if (fromValue.getClass().isAssignableFrom(short[].class)) {
                return convertArrayToString((short[]) fromValue, DELIMITER);
            }
            if (fromValue.getClass().isAssignableFrom(long[].class)) {
                return convertArrayToString((long[]) fromValue, DELIMITER);
            }
            if (fromValue.getClass().isAssignableFrom(int[].class)) {
                return convertArrayToString((int[]) fromValue, DELIMITER);
            }
            if (fromValue.getClass().isAssignableFrom(String[].class)) {
                return convertArrayToString((String[]) fromValue, DELIMITER);
            }
            if (Number[].class.isAssignableFrom(fromValue.getClass())) {
                return convertArrayToString((Number[]) fromValue, DELIMITER);
            }
            if (Object[].class.isAssignableFrom(fromValue.getClass())) {
                return convertArrayToString((Object[]) fromValue, DELIMITER);
            }
            return convertToString(fromValue);

        }
        if (fromValue.getClass().isAssignableFrom(String.class)) {
            return convertStringToArray((String) fromValue);

        }
        if (fromValue.getClass().isArray()) {
            return mapper.writeArrayAsString((Object[]) fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue.getClass().isAssignableFrom(String.class)) {
            return convertStringToArray((String) fromValue);

        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {

        if (fromValue.getClass().isAssignableFrom(java.util.Date.class)) {
            return new DateConverter(fromValue.getClass()).convertFromDate(String.class, (java.util.Date) fromValue);

        } else if (fromValue instanceof String) {
            return (String) fromValue;
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    private String convertToString(Object fromValue) throws Exception {

        if (fromValue instanceof Byte) {
            return String.format("%02X", (Byte) fromValue);

        } else if (fromValue.getClass().isAssignableFrom(byte.class)) {
            return String.format("%02X", (Byte) fromValue);

        } else if (fromValue instanceof BigDecimal) {
            return Double.toString(((BigDecimal) fromValue).doubleValue());

        } else if (fromValue instanceof BigInteger) {
            return Long.toString(((BigInteger) fromValue).longValue());

        } else if (fromValue instanceof Double) {
            return Double.toString((Double) fromValue);

        } else if (fromValue instanceof Float) {
            return Float.toString((Float) fromValue);

        } else if (fromValue instanceof Long) {
            return Long.toString((Long) fromValue);

        } else if (fromValue instanceof Integer) {
            return Integer.toString((Integer) fromValue);

        } else if (fromValue instanceof Short) {
            return Short.toString((Short) fromValue);

        } else if (fromValue instanceof Blob) {
            return null; // TODO

        } else if (fromValue instanceof Boolean) {
            return Boolean.toString((Boolean) fromValue);

        } else if (fromValue instanceof Clob) {
            return null; // TODO

        } else if (fromValue instanceof Character) {
            return "" + fromValue;

        } else if (fromValue.getClass().isAssignableFrom(java.util.Date.class)) {
            return new DateConverter(fromValue.getClass()).convertFromDate(String.class, (java.util.Date) fromValue);

        } else if (fromValue instanceof String) {
            return (String) fromValue;

        } else if (fromValue instanceof URL) {
            return new URLConverter().convertToString((Column)null, fromValue, null);

        }
        return new BeanConverter().convertToString((Column)null, fromValue, null);
    }

    private String convertArrayToString(Object[] fromValues, String delimiter) throws Exception {
        if (fromValues == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (Object fromValue : fromValues) {
            if (buf.length() > 0) {
                buf.append(delimiter);
            }
            String item = convertToString(fromValue);
            buf.append(item);
        }
        return buf.toString();
    }

    private String convertArrayToString(boolean[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (boolean fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(convertToString(fromValue));
            }
        }
        return buf.toString();
    }

    private String convertArrayToString(Boolean[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (Boolean fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(convertToString(fromValue));
            }
        }
        return buf.toString();
    }

    private String convertArrayToString(short[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (short fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(convertToString(fromValue));
            }
        }
        return buf.toString();
    }

    private String convertArrayToString(int[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (int fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(convertToString(fromValue));
            }
        }
        return buf.toString();
    }

    private String convertArrayToString(long[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (long fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(convertToString(fromValue));
            }
        }
        return buf.toString();
    }

    private String convertArrayToString(float[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (float fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(convertToString(fromValue));
            }
        }
        return buf.toString();
    }

    private String convertArrayToString(double[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (double fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(convertToString(fromValue));
            }
        }
        return buf.toString();
    }

    private String convertArrayToString(char[] fromValues) throws Exception {
        return new String(fromValues);
    }

    private String convertArrayToString(Character[] fromValues) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (Character fromValue : fromValues) {
                buf.append(fromValue);
            }
        }
        return buf.toString();
    }

    protected String convertToHexString(Object value) throws Exception {
        if (value instanceof Blob) {
            Blob blob = (Blob) value;
            return ConverterUtils.toHex(blob.getBytes(1L, (int) blob.length()));
        } else if (value instanceof byte[]) {
            return ConverterUtils.toHex((byte[]) value);
        } else if (value instanceof Byte[]) {
            return ConverterUtils.toHex((byte[]) value);
        }
        return (value == null) ? null : value.toString();
    }

    private String[] convertStringToArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        return fromValues.split("\\s*(" + DELIMITER + ")\\s*");
    }
}
