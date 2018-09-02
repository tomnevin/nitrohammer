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

import com.viper.database.annotations.Column;

public final class CharacterConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public CharacterConverter(Class<?> defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public Class<?> getDefaultType() {
        return defaultType;
    }

    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }
        if (toType.equals(Character.class)) {
            return convertToCharacter(fromValue);
        }
        if (toType.equals(char.class)) {
            return convertToCharacter(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            if (toType.equals(char.class)) {
                return convertToCharacterPrimitiveArray((String) fromValue);
            }
            if (toType.equals(Character.class)) {
                return convertToCharacterArray((String) fromValue);
            }
        }

        if (fromValue instanceof Object[]) {
            if (toType.equals(Character.class)) {
                return convertToCharacterArray((Object[]) fromValue);
            }

            if (toType.equals(char.class)) {
                return convertToCharacterPrimitiveArray((Object[]) fromValue);
            }
        }

        if (Clob.class.isAssignableFrom(fromValue.getClass())) {
            if (toType.equals(char.class)) {
                return convertClobToChars(fromValue);
            }
            if (toType.equals(Character.class)) {
                return convertClobToChars(fromValue);
            }
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Character) {
            new DecimalFormat(qualifiers[0]).format((Character) fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    public Character convertToCharacter(Object fromValue) throws Exception {

        if (fromValue instanceof Number) {
            return (char) (((Number) fromValue).intValue());

        } else if (fromValue.getClass().isPrimitive()) {
            return (char) fromValue;

        } else if (fromValue instanceof Boolean) {
            return ((Boolean) fromValue) ? 'Y' : 'N';

        } else if (fromValue instanceof Blob) {
            return null; // TODO

        } else if (fromValue instanceof Clob) {
            return null;

        } else if (fromValue instanceof String) {
            String str = (String) fromValue;
            return (str.length() > 0) ? str.charAt(0) : ' ';

        } else if (fromValue instanceof Character) {
            return (Character)fromValue;
            
        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to Character.");
        }
    }

    public Character[] convertToCharacterArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        Character[] toValues = new Character[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToCharacter(fromValues[i]);
        }
        return toValues;
    }

    public char[] convertToCharacterPrimitiveArray(Object[] fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        char[] toValues = new char[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = (char) convertToCharacter(fromValues[i]);
        }
        return toValues;
    }

    public Character[] convertToCharacterArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        Character[] toValues = new Character[fromValues.length()];
        for (int i = 0; i < fromValues.length(); i++) {
            toValues[i] = fromValues.charAt(i);
        }
        return toValues;
    }

    public char[] convertToCharacterPrimitiveArray(String fromValues) throws Exception {
        if (fromValues == null) {
            return null;
        }
        return fromValues.toCharArray();
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
}
