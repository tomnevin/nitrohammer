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

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.Procedure;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestSQLProcedure extends AbstractTestCase {

	private final static String META_FILENAME = "res:/com/viper/test/schema/MetaDatabaseManagerExporter001.xml";
	private final static String procedureName = "CalculatSums";

    private static final SQLDriver driver = new SQLDriver();

	@Rule
	public MethodRule benchmarkRule = new BenchmarkRule();

	@Test
	public void testMetaProcedure() throws Exception {
		Databases databases = DatabaseMapper.read(Databases.class, META_FILENAME);

		Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

		Procedure proc = DatabaseUtil.findOneItem(database.getProcedures(), "name", procedureName);
		assertNotNull(procedureName + " procedure not found", proc);

		List<Procedure> list = database.getProcedures();
		assertNotNull("procedures not found", list);

		assertEquals("number of procedures mismatched", 1, list.size());
	}

	@Test
	public void testCreateProcedure() throws Exception {
		Databases databases = DatabaseMapper.read(Databases.class, META_FILENAME);

		Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

		Procedure proc = DatabaseUtil.findOneItem(database.getProcedures(), "name", procedureName);
		assertNotNull(procedureName + " procedure not found", proc);

		String sql = driver.createProcedure(database, proc);

		assertNotNull("create Procedure", sql);
		assertEqualsSorta("create procedure test.sum(int a, int b) { return a + b; }", sql.trim());
	}

	@Test
	public void testDropProcedure() throws Exception {
		Databases databases = DatabaseMapper.read(Databases.class, META_FILENAME);

		Database database = DatabaseUtil.findOneItem(databases.getDatabases(), "name", "test");

		Procedure proc = DatabaseUtil.findOneItem(database.getProcedures(), "name", procedureName);
		assertNotNull(procedureName + " procedure not found", proc);

		String sql = driver.dropProcedure(database, proc);

		assertNotNull("dropProcedure", sql);
		assertEqualsSorta("drop procedure if exists " + database.getName() + "." + procedureName, sql.trim());
	}
}
