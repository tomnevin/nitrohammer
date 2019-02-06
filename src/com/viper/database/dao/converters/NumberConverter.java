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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;

// byte
// double
// float
// integer
// long
// short

// boolean
// string

public final class NumberConverter {

    public static final void initialize() {

        registerFrom(Byte.class, NumberConverter::convertNumberToByte);
        registerFrom(byte.class, NumberConverter::convertNumberToByte);

        registerFrom(Double.class, NumberConverter::convertNumberToDouble);
        registerFrom(double.class, NumberConverter::convertNumberToDouble);

        registerFrom(Float.class, NumberConverter::convertNumberToFloat);
        registerFrom(float.class, NumberConverter::convertNumberToFloat);

        registerFrom(Integer.class, NumberConverter::convertNumberToInteger);
        registerFrom(int.class, NumberConverter::convertNumberToInteger);

        registerFrom(Long.class, NumberConverter::convertNumberToLong);
        registerFrom(long.class, NumberConverter::convertNumberToLong);

        registerFrom(Short.class, NumberConverter::convertNumberToShort);
        registerFrom(short.class, NumberConverter::convertNumberToShort);

        registerFrom(BigDecimal.class, NumberConverter::convertNumberToBigDecimal);
        registerFrom(BigInteger.class, NumberConverter::convertNumberToBigInteger);

        registerTo(BigDecimal.class, NumberConverter::convertBigDecimalToNumber);
        registerTo(BigInteger.class, NumberConverter::convertBigIntegerToNumber);

        Converters.register(Boolean.class, Byte.class, NumberConverter::convertBooleanToByte);
        Converters.register(boolean.class, Byte.class, NumberConverter::convertBooleanToByte);
        Converters.register(Boolean.class, byte.class, NumberConverter::convertBooleanToByte);
        Converters.register(boolean.class, byte.class, NumberConverter::convertBooleanToByte);

        Converters.register(Boolean.class, Double.class, NumberConverter::convertBooleanToDouble);
        Converters.register(boolean.class, Double.class, NumberConverter::convertBooleanToDouble);
        Converters.register(Boolean.class, double.class, NumberConverter::convertBooleanToDouble);
        Converters.register(boolean.class, double.class, NumberConverter::convertBooleanToDouble);

        Converters.register(Boolean.class, Float.class, NumberConverter::convertBooleanToFloat);
        Converters.register(boolean.class, Float.class, NumberConverter::convertBooleanToFloat);
        Converters.register(Boolean.class, float.class, NumberConverter::convertBooleanToFloat);
        Converters.register(boolean.class, float.class, NumberConverter::convertBooleanToFloat);

        Converters.register(Boolean.class, Integer.class, NumberConverter::convertBooleanToInteger);
        Converters.register(boolean.class, Integer.class, NumberConverter::convertBooleanToInteger);
        Converters.register(Boolean.class, int.class, NumberConverter::convertBooleanToInteger);
        Converters.register(boolean.class, int.class, NumberConverter::convertBooleanToInteger);

        Converters.register(Boolean.class, Long.class, NumberConverter::convertBooleanToLong);
        Converters.register(boolean.class, Long.class, NumberConverter::convertBooleanToLong);
        Converters.register(Boolean.class, long.class, NumberConverter::convertBooleanToLong);
        Converters.register(boolean.class, long.class, NumberConverter::convertBooleanToLong);

        Converters.register(Boolean.class, Short.class, NumberConverter::convertBooleanToShort);
        Converters.register(boolean.class, Short.class, NumberConverter::convertBooleanToShort);
        Converters.register(Boolean.class, short.class, NumberConverter::convertBooleanToShort);
        Converters.register(boolean.class, short.class, NumberConverter::convertBooleanToShort);

        Converters.register(Byte.class, Boolean.class, NumberConverter::convertByteToBoolean);
        Converters.register(Byte.class, boolean.class, NumberConverter::convertByteToBoolean);
        Converters.register(byte.class, Boolean.class, NumberConverter::convertByteToBoolean);
        Converters.register(byte.class, boolean.class, NumberConverter::convertByteToBoolean);

        Converters.register(Double.class, Boolean.class, NumberConverter::convertDoubleToBoolean);
        Converters.register(Double.class, boolean.class, NumberConverter::convertDoubleToBoolean);
        Converters.register(double.class, Boolean.class, NumberConverter::convertDoubleToBoolean);
        Converters.register(double.class, boolean.class, NumberConverter::convertDoubleToBoolean);

        Converters.register(Float.class, Boolean.class, NumberConverter::convertFloatToBoolean);
        Converters.register(Float.class, boolean.class, NumberConverter::convertFloatToBoolean);
        Converters.register(float.class, Boolean.class, NumberConverter::convertFloatToBoolean);
        Converters.register(float.class, boolean.class, NumberConverter::convertFloatToBoolean);

        Converters.register(Integer.class, Boolean.class, NumberConverter::convertIntegerToBoolean);
        Converters.register(Integer.class, boolean.class, NumberConverter::convertIntegerToBoolean);
        Converters.register(int.class, Boolean.class, NumberConverter::convertIntegerToBoolean);
        Converters.register(int.class, boolean.class, NumberConverter::convertIntegerToBoolean);

        Converters.register(Long.class, Boolean.class, NumberConverter::convertLongToBoolean);
        Converters.register(Long.class, boolean.class, NumberConverter::convertLongToBoolean);
        Converters.register(long.class, Boolean.class, NumberConverter::convertLongToBoolean);
        Converters.register(long.class, boolean.class, NumberConverter::convertLongToBoolean);

        Converters.register(Short.class, Boolean.class, NumberConverter::convertShortToBoolean);
        Converters.register(Short.class, boolean.class, NumberConverter::convertShortToBoolean);
        Converters.register(short.class, Boolean.class, NumberConverter::convertShortToBoolean);
        Converters.register(short.class, boolean.class, NumberConverter::convertShortToBoolean);

        registerFrom(Array.class, ArrayConverter::convertToArrayFromArray);
        registerTo(Array.class, ArrayConverter::convertToArrayFromArray);

    }

