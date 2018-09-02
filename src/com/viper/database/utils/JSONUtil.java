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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;

public class JSONUtil {

	private final static Mapper mapper = new MapperBuilder().build();

	public final static <T> T fromJSON(Class<T> clazz, String str) throws Exception {
		return mapper.readObject(str, clazz);
	}

	public final static <T> List<T> fromJSONList(Class<T> clazz, String str) throws Exception {
		return new ArrayList<T>(Arrays.asList(mapper.readArray(new StringReader(str), clazz)));
	}

	public final static <T> String toJSON(T bean) throws Exception {
		return mapper.writeObjectAsString(bean);
	}

	public final static <T> String toJSON(Collection<T> beans) throws Exception {
		return mapper.writeArrayAsString(beans);
	}

	private final static boolean classExists(String classname) {
		try {
			return (Class.forName(classname) != null);
		} catch (Exception ex) {
			return false;
		}
	}

	private final static boolean isArray(final Type runtimeType) {
		return Class.class.isInstance(runtimeType) && Class.class.cast(runtimeType).isArray();
	}

	private final static boolean isCollection(final Type runtimeType) {
		if (!ParameterizedType.class.isInstance(runtimeType)) {
			return false;
		}
		final Type rawType = ParameterizedType.class.cast(runtimeType).getRawType();
		return Class.class.isInstance(rawType) && Collection.class.isAssignableFrom(Class.class.cast(rawType));
	}
}
