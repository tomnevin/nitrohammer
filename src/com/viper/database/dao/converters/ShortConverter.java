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

public final class ShortConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public ShortConverter(Class<?> defaultType) {
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

        if (toType.equals(Short.class)) {
            return convertToShort(fromValue);
        }

        if (toType.equals(short.class)) {
            return convertToShort(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            if (toType.equals(short.class)) {
                return convertToShortPrimitiveArray((String) fromValue);
            }
            if (toType.equals(Short.class)) {
                return convertToShortArray((String) fromValue);
            }
        }

        if (fromValue.getClass().isArray()) {
            if (toType.equals(short.class)) {
                return convertToShortPrimitiveArray((Object[]) fromValue);
            }

            if (toType.equals(Short.class)) {
                return convertToShortArray((Object[]) fromValue);
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

    public short convertToShort(Object fromValue) throws Exception {

        if (fromValue instanceof Number) {
            return ((Number) fromValue).shortValue();

        } else if (fromValue instanceof Blob) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Short.");

        } else if (fromValue instanceof Boolean) {
            return ((Boolean) fromValue) ? (short) 1 : (short) 0;

        } else if (fromValue instanceof Clob) {
            throw new Exception("Unhandled conversion from " + fromValue + " to Short.");

        } else if (fromValue instanceof String) {
            return Short.parseShort((String) fromValue);

        }
        throw new Exception("Unhandled conversion from " + fromValue + " to Short.");

    }

    public Short[] convertToShortArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        Short[] toValues = new Short[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToShort(fromValues[i]);
        }
        return toValues;
    }

    public short[] convertToShortPrimitiveArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        short[] toValues = new short[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToShort(fromValues[i]);
        }
        return toValues;
    }

    public Short[] convertToShortArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        Short[] toValues = new Short[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertToShort(items[i]);
        }
        return toValues;
    }

    public short[] convertToShortPrimitiveArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] =  ConverterUtils.toArray(fromValues);
        short[] toValues = new short[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertToShort(items[i]);
        }
        return toValues;
    }
}
