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

// byte
// double
// float
// integer
// long
// short

// boolean
// string

public final class ArrayConverter {

    public static final void initialize() {

        Converters.register(boolean[].class, Boolean[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(Boolean[].class, boolean[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(byte[].class, Byte[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(Byte[].class, byte[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(double[].class, Double[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(Double[].class, double[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(float[].class, Float[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(Float[].class, float[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(int[].class, Integer[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(Integer[].class, int[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(long[].class, Long[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(Long[].class, long[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(short[].class, Short[].class, ArrayConverter::convertToArrayFromArray);
        Converters.register(Short[].class, short[].class, ArrayConverter::convertToArrayFromArray);

        Converters.register(Array.class, Array.class, ArrayConverter::convertToArrayFromArray); 

    }

    @SuppressWarnings("unchecked")
    public static final <T, S> T convertToArrayFromArray(Class<T> toType, S fromValue) throws Exception {

        Object[] items = (Object[]) fromValue;
        Object[] toValues = new Object[items.length];
        for (int i = 0; i < items.length; i++) {
            toValues[i] = Converters.convert(toType, items[i]);
        }
        return (T) items;
    }

}
