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

import java.sql.Blob;
import java.sql.Clob;
import java.text.DecimalFormat;

import com.viper.database.annotations.Column;

public final class LongConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public LongConverter(Class<?> defaultType) {
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

        if (toType.equals(Long.class)) {
            return convertToLong(fromValue);
        }

        if (toType.equals(long.class)) {
            return convertToLong(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            if (toType.equals(long.class)) {
                return convertToLongPrimitiveArray((String) fromValue);
            }
            if (toType.equals(Long.class)) {
                return convertToLongArray((String) fromValue);
            }
        }

        if (fromValue.getClass().isArray()) {
//        	if (fromValue.getClass().equals(byte[].class)) {
//        		throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
//        	}
            if (toType.equals(long.class)) {
                return convertToLongPrimitiveArray(ConverterUtils.createArrayFromArrayObject(fromValue));
            }
            if (toType.equals(Long.class)) {
                return convertToLongArray(ConverterUtils.createArrayFromArrayObject(fromValue));
            }
        }
        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Long) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    private long convertToLong(Object fromValue) throws Exception {

        if (fromValue instanceof Number) {
            return ((Number) fromValue).longValue();

        } else if (fromValue.getClass().isAssignableFrom(byte.class)) {
            return (long) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(int.class)) {
            return (long) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(double.class)) {
            return (long) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(short.class)) {
            return (long) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(float.class)) {
            return (long) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(long.class)) {
            return (long) fromValue;

        } else if (java.util.Date.class.isAssignableFrom(fromValue.getClass())) {
            return ((java.util.Date) fromValue).getTime();

        } else if (java.util.Calendar.class.isAssignableFrom(fromValue.getClass())) {
            return ((java.util.Calendar) fromValue).getTimeInMillis();

        } else if (fromValue instanceof Blob) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Long.");

        } else if (fromValue instanceof byte[]) {
            throw new Exception("Unhandled conversion from " + fromValue + " to long[].");

        } else if (fromValue instanceof Boolean) {
            return ((Boolean) fromValue) ? 1L : 0L;

        } else if (fromValue instanceof Clob) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Long.");

        } else if (fromValue instanceof String) {
            return convertToLong((String) fromValue);

        }
        throw new Exception("Unhandled conversion from " + fromValue + " to Long.");
    }

    private Long[] convertToLongArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        Long[] toValues = new Long[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToLong(fromValues[i]);
        }
        return toValues;
    }

    private long[] convertToLongPrimitiveArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        long[] toValues = new long[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToLong(fromValues[i]);
        }
        return toValues;
    }

    private Long[] convertToLongArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        Long[] toValues = new Long[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertToLong(items[i]);
        }
        return toValues;
    }

    private long[] convertToLongPrimitiveArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        long[] toValues = new long[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertToLong(items[i]);
        }
        return toValues;
    }
    
    private long convertToLong(String fromValue) throws Exception {
    	if (isNumeric(fromValue)) {
    		return Long.parseLong(fromValue);
    	}
    	
    	return DateConverter.convertFromString(fromValue).getTime();
    }
    
    private boolean isNumeric(String maybeNumeric) {
        return maybeNumeric != null && maybeNumeric.matches("[0-9]+");
    }
}
