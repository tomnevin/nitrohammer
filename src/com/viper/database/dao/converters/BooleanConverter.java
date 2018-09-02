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

import com.viper.database.annotations.Column;

public final class BooleanConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public BooleanConverter(Class<?> defaultType) {
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

        if (fromValue.getClass().isArray()) {
            return convertToArray(toType, fromValue);
        }

        // Needed??
        if (toType.equals(Boolean.class)) {
            return convertToBoolean(fromValue);
        }
        if (toType.equals(boolean.class)) {
            return convertToBoolean(fromValue);
        }

        if (!(fromValue instanceof Boolean)) {
            throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");

        } else if (toType.equals(Boolean.class)) {
            return fromValue;

        } else if (toType.equals(BigInteger.class)) {
            return BigInteger.valueOf(((Boolean) fromValue) ? 1 : 0);

        } else if (toType.equals(BigDecimal.class)) {
            return new BigDecimal(((Boolean) fromValue) ? 1.0 : 0.0);

        } else if (toType.equals(Blob.class)) {
            throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");

        } else if (toType.equals(Clob.class)) {
            throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");

        } else if (toType.equals(Byte.class)) {
            return ((Boolean) fromValue) ? 1 : 0;

        } else if (toType.equals(Character.class)) {
            return ((Boolean) fromValue) ? 'Y' : 'N';

        } else if (toType.equals(Double.class)) {
            return ((Boolean) fromValue) ? 1.0 : 0.0;

        } else if (toType.equals(Float.class)) {
            return ((Boolean) fromValue) ? 1.0 : 0.0;

        } else if (toType.equals(Integer.class)) {
            return ((Boolean) fromValue) ? 1 : 0;

        } else if (toType.equals(Long.class)) {
            return ((Boolean) fromValue) ? 1 : 0;

        } else if (toType.equals(Short.class)) {
            return ((Boolean) fromValue) ? 1 : 0;

        } else if (toType.equals(String.class)) {
            return ((Boolean) fromValue).toString();

        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
        }
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            if (toType.equals(boolean.class)) {
                return convertToBooleanPrimitiveArray((String) fromValue);
            }
            if (toType.equals(Boolean.class)) {
                return convertToBooleanArray((String) fromValue);
            }
        }

        if (fromValue.getClass().isArray()) {
            if (toType.equals(boolean.class)) {
                return convertToBooleanPrimitiveArray((Object[]) fromValue);
            }
            if (toType.equals(Boolean.class)) {
                return convertToBooleanArray((Object[]) fromValue);
            }
        }
        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (defaultType.isInstance(fromValue)) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    public Boolean convertToBoolean(Object fromValue) throws Exception {

        if (fromValue == null) {
            return null;

        } else if (fromValue.getClass().isAssignableFrom(Boolean.class)) {
            return (Boolean) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(boolean.class)) {
            return (Boolean) fromValue;

        } else if (fromValue instanceof Number) {
            return (((Number) fromValue).longValue() != 0L) ? true : false;

        } else if (fromValue.getClass().isAssignableFrom(byte.class)) {
            return ((byte) fromValue != 0L) ? true : false;

        } else if (fromValue.getClass().isAssignableFrom(int.class)) {
            return ((int) fromValue != 0L) ? true : false;

        } else if (fromValue.getClass().isAssignableFrom(double.class)) {
            return ((double) fromValue != 0L) ? true : false;

        } else if (fromValue.getClass().isAssignableFrom(short.class)) {
            return ((short) fromValue != 0L) ? true : false;

        } else if (fromValue.getClass().isAssignableFrom(float.class)) {
            return ((float) fromValue != 0L) ? true : false;

        } else if (fromValue.getClass().isAssignableFrom(long.class)) {
            return ((long) fromValue != 0L) ? true : false;

        } else if (fromValue instanceof String) {
            return Boolean.parseBoolean((String) fromValue);

        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to Boolean.");
        }
    }

    public Boolean[] convertToBooleanArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        Boolean[] toValues = new Boolean[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToBoolean(fromValues[i]);
        }
        return toValues;
    }

    public boolean[] convertToBooleanPrimitiveArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        boolean[] toValues = new boolean[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToBoolean(fromValues[i]);
        }
        return toValues;
    }

    private Boolean[] convertToBooleanArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        return convertToBooleanArray(fromValues.split("\\s*(,)\\s*"));
    }

    private boolean[] convertToBooleanPrimitiveArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        return convertToBooleanPrimitiveArray(fromValues.split("\\s*(,)\\s*"));
    }
}
