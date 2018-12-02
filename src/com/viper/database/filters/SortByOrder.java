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

import java.util.Comparator;
import java.util.List;

import com.viper.database.dao.DatabaseUtil;
import com.viper.database.model.ColumnParam;
import com.viper.database.model.SortType;

public class SortByOrder<T> implements Comparator<T> {
    List<ColumnParam> columnParams;

    public SortByOrder(List<ColumnParam> columnParams) {
        this.columnParams = columnParams;

        for (ColumnParam param : columnParams) {
            System.out.println("MemoryAdapter: SortByOrder: " + param.getName() + " by " + param.getOrderBy());
        }
    }

    public int compare(T a, T b) {
        for (ColumnParam param : columnParams) {
            if (param.getOrderBy() == SortType.NONE) {
                continue;
            }
            int sgn = (param.getOrderBy() == SortType.ASCEND) ? 1 : -1;
            String fieldname = param.getName().substring(param.getName().lastIndexOf('.') + 1);
            Object v1 = DatabaseUtil.getValue(a, fieldname);
            Object v2 = DatabaseUtil.getValue(b, fieldname);
            if (v1 == null && v2 == null) {
                return 0;
            }
            if (v1 == null) {
                return -1 * sgn;
            }
            if (v2 == null) {
                return 1 * sgn;
            }
            if (v1 instanceof Number) {
                int dx = (int) (((Number) v1).longValue() - ((Number) v2).longValue());
                if (dx != 0) {
                    return dx * sgn;
                }
            }

            String s1 = v1.toString().toLowerCase();
            String s2 = v2.toString().toLowerCase();
            
            int dx = (s1).compareTo(s2);
            if (dx != 0) {
                return dx * sgn;
            }
        }
        return 0;
    }
}
