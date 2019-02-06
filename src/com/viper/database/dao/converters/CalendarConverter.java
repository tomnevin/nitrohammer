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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.viper.database.annotations.Column;

public final class CalendarConverter {

    private static final String TimePattern = "HH:mm:ss.SSS";
    private static final String DatePattern = "yyyy-MM-dd";
    private static final String TimestampPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final List<String> patterns = new ArrayList<String>();
    static {
        patterns.add(TimestampPattern);
        patterns.add("yyyy-MM-dd HH:mm:ss.S");
        patterns.add("yyyy-MM-dd hh:mm:ss");
        patterns.add("MM-dd-yyyy HH:mm:ss");
        patterns.add("MM/dd/yyyy HH:mm:ss");
        patterns.add("MM-dd-yyyy HH:mm");
        patterns.add("MM/dd/yyyy HH:mm");
        patterns.add(TimePattern);
        patterns.add("HH:mm:ss");
        patterns.add(DatePattern);
    }

    public static final void addPattern(String pattern) {
        if (!patterns.contains(pattern)) {
            patterns.add(pattern);
        }
    }

    public static final void initialize() {

        Converters.register(BigDecimal.class, Calendar.class, CalendarConverter::convertBigDecimalToCalendar);
        Converters.register(BigInteger.class, Calendar.class, CalendarConverter::convertBigIntegerToCalendar);
        Converters.register(Double.class, Calendar.class, CalendarConverter::convertDoubleToCalendar);
        Converters.register(Float.class, Calendar.class, CalendarConverter::convertFloatToCalendar);
        Converters.register(Integer.class, Calendar.class, CalendarConverter::convertIntegerToCalendar);
        Converters.register(Long.class, Calendar.class, CalendarConverter::convertLongToCalendar);
        Converters.register(Short.class, Calendar.class, CalendarConverter::convertShortToCalendar);
        Converters.register(double.class, Calendar.class, CalendarConverter::convertDoubleToCalendar);
        Converters.register(float.class, Calendar.class, CalendarConverter::convertFloatToCalendar);
        Converters.register(int.class, Calendar.class, CalendarConverter::convertIntegerToCalendar);
        Converters.register(long.class, Calendar.class, CalendarConverter::convertLongToCalendar);
        Converters.register(short.class, Calendar.class, CalendarConverter::convertShortToCalendar);

        Converters.register(Calendar.class, BigDecimal.class, CalendarConverter::convertCalendarToBigDecimal);
        Converters.register(Calendar.class, BigInteger.class, CalendarConverter::convertCalendarToBigInteger);
        Converters.register(Calendar.class, Double.class, CalendarConverter::convertCalendarToDouble);
        Converters.register(Calendar.class, Float.class, CalendarConverter::convertCalendarToFloat);
        Converters.register(Calendar.class, Integer.class, CalendarConverter::convertCalendarToInteger);
        Converters.register(Calendar.class, Long.class, CalendarConverter::convertCalendarToLong);
        Converters.register(Calendar.class, Short.class, CalendarConverter::convertCalendarToShort);
        Converters.register(Calendar.class, double.class, CalendarConverter::convertCalendarToDouble);
        Converters.register(Calendar.class, float.class, CalendarConverter::convertCalendarToFloat);
        Converters.register(Calendar.class, int.class, CalendarConverter::convertCalendarToInteger);
        Converters.register(Calendar.class, long.class, CalendarConverter::convertCalendarToLong);
        Converters.register(Calendar.class, short.class, CalendarConverter::convertCalendarToShort);

        Converters.register(double.class, java.sql.Date.class, CalendarConverter::convertDoubleToSQLDate);
        Converters.register(Double.class, java.sql.Date.class, CalendarConverter::convertDoubleToSQLDate);
        Converters.register(double.class, java.sql.Time.class, CalendarConverter::convertDoubleToSQLTime);
        Converters.register(Double.class, java.sql.Time.class, CalendarConverter::convertDoubleToSQLTime);
        Converters.register(double.class, java.sql.Timestamp.class, CalendarConverter::convertDoubleToSQLTimestamp);
        Converters.register(Double.class, java.sql.Timestamp.class, CalendarConverter::convertDoubleToSQLTimestamp);
        Converters.register(double.class, java.util.Date.class, CalendarConverter::convertDoubleToDate);
        Converters.register(Double.class, java.util.Date.class, CalendarConverter::convertDoubleToDate);

        Converters.register(float.class, java.sql.Date.class, CalendarConverter::convertFloatToSQLDate);
        Converters.register(Float.class, java.sql.Date.class, CalendarConverter::convertFloatToSQLDate);
        Converters.register(float.class, java.sql.Time.class, CalendarConverter::convertFloatToSQLTime);
        Converters.register(Float.class, java.sql.Time.class, CalendarConverter::convertFloatToSQLTime);
        Converters.register(float.class, java.sql.Timestamp.class, CalendarConverter::convertFloatToSQLTimestamp);
        Converters.register(Float.class, java.sql.Timestamp.class, CalendarConverter::convertFloatToSQLTimestamp);
        Converters.register(float.class, java.util.Date.class, CalendarConverter::convertFloatToDate);
        Converters.register(Float.class, java.util.Date.class, CalendarConverter::convertFloatToDate);

        Converters.register(int.class, java.sql.Date.class, CalendarConverter::convertIntegerToSQLDate);
        Converters.register(Integer.class, java.sql.Date.class, CalendarConverter::convertIntegerToSQLDate);
        Converters.register(int.class, java.sql.Time.class, CalendarConverter::convertIntegerToSQLTime);
        Converters.register(Integer.class, java.sql.Time.class, CalendarConverter::convertIntegerToSQLTime);
        Converters.register(int.class, java.sql.Timestamp.class, CalendarConverter::convertIntegerToSQLTimestamp);
        Converters.register(Integer.class, java.sql.Timestamp.class, CalendarConverter::convertIntegerToSQLTimestamp);
        Converters.register(int.class, java.util.Date.class, CalendarConverter::convertIntegerToDate);
        Converters.register(Integer.class, java.util.Date.class, CalendarConverter::convertIntegerToDate);

        Converters.register(long.class, java.sql.Date.class, CalendarConverter::convertLongToSQLDate);
        Converters.register(Long.class, java.sql.Date.class, CalendarConverter::convertLongToSQLDate);
        Converters.register(long.class, java.sql.Time.class, CalendarConverter::convertLongToSQLTime);
        Converters.register(Long.class, java.sql.Time.class, CalendarConverter::convertLongToSQLTime);
        Converters.register(long.class, java.sql.Timestamp.class, CalendarConverter::convertLongToSQLTimestamp);
        Converters.register(Long.class, java.sql.Timestamp.class, CalendarConverter::convertLongToSQLTimestamp);
        Converters.register(long.class, java.util.Date.class, CalendarConverter::convertLongToDate);
        Converters.register(Long.class, java.util.Date.class, CalendarConverter::convertLongToDate);

        Converters.register(short.class, java.sql.Date.class, CalendarConverter::convertShortToSQLDate);
        Converters.register(Short.class, java.sql.Date.class, CalendarConverter::convertShortToSQLDate);
        Converters.register(short.class, java.sql.Time.class, CalendarConverter::convertShortToSQLTime);
        Converters.register(Short.class, java.sql.Time.class, CalendarConverter::convertShortToSQLTime);
        Converters.register(short.class, java.sql.Timestamp.class, CalendarConverter::convertShortToSQLTimestamp);
        Converters.register(Short.class, java.sql.Timestamp.class, CalendarConverter::convertShortToSQLTimestamp);
        Converters.register(short.class, java.util.Date.class, CalendarConverter::convertShortToDate);
        Converters.register(Short.class, java.util.Date.class, CalendarConverter::convertShortToDate);

        Converters.register(BigDecimal.class, java.sql.Date.class, CalendarConverter::convertBigDecimalToSQLDate);
        Converters.register(BigDecimal.class, java.sql.Time.class, CalendarConverter::convertBigDecimalToSQLTime);
        Converters.register(BigDecimal.class, java.sql.Timestamp.class, CalendarConverter::convertBigDecimalToSQLTimestamp);
        Converters.register(BigDecimal.class, java.util.Date.class, CalendarConverter::convertBigDecimalToDate);

        Converters.register(BigInteger.class, java.sql.Date.class, CalendarConverter::convertBigIntegerToSQLDate);
        Converters.register(BigInteger.class, java.sql.Time.class, CalendarConverter::convertBigIntegerToSQLTime);
        Converters.register(BigInteger.class, java.sql.Timestamp.class, CalendarConverter::convertBigIntegerToSQLTimestamp);
        Converters.register(BigInteger.class, java.util.Date.class, CalendarConverter::convertBigIntegerToDate);

        Converters.register(Date.class, java.sql.Date.class, CalendarConverter::convertDateToSQLDate);
        Converters.register(Date.class, java.sql.Time.class, CalendarConverter::convertDateToSQLTime);
        Converters.register(Date.class, java.sql.Timestamp.class, CalendarConverter::convertDateToSQLTimestamp);
        Converters.register(Date.class, java.util.Date.class, CalendarConverter::convertDateToDate);

        Converters.register(String.class, java.sql.Date.class, CalendarConverter::convertStringToDate);
        Converters.register(String.class, java.sql.Time.class, CalendarConverter::convertStringToDate);
        Converters.register(String.class, java.sql.Timestamp.class, CalendarConverter::convertStringToDate);
        Converters.register(String.class, java.util.Date.class, CalendarConverter::convertStringToDate);

        Converters.register(Date.class, BigDecimal.class, CalendarConverter::convertDateToBigDecimal);
        Converters.register(Date.class, BigInteger.class, CalendarConverter::convertDateToBigInteger);
        Converters.register(Date.class, Integer.class, CalendarConverter::convertDateToInteger);
        Converters.register(Date.class, Float.class, CalendarConverter::convertDateToFloat);
        Converters.register(Date.class, Double.class, CalendarConverter::convertDateToDouble);
        Converters.register(Date.class, Long.class, CalendarConverter::convertDateToLong);
        Converters.register(Date.class, Short.class, CalendarConverter::convertDateToShort);

        Converters.register(Date.class, int.class, CalendarConverter::convertDateToInteger);
        Converters.register(Date.class, float.class, CalendarConverter::convertDateToFloat);
        Converters.register(Date.class, double.class, CalendarConverter::convertDateToDouble);
        Converters.register(Date.class, long.class, CalendarConverter::convertDateToLong);
        Converters.register(Date.class, short.class, CalendarConverter::convertDateToShort);

        Converters.register(java.sql.Date.class, BigDecimal.class, CalendarConverter::convertDateToBigDecimal);
        Converters.register(java.sql.Date.class, BigInteger.class, CalendarConverter::convertDateToBigInteger);
        Converters.register(java.sql.Date.class, Integer.class, CalendarConverter::convertDateToInteger);
        Converters.register(java.sql.Date.class, Float.class, CalendarConverter::convertDateToFloat);
        Converters.register(java.sql.Date.class, Double.class, CalendarConverter::convertDateToDouble);
        Converters.register(java.sql.Date.class, Long.class, CalendarConverter::convertDateToLong);
        Converters.register(java.sql.Date.class, Short.class, CalendarConverter::convertDateToShort);

        Converters.register(java.sql.Date.class, int.class, CalendarConverter::convertDateToInteger);
        Converters.register(java.sql.Date.class, float.class, CalendarConverter::convertDateToFloat);
        Converters.register(java.sql.Date.class, double.class, CalendarConverter::convertDateToDouble);
        Converters.register(java.sql.Date.class, long.class, CalendarConverter::convertDateToLong);
        Converters.register(java.sql.Date.class, short.class, CalendarConverter::convertDateToShort);

        Converters.register(java.sql.Time.class, BigDecimal.class, CalendarConverter::convertDateToBigDecimal);
        Converters.register(java.sql.Time.class, BigInteger.class, CalendarConverter::convertDateToBigInteger);
        Converters.register(java.sql.Time.class, Integer.class, CalendarConverter::convertDateToInteger);
        Converters.register(java.sql.Time.class, Float.class, CalendarConverter::convertDateToFloat);
        Converters.register(java.sql.Time.class, Double.class, CalendarConverter::convertDateToDouble);
        Converters.register(java.sql.Time.class, Long.class, CalendarConverter::convertDateToLong);
        Converters.register(java.sql.Time.class, Short.class, CalendarConverter::convertDateToShort);

        Converters.register(java.sql.Time.class, int.class, CalendarConverter::convertDateToInteger);
        Converters.register(java.sql.Time.class, float.class, CalendarConverter::convertDateToFloat);
        Converters.register(java.sql.Time.class, double.class, CalendarConverter::convertDateToDouble);
        Converters.register(java.sql.Time.class, long.class, CalendarConverter::convertDateToLong);
        Converters.register(java.sql.Time.class, short.class, CalendarConverter::convertDateToShort);

        Converters.register(java.sql.Timestamp.class, BigDecimal.class, CalendarConverter::convertDateToBigDecimal);
        Converters.register(java.sql.Timestamp.class, BigInteger.class, CalendarConverter::convertDateToBigInteger);
        Converters.register(java.sql.Timestamp.class, Integer.class, CalendarConverter::convertDateToInteger);
        Converters.register(java.sql.Timestamp.class, Float.class, CalendarConverter::convertDateToFloat);
        Converters.register(java.sql.Timestamp.class, Double.class, CalendarConverter::convertDateToDouble);
        Converters.register(java.sql.Timestamp.class, Long.class, CalendarConverter::convertDateToLong);
        Converters.register(java.sql.Timestamp.class, Short.class, CalendarConverter::convertDateToShort);

        Converters.register(java.sql.Timestamp.class, int.class, CalendarConverter::convertDateToInteger);
        Converters.register(java.sql.Timestamp.class, float.class, CalendarConverter::convertDateToFloat);
        Converters.register(java.sql.Timestamp.class, double.class, CalendarConverter::convertDateToDouble);
        Converters.register(java.sql.Timestamp.class, long.class, CalendarConverter::convertDateToLong);
        Converters.register(java.sql.Timestamp.class, short.class, CalendarConverter::convertDateToShort);

        Converters.register(java.sql.Timestamp.class, String.class, CalendarConverter::convertSQLTimestampToString);
        Converters.register(java.sql.Time.class, String.class, CalendarConverter::convertSQLTimeToString);
        Converters.register(java.sql.Date.class, String.class, CalendarConverter::convertSQLDateToString);
        Converters.register(Date.class, String.class, CalendarConverter::convertDateToString);

    }

