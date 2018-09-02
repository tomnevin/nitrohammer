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

public final class BigIntegerConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public BigIntegerConverter(Class<?> defaultType) {
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

        // Needed???
        if (toType.equals(BigInteger.class)) {
            return convertToBigInteger(fromValue);
        }

        if (!(fromValue instanceof BigInteger)) {
            throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");

        } else if (toType.equals(BigInteger.class)) {
            return fromValue;

        } else if (toType.equals(BigDecimal.class)) {
            return new BigDecimal((BigInteger) fromValue);

        } else if (toType.equals(Blob.class)) {
            throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");

        } else if (toType.equals(Clob.class)) {
            throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");

        } else if (toType.equals(Boolean.class)) {
            return (((BigInteger) fromValue).doubleValue() == 0.0) ? false : true;

        } else if (toType.equals(Byte.class)) {
            return ((BigInteger) fromValue).byteValue();

        } else if (toType.equals(Character.class)) {
            return (char) ((BigInteger) fromValue).byteValue();

        } else if (toType.equals(Double.class)) {
            return ((BigInteger) fromValue).doubleValue();

        } else if (toType.equals(Float.class)) {
            return ((BigInteger) fromValue).floatValue();

        } else if (toType.equals(Integer.class)) {
            return ((BigInteger) fromValue).intValue();

        } else if (toType.equals(Long.class)) {
            return ((BigInteger) fromValue).longValue();

        } else if (toType.equals(Short.class)) {
            return ((BigInteger) fromValue).shortValue();

        } else if (toType.equals(String.class)) {
            return ((BigInteger) fromValue).toString();

        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
        }
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof BigInteger) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }
        if (fromValue instanceof Object[]) {
            Object[] fromValues = (Object[]) fromValue;
            BigInteger[] toValues = new BigInteger[fromValues.length];
            for (int i = 0; i < fromValues.length; i++) {
                toValues[i] = convertToBigInteger(fromValues[i]);
            }
            return toValues;
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    public BigInteger convertToBigInteger(Object fromValue) throws Exception {

        if (fromValue instanceof Number) {
            return BigInteger.valueOf(((Number) fromValue).longValue());

        } else if (fromValue instanceof Blob) {
            return BigInteger.valueOf((Byte) fromValue);

        } else if (fromValue instanceof Boolean) {
            return BigInteger.valueOf(((Boolean) fromValue) ? 1L : 0L);

        } else if (fromValue instanceof Clob) {
            return null;

        } else if (fromValue instanceof String) {
            return new BigInteger((String) fromValue);

        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to BigInteger.");
        }
    }
}
