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

package com.viper.test.filters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.viper.database.dao.DatabaseUtil;
import com.viper.database.filters.DateOperator;
import com.viper.database.filters.DatePredicate;
import com.viper.database.filters.NumberOperator;
import com.viper.database.filters.NumberPredicate;
import com.viper.database.filters.Predicate;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.demo.beans.model.Bean;

public class TestPredicates extends AbstractTestCase {

    private static final long ONE_YEAR = 365 * 24 * 60 * 60 * 1000;
    private final RandomBean randomBean = new RandomBean();

    private List<Bean> buildTestData(int nbeans) {
        List<Bean> beans = new ArrayList<Bean>();

        for (int i = 0; i < nbeans; i++) {
            Bean bean = new Bean();

            bean.setId(i);
            bean.setIntField(i);
            bean.setStringField(randomBean.randomString(20));
            bean.setDate1Field(randomBean.randomDate(System.currentTimeMillis(), ONE_YEAR, ONE_YEAR));
            bean.setDate2Field(randomBean.randomDate(System.currentTimeMillis(), ONE_YEAR, ONE_YEAR));

            beans.add(bean);
        }
        return beans;
    }

    @Test
    public void testNumberPredicate() throws Exception {

        int nbeans = 100;
        List<Bean> beans = buildTestData(nbeans);
        assertNotNull(getCallerMethodName(), beans);
        assertEquals(getCallerMethodName(), nbeans, beans.size());

        Predicate predicate = new NumberPredicate("intField", NumberOperator.EQUALS, nbeans / 2);

        List<Bean> results = DatabaseUtil.applyFilter(beans, predicate);

        assertNotNull(getCallerMethodName(), results);
        assertEquals(getCallerMethodName(), 1, results.size());
    }

    @Test
    public void testDatePredicateEquals() throws Exception {

        int nbeans = 100;
        List<Bean> beans = buildTestData(nbeans);

        assertNotNull(getCallerMethodName(), beans);
        assertEquals(getCallerMethodName(), nbeans, beans.size());

        for (Bean bean : beans) {
            assertNotNull(getCallerMethodName(), bean.getDate1Field());
            assertNotNull(getCallerMethodName(), bean.getDate2Field());
        }

        Predicate predicate = new DatePredicate("date1Field", DateOperator.EQUALS,
                beans.get(nbeans / 2).getDate1Field());

        List<Bean> results = DatabaseUtil.applyFilter(beans, predicate);

        assertNotNull(getCallerMethodName(), beans.get(nbeans / 2).getDate1Field());
        assertNotNull(getCallerMethodName(), results);
        assertEquals(getCallerMethodName(), 1, results.size());
    }

    @Test
    public void testDatePredicateNotEquals() throws Exception {

        int nbeans = 100;
        List<Bean> beans = buildTestData(nbeans);
        assertNotNull(getCallerMethodName(), beans);
        assertEquals(getCallerMethodName(), nbeans, beans.size());

        Date date = beans.get(nbeans / 2).getDate1Field();
        Predicate predicate = new DatePredicate("date1Field", DateOperator.NOT_EQUALS, date);

        List<Bean> results = DatabaseUtil.applyFilter(beans, predicate);

        assertNotNull(getCallerMethodName(), results);
        assertEquals(getCallerMethodName(), nbeans - 1, results.size());
    }
}