    public static final <T, S> T convertBigDecimalToCalendar(Class<T> toType, S fromValue) throws Exception {
        long millis = (long) (((BigDecimal) fromValue).doubleValue() * 1000.0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return toType.cast(cal);
    }

    public static final <T, S> T convertBigIntegerToCalendar(Class<T> toType, S fromValue) throws Exception {
        long millis = (long) (((BigInteger) fromValue).longValue());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return toType.cast(cal);
    }

    public static final <T, S> T convertDoubleToCalendar(Class<T> toType, S fromValue) throws Exception {
        long millis = (long) ((Double) fromValue * 1000.0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return toType.cast(cal);
    }

    public static final <T, S> T convertFloatToCalendar(Class<T> toType, S fromValue) throws Exception {
        long millis = (long) ((Float) fromValue * 1000.0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return toType.cast(cal);
    }

    public static final <T, S> T convertIntegerToCalendar(Class<T> toType, S fromValue) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis((Integer) fromValue);
        return toType.cast(cal);
    }

    public static final <T, S> T convertLongToCalendar(Class<T> toType, S fromValue) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis((Long) fromValue);
        return toType.cast(cal);
    }

    public static final <T, S> T convertShortToCalendar(Class<T> toType, S fromValue) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis((Short) fromValue);
        return toType.cast(cal);
    }

    public static final <T, S> T convertCalendarToBigDecimal(Class<T> toType, S fromValue) throws Exception {
        double val = (double) (((Calendar) fromValue).getTimeInMillis()) / 1000.0;
        return toType.cast(BigDecimal.valueOf(val));
    }

    public static final <T, S> T convertCalendarToBigInteger(Class<T> toType, S fromValue) throws Exception {
        long val = ((Calendar) fromValue).getTimeInMillis();
        return toType.cast(BigInteger.valueOf(val));
    }

    public static final <T, S> T convertCalendarToCalendar(Class<T> toType, S fromValue) throws Exception {
        return toType.cast((Calendar) fromValue);
    }

    public static final <T, S> T convertCalendarToDouble(Class<T> toType, S fromValue) throws Exception {
        double val = (double) (((Calendar) fromValue).getTimeInMillis()) / 1000.0;
        return toType.cast(Double.valueOf(val));
    }

    public static final <T, S> T convertCalendarToFloat(Class<T> toType, S fromValue) throws Exception {
        float val = (float) (((Calendar) fromValue).getTimeInMillis()) / 1000.0F;
        return toType.cast(Float.valueOf(val));
    }

    public static final <T, S> T convertCalendarToInteger(Class<T> toType, S fromValue) throws Exception {
        int val = (int) (((Calendar) fromValue).getTimeInMillis() / 1000);
        return toType.cast(Integer.valueOf(val));
    }

