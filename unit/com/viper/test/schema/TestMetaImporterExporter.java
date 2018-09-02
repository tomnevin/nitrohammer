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

package com.viper.test.schema;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.model.Cell;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.DatabaseConnections;
import com.viper.database.model.Databases;
import com.viper.database.model.ForeignKey;
import com.viper.database.model.ForeignKeyReference;
import com.viper.database.model.IdMethodType;
import com.viper.database.model.Index;
import com.viper.database.model.IndexClassType;
import com.viper.database.model.IndexType;
import com.viper.database.model.JavaNamingMethodType;
import com.viper.database.model.Procedure;
import com.viper.database.model.Row;
import com.viper.database.model.RowFormatType;
import com.viper.database.model.Table;
import com.viper.database.model.TableType;
import com.viper.database.model.UpdateType;
import com.viper.database.model.User;
import com.viper.database.utils.FileUtil;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestMetaImporterExporter extends AbstractTestCase {

	private final static String DATABASE_NAME = "test";

    private final static String EXPECTED_META_FILENAME = "unit/com/viper/test/schema/MetaDatabaseManagerExporter001.xml";
    private final static String ACTUAL_META_FILENAME = "build/MetaDatabaseManagerExporter001.xml";
    private final static String EXPECTED_CONNECTIONS_FILENAME = "res:/com/viper/test/schema/data/databases.xml";
    private final static String ACTUAL_CONNECTIONS_FILENAME = "build/databases.xml";

    private Databases databases = null;
    private DatabaseInterface dao = null;

    @Before
    public void setUp() throws Exception {

        dao = DatabaseFactory.getInstance(DATABASE_NAME);
        assertNotNull("DatabaseFactory should not be null", dao);

        databases = DatabaseMapper.read(Databases.class, EXPECTED_META_FILENAME);
    }

    @After
    public void tearDown() throws Exception {
        dao.release();
    }

    @Test
    public void testRoundTrip() throws Exception {

        DatabaseMapper.writeDatabases(ACTUAL_META_FILENAME, databases);

        String expectContents = FileUtil.readFile(EXPECTED_META_FILENAME);
        String actualContents = FileUtil.readFile(ACTUAL_META_FILENAME);

        // assertEqualsDom(EXPECTED_META_FILENAME + " vs " + ACTUAL_META_FILENAME, expectContents, actualContents);
    }

    @Test
    public void testConnectionsRoundTrip() throws Exception {

        DatabaseConnections expected =  DatabaseMapper.readConnections(EXPECTED_CONNECTIONS_FILENAME);
        DatabaseMapper.writeConnections(ACTUAL_CONNECTIONS_FILENAME, expected);
        DatabaseConnections actual =  DatabaseMapper.readConnections(EXPECTED_CONNECTIONS_FILENAME);

        assertNotNull(getCallerMethodName(), actual);
        assertEquals(getCallerMethodName(), actual.getConnections().size(), expected.getConnections().size());
    }

    @Test
    public void testDatabase() throws Exception {
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        assertEquals("test", database.getName());
        assertEquals("utf8", database.getCharsetName());
        assertEquals("unknown", database.getCollationName());
        assertEquals(JavaNamingMethodType.NOCHANGE, database.getDefaultJavaNamingMethod());
        assertEquals("3.2", database.getVersion());
        assertEquals("build/src/com/viper/test.java", database.getFilename());
        assertEquals("Tables.size()", 2, database.getTables().size());
    }

    @Test
    public void testBasicTable() throws Exception {
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table not found", table);

        assertEquals("basic_table", table.getName());
        assertEquals(null, table.getBaseClass());
        assertEquals(null, table.getCharsetName());
        assertEquals(null, table.getCollationName());
        assertEquals(false, table.isDelayKeyWrite());
        assertEquals(null, table.getDescription());
        assertEquals(null, table.getEngine());
        assertEquals(false, table.isHasChecksum());
        assertEquals(null, table.getInterface());
        assertEquals(null, table.getIndexDirectory());
        assertEquals(true, table.isIsAbstract());
        assertEquals(true, table.isIsDefault());
        assertEquals(null, table.getMaximumRows());
        assertEquals(null, table.getMinimumRows());
        assertEquals("1", table.getPackKeys());
        assertEquals("tnevin", table.getPassword());
        assertEquals("striped", table.getRaidType());
        assertEquals("32", table.getRaidChunks());
        assertEquals(new Integer(512), table.getRaidChunkSize());
        assertEquals(RowFormatType.DEFAULT, table.getRowFormat());
        assertEquals(true, table.isSkipSql());
        assertEquals(TableType.TABLE, table.getTableType());
        assertEquals("union", table.getUnion());
        assertEquals("select * from table", table.getSqlSelect());
        assertEquals("update a=\"1\", b=\"2\" into table where c=\"3\"", table.getSqlUpdate());
        assertEquals("insert (a, b, c) values (1, 2, 4) from table", table.getSqlInsert());
        assertEquals("delete table", table.getSqlDelete());

        assertEquals("TableName", "basic_table", table.getName());
        assertEquals("DatabaseName", "test", database.getName());
        assertEquals("Columns.size()", 2, table.getColumns().size());
    }

    @Test
    public void testColumnBasicTableId() throws Exception {
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table not found", table);

        Column column = DatabaseUtil.findOneItem(table.getColumns(), "name", "ID");
        assertNotNull("basic_table COLUMN ID not found", column);
        assertEquals("ID", column.getName());
        assertEquals(null, column.getDecimalSize());
        assertEquals(null, column.getDefaultValue());
        assertEquals(10, column.getSize());
        assertEquals(IdMethodType.NONE, column.getIdMethod());
        assertEquals(false, column.isNaturalKey());
        assertEquals(true, column.isPrimaryKey());
        assertEquals(true, column.isPersistent());
        assertEquals(false, column.isRequired());
        assertEquals("int", column.getJavaType());
        assertEquals(null, column.getLogicalType());
        assertEquals(null, column.getMinimumValue());
        assertEquals(null, column.getMaximumValue());
        assertEquals(10L, column.getSize());
        assertEquals("Validator.class", column.getValidator());
    }

    @Test
    public void testColumnBasicTableName() throws Exception {
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table not found", table);

        Column column = DatabaseUtil.findOneItem(table.getColumns(), "name", "NAME");
        assertNotNull("basic_table COLUMN NAME not found", column);

        assertEquals("NAME", column.getName());
        assertEquals(null, column.getDecimalSize());
        assertEquals(null, column.getDefaultValue());
        assertEquals(null, column.getDescription());
        assertEquals(0, column.getSize());
        assertEquals(IdMethodType.NONE, column.getIdMethod());
        assertEquals(false, column.isNaturalKey());
        assertEquals(false, column.isPrimaryKey());
        assertEquals(true, column.isPersistent());
        assertEquals(false, column.isRequired());
        assertEquals("String", column.getJavaType());
        assertEquals(null, column.getLogicalType());
        assertEquals(null, column.getMinimumValue());
        assertEquals(null, column.getMaximumValue());
        assertEquals(0L, column.getSize());
        assertEquals("Validator.class", column.getValidator());
    }

    @Test
    public void testUser() throws Exception {

        List<User> users = databases.getUsers();
        assertNotNull("No users found", users);
        assertEquals("Wrong number of users found", 1, users.size());

        User user = users.get(0);
        assertNotNull("No user found", user);

        assertEquals("demo", user.getName());
        assertEquals("pass", user.getPassword());
    }

    @Test
    public void testProcedure() throws Exception {
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        List<Procedure> procedures = database.getProcedures();
        assertNotNull("No procedures found", procedures);
        assertEquals("Wrong number of procedures found", 1, procedures.size());

        Procedure procedure = procedures.get(0);
        assertNotNull("No procedure found", procedure);

        assertEquals("testProcedure", "CalculatSums", procedure.getName());
        // assertEquals("testProcedure", "etc/CalculateSums.proc",
        // procedure.getFilename());
        assertEquals("testProcedure", "sample procedure description", procedure.getDescription());
        assertEquals("testProcedure", "oracle", procedure.getVendor());
        assertEqualsSorta("procedure source", "procedure sum(int a, int b) {  return a + b; }", procedure.getSource().trim());
    }

    @Test
    public void testForeignKey() throws Exception {
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table not found", table);

        List<ForeignKey> foreignKeys = table.getForeignKeys();
        assertNotNull("No foreignKeys found", foreignKeys);
        assertEquals("Wrong number of foreignKeys found", 1, foreignKeys.size());

        ForeignKey foreignKey = foreignKeys.get(0);
        assertNotNull("No foreignKey found", foreignKey);

        assertEquals("constrain0002", foreignKey.getConstraintName());
        assertEquals("local-key-name", foreignKey.getName());
        assertEquals("foreign-catalog", foreignKey.getForeignDatabase());
        assertEquals("foreign-table", foreignKey.getForeignTable());
        assertEquals("deferrability", foreignKey.getDeferrability());
        assertEquals(UpdateType.RESTRICT, foreignKey.getOnDelete());
        assertEquals(UpdateType.CASCADE, foreignKey.getOnUpdate());
        assertEquals(true, foreignKey.isUnique());
    }

    @Test
    public void testReference() throws Exception {
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");
        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table not found", table);

        List<ForeignKey> foreignKeys = table.getForeignKeys();
        assertNotNull("No foreignKeys found", foreignKeys);
        assertEquals("Wrong number of foreignKeys found", 1, foreignKeys.size());

        ForeignKey foreignKey = foreignKeys.get(0);
        assertNotNull("No foreignKey found", foreignKey);

        List<ForeignKeyReference> references = foreignKey.getForeignKeyReferences();
        assertNotNull("No reference found", references);
        assertEquals("Wrong number of reference found", 1, references.size());

        ForeignKeyReference reference = references.get(0);
        assertNotNull("No reference found", reference);

        assertEquals("local-column-name", reference.getLocalColumn());
        assertEquals("foreign-column", reference.getForeignColumn());
    }

    @Test
    public void testIndex() throws Exception {
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");
        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table not found", table);

        List<Index> indicies = table.getIndices();
        assertNotNull("No indexes found", indicies);
        assertEquals("Wrong number of indexes found", 2, indicies.size());

        Index index = indicies.get(0);
        assertNotNull("No index found", index);

        assertEquals("indexname", index.getName());
        assertEquals(IndexClassType.UNIQUE, index.getIndexClass());
        assertEquals(IndexType.BTREE, index.getIndexType());
        
        List<String> indexColumns  = new ArrayList<String>();
        for (Column column : table.getColumns()) {
            if (index.getName().equalsIgnoreCase(column.getIndexName())) {
                indexColumns.add(column.getName());
            }
        }

        assertNotNull("No indexColumns found", indexColumns);
        assertEquals("Wrong number of indexColumns found", 1, indexColumns.size());

        String indexColumn = indexColumns.get(0);
        assertNotNull("No IndexColumn found", indexColumn);

    }

    @Test
    public void testRows() throws Exception {
        Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

        Table table = DatabaseUtil.findOneItem(database.getTables(), "name", "basic_table");
        assertNotNull("basic_table not found", table);

        List<Row> rows = table.getRows();
        assertNotNull("basic_table Rows not found", rows);
        assertEquals("basic_table Rows not found", 3, rows.size());

        Row row = null;
        Cell idCell = null;
        Cell nameCell = null;

        row = rows.get(0);
        idCell = DatabaseUtil.findOneItem(row.getCells(), "name", "ID");
        nameCell = DatabaseUtil.findOneItem(row.getCells(), "name", "NAME");
        assertNotNull("basic_table Row[0] not found", row);
        assertNotNull("basic_table Cell[id] not found", idCell);
        assertNotNull("basic_table Cell[name] not found", nameCell);
        assertEquals("row[0].ID", "1", idCell.getValue());
        assertEquals("row[0].NAME", "Tom", nameCell.getValue());

        row = rows.get(1);
        idCell = DatabaseUtil.findOneItem(row.getCells(), "name", "ID");
        nameCell = DatabaseUtil.findOneItem(row.getCells(), "name", "NAME");
        assertNotNull("basic_table Row[1] not found", row);
        assertEquals("row[1].ID", "2", idCell.getValue());
        assertEquals("row[1].NAME", "Dana", nameCell.getValue());

        row = rows.get(2);
        idCell = DatabaseUtil.findOneItem(row.getCells(), "name", "ID");
        nameCell = DatabaseUtil.findOneItem(row.getCells(), "name", "NAME");
        assertNotNull("basic_table Row[2] not found", row);
        assertEquals("row[2].ID", "3", idCell.getValue());
        assertEquals("row[2].NAME", "Chrissy", nameCell.getValue());
    }
}
