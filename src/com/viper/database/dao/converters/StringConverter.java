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
import java.nio.charset.Charset;

public final class StringConverter {

    private static final String DELIMITER = ",";

    public static final void initialize() {

        Converters.register(String.class, BigDecimal.class, StringConverter::convertStringToBigDecimal);
        Converters.register(String.class, BigInteger.class, StringConverter::convertStringToBigInteger);
        Converters.register(String.class, Boolean.class, StringConverter::convertStringToBoolean);
        Converters.register(String.class, boolean.class, StringConverter::convertStringToBoolean);
        Converters.register(String.class, Byte.class, StringConverter::convertStringToByte);
        Converters.register(String.class, byte.class, StringConverter::convertStringToByte);
        Converters.register(String.class, Character.class, StringConverter::convertStringToCharacter);
        Converters.register(String.class, char.class, StringConverter::convertStringToChar);
        Converters.register(String.class, Double.class, StringConverter::convertStringToDouble);
        Converters.register(String.class, double.class, StringConverter::convertStringToDouble);
        Converters.register(String.class, Float.class, StringConverter::convertStringToFloat);
        Converters.register(String.class, float.class, StringConverter::convertStringToFloat);
        Converters.register(String.class, Integer.class, StringConverter::convertStringToInteger);
        Converters.register(String.class, int.class, StringConverter::convertStringToInteger);
        Converters.register(String.class, Long.class, StringConverter::convertStringToLong);
        Converters.register(String.class, long.class, StringConverter::convertStringToLong);
        Converters.register(String.class, Short.class, StringConverter::convertStringToShort);
        Converters.register(String.class, short.class, StringConverter::convertStringToShort);

        Converters.register(BigDecimal.class, String.class, StringConverter::convertBigDecimalToString);
        Converters.register(BigInteger.class, String.class, StringConverter::convertBigIntegerToString);
        Converters.register(Boolean.class, String.class, StringConverter::convertBooleanToString);
        Converters.register(boolean.class, String.class, StringConverter::convertBooleanToString);
        Converters.register(Byte.class, String.class, StringConverter::convertByteToString);
        Converters.register(byte.class, String.class, StringConverter::convertByteToString);
        Converters.register(Character.class, String.class, StringConverter::convertCharacterToString);
        Converters.register(char.class, String.class, StringConverter::convertCharToString);
        Converters.register(Double.class, String.class, StringConverter::convertDoubleToString);
        Converters.register(double.class, String.class, StringConverter::convertDoubleToString);
        Converters.register(Float.class, String.class, StringConverter::convertFloatToString);
        Converters.register(float.class, String.class, StringConverter::convertFloatToString);
        Converters.register(Integer.class, String.class, StringConverter::convertIntegerToString);
        Converters.register(int.class, String.class, StringConverter::convertIntegerToString);
        Converters.register(Long.class, String.class, StringConverter::convertLongToString);
        Converters.register(long.class, String.class, StringConverter::convertLongToString);
        Converters.register(Short.class, String.class, StringConverter::convertShortToString);
        Converters.register(short.class, String.class, StringConverter::convertShortToString);

        Converters.register(boolean[].class, String.class, StringConverter::convertbooleanArrayToString);
        Converters.register(Boolean[].class, String.class, StringConverter::convertBooleanArrayToString);
        Converters.register(byte[].class, String.class, StringConverter::convertbyteArrayToString);
        Converters.register(Byte[].class, String.class, StringConverter::convertByteArrayToString);
        Converters.register(char[].class, String.class, StringConverter::convertCharArrayToString);
        Converters.register(Character[].class, String.class, StringConverter::convertCharacterArrayToString);
        Converters.register(double[].class, String.class, StringConverter::convertdoubleArrayToString);
        Converters.register(Double[].class, String.class, StringConverter::convertDoubleArrayToString);
        Converters.register(float[].class, String.class, StringConverter::convertfloatArrayToString);
        Converters.register(Float[].class, String.class, StringConverter::convertFloatArrayToString);
        Converters.register(int[].class, String.class, StringConverter::convertIntArrayToString);
        Converters.register(Integer[].class, String.class, StringConverter::convertIntegerArrayToString);
        Converters.register(long[].class, String.class, StringConverter::convertlongArrayToString);
        Converters.register(Long[].class, String.class, StringConverter::convertLongArrayToString);
        Converters.register(short[].class, String.class, StringConverter::convertshortArrayToString);
        Converters.register(Short[].class, String.class, StringConverter::convertShortArrayToString);

        Converters.register(String.class, boolean[].class, StringConverter::convertStringTobooleanArray);
        Converters.register(String.class, Boolean[].class, StringConverter::convertStringToBooleanArray);
        Converters.register(String.class, byte[].class, StringConverter::convertStringTobyteArray);
        Converters.register(String.class, Byte[].class, StringConverter::convertStringToByteArray);
        Converters.register(String.class, char[].class, StringConverter::convertStringToCharArray);
        Converters.register(String.class, Character[].class, StringConverter::convertStringToCharacterArray);
        Converters.register(String.class, double[].class, StringConverter::convertStringTodoubleArray);
        Converters.register(String.class, Double[].class, StringConverter::convertStringToDoubleArray);
        Converters.register(String.class, float[].class, StringConverter::convertStringTofloatArray);
        Converters.register(String.class, Float[].class, StringConverter::convertStringToFloatArray);
        Converters.register(String.class, int[].class, StringConverter::convertStringToIntArray);
        Converters.register(String.class, Integer[].class, StringConverter::convertStringToIntegerArray);
        Converters.register(String.class, long[].class, StringConverter::convertStringTolongArray);
        Converters.register(String.class, Long[].class, StringConverter::convertStringToLongArray);
        Converters.register(String.class, short[].class, StringConverter::convertStringToshortArray);
        Converters.register(String.class, Short[].class, StringConverter::convertStringToShortArray);

        Converters.register(String.class, String.class, StringConverter::convertStringToString);
        Converters.register(String.class, String[].class, StringConverter::convertStringToStringArray);
        Converters.register(String[].class, String.class, StringConverter::convertStringArrayToString);
    }

