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

import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.Predicate;

public class NumberPredicate<T> implements Predicate<T> {

    private String fieldname = null;
    private NumberOperator operator = null;
    private Number filterValue = null;

    public NumberPredicate(String fieldname, NumberOperator operator, Number filterValue) {
        this.fieldname = fieldname;
        this.operator = operator;
        this.filterValue = filterValue;
    }

    @Override
    public boolean apply(T bean) {

        Object v = DatabaseUtil.get(bean, fieldname);
        if (!(v instanceof Number)) {
            return false;
        }
        Number value = (Number) v;

        switch (operator) {
        case EQUALS:
            return filterValue.doubleValue() == value.doubleValue();
        case NOT_EQUALS:
            return filterValue.doubleValue() != value.doubleValue();
        case LESS:
            return value.doubleValue() < filterValue.doubleValue();
        case GREATER:
            return value.doubleValue() > filterValue.doubleValue();
        case GREATER_EQUAL:
            return value.doubleValue() >= filterValue.doubleValue();
        case LESS_EQUAL:
            return value.doubleValue() <= filterValue.doubleValue();
        }
        return false;
    }
}