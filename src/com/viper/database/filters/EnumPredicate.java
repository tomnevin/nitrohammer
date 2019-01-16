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

package com.viper.database.filters;

import java.util.Arrays;
import java.util.List;

import com.viper.database.dao.DatabaseUtil;

public class EnumPredicate<T> implements Predicate<T> {

    private String fieldname = null;
    private EnumOperator operator = null;
    private String filterValues = null;

    public EnumPredicate(String fieldname, EnumOperator operator, String filterValues) {
        this.fieldname = fieldname;
        this.operator = operator;
        this.filterValues = filterValues;
    }

    @Override
    public boolean apply(T bean) {

        if (operator == null) {
            return true;
        }
        if (fieldname == null) {
            return true;
        }
        if (operator == null) {
            return true;
        }
        if (filterValues == null) {
            return true;
        }
        if (bean == null) {
            return false;
        }

        String name = fieldname.substring(fieldname.lastIndexOf('.') + 1);
        String value = DatabaseUtil.getString(bean, name);
        if (value == null) {
            value = "";
        }

        switch (operator) {
        case IN_LIST:
            return filterValues.contains(value);
        case NOT_IN_LIST:
            return !filterValues.contains(value);
        }
        return false;
    }

    @Override
    public String toSQL() {
        StringBuilder buf = new StringBuilder();

        switch (operator) {
        case IN_LIST:
            return fieldname + " in (" + toString(filterValues) + ")";
        case NOT_IN_LIST:
            return fieldname + " not in (" + toString(filterValues) + ")";
        }
        return buf.toString();
    }

    private String toString(String values) {
        List<String> items = Arrays.asList(values.split(","));
        StringBuilder buf = new StringBuilder();
        for (String value : items) {
            String item = value.trim();
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append("'");
            buf.append(item);
            buf.append("'");
        }
        return buf.toString();
    }
}