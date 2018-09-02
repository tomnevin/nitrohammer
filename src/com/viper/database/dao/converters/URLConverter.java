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
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;

import com.viper.database.annotations.Column;

public final class URLConverter implements ConverterInterface {

    public Class<URL> getDefaultType() {
        return URL.class;
    }

    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {

        if (toType.equals(URL.class)) {
            return convertToURL(fromValue);

        } else if (fromValue instanceof URL) {
            return convertFromURL(toType, (URL) fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof URL) {
            return (fromValue == null) ? "" : fromValue.toString();
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    public URL convertToURL(Object fromValue) throws Exception {

        if (fromValue instanceof Byte) {
            return null; // TODO

        } else if (fromValue instanceof BigDecimal) {
            return null; // TODO

        } else if (fromValue instanceof Blob) {
            return null; // TODO

        } else if (fromValue instanceof Boolean) {
            return null; // TODO

        } else if (fromValue instanceof BigInteger) {
            return null; // TODO

        } else if (fromValue instanceof Clob) {
            return null; // TODO

        } else if (fromValue instanceof Float) {
            return null; // TODO

        } else if (fromValue instanceof Integer) {
            return null; // TODO

        } else if (fromValue instanceof Long) {
            return null; // TODO

        } else if (fromValue instanceof Short) {
            return null; // TODO

        } else if (fromValue instanceof String) {
            return new URL((String) fromValue);

        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to URL.");
        }
    }

    public <T> T convertFromURL(Class<T> targetType, URL fromValue) throws Exception {

        if (targetType.equals(Byte.class)) {
            return null; // TODO

        } else if (targetType.equals(BigDecimal.class)) {
            return null; // TODO

        } else if (targetType.equals(Blob.class)) {
            return null; // TODO

        } else if (targetType.equals(Boolean.class)) {
            return null; // TODO

        } else if (targetType.equals(BigInteger.class)) {
            return null; // TODO

        } else if (targetType.equals(Clob.class)) {
            return null; // TODO

        } else if (targetType.equals(Double.class)) {
            return null; // TODO

        } else if (targetType.equals(Float.class)) {
            return null; // TODO

        } else if (targetType.equals(URL.class)) {
            return null; // TODO

        } else if (targetType.equals(Integer.class)) {
            return null; // TODO

        } else if (targetType.equals(Short.class)) {
            return null; // TODO

        } else if (targetType.equals(String.class)) {
            return targetType.cast(((URL) fromValue).toString());

        } else {
            throw new Exception("Unhandled conversion from Short to " + fromValue + ".");
        }
    }
}
