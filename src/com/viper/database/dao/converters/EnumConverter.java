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

import com.viper.database.annotations.Column;
import com.viper.database.dao.DatabaseUtil;

public class EnumConverter implements ConverterInterface {

	private Class<?> defaultType = null;

	public EnumConverter(Class<?> defaultType) {
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

		if (toType.isEnum() && fromValue.getClass().isEnum()) {
			return valueOf(toType, ((Enum) fromValue).name());
		}

		if (toType.isEnum()) {
			return valueOf(toType, fromValue.toString());
		}

		if (fromValue.getClass().isEnum()) {
			return ((Enum) fromValue).toString();
		}

		throw new Exception("EnumConverter: Unhandled conversion from " + fromValue + " to " + toType + ".");
	}

	@Override
	public Object convertToArray(Class toType, Object fromValue) throws Exception {
		if (fromValue == null) {
			return null;
		}

		throw new Exception("EnumConverter: Unhandled conversion from " + fromValue + " to " + toType + ".");
	}

	@Override
	public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
		return ((Enum) fromValue).name();
	}
	
	private <T extends Enum<T>> T valueOf(Class<T> enumType,  String name) {
		try {
			return Enum.valueOf(enumType, name);
		} catch (Throwable t) {
			; // Intentionally not handled
		}
		try {
			return DatabaseUtil.invoke(enumType, "findEnumValue", name);
		} catch (Throwable t) {
			; // Intentionally not handled
		}
		return null;
	}
}
