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

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.managers.DatabaseMgr;
import com.viper.database.model.Databases;
import com.viper.database.utils.FileUtil;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestMetaConverter extends AbstractTestCase {

	private final static String DATABASE_NAME = "test";
    private static final SQLDriver driver = new SQLDriver();

	private DatabaseSQLInterface dao = null;

	@Before
	public void setUp() throws Exception {

		dao = (DatabaseSQLInterface)DatabaseFactory.getInstance(DATABASE_NAME);
		assertNotNull("JDBCDriver should not be null", dao);

	}

	@After
	public void tearDown() throws Exception {
	    ((DatabaseInterface)dao).release();
	}

	@Test
	public void testRoundTrip() throws Exception {

		Databases databases = driver.load(dao, null, null);
		// for (Database database : databases.getDatabase()) {
		// System.out.println("Database: " + database.getName());
		// }

		List<String> schemas = DatabaseMgr.listSchemas(databases.getDatabases());
		// for (String schema : schemas) {
		// System.out.println("schema: " + schema);
		// }

		databases = driver.load(dao, null, null);

		String ACTUAL_META_DIRECTORY = "build/actual-converter-files";
		DatabaseMapper.writeDatabases(ACTUAL_META_DIRECTORY, databases);

		String EXPECT_META_DIRECTORY = "unit/com/viper/test/schema/data";
		File file = new File(EXPECT_META_DIRECTORY);
		assertTrue(EXPECT_META_DIRECTORY + " should be a directory", file.isDirectory());

		File files[] = file.listFiles();
		if (files != null) {
			for (File f : files) {
				String expectFilename = f.getAbsolutePath();
				String actualFilename = ACTUAL_META_DIRECTORY + "/" + f.getName();
				if (expectFilename.startsWith(".xml")) {
					String expectContents = FileUtil.readFile(expectFilename);
					String actualContents = FileUtil.readFile(actualFilename);

					assertEqualsSorta(expectFilename + " vs " + actualFilename, expectContents, actualContents);
				}
			}
		}
	}
}