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

import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.DynamicEnum;

public final class DynamicEnumConverter {

    public static final void initialize() {
        Converters.register(String.class, DynamicEnum.class, DynamicEnumConverter::convertStringToDynamicEnum);
        Converters.register(DynamicEnum.class, String.class, DynamicEnumConverter::convertDynamicEnumToString);

        Converters.register(String.class, DynamicEnum[].class, DynamicEnumConverter::convertStringToDynamicEnumArray);
        Converters.register(DynamicEnum[].class, String.class, DynamicEnumConverter::convertDynamicEnumArrayToString);
    }

    public static final <T, S> T convertStringToDynamicEnum(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(DatabaseUtil.newInstance(toType, fromValue));
    }

    public static final <T, S> T convertDynamicEnumToString(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(((DynamicEnum) fromValue).value());
    }

    private static final <T, S> T convertStringToDynamicEnumArray(Class<T> toType, S fromValue) throws Exception {
        String items[] = ConverterUtils.toArray((String) fromValue);
        DynamicEnum[] toValues = new DynamicEnum[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = convertStringToDynamicEnum(DynamicEnum.class, items[i]);
        }
        return toType.cast(toValues);
    }

    public static final <T, S> T convertDynamicEnumArrayToString(Class<T> toType, S fromValue) throws Exception {
        return null;
    }

}
