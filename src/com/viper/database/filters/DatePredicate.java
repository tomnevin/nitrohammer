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

package com.viper.database.filters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.converters.DateConverter;

public class DatePredicate<T> implements Predicate<T> {

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private String fieldname = null;
    private DateOperator operator = null;
    private Date filterValue = null;
    private Date filterValue2 = null;
    private int ncount = 1;

    public DatePredicate(String fieldname, DateOperator operator, Date filterValue) {
        this.fieldname = fieldname;
        this.operator = operator;
        this.filterValue = filterValue;
    }

    public DatePredicate(String fieldname, DateOperator operator, Date filterValue1, Date filterValue2) {
        this.fieldname = fieldname;
        this.operator = operator;
        this.filterValue = filterValue1;
        this.filterValue2 = filterValue2;
    }

    public DatePredicate(String fieldname, DateOperator operator, int ncount) {
        this.fieldname = fieldname;
        this.operator = operator;
        this.ncount = ncount;
    }

    public DatePredicate(String fieldname, DateOperator operator) {
        this.fieldname = fieldname;
        this.operator = operator;
    }

    @Override
    public boolean apply(T bean) {

        String name = fieldname.substring(fieldname.lastIndexOf('.') + 1);
        Object o = DatabaseUtil.getValue(bean, name);

        Date date = null;
        if (o instanceof Date) {
            date = (Date) o;
        } else if (o instanceof String) {
            try {
                date = DateConverter.convertFromString((String) o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (o instanceof Long) {
            date = new Date((Long) o);
        }

        if (date == null) {
            return false;
        }

        Calendar cal = toCalendar(date);

        switch (operator) {
        case EQUALS: {
            return cal.equals(toCalendar(filterValue));
        }
        case NOT_EQUALS:
            return !cal.equals(toCalendar(filterValue));

        case LESS:
            return cal.before(toCalendar(filterValue));

        case GREATER:
            return cal.after(toCalendar(filterValue));

        case LESS_EQUAL: {
            Calendar filter1 = toCalendar(filterValue);
            return cal.equals(filter1) || cal.before(filter1);
        }
        case GREATER_EQUAL: {
            Calendar filter1 = toCalendar(filterValue);
            return cal.equals(filter1) || cal.after(filter1);
        }
        case IN_RANGE: {
            Calendar filter1 = toCalendar(filterValue);
            Calendar filter2 = toCalendar(filterValue2);
            return cal.after(filter1) && cal.before(filter2);
        }
        case TODAY: {
            Calendar today = toCalendar(new Date());
            return cal.equals(today);
        }
        case YESTERDAY: {
            Calendar yesterday = toCalendar(new Date());
            yesterday.add(Calendar.DATE, -1);
            return cal.equals(yesterday);
        }
        case THIS_WEEK: {
            Calendar today = toCalendar(new Date());
            Calendar sunday = toCalendar(new Date());
            sunday.add(Calendar.DATE, Calendar.SUNDAY - today.get(Calendar.DAY_OF_WEEK));
            return ((cal.equals(sunday) || cal.after(sunday)) && (cal.equals(today) || cal.before(today)));
        }
        case THIS_MONTH: {
            Calendar today = toCalendar(new Date());
            Calendar firstOfMonth = toCalendar(new Date());
            firstOfMonth.add(Calendar.DATE, 1 - today.get(Calendar.DAY_OF_MONTH));
            return ((cal.equals(firstOfMonth) || cal.after(firstOfMonth)) && (cal.equals(today) || cal.before(today)));
        }
        case THIS_YEAR: {
            Calendar today = toCalendar(new Date());
            Calendar firstOfYear = toCalendar(new Date());
            firstOfYear.add(Calendar.DATE, 1 - today.get(Calendar.DAY_OF_YEAR));
            return ((cal.equals(firstOfYear) || cal.after(firstOfYear)) && (cal.equals(today) || cal.before(today)));
        }
        case LAST_N_HOURS: {
            Calendar today = Calendar.getInstance();
            today.setTime(new Date());
            
            Calendar lastNHours = Calendar.getInstance();
            lastNHours.setTime(new Date());
            
            lastNHours.add(Calendar.HOUR, -ncount);
            return ((cal.equals(lastNHours) || cal.after(lastNHours)) && (cal.equals(today) || cal.before(today)));
        }
        case LAST_N_DAYS: {
            Calendar today = toCalendar(new Date());
            Calendar lastNDays = toCalendar(new Date());
            lastNDays.add(Calendar.DATE, -ncount);
            return ((cal.equals(lastNDays) || cal.after(lastNDays)) && (cal.equals(today) || cal.before(today)));
        }
        case LAST_N_WEEKS: {
            Calendar today = toCalendar(new Date());
            Calendar lastNWeeks = toCalendar(new Date());
            lastNWeeks.add(Calendar.DATE, -7 * ncount + (Calendar.SUNDAY - today.get(Calendar.DAY_OF_WEEK)));
            return ((cal.equals(lastNWeeks) || cal.after(lastNWeeks)) && (cal.equals(today) || cal.before(today)));
        }
        case LAST_N_MONTHS: {
            Calendar today = toCalendar(new Date());
            Calendar lastNMonths = toCalendar(new Date());
            lastNMonths.add(Calendar.DATE, 1 - today.get(Calendar.DAY_OF_MONTH));
            lastNMonths.add(Calendar.MONTH, -ncount);
            return ((cal.equals(lastNMonths) || cal.after(lastNMonths)) && (cal.equals(today) || cal.before(today)));
        }
        case NEXT_N_HOURS: {
            Calendar today = Calendar.getInstance();
            today.setTime(new Date());
            Calendar nextNHours = Calendar.getInstance();
            nextNHours.setTime(new Date());
            nextNHours.add(Calendar.HOUR, ncount);           
            return ((cal.equals(nextNHours) || cal.before(nextNHours)) && (cal.equals(today) || cal.after(today)));
        }
        case NEXT_N_DAYS: {
            Calendar today = toCalendar(new Date());
            Calendar nextNDays = toCalendar(new Date());
            nextNDays.add(Calendar.DATE, ncount);
            return ((cal.equals(nextNDays) || cal.before(nextNDays)) && (cal.equals(today) || cal.after(today)));
        }
        case NEXT_N_WEEKS: {
            Calendar today = toCalendar(new Date());
            Calendar nextNWeeks = toCalendar(new Date());
            nextNWeeks.add(Calendar.DATE, Calendar.SUNDAY - today.get(Calendar.DAY_OF_WEEK));
            nextNWeeks.add(Calendar.DATE, 7 * ncount);
            return ((cal.equals(nextNWeeks) || cal.before(nextNWeeks)) && (cal.equals(today) || cal.after(today)));
        }
        case NEXT_N_MONTHS: {
            Calendar today = toCalendar(new Date());
            Calendar nextNMonths = toCalendar(new Date());
            nextNMonths.add(Calendar.DATE, 1 - today.get(Calendar.DAY_OF_MONTH));
            nextNMonths.add(Calendar.MONTH, ncount);
            return ((cal.equals(nextNMonths) || cal.before(nextNMonths)) && (cal.equals(today) || cal.after(today)));
        }
        }

        return false;
    }

    @Override
    public String toSQL() {
        StringBuilder buf = new StringBuilder();

        switch (operator) {
        case EQUALS: {
            return "DATE(" + fieldname + ") = '" + format.format(filterValue) + "'";
        }
        case NOT_EQUALS: {
            return "DATE(" + fieldname + ") <> '" + format.format(filterValue) + "'";
        }
        case LESS:
            return "DATE(" + fieldname + ") < '" + format.format(filterValue) + "'";

        case GREATER: {
            return "DATE(" + fieldname + ") > '" + format.format(filterValue) + "'";
        }
        case LESS_EQUAL: {
            return "DATE(" + fieldname + ") <= '" + format.format(filterValue) + "'";
        }
        case GREATER_EQUAL: {
            return "DATE(" + fieldname + ") >= '" + format.format(filterValue) + "'";
        }
        case IN_RANGE: {
            if (filterValue == null) {
                filterValue = new Date();
            }
            if (filterValue2 == null) {
                filterValue2 = filterValue;
            }
            return "DATE(" + fieldname + ") between  '" + format.format(filterValue) + "' AND '"
                    + format.format(filterValue2) + "'";
        }
        case TODAY: {
            return "DATE(" + fieldname + ") = DATE(NOW())";
        }
        case YESTERDAY: {
            return "DATE(" + fieldname + ") =  DATE(NOW() - INTERVAL 1 DAY)";
        }
        case THIS_WEEK: {
            return "DATE(" + fieldname + ") between DATE(NOW() - INTERVAL 1 WEEK) AND DATE(NOW())";
        }
        case THIS_MONTH: {
            return "DATE(" + fieldname + ") between DATE_FORMAT(NOW() ,'%Y-%m-01') AND DATE(NOW())";
        }
        case THIS_YEAR: {
            return "DATE(" + fieldname + ") between DATE_FORMAT(NOW() ,'%Y-01-01') AND DATE(NOW())";
        }
        case LAST_N_HOURS: {
            return "DATE(" + fieldname + ") between (NOW() - INTERVAL " + ncount + " HOUR) AND NOW()";
        }
        case LAST_N_DAYS: {
            return "DATE(" + fieldname + ") between (NOW() - INTERVAL " + ncount + " DAY) AND NOW()";
        }
        case LAST_N_WEEKS: {
            return "DATE(" + fieldname + ") between (NOW() - INTERVAL " + ncount + " WEEK) AND NOW()";
        }
        case LAST_N_MONTHS: {
            return "DATE(" + fieldname + ") between (NOW() - INTERVAL " + ncount + " MONTH) AND NOW()";
        }
        case NEXT_N_HOURS: {
            return "DATE(" + fieldname + ") between NOW() AND (NOW() + INTERVAL " + ncount + " HOUR)";
        }
        case NEXT_N_DAYS: {
            return "DATE(" + fieldname + ") between  NOW() AND (NOW() + INTERVAL " + ncount + " DAY)";
        }
        case NEXT_N_WEEKS: {
            return "DATE(" + fieldname + ") between NOW() AND (NOW() + INTERVAL " + ncount + " WEEK)";
        }
        case NEXT_N_MONTHS: {
            return "DATE(" + fieldname + ") between NOW() AND (NOW() + INTERVAL " + ncount + " MONTH)";
        }
        }
        return buf.toString();
    }

    private final Calendar toCalendar(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
}