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

package com.viper.database.dao.drivers;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.viper.database.CustomXPathFunctions;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.model.Cell;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.EngineType;
import com.viper.database.model.EnumItem;
import com.viper.database.model.ForeignKey;
import com.viper.database.model.ForeignKeyReference;
import com.viper.database.model.IdMethodType;
import com.viper.database.model.Index;
import com.viper.database.model.IndexClassType;
import com.viper.database.model.IndexType;
import com.viper.database.model.Privilege;
import com.viper.database.model.Procedure;
import com.viper.database.model.Row;
import com.viper.database.model.RowFormatType;
import com.viper.database.model.Table;
import com.viper.database.model.TableType;
import com.viper.database.model.Trigger;
import com.viper.database.model.User;
import com.viper.database.utils.FileUtil;

public class SQLDriver {

    private static final int MAX_COLUMN_COMMENT_CHARS = 255;

    private boolean ignore = false;
    private String variant = "mysql";
    private StringBuilder out = new StringBuilder();

    public SQLDriver() {
    }

    public SQLDriver(String variant) {
        this.variant = variant;
    }

    /**
     * Convert Java type, including column size to database type
     * 
     * @param column
     *            the column bean contains column definitions
     * @return the column type
     */

    public String toColumnType(Column column) {
        long columnSize = -1;
        long columnDecimalSize = 0;

        if (column.getSize() != 0) {
            columnSize = column.getSize();
        }

        // get column decimal size (if there) and check its validity
        columnDecimalSize = column.getDecimalSize();

        String def = toDatabaseType(column.getJavaType(), column.getDataType(), columnSize);
        if (columnSize <= 0) {
            if (def.startsWith("varchar")) {
                def = def.replace("<size>", "255");
            } else {
                def = def.replace("(<size>)", "");
                def = def.replace("(<size>,<decimal>)", "");
                def = def.replace("<size>", "20");
            }
        } else {
            def = def.replaceAll("<size>", "" + columnSize);
        }
        def = def.replace("<enums>", toEnumString(column.getEnumValues()));
        def = def.replace("<decimal>", "" + columnDecimalSize);

        return def;
    }

