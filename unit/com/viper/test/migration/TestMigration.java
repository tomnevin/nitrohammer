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

package com.viper.test.migration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestMigration extends AbstractTestCase {

	private final static String DATABASE_NAME = "test";
	private final static String DRIVER_NAME = "mysql";

	@Rule
	public MethodRule benchmarkRule = new BenchmarkRule();

	@Test
	public void testDatabaseDriver() throws Exception {

		DatabaseSQLInterface dao = (DatabaseSQLInterface)DatabaseFactory.getInstance(DATABASE_NAME);
		assertNotNull("JDBCDriver should not be null", dao);

		SQLDriver driver = new SQLDriver();
		assertNotNull("Driver should not be null: " + DRIVER_NAME, driver);

		assertTrue("Meta.getAttributes", dao.readMetaRows("attributes").size() >= 0);
		assertTrue("Meta.getCrossReference", dao.readMetaRows("crossreference").size() > 0);
		assertTrue("Meta.getPrimaryKeys", dao.readMetaRows("primarykeys").size() > 0);
		assertTrue("Meta.getTriggerInfo", dao.readMetaRows("triggerinfo").size() > 0);
		assertTrue("Meta.getTypeInfo", dao.readMetaRows("typeinfo").size() > 0);
		assertTrue("Meta.getSupportsConvertByType", dao.readMetaRows("SupportsConvert").size() > 0);

		((DatabaseInterface)dao).release();
	}
}
