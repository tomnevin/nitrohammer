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

import java.lang.reflect.Array;
import java.sql.Blob;
import java.text.DecimalFormat;

import com.viper.database.annotations.Column;

public final class ByteConverter implements ConverterInterface {

	private Class<?> defaultType = null;

	public ByteConverter(Class<?> defaultType) {
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

		if (fromValue instanceof Blob) {
			return new BlobConverter(fromValue.getClass()).convertToType(toType, fromValue);
		}
		if (toType.isInstance(Blob.class)) {
			return new BlobConverter(toType).convertToType(toType, fromValue);
		}

		if (toType.equals(byte.class)) {
			return convertToByte(fromValue);
		}

		if (toType.equals(Byte.class)) {
			return convertToByte(fromValue);
		}

		throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
	}

	@Override
	public Object convertToArray(Class toType, Object fromValue) throws Exception {
		if (fromValue == null) {
			return null;
		}

		if (fromValue instanceof String) {
			if (toType.equals(byte.class)) {
				if (((String) fromValue).startsWith("X'")) {
					return ConverterUtils.fromHex((String) fromValue);
				}
				return convertToBytePrimitiveArray((String) fromValue);
			}
			if (toType.equals(Byte.class)) {
				return convertToByteArray((String) fromValue);
			}
		}

		if (fromValue instanceof Blob) {
			if (toType.equals(byte.class)) {
				return ConverterUtils.convertBlobToBytes(fromValue);
			}
		}

		if (fromValue.getClass().isArray()) {
			if (toType.equals(byte.class)) {
				return convertToBytePrimitiveArray(ConverterUtils.createArrayFromArrayObject(fromValue));
			}
			if (toType.equals(Byte.class)) {
				return convertToByteArray(ConverterUtils.createArrayFromArrayObject(fromValue));
			}
		}
		throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
	}

	@Override
	public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
		if (fromValue instanceof Byte) {
			new DecimalFormat(qualifiers[0]).format(fromValue);
		}

		throw new Exception("Unhandled conversion from " + fromValue + " to String.");
	}

	private byte convertToByte(Object fromValue) throws Exception {

		if (fromValue.getClass().isAssignableFrom(byte.class)) {
			return (byte) fromValue;

		} else if (fromValue.getClass().isAssignableFrom(int.class)) {
			return (byte) fromValue;

		} else if (fromValue.getClass().isAssignableFrom(double.class)) {
			return (byte) fromValue;

		} else if (fromValue.getClass().isAssignableFrom(short.class)) {
			return (byte) fromValue;

		} else if (fromValue.getClass().isAssignableFrom(float.class)) {
			return (byte) fromValue;

		} else if (fromValue.getClass().isAssignableFrom(long.class)) {
			return (byte) fromValue;

		} else if (fromValue instanceof Boolean) {
			return ((Boolean) fromValue) ? (byte) 1 : (byte) 0;

		} else if (fromValue instanceof Number) {
			return ((Number) fromValue).byteValue();

		} else if (fromValue instanceof String) {
			return (byte) (Integer.valueOf((String) fromValue, 16) & 0xFF);
		}
		throw new Exception("Unhandled conversion from " + fromValue + " to Byte.");
	}

	private Byte[] convertToByteArray(Object[] fromValues) throws Exception {
		if (fromValues == null) {
			return null;
		}
		Byte[] toValues = new Byte[fromValues.length];
		for (int i = 0; i < fromValues.length; i++) {
			toValues[i] = convertToByte(fromValues[i]);
		}
		return toValues;
	}

	private byte[] convertToBytePrimitiveArray(Object[] fromValues) throws Exception {
		if (fromValues == null) {
			return null;
		}
		byte[] toValues = new byte[fromValues.length];
		for (int i = 0; i < fromValues.length; i++) {
			toValues[i] = convertToByte(fromValues[i]);
		}
		return toValues;
	}

	private Byte[] convertToByteArray(String fromValues) throws Exception {
		if (fromValues == null) {
			return null;
		}
        String items[] =  ConverterUtils.toArray(fromValues);
		Byte[] toValues = new Byte[items.length];
		for (int i = 0; i < items.length; i++) {
			toValues[i] = convertToByte(items[i]);
		}
		return toValues;
	}

	private byte[] convertToBytePrimitiveArray(String fromValues) throws Exception {
		if (fromValues == null) {
			return null;
		}
		byte[] toValues = new byte[fromValues.length() >> 1];
		int j = 0;
		for (int i = 0; i < toValues.length; i++) {
			toValues[i] = convertToByte(substring(fromValues, j, j + 2));
			j = j + 2;
		}
		return toValues;
	}

	private String substring(String str, int index1, int index2) {
		if (index2 > str.length()) {
			index2 = str.length();
		}
		return str.substring(index1, index2);
	}
}
