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

package com.viper.database.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IndexNonUnique2 extends HashMap<Object, List<Row>> {

	public IndexNonUnique2(String columnName1, String columnName2, List<Row> rows) {
		for (Row row : rows) {
			Object value1 = row.getValue(columnName1);
			Object value2 = row.getValue(columnName2);
			Object key = value1 + "=" + value2;
			List<Row> list = get(key);
			if (list == null) {
				list = new ArrayList<Row>();
				put(key, list);
			}
			list.add(row);
		}
	}

	public List<Row> getValue(Object value1, Object value2) {
		Object key = value1 + "=" + value2;
		return get(key);
	}

	public void setValue(Object value1, Object value2, List<Row> rows) {
		Object key = value1 + "=" + value2;
		put(key, rows);
	}
}