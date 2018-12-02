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
import java.sql.Blob;
import java.sql.Clob;

import com.viper.database.annotations.Column;

public final class BeanConverter implements ConverterInterface {

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Class<Object> getDefaultType() {
        return Object.class;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {

        if (fromValue == null || toType == null) {
            return null;
        }
        // Necessary?
        if (toType.isInstance(String.class)) {
            return toType.cast(ConverterUtils.convertToString(fromValue));
        }
        if (fromValue instanceof String) {
            return ConverterUtils.convertFromString(toType, (String) fromValue);
        }
        if (fromValue instanceof char[]) {
            return ConverterUtils.convertFromString(toType, new StringReader(new String((char[]) fromValue)));
        }
        if (fromValue instanceof Character[]) {
            return ConverterUtils.convertFromString(toType,
                    new StringReader(ConverterUtils.convertToString((Character[]) fromValue)));
        }
        if (fromValue instanceof byte[]) {
            return ConverterUtils.convertFromString(toType, new StringReader(new String((byte[]) fromValue)));
        }
        if (fromValue instanceof Byte[]) {
            return ConverterUtils.convertFromString(toType, new StringReader(ConverterUtils.convertToString((Byte[]) fromValue)));
        }
        if (fromValue instanceof Clob) {
            return ConverterUtils.convertFromString(toType,
                    new StringReader(new String(ConverterUtils.convertClobToChars(fromValue))));
        }
        if (fromValue instanceof Blob) {
            return ConverterUtils.convertFromString(toType,
                    new StringReader(new String(ConverterUtils.convertBlobToBytes(fromValue))));
        }
        throw new Exception("BeanConverter: Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {

        if (fromValue == null || toType == null) {
            return null;
        }
        if (fromValue instanceof String) {
            return ConverterUtils.convertToArray(toType, new StringReader((String) fromValue));
        }
        if (fromValue instanceof char[]) {
            return ConverterUtils.convertToArray(toType, new StringReader(new String((char[]) fromValue)));
        }
        if (fromValue instanceof Character[]) {
            return ConverterUtils.convertToArray(toType,
                    new StringReader(ConverterUtils.convertToString((Character[]) fromValue)));
        }
        if (fromValue instanceof byte[]) {
            return ConverterUtils.convertToArray(toType, new StringReader(new String((byte[]) fromValue)));
        }
        if (fromValue instanceof Byte[]) {
            return ConverterUtils.convertToArray(toType, new StringReader(ConverterUtils.convertToString((Byte[]) fromValue)));
        }
        if (fromValue instanceof Clob) {
            return ConverterUtils.convertToArray(toType,
                    new StringReader(new String(ConverterUtils.convertClobToChars(fromValue))));
        }
        if (fromValue instanceof Blob) {
            return ConverterUtils.convertToArray(toType,
                    new StringReader(new String(ConverterUtils.convertBlobToBytes(fromValue))));
        }
        throw new Exception("BeanConverter: Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        return ConverterUtils.convertToString(fromValue);
    }
}
