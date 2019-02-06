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

import javax.sql.rowset.serial.SerialBlob;

public class BlobConverter {

    public static final void initialize() {

        Converters.register(SerialBlob.class, byte[].class, BlobConverter::convertBlobToBytes);
        Converters.register(SerialBlob.class, Byte[].class, BlobConverter::convertBlobToBytes);
        Converters.register(SerialBlob.class, byte.class, BlobConverter::convertBlobToBytes);
        Converters.register(SerialBlob.class, Byte.class, BlobConverter::convertBlobToBytes);

        Converters.register(byte[].class, SerialBlob.class, BlobConverter::convertBytesToBlob);
        Converters.register(Byte[].class, SerialBlob.class, BlobConverter::convertBytesToBlob);
        Converters.register(byte.class, SerialBlob.class, BlobConverter::convertByteToBlob);
        Converters.register(Byte.class, SerialBlob.class, BlobConverter::convertByteToBlob);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBlobToBytes(Class<T> toType, S fromValue) throws Exception {
        return (T) convertBlobToBytes(fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertBytesToBlob(Class<T> toType, S fromValue) throws Exception {
        return (T) convertBytesToBlob(fromValue);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertByteToBlob(Class<T> toType, S fromValue) throws Exception {
        return (T) convertByteToBlob(fromValue);
    }

    private static final byte[] convertBlobToBytes(Object source) throws Exception {
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

    private static final Blob convertBytesToBlob(Object source) throws Exception {
        byte[] blob = (byte[]) source;
        return new javax.sql.rowset.serial.SerialBlob(blob);
    }

    private static final Blob convertByteToBlob(Object source) throws Exception {
        Byte blob = (Byte) source;
        return new javax.sql.rowset.serial.SerialBlob(new byte[] { blob });
    }

    private static final <T> String convertToString(T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Blob) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

}
