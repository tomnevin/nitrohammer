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
import java.sql.Blob;
import java.sql.Clob;
import java.text.DecimalFormat;

import javax.sql.rowset.serial.SerialClob;

import com.viper.database.annotations.Column;

public class ClobConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public ClobConverter(Class<?> defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public Class<?> getDefaultType() {
        return defaultType;
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Blob) {
            new DecimalFormat(qualifiers[0]).format((Blob) fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }
        if (toType.equals(char.class) && Clob.class.isAssignableFrom(fromValue.getClass())) {
            return convertClobToChars(fromValue);
        }
        if (toType.equals(Character.class) && Clob.class.isAssignableFrom(fromValue.getClass())) {
            return convertClobToChars(fromValue);
        }
        if (Clob.class.isAssignableFrom(Clob.class) && fromValue instanceof char[]) {
            return convertCharsToClob(fromValue);
        }
        if (Clob.class.isAssignableFrom(Clob.class) && fromValue instanceof Character[]) {
            return convertCharsToClob(fromValue);
        }
        if (Clob.class.isAssignableFrom(Clob.class) && fromValue instanceof Character) {
            return convertCharToClob(fromValue);
        }
        if (toType.isInstance(String.class) && fromValue instanceof char[]) {
            return new CharacterConverter(char.class).convertToType(toType, fromValue);
        }
        if (toType.isInstance(String.class) && fromValue instanceof Character[]) {
            return new CharacterConverter(Character.class).convertToType(toType, fromValue);
        }
        if (toType.isInstance(String.class) && fromValue instanceof Character) {
            return new CharacterConverter(Character.class).convertToType(toType, fromValue);
        }

        if (toType.isInstance(Character.class) && fromValue instanceof String) {
            return new CharacterConverter(Character.class).convertToType(toType, fromValue);
        }
        return fromValue;
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }
        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    public String convertToString(Object value) throws Exception {
        return convertImpl(value);
    }

    public String convertImpl(Object source) throws Exception {
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

    public Clob convertFromString(String source) throws Exception {
        return new SerialClob(source.toCharArray());
    }

    public char[] convertClobToChars(Object source) throws Exception {
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

    public Clob convertCharsToClob(Object source) throws Exception {
        char[] blob = (char[]) source;
        return new javax.sql.rowset.serial.SerialClob(blob);
    }

    public Clob convertCharToClob(Object source) throws Exception {
        Character blob = (Character) source;
        return new javax.sql.rowset.serial.SerialClob(new char[] { blob });
    }
}
