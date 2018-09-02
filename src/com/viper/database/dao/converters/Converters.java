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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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

    private final static Logger log = Logger.getLogger(Converters.class.getName());

    private final static List<ConverterInterface> conversionList = new ArrayList<ConverterInterface>();

    static {
        initialize();
    }

    private final static void initialize() {

        try {
            register(new BigDecimalConverter(BigDecimal.class));
            register(new BigIntegerConverter(BigInteger.class));
            register(new BooleanConverter(Boolean.class));
            register(new BooleanConverter(boolean.class));
            register(new ByteConverter(Byte.class));
            register(new ByteConverter(byte.class));
            register(new CharacterConverter(Character.class));
            register(new CharacterConverter(char.class));
            register(new DoubleConverter(Double.class));
            register(new DoubleConverter(double.class));
            register(new FloatConverter(Float.class));
            register(new FloatConverter(float.class));
            register(new IntegerConverter(Integer.class));
            register(new IntegerConverter(int.class));
            register(new LongConverter(Long.class));
            register(new LongConverter(long.class));
            register(new ShortConverter(Short.class));
            register(new ShortConverter(short.class));
            register(new StringConverter(String.class));

            register(new ClobConverter(org.h2.jdbc.JdbcClob.class));
            register(new BlobConverter(org.h2.jdbc.JdbcBlob.class));
            register(new ClobConverter(javax.sql.rowset.serial.SerialClob.class));
            register(new BlobConverter(javax.sql.rowset.serial.SerialBlob.class));
            register(new ClobConverter(Clob.class));
            register(new BlobConverter(Blob.class));
            register(new DateConverter(java.sql.Date.class));
            register(new DateConverter(java.sql.Time.class));
            register(new DateConverter(java.sql.Timestamp.class));
            register(new DateConverter(java.util.Date.class));
            register(new CalendarConverter());
            register(new ListConverter());
            register(new MapConverter());

            register(new URLConverter());
            register(new FileConverter());
            register(new BeanConverter());

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Register a conversion class which implements the ConverterInterface for use by the convert
     * method.
     * 
     * @param converter
     *            the conversion class
     * 
     */
    public static void register(ConverterInterface converter) {
        conversionList.add(converter);
    }

    /**
     * Given a target class, this method will lookup a conversion class, which will convert data to
     * the target class specified here.
     * 
     * @param clazz
     *            The target class to which data should be converted.
     * 
     * @return the conversion class which implements all conversions (hopefully all) conversions to
     *         the target class.
     * 
     * @throws Exception
     *             raise an exception if the conversion class can not be found.
     */

    public static ConverterInterface lookup(Class<?> clazz) throws Exception {
        for (ConverterInterface converter : conversionList) {
            if (converter.getDefaultType().isAssignableFrom(clazz)) {
                return converter;
            }
        }
        throw new Exception("ERROR : can't find converter for: " + clazz);
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

    public static <T> T convert(Class<T> target, Object value) throws Exception {
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

            if (target.isEnum()) {
                return (T) new EnumConverter(target).convertToType(target, value);
            }

            if (value.getClass().isEnum()) {
                return (T) new EnumConverter(value.getClass()).convertToType(target, value);
            }

            if (target.isArray()) {
                Class clazz = target.getComponentType();
                converter = lookup(clazz);
                return (T) converter.convertToArray(clazz, value);
            }

            converter = lookup(target);
            return (T) converter.convertToType(target, value);

        } catch (Throwable ex) {
            String name = ((target == null) ? "target is null" : target.getName());
            log.severe("ERROR: convert: " + name + "," + converter + "," + ex);
            throw ex;
        }
    }

    /**
     * This method to be incorprated into the convert method. Not yet deprecated, as not replaced
     * yet.
     */

    public static <T> List<T> convertToList(Class<T> target, Object value) throws Exception {
        try {
            if (value == null || target == null) {
                return null;
            }

            return new ListConverter().convertToList(target, value);

        } catch (Exception ex) {
            System.err.println("ERROR: convertToList: " + ((target == null) ? "target is null" : target.getName()) + "," + ex);
            throw ex;
        }
    }
}
