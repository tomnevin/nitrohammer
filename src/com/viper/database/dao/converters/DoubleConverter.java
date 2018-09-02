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

public final class DoubleConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public DoubleConverter(Class<?> defaultType) {
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

        if (toType.equals(Double.class)) {
            return convertToDouble(fromValue);
        }
        if (toType.equals(double.class)) {
            return convertToDouble(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            if (toType.equals(double.class)) {
                return convertToDoublePrimitiveArray((String) fromValue);
            }
            if (toType.equals(Double.class)) {
                return convertToDoubleArray((String) fromValue);
            }
        }

        if (fromValue.getClass().isArray()) {
            if (toType.equals(double.class)) {
                return convertToDoublePrimitiveArray((Object[]) fromValue);
            }
            if (toType.equals(Double.class)) {
                return convertToDoubleArray((Object[]) fromValue);
            }
        }
        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Double) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    private double convertToDouble(Object fromValue) throws Exception {

        if (fromValue instanceof Number) {
            return ((Number) fromValue).doubleValue();

        } else if (fromValue.getClass().isAssignableFrom(boolean.class)) {
            return ((boolean) fromValue) ? 1.0 : 0.0;

        } else if (fromValue.getClass().isAssignableFrom(byte.class)) {
            return (byte) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(int.class)) {
            return (int) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(double.class)) {
            return (double) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(short.class)) {
            return (short) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(float.class)) {
            return (float) fromValue;

        } else if (fromValue.getClass().isAssignableFrom(long.class)) {
            return (long) fromValue;

        } else if (fromValue instanceof Blob) {
            throw new Exception("Unhandled conversion from " + fromValue + "," + fromValue.getClass() + " to Double.");

        } else if (fromValue instanceof Boolean) {
            return ((Boolean) fromValue) ? 1.0 : 0.0;

        } else if (fromValue instanceof Clob) {
            throw new Exception("Unhandled conversion from " + fromValue + "," + fromValue.getClass() + " to Double.");

        } else if (fromValue instanceof String) {
            return Double.parseDouble((String) fromValue);

        }
        throw new Exception("Unhandled conversion from " + fromValue + "," + fromValue.getClass() + " to Double.");
    }

    private Double[] convertToDoubleArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        Double[] toValues = new Double[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToDouble(fromValues[i]);
        }
        return toValues;
    }

    private double[] convertToDoublePrimitiveArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        double[] toValues = new double[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToDouble(fromValues[i]);
        }
        return toValues;
    }

    private Double[] convertToDoubleArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        
        Double[] toValues = new Double[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertToDouble(items[i]);
        }
        return toValues;
    }

    private double[] convertToDoublePrimitiveArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        double[] toValues = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertToDouble(items[i]);
        }
        return toValues;
    }
}
