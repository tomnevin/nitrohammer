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

/**
 * This class defines conversions of fromValue of BigDecimal to any other reasonable type.
 * 
 * @author Tom
 *
 */
public final class BigDecimalConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public BigDecimalConverter(Class<?> defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public Class<?> getDefaultType() {
        return defaultType;
    }

    /**
     * The fromValue is converted from BigDecimal to the desired toType, The fromValue should be
     * check as to being a BigDecimal.
     * 
     * @param toType
     *            the class to which big decimal is to be converted to.
     * 
     * @param fromValue
     *            the value of a BigDecimal which is to be converted to a value of type toType
     *            class.
     */
    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {

        if (fromValue == null) {
            return null;
        }

        if (fromValue.getClass().isArray()) {
            return convertToArray(toType, fromValue);
        }

        // Needed???
        if (toType.equals(BigDecimal.class)) {
            return convertToBigDecimal(fromValue);
        }

        if (!(fromValue instanceof BigDecimal)) {
            throw new Exception("BigDecimalConverter: Unhandled conversion from " + fromValue + " to " + toType + ".");

        } else if (toType.equals(BigDecimal.class)) {
            return fromValue;

        } else if (toType.equals(BigInteger.class)) {
            return ((BigDecimal) fromValue).toBigInteger();

        } else if (toType.equals(Blob.class)) {
            throw new Exception("BigDecimalConverter: Unhandled conversion from " + fromValue + " to " + toType + ".");

        } else if (toType.equals(Clob.class)) {
            throw new Exception("BigDecimalConverter: Unhandled conversion from " + fromValue + " to " + toType + ".");

        } else if (toType.equals(Boolean.class)) {
            return (((BigDecimal) fromValue).doubleValue() == 0.0) ? false : true;

        } else if (toType.equals(Byte.class)) {
            return ((BigDecimal) fromValue).byteValue();

        } else if (toType.equals(Character.class)) {
            return (char) ((BigDecimal) fromValue).byteValue();

        } else if (toType.equals(Double.class)) {
            return ((BigDecimal) fromValue).doubleValue();

        } else if (toType.equals(Float.class)) {
            return ((BigDecimal) fromValue).floatValue();

        } else if (toType.equals(Integer.class)) {
            return ((BigDecimal) fromValue).intValue();

        } else if (toType.equals(Long.class)) {
            return ((BigDecimal) fromValue).longValue();

        } else if (toType.equals(Short.class)) {
            return ((BigDecimal) fromValue).shortValue();

        } else if (toType.equals(String.class)) {
            return ((BigDecimal) fromValue).toString();

        } else {
            throw new Exception("BigDecimalConverter: Unhandled conversion from " + fromValue + " to " + toType + ".");
        }
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof BigDecimal) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("BigDecimalConverter: Unhandled conversion from " + fromValue + " to String.");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }
        if (fromValue instanceof Object[]) {
            Object[] fromValues = (Object[]) fromValue;
            BigDecimal[] toValues = new BigDecimal[fromValues.length];
            for (int i = 0; i < fromValues.length; i++) {
                toValues[i] = convertToBigDecimal(fromValues[i]);
            }
            return toValues;
        }

        throw new Exception("BigDecimalConverter: Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    public BigDecimal convertToBigDecimal(Object fromValue) throws Exception {

        if (fromValue instanceof BigDecimal) {
            return (BigDecimal) fromValue;

        } else if (fromValue instanceof BigInteger) {
            return new BigDecimal((BigInteger) fromValue);

        } else if (fromValue instanceof Blob) {
            return BigDecimal.valueOf((Byte) fromValue);

        } else if (fromValue instanceof Boolean) {
            return BigDecimal.valueOf(((Boolean) fromValue) ? 1.0 : 0.0);

        } else if (fromValue instanceof Byte) {
            return BigDecimal.valueOf((Byte) fromValue);

        } else if (fromValue instanceof Clob) {
            return null;

        } else if (fromValue instanceof Double) {
            return BigDecimal.valueOf((Double) fromValue);

        } else if (fromValue instanceof Integer) {
            return BigDecimal.valueOf((Integer) fromValue);

        } else if (fromValue instanceof Long) {
            return BigDecimal.valueOf((Long) fromValue);

        } else if (fromValue instanceof Short) {
            return BigDecimal.valueOf((Short) fromValue);

        } else if (fromValue instanceof String) {
            return new BigDecimal((String) fromValue);

        } else {
            throw new Exception("BigDecimalConverter: Unhandled conversion from " + fromValue + " to BigDecimal.");
        }
    }
}