    private String toEnumString(List<EnumItem> items) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        for (EnumItem item : items) {
            if (buf.length() > 1) {
                buf.append(",");
            }
            buf.append("'");
            buf.append(item.getValue());
            buf.append("'");
        }
        buf.append(")");
        return buf.toString();
    }

    private String toDatabaseType(String javaType, String dataType, long columnSize) {
        if (dataType != null && !dataType.isEmpty()) {

            if ("enum".equalsIgnoreCase(dataType)) {
                return "enum <enums>";
            }
            return dataType;
        }
        if (javaType == null) {
            return "varchar(<size>)";
        }
        if (columnSize == -1) {
            if ("byte[]".equalsIgnoreCase(javaType)) {
                return "blob";
            }
            if ("char[]".equalsIgnoreCase(javaType)) {
                return "longtext";
            }
        }
        return SQLConversionTables.getDatabaseType(javaType);
    }

    private final static String getValue(Row row, String name) {
        Object value = DatabaseUtil.findValue(row.getCells(), name);
        return (value == null) ? null : value.toString();
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public String createDatabase(Database database) {

        if ("h2".equalsIgnoreCase(variant)) {
            write("create schema if not exists ");
        } else {
            write("create database if not exists ");
        }
        write(database.getName());
        writeNameText("CHARACTER SET", database.getCharsetName(), -1);
        writeNameText("COLLATE", database.getCollationName(), -1);
        return getBuffer();
    }

    public String alterDatabase(Database from, Database to) {
        write("alter database ");
        write(from.getName());

        boolean needComma = false;
        if (!isEquals(from.getCharsetName(), to.getCharsetName())) {
            if (to.getCharsetName() != null) {
                write(" CHARACTER SET ");
                write(to.getCharsetName());
                needComma = true;
            }
        }
        if (!isEquals(from.getCollationName(), to.getCollationName())) {
            if (to.getCollationName() != null) {
                if (needComma) {
                    write(",");
                }
                write(" COLLATE ");
                write(to.getCollationName());
            }
        }
        return getBuffer();
    }

    public String dropDatabase(Database database) {

        if ("h2".equalsIgnoreCase(variant)) {
            write("drop schema if exists ");
        } else {
            write("drop database if exists ");
        }
        write(database.getName());
        return getBuffer();
    }

    /**
     * Given the table bean, generate a sql string which will create a view as defined by the table
     * bean.
     * 
     * @param database
     *            the database definition
     * @param table
     *            the table definition
     * @return the sql string for creating a table.
     * 
     *         The following is the mysql "create view" syntax:
     * 
     *         <pre>
     * CREATE
     * [OR REPLACE]
     * [ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
     * [DEFINER = { user | CURRENT_USER }]
     * [SQL SECURITY { DEFINER | INVOKER }]
     * VIEW view_name [(column_list)]
     * AS select_statement
     * [WITH [CASCADED | LOCAL] CHECK OPTION]
     * 
     * select_statement:
     * [IGNORE | REPLACE] [AS] SELECT ... (Some legal select statement)
     *         </pre>
     */

    public String createTable(Database database, Table table) {

        if (table.getTableType() == TableType.VIEW) {
            write("create or replace view ");
            writeIdentifier(database.getName(), table.getName());
            write(" (");
            writeColumnNames(table.getColumns(), "");
            write(") as ");
            write(table.getSqlSelect());

        } else {
            write("create");
            writeConditional(table.getTableType() == TableType.LOCAL_TEMPORARY, "temporary");
            writeConditional(table.getTableType() == TableType.GLOBAL_TEMPORARY, "temporary");
            write(" table if not exists ");
            writeIdentifier(database.getName(), table.getName());

            List<Column> columns = table.getColumns();
            if (columns != null && columns.size() > 0) {
                write(" (");
                boolean first = true;
                for (Column column : columns) {
                    if (column.isPersistent()) {
                        if (!first) {
                            write(", ");
                        }
                        first = false;
                        writeDefinition(column);
                    }
                }
                write(")");
            }
            writeDefinition(table);
        }
        return getBuffer();
    }

    /**
     * Given the table bean, generate a sql string which will alter the table as defined by the
     * table bean. The following is the mysql "alter table" syntax:
     * 
     * <pre>
     * ALTER [IGNORE] TABLE tbl_name
     * alter_specification [, alter_specification] ...
     * 
     * alter_specification:
     * ADD [COLUMN] column_definition [FIRST | AFTER col_name ]
     * | ADD [COLUMN] (column_definition,...)
     * | ADD INDEX [index_name] [index_type] (index_col_name,...)
     * | ADD [CONSTRAINT [symbol]] PRIMARY KEY [index_type]
     * (index_col_name,...)
     * | ADD [CONSTRAINT [symbol]] UNIQUE [index_name] [index_type]
     * (index_col_name,...)
     * | ADD [FULLTEXT|SPATIAL] [index_name] (index_col_name,...)
     * | ADD [CONSTRAINT [symbol]] FOREIGN KEY [index_name]
     * (index_col_name,...) [reference_definition]
     * | ALTER [COLUMN] col_name {SET DEFAULT literal | DROP DEFAULT}
     * | CHANGE [COLUMN] old_col_name column_definition [FIRST|AFTER
     * col_name]
     * | MODIFY [COLUMN] column_definition [FIRST | AFTER col_name]
     * | DROP [COLUMN] col_name
     * | DROP PRIMARY KEY
     * | DROP INDEX index_name
     * | DROP FOREIGN KEY fk_symbol
     * | DISABLE KEYS
     * | ENABLE KEYS
     * | RENAME [TO] new_tbl_name
     * | ORDER BY col_name
     * | CONVERT TO CHARACTER SET charset_name [COLLATE collation_name]
     * | [DEFAULT] CHARACTER SET charset_name [COLLATE collation_name]
     * | DISCARD TABLESPACE
     * | IMPORT TABLESPACE
     * | table_options
     * | ADD [CONSTRAINT [symbol]] UNIQUE [index_name] [index_type]
     * (index_col_name,...)
     * </pre>
     */

    public String alterTable(Database database, Table from, Table to) {

        if (from.getTableType() == TableType.VIEW) {
            write("create or replace view ");
            writeIdentifier(database.getName(), to.getName());
            write(" (");
            writeColumnNames(to.getColumns(), "");
            write(") as ");
            write(to.getSqlSelect());

        } else {
            write("alter table ");
            writeIdentifier(database.getName(), from.getName());
            writeConditional(to.getTableType() == TableType.LOCAL_TEMPORARY, "temporary");
            writeConditional(to.getTableType() == TableType.GLOBAL_TEMPORARY, "temporary");
            write(" if not exists");
            writeDefinition(to);

            // drop columns
            boolean needComma = false;
            for (Column column : from.getColumns()) {
                if (column.isPersistent()) {
                    if (DatabaseUtil.findOneItem(to.getColumns(), "name", column.getName()) == null) {
                        if (needComma) {
                            write(",");
                        }
                        needComma = true;
                        write(" drop column ");
                        write(column.getName());
                    }
                }
            }

            // Add column
            boolean isFirst = true;
            for (Column column : to.getColumns()) {
                if (column.isPersistent()) {
                    if (DatabaseUtil.findOneItem(from.getColumns(), "name", column.getName()) == null) {
                        if (needComma) {
                            write(",");
                        }
                        needComma = true;
                        if (isFirst) {
                            write(" add column (");
                        } else {
                            write(",");
                        }
                        isFirst = false;
                        writeDefinition(column);
                    }
                }
            }
            if (!isFirst) {
                write(")");
            }

            // alter column
            isFirst = true;
            for (Column column : to.getColumns()) {
                if (column.isPersistent()) {
                    if (DatabaseUtil.findOneItem(from.getColumns(), "name", column.getName()) != null) {
                        if (needComma) {
                            write(",");
                        }
                        needComma = true;
                        if (isFirst) {
                            write(" modify column (");
                        } else {
                            write(",");
                        }
                        isFirst = false;
                        writeDefinition(column);
                    }
                }
            }
            if (!isFirst) {
                write(")");
            }
        }
        return getBuffer();
    }

    public String dropTable(Database database, Table table) {

        if (table.getTableType() == TableType.VIEW) {
            write("drop view if exists ");
            writeIdentifier(database.getName(), table.getName());
        } else {
            write("drop");
            writeConditional(table.getTableType() == TableType.LOCAL_TEMPORARY, "temporary");
            writeConditional(table.getTableType() == TableType.GLOBAL_TEMPORARY, "temporary");
            write(" table if exists ");
            writeIdentifier(database.getName(), table.getName());
        }
        return getBuffer();
    }

    public String delete(Database database, Table table, Row row) {
        write("delete");
        writeConditional(false, "low_priority");
        writeConditional(false, "quick");
        writeConditional(ignore, "ignore");
        write(" from ");
        writeIdentifier(database.getName(), table.getName());
        write(" ");
        writeWhereClause(table, row);
        // getOrderByClause(sql, model, rowno, primarycolumns);
        return getBuffer();
    }

    /**
     * Given the table bean, generate a sql string which will rename the table as defined by the
     * table bean. The following is the mysql "rename table" syntax:
     * 
     * <pre>
     * </pre>
     * 
     * remove the old name from table bean and make old name a calling argument.
     */

    public String renameTable(Database database, Table table, String newTablename) {
        if (table.getTableType() == TableType.VIEW) {
            write("rename view ");
            writeIdentifier(database.getName(), table.getName());
            write(" to ");
            write(newTablename);
        } else {
            write("rename table ");
            writeIdentifier(database.getName(), table.getName());
            write(" to ");
            writeIdentifier(database.getName(), newTablename);
        }
        return getBuffer();
    }

    public String truncateTable(Database database, Table table) {
        write("truncate table ");
        writeIdentifier(database.getName(), table.getName());
        return getBuffer();
    }

    // {ENGINE|TYPE} = {BDB|HEAP|ISAM|InnoDB|MERGE|MRG_MYISAM|MYISAM}
    // | AUTO_INCREMENT = value
    // | AVG_ROW_LENGTH = value
    // | CHECKSUM = {0 | 1}
    // | COMMENT = 'string'
    // | MAX_ROWS = value
    // | MIN_ROWS = value
    // | PACK_KEYS = {0 | 1 | DEFAULT}
    // | PASSWORD = 'string'
    // | DELAY_KEY_WRITE = {0 | 1}
    // | ROW_FORMAT = { DEFAULT | DYNAMIC | FIXED | COMPRESSED }
    // | RAID_TYPE = { 1 | STRIPED | RAID0 }
    // RAID_CHUNKS = value
    // RAID_CHUNKSIZE = value
    // | UNION = (tbl_name[,tbl_name]...)
    // | INSERT_METHOD = { NO | FIRST | LAST }
    // | DATA DIRECTORY = 'absolute path to directory'
    // | INDEX DIRECTORY = 'absolute path to directory'
    // | [DEFAULT] CHARACTER SET charset_name [COLLATE collation_name]

    private void writeDefinition(Table table) {

        if (table.getEngine() != null && table.getEngine() != EngineType.DEFAULT) {
            writeNameValue("ENGINE", table.getEngine().toString());
        }
        // writeConditional(table.getAverageRowLength(), "AVG_ROW_LENGTH",
        // table.getAverageRowLength());
        writeConditional(table.isHasChecksum(), "CHECKSUM", 1);
        writeNameText("COMMENT", table.getDescription(), MAX_COLUMN_COMMENT_CHARS);
        writeNameValue("PACK_KEYS", table.getPackKeys());
        writeNameValue("PASSWORD", table.getPassword());
        writeConditional(table.isDelayKeyWrite(), "DELAY_KEY_WRITE", 1);
        writeNameValue("ROW_FORMAT", (table.getRowFormat() == null) ? null : table.getRowFormat().toString());
        writeNameValue("UNION", table.getUnion());
        writeNameText("DATA_DIRECTORY", table.getDataDirectory(), 0);
        writeNameText("INDEX_DIRECTORY", table.getIndexDirectory(), 0);
        String charSetName = table.getCharsetName();
        if (!isEmpty(charSetName)) {
            write(" CHARACTER SET " + charSetName);
            String collationName = table.getCollationName();
            if (!isEmpty(collationName)) {
                write(" COLLATE " + collationName);
            }
        }
    }

    // -------------------------------------------------------------------------
    // SELECT COMMANDS
    // -------------------------------------------------------------------------

    public String load(Database database, Table table, int startIndex, int rowCount) throws Exception {

        write("select * from ");
        writeIdentifier(database.getName(), table.getName());
        writeConditional(startIndex != -1 && rowCount != -1, " limit " + startIndex + "," + rowCount);
        return getBuffer();
    }

    public String load(Database database, Table table) throws Exception {
        write("select * from ");
        writeIdentifier(database.getName(), table.getName());
        return getBuffer();
    }

    // -------------------------------------------------------------------------
    // Update SQL Commands
    // -------------------------------------------------------------------------

    public String updateRow(Database database, Table table, Row row) {
        write("UPDATE ");
        writeIdentifier(database.getName(), table.getName());
        write(" SET ");
        writeColumnNameValues(table, row);
        write(" ");
        writeWhereClause(table, row);
        return getBuffer();
    }

    // INSERT [LOW_PRIORITY | DELAYED] [IGNORE]
    // [INTO] tbl_name [(col_name,...)]
    // VALUES ({expr | DEFAULT},...),(...),...
    // [ ON DUPLICATE KEY UPDATE col_name=expr, ... ]
    //
    // Or:
    //
    // INSERT [LOW_PRIORITY | DELAYED] [IGNORE]
    // [INTO] tbl_name
    // SET col_name={expr | DEFAULT}, ...
    // [ ON DUPLICATE KEY UPDATE col_name=expr, ... ]
    //
    // Or:
    //
    // INSERT [LOW_PRIORITY | DELAYED] [IGNORE]
    // [INTO] tbl_name [(col_name,...)]
    // SELECT ...

    /*
     * (non-Javadoc)
     * 
     * @see com.viper.database.model.resources.mysql.DriverInterface#insertRow
     * (com.viper.database.model.Table, com.viper.database.utils.Row)
     */

    public String insertRows(Database database, Table table, List<Row> rows, int firstIndex, int numberOfRows) {

        if (rows == null || rows.size() == 0) {
            return "";
        }

        write("insert into ");
        writeIdentifier(database.getName(), table.getName());
        write(" (");
        writeColumnNames(table, rows.get(0), ",");
        write(") values ");
        for (int i = 0; i < numberOfRows; i++) {
            Row row = rows.get(firstIndex + i);
            if (i > 0) {
                write(",");
            }
            write("(");
            writeColumnValues(table, row, ",");
            write(")");
        }
        return getBuffer();
    }

    /**
     * CREATE [DEFINER = { user | CURRENT_USER }] TRIGGER trigger_name trigger_time trigger_event ON
     * tbl_name FOR EACH ROW trigger_stmt
     */

    public String createTrigger(Database database, Trigger trigger) {

        write("create trigger ");
        writeIdentifier(trigger.getName());
        write(" ");
        write(trigger.getTime());
        write(" ");
        write(trigger.getEvent());
        write(" on ");
        writeIdentifier(database.getName(), trigger.getTableName());
        write(" for each row ");
        write(trigger.getStatement());
        return getBuffer();
    }

    public String renameTrigger(Database database, Trigger trigger, String newTriggerName) {

        // write(dropTrigger(database, table, trigger, options));

        write("create trigger ");
        writeIdentifier(newTriggerName);
        write(" ");
        write(trigger.getTime());
        write(" ");
        write(trigger.getEvent());
        write(" on ");
        writeIdentifier(database.getName(), trigger.getTableName());
        write(" for each row ");
        write(trigger.getStatement());
        return getBuffer();
    }

    // DROP TRIGGER [IF EXISTS] [schema_name.]trigger_name

    public String dropTrigger(Database database, Trigger trigger) {
        write("drop trigger");
        writeConditional(trigger.isDropIfExists(), "if exists");
        write(" ");
        writeIdentifier(database.getName(), trigger.getName());
        return getBuffer();
    }

    public String createUser(User user) {
        write("grant all on *.* to ");
        writeUsername(user.getName(), user.getHost());
        write(" identified by '" + user.getPassword() + "'");
        return getBuffer();
    }

    public String grantUser(User user) {
        write("grant usage on *.* to ");
        writeUsername(user.getName(), user.getHost());
        return getBuffer();
    }

    // DROP USER user [, user] ...

    public String dropUser(User user) {
        if ("root".equalsIgnoreCase(user.getName())) {
            return "";
        }

        write("drop user ");
        writeUsername(user.getName(), user.getHost());
        return getBuffer();
    }

    // -------------------------------------------------------------------------
    // Select SQL Commands
    // -------------------------------------------------------------------------

    // CREATE
    // [DEFINER = { user | CURRENT_USER }]
    // PROCEDURE sp_name ([proc_parameter[,...]])
    // [characteristic ...] routine_body
    //
    // CREATE
    // [DEFINER = { user | CURRENT_USER }]
    // FUNCTION sp_name ([func_parameter[,...]])
    // RETURNS type
    // [characteristic ...] routine_body
    //
    // proc_parameter:
    // [ IN | OUT | INOUT ] param_name type
    //
    // func_parameter:
    // param_name type
    //
    // type:
    // Any valid MySQL data type
    //
    // characteristic:
    // LANGUAGE SQL
    // | [NOT] DETERMINISTIC
    // | { CONTAINS SQL | NO SQL | READS SQL DATA | MODIFIES SQL DATA }
    // | SQL SECURITY { DEFINER | INVOKER }
    // | COMMENT 'string'
    //
    // routine_body:
    // Valid SQL procedure statement

    public String createProcedure(Database database, Procedure procedure) {

        write("create ");

        String filename = procedure.getFilename();
        String source = procedure.getSource();
        if (source == null || source.trim().length() == 0) {
            if (filename != null && filename.length() > 0) {
                try {
                    String buffer = FileUtil.readFile(filename);
                    if (buffer != null) {
                        source = buffer;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        source = source.replaceAll("(?i)procedure\\s+", "procedure " + database.getName() + ".");
        source = source.replaceAll("(?i)function\\s+", "function " + database.getName() + ".");
        write(source);
        return getBuffer();
    }

    // DROP {PROCEDURE | FUNCTION} [IF EXISTS] sp_name

    public String dropProcedure(Database database, Procedure procedure) {

        write("drop procedure");
        writeConditional(true, "if exists");
        write(" ");
        writeIdentifier(database.getName(), procedure.getName());
        return getBuffer();
    }

    // GRANT
    // priv_type [(column_list)]
    // [, priv_type [(column_list)]] ...
    // ON [object_type]
    // { * | *.* | db_name.* | db_name.tbl_name | tbl_name |
    // db_name.routine_name }
    // TO user [IDENTIFIED BY [PASSWORD] 'password']
    // [, user [IDENTIFIED BY [PASSWORD] 'password']] ...
    // [REQUIRE
    // NONE |
    // [{SSL| X509}]
    // [CIPHER 'cipher' [AND]]
    // [ISSUER 'issuer' [AND]]
    // [SUBJECT 'subject']]
    // [WITH with_option [with_option] ...]
    //
    // object_type = TABLE | FUNCTION | PROCEDURE
    //
    // with_option =
    // GRANT OPTION
    // | MAX_QUERIES_PER_HOUR count
    // | MAX_UPDATES_PER_HOUR count
    // | MAX_CONNECTIONS_PER_HOUR count
    // | MAX_USER_CONNECTIONS count

    public String createPrivilege(User user, Privilege priv) {
        write("GRANT ");
        write(priv.getName());
        write(" ON * TO ");
        write(user.getName());
        return getBuffer();
    }

    // REVOKE
    // priv_type [(column_list)]
    // [, priv_type [(column_list)]] ...
    // ON [object_type]
    // { * | *.* | db_name.* | db_name.tbl_name | tbl_name |
    // db_name.routine_name }
    // FROM user [, user] ...
    // REVOKE ALL PRIVILEGES, GRANT OPTION FROM user [, user] ...

    public String dropPrivilege(User user, Privilege priv) {
        write("REVOKE ");
        write(priv.getName());
        write(" ON * FROM ");
        write(user.getName());
        return getBuffer();
    }

    // col_name [(length)] [ASC | DESC]
    private void writeDefinition(String column) {
        write(" ");
        write(column);
    }

    /**
     * Given the database bean, generate a sql string which will create the index as defined by the
     * database bean. The following is the mysql "create index" syntax:
     * 
     * @param database
     * @param table
     * @param index
     * 
     *            <pre>
     * CREATE [UNIQUE|FULLTEXT|SPATIAL] INDEX index_name
     * [index_type]
     *    ON tbl_name (index_col_name,...)
     *    [index_type]
     * 
     * index_col_name:
     *    col_name [(length)] [ASC | DESC]
     * 
     * index_type:
     *    USING {BTREE | HASH | RTREE}
     *            </pre>
     */

    public String createIndex(Database database, Table table, Index index) {

        write("create");
        writeConditional(index.getIndexClass() != IndexClassType.DEFAULT, index.getIndexClass().toString());
        write(" index ");
        writeIdentifier(index.getName());
        writeConditional(index.getIndexType() != IndexType.DEFAULT, "using " + index.getIndexType());
        write(" on ");
        writeIdentifier(database.getName(), table.getName());
        write(" (");

        boolean isFirst = true;
        for (String name : listColumnNames(table, index)) {
            if (!isFirst) {
                write(", ");
            }
            isFirst = false;
            writeDefinition(name);
        }
        write(" )");
        return getBuffer();
    }

    public final static List<String> listColumnNames(Table table, Index index) {
        List<String> columnNames = new ArrayList<String>();

        for (Column column : table.getColumns()) {
            if (index.getName().equals(column.getIndexName())) {
                columnNames.add(column.getName());
            }
        }

        return columnNames;
    }

    /**
     * Given the database bean, generate a SQL string which will rename the index as defined by the
     * database bean. The following is the MySql "rename index" syntax:
     * 
     * @param database
     * @param table
     * @param index
     * @param name
     * 
     * @return the SQL string for dropping the specific index.
     * 
     *         <pre>
     * RENAME INDEX &lt;old_index_name&gt; [ON &lt;table_name&gt;] TO &lt;new_index_name&gt;
     *         </pre>
     */

    public String renameIndex(Database database, Table table, Index index, String name) {
        String oldName = index.getName();

        try {
            dropIndex(database, table, index);
            index.setName(name);
            createIndex(database, table, index);
        } finally {
            index.setName(oldName);
        }
        return getBuffer();
    }

    /**
     * Given the database bean, generate a SQL string which will drop the index as defined by the
     * database bean. The following is the mysql "drop index" syntax:
     * 
     * @param database
     * @param table
     * @param index
     * 
     * @return the SQL string for dropping the specific index.
     * 
     *         <pre>
     * DROP INDEX index_name ON tbl_name
     *         </pre>
     */

    public String dropIndex(Database database, Table table, Index index) {

        if (!index.isPrimary()) {
            write("alter");
            writeConditional(ignore, "ignore");
            write(" table ");
            writeIdentifier(database.getName(), table.getName());
            write(" drop index ");
            writeIdentifier(index.getName());
        }
        return getBuffer();
    }

    /**
     * Given the table bean, generate a sql string which will alter the table as defined by the
     * table bean. The following is the mysql "alter table" syntax:
     * 
     * @param database
     * @param table
     * @param foreignKey
     * 
     *            <pre>
     * ALTER [IGNORE] TABLE tbl_name
     * alter_specification [, alter_specification] ...
     * 
     * alter_specification:
     * | ADD [CONSTRAINT [symbol]] FOREIGN KEY [index_name]
     * (index_col_name,...) [reference_definition]
     * | DROP FOREIGN KEY fk_symbol
     *            </pre>
     * 
     *            ALTER TABLE table_name ADD CONSTRAINT constraint_name FOREIGN KEY (col1, col2)
     *            REFERENCES table_2 (cola,colb);
     */

    public String createForeignKey(Database database, Table table, ForeignKey foreignKey) {

        write("alter table ");
        writeIdentifier(database.getName(), table.getName());
        write(" add");
        if (foreignKey.getConstraintName() != null && foreignKey.getConstraintName().length() > 0) {
            write(" constraint ");
            write(foreignKey.getConstraintName());
        }
        write(" foreign key");
        // if (foreignKey.getName() != null) {
        // write(" ");
        // writeIdentifier(foreignKey.getName());
        // }
        write(" (");

        boolean isFirst = true;
        for (ForeignKeyReference reference : foreignKey.getForeignKeyReferences()) {
            if (!isFirst) {
                write(", ");
            }
            isFirst = false;
            writeIdentifier(reference.getLocalColumn());
        }
        write(") references ");
        writeIdentifier(foreignKey.getForeignDatabase(), foreignKey.getForeignTable());

        write(" (");

        isFirst = true;
        for (ForeignKeyReference reference : foreignKey.getForeignKeyReferences()) {
            if (!isFirst) {
                write(", ");
            }
            isFirst = false;
            writeIdentifier(reference.getForeignColumn());
        }
        write(")");
        return getBuffer();
    }

    /**
     * Given the database bean, generate a sql string which will drop the index as defined by the
     * database bean. The following is the mysql "drop index" syntax:
     * 
     * <pre>
     * 
     * </pre>
     */

    public String dropForeignKey(Database database, Table table, ForeignKey foreignKey) {

        if (!"primary".equalsIgnoreCase(foreignKey.getConstraintName())) {
            write("alter");
            writeConditional(ignore, "ignore");
            write(" table ");
            writeIdentifier(database.getName(), table.getName());
            write(" drop foreign key ");
            write(foreignKey.getConstraintName());
        }
        return getBuffer();
    }

    // SQL Columns

    public String addColumn(Database database, Table table, Column column) {
        write("alter");
        writeConditional(ignore, "ignore");
        write(" table ");
        writeIdentifier(database.getName(), table.getName());
        write(" add column ");
        writeDefinition(column);
        return getBuffer();
    }

    public String modifyColumn(Database database, Table table, Column column) {
        write("alter");
        writeConditional(ignore, "ignore");
        write(" table ");
        writeIdentifier(database.getName(), table.getName());
        write(" modify column ");
        writeDefinition(column);
        return getBuffer();
    }

    public String dropColumn(Database database, Table table, Column column) {
        write("alter");
        writeConditional(ignore, "ignore");
        write(" table ");
        writeIdentifier(database.getName(), table.getName());
        write(" drop column ");
        write(column.getName());
        return getBuffer();
    }

    // RENAME COLUMN <table_name>.<column_name> TO <column_name>

    public String renameColumn(Database database, Table table, Column column, String columnName) {
        write("alter");
        writeConditional(ignore, "ignore");
        write(" table ");
        writeIdentifier(database.getName(), table.getName());
        write(" change column ");
        write(column.getName());
        write(" ");
        write(columnName);
        write(" ");
        writeColumnType(column);
        return getBuffer();
    }

    /**
     * Convert Java type, including column size to database type
     * 
     * @param javaType
     * @param columnSize
     * @return
     */

    private void writeColumnType(Column column) {

        write(toColumnType(column));
        writeConditional(column.isUnsigned(), "unsigned");
        writeConditional(column.isZeroFill(), "zerofill");
        writeConditional(column.isBinary(), "binary");
        writeConditional(column.isAscii(), "ascii");
        writeConditional(column.isUnicode(), "unicode");
    }

    // column_definition:
    // col_name type [NOT NULL | NULL] [DEFAULT default_value]
    // [AUTO_INCREMENT] [[PRIMARY] KEY] [COMMENT 'string']
    // [reference_definition]

    private void writeDefinition(Column column) {
        writeIdentifier(column.getName());
        write(" ");
        writeColumnType(column);
        writeConditional(column.isRequired(), "NOT NULL");
        String defaultValue = column.getDefaultValue();

        // Note: the default value should be converted to the actual data type
        if (!isEmpty(defaultValue)) {
            write(" default ");
            writeColumnValue(column, defaultValue);
        }
        writeConditional(column.getIdMethod() == IdMethodType.AUTOINCREMENT, "AUTO_INCREMENT");
        writeConditional(column.isPrimaryKey(), "PRIMARY KEY");
        writeNameText("COMMENT", column.getDescription(), MAX_COLUMN_COMMENT_CHARS);

        if (column.isNaturalKey()) {
            write(",");
            write(" index ");
            write(" (");
            write(column.getName());
            write(")");
        }

        // [reference_definition]
    }

    private void write(String str) {
        if (str != null) {
            out.append(str);
        }
    }

    private void write(Object o) {
        if (o != null) {
            write(o.toString().toLowerCase());
        }
    }

    private void writeString(String str) {
        out.append("'");
        write(escape(str));
        out.append("'");
    }

    private void writeBoolean(String str) {
        if ("true".equalsIgnoreCase(str)) {
            out.append("1");
        } else if ("false".equalsIgnoreCase(str)) {
            out.append("0");
        } else {
            write(str);
        }
    }

    private void writeIdentifier(String str) {
        if (str == null) {

        } else if ("h2".equalsIgnoreCase(variant)) {
            if (isReservedWord(str)) {
                out.append("\"");
                write(str.toUpperCase());
                out.append("\"");
            } else {
                write(str);
            }
        } else if (containsSpecialCharacters(str) || isReservedWord(str)) {
            out.append("`");
            write(str);
            out.append("`");
        } else {
            write(str);
        }
    }

    private void writeIdentifier(String databasename, String name) {
        if (databasename != null && databasename.length() > 0) {
            writeIdentifier(databasename);
            out.append(".");
            writeIdentifier(name);
        } else {
            writeIdentifier(name);
        }
    }

    // -------------------------------------------------------------------------

    private void writeConditional(Boolean isValid, String value) {
        if (isValid != null && isValid) {
            out.append(" ");
            write(value.toLowerCase());
        }
    }

    private void writeConditional(Boolean isValid, String name, Object value) {
        if (isValid != null && isValid && value != null) {
            out.append(" ");
            write(name.toLowerCase());
            out.append("=");
            write(value);
        }
    }

    private void writeUsername(String name, String hostname) {
        out.append("'");
        write(name);
        out.append("'@'");
        write(((hostname == null) ? "localhost" : hostname));
        out.append("'");
    }

    private void writeNameValue(String name, String value) {
        if (value != null && value.length() > 0 && !"default".equalsIgnoreCase(value)) {
            out.append(" ");
            write(name);
            out.append("=");
            write(value);
        }
    }

    private void writeNameText(String name, String value, int maxchars) {
        if (value != null && value.length() > 0 && !"default".equalsIgnoreCase(value)) {
            out.append(" ");
            write(name);
            out.append(" ");
            if (maxchars > 0 && value.length() > maxchars) {
                writeString(value.substring(0, maxchars));
            } else {
                writeString(value);
            }
        }
    }

    // -------------------------------------------------------------------------
    // COLUMN NAMES (PART OF INSERT CLAUSE)
    // -------------------------------------------------------------------------

    private void writeColumnNames(List<Column> columns, String defaultValue) {
        if (columns == null || columns.size() == 0) {
            write(defaultValue);
            return;
        }
        boolean first = true;
        for (Column column : columns) {
            if (column.isPersistent()) {
                if (!first) {
                    out.append(",");
                }
                first = false;
                writeIdentifier(column.getName());
            }
        }
    }

    private void writeColumnNames(Table table, Row row, String separator) {
        boolean first = true;
        int index = 0;
        if (row != null) {
            for (Cell cell : row.getCells()) {
                Column column = findColumn(cell.getName(), table.getColumns());
                if (column == null) {
                    column = table.getColumns().get(index);
                }
                index = index + 1;
                if (column != null && column.isPersistent()) {
                    if (!first) {
                        write(separator);
                    }
                    first = false;
                    writeIdentifier(column.getName());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // COLUMN VALUES (PART OF INSERT CLAUSE)
    // -------------------------------------------------------------------------

    private void writeColumnValues(Table table, Row row, String separator) {
        int index = 0;
        int length = out.length();
        if (row != null) {
            for (Cell cell : row.getCells()) {
                Column column = findColumn(cell.getName(), table.getColumns());
                if (column == null) {
                    column = table.getColumns().get(index);
                }
                index = index + 1;
                if (column != null && column.isPersistent()) {
                    if (length < out.length()) {
                        write(separator);
                    }
                    writeColumnValue(column, cell.getValue());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // COLUMN NAMES AND VALUES (PART OF UPDATE CLAUSE)
    // -------------------------------------------------------------------------

    private void writeColumnNameValues(Table table, Row row) {
        boolean first = true;
        for (Cell cell : row.getCells()) {
            Column column = findColumn(cell.getName(), table.getColumns());
            if (column.isPersistent() && !column.isPrimaryKey()) {
                if (!first) {
                    out.append(",");
                }
                first = false;
                writeColumnNameValue(column, cell.getValue());
            }
        }
    }

    // -------------------------------------------------------------------------

    private void writeWhereClause(Table table, Row row) {
        boolean isFirst = true;
        if (row != null) {
            for (Cell cell : row.getCells()) {
                Column column = findColumn(cell.getName(), table.getColumns());
                if (column.isPersistent()) {
                    if (isFirst) {
                        out.append(" where ");
                    } else {
                        out.append(" and ");
                    }
                    isFirst = false;
                    writeColumnNameValue(column, cell.getValue());
                }
            }
        }
    }

    private void writeColumnNameValue(Column column, Object value) {
        writeIdentifier(column.getName());
        out.append("=");
        writeColumnValue(column, value);
    }

    private void writeColumnValue(Column column, Object value) {
        if (value == null) {
            out.append("null");
            return;
        }

        String str = value.toString();
        if ("CURRENT_TIMESTAMP".equalsIgnoreCase(str)) {
            write(str);

        } else if (value instanceof Boolean) {
            writeBoolean(value.toString());

        } else if ("boolean".equalsIgnoreCase(column.getJavaType())) {
            writeBoolean(value.toString());

        } else if (value instanceof Time) {
            str = str.trim();
            if (str.length() > 0 && Character.isDigit(str.charAt(0))) {
                write("time('" + value + "')");
            } else {
                write(str);
            }
        } else if (value instanceof Timestamp) {
            str = str.trim();
            if (str.length() == 0 || !Character.isDigit(str.charAt(0))) {
                write(str);
            } else {
                write("timestamp('" + value + "')");
            }
        } else if (value instanceof Date) {
            str = str.trim();
            if (str.length() > 0 && Character.isDigit(str.charAt(0))) {
                write("date('" + value + "')");
            } else {
                write(str);
            }
        } else {
            writeString(value.toString());
        }
    }

    // -------------------------------------------------------------------------

    private String escape(String str) {
        return (str == null) ? ""
                : str.replace("\\", "\\\\").replace("\"", "\"\"").replace("'", "''").replace("(", "\\(").replace(")", "\\)");
    }

    private Column findColumn(String columnname, List<Column> columns) {
        if (columnname != null) {
            for (Column column : columns) {
                if (column.getName().equalsIgnoreCase(columnname)) {
                    return column;
                }
            }
        }
        return null;
    }

    private boolean containsSpecialCharacters(String str) {
        return (str != null && str.indexOf('-') != -1);
    }

    private boolean isReservedWord(String str) {
        return ArrayUtils.contains(RESERVED_WORDS, str.toUpperCase());
    }

    private String getBuffer() {
        String str = out.toString();
        out.setLength(0);
        return str;
    }

    public final Databases load(DatabaseSQLInterface dao, String databaseName, String tableName) throws Exception {

        Databases databases = new Databases();
        databases.getDatabases().addAll(loadDatabases(dao, databaseName));

        databases.getUsers().addAll(loadUsers(dao, null));
        databases.getPrivileges().addAll(loadPrivileges(dao, null));
        mergeProcedures(databases, loadProcedures(dao, databaseName, null));
        mergeTables(databases, loadTables(dao, databaseName, tableName));
        mergeTables(databases, loadViews(dao, databases, databaseName, tableName));
        mergeColumns(databases, loadColumns(dao, databaseName, tableName, null));
        mergeIndex(databases, loadIndexInfo(dao, databaseName, tableName, null));
        mergeForeignKeys(databases, loadForeignKeys(dao, databaseName, tableName, null));
        loadExportedKeys(dao, databases, databaseName, tableName, null);
        mergeTriggers(databases, loadTriggers(dao, databaseName, null));
        // loadCharacterSets(writer, databases);
        // loadCollationNames(writer, databases);

        return databases;
    }

    private static final void mergeProcedures(Databases databases, List<Procedure> procedures) {

        for (Procedure procedure : procedures) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", procedure.getDatabaseName());
            if (database != null) {
                database.getProcedures().add(procedure);
            }
        }
    }

    private static final void mergeTriggers(Databases databases, List<Trigger> triggers) {

        for (Trigger trigger : triggers) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", trigger.getDatabaseName());
            if (database != null) {
                database.getTriggers().add(trigger);
            }
        }
    }

    private static final void mergeTables(Databases databases, List<Table> tables) {

        for (Table table : tables) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", table.getDatabaseName());
            if (database != null) {
                database.getTables().add(table);
            } else {
                System.out.println("MergeTables: could not find database : " + table.getDatabaseName());
            }
        }
    }

    private static final void mergeColumns(Databases databases, List<Column> columns) {

        for (Column column : columns) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", column.getDatabaseName());
            if (database != null) {
                Table table = DatabaseUtil.findOneItem(database.getTables(), "name", column.getTableName());
                if (table != null) {
                    table.getColumns().add(column);
                }
            }
        }
    }

    private static final void mergeIndex(Databases databases, List<Index> indicies) {

        for (Index index : indicies) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", index.getDatabaseName());
            if (database != null) {
                Table table = DatabaseUtil.findOneItem(database.getTables(), "name", index.getTableName());
                if (table != null) {
                    table.getIndices().add(index);

                    Column column = DatabaseUtil.findOneItem(table.getColumns(), "name", index.getColumnName());
                    if (column != null) {
                        column.setIndexName(index.getColumnName());
                    }

                }
            }
        }
    }

    private static final void mergeForeignKeys(Databases databases, List<ForeignKey> foreignKeys) {

        for (ForeignKey foreignKey : foreignKeys) {
            Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", foreignKey.getLocalDatabase());
            if (database != null) {
                Table table = DatabaseUtil.findOneItem(database.getTables(), "name", foreignKey.getLocalTable());
                if (table != null) {
                    table.getForeignKeys().add(foreignKey);
                }
            }
        }
    }

    /**
     * 
     * @param dao
     * @return list of databases.
     * 
     * @exception
     * 
     *            <pre>
     * +----------------------------+--------------+------+-----+---------+-------+
     * | Field                      | Type         | Null | Key | Default | Extra |
     * +----------------------------+--------------+------+-----+---------+-------+
     * | CATALOG_NAME               | varchar(512) | YES  |     | NULL    |       |
     * | SCHEMA_NAME                | varchar(64)  | NO   |     |         |       |
     * | DEFAULT_CHARACTER_SET_NAME | varchar(64)  | NO   |     |         |       |
     * | DEFAULT_COLLATION_NAME     | varchar(64)  | NO   |     |         |       |
     * | SQL_PATH                   | varchar(512) | YES  |     | NULL    |       |
     * +----------------------------+--------------+------+-----+---------+-------+
     *            </pre>
     */

    public final List<Database> loadDatabases(DatabaseSQLInterface dao, String databaseName) throws Exception {

        String whereClause = "";
        if (!isEmpty(databaseName)) {
            whereClause = " where schema_name='" + databaseName + "' or catalog_name='" + databaseName + "' ";
        }

        List<Database> databases = new ArrayList<Database>();
        List<Row> rows = dao.readRows("select * from information_schema.schemata " + whereClause);

        for (Row row : rows) {
            Database item = new Database();

            item.setName(getDatabaseName(row));
            item.setCharsetName(getValue(row, "default_character_set_name"));
            item.setCollationName(getValue(row, "default_collation_name"));
            item.setCatalog(getValue(row, "catalog_name"));

            databases.add(item);
        }
        return databases;
    }

    /**
     * <pre> +-----------------+---------------------+------+-----+---------+-------+ | Field | Type
     * | Null | Key | Default | Extra |
     * +-----------------+---------------------+------+-----+---------+-------+ | TABLE_CATALOG |
     * varchar(512) | YES | | NULL | | | TABLE_SCHEMA | varchar(64) | NO | | | | | TABLE_NAME |
     * varchar(64) | NO | | | | | TABLE_TYPE | varchar(64) | NO | | | | | ENGINE | varchar(64) | YES
     * | | NULL | | | VERSION | bigint(21) unsigned | YES | | NULL | | | ROW_FORMAT | varchar(10) |
     * YES | | NULL | | | TABLE_ROWS | bigint(21) unsigned | YES | | NULL | | | AVG_ROW_LENGTH |
     * bigint(21) unsigned | YES | | NULL | | | DATA_LENGTH | bigint(21) unsigned | YES | | NULL | |
     * | MAX_DATA_LENGTH | bigint(21) unsigned | YES | | NULL | | | INDEX_LENGTH | bigint(21)
     * unsigned | YES | | NULL | | | DATA_FREE | bigint(21) unsigned | YES | | NULL | | |
     * AUTO_INCREMENT | bigint(21) unsigned | YES | | NULL | | | CREATE_TIME | datetime | YES | |
     * NULL | | | UPDATE_TIME | datetime | YES | | NULL | | | CHECK_TIME | datetime | YES | | NULL |
     * | | TABLE_COLLATION | varchar(64) | YES | | NULL | | | CHECKSUM | bigint(21) unsigned | YES |
     * | NULL | | | CREATE_OPTIONS | varchar(255) | YES | | NULL | | | TABLE_COMMENT | varchar(80) |
     * NO | | | | +-----------------+---------------------+------+-----+---------+-------+ </pre>
     * 
     * @param dao @param databaseName @param tableName
     * 
     * @return list of databases. @throws Exception
     * 
     * @exception
     * 
     */

    public final List<Table> loadTables(DatabaseSQLInterface dao, String databaseName, String tableName) throws Exception {

        String whereClause = "";
        if (!isEmpty(databaseName) && !isEmpty(tableName)) {
            whereClause = " where table_schema='" + databaseName + "' and table_name='" + tableName + "'";
        } else if (!isEmpty(databaseName)) {
            whereClause = " where table_schema='" + databaseName + "'";
        }

        List<Row> rows = dao.readRows("select * from information_schema.tables " + whereClause);
        List<Table> tables = new ArrayList<Table>();

        for (Row row : rows) {
            Table item = new Table();
            item.setName(getValue(row, "table_name"));
            if (databaseName != null) {
                // TEMPORARY mysql case sensitivity reade
                // table_schema values as lower case alway
                item.setDatabaseName(databaseName);

            } else {
                item.setDatabaseName(getDatabaseName(row));
            }

            item.setTableType(toTableType(getValue(row, "table_type")));
            item.setDescription(getDescription(row));

            item.setEngine(toEngine(getValue(row, "engine")));
            item.setRowFormat(toRowFormatType(getValue(row, "row_format")));
            item.setCollationName(getValue(row, "table_collation"));

            tables.add(item);
        }
        return tables;
    }

    /**
     * <pre>
     * +----------------------+--------------+------+-----+---------------------+-------+
     * | Field                | Type         | Null | Key | Default             | Extra |
     * +----------------------+--------------+------+-----+---------------------+-------+
     * | SPECIFIC_NAME        | varchar(64)  | NO   |     |                     |       |
     * | ROUTINE_CATALOG      | varchar(512) | YES  |     | NULL                |       |
     * | ROUTINE_SCHEMA       | varchar(64)  | NO   |     |                     |       |
     * | ROUTINE_NAME         | varchar(64)  | NO   |     |                     |       |
     * | ROUTINE_TYPE         | varchar(9)   | NO   |     |                     |       |
     * | DTD_IDENTIFIER       | varchar(64)  | YES  |     | NULL                |       |
     * | ROUTINE_BODY         | varchar(8)   | NO   |     |                     |       |
     * | ROUTINE_DEFINITION   | longtext     | YES  |     | NULL                |       |
     * | EXTERNAL_NAME        | varchar(64)  | YES  |     | NULL                |       |
     * | EXTERNAL_LANGUAGE    | varchar(64)  | YES  |     | NULL                |       |
     * | PARAMETER_STYLE      | varchar(8)   | NO   |     |                     |       |
     * | IS_DETERMINISTIC     | varchar(3)   | NO   |     |                     |       |
     * | SQL_DATA_ACCESS      | varchar(64)  | NO   |     |                     |       |
     * | SQL_PATH             | varchar(64)  | YES  |     | NULL                |       |
     * | SECURITY_TYPE        | varchar(7)   | NO   |     |                     |       |
     * | CREATED              | datetime     | NO   |     | 0000-00-00 00:00:00 |       |
     * | LAST_ALTERED         | datetime     | NO   |     | 0000-00-00 00:00:00 |       |
     * | SQL_MODE             | longtext     | NO   |     | NULL                |       |
     * | ROUTINE_COMMENT      | varchar(64)  | NO   |     |                     |       |
     * | DEFINER              | varchar(77)  | NO   |     |                     |       |
     * | CHARACTER_SET_CLIENT | varchar(32)  | NO   |     |                     |       |
     * | COLLATION_CONNECTION | varchar(32)  | NO   |     |                     |       |
     * | DATABASE_COLLATION   | varchar(32)  | NO   |     |                     |       |
     * +----------------------+--------------+------+-----+---------------------+-------+
     * </pre>
     */

    public final List<Procedure> loadProcedures(DatabaseSQLInterface dao, String databaseName, String routineName)
            throws Exception {

        String whereClause = "";
        if (!isEmpty(databaseName) && !isEmpty(routineName)) {
            whereClause = " where routine_schema='" + databaseName + "' and routine_name='" + routineName + "'";
        } else if (!isEmpty(databaseName)) {
            whereClause = " where routine_schema='" + databaseName + "'";
        }

        List<Row> rows = dao.readRows("select * from information_schema.routines " + whereClause);
        List<Procedure> procedures = new ArrayList<Procedure>();

        for (Row row : rows) {
            Procedure item = new Procedure();
            item.setName(getValue(row, "routine_name"));
            item.setDatabaseName(getValue(row, "routine_schema"));
            item.setSource(getValue(row, "routine_definition"));
            item.setDescription(getDescription(row));

            procedures.add(item);
        }
        return procedures;
    }

    /**
     * <pre>
     * +--------------------------+---------------------+------+-----+---------+-------+
     * | Field                    | Type                | Null | Key | Default | Extra |
     * +--------------------------+---------------------+------+-----+---------+-------+
     * | TABLE_CATALOG            | varchar(512)        | YES  |     | NULL    |       |
     * | TABLE_SCHEMA             | varchar(64)         | NO   |     |         |       |
     * | TABLE_NAME               | varchar(64)         | NO   |     |         |       |
     * | COLUMN_NAME              | varchar(64)         | NO   |     |         |       |
     * | ORDINAL_POSITION         | bigint(21) unsigned | NO   |     | 0       |       |
     * | COLUMN_DEFAULT           | longtext            | YES  |     | NULL    |       |
     * | IS_NULLABLE              | varchar(3)          | NO   |     |         |       |
     * | DATA_TYPE                | varchar(64)         | NO   |     |         |       |
     * | CHARACTER_MAXIMUM_LENGTH | bigint(21) unsigned | YES  |     | NULL    |       |
     * | CHARACTER_OCTET_LENGTH   | bigint(21) unsigned | YES  |     | NULL    |       |
     * | NUMERIC_PRECISION        | bigint(21) unsigned | YES  |     | NULL    |       |
     * | NUMERIC_SCALE            | bigint(21) unsigned | YES  |     | NULL    |       |
     * | CHARACTER_SET_NAME       | varchar(64)         | YES  |     | NULL    |       |
     * | COLLATION_NAME           | varchar(64)         | YES  |     | NULL    |       |
     * | COLUMN_TYPE              | longtext            | NO   |     | NULL    |       |
     * | COLUMN_KEY               | varchar(3)          | NO   |     |         |       |
     * | EXTRA                    | varchar(27)         | NO   |     |         |       |
     * | PRIVILEGES               | varchar(80)         | NO   |     |         |       |
     * | COLUMN_COMMENT           | varchar(255)        | NO   |     |         |       |
     * +--------------------------+---------------------+------+-----+---------+-------+
     * </pre>
     */

    public final List<Column> loadColumns(DatabaseSQLInterface dao, String databaseName, String tableName, String columnName)
            throws Exception {

        String whereClause = "";
        if (!isEmpty(databaseName) && !isEmpty(tableName) && !isEmpty(columnName)) {
            whereClause = " where table_schema='" + databaseName + "' and table_name='" + tableName + "' and column_name='"
                    + columnName + "'";
        } else if (!isEmpty(databaseName) && !isEmpty(tableName)) {
            whereClause = " where table_schema='" + databaseName + "' and table_name='" + tableName + "'";
        } else if (!isEmpty(databaseName)) {
            whereClause = " where table_schema='" + databaseName + "'";
        }

        String orderBy = " order by ordinal_position";
        List<Row> rows = dao.readRows("select * from information_schema.columns " + whereClause);
        List<Column> columns = new ArrayList<Column>();

        for (Row row : rows) {
            Column item = new Column();
            item.setName(getValue(row, "column_name"));
            item.setDatabaseName(getDatabaseName(row));
            item.setDatabaseName(databaseName);
            item.setTableName(getValue(row, "table_name"));

            // if (getValue(row, "ordinal_position") != null) {
            // item.setOrdinalPosition(toInt(getValue(row, "ordinal_position")));
            // }

            if (getValue(row, "column_default") != null && !getValue(row, "column_default").isEmpty()) {
                item.setDefaultValue(getValue(row, "column_default"));
            }
            if (getValue(row, "column_def") != null && !getValue(row, "column_def").isEmpty()) {
                item.setDefaultValue(getValue(row, "column_def"));
            }
            if (getValue(row, "is_nullable") != null) {
                item.setRequired(!toBoolean(getValue(row, "is_nullable")));
            }
            if ("pri".equalsIgnoreCase(getValue(row, "column_key"))) {
                item.setPrimaryKey(true);
            }
            if ("uni".equalsIgnoreCase(getValue(row, "column_key"))) {
                item.setUnique(true);
            }
            if ("mul".equalsIgnoreCase(getValue(row, "column_key"))) {
                item.setNaturalKey(true);
            }
            if (toBoolean(getValue(row, "IS_AUTOINCREMENT"))) {
                item.setIdMethod(IdMethodType.AUTOINCREMENT);
            }
            if (getValue(row, "extra").contains("auto_increment")) {
                item.setIdMethod(IdMethodType.AUTOINCREMENT);
            }

            if (getValue(row, "character_maximum_length") != null) {
                item.setSize(toLong(getValue(row, "character_maximum_length")));
            } else {
                item.setSize(toLong(getValue(row, "numeric_precision")));
            }
            int size = toInt(getValue(row, "COLUMN_SIZE"));
            if (size > 0 && size < 21845) {
                item.setSize((long) size);
            }
            if (item.getSize() == 0) {
                item.setSize(null);
            }
            item.setDecimalSize(toInt(getValue(row, "numeric_scale")));
            if (item.getDecimalSize() == 0) {
                item.setDecimalSize(null);
            }

            item.setDescription(getDescription(row));

            String dataType = getValue(row, "data_type");
            item.setDataType(dataType);

            String columnType = getValue(row, "column_type");
            item.setExtraDataType(columnType);

            parseIfEnumType(columnType, item);

            String javaType = toJavaType(item.getTableName(), item.getName(), dataType, item.isRequired());
            if (javaType != null) {
                item.setJavaType(javaType);
            } else {
                System.err.println("UNKNOWN SQL FIELD TYPE (" + columnType + ")=" + item.getDatabaseName() + "."
                        + item.getTableName() + "." + item.getName());
            }
            columns.add(item);
        }
        return columns;
    }

    /**
     * <pre>
     * +-----------------------+-----------------------------------+------+-----+---------+-------+
     * | Field                 | Type                              | Null | Key | Default | Extra |
     * +-----------------------+-----------------------------------+------+-----+---------+-------+
     * | Host                  | char(60)                          | NO   | PRI |         |       |
     * | User                  | char(16)                          | NO   | PRI |         |       |
     * | Password              | char(41)                          | NO   |     |         |       |
     * | Select_priv           | enum('N','Y')                     | NO   |     | N       |       |
     * | Insert_priv           | enum('N','Y')                     | NO   |     | N       |       |
     * | Update_priv           | enum('N','Y')                     | NO   |     | N       |       |
     * | Delete_priv           | enum('N','Y')                     | NO   |     | N       |       |
     * | Create_priv           | enum('N','Y')                     | NO   |     | N       |       |
     * | Drop_priv             | enum('N','Y')                     | NO   |     | N       |       |
     * | Reload_priv           | enum('N','Y')                     | NO   |     | N       |       |
     * | Shutdown_priv         | enum('N','Y')                     | NO   |     | N       |       |
     * | Process_priv          | enum('N','Y')                     | NO   |     | N       |       |
     * | File_priv             | enum('N','Y')                     | NO   |     | N       |       |
     * | Grant_priv            | enum('N','Y')                     | NO   |     | N       |       |
     * | References_priv       | enum('N','Y')                     | NO   |     | N       |       |
     * | Index_priv            | enum('N','Y')                     | NO   |     | N       |       |
     * | Alter_priv            | enum('N','Y')                     | NO   |     | N       |       |
     * | Show_db_priv          | enum('N','Y')                     | NO   |     | N       |       |
     * | Super_priv            | enum('N','Y')                     | NO   |     | N       |       |
     * | Create_tmp_table_priv | enum('N','Y')                     | NO   |     | N       |       |
     * | Lock_tables_priv      | enum('N','Y')                     | NO   |     | N       |       |
     * | Execute_priv          | enum('N','Y')                     | NO   |     | N       |       |
     * | Repl_slave_priv       | enum('N','Y')                     | NO   |     | N       |       |
     * | Repl_client_priv      | enum('N','Y')                     | NO   |     | N       |       |
     * | Create_view_priv      | enum('N','Y')                     | NO   |     | N       |       |
     * | Show_view_priv        | enum('N','Y')                     | NO   |     | N       |       |
     * | Create_routine_priv   | enum('N','Y')                     | NO   |     | N       |       |
     * | Alter_routine_priv    | enum('N','Y')                     | NO   |     | N       |       |
     * | Create_user_priv      | enum('N','Y')                     | NO   |     | N       |       |
     * | Event_priv            | enum('N','Y')                     | NO   |     | N       |       |
     * | Trigger_priv          | enum('N','Y')                     | NO   |     | N       |       |
     * | ssl_type              | enum('','ANY','X509','SPECIFIED') | NO   |     |         |       |
     * | ssl_cipher            | blob                              | NO   |     | NULL    |       |
     * | x509_issuer           | blob                              | NO   |     | NULL    |       |
     * | x509_subject          | blob                              | NO   |     | NULL    |       |
     * | max_questions         | int(11) unsigned                  | NO   |     | 0       |       |
     * | max_updates           | int(11) unsigned                  | NO   |     | 0       |       |
     * | max_connections       | int(11) unsigned                  | NO   |     | 0       |       |
     * | max_user_connections  | int(11) unsigned                  | NO   |     | 0       |       |
     * +-----------------------+-----------------------------------+------+-----+---------+-------+
     * </pre>
     */

    public final List<User> loadUsers(DatabaseSQLInterface dao, String username) throws Exception {

        String whereClause = "";
        if (!isEmpty(username)) {
            whereClause = " where user='" + username + "'";
        }

        List<User> users = new ArrayList<User>();
        List<Row> rows = dao.readRows("select * from mysql.user " + whereClause);

        for (Row row : rows) {
            User item = new User();
            item.setName(getValue(row, "user"));
            item.setHost(getValue(row, "host"));
            item.setPassword(getValue(row, "password"));

            users.add(item);
        }
        return users;
    }

    /**
     * <pre>
     * +---------------+--------------+------+-----+---------+-------+
     * | Field         | Type         | Null | Key | Default | Extra |
     * +---------------+--------------+------+-----+---------+-------+
     * | TABLE_CATALOG | varchar(512) | YES  |     | NULL    |       |
     * | TABLE_SCHEMA  | varchar(64)  | NO   |     |         |       |
     * | TABLE_NAME    | varchar(64)  | NO   |     |         |       |
     * | NON_UNIQUE    | bigint(1)    | NO   |     | 0       |       |
     * | INDEX_SCHEMA  | varchar(64)  | NO   |     |         |       |
     * | INDEX_NAME    | varchar(64)  | NO   |     |         |       |
     * | SEQ_IN_INDEX  | bigint(2)    | NO   |     | 0       |       |
     * | COLUMN_NAME   | varchar(64)  | NO   |     |         |       |
     * | COLLATION     | varchar(1)   | YES  |     | NULL    |       |
     * | CARDINALITY   | bigint(21)   | YES  |     | NULL    |       |
     * | SUB_PART      | bigint(3)    | YES  |     | NULL    |       |
     * | PACKED        | varchar(10)  | YES  |     | NULL    |       |
     * | NULLABLE      | varchar(3)   | NO   |     |         |       |
     * | INDEX_TYPE    | varchar(16)  | NO   |     |         |       |
     * | COMMENT       | varchar(16)  | YES  |     | NULL    |       |
     * +---------------+--------------+------+-----+---------+-------+
     * </pre>
     */
    public final List<Index> loadIndexInfo(DatabaseSQLInterface dao, String databaseName, String tableName, String indexName)
            throws Exception {

        String whereClause = "";
        if (!isEmpty(databaseName) && !isEmpty(tableName) && !isEmpty(indexName)) {
            whereClause = " where table_schema='" + databaseName + "' and table_schema='" + tableName + "' and index_name='"
                    + indexName + "'";
        } else if (!isEmpty(databaseName) && !isEmpty(tableName)) {
            whereClause = " where table_schema='" + databaseName + "' and table_schema='" + tableName + "'";
        } else if (!isEmpty(databaseName)) {
            whereClause = " where table_schema='" + databaseName + "'";
        }

        List<Row> rows = dao.readRows("select * from information_schema.statistics " + whereClause);
        List<Index> indexInfo = new ArrayList<Index>();

        for (Row row : rows) {
            String name = getValue(row, "index_name");
            if ("primary".equalsIgnoreCase(name)) {
                continue;
            }

            Index item = new Index();
            item.setName(name);
            item.setDatabaseName(getDatabaseName(row));
            item.setTableName(getValue(row, "table_name"));
            item.setColumnName(getValue(row, "column_name"));
            item.setDescription(getDescription(row));

            String indexType = getValue(row, "index_type");
            boolean isNotUnique = toBoolean(getValue(row, "non_unique"));
            if ("fulltext".equalsIgnoreCase(indexType)) {
                item.setIndexClass(IndexClassType.FULLTEXT);
                item.setIndexType(IndexType.DEFAULT);
            } else if ("btree".equalsIgnoreCase(indexType)) {
                item.setIndexClass((isNotUnique) ? IndexClassType.DEFAULT : IndexClassType.UNIQUE);
                item.setIndexType(IndexType.BTREE);
            } else if ("spatial".equalsIgnoreCase(indexType)) {
                item.setIndexClass(IndexClassType.SPATIAL);
                item.setIndexType(IndexType.DEFAULT);
            } else if ("hash".equalsIgnoreCase(indexType)) {
                item.setIndexClass(IndexClassType.DEFAULT);
                item.setIndexType(IndexType.HASH);
            } else {
                item.setIndexClass(IndexClassType.DEFAULT);
                item.setIndexType(IndexType.DEFAULT);
            }

            if ("primary".equalsIgnoreCase(name)) {
                item.setEditable(false);
            }

            indexInfo.add(item);
        }
        return indexInfo;
    }

    /**
     * <pre>
     * CONSTRAINT_CATALOG 	
     * CONSTRAINT_SCHEMA 	  	 
     * CONSTRAINT_NAME 	  	 
     * TABLE_CATALOG 	  	 
     * TABLE_SCHEMA 	  	 
     * TABLE_NAME 	  	 
     * COLUMN_NAME 	  	 
     * ORDINAL_POSITION 	  	 
     * POSITION_IN_UNIQUE_CONSTRAINT 	  	 
     * REFERENCED_TABLE_SCHEMA 	  	 
     * REFERENCED_TABLE_NAME 	  	 
     * REFERENCED_COLUMN_NAME
     * </pre>
     */

    public final List<ForeignKey> loadForeignKeys(DatabaseSQLInterface dao, String databaseName, String tableName,
            String foreignKeyName) throws Exception {

        String whereClause = "";
        if (!isEmpty(databaseName) && !isEmpty(tableName) && !isEmpty(foreignKeyName)) {
            whereClause = " where table_schema='" + databaseName + "' and table_schema='" + tableName + "' and constraint_name='"
                    + foreignKeyName + "'";
        } else if (!isEmpty(databaseName) && !isEmpty(tableName)) {
            whereClause = " where table_schema='" + databaseName + "' and table_schema='" + tableName + "'";
        } else if (!isEmpty(databaseName)) {
            whereClause = " where table_schema='" + databaseName + "'";
        }

        List<Row> rows = dao.readRows("select * from information_schema.key_column_usage " + whereClause);
        List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

        for (Row row : rows) {

            if ("primary".equalsIgnoreCase(getValue(row, "constraint_name"))) {
                continue;
            }

            if (getValue(row, "referenced_table_schema") == null || getValue(row, "referenced_table_schema").isEmpty()) {
                continue;
            }

            if (getValue(row, "referenced_table_name") == null || getValue(row, "referenced_table_name").isEmpty()) {
                continue;
            }

            if (getValue(row, "referenced_column_name") == null || getValue(row, "referenced_column_name").isEmpty()) {
                continue;
            }

            String name = getValue(row, "constraint_name");
            String localDatabaseName = getDatabaseName(row);
            String localTableName = getValue(row, "table_name");

            ForeignKey item = DatabaseUtil.findOneItem(foreignKeys, "name", name);

            if (item == null) {
                item = new ForeignKey();
                item.setName(name);
                item.setLocalTable(localTableName);
                item.setLocalDatabase(localDatabaseName);
                item.setForeignDatabase(getValue(row, "referenced_table_schema"));
                item.setForeignTable(getValue(row, "referenced_table_name"));

                item.setConstraintName(name);

                foreignKeys.add(item);
            }

            ForeignKeyReference ref = new ForeignKeyReference();
            ref.setLocalColumn(getValue(row, "column_name"));
            ref.setForeignColumn(getValue(row, "referenced_column_name"));
            ref.setSequenceNumber(toInt(getValue(row, "ordinal_position")));
            int position = toInt(getValue(row, "position_in_unique_constraint"));

            item.getForeignKeyReferences().add(ref);

        }

        return foreignKeys;
    }

    public final void loadExportedKeys(DatabaseSQLInterface dao, Databases databases, String databaseName, String tableName,
            String foreignKeyName) {

        List<ForeignKey> items = new ArrayList<ForeignKey>();
    }

    /**
     * <pre>
     * +----------------+--------------+------+-----+---------+-------+
     * | Field          | Type         | Null | Key | Default | Extra |
     * +----------------+--------------+------+-----+---------+-------+
     * | GRANTEE        | varchar(81)  | NO   |     |         |       |
     * | TABLE_CATALOG  | varchar(512) | YES  |     | NULL    |       |
     * | PRIVILEGE_TYPE | varchar(64)  | NO   |     |         |       |
     * | IS_GRANTABLE   | varchar(3)   | NO   |     |         |       |
     * +----------------+--------------+------+-----+---------+-------+
     * </pre>
     * 
     * @param dao
     * @param privilegeName
     * @return
     * @throws Exception
     */

    public final List<Privilege> loadPrivileges(DatabaseSQLInterface dao, String privilegeName) throws Exception {

        String whereClause = "";
        if (!isEmpty(privilegeName)) {
            whereClause = " where privilege_type='" + privilegeName + "'";
        }

        List<Row> rows = dao.readRows("select * from information_schema.user_privileges " + whereClause);
        List<Privilege> privileges = new ArrayList<Privilege>();

        for (Row row : rows) {
            String grantee = getValue(row, "grantee");
            String privilege = getValue(row, "privilege_type");

            Privilege item = new Privilege();
            item.setGrantee(grantee);
            item.setName(privilege);
            item.setIsGrantable(toBoolean(getValue(row, "is_grantable")));

            privileges.add(item);
        }
        return privileges;
    }

    /**
     * <pre>
     * +----------------------------+--------------+------+-----+---------+-------+
     * | Field                      | Type         | Null | Key | Default | Extra |
     * +----------------------------+--------------+------+-----+---------+-------+
     * | TRIGGER_CATALOG            | varchar(512) | YES  |     | NULL    |       |
     * | TRIGGER_SCHEMA             | varchar(64)  | NO   |     |         |       |
     * | TRIGGER_NAME               | varchar(64)  | NO   |     |         |       |
     * | EVENT_MANIPULATION         | varchar(6)   | NO   |     |         |       |
     * | EVENT_OBJECT_CATALOG       | varchar(512) | YES  |     | NULL    |       |
     * | EVENT_OBJECT_SCHEMA        | varchar(64)  | NO   |     |         |       |
     * | EVENT_OBJECT_TABLE         | varchar(64)  | NO   |     |         |       |
     * | ACTION_ORDER               | bigint(4)    | NO   |     | 0       |       |
     * | ACTION_CONDITION           | longtext     | YES  |     | NULL    |       |
     * | ACTION_STATEMENT           | longtext     | NO   |     | NULL    |       |
     * | ACTION_ORIENTATION         | varchar(9)   | NO   |     |         |       |
     * | ACTION_TIMING              | varchar(6)   | NO   |     |         |       |
     * | ACTION_REFERENCE_OLD_TABLE | varchar(64)  | YES  |     | NULL    |       |
     * | ACTION_REFERENCE_NEW_TABLE | varchar(64)  | YES  |     | NULL    |       |
     * | ACTION_REFERENCE_OLD_ROW   | varchar(3)   | NO   |     |         |       |
     * | ACTION_REFERENCE_NEW_ROW   | varchar(3)   | NO   |     |         |       |
     * | CREATED                    | datetime     | YES  |     | NULL    |       |
     * | SQL_MODE                   | longtext     | NO   |     | NULL    |       |
     * | DEFINER                    | longtext     | NO   |     | NULL    |       |
     * | CHARACTER_SET_CLIENT       | varchar(32)  | NO   |     |         |       |
     * | COLLATION_CONNECTION       | varchar(32)  | NO   |     |         |       |
     * | DATABASE_COLLATION         | varchar(32)  | NO   |     |         |       |
     * +----------------------------+--------------+------+-----+---------+-------+
     * </pre>
     */

    public final List<Trigger> loadTriggers(DatabaseSQLInterface dao, String databaseName, String triggerName) throws Exception {

        String whereClause = "";
        if (!isEmpty(databaseName) && !isEmpty(triggerName)) {
            whereClause = " where trigger_schema='" + databaseName + "' and trigger_name='" + triggerName + "'";
        } else if (!isEmpty(databaseName)) {
            whereClause = " where trigger_schema='" + databaseName + "'";
        }

        List<Trigger> triggers = new ArrayList<Trigger>();
        List<Row> rows = dao.readRows("select * from information_schema.triggers " + whereClause);

        for (Row row : rows) {

            Trigger item = new Trigger();
            item.setName(getValue(row, "trigger_name"));
            item.setDatabaseName(getDatabaseName(row));
            item.setTableName(getValue(row, getValue(row, "EVENT_OBJECT_TABLE")));

            triggers.add(item);
        }
        return triggers;
    }

    /**
     * <pre>
     * +----------------------+--------------+------+-----+---------+-------+
     * | Field                | Type         | Null | Key | Default | Extra |
     * +----------------------+--------------+------+-----+---------+-------+
     * | TABLE_CATALOG        | varchar(512) | YES  |     | NULL    |       |
     * | TABLE_SCHEMA         | varchar(64)  | NO   |     |         |       |
     * | TABLE_NAME           | varchar(64)  | NO   |     |         |       |
     * | VIEW_DEFINITION      | longtext     | NO   |     | NULL    |       |
     * | CHECK_OPTION         | varchar(8)   | NO   |     |         |       |
     * | IS_UPDATABLE         | varchar(3)   | NO   |     |         |       |
     * | DEFINER              | varchar(77)  | NO   |     |         |       |
     * | SECURITY_TYPE        | varchar(7)   | NO   |     |         |       |
     * | CHARACTER_SET_CLIENT | varchar(32)  | NO   |     |         |       |
     * | COLLATION_CONNECTION | varchar(32)  | NO   |     |         |       |
     * +----------------------+--------------+------+-----+---------+-------+
     * </pre>
     */

    public final List<Table> loadViews(DatabaseSQLInterface dao, Databases databases, String databaseName, String tableName)
            throws Exception {

        String whereClause = "";
        if (!isEmpty(databaseName) && !isEmpty(tableName)) {
            whereClause = " where table_schema='" + databaseName + "' and table_name='" + tableName + "'";
        } else if (!isEmpty(databaseName)) {
            whereClause = " where table_schema='" + databaseName + "'";
        }

        List<Table> tables = new ArrayList<Table>();
        List<Row> rows = dao.readRows("select * from information_schema.views " + whereClause);

        for (Row row : rows) {
            String dName = getDatabaseName(row);
            String tName = getValue(row, "table_name");

            Table item = findTable(databases, dName, tName);
            if (item == null) {
                item = new Table();
            }

            item.setDatabaseName(getDatabaseName(row));
            item.setName(getValue(row, "table_name"));

            item.setTableType(TableType.VIEW);
            item.setSqlSelect(getValue(row, "view_definition"));
            item.setDescription(getValue(row, getDescription(row)));

            tables.add(item);
        }
        return tables;
    }

    // -------------------------------------------------------------------------

    public final List<Database> loadProcedureColumns(DatabaseSQLInterface dao, List<Database> databases) {
        return databases;
    }

    /**
     * Get a list of all tables in the database.
     * 
     * @return A list of all tables in the database.
     */

    public final List<Row> loadCharacterSets(DatabaseSQLInterface dao) {
        return readRows(dao, "show character set");
    }

    public final List<Row> loadCollationNames(DatabaseSQLInterface dao) {
        return readRows(dao, "show character set");
    }

    public final void loadSchema(DatabaseSQLInterface dao, Databases databases, String databaseName) throws Exception {

        for (Database database : databases.getDatabases()) {

            mergeTables(databases, loadTables(dao, database.getName(), null));
            mergeTables(databases, loadViews(dao, databases, database.getName(), null));

            for (Table table : database.getTables()) {
                String tableSchema = table.getDatabaseName();

                if (!"public".equalsIgnoreCase(tableSchema)) {
                    continue;
                }

                mergeColumns(databases, loadColumns(dao, database.getName(), table.getName(), null));
            }
        }
    }

    private final static void parseIfEnumType(String columnType, Column item) {
        if (columnType != null && columnType.toLowerCase().startsWith("enum")) {
            int j1 = columnType.indexOf("(");
            int j2 = columnType.lastIndexOf(")");
            String[] tokens = columnType.substring(j1 + 1, j2).split(",");
            if (tokens != null) {
                for (int i = 0; i < tokens.length; i++) {
                    String t = tokens[i].trim();
                    int i1 = t.indexOf("'");
                    int i2 = t.lastIndexOf("'");
                    String token = (i1 == -1 || i2 == -1 || i2 <= i1) ? t : t.substring(i1 + 1, i2);
                    EnumItem enumItem = new EnumItem();
                    enumItem.setName(token);
                    enumItem.setValue(convertViaJavaRules(token));
                    item.getEnumValues().add(enumItem);
                }
            }
        }
    }

    // -------------------------------------------------------------------------

    private final static Table findTable(Databases databases, String databasename, String tablename) {
        for (Database database : databases.getDatabases()) {
            if (databasename.equalsIgnoreCase(database.getName())) {
                for (Table table : database.getTables()) {
                    if (tablename.equalsIgnoreCase(table.getName())) {
                        return table;
                    }
                }
            }
        }
        return null;
    }

    private final static String getDatabaseName(Row row) {

        String name = getValue(row, "catalog_name");
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = getValue(row, "schema_name");
        }
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = getValue(row, "table_catalog");
        }
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = getValue(row, "table_schema");
        }
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = getValue(row, "trigger_catalog");
        }
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = getValue(row, "trigger_schema");
        }
        return name;
    }

    private final static String getDescription(Row row) {

        String name = getValue(row, "remarks");
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = getValue(row, "comment");
        }
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = getValue(row, "table_comment");
        }
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = getValue(row, "column_comment");
        }
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = getValue(row, "routine_comment");
        }
        if (name == null || name.equalsIgnoreCase("def") || name.isEmpty()) {
            name = null;
        }
        return name;
    }

    private final static String convertViaJavaRules(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder buf = new StringBuilder();

        if (!Character.isJavaIdentifierStart(str.charAt(0))) {
            buf.append("_");
        }
        buf.append(str.charAt(0));

        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (!Character.isJavaIdentifierPart(ch)) {
                buf.append("_");
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    private final static EngineType toEngine(String value) {
        EngineType engine = null;
        if (value != null) {
            engine = EngineType.fromValue(toEnum(value).toLowerCase());
        }
        if (engine == EngineType.DEFAULT) {
            engine = EngineType.MYISAM;
        }
        return engine;
    }

    private final static RowFormatType toRowFormatType(String str) {
        RowFormatType value = null;
        if (str != null) {
            value = RowFormatType.valueOf(toEnum(str));
        }
        return value;
    }

    private final static TableType toTableType(String str) {
        TableType value = null;
        if (str != null) {
            value = TableType.valueOf(toEnum(str));
        }
        return value;
    }

    private final static boolean toBoolean(String str) {
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        String strLC = str.toLowerCase();
        if ("t".equals(strLC)) {
            return true;
        }
        if ("f".equals(strLC)) {
            return false;
        }
        if ("true".equals(strLC)) {
            return true;
        }
        if ("false".equals(strLC)) {
            return false;
        }
        if ("yes".equals(strLC)) {
            return true;
        }
        if ("no".equals(strLC)) {
            return false;
        }
        if ("y".equals(strLC)) {
            return true;
        }
        if ("n".equals(strLC)) {
            return false;
        }
        if ("1".equals(strLC)) {
            return true;
        }
        if ("0".equals(strLC)) {
            return false;
        }
        return false;
    }

    private final static int toInt(String str) {
        return (str == null || str.length() == 0) ? 0 : Integer.parseInt(str);
    }

    private final static long toLong(String str) {
        return (str == null || str.length() == 0) ? 0 : Long.parseLong(str);
    }

    private final static String toEnum(String value) {
        return (value == null) ? null : value.toUpperCase().replace(" ", "_");
    }

    private final static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    private final static boolean isEquals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null) {
            return false;
        }
        return (s1.equalsIgnoreCase(s2));
    }

    public String readRows(String databaseName, String tableName) {
        write("select * from ");
        writeIdentifier(databaseName, tableName);

        return getBuffer();
    }

    public final static String toJavaType(String tablename, String name, String dataType, boolean isRequired) {

        String javaType = null;
        if (isRequired) {
            javaType = SQLConversionTables.getJavaTypeNoNulls(dataType);
        } else {
            javaType = SQLConversionTables.getJavaTypeNull(dataType);
        }
        if ("enum".equalsIgnoreCase(javaType)) {
            javaType = CustomXPathFunctions.toJavaNameFromDBName(tablename + "-" + name, true) + "Enum";
        }
        return javaType;
    }

    public final static List<Row> readRows(DatabaseSQLInterface dao, String sql) {
        try {
            return dao.readRows(sql);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<Row>();
    }

    public final static String[] RESERVED_WORDS = new String[] {

            "ACCESSIBLE", "ACTION", "ADD", "AFTER", "AGAINST", "AGGREGATE", "ALGORITHM", "ALL", "ALTER", "ANALYZE", "AND", "ANY",
            "AS", "ASC", "ASCII", "ASENSITIVE", "AT", "AUTHORS", "AUTOEXTEND_SIZE", "AUTO_INCREMENT", "AVG", "AVG_ROW_LENGTH",
            "BACKUP", "BEFORE", "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BINLOG", "BIT", "BLOB", "BLOCK", "BOOL", "BOOLEAN",
            "BOTH", "BTREE", "BY", "BYTE", "CACHE", "CALL", "CASCADE", "CASCADED", "CASE", "CATALOG_NAME", "CHAIN", "CHANGE",
            "CHANGED", "CHAR", "CHARACTER", "CHARSET", "CHECK", "CHECKSUM", "CIPHER", "CLASS_ORIGIN", "CLIENT", "CLOSE",
            "COALESCE", "CODE", "COLLATE", "COLLATION", "COLUMN", "COLUMNS", "COLUMN_NAME", "COMMENT", "COMMIT", "COMMITTED",
            "COMPACT", "COMPLETION", "COMPRESSED", "CONCURRENT", "CONDITION", "CONNECTION", "CONSISTENT", "CONSTRAINT",
            "CONSTRAINT_CATALOG", "CONSTRAINT_NAME", "CONSTRAINT_SCHEMA", "CONTAINS", "CONTEXT", "CONTINUE", "CONTRIBUTORS",
            "CONVERT", "CPU", "CREATE", "CROSS", "CUBE", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER",
            "CURSOR", "CURSOR_NAME", "DATA", "DATABASE", "DATABASES", "DATAFILE", "DATE", "DATETIME", "DAY", "DAY_HOUR",
            "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFINER",
            "DELAYED", "DELAY_KEY_WRITE", "DELETE", "DESC", "DESCRIBE", "DES_KEY_FILE", "DETERMINISTIC", "DIRECTORY", "DISABLE",
            "DISCARD", "DISK", "DISTINCT", "DISTINCTROW", "DIV", "DO", "DOUBLE", "DROP", "DUAL", "DUMPFILE", "DUPLICATE",
            "DYNAMIC", "EACH", "ELSE", "ELSEIF", "ENABLE", "ENCLOSED", "END", "ENDS", "ENGINE", "ENGINES", "ENUM", "ERROR",
            "ERRORS", "ESCAPE", "ESCAPED", "EVENT", "EVENTS", "EVERY", "EXECUTE", "EXISTS", "EXIT", "EXPANSION", "EXPLAIN",
            "EXTENDED", "EXTENT_SIZE", "FALSE", "FAST", "FAULTS", "FETCH", "FIELDS", "FILE", "FIRST", "FIXED", "FLOAT", "FLOAT4",
            "FLOAT8", "FLUSH", "FOR", "FORCE", "FOREIGN", "FOUND", "FRAC_SECOND", "FROM", "FULL", "FULLTEXT", "FUNCTION",
            "GENERAL", "GEOMETRY", "GEOMETRYCOLLECTION", "GET_FORMAT", "GLOBAL", "GRANT", "GRANTS", "GROUP", "HANDLER", "HASH",
            "HAVING", "HELP", "HIGH_PRIORITY", "HOST", "HOSTS", "HOUR", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND",
            "IDENTIFIED", "IF", "IGNORE", "IGNORE_SERVER_IDS", "IMPORT", "IN", "INDEX", "INDEXES", "INFILE", "INITIAL_SIZE",
            "INNER", "INNOBASE", "INNODB", "INOUT", "INSENSITIVE", "INSERT", "INSERT_METHOD", "INSTALL", "INT", "INT1", "INT2",
            "INT3", "INT4", "INT8", "INTEGER", "INTERVAL", "INTO", "INVOKER", "IO", "IO_THREAD", "IPC", "IS", "ISOLATION",
            "ISSUER", "ITERATE", "JOIN", "KEY", "KEYS", "KEY_BLOCK_SIZE", "KILL", "LANGUAGE", "LAST", "LEADING", "LEAVE",
            "LEAVES", "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LINEAR", "LINES", "LINESTRING", "LIST", "LOAD", "LOCAL",
            "LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LOCKS", "LOGFILE", "LOGS", "LONG", "LONGBLOB", "LONGTEXT", "LOOP",
            "LOW_PRIORITY", "MASTER", "MASTER_CONNECT_RETRY", "MASTER_HEARTBEAT_PERIOD", "MASTER_HOST", "MASTER_LOG_FILE",
            "MASTER_LOG_POS", "MASTER_PASSWORD", "MASTER_PORT", "MASTER_SERVER_ID", "MASTER_SSL", "MASTER_SSL_CA",
            "MASTER_SSL_CAPATH", "MASTER_SSL_CERT", "MASTER_SSL_CIPHER", "MASTER_SSL_KEY", "MASTER_SSL_VERIFY_SERVER_CERT",
            "MASTER_USER", "MATCH", "MAXVALUE", "MAX_CONNECTIONS_PER_HOUR", "MAX_QUERIES_PER_HOUR", "MAX_ROWS", "MAX_SIZE",
            "MAX_UPDATES_PER_HOUR", "MAX_USER_CONNECTIONS", "MEDIUM", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MEMORY", "MERGE",
            "MESSAGE_TEXT", "MICROSECOND", "MIDDLEINT", "MIGRATE", "MINUTE", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MIN_ROWS",
            "MOD", "MODE", "MODIFIES", "MODIFY", "MONTH", "MULTILINESTRING", "MULTIPOINT", "MULTIPOLYGON", "MUTEX", "MYSQL_ERRNO",
            "NAME", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NDB", "NDBCLUSTER", "NEW", "NEXT", "NO", "NODEGROUP", "NONE", "NOT",
            "NO_WAIT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "NVARCHAR", "OFFSET", "OLD_PASSWORD", "ON", "ONE", "ONE_SHOT",
            "OPEN", "OPTIMIZE", "OPTION", "OPTIONALLY", "OPTIONS", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "OWNER", "PACK_KEYS",
            "PAGE", "PARSER", "PARTIAL", "PARTITION", "PARTITIONING", "PARTITIONS", "PASSWORD", "PHASE", "PLUGIN", "PLUGINS",
            "POINT", "POLYGON", "PORT", "PRECISION", "PREPARE", "PRESERVE", "PREV", "PRIMARY", "PRIVILEGES", "PROCEDURE",
            "PROCESSLIST", "PROFILE", "PROFILES", "PROXY", "PURGE", "QUARTER", "QUERY", "QUICK", "RANGE", "READ", "READS",
            "READ_ONLY", "READ_WRITE", "REAL", "REBUILD", "RECOVER", "REDOFILE", "REDO_BUFFER_SIZE", "REDUNDANT", "REFERENCES",
            "REGEXP", "RELAY", "RELAYLOG", "RELAY_LOG_FILE", "RELAY_LOG_POS", "RELAY_THREAD", "RELEASE", "RELOAD", "REMOVE",
            "RENAME", "REORGANIZE", "REPAIR", "REPEAT", "REPEATABLE", "REPLACE", "REPLICATION", "REQUIRE", "RESET", "RESIGNAL",
            "RESTORE", "RESTRICT", "RESUME", "RETURN", "RETURNS", "REVOKE", "RIGHT", "RLIKE", "ROLLBACK", "ROLLUP", "ROUTINE",
            "ROW", "ROWS", "ROW_FORMAT", "RTREE", "SAVEPOINT", "SCHEDULE", "SCHEMA", "SCHEMAS", "SCHEMA_NAME", "SECOND",
            "SECOND_MICROSECOND", "SECURITY", "SELECT", "SENSITIVE", "SEPARATOR", "SERIAL", "SERIALIZABLE", "SERVER", "SESSION",
            "SET", "SHARE", "SHOW", "SHUTDOWN", "SIGNAL", "SIGNED", "SIMPLE", "SLAVE", "SLOW", "SMALLINT", "SNAPSHOT", "SOCKET",
            "SOME", "SONAME", "SOUNDS", "SOURCE", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING",
            "SQL_BIG_RESULT", "SQL_BUFFER_RESULT", "SQL_CACHE", "SQL_CALC_FOUND_ROWS", "SQL_NO_CACHE", "SQL_SMALL_RESULT",
            "SQL_THREAD", "SQL_TSI_DAY", "SQL_TSI_FRAC_SECOND", "SQL_TSI_HOUR", "SQL_TSI_MINUTE", "SQL_TSI_MONTH",
            "SQL_TSI_QUARTER", "SQL_TSI_SECOND", "SQL_TSI_WEEK", "SQL_TSI_YEAR", "SSL", "START", "STARTING", "STARTS", "STATUS",
            "STOP", "STORAGE", "STRAIGHT_JOIN", "STRING", "SUBCLASS_ORIGIN", "SUBJECT", "SUBPARTITION", "SUBPARTITIONS", "SUPER",
            "SUSPEND", "SWAPS", "SWITCHES", "TABLE", "TABLES", "TABLESPACE", "TABLE_CHECKSUM", "TABLE_NAME", "TEMPORARY",
            "TEMPTABLE", "TERMINATED", "TEXT", "THAN", "THEN", "TIME", "TIMESTAMP", "TIMESTAMPADD", "TIMESTAMPDIFF", "TINYBLOB",
            "TINYINT", " TINYTEXT", "TO", "TRAILING", "TRANSACTION", "TRIGGER", "TRIGGERS", "TRUE", "TRUNCATE", "TYPE", "TYPES",
            "UNCOMMITTED", "UNDEFINED", "UNDO", "UNDOFILE", "UNDO_BUFFER_SIZE", "UNICODE", "UNINSTALL", "UNION", "UNIQUE",
            "UNKNOWN", "UNLOCK", "UNSIGNED", "UNTIL", "UPDATE", "UPGRADE", "USAGE", "USE", "USER", "USER_RESOURCES", "USE_FRM",
            "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUE", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER",
            "VARIABLES", "VARYING", "VIEW", "WAIT", "WARNINGS", "WEEK", "WHEN", "WHERE", "WHILE", "WITH", "WORK", "WRAPPER",
            "WRITE", "X509", "XA", "XML", "XOR", "YEAR", "YEAR_MONTH", "ZEROFILL"

    };
}
