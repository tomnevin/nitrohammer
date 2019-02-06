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

public class EnumConverter {

    public static final void initialize() {
        Converters.register(String.class, Enum.class, EnumConverter::convertStringToEnum);
        Converters.register(Enum.class, String.class, EnumConverter::convertEnumToString);

        Converters.register(String.class, Enum[].class, EnumConverter::convertStringToEnumArray);
        Converters.register(Enum[].class, String.class, EnumConverter::convertEnumArrayToString);

        Converters.register(Enum.class, Enum.class, EnumConverter::convertEnumToEnum);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToEnum(Class<T> toType, S fromValue) throws Exception {
        return (T) Enum.valueOf((Class) toType, (String) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertEnumToEnum(Class<T> toType, S fromValue) throws Exception {
        return toType.cast((Enum) fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertEnumToString(Class<T> toType, S fromValue) throws Exception {
        return (T) ((Enum) fromValue).name();
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertStringToEnumArray(Class<T> toType, S fromValue) throws Exception {
        return null;
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertEnumArrayToString(Class<T> toType, S fromValue) throws Exception {
        return null;
    }
}
