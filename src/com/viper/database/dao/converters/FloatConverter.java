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

public final class FloatConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public FloatConverter(Class<?> defaultType) {
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

        if (fromValue instanceof String) {
            if (toType.equals(float[].class)) {
                return convertToFloatPrimitiveArray((String) fromValue);
            }

            if (toType.equals(Float[].class)) {
                return convertToFloatArray((String) fromValue);
            }
        }

        if (toType.equals(Float.class)) {
            return convertToFloat(fromValue);
        }

        if (toType.equals(float.class)) {
            return convertToFloat(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            if (toType.equals(float.class)) {
                return convertToFloatPrimitiveArray((String) fromValue);
            }
            if (toType.equals(Float.class)) {
                return convertToFloatArray((String) fromValue);
            }
        }

        if (fromValue.getClass().isArray()) {
            if (toType.equals(float[].class)) {
                return convertToFloatPrimitiveArray((Object[]) fromValue);
            }
            if (toType.equals(Float[].class)) {
                return convertToFloatArray((Object[]) fromValue);
            }
        }
        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Float) {
            new DecimalFormat(qualifiers[0]).format((Float) fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    public Float convertToFloat(Object fromValue) throws Exception {

        if (fromValue instanceof Number) {
            return ((Number) fromValue).floatValue();

        } else if (fromValue instanceof Blob) {
            return null; // TODO

        } else if (fromValue instanceof Boolean) {
            return ((Boolean) fromValue) ? 1.0F : 0.0F;

        } else if (fromValue instanceof Clob) {
            return null; // TODO

        } else if (fromValue instanceof String) {
            return Float.parseFloat((String) fromValue);

        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to Float.");
        }
    }

    public Float[] convertToFloatArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        Float[] toValues = new Float[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToFloat(fromValues[i]);
        }
        return toValues;
    }

    public float[] convertToFloatPrimitiveArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        float[] toValues = new float[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = (float) convertToFloat(fromValues[i]);
        }
        return toValues;
    }

    public Float[] convertToFloatArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        Float[] toValues = new Float[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertToFloat(items[i]);
        }
        return toValues;
    }

    public float[] convertToFloatPrimitiveArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        float[] toValues = new float[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = (float) convertToFloat(items[i]);
        }
        return toValues;
    }
}
