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

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;

import com.viper.database.annotations.Column;

public final class ListConverter implements ConverterInterface {

    private final static Mapper mapper = new MapperBuilder().build();

    @Override
    public Class<?> getDefaultType() {
        return List.class;
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof List) {
            return mapper.writeArrayAsString((List) fromValue);
        } else if (fromValue.getClass().isArray()) {
            // return mapper.writeArrayAsString( fromValue);
            throw new Exception("ERROR : can't implement connversion to String from " + fromValue.getClass());
        }
        return mapper.writeObjectAsString(fromValue);
    }

    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {

        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            StringReader reader = new StringReader((String) fromValue);
            return Arrays.asList(mapper.readArray(reader, toType));
        }
        
        if (fromValue instanceof java.util.List) {
            return fromValue;
        }

        throw new Exception("ERROR : can't implement connversion to " + toType + " from " + fromValue.getClass());
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            StringReader reader = new StringReader((String) fromValue);
            return mapper.readArray(reader, toType);
        }

        throw new Exception("ERROR : can't implement connversion to " + toType + " from " + fromValue.getClass());
    }

    public <T> List<T> convertToList(Class<T> toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            StringReader reader = new StringReader((String) fromValue);
            return Arrays.asList(mapper.readArray(reader, toType));
        }

        throw new Exception(
                "ERROR : can't implement connversion to " + toType + " from " + fromValue.getClass() + ":" + fromValue);
    }
}
