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
import com.viper.database.dao.DynamicEnum;

public final class DynamicEnumConverter implements ConverterInterface {

    private Class<?> defaultType = null;

    public DynamicEnumConverter(Class<?> defaultType) {
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
        if (DynamicEnum.class.isAssignableFrom(toType)) {
            return convertToDynamicEnum(toType, fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        if (fromValue instanceof String) {
            if (DynamicEnum.class.isAssignableFrom(toType)) {
                return convertToDynamicEnumArray(toType, (String) fromValue);
            }
        }

        if (fromValue.getClass().isArray()) {
            // if (fromValue.getClass().equals(byte[].class)) {
            // throw new Exception("Unhandled conversion from " + fromValue + " to " +
            // toType + ".");
            // }
            if (DynamicEnum.class.isAssignableFrom(toType)) {
                return convertToDynamicEnumArray(toType, ConverterUtils.createArrayFromArrayObject(fromValue));
            }
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof DynamicEnum) {
            return ((DynamicEnum)fromValue).value();
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    private DynamicEnum convertToDynamicEnum(Class<? extends DynamicEnum> toType, Object fromValue) throws Exception {
        if (fromValue instanceof String) {
            return DatabaseUtil.newInstance(toType, fromValue);
        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to Integer.");
        }
    }

    private DynamicEnum[] convertToDynamicEnumArray(Class<? extends DynamicEnum> toType, Object[] fromValues)
            throws Exception {
        if (fromValues == null) {
            return null;
        }
        DynamicEnum[] toValues = new DynamicEnum[fromValues.length];
        for (int i = 0; i < fromValues.length; i++) {
            toValues[i] = convertToDynamicEnum(toType, fromValues[i]);
        }
        return toValues;
    }

    private DynamicEnum[] convertToDynamicEnumArray(Class<? extends DynamicEnum> toType, String fromValues)
            throws Exception {
        if (fromValues == null) {
            return null;
        }
        String items[] = ConverterUtils.toArray(fromValues);
        DynamicEnum[] toValues = new DynamicEnum[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertToDynamicEnum(toType, items[i]);
        }
        return toValues;
    }
}
