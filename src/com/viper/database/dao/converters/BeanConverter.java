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
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.viper.database.utils.JSONUtil;

public final class BeanConverter {

    public static final void initialize() {

        Converters.register(Object.class, String.class, BeanConverter::convertBeanToString);
        Converters.register(String.class, Object.class, BeanConverter::convertStringToBean);
        Converters.register(char[].class, Object.class, BeanConverter::convertCharsToBean);
        Converters.register(Character[].class, Object.class, BeanConverter::convertCharacterArrayToBean);
        Converters.register(byte[].class, Object.class, BeanConverter::convertBytesToBean);
        Converters.register(Byte[].class, Object.class, BeanConverter::convertBytesArrayToBean);
        Converters.register(Clob.class, Object.class, BeanConverter::convertClobToBean);
        Converters.register(Blob.class, Object.class, BeanConverter::convertBlobToBean);

        Converters.register(String.class, Object[].class, BeanConverter::convertStringToBeanArray);
        Converters.register(char[].class, Object[].class, BeanConverter::convertCharsToBeanArray);
        Converters.register(Character[].class, Object[].class, BeanConverter::convertCharactersToBeanArray);
        Converters.register(byte[].class, Object[].class, BeanConverter::convertBytesToBeanArray);
        Converters.register(Byte[].class, Object[].class, BeanConverter::convertByteArrayToBeanArray);
        Converters.register(Clob[].class, Object.class, BeanConverter::convertClobToBeanArray);
        Converters.register(Blob[].class, Object.class, BeanConverter::convertBlobToBeanArray);

        Converters.register(ArrayList.class, String.class, BeanConverter::convertListToString);
        Converters.register(List.class, String.class, BeanConverter::convertListToString);
        Converters.register(HashMap.class, String.class, BeanConverter::convertMapToString);
        Converters.register(Map.class, String.class, BeanConverter::convertMapToString);
        Converters.register(Array.class, String.class, BeanConverter::convertArrayToString);

        Converters.register(String.class, ArrayList.class, BeanConverter::convertStringToList);
        Converters.register(String.class, List.class, BeanConverter::convertStringToList);
        Converters.register(String.class, HashMap.class, BeanConverter::convertStringToMap);
        Converters.register(String.class, Map.class, BeanConverter::convertStringToMap);
        Converters.register(String.class, Array.class, BeanConverter::convertStringToArray);

    }

    public static final <T, S> T convertBeanToString(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(JSONUtil.toJSON(fromValue));
    }

    public static final <T, S> T convertStringToBean(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSON(toType, (String) fromValue);
    }

    public static final <T, S> T convertCharsToBean(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSON(toType, new String((char[]) fromValue));
    }

    public static final <T, S> T convertCharacterArrayToBean(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSON(toType, ConverterUtils.convertToString((Character[]) fromValue));
    }

    public static final <T, S> T convertBytesToBean(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSON(toType, new String((byte[]) fromValue));
    }

    public static final <T, S> T convertBytesArrayToBean(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSON(toType, ConverterUtils.convertToString((Byte[]) fromValue));
    }

    public static final <T, S> T convertClobToBean(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSON(toType, new String(ConverterUtils.convertClobToChars(fromValue)));
    }

    public static final <T, S> T convertBlobToBean(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSON(toType, new String(ConverterUtils.convertBlobToBytes(fromValue)));
    }

    // Arrays
    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToBeanArray(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSONArray(toType, (String) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertCharsToBeanArray(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSONArray(toType, new String((char[]) fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertCharactersToBeanArray(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSONArray(toType, ConverterUtils.convertToString((Character[]) fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBytesToBeanArray(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSONArray(toType, new String((byte[]) fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertByteArrayToBeanArray(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSONArray(toType, ConverterUtils.convertToString((Byte[]) fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertClobToBeanArray(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSONArray(toType, new String(ConverterUtils.convertClobToChars(fromValue)));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBlobToBeanArray(Class<T> toType, S fromValue) throws Exception {
        return JSONUtil.fromJSONArray(toType, new String(ConverterUtils.convertBlobToBytes(fromValue)));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertListToString(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(JSONUtil.toJSON((List) fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertArrayToString(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(JSONUtil.toJSON(fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToList(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(JSONUtil.fromJSONList(toType, (String) fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToArray(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(JSONUtil.fromJSONArray(toType, (String) fromValue));
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertMapToString(Class<T> toType, S fromValue) throws Exception {
        return (T) JSONUtil.toJSON(fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToMap(Class<T> toType, S fromValue) throws Exception {
        return (T) JSONUtil.fromJSON(Map.class, (String)fromValue); 
    }
}