    private static final <S> void registerFrom(Class<S> toClazz, ConverterInterface converter) {

        Converters.register(Byte.class, toClazz, converter);
        Converters.register(Double.class, toClazz, converter);
        Converters.register(Float.class, toClazz, converter);
        Converters.register(Integer.class, toClazz, converter);
        Converters.register(Long.class, toClazz, converter);
        Converters.register(Short.class, toClazz, converter);

        Converters.register(byte.class, toClazz, converter);
        Converters.register(double.class, toClazz, converter);
        Converters.register(float.class, toClazz, converter);
        Converters.register(int.class, toClazz, converter);
        Converters.register(long.class, toClazz, converter);
        Converters.register(short.class, toClazz, converter);
    }

    private static final <S> void registerTo(Class<S> fromClazz, ConverterInterface converter) {

        Converters.register(fromClazz, Byte.class, converter);
        Converters.register(fromClazz, Double.class, converter);
        Converters.register(fromClazz, Float.class, converter);
        Converters.register(fromClazz, Integer.class, converter);
        Converters.register(fromClazz, Long.class, converter);
        Converters.register(fromClazz, Short.class, converter);

        Converters.register(fromClazz, byte.class, converter);
        Converters.register(fromClazz, double.class, converter);
        Converters.register(fromClazz, float.class, converter);
        Converters.register(fromClazz, int.class, converter);
        Converters.register(fromClazz, long.class, converter);
        Converters.register(fromClazz, short.class, converter);
    }

    // Number / Byte
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertNumberToByte(Class<T> toType, S fromValue) throws Exception {
        return (T) new Byte(((Number) fromValue).byteValue());
    }

    // Number / Double
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertNumberToDouble(Class<T> toType, S fromValue) throws Exception {
        return (T) new Double(((Number) fromValue).doubleValue());
    }

    // Number / Float
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertNumberToFloat(Class<T> toType, S fromValue) throws Exception {
        return (T) new Float(((Number) fromValue).floatValue());
    }

    // Number / Integer
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertNumberToInteger(Class<T> toType, S fromValue) throws Exception {
        return (T) new Integer(((Number) fromValue).intValue());
    }

    // Number / Long
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertNumberToLong(Class<T> toType, S fromValue) throws Exception {
        return (T) new Long(((Number) fromValue).longValue());
    }

    // Number / Short
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertNumberToShort(Class<T> toType, S fromValue) throws Exception {
        return (T) new Short(((Number) fromValue).shortValue());
    }

    // Number / BigDecimal
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertNumberToBigDecimal(Class<T> toType, S fromValue) throws Exception {
        return (T) new BigDecimal(((Number) fromValue).doubleValue());
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBigDecimalToNumber(Class<T> toType, S fromValue) throws Exception {
        return (T) new Double(((BigDecimal) fromValue).doubleValue());
    }

    // Number / BigInteger
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertNumberToBigInteger(Class<T> toType, S fromValue) throws Exception {
        return (T) BigInteger.valueOf(((Number) fromValue).longValue());
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBigIntegerToNumber(Class<T> toType, S fromValue) throws Exception {
        return (T) new Long(((BigInteger) fromValue).longValue());
    }

    // Byte / Boolean
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBooleanToByte(Class<T> toType, S fromValue) throws Exception {
        return (T) (Byte) (((Boolean) fromValue) ? (byte) 1 : (byte) 0);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertByteToBoolean(Class<T> toType, S fromValue) throws Exception {
        return (T) new Boolean(((Byte) fromValue == 0) ? false : true);
    }

    // Double / Boolean
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBooleanToDouble(Class<T> toType, S fromValue) throws Exception {
        return (T) new Double(((Boolean) fromValue) ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertDoubleToBoolean(Class<T> toType, S fromValue) throws Exception {
        return (T) new Boolean(((Double) fromValue == 0.0) ? false : true);
    }

    // Float / Boolean
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBooleanToFloat(Class<T> toType, S fromValue) throws Exception {
        return (T) new Float(((Boolean) fromValue) ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertFloatToBoolean(Class<T> toType, S fromValue) throws Exception {
        return (T) new Boolean(((Float) fromValue == 0.0) ? false : true);
    }

    // Integer / Boolean
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBooleanToInteger(Class<T> toType, S fromValue) throws Exception {
        return (T) new Integer(((Boolean) fromValue) ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertIntegerToBoolean(Class<T> toType, S fromValue) throws Exception {
        return (T) new Boolean(((Integer) fromValue == null || (Integer) fromValue == 0) ? false : true);
    }

    // Long / Boolean
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBooleanToLong(Class<T> toType, S fromValue) throws Exception {
        return (T) new Long(((Boolean) fromValue) ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertLongToBoolean(Class<T> toType, S fromValue) throws Exception {
        return (T) new Boolean(((Long) fromValue == null || (Long) fromValue == 0) ? false : true);
    }

    // Short / Boolean
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBooleanToShort(Class<T> toType, S fromValue) throws Exception {
        return (T) new Short(((Boolean) fromValue) ? (short) 1 : (short) 0);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertShortToBoolean(Class<T> toType, S fromValue) throws Exception {
        return (T) new Boolean(((Short) fromValue == null || (Short) fromValue == 0) ? false : true);
    }

}
