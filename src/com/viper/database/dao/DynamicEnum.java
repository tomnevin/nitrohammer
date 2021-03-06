/*
 * -----------------------------------------------------------------------------
 *                      VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * MIT License
 * 
 * Copyright (c) #{classname}.html #{util.YYYY()} Viper Software Services
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

package com.viper.database.dao;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.johnzon.mapper.JohnzonIgnore;

public abstract class DynamicEnum {

    @JohnzonIgnore
    @XmlTransient
    private static List<DynamicEnum> values = new ArrayList<DynamicEnum>();

    abstract public String value();

    public static List<? extends DynamicEnum> values() {
        return values;
    }

    public static void add(DynamicEnum item) {
        values.add(item);
    }

    public static <T extends DynamicEnum> T valueOf(String s) {
        for (DynamicEnum value : values) {
            if (value.value().equalsIgnoreCase(s)) {
                return (T) value;
            }
        }
        return null;
    }

    public static <T extends DynamicEnum> List<String> listOf() {
        List<String> items = new ArrayList<String>();
        for (DynamicEnum enumValue : values) {
            items.add(enumValue.value());
        }
        return items;
    }

}