    public static final <T, S> T convertCalendarToLong(Class<T> toType, S fromValue) throws Exception {
        long val = ((Calendar) fromValue).getTimeInMillis();
        return toType.cast(Long.valueOf(val));
    }

    public static final <T, S> T convertCalendarToShort(Class<T> toType, S fromValue) throws Exception {
        short val = (short) (((Calendar) fromValue).getTimeInMillis() / 1000);
        return toType.cast(Short.valueOf(val));
    }

    public static final <T, S> T convertLongToSQLDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Date((Long) fromValue));
    }

    public static final <T, S> T convertLongToSQLTime(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Time((Long) fromValue));
    }

    public static final <T, S> T convertLongToSQLTimestamp(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Timestamp((Long) fromValue));
    }

    public static final <T, S> T convertLongToDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.util.Date((Long) fromValue));
    }

    public static final <T, S> T convertIntegerToSQLDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Date((Integer) fromValue));
    }

    public static final <T, S> T convertIntegerToSQLTime(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Time((Integer) fromValue));
    }

    public static final <T, S> T convertIntegerToSQLTimestamp(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Timestamp((Integer) fromValue));
    }

    public static final <T, S> T convertIntegerToDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.util.Date((Integer) fromValue));
    }

    public static final <T, S> T convertDoubleToSQLDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Date((long) ((Double) fromValue * 1000.0)));
    }

    public static final <T, S> T convertDoubleToSQLTime(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Time((long) ((Double) fromValue * 1000.0)));
    }

    public static final <T, S> T convertDoubleToSQLTimestamp(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Timestamp((long) ((Double) fromValue * 1000.0)));
    }

    public static final <T, S> T convertDoubleToDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.util.Date((long) ((Double) fromValue * 1000.0)));
    }

    public static final <T, S> T convertFloatToSQLDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Date((long) ((Float) fromValue * 1000.0)));
    }

    public static final <T, S> T convertFloatToSQLTime(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Time((long) ((Float) fromValue * 1000.0)));
    }

    public static final <T, S> T convertFloatToSQLTimestamp(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Timestamp((long) ((Float) fromValue * 1000.0)));
    }

    public static final <T, S> T convertFloatToDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.util.Date((long) ((Float) fromValue * 1000.0)));
    }

    public static final <T, S> T convertShortToSQLDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Date((long) ((Short) fromValue * 1000.0)));
    }

    public static final <T, S> T convertShortToSQLTime(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Time((long) ((Short) fromValue * 1000.0)));
    }

    public static final <T, S> T convertShortToSQLTimestamp(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Timestamp((long) ((Short) fromValue * 1000.0)));
    }

    public static final <T, S> T convertShortToDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.util.Date((long) ((Short) fromValue * 1000.0)));
    }

    public static final <T, S> T convertBigDecimalToSQLDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Date((long) (((BigDecimal) fromValue).doubleValue() * 1000.0)));
    }

    public static final <T, S> T convertBigDecimalToSQLTime(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Time((long) (((BigDecimal) fromValue).doubleValue() * 1000.0)));
    }

    public static final <T, S> T convertBigDecimalToSQLTimestamp(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Timestamp((long) (((BigDecimal) fromValue).doubleValue() * 1000.0)));
    }

    public static final <T, S> T convertBigDecimalToDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.util.Date((long) (((BigDecimal) fromValue).doubleValue() * 1000.0)));
    }

    public static final <T, S> T convertBigIntegerToSQLDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Date((long) (((BigInteger) fromValue).longValue())));
    }

    public static final <T, S> T convertBigIntegerToSQLTime(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Time((long) (((BigInteger) fromValue).longValue())));
    }

    public static final <T, S> T convertBigIntegerToSQLTimestamp(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.sql.Timestamp((long) (((BigInteger) fromValue).longValue())));
    }

    public static final <T, S> T convertBigIntegerToDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(new java.util.Date((long) (((BigInteger) fromValue).longValue())));
    }

    public static final <T, S> T convertDateToSQLDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(fromValue);
    }

    public static final <T, S> T convertDateToSQLTime(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(fromValue);
    }

    public static final <T, S> T convertDateToSQLTimestamp(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(fromValue);
    }

    public static final <T, S> T convertDateToDate(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(fromValue);
    }

    public static final <T, S> T convertStringToDate(Class<T> toType, S fromValue) throws Exception {

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                Date date = sdf.parse((String) fromValue);
                if (date != null) {
                    return toType.cast(date);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                ; // Intentional
            }
        }
        throw new Exception("Unhandled to convertStringToDate " + fromValue);
    }

    public static final <T, S> T convertDateToBigDecimal(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(BigDecimal.valueOf(((Date) fromValue).getTime() / 1000.0));
    }

    public static final <T, S> T convertDateToBigInteger(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(BigInteger.valueOf(((Date) fromValue).getTime()));
    }

    public static final <T, S> T convertDateToDouble(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(Double.valueOf(((Date) fromValue).getTime() / 1000.0));
    }

    public static final <T, S> T convertDateToFloat(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(Float.valueOf(((Date) fromValue).getTime() / 1000.0F));
    }

    public static final <T, S> T convertDateToInteger(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(Integer.valueOf((int) ((Date) fromValue).getTime() / 1000));
    }

    public static final <T, S> T convertDateToLong(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(Long.valueOf((long) ((Date) fromValue).getTime()));
    }

    public static final <T, S> T convertDateToShort(Class<T> toType, S fromValue) throws Exception {
        return toType.cast(Short.valueOf((short) (((Date) fromValue).getTime() / 1000)));
    }

    public static final <T, S> T convertSQLTimestampToString(Class<T> toType, S fromValue) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(TimestampPattern);
        return toType.cast(sdf.format(fromValue));
    }

    public static final <T, S> T convertSQLTimeToString(Class<T> toType, S fromValue) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(TimePattern);
        return toType.cast(sdf.format(fromValue));
    }

    public static final <T, S> T convertSQLDateToString(Class<T> toType, S fromValue) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(DatePattern);
        return toType.cast(sdf.format(fromValue));
    }

    public static final <T, S> T convertDateToString(Class<T> toType, S fromValue) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(TimestampPattern);
        return toType.cast(sdf.format(fromValue));
    }

    private static final <T, S> String convertToString(Class<T> toType, S fromValue, String[] qualifiers) throws Exception {
        return new DecimalFormat(qualifiers[0]).format(fromValue);
    }

    private static final <T> String convertToString(Column column, T fromValue, String[] qualifiers) throws Exception {
        if (fromValue instanceof Calendar) {
            new DecimalFormat(qualifiers[0]).format(fromValue);
        }

        throw new Exception("Unhandled conversion from " + fromValue + " to String.");
    }
}
