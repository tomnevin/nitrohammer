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

import java.io.Reader;
import java.sql.Clob;

import javax.sql.rowset.serial.SerialClob;

public class ClobConverter {

    public static final void initialize() {

        Converters.register(SerialClob.class, char[].class, ClobConverter::convertClobToChars);
        Converters.register(SerialClob.class, Character[].class, ClobConverter::convertClobToChars);
        Converters.register(SerialClob.class, char.class, ClobConverter::convertClobToChars);
        Converters.register(SerialClob.class, Character.class, ClobConverter::convertClobToChars);

        Converters.register(char[].class, SerialClob.class, ClobConverter::convertCharsToClob);
        Converters.register(Character[].class, SerialClob.class, ClobConverter::convertCharsToClob);
        Converters.register(char.class, SerialClob.class, ClobConverter::convertCharsToClob);
        Converters.register(Character.class, SerialClob.class, ClobConverter::convertCharsToClob);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertClobToChars(Class<T> toType, S fromValue) throws Exception {
        return (T) convertClobToChars(fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertCharsToClob(Class<T> toType, S fromValue) throws Exception {
        return (T) convertCharsToClob(fromValue);
    }

    private static final  char[] convertClobToChars(Object source) throws Exception {
        Clob blob = (Clob) source;
        char[] bytes = null;
        Reader r = null;
        try {
            bytes = new char[(int) blob.length()];
            r = blob.getCharacterStream();
            r.read(bytes);
        } finally {
            r.close();
        }
        return bytes;
    }

    private static final  Clob convertCharsToClob(Object source) throws Exception {
        char[] blob = (char[]) source;
        return new javax.sql.rowset.serial.SerialClob(blob);
    }

    private static final  Clob convertCharToClob(Object source) throws Exception {
        Character blob = (Character) source;
        return new javax.sql.rowset.serial.SerialClob(new char[] { blob });
    }

    private static final  String convertToString(Object value) throws Exception {
        return convertImpl(value);
    }

    private static final  String convertImpl(Object source) throws Exception {
        Clob clob = (Clob) source;
        Reader r = null;
        try {
            StringBuilder sb = new StringBuilder();
            char[] cbuf = new char[4096];
            int read;
            r = clob.getCharacterStream();
            while ((read = r.read(cbuf)) > 0) {
                sb.append(cbuf, 0, read);
            }
            return sb.toString();
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    private static final  Clob convertFromString(String source) throws Exception {
        return new SerialClob(source.toCharArray());
    }
 
}
