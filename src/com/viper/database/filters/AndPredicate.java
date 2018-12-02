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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndPredicate<T> implements Predicate<T> {

    private List<Predicate> predicates = new ArrayList<Predicate>();

    public AndPredicate() {
    }

    public AndPredicate(Predicate[] predicates) {
        this.predicates.addAll(Arrays.asList(predicates));
    }

    public void addPredicate(Predicate predicate) {
        predicates.add(predicate);
    }

    @Override
    public boolean apply(T item) {
        for (Predicate predicate : predicates) {
            if (predicate != null && !predicate.apply(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toSQL() {
        StringBuilder buf = new StringBuilder();
        for (Predicate predicate : predicates) {
            if (predicate != null) {
                if (buf.length() > 0) {
                    buf.append(" AND ");
                }
                buf.append(predicate.toSQL());
            }
        }
        return buf.toString();
    }
}