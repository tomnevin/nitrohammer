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

package com.viper.test.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.viper.demo.beans.model.Bean;
import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.converters.ConverterUtils;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;

public class TestConverterUtils extends AbstractTestCase {

	private final static String DATABASE_NAME = "local";

	@Rule
	public TestRule benchmarkRule = new BenchmarkRule();

	@BeforeClass
	public static void initializeClass() throws Exception {
		Logger.getGlobal().setLevel(Level.INFO);
	}

	@Test
	public void testToAndFromJson1() throws Exception {

		String expected = "name";
		DatabaseConnection connection1 = new DatabaseConnection();
		connection1.setName(expected);

		String json = ConverterUtils.writeJson(connection1);
		assertNotNull(getCallerMethodName() + " : did not get value ", json);

		DatabaseConnection connection2 = ConverterUtils.readJson(json, DatabaseConnection.class);
		assertNotNull(getCallerMethodName() + " : did not get value ", connection2);
		assertEquals(getCallerMethodName() + " : names do not match ", connection1.getName(), connection2.getName());
	}

	@Test
	public void testToAndFromJson2() throws Exception {

		DatabaseConnection dbc1 = DatabaseFactory.getDatabaseConnection(null, DATABASE_NAME);
		assertNotNull(getCallerMethodName() + " : dbc1 not loaded ", dbc1);

		String json = ConverterUtils.writeJson(dbc1);
		assertNotNull(getCallerMethodName() + " : dbc1 not converted to json ", json);

		DatabaseConnection dbc2 = ConverterUtils.readJson(json, DatabaseConnection.class);
		assertNotNull(getCallerMethodName() + " : dbc2 not create from json ", dbc2);
        assertBeanEquals(getCallerMethodName() + " : beans do not match ", dbc1, dbc2);
	}

    @Test
    public void testToAndFromJson3() throws Exception {

        Bean bean1 = RandomBean.getRandomBean(Bean.class, 324573789);
        assertNotNull(getCallerMethodName() + " : bean1 not created ", bean1);

        String json = ConverterUtils.writeJson(bean1);
        assertNotNull(getCallerMethodName() + " : bean1 not converted to json ", json);

        Bean bean2 = ConverterUtils.readJson(json, Bean.class);
        assertNotNull(getCallerMethodName() + " : bean2 not create from json ", bean2);
        assertBeanEquals(getCallerMethodName() + " : beans do not match ", bean1, bean2);
    }

	@Test
	public void testToFromJsonList1() throws Exception {

		String json = "[\"tom@viper.com\",\"john@viper.com\",\"bill@viper.com\"]";
		List<String> expected = new ArrayList<String>();
		{
			expected.add("tom@viper.com");
			expected.add("john@viper.com");
			expected.add("bill@viper.com");
		}

		List<String> actual = ConverterUtils.readJsonToList(json, String.class);
		assertNotNull(getCallerMethodName() + " : did not get value ", actual);
		assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);

		String actualJson = ConverterUtils.writeJson(expected);
		assertNotNull(getCallerMethodName() + " : did not get value ", actualJson);
		assertEquals(getCallerMethodName() + " : value do not match ", json, actualJson);
	}

	@Test
	public void testToFromJsonMap1() throws Exception {

		String json = "{\"tom\":\"tom@viper.com\",\"bill\":\"bill@viper.com\",\"john\":\"john@viper.com\"}";
		Map<String, Object> expected = new HashMap<String, Object>();
		{
		    expected.put("tom", "tom@viper.com");
		    expected.put("john", "john@viper.com");
		    expected.put("bill", "bill@viper.com");
		}

		String actualJson = ConverterUtils.writeJsonFromMap(expected);
		assertNotNull(getCallerMethodName() + " : did not get value ", actualJson);
		assertEquals(getCallerMethodName() + " : value do not match ", json, actualJson);

		Map<String, Object> actual = ConverterUtils.readJsonToMap(json);
		assertNotNull(getCallerMethodName() + " : did not get value ", actual);
		assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);

	}
}