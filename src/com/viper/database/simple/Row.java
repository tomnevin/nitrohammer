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

import java.util.HashMap;

import java.sql.Types;

public class Row extends HashMap<String, Object> {

    public Row() {
        super();
    }

    public Row(int columnCount) {
        super(columnCount);
    }

    public Object getValue(String columnName) {
        return get(columnName.toLowerCase());
    }

    public int getInt(String columnName) {
        Object value = get(columnName.toLowerCase());
        return (value == null) ? 0 : (Integer) value;
    }

    public void setValue(String columnName, Object value) {
        put(columnName.toLowerCase(), value);
    }

    public String getValueForSQL(String columnname, Row columnInfo) {
        int dataType = (Integer) columnInfo.get("data_type");
        int columnSize = (Integer) columnInfo.get("column_size");
        Object value = getValue(columnname);
        String str = "";

        switch (dataType) {

        case Types.BIT:
            str = "0";
            if (value == null) {
                ;
            } else if (value instanceof Boolean && (Boolean) value) {
                str = "1";
            } else if (value instanceof Boolean && !(Boolean) value) {
                str = "0";
            } else {
                str = value.toString();
            }
            return str;
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.FLOAT:
        case Types.REAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.DECIMAL:
            return (value == null) ? "NULL" : value.toString();
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            if (value != null) {
                str = value.toString();
                if (columnSize > 0 && str.length() > columnSize) {
                    // TODO Warning here
                    str = str.substring(0, columnSize);
                }
            }
            return (value == null) ? "''" : "'" + escape(str) + "'";
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
            return (value == null) ? "''" : "'" + value + "'";
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.NULL:
        case Types.OTHER:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.STRUCT:
        case Types.ARRAY:
        case Types.BLOB:
        case Types.CLOB:
        case Types.REF:
        case Types.DATALINK:
        case Types.BOOLEAN:
        default:
            return (value == null) ? "NULL" : value.toString();
        }
    }

    private String escape(String s) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                buf.append("\\'");
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }
}
