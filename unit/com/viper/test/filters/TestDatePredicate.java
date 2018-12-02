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

import org.junit.Test;

import com.viper.database.filters.DateOperator;
import com.viper.database.filters.DatePredicate;
import com.viper.database.filters.Predicate;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.demo.beans.model.Bean;

public class TestDatePredicate extends AbstractTestCase {

    private static final long ONE_YEAR = 365 * 24 * 60 * 60 * 1000;
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;
    private final RandomBean randomBean = new RandomBean();

    private Bean buildTestData() {
        Bean bean = new Bean();

        bean.setId(randomBean.randomInt(10000));
        bean.setIntField(randomBean.randomInt(10000));
        bean.setStringField(randomBean.randomString(20));
        bean.setDate1Field(randomBean.randomDate(System.currentTimeMillis(), ONE_YEAR, ONE_YEAR));
        bean.setDate2Field(randomBean.randomDate(System.currentTimeMillis(), ONE_YEAR, ONE_YEAR));

        return bean;
    }
    
    private Bean buildDayTestData() {
        Bean bean = new Bean();

        bean.setId(randomBean.randomInt(10000));
        bean.setIntField(randomBean.randomInt(10000));
        bean.setStringField(randomBean.randomString(20));
        bean.setDate1Field(randomBean.randomDate(System.currentTimeMillis(), ONE_DAY, ONE_DAY));
        bean.setDate2Field(randomBean.randomDate(System.currentTimeMillis(), ONE_DAY, ONE_DAY));

        return bean;
    }

    @Test
    public void testDatePredicateEquals() throws Exception {

        Bean bean = buildTestData();

        assertNotNull(getCallerMethodName(), bean);
        assertNotNull(getCallerMethodName(), bean.getDate1Field());
        assertNotNull(getCallerMethodName(), bean.getDate2Field());

        Predicate predicate = new DatePredicate("date1Field", DateOperator.EQUALS, bean.getDate1Field());

        boolean result = predicate.apply(bean);

        assertTrue(getCallerMethodName(), result);
    }

    @Test
    public void testDatePredicateNotEquals() throws Exception {

        Bean bean = buildTestData();

        assertNotNull(getCallerMethodName(), bean);
        assertNotNull(getCallerMethodName(), bean.getDate1Field());
        assertNotNull(getCallerMethodName(), bean.getDate2Field());

        Predicate predicate = new DatePredicate("date2Field", DateOperator.NOT_EQUALS, bean.getDate1Field());

        boolean result = predicate.apply(bean);

        assertFalse(getCallerMethodName() + "=" +  bean.getDate1Field() + " vs " +  bean.getDate2Field(), result);
    }
    

    @Test
    public void testDatePredicateLastNHours() throws Exception {

        Bean bean = buildDayTestData();

        assertNotNull(getCallerMethodName(), bean);
        assertNotNull(getCallerMethodName(), bean.getDate1Field());
        assertNotNull(getCallerMethodName(), bean.getDate2Field());

        Predicate predicate = new DatePredicate("date1Field", DateOperator.LAST_N_HOURS, 24);

        boolean result = predicate.apply(bean);

        assertTrue(getCallerMethodName(), result);
    }

    @Test
    public void testDatePredicateNextNHours() throws Exception {

        Bean bean = buildDayTestData();

        assertNotNull(getCallerMethodName(), bean);
        assertNotNull(getCallerMethodName(), bean.getDate1Field());
        assertNotNull(getCallerMethodName(), bean.getDate2Field());

        Predicate predicate = new DatePredicate("date1Field", DateOperator.NEXT_N_HOURS, 24);

        boolean result = predicate.apply(bean);

        assertTrue(getCallerMethodName(), result);
    }
}
