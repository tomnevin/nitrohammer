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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.viper.database.utils.JSONUtil;

public class ConverterUtils {

    public final static String convertToString(Character[] s) {
        if (s == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (Character c : s) {
            buf.append(c);
        }
        return buf.toString();
    }

    public final static String convertToString(Byte[] s) {
        if (s == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (Byte c : s) {
            buf.append(c);
        }
        return buf.toString();
    }

    public final static char[] convertClobToChars(Object source) throws Exception {
        Clob blob = (Clob) source;
        char[] bytes = null;
        Reader r = null;
        try {
            bytes = new char[(int) blob.length()];
            r = blob.getCharacterStream();
            r.read(bytes);
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return bytes;
    }

    public final static byte[] convertBlobToBytes(Object source) throws Exception {
        Blob blob = (Blob) source;
        byte[] bytes = null;
        InputStream r = null;
        try {
            bytes = new byte[(int) blob.length()];
            r = blob.getBinaryStream();
            r.read(bytes);
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return bytes;
    }

    /**
     * 
     * @param fromValue
     * @return the json string
     * @throws Exception
     */

    public final static String convertToString(Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }
        return JSONUtil.toJSON(fromValue);
    }

    public final static Object convertFromString(Class toType, String fromValue) throws Exception {
        if (fromValue == null || toType == null) {
            return null;
        }
        return JSONUtil.fromJSON(toType, fromValue);
    }

    public final static Object convertFromString(Class toType, Reader fromValue) throws Exception {
        if (fromValue == null || toType == null) {
            return null;
        }
        return JSONUtil.fromJSON(toType, fromValue);
    }

    public final static Object convertFromString(Class toType, InputStream fromValue) throws Exception {
        if (fromValue == null || toType == null) {
            return null;
        }
        return JSONUtil.fromJSON(toType, fromValue);
    }

    public final static Object[] convertToArray(Class toType, Reader fromValue) throws Exception {
        if (fromValue == null || toType == null) {
            return null;
        }
        return (Object[]) JSONUtil.fromJSON(toType, fromValue);
    }

    public final static Object[] convertToArray(Class<?> toType, InputStream fromValue) throws Exception {
        if (fromValue == null || toType == null) {
            return null;
        }
        return (Object[]) JSONUtil.fromJSON(toType, fromValue);
    }

    public final static String toHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            // TODO CHeck this, it's mysql put in the drivers.
            return "NULL";
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(String.format("%02X", b));
        }
        return "X'" + buf.toString() + "'";
    }

    public final static String toHex(Byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            // TODO CHeck this, it's mysql put in the drivers.
            return "NULL";
        }
        StringBuilder buf = new StringBuilder();
        for (Byte b : bytes) {
            buf.append(String.format("%02X", b));
        }
        return "X'" + buf.toString() + "'";
    }

    public final static byte[] fromHex(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        return DatatypeConverter.parseHexBinary(value.substring(2, value.length() - 1));
    }

