/*
 * -----------------------------------------------------------------------------
 *                      VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * MIT License
 * 
 * Copyright (c) #{classname}.html #{util.YYYY()} Viper Software Services
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

package com.viper.database.utils;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;
import org.apache.johnzon.mapper.reflection.JohnzonParameterizedType;

public class JSONUtil {

    private final static Mapper mapper = new MapperBuilder().build();

    public final static Class registerProvider() {
        return JohnzonProvider.class;
    }

    public final static <T> T fromJSON(Class<T> clazz, String str) throws Exception {
        try {
            return mapper.readObject(str, clazz);
        } catch (Exception ex) {
            System.out.println("ERROR: fromJSON: " + str);
            throw ex;
        }
    }

    public final static <T> T fromJSON(Class<T> clazz, Reader reader) throws Exception {
        try {
            return mapper.readObject(reader, clazz);
        } catch (Exception ex) {
            System.out.println("ERROR: fromJSON: " + clazz);
            throw ex;
        }
    }

    public final static <T> T fromJSON(Class<T> clazz, InputStream reader) throws Exception {
        try {
            return mapper.readObject(reader, clazz);
        } catch (Exception ex) {
            System.out.println("ERROR: fromJSON: " + clazz);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    public final static <T> T fromJSONArray(Class<T> clazz, String str) throws Exception {
        try {
            return (T) mapper.readArray(new StringReader((String) str), clazz.getComponentType());
        } catch (Exception ex) {
            System.out.println("ERROR: fromJSONArray: " + str);
            throw ex;
        }
    }

    public final static <T> List<T> fromJSONList(Class<T> clazz, String str) throws Exception {
        try {

            StringReader reader = new StringReader((String) str);
            JohnzonParameterizedType parameterizedType = new JohnzonParameterizedType(List.class, clazz);
            return new ArrayList<T>(mapper.readCollection(reader, parameterizedType));
        } catch (Exception ex) {
            System.out.println("ERROR: fromJSONList: " + str);
            throw ex;
        }
    }

    public final static <T> String toJSON(T bean) throws Exception {
        try {
            return mapper.writeObjectAsString(bean);
        } catch (Exception ex) {
            System.out.println("ERROR: toJSON: " + bean.getClass().getName());
            throw ex;
        }
    }

    public final static <T> String toJSON(Collection<T> beans) throws Exception {
        try {
            return mapper.writeArrayAsString(beans);
        } catch (Exception ex) {
            System.out.println("ERROR: toJSON: " + beans);
            throw ex;
        }
    }
}
