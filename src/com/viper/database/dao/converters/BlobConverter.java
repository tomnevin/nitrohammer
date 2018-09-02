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

import java.io.InputStream;
import java.sql.Blob;
import java.text.DecimalFormat;

import org.apache.commons.beanutils.converters.ByteConverter;

import com.viper.database.annotations.Column;

public class BlobConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public BlobConverter(Class<?> defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public Class<?> getDefaultType() {
        return defaultType;
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Blob) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }
        if (toType.equals(byte[].class) && Blob.class.isAssignableFrom(fromValue.getClass())) {
            return convertBlobToBytes(fromValue);
        }
        if (toType.equals(Byte[].class) && Blob.class.isAssignableFrom(fromValue.getClass())) {
            return convertBlobToBytes(fromValue);
        }
        if (toType.equals(byte.class) && Blob.class.isAssignableFrom(fromValue.getClass())) {
            return convertBlobToBytes(fromValue);
        }
        if (toType.equals(Byte.class) && Blob.class.isAssignableFrom(fromValue.getClass())) {
            return convertBlobToBytes(fromValue);
        }
        if (Blob.class.isAssignableFrom(Blob.class) && fromValue instanceof byte[]) {
            return convertBytesToBlob(fromValue);
        }
        if (Blob.class.isAssignableFrom(Blob.class) && fromValue instanceof Byte[]) {
            return convertBytesToBlob(fromValue);
        }
        if (Blob.class.isAssignableFrom(Blob.class) && fromValue instanceof Byte) {
            return convertByteToBlob(fromValue);
        }

        if (toType.isInstance(String.class) && fromValue instanceof byte[]) {
            return new ByteConverter().convert(toType, fromValue);
        }
        if (toType.isInstance(String.class) && fromValue instanceof Byte[]) {
            return new ByteConverter().convert(toType, fromValue);
        }
        if (toType.isInstance(String.class) && fromValue instanceof Byte) {
            return new ByteConverter().convert(toType, fromValue);
        }

        if (toType.isInstance(byte[].class) && fromValue instanceof String) {
            return new ByteConverter().convert(toType, fromValue);
        }
        if (toType.isInstance(Byte[].class) && fromValue instanceof String) {
            return new ByteConverter().convert(toType, fromValue);
        }
        if (toType.isInstance(Byte.class) && fromValue instanceof String) {
            return new ByteConverter().convert(toType, fromValue);
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

    protected String convertToString(Object value) throws Exception {
        if (value instanceof Blob) {
            Blob blob = (Blob) value;
            return toHex(blob.getBytes(1L, (int) blob.length()));
        } else if (value instanceof byte[]) {
            return toHex((byte[]) value);
        } else if (value instanceof Byte) {
            return toHex((byte[]) value);
        }
        return (value == null) ? null : value.toString();
    }

    public byte[] convertBlobToBytes(Object source) throws Exception {
        Blob blob = (Blob) source;
        byte[] bytes = null;
        InputStream r = null;
        try {
            bytes = new byte[(int) blob.length()];
            r = blob.getBinaryStream();
            r.read(bytes);
        } finally {
            r.close();
        }
        return bytes;
    }

    private String toHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "NULL";
        }
        StringBuilder buf = new StringBuilder();
        // buf.append("0x");
        for (byte b : bytes) {
            buf.append(String.format("%02X", b));
        }
        return "X'" + buf.toString() + "'";
    }

    public Blob convertBytesToBlob(Object source) throws Exception {
        byte[] blob = (byte[]) source;
        return new javax.sql.rowset.serial.SerialBlob(blob);
    }

    public Blob convertByteToBlob(Object source) throws Exception {
        Byte blob = (Byte) source;
        return new javax.sql.rowset.serial.SerialBlob(new byte[] { blob });
    }
}
