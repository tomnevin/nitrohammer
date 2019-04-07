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

public class GeoPositionPredicate<T> implements Predicate<T> {

    private String fieldname = null;
    private GeoPositionOperator operator = null;
    private Number lat = null;
    private Number lon = null;
    private Number distance = null;

    public GeoPositionPredicate(String fieldname, GeoPositionOperator operator, Number lat, Number lon, Number distance) {
        this.fieldname = fieldname;
        this.operator = operator;
        this.lat = lat;
        this.lon = lon;
        this.distance = distance;
    }

    @Override
    public boolean apply(T bean) {

        String name = fieldname.substring(fieldname.lastIndexOf('.') + 1);
        Object v = DatabaseUtil.getValue(bean, name);
        if (!(v instanceof Number)) {
            return false;
        }
        Number value = (Number) v;

        // TODO
        switch (operator) {
        case INSIDE_RANGE:
            return value.doubleValue() >= lat.doubleValue() && value.doubleValue() <= lat.doubleValue();
        case OUTSIDE_RANGE:
            return value.doubleValue() > lat.doubleValue() && value.doubleValue() > lat.doubleValue();
        }
        return false;
    }

    @Override
    public String toSQL() {
        StringBuilder buf = new StringBuilder();

        // TODO
        switch (operator) {
        case INSIDE_RANGE:
            // TODO
            return fieldname + " between " + lat + " " + lat;
        case OUTSIDE_RANGE:
            return fieldname + " between " + lon + " " + lon;

        }
        return buf.toString();
    }
}
