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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Table extends ArrayList<Row> {

    String tablename;
    List<Row> columnInfo = null;

    public Table(String tablename) {
        this.tablename = tablename;
    }

    public void loadColumnInfo(Database db) throws Exception {
        if (columnInfo == null) {
            columnInfo = db.listColumnInfo(tablename);
            // debug();
        }
    }

    public Table load(Database db) throws Exception {
        return load(db, null, null);
    }

    public Table load(Database db, String whereClause, String selectClause) throws Exception {
        if (!db.tableExists(tablename)) {
            throw new Exception("Table " + tablename + " does not exist!");
        }

        loadColumnInfo(db);

        if (selectClause == null) {
            selectClause = "*";
        }
        if (whereClause == null) {
            whereClause = "";
        }
        String sql = "select " + selectClause + " from " + tablename + " " + whereClause;

        ResultSet rs = null;
        try {
            rs = db.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            List<String> columnNames = new ArrayList<String>(rsmd.getColumnCount());
            for (int i = 0; i < columnCount; i++) {
                columnNames.add(rsmd.getColumnName(i + 1).toLowerCase());
            }
            clear();
            while (rs.next()) {
                Row row = new Row();
                add(row);
                for (int i = 0; i < columnCount; i++) {
                    row.setValue(columnNames.get(i), rs.getObject(i + 1));
                }
            }
        } catch (SQLException e) {
            throw new Exception(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
        }
        return this;
    }

    public void persist(Database db) throws Exception {
        if (!db.tableExists(tablename)) {
            throw new Exception("Table " + tablename + " does not exist!");
        }

        System.out.println("Persisting " + size() + " rows, in table " + tablename);

        loadColumnInfo(db);

        for (int i = 0; i < size(); i++) {
            Row row = get(i);
            StringBuilder sql = new StringBuilder();
            sql.append("insert into " + tablename);
            sql.append(" (");

            boolean first = true;
            for (String name : row.keySet()) {
                if (!first) {
                    sql.append(",");
                }
                first = false;
                sql.append(name);
            }
            sql.append(") values (");

            first = true;
            for (String name : row.keySet()) {
                if (!first) {
                    sql.append(",");
                }
                first = false;
                String value = row.getValueForSQL(name, getColumnInfo(name));
                sql.append(value);
            }
            sql.append(")");

            db.execute(sql.toString());
        }
    }

    public void persistFastBroken(Database db) throws Exception {
        if (!db.tableExists(tablename)) {
            throw new Exception("Table " + tablename + " does not exist!");
        }

        if (size() == 0) {
            return;
        }

        System.out.println("Persisting " + size() + " rows, in table " + tablename);

        loadColumnInfo(db);

        StringBuilder sql = new StringBuilder();
        sql.append("insert into " + tablename);
        sql.append(" (");

        Row masterRow = get(0);
        boolean first = true;
        for (String name : masterRow.keySet()) {
            if (!first) {
                sql.append(",");
            }
            first = false;
            sql.append(name);
        }
        sql.append(") values (");

        for (int i = 0; i < size(); i++) {
            Row row = get(i);
            first = true;
            if (i > 0) {
                sql.append(",");
            }
            sql.append("(");
            for (String name : masterRow.keySet()) {
                if (!first) {
                    sql.append(",");
                }
                first = false;
                String value = row.getValueForSQL(name, getColumnInfo(name));
                sql.append(value);
            }
            sql.append(")");
        }
        sql.append(")");

        db.execute(sql.toString());
    }

    public void clean(Database db) throws Exception {
        if (!db.tableExists(tablename)) {
            throw new Exception("Table " + tablename + " does not exist!");
        }

        loadColumnInfo(db);

        db.execute("delete from " + tablename);
    }

    public void clean(Database db, String idColumn, String parentColumn) throws Exception {
        if (!db.tableExists(tablename)) {
            throw new Exception("Table " + tablename + " does not exist!");
        }

        loadColumnInfo(db);

        try {
            clean(db);
            return;
        } catch (Exception e) {
            ;
        }

        load(db, null, idColumn + "," + parentColumn);
        for (Row child : this) {
            deleteChild(db, idColumn, parentColumn, child);
        }
    }

    private void deleteChild(Database db, String idColumn, String parentColumn, Row parent) throws Exception {
        for (Row child : this) {
            if (parent.getValue(idColumn).equals(child.getValue(parentColumn))) {
                deleteChild(db, idColumn, parentColumn, child);
            }
        }
        db.execute("delete from " + tablename + " where " + idColumn + "=" + parent.getValue(idColumn));
    }

    public Row find(String columnName, Object value) throws Exception {
        if (!columnExists(columnName)) {
            throw new Exception("Column " + columnName + " not in table " + tablename);
        }
        return find(this, columnName, value);
    }

    public static Row find(List<Row> rows, String columnName, Object value) throws Exception {
        for (Row row : rows) {
            if (value.equals(row.getValue(columnName))) {
                return row;
            }
        }
        return null;
    }

    public List<Row> findAll(String columnName, Object value) throws Exception {
        if (!columnExists(columnName)) {
            throw new Exception("Column " + columnName + " not in table " + tablename);
        }
        return findAll(this, columnName, value);
    }

    public static List<Row> findAll(List<Row> rows, String columnName, Object value) throws Exception {
        List<Row> foundRows = new ArrayList<Row>();
        for (Row row : rows) {
            if (value.equals(row.getValue(columnName))) {
                foundRows.add(row);
            }
        }
        return foundRows;
    }

    public Row find(Row seekRow) throws Exception {
        return find(this, seekRow);
    }

    public static Row find(List<Row> seekRows, Row seekRow) throws Exception {
        for (Row row : seekRows) {
            boolean found = true;
            for (String columnName : seekRow.keySet()) {
                Object valueExpect = seekRow.getValue(columnName);
                Object valueActual = row.getValue(columnName);
                if (!valueExpect.equals(valueActual)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return row;
            }
        }
        return null;
    }

    public List<Row> findAll(Row seekRow) throws Exception {
        return findAll(this, seekRow);
    }

    public static List<Row> findAll(List<Row> seekRows, Row seekRow) throws Exception {
        List<Row> rows = new ArrayList<Row>();
        for (Row row : seekRows) {
            boolean found = true;
            for (String columnName : seekRow.keySet()) {
                Object valueExpect = seekRow.getValue(columnName);
                Object valueActual = row.getValue(columnName);
                if (!valueExpect.equals(valueActual)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                rows.add(row);
            }
        }
        return rows;
    }

    public int count(String columnName, Object value) throws Exception {
        if (!columnExists(columnName)) {
            throw new Exception("Column " + columnName + " not in table " + tablename);
        }
        return count(this, columnName, value);
    }

    public static int count(List<Row> rows, String columnName, Object value) throws Exception {
        int counter = 0;
        for (Row row : rows) {
            if (value.equals(row.getValue(columnName))) {
                counter = counter + 1;
            }
        }
        return counter;
    }

    public int count(Row seekRow) throws Exception {
        return count(this, seekRow);
    }

    public static int count(List<Row> rows, Row seekRow) throws Exception {
        int counter = 0;
        for (Row row : rows) {
            boolean found = true;
            for (String columnName : seekRow.keySet()) {
                Object valueExpect = seekRow.getValue(columnName);
                Object valueActual = row.getValue(columnName);
                if (!valueExpect.equals(valueActual)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                counter = counter + 1;
            }
        }
        return counter;
    }

    public boolean columnExists(String columnName) {
        for (Row row : columnInfo) {
            if (columnName.equalsIgnoreCase(row.getValue("column_name").toString())) {
                return true;
            }
        }
        return false;
    }

    public Row getColumnInfo(String columnName) {
        if (!columnExists(columnName)) {
            System.out.println("In table " + tablename + " column " + columnName + " does not exists.");
        }
        for (Row row : columnInfo) {
            if (columnName.equalsIgnoreCase(row.getValue("column_name").toString())) {
                return row;
            }
        }
        return null;
    }

    public int getNumberOfRows() {
        return size();
    }

    public Row newRow() {
        Row row = new Row();
        add(row);
        return row;
    }

    public void debug() {
        if (columnInfo == null || columnInfo.size() == 0) {
            System.out.println("No columns found for table: " + tablename);
        }
        System.out.println("Start Code Snippet ---------------");
        for (Row column : columnInfo) {
            System.out.println(tablename + ".setValue(\"" + column.getValue("column_name") + "\", \"\");");
        }
        System.out.println("End Code Snippet -----------------");
    }
}
