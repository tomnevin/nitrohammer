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

package com.viper.test.simple;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.DatabaseWriter;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.DatabaseConnections;
import com.viper.database.model.Databases;
import com.viper.database.simple.Database;
import com.viper.database.simple.Row;
import com.viper.database.tools.SqlConverter;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSimpleDatabase extends AbstractTestCase {

	private final static String DATABASE_NAME = "test";
	private final static SqlConverter sqlManager = new SqlConverter();
	private final static String ConnectionsFilename = "res:/databases.xml";
	private final static String TEST_FILENAME_001 = "res:/com/viper/test/simple/TestSimpleDatabase001.xml";

	private DatabaseConnection dbc = null;

	@Rule
	public TestRule benchmarkRule = new BenchmarkRule();

	@Before
	public void setUp() throws Exception {

		DatabaseConnections dbcs = DatabaseMapper.readConnections(ConnectionsFilename);
		assertNotNull("Database connections(databases.xml) empty", dbcs);

		dbc = DatabaseUtil.findOneItem(dbcs.getConnections(), "name", DATABASE_NAME);
		assertNotNull("Database connection (test) not found", dbc);

		DatabaseSQLInterface dao = (DatabaseSQLInterface)DatabaseFactory.getInstance(dbc);
		assertNotNull("JDBCDriver should not be null", dao);
		
		Databases databases = DatabaseMapper.readDatabases(TEST_FILENAME_001);
		assertNotNull("databases should not be null: " + TEST_FILENAME_001, databases);

		sqlManager.write(new DatabaseWriter(dao), dbc.getVendor(), databases);
	}

	@Test
	public void testDatabaseConnect() throws Exception {
		Database database = new Database();
		database.connect(dbc.getDriver(), dbc.getDatabaseUrl(), dbc.getUsername(), dbc.getPassword());

		database.close();
	}

	@Test
	public void testDatabaseDropTable() throws Exception {

		Database database = new Database();
		String sql = database.generateDrop("test", "activity");

		assertEquals(getCallerMethodName(), "DROP TABLE test.activity;\n", sql);
	}

	@Test
	public void testDatabaseCreate() throws Exception {
		Database database = new Database();
		database.connect(dbc.getDriver(), dbc.getDatabaseUrl(), dbc.getUsername(), dbc.getPassword());

		String sql = clean(database.generateCreate("test", "activity"));

		database.close();

		assertEqualsIgnoreCase(getCallerMethodName(),
		        "create table test.activity ( id integer(10) not null auto_increment,name varchar(50) not null ,organizationid integer(10) not null ,activitycategoryid integer(10) not null ,description varchar(200) null ,ispaid tinyint(3) not null ,isdeleted tinyint(3) not null ,adherencetolerance smallint(5) null ,isusedinshift tinyint(3) not null ,color varchar(6) null ,isusedinshiftevent tinyint(3) not null ,isusedincalendarevent tinyint(3) not null ,isunavailability tinyint(3) not null ,isrequestable tinyint(3) not null ,isout tinyint(3) not null ,istimeoff tinyint(3) not null ,istimeoffwithallotment tinyint(3) not null ,colorcode varchar(4) null ,earningtypeid integer(10) not null ,maxdurationthreshold integer(10) not null ,modifiedby varchar(50) null ,lastmodifiedat timestamp(19) not null ,sourcemeasureid integer(10) null ,isvisibletotcmapping tinyint(3) not null ,isqueuehopping tinyint(3) not null ,primary key(id) ); ",
		        sql);
	}

	@Test
	public void testDatabaseQueryRows() throws Exception {

		Database database = new Database();
		database.connect(dbc.getDriver(), dbc.getDatabaseUrl(), dbc.getUsername(), dbc.getPassword());

		List<Row> table = database.executeQueryRows("select * from test.basic_table");

		database.close();

		assertEquals(getCallerMethodName(), 3, table.size());
	}

	@Test
	public void testDatabaseListTables() throws Exception {
		Database database = new Database();
		database.connect(dbc.getDriver(), dbc.getDatabaseUrl(), dbc.getUsername(), dbc.getPassword());

		List<String> tables = database.listTables();

		database.close();

		for (String tablename : tables) {
			System.out.println("testDatabaseListTables:" + tablename);
		}

		assertTrue(getCallerMethodName() + ":" + dbc.getDatabaseUrl(), tables.size() > 0);
	}

	@Test
	public void testDatabaseTableExists() throws Exception {

		Database database = new Database();
		database.connect(dbc.getDriver(), dbc.getDatabaseUrl(), dbc.getUsername(), dbc.getPassword());

		boolean exists = database.tableExists("SecondaryTable");

		database.close();

		assertEquals(getCallerMethodName() + ":" + dbc.getDatabaseUrl(), true, exists);
	}

	@Test
	public void testDatabaseListColumns() throws Exception {

		Database database = new Database();
		database.connect(dbc.getDriver(), dbc.getDatabaseUrl(), dbc.getUsername(), dbc.getPassword());

		List<String> columns = database.listColumns("SecondaryTable");

		database.close();

		assertEquals(getCallerMethodName() + ":" + dbc.getDatabaseUrl(), 25, columns.size());
	}
}
