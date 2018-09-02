/*
 * -----------------------------------------------------------------------------
 *                      VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * MIT License
 * 
 * Copyright (c) #{classname}.java #{util.YYYY()} Viper Software Services
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

package com.viper.database.dao.converters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.viper.database.annotations.Column;

public final class CalendarConverter implements ConverterInterface {

    public Class<Calendar> getDefaultType() {
        return Calendar.class;
    }

    @Override
    public Object convertToType(Class toType, Object fromValue) throws Exception {

        if (toType.equals(Calendar.class)) {
            return convertToCalendar(fromValue);

        } else if (fromValue instanceof Calendar) {
            return convertFromCalendar(toType, (Calendar) fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    @Override
    public <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Calendar) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }

    @Override
    public Object convertToArray(Class toType, Object fromValue) throws Exception {
        if (fromValue == null) {
            return null;
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to " + toType + ".");
    }

    public Calendar convertToCalendar(Object fromValue) throws Exception {

        if (fromValue instanceof Byte) {
            return null; // TODO

        } else if (fromValue instanceof BigDecimal) {
            long millis = (long) (((BigDecimal) fromValue).doubleValue() * 1000.0);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(millis);
            return cal;

        } else if (fromValue instanceof Blob) {
            return null; // TODO

        } else if (fromValue instanceof Boolean) {
            return null; // TODO

        } else if (fromValue instanceof BigInteger) {
            long millis = (long) (((BigInteger) fromValue).longValue());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(millis);
            return cal;

        } else if (fromValue instanceof Clob) {
            return null;

        } else if (fromValue instanceof Double) {
            long millis = (long) ((Double) fromValue * 1000.0);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(millis);
            return cal;

        } else if (fromValue instanceof Integer) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis((Integer) fromValue);
            return cal;

        } else if (fromValue instanceof Long) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis((Long) fromValue);
            return cal;

        } else if (fromValue instanceof Short) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis((Short) fromValue);
            return cal;

        } else if (fromValue instanceof String) {
            return null; // TODO

        } else {
            throw new Exception("Unhandled conversion from " + fromValue + " to Calendar.");
        }
    }

    public <T> T convertFromCalendar(Class<T> targetType, Calendar fromValue) throws Exception {

        if (targetType.equals(Byte.class)) {
            return null; // TODO

        } else if (targetType.equals(BigDecimal.class)) {
            double val = (double) (fromValue.getTimeInMillis()) / 1000.0;
            return targetType.cast(BigDecimal.valueOf(val));

        } else if (targetType.equals(Blob.class)) {
            return null; // TODO

        } else if (targetType.equals(Boolean.class)) {
            return null; // TODO

        } else if (targetType.equals(BigInteger.class)) {
            long val = fromValue.getTimeInMillis();
            return targetType.cast(BigInteger.valueOf(val));

        } else if (targetType.equals(Calendar.class)) {
            return targetType.cast(fromValue);

        } else if (targetType.equals(Clob.class)) {
            return null; // TODO

        } else if (targetType.equals(Double.class)) {
            double val = (double) (fromValue.getTimeInMillis()) / 1000.0;
            return targetType.cast(Double.valueOf(val));

        } else if (targetType.equals(Float.class)) {
            float val = (float) (fromValue.getTimeInMillis()) / 1000.0F;
            return targetType.cast(Float.valueOf(val));

        } else if (targetType.equals(Integer.class)) {
            int val = (int) (fromValue.getTimeInMillis() / 1000);
            return targetType.cast(Integer.valueOf(val));

        } else if (targetType.equals(Long.class)) {
            long val = fromValue.getTimeInMillis();
            return targetType.cast(Long.valueOf(val));

        } else if (targetType.equals(Short.class)) {
            short val = (short) (fromValue.getTimeInMillis() / 1000);
            return targetType.cast(Short.valueOf(val));

        } else if (targetType.equals(String.class)) {
            return null;

        } else {
            throw new Exception("Unhandled conversion from Calendar to " + fromValue + ".");
        }
    }

    public Calendar parseString(String str) {
        Calendar cal = Calendar.getInstance();
        try {
            Date date = new SimpleDateFormat("MM/dd/yyyy").parse(str);
            cal.setTime(date);
        } catch (Exception e) {
        }
        return cal;
    }
}
