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

package com.viper.database.managers;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import com.viper.database.CustomXPathFunctions;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.model.Cell;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.ForeignKey;
import com.viper.database.model.ForeignKeyReference;
import com.viper.database.model.Index;
import com.viper.database.model.Privilege;
import com.viper.database.model.Procedure;
import com.viper.database.model.Row;
import com.viper.database.model.Table;
import com.viper.database.model.Trigger;

public class DatabaseMgr {

    public static Database findDatabase(Databases databases, String name) {
        return (databases == null) ? null : DatabaseUtil.findOneItem(databases.getDatabases(), "name", name);
    }

    public static List<String> listSchemas(List<Database> databases) {
        List<String> list = new ArrayList<String>();
        for (Database database : databases) {
            list.add(database.getCatalog() + "," + database.getName());
        }
        return list;
    }

    // -------------------------------------------------------------------------

    private static Table findTable(Database database, String tablename) {
        return (database == null) ? null : DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
    }

    public static Table findTable(Databases databases, String databasename, String tablename) {
        return (databases == null) ? null : findTable(findDatabase(databases, databasename), tablename);
    }

    // -------------------------------------------------------------------------

    public static Column findColumn(Databases databases, String databasename, String tablename, String columnname) {
        if (databases != null) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", databasename);
            if (database != null) {
                Table table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
                if (table != null) {
                    return DatabaseUtil.findOneItem(table.getColumns(), "name", columnname);
                }
            }
        }
        return null;
    }

    public static Index findIndex(Databases databases, String databasename, String tablename, String indexName) {
        if (databases != null) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", databasename);
            if (database != null) {
                Table table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
                if (table != null) {
                    return DatabaseUtil.findOneItem(table.getIndices(), "name", indexName);
                }
            }
        }
        return null;
    }

    public static ForeignKey findForeignKey(Databases databases, String databasename, String tablename, String name) {
        if (databases != null) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", databasename);
            if (database != null) {
                Table table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
                if (table != null) {
                    return DatabaseUtil.findOneItem(table.getForeignKeys(), "name", name);
                }
            }
        }
        return null;
    }

    public static Procedure findProcedure(Databases databases, String databasename, String name) {
        if (databases != null) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", databasename);
            if (database != null) {
                return DatabaseUtil.findOneItem(database.getProcedures(), "name", name);
            }
        }
        return null;
    }

    private static ForeignKey findByLocalColumnName(List<ForeignKey> list, String name) {
        if (list != null && name != null) {
            for (ForeignKey item : list) {
                for (ForeignKeyReference ref : item.getForeignKeyReferences()) {
                    if (name.equalsIgnoreCase(ref.getLocalColumn())) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public static ForeignKey findForeignKeyByLocalColumnName(Databases databases, String databasename, String tablename,
            String columnName) {
        if (databases != null) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", databasename);
            if (database != null) {
                Table table = DatabaseUtil.findOneItem(database.getTables(), "name", tablename);
                if (table != null) {
                    return findByLocalColumnName(table.getForeignKeys(), columnName);
                }
            }
        }
        return null;
    }

    public static Privilege findPrivilege(List<Privilege> list, String grantee, String name) {
        if (list != null && name != null) {
            for (Privilege item : list) {
                if (name.equalsIgnoreCase(item.getPrivilege())) {
                    if (grantee.equalsIgnoreCase(item.getGrantee())) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------

    public static Trigger findTrigger(Databases databases, String databasename, String name) {
        if (databases != null) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", databasename);
            if (database != null) {
                return DatabaseUtil.findOneItem(database.getTriggers(), "name", name);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------

    public static Object findValue(List<Cell> list, String name) {
        if (list != null && name != null) {
            for (Cell item : list) {
                if (name.equalsIgnoreCase(item.getName())) {
                    return item.getValue();
                }
            }
        }
        return null;
    }

    public static Object findValue(Row row, String name) {
        return findValue(row.getCells(), name);
    }

    /**
     * Given the SQL select string, update the model after executing the SQL string against the
     * database.
     * 
     * @param sql
     */
    public static Row beanToRow(Object bean) throws Exception {
        Row row = new Row();
        BeanInfo beaninfo = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor[] descriptors = beaninfo.getPropertyDescriptors();
        if (descriptors != null) {
            for (PropertyDescriptor descriptor : descriptors) {
                Cell cell = new Cell();
                cell.setName(descriptor.getDisplayName());
                // cell.setValue(descriptor.getValue());
                row.getCells().add(cell);
            }
        }
        return row;
    }

    // -------------------------------------------------------------------------
    public static boolean isDependentOn(Table table1, Table table2) {
        List<ForeignKey> keys = table1.getForeignKeys();
        for (ForeignKey key : keys) {
            if (key.getForeignTable() != null && key.getForeignTable().equalsIgnoreCase(table2.getName())) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------

    public static String getJavaName(Column column) {
        return CustomXPathFunctions.toJavaName(column);
    }

    public static Cell findParameter(List<Table> md, String tablename, String keyname, String keyvalue, String fieldname) {
        Table table = DatabaseUtil.findOneItem(md, "name", tablename);
        if (table == null) {
            return null;
        }
        for (Row row : table.getRows()) {
            for (Cell cell : row.getCells()) {
                if (keyname.equalsIgnoreCase(cell.getName())) {
                    if (keyvalue.equalsIgnoreCase((String)cell.getValue())) {
                        for (Cell c : row.getCells()) {
                            if (fieldname.equalsIgnoreCase(c.getName())) {
                                return c;
                            }
                        }
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public static Object getParameter(List<Table> md, String fieldname) {
        Table table = DatabaseUtil.findOneItem(md, "name", "basic");
        if (table == null) {
            return null;
        }
        for (Row row : table.getRows()) {
            for (Cell cell : row.getCells()) {
                if (fieldname.equalsIgnoreCase(cell.getName())) {
                    return cell.getValue();
                }
            }
        }
        return null;
    }
}
