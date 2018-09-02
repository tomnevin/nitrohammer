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

public final class IntegerConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public IntegerConverter(Class<?> defaultType) {
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
        if (toType.equals(Integer.class)) {
            return convertToInteger(fromValue);
        }
        if (toType.equals(int.class)) {
            return convertToInteger(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            if (toType.equals(int.class)) {
                return convertToIntegerPrimitiveArray((String) fromValue);
            }
            if (toType.equals(Integer.class)) {
                return convertToIntegerArray((String) fromValue);
            }
        }

        if (fromValue.getClass().isArray()) {
//        	if (fromValue.getClass().equals(byte[].class)) {
//        		throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
//        	}
            if (toType.equals(int.class)) {
                return convertToIntegerPrimitiveArray(ConverterUtils.createArrayFromArrayObject(fromValue));
            }

            if (toType.equals(Integer.class)) {
                return convertToIntegerArray(ConverterUtils.createArrayFromArrayObject(fromValue));
            }
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Integer) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    private Integer convertToInteger(Object fromValue) throws Exception {

        if (fromValue instanceof Number) {
            return ((Number) fromValue).intValue();

        } else if (fromValue instanceof Blob) {
            return null; // TODO

        } else if (fromValue instanceof Boolean) {
            return ((Boolean) fromValue) ? 1 : 0;

        } else if (fromValue instanceof Clob) {
            return null; // TODO

        } else if (fromValue instanceof String) {
            return Integer.parseInt((String) fromValue);

        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to Integer.");
        }
    }

    private Integer[] convertToIntegerArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        Integer[] toValues = new Integer[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToInteger(fromValues[i]);
        }
        return toValues;
    }

    private int[] convertToIntegerPrimitiveArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        int[] toValues = new int[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToInteger(fromValues[i]);
        }
        return toValues;
    }

    private Integer[] convertToIntegerArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        Integer[] toValues = new Integer[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertToInteger(items[i]);
        }
        return toValues;
    }

    private int[] convertToIntegerPrimitiveArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        int[] toValues = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] =  convertToInteger(items[i]);
        }
        return toValues;
    }
}
