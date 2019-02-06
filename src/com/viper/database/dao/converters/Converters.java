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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;

/**
 * The converter class is the central conversion class which will support converting a value of one
 * type to a value of another type.
 * 
 * This class use the target class to lookup a converter to use for converting the from value. Many
 * converter classes are pre-registered see the initialize method. However, it is possible to add
 * (register) custom converter classes.
 * 
 * The convertList method is a hack, and should be removed and incorporated into the convert method
 * in the future.
 * 
 * Also, the converters are registered in a list, but should if possible be registered in a map for
 * improved performance, use of the list was done for reasons forgotten.
 * 
 * The convert method is the core method for converting data from one type to another. The convert
 * method handles all conversions to a target class from any other class type. So, the
 * StringConverter will handle all conversions to a String class from other classes, such as double,
 * float, Float, int, Integer, etc.
 * 
 * @note the conversion classes are handling conversions of type only, not for handling formatting
 *       issues.
 * 
 * @author Tom
 *
 */

public class Converters {

    private static final Logger log = Logger.getLogger(Converters.class.getName());

    private static final Mapper mapper = new MapperBuilder().build();

    static class ConverterKey {
        Class<?> fromClazz;
        Class<?> toClazz;

        public ConverterKey(Class<?> fromClazz, Class<?> toClazz) {
            this.fromClazz = fromClazz;
            this.toClazz = toClazz;
        }

        private String getKey() {
            return fromClazz.getName() + '.' + toClazz.getName();
        }

        public int hashCode() {
            return getKey().hashCode();
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof ConverterKey)) {
                return false;
            }
            ConverterKey key2 = (ConverterKey) obj;
            return getKey().equals(key2.getKey());
        }
    }

    private final static Map<ConverterKey, ConverterInterface> conversionMap = new HashMap<ConverterKey, ConverterInterface>();

    static {
        ArrayConverter.initialize();
        BeanConverter.initialize();
        BlobConverter.initialize();
        ClobConverter.initialize();
        CalendarConverter.initialize();
        DynamicEnumConverter.initialize();
        EnumConverter.initialize();
        NumberConverter.initialize();
        StringConverter.initialize();
        URLConverter.initialize();
    }

    /**
     * Register a conversion class which implements the ConverterInterface for use by the convert
     * method.
     * 
     * @param converter
     *            the conversion class
     * 
     */
    public static final <T, S> void register(Class<T> fromClazz, Class<S> toClazz, ConverterInterface converter) {
        conversionMap.put(new ConverterKey(fromClazz, toClazz), converter);
    }

    /**
     * 
     * @param fromClazz
     * @param value
     * @return
     */
    public static final <T, S> ConverterInterface lookup(Class<T> toClazz, S value) {
        Class fromClazz = (Class) value.getClass();
        for (int i = 0; i < 10; i++) {
            if (fromClazz == null) {
                break;
            }
            Class toClazz1 = toClazz;
            for (int j = 0; j < 10; j++) {
                if (toClazz1 == null) {
                    break;
                }
                ConverterKey key = new ConverterKey(fromClazz, toClazz1);
                if (conversionMap.containsKey(key)) {
                    return conversionMap.get(key);
                }
                toClazz1 = toClazz1.getSuperclass();
            }
            fromClazz = fromClazz.getSuperclass();

        }
        return null;
    }

    /**
     * Given a target class, find the converter class which will convert to the target class, and
     * then convert the specified value to a target value of the target class.
     * 
     * If the value is null, no conversion required but a null cast as the target class will be
     * returned.
     * 
     * If the target class is null, no conversion required but a value cast as the generic class
     * will be returned.
     * 
     * There are special hooks for handling enums and arrays which should be built into the convert
     * classes.
     * 
     * @param clazz
     *            The target class to which data should be converted.
     * @param value
     *            The value which is to be converted to a value of the target class.
     * 
     * @return the value of the form of the target class.
     * 
     * @throws Exception
     *             raise an exception if the conversion fails, conversion class cannot be found.
     */

    public static final <T, S> T convert(Class<T> target, S value) throws Exception {
        ConverterInterface converter = null;
        try {
            if (value == null) {
                return null;
            }

            if (target == null) {
                return (T) value;
            }

            if (value.getClass().equals(target)) {
                return target.cast(value);
            }

            converter = lookup(target, value);
            if (converter == null) {
                throw new Exception("Conversion not found: from " + value.getClass() + " to " + target.getName());
            }
            return converter.convert(target, value);

        } catch (Throwable ex) {
            System.err.println(ex.getMessage());
            log.throwing(ex.getMessage(), "", ex);
            throw ex;
        }
    }

    public static final <T, S> List<T> convertToList(Class<T> target, S value) throws Exception {
        try {
            if (value == null || target == null) {
                return null;
            }

            if (value instanceof String) {
                StringReader reader = new StringReader((String) value);
                return (List<T>) Arrays.asList(mapper.readArray(reader, target));
            }

            throw new Exception("ERROR : can't implement connversion to " + target + " from " + value.getClass() + ":" + value);

        } catch (Exception ex) {
            System.err.println("ERROR: convertToList: " + ((target == null) ? "target is null" : target.getName()) + "," + ex);
            throw ex;
        }
    }

    /**
     * Given a bean, convert the bean to a JSON string
     * 
     * @param beans
     *            the Java bean of the data model.
     * @return the JSON string representing the bean
     * @note returns null if conversion failed.
     */
    public static final <T, S> String convertFromList(Collection<T> beans) {
        try {
            if (beans == null) {
                return null;
            }
            return mapper.writeArrayAsString(beans);

        } catch (Exception ex) {
            System.err.println("ERROR: convertFromList: " + ex);
            throw ex;
        }
    }
}