    /**
     * Given a bean, convert the bean to a json string
     * 
     * @param bean
     *            the java bean of the data model.
     * @return the json string representing the bean
     * @note returns null if conversion failed.
     */
    public final static <T> String writeJson(T bean) {
        try {
            return JSONUtil.toJSON(bean);
        } catch (Exception ex) {
            System.err.println("ERROR: failed to translte to string " + bean.getClass() + "," + bean);
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Given a bean, convert the bean to a json string
     * 
     * @param bean
     *            the java bean of the data model.
     * @return the json string representing the bean
     * @note returns null if conversion failed.
     */
    public final static <T> String writeJsonFromArray(T[] bean) {
        try {
            return JSONUtil.toJSON(bean);
        } catch (Exception ex) {
            System.err.println("ERROR: failed to translte to string " + bean.getClass() + "," + bean);
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Given a bean, convert the bean to a JSON string
     * 
     * @param beans
     *            the Java bean of the data model.
     * @return the JSON string representing the bean
     * @note returns null if conversion failed.
     */
    public final static <T> String writeJsonFromList(Collection<T> beans) {
        try {
            return JSONUtil.toJSON(beans);
        } catch (Exception ex) {
            System.err.println("ERROR: failed to translte to string " + beans.getClass() + "," + beans);
        }
        return null;
    }

    /**
     * Given a map of beans, convert the beans to a json string
     * 
     * @param map
     *            the map of beans.
     * @return the json string representing the map of beans.
     * @note returns null if conversion failed.
     */
    public final static String writeJsonFromMap(Map<String, Object> map) {
        try {
            return JSONUtil.toJSON(map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Given a json string, convert to java bean of type clazz.
     * 
     * @param json
     *            the json string with which to populate a java bean
     * @param clazz
     *            the bean clazz for mapping json to
     * @return the populated java bean.
     * @note returns null if conversion failed.
     */
    public final static <T> T readJson(String json, Class<T> clazz) {
        try {
            if (json != null && clazz != null) {
                return JSONUtil.fromJSON(clazz, json);
            }
        } catch (Exception ex) {
            System.err.println("readJson: Failed to convert JSON to bean: " + json);
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Given a json string, convert to java list of beans of type clazz.
     * 
     * @param json
     *            the json string with which to populate a java list of beans
     * @param clazz
     *            the bean clazz for mapping json to
     * @return the populated java list of beans.
     * @note returns null if conversion failed.
     */
    public final static <T> List<T> readJsonToList(String json, Class<T> clazz) {
        try {
            if (json != null && json.length() > 0) {
                StringReader reader = new StringReader(json);
                return (List<T>) JSONUtil.fromJSON(clazz, reader);
            }
        } catch (Exception ex) {
            System.err.println("readJsonToList: Failed to convert JSON to bean: " + json);
            ex.printStackTrace();
        }
        return new ArrayList<T>();
    }

    /**
     * Give a list of json strings, and the bean mapper class, convert each item in the list of json
     * strings to a bean object. This is a convenience function for processing many separate json
     * strings.
     * 
     * @param items
     *            the list of json strings
     * @param clazz
     *            the bean object for mapping json to
     * @return the list of bean objects mapped from the json strings.
     */

    public final static <T> List<T> readJsonListToList(List<String> items, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        for (String item : items) {
            list.add(readJson(item, clazz));
        }
        return list;
    }

    /**
     * Given a json string, convert to java map of beans of type clazz.
     * 
     * @param json
     *            the json string with which to populate a java map of beans
     * @return the populated java map of beans.
     * @note returns null if conversion failed.
     */
    public final static Map<String, Object> readJsonToMap(String json) {
        try {
            return JSONUtil.fromJSON(Map.class, json);
        } catch (Exception ex) {
            System.err.println("readJsonToList: Failed to convert JSON to map: " + json);
            ex.printStackTrace();
        }
        return new HashMap<String, Object>();
    }

    public static Object[] createArrayFromArrayObject(Object o) throws Exception {
        if (!o.getClass().isArray())
            throw new Exception("parameter is not an array");

        if (!o.getClass().getComponentType().isPrimitive())
            return (Object[]) o;

        int element_count = Array.getLength(o);
        Object elements[] = new Object[element_count];

        for (int i = 0; i < element_count; i++) {
            elements[i] = Array.get(o, i);
        }

        return elements;
    }

    public static final String[] toArray(String str) {
        String str1 = ConverterUtils.removeBrackets(str);
        return str1.split("\\s*(,)\\s*");
    }

    public static final String removeBrackets(String str) {

        int index = str.indexOf('[');
        if (index != -1) {
            str = str.substring(index + 1);
        }
        index = str.lastIndexOf(']');
        if (index != -1) {
            str = str.substring(0, index);
        }
        return str;
    }
}
