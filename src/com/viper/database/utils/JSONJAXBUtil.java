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

import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import org.glassfish.jersey.jackson.JacksonFeature;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class JSONJAXBUtil {

    private static ObjectMapper mapper = null;

    private final static void initialize() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
            mapper.setAnnotationIntrospector(introspector);
        }
    }

    public final static Class registerProvider() {
        return JacksonFeature.class;
    }

    public final static <T> T fromJSON(Class<T> clazz, String str) throws Exception {

        initialize();

        return mapper.readValue(str, clazz);
    }

    public final static <T> T fromJSONArray(Class<T> clazz, String str) throws Exception {

        initialize();

        ArrayType javaType = mapper.getTypeFactory().constructArrayType(clazz);

        return (T) mapper.readValue(new StringReader((String) str), clazz.getComponentType());
    }

    public final static <T> List<T> fromJSONList(Class<T> clazz, String str) throws Exception {

        initialize();

        CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class, clazz);

        return mapper.readValue(str, javaType);
    }

    public final static <T> String toJSON(T bean) throws Exception {

        initialize();

        return mapper.writeValueAsString(bean);
    }

    public final static <T> String toJSON(Collection<T> beans) throws Exception {

        initialize();

        return mapper.writeValueAsString(beans);
    }

}