    // String / BigDecimal
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBigDecimalToString(Class<T> toType, S fromValue) throws Exception {
        return (T) Double.toString(((BigDecimal) fromValue).doubleValue());
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToBigDecimal(Class<T> toType, S fromValue) throws Exception {
        return (T) new BigDecimal((String) fromValue);
    }

    // String / BigInteger
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBigIntegerToString(Class<T> toType, S fromValue) throws Exception {
        return (T) Long.toString(((BigInteger) fromValue).longValue());
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToBigInteger(Class<T> toType, S fromValue) throws Exception {
        return (T) new BigInteger((String) fromValue);
    }

    // String / Boolean
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToBoolean(Class<T> toType, S fromValue) throws Exception {
        return (T) new Boolean(Boolean.parseBoolean((String) fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBooleanToString(Class<T> toType, S fromValue) throws Exception {
        return (T) Boolean.toString((Boolean) fromValue);
    }

    // String / char / Character
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertCharToString(Class<T> toType, S fromValue) throws Exception {
        return (T) ("" + fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToChar(Class<T> toType, S fromValue) throws Exception {
        return (T) new Character(("" + fromValue).charAt(0));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertCharacterToString(Class<T> toType, S fromValue) throws Exception {
        return (T) ("" + fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToCharacter(Class<T> toType, S fromValue) throws Exception {
        if (fromValue == null || ((String) fromValue).length() == 0) {
            return null;
        }
        return (T) new Character(("" + fromValue).charAt(0));
    }

    // String / Byte
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToByte(Class<T> toType, S fromValue) throws Exception {
        return (T) Byte.valueOf((byte) (Integer.valueOf((String) fromValue, 16) & 0xFF));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertByteToString(Class<T> toType, S fromValue) throws Exception {
        return (T) String.format("%02X", (Byte) fromValue);
    }

    // String / Double
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToDouble(Class<T> toType, S fromValue) throws Exception {
        return (T) toDouble(fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertDoubleToString(Class<T> toType, S fromValue) throws Exception {
        return (T) Double.toString((Double) fromValue);
    }

    // String / Float
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToFloat(Class<T> toType, S fromValue) throws Exception {
        return (T) toFloat(fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertFloatToString(Class<T> toType, S fromValue) throws Exception {
        return (T) Float.toString((Float) fromValue);
    }

    // String / Integer
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToInteger(Class<T> toType, S fromValue) throws Exception {
        return (T) toInt(fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertIntegerToString(Class<T> toType, S fromValue) throws Exception {
        return (T) Integer.toString((Integer) fromValue);
    }

    // String / Long
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToLong(Class<T> toType, S fromValue) throws Exception {
        return (T) toLong(fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertLongToString(Class<T> toType, S fromValue) throws Exception {
        return (T) Long.toString((Long) fromValue);
    }

    // String / Short
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToShort(Class<T> toType, S fromValue) throws Exception {
        return (T) toShort(fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertShortToString(Class<T> toType, S fromValue) throws Exception {
        return (T) Short.toString((Short) fromValue);
    }

    // String / String
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToString(Class<T> toType, S fromValue) throws Exception {
        return (T) fromValue;
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToStringArray(Class<T> toType, S fromValue) throws Exception {
        return (T) ((String) fromValue).split("\\s*(" + DELIMITER + ")\\s*");
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) String.join(DELIMITER, (String[]) fromValue);
    }

    // String / boolean array
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBooleanArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((Boolean[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertbooleanArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((boolean[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToBooleanArray(Class<T> toType, S fromValue) throws Exception {
        return (T) convertStringToArray(Boolean.class, (String) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringTobooleanArray(Class<T> toType, S fromValue) throws Exception {
        String[] items = ((String) fromValue).split("\\s*(" + DELIMITER + ")\\s*");
        boolean[] toValues = new boolean[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = Converters.convert(boolean.class, items[i]);
        }
        return (T) toValues;
    }

    // String / byte array
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertByteArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) new String(toBytes((Byte[]) fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertbyteArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) new String((byte[]) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToByteArray(Class<T> toType, S fromValue) throws Exception {
        return (T) toByteObjects(((String) fromValue).getBytes(Charset.forName("UTF-8")));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringTobyteArray(Class<T> toType, S fromValue) throws Exception {
        return (T) ((String) fromValue).getBytes(Charset.forName("UTF-8"));
    }

    // String / char array
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertCharacterArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((Character[]) fromValue, "");
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertCharArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) new String((char[]) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToCharacterArray(Class<T> toType, S fromValue) throws Exception {
        String str = (String) fromValue;
        Character[] toValues = new Character[str.length()];
        for (int i = 0; i < str.length(); i++) {
            toValues[i] = str.charAt(i);
        }
        return (T) toValues;
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToCharArray(Class<T> toType, S fromValue) throws Exception {
        return (T) ((String) fromValue).toCharArray();
    }

    // String / double array
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertDoubleArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((Double[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertdoubleArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((double[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToDoubleArray(Class<T> toType, S fromValue) throws Exception {
        return (T) convertStringToArray(Double.class, (String) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringTodoubleArray(Class<T> toType, S fromValue) throws Exception {
        String[] items = ((String) fromValue).split("\\s*\\(|[" + DELIMITER + "]|\\)\\s*");
        double[] toValues = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = toDouble(items[i]);
        }
        return (T) toValues;
    }

    // String / float array
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertFloatArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((Float[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertfloatArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((float[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToFloatArray(Class<T> toType, S fromValue) throws Exception {
        return (T) convertStringToArray(Float.class, (String) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringTofloatArray(Class<T> toType, S fromValue) throws Exception {
        String[] items = ((String) fromValue).split("\\s*(" + DELIMITER + ")\\s*");
        float[] toValues = new float[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = toFloat(items[i]);
        }
        return (T) toValues;
    }

    // String integer array
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertIntegerArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((Integer[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertIntArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((int[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToIntegerArray(Class<T> toType, S fromValue) throws Exception {
        return (T) convertStringToArray(Integer.class, (String) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToIntArray(Class<T> toType, S fromValue) throws Exception {
        String[] items = ((String) fromValue).split("\\s*(" + DELIMITER + ")\\s*");
        int[] toValues = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = toInt(items[i]);
        }
        return (T) toValues;
    }

    // String / long array
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertLongArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((Long[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertlongArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((long[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToLongArray(Class<T> toType, S fromValue) throws Exception {
        return (T) convertStringToArray(Long.class, (String) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringTolongArray(Class<T> toType, S fromValue) throws Exception {
        String[] items = ((String) fromValue).split("\\s*(" + DELIMITER + ")\\s*");
        long[] toValues = new long[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = toLong(items[i]);
        }
        return (T) toValues;
    }

    // String / short array
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertShortArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((Short[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertshortArrayToString(Class<T> toType, S fromValue) throws Exception {
        return (T) convertArrayToString((short[]) fromValue, DELIMITER);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToShortArray(Class<T> toType, S fromValue) throws Exception {
        return (T) convertStringToArray(Short.class, (String) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToshortArray(Class<T> toType, S fromValue) throws Exception {
        String[] items = ((String) fromValue).split("\\s*(" + DELIMITER + ")\\s*");
        short[] toValues = new short[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = toShort(items[i]);
        }
        return (T) toValues;
    }

    private static final String convertArrayToString(Object[] fromValues, String delimiter) throws Exception {

        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (Object fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                String item = Converters.convert(String.class, fromValue);
                buf.append(item);
            }
        }
        return buf.toString();
    }

    private static final String convertArrayToString(boolean[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (boolean fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(Converters.convert(String.class, fromValue));
            }
        }
        return buf.toString();
    }

    private static final String convertArrayToString(short[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (short fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(Converters.convert(String.class, fromValue));
            }
        }
        return buf.toString();
    }

    private static final String convertArrayToString(int[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (int fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(Converters.convert(String.class, fromValue));
            }
        }
        return buf.toString();
    }

    private static final String convertArrayToString(long[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (long fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(Converters.convert(String.class, fromValue));
            }
        }
        return buf.toString();
    }

    private static final String convertArrayToString(float[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (float fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(Converters.convert(String.class, fromValue));
            }
        }
        return buf.toString();
    }

    private static final String convertArrayToString(double[] fromValues, String delimiter) throws Exception {
        StringBuilder buf = new StringBuilder();
        if (fromValues != null) {
            for (double fromValue : fromValues) {
                if (buf.length() > 0) {
                    buf.append(delimiter);
                }
                buf.append(Converters.convert(String.class, fromValue));
            }
        }
        return buf.toString();
    }

    @SuppressWarnings("unchecked")
    private static final <T> T[] convertStringToArray(Class<T> toClass, String fromValues) throws Exception {
        String[] items = fromValues.split("\\s*(" + DELIMITER + ")\\s*");
        T[] toValues = (T[]) Array.newInstance(toClass, items.length);
        for (int i = 0; i < items.length; i++) {
            toValues[i] = Converters.convert(toClass, items[i]);
        }
        return toValues;
    }

    // byte[] to Byte[]
    private static final Byte[] toByteObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) {
            bytes[i++] = b;
        }
        return bytes;

    }

    // Byte[] to byte[]
    private static final byte[] toBytes(Byte[] bytesObject) {
        byte[] bytes = new byte[bytesObject.length];
        int i = 0;
        for (Byte b : bytesObject) {
            bytes[i++] = b;
        }
        return bytes;
    }

    private static final String toHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "NULL";
        }
        StringBuilder buf = new StringBuilder();
        // buf.append("0x");
        for (byte b : bytes) {
            buf.append(String.format("%02X", b));
        }
        return "X'" + buf.toString() + "'";
    }

    private static final <S> Integer toInt(S value) {
        if (isEmpty(value)) {
            return 0;
        }
        String str = ((String) value).replace('[', ' ').replace(']', ' ');
        return Integer.valueOf(Integer.parseInt(str.trim()));
    }

    private static final <S> Short toShort(S value) {
        if (isEmpty(value)) {
            return 0;
        }
        String str = ((String) value).replace('[', ' ').replace(']', ' ');
        return Short.valueOf(Short.parseShort(str.trim()));
    }

    private static final <S> Long toLong(S value) {
        if (isEmpty(value)) {
            return 0L;
        }
        String str = ((String) value).replace('[', ' ').replace(']', ' ');
        return Long.valueOf(Long.parseLong(str.trim()));
    }

    private static final <S> Float toFloat(S value) {
        if (isEmpty(value)) {
            return 0.0F;
        }
        String str = ((String) value).replace('[', ' ').replace(']', ' ');
        return Float.valueOf(Float.parseFloat(str.trim()));
    }

    private static final <S> Double toDouble(S value) {
        if (isEmpty(value)) {
            return 0.0;
        }
        String str = ((String) value).replace('[', ' ').replace(']', ' ');
        return Double.valueOf(Double.parseDouble(str.trim()));
    }

    private static final <S> boolean isEmpty(S value) {
        return ((String) value == null || ((String) value).trim().isEmpty());
    }
}
