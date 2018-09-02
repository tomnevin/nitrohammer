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

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.utils.DatabaseRegistry;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.demo.beans.model.BeanGeneration;

public class TestGenerator extends AbstractTestCase {

	@BeforeClass
	public static void initializeClass() throws Exception {

		Logger.getGlobal().setLevel(Level.INFO);

		DatabaseRegistry.getInstance();
	}

	private DatabaseInterface getDatabase() throws Exception {
		// connection.setPackageName("com.viper.primefaces.model");
		DatabaseInterface database = DatabaseFactory.getInstance("test");

		if (!DatabaseUtil.isTableExist(database, "test", "bean_generation")) {
			database.create(BeanGeneration.class);
		}
		return database;
	}

	@Test
	public void testGeneration() throws Exception {

		DatabaseInterface database = getDatabase();
		List<BeanGeneration> expected = RandomBean.getRandomBeans(BeanGeneration.class, 100, 107);
		database.deleteAll(BeanGeneration.class);
		database.insertAll(expected);

		Collection<BeanGeneration> beans = database.queryAll(BeanGeneration.class);
		Assert.assertNotNull(getCallerMethodName() + " null - could not find BeanGeneration", beans);
		Assert.assertEquals(getCallerMethodName() + " size - could not find BeanGeneration: " + beans.size(),
				beans.size(), expected.size());

		for (BeanGeneration bean : beans) {
			Assert.assertNotEquals(getCallerMethodName() + " A = 0", bean.getA());
			Assert.assertNotEquals(getCallerMethodName() + " B = 0", bean.getB());
			Assert.assertNotEquals(getCallerMethodName() + " C = 0", bean.getC());
			Assert.assertEquals(getCallerMethodName() + " C /= A + B", bean.getC(), bean.getA() + bean.getB());
		}
	}
}