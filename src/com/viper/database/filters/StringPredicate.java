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

import java.util.logging.Logger;

import com.viper.database.dao.DatabaseUtil;

public class StringPredicate<T> implements Predicate<T> {

    private final static Logger log = Logger.getLogger(StringPredicate.class.getName());

    private String fieldname = null;
    private StringOperator operator = null;
    private String filterValue = null;

    public StringPredicate(String fieldname, StringOperator operator, String filterValue) {
        this.fieldname = fieldname;
        this.operator = operator;
        this.filterValue = filterValue;
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
        if (filterValue == null) {
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
        String valueLC = value.toLowerCase();
        String filterValueLC = filterValue.toLowerCase();

        log.fine("StringPredicate: " + operator + "=" + valueLC + "," + filterValueLC);

        switch (operator) {
        case EQUALS:
            return filterValueLC.equals(valueLC);
        case NOT_EQUALS:
            return !filterValueLC.equals(valueLC);
        case STARTS_WITH:
            return valueLC.startsWith(filterValueLC);
        case END_WITH:
            return valueLC.endsWith(filterValueLC);
        case CONTAINS:
            return valueLC.contains(filterValueLC);
        case NOT_CONTAINS:
            return !valueLC.contains(filterValueLC);
        }
        return false;
    }

    @Override
    public String toSQL() {
        StringBuilder buf = new StringBuilder();

        switch (operator) {
        case EQUALS:
            return fieldname + " like '" + filterValue + "'";
        case NOT_EQUALS:
            return fieldname + " not like '" + filterValue + "'";
        case STARTS_WITH:
            return fieldname + " like '" + filterValue + "%' COLLATE utf8_bin";
        case END_WITH:
            return fieldname + " like '%" + filterValue + "' COLLATE utf8_bin";
        case CONTAINS:
            return fieldname + " like '%" + filterValue + "%' COLLATE utf8_bin";
        case NOT_CONTAINS:
            return fieldname + " not like '%" + filterValue + "%' COLLATE utf8_bin";
        }
        return buf.toString();
    }
}