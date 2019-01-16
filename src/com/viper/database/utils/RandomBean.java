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

package com.viper.database.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.viper.database.annotations.Column;
import com.viper.database.annotations.Table;
import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseUtil;

public class RandomBean {

    private final static Logger log = Logger.getLogger(RandomBean.class.getName());

    private static final String DELIMITERS = "\\s*[<>,]\\s*";
    private static final long ONE_YEAR = 365 * 24 * 60 * 60 * 1000;
    private static final String TEMPLATE = "abcdefghijklmnopqrstuvwxyz";
    private static final Random random = new Random();
    private static final Map<String, List<CSVRecord>> cache = new HashMap<String, List<CSVRecord>>();
    private static final Map<String, List> tableCache = new HashMap<String, List>();

    private final static String DF = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * 
     * @param clazz
     * @param iteration
     * @param nitems
     * @return the list of random generated beans, list size should be nitems, else
     *         exception.
     * @throws Exception
     *             unable to build list of random beans, generally unable to create
     *             bean objects.
     */
    public final static <T> List<T> getRandomBeans(Class<T> clazz, int iteration, int nitems) throws Exception {
        List<T> items = new ArrayList<T>();
        for (int i = 0; i < nitems; i++) {
            items.add(getRandomBean(clazz, iteration + i));
        }
        return items;
    }

    /**
     * 
     * @param clazz
     * @param index
     * @return the random generated bean, never null value.
     * @throws Exception
     *             if unable to allocate the random set bean.
     */
    public final static <T> T getRandomBean(Class<T> clazz, int index) throws Exception {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            System.err.println("RandomBean.getRandomBean: Table annontation not found in class: " + clazz);
            return clazz.newInstance();
        }

        String generator = table.beanGenerator();
        T bean = clazz.newInstance();

        BeanInfo info = Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            Method readMethod = propertyDescriptor.getReadMethod();
            Method writeMethod = propertyDescriptor.getWriteMethod();
            Class propertyType = propertyDescriptor.getPropertyType();
            Type genericType = propertyType.getGenericSuperclass();

            Annotation annotation = readMethod.getAnnotation(Column.class);
            if (annotation != null) {
                Column column = (Column) annotation;
                if (!isAssignedField(column)) {
                    try {
                        writeMethod.invoke(bean, randomValue(bean, table, column, propertyType, genericType, index));
                    } catch (Throwable t) {
                        log.throwing("Exception while setting field for: " + propertyDescriptor.getName(), "", t);
                    }
                }
            } else {
                try {
                    writeMethod.invoke(bean, randomValue(bean, table, null, propertyType, genericType, index));
                } catch (Throwable t) {
                    log.throwing("Exception while setting field for: " + propertyDescriptor.getName(), "", t);
                }
            }
        }

        try {
            executeGenerator(bean, generator);
        } catch (Throwable t) {
            log.throwing("Exception while generating data for: " + clazz.getName() + ", " + generator, "", t);
        }

        return bean;
    }

    /**
     * 
     * @param column
     * @return
     */
    private final static boolean isAssignedField(Column column) {
        if (!column.primaryKey()) {
            return false;
        }
        if (column.idMethod().equalsIgnoreCase("autoincrement")) {
            return true;
        }
        return false;
    }

    /**
     * @param database
     * @param table
     * @param beans
     */
    public final static void setTableData(String database, String table, List beans) {
        String key = makeKey(database, table);

        tableCache.put(key, beans);
    }

    /**
     * @param bean
     * @param table
     * @param column
     * @param propertyType
     * @param genericType
     * @param index
     * @return
     */
    public final static <T> Object randomValue(T bean, Table table, Column column, Class propertyType, Type genericType,
            int index) {

        Class genericClass = (genericType == null) ? null : genericType.getClass();

        if (propertyType == null) {
            return null;
        }

        if (List.class.isAssignableFrom(propertyType)) {
            List<Object> items = (List<Object>) DatabaseUtil.getValue(bean, column.field());
            if (items == null) {
                System.err
                        .println("ERROR: list field " + column.field() + " not found in " + bean.getClass().getName());
                items = new ArrayList();
            }
            int size = randomInt(1, 20); // TODO getsize
            for (int i = 0; i < size; i++) {
                items.add(randomValue(bean, table, column, genericClass, null, index));
            }
            return items;
        }

        if (Map.class.isAssignableFrom(propertyType)) {
            Map items = (Map) DatabaseUtil.getValue(bean, column.field());
            if (items == null) {
                System.err.println("ERROR: map field " + column.field() + " not found in " + bean.getClass().getName());
                items = new HashMap();
            }
            int size = randomInt(1, 20);
            for (int i = 0; i < size; i++) {
                int length = randomInt(5, 100);
                items.put(randomString(length), (Map) randomValue(bean, table, column, genericClass, null, index));
            }
            return items;
        }

        String logicalType = null;
        if (useLogicalType(column)) {

            String arguments[] = getLogicalArguments(column.logicalType());
            if (arguments == null || arguments.length == 0) {
                log.severe("Unhandled randomValue type(" + column.logicalType() + ")");
                return null;
            }

            logicalType = arguments[0];

            if ("regex".equalsIgnoreCase(logicalType)) {
                String regex = arguments[1];
                // return new Xeger(regex).generate();
                return null;
            }

            if ("ip".equalsIgnoreCase(logicalType)) {
                return randomIP(arguments[1]);
            }

            if ("mac".equalsIgnoreCase(logicalType)) {
                return randomMac(arguments[1]);
            }

            if ("name".equalsIgnoreCase(logicalType)) {
                if (column.unique() || (arguments.length >= 3 && "sequential".equalsIgnoreCase(arguments[2]))) {
                    return sequentialName(arguments, index);
                }
                return randomName(arguments);
            }

            if ("companion".equalsIgnoreCase(logicalType)) {
                return companion(bean, arguments);
            }

            if ("default".equalsIgnoreCase(logicalType)) {
                return arguments[1];
            }

            if ("random".equalsIgnoreCase(logicalType)) {
                return generateRandomValue(arguments[1]);
            }

            if ("jexl".equalsIgnoreCase(logicalType)) {
                try {
                    Object result = internalJexl(bean, null, arguments[1]);
                    if (propertyType.isInstance(String.class)) {
                        return ((String) result).trim();
                    }
                    return result;
                } catch (Exception ex) {
                    log.throwing("RandomBean", "Evaluating expression: " + arguments[1], ex);
                }
            }

            // Deprecated // use epoch
            if ("timestamp".equalsIgnoreCase(logicalType)) {
                return randomLong(System.currentTimeMillis() - ONE_YEAR, System.currentTimeMillis() + ONE_YEAR);
            }

            if ("date".equalsIgnoreCase(logicalType)) {
                return randomDate(System.currentTimeMillis(), arguments[1], arguments[2]);
            }

            if ("datetime".equalsIgnoreCase(logicalType)) {
                return randomDateTime(System.currentTimeMillis(), arguments);
            }

            if ("epoch".equalsIgnoreCase(logicalType)) {
                return randomLong(System.currentTimeMillis() - ONE_YEAR, System.currentTimeMillis() + ONE_YEAR);
            }

            if ("percent".equalsIgnoreCase(logicalType)) {
                return randomDouble(0.0, 100.0);
            }

            if ("zipcode".equalsIgnoreCase(logicalType)) {
                return randomString("99999");
            }

            if ("email".equalsIgnoreCase(logicalType)) {
                if (column.unique() || column.primaryKey()) {
                    return sequentialEmail(arguments, index);
                }
                return randomEmail(arguments);
            }

            if ("custom".equalsIgnoreCase(logicalType)) {
                return randomCustom(bean, arguments);
            }

            if ("phone".equalsIgnoreCase(logicalType)) {
                return randomPhone(arguments);
            }

            if ("street".equalsIgnoreCase(logicalType)) {
                return randomEmail(arguments); // TODO Fix
            }

            if ("latlon".equalsIgnoreCase(logicalType)) {
                return randomLatLon(arguments); // TODO Fix
            }

            if ("ports".equalsIgnoreCase(logicalType)) {
                return randomPorts(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]),
                        Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4]));
            }

            if ("list".equalsIgnoreCase(logicalType)) {
                if (column.unique() || column.primaryKey()) {
                    return sequentialList(arguments, index);
                }
                return randomList(arguments);
            }

            if ("seqlist".equalsIgnoreCase(logicalType)) {
                return sequentialList(arguments, index);
            }

            if ("enum".equalsIgnoreCase(logicalType)) {
                if (column.unique() || column.primaryKey()) {
                    return sequentialEnum(propertyType, arguments, index);
                }
                return randomEnum(propertyType, arguments);
            }

            if ("table".equalsIgnoreCase(logicalType)) {
                return randomTable(bean, table, arguments);
            }

            if ("ref".equalsIgnoreCase(logicalType)) {
                return randomRef(bean, table, arguments);
            }

            if ("int".equalsIgnoreCase(logicalType)) {
                // if (arguments.length >= 3 &&
                // "sequential".equalsIgnoreCase(arguments[1])) {
                // return sequentialInt(Integer.parseInt(arguments[2]),
                // Integer.parseInt(arguments[3]));
                // }
                return randomInt(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
            }
            if ("short".equalsIgnoreCase(logicalType)) {
                return randomShort(Short.parseShort(arguments[1]), Short.parseShort(arguments[2]));
            }
            if ("long".equalsIgnoreCase(logicalType)) {
                return randomLong(Long.parseLong(arguments[1]), Long.parseLong(arguments[2]));
            }
            if ("double".equalsIgnoreCase(logicalType)) {
                return randomDouble(Double.parseDouble(arguments[1]), Double.parseDouble(arguments[2]));
            }
            if ("float".equalsIgnoreCase(logicalType)) {
                return randomFloat(Float.parseFloat(arguments[1]), Float.parseFloat(arguments[2]));
            }
            if ("bytes".equalsIgnoreCase(logicalType)) {
                return randomBytes(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
            }
            if ("ints".equalsIgnoreCase(logicalType)) {
                return randomInts(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]),
                        Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4]));
            }

            if (bean.getClass().isEnum()) {
                return randomEnum(bean.getClass(), arguments);
            }

            if (column.options() != null && column.options().length() > 0) {
                String options[] = new String[] { "options", column.options() };
                if (column.unique()) {
                    return sequentialList(options, index);
                }
                return randomList(options);
            }
        }

        // Use database data type
        if ("timestamp".equalsIgnoreCase(column.dataType())) {
            if (long.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType)) {
                return randomLong(System.currentTimeMillis() - ONE_YEAR, System.currentTimeMillis() + ONE_YEAR);
            }
        }
        if ("tinyint".equalsIgnoreCase(column.dataType())) {
            if (int.class.isAssignableFrom(propertyType) || Integer.class.isAssignableFrom(propertyType)) {
                int minval = parseInt(column.minimumValue(), 0);
                int maxval = parseInt(column.maximumValue(), 7);
                return randomInt(minval, maxval);
            }
        }
        if ("enum".equalsIgnoreCase(column.dataType())) {
            return randomEnum(propertyType, column.enumValue());
        }

        // Use the data type

        if (Byte.class.isAssignableFrom(propertyType) || byte.class.isAssignableFrom(propertyType)) {
            return randomByte(-127, 127);

        } else if (boolean.class.isAssignableFrom(propertyType) || Boolean.class.isAssignableFrom(propertyType)) {
            return randomBoolean(50.0);

        } else if (char.class.isAssignableFrom(propertyType) || Character.class.isAssignableFrom(propertyType)) {
            return randomChar((char) 0, (char) 255);

        } else if (int.class.isAssignableFrom(propertyType) || Integer.class.isAssignableFrom(propertyType)) {
            int minval = parseInt(column.minimumValue(), 0);
            int maxval = parseInt(column.maximumValue(), 512);
            return randomInt(minval, maxval);

        } else if (long.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType)) {
            long minval = parseLong(column.minimumValue(), 0);
            long maxval = parseLong(column.maximumValue(), 512);
            return randomLong(minval, maxval);

        } else if (short.class.isAssignableFrom(propertyType) || Short.class.isAssignableFrom(propertyType)) {
            return randomShort(0, 1024000);

        } else if (float.class.isAssignableFrom(propertyType) || Float.class.isAssignableFrom(propertyType)) {
            double minval = parseDouble(column.minimumValue(), -100.0);
            double maxval = parseDouble(column.maximumValue(), 100.0);
            return (float) randomDouble(minval, maxval);

        } else if (double.class.isAssignableFrom(propertyType) || Double.class.isAssignableFrom(propertyType)) {
            double minval = parseDouble(column.minimumValue(), -100.0);
            double maxval = parseDouble(column.maximumValue(), 100.0);
            return randomDouble(minval, maxval);

        } else if (java.sql.Time.class.isAssignableFrom(propertyType)) {
            return new java.sql.Time(randomLong(System.currentTimeMillis(), -ONE_YEAR, ONE_YEAR));

        } else if (java.sql.Date.class.isAssignableFrom(propertyType)) {
            return new java.sql.Date(randomLong(System.currentTimeMillis(), -ONE_YEAR, ONE_YEAR));

        } else if (java.sql.Timestamp.class.isAssignableFrom(propertyType)) {
            return randomTimestamp(System.currentTimeMillis(), -ONE_YEAR, ONE_YEAR);

        } else if (java.util.Date.class.isAssignableFrom(propertyType)) {
            return randomDate(System.currentTimeMillis(), Long.toString(-ONE_YEAR), Long.toString(ONE_YEAR));

        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            double minval = parseDouble(column.minimumValue(), -100.0);
            double maxval = parseDouble(column.maximumValue(), 100.0);
            return randomBigDecimal(minval, maxval);

        } else if (BigInteger.class.isAssignableFrom(propertyType)) {
            int minval = parseInt(column.minimumValue(), 1);
            int maxval = parseInt(column.maximumValue(), 20);
            return randomBigInteger(minval, maxval);

        } else if (String.class.isAssignableFrom(propertyType)) {
            long size = (column == null || column.size() > 127 || column.size() <= 0) ? 127 : column.size();
            return randomString(1, (int) size);

        } else if (Clob.class.isAssignableFrom(propertyType)) {
            try {
                return new SerialClob(randomString(1, 128).toCharArray());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (Blob.class.isAssignableFrom(propertyType)) {
            try {
                return new SerialBlob(randomBytes(1, 128));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (byte[].class.isAssignableFrom(propertyType)) {
            return randomBytes(0, (int) column.size());

        } else if (char[].class.isAssignableFrom(propertyType)) {
            return randomChars(0, (int) column.size());

        } else if (int[].class.isAssignableFrom(propertyType)) {
            return randomInts(0, (int) column.size(), 0, 100);

        } else if (long[].class.isAssignableFrom(propertyType)) {
            return randomLongs(0, (int) column.size(), 0L, 100L);

        } else if (boolean[].class.isAssignableFrom(propertyType)) {
            return randomBooleans(0, (int) column.size());

        } else if (double[].class.isAssignableFrom(propertyType)) {
            return randomDoubles(0, 1000, 0.0, 100.0);

        } else if (float[].class.isAssignableFrom(propertyType)) {
            return randomFloats(0, 1000, 0.0F, 100.0F);

        } else if (String[].class.isAssignableFrom(propertyType)) {
            return randomStrings(0, 1000, 0, (int) column.size());

        } else if (propertyType.isEnum()) {
            if (column.unique() || column.naturalKey() || column.primaryKey()) {
                return sequentialEnum(propertyType, null, index);
            }
            return randomEnum(propertyType, null);
        }

        log.severe("Unhandled randomValue type(" + propertyType + "," + logicalType + "," + table.name() + ","
                + column.name() + ")");
        return null;
    }

    /**
     * @param column
     * @return
     */
    private final static boolean useLogicalType(Column column) {
        if (column != null && column.logicalType() != null && column.logicalType().length() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 
     * @param str
     * @return
     */
    private final static String[] getLogicalArguments(String str) {
        if (str == null) {
            return null;
        }
        if (str.startsWith("custom:")) {
            int index1 = str.indexOf(':');
            int index2 = str.indexOf(',');
            String args[] = new String[3];
            args[0] = str.substring(0, index1).trim();
            args[1] = str.substring(index1 + 1, index2).trim();
            args[2] = str.substring(index2 + 1).trim();
            return args;
        }
        if (str.startsWith("jexl:")) {
            int index1 = str.indexOf(':');
            String args[] = new String[2];
            args[0] = str.substring(0, index1).trim();
            args[1] = str.substring(index1 + 1).trim();
            return args;
        }
        if (str.indexOf(":[") != -1) {
            return str.split(":");
        }
        String[] tokens = str.split(",|:");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }

        return tokens;
    }

    /**
     * @param arguments
     * @return
     */
    public final static String randomPhone(String[] arguments) {
        StringBuilder buf = new StringBuilder();

        buf.append("1-");
        buf.append(randomInt(0, 9));
        buf.append(randomInt(0, 9));
        buf.append(randomInt(0, 9));
        buf.append("-");
        buf.append(randomInt(0, 9));
        buf.append(randomInt(0, 9));
        buf.append(randomInt(0, 9));
        buf.append("-");
        buf.append(randomInt(0, 9));
        buf.append(randomInt(0, 9));
        buf.append(randomInt(0, 9));
        buf.append(randomInt(0, 9));
        return buf.toString();
    }

    /**
     * 
     * @param filename
     * @return
     */
    public final static List<CSVRecord> parseCSV(String filename) {
        try {
            String str = FileUtil.readFile(filename);
            return new CSVParser(new StringReader(str), CSVFormat.newFormat(',')).getRecords();
        } catch (Exception ex) {
            log.throwing("Unable to parse CSV file " + filename, "", ex);
        }
        return new ArrayList<CSVRecord>();
    }

    /**
     * @param arguments
     * @return a randomly generate string, which is a person's name.
     */
    public final static String randomName(String[] arguments) {
        CSVRecord item = getRecord(arguments[1], -1);
        if (item == null || item.size() == 0) {
            return randomString(5, 25);
        }
        String name = item.get(0);
        if (isEmpty(name)) {
            name = randomString(5, 25);
        }
        return name.trim();
    }

    /**
     * 
     * @param arguments
     * @param index
     * @return a sequentially generate string, which is a person's name.
     */
    public final static String sequentialName(String[] arguments, int index) {
        CSVRecord item = getRecord(arguments[1], index);

        String name = (item == null) ? null : item.get(0);
        if (isEmpty(name)) {
            name = randomString(5, 25);
        }
        return name.trim();
    }

    /**
     * 
     * @param bean
     * @param arguments
     * @return
     */
    public final static <T> String companion(T bean, String[] arguments) {
        String fieldname = arguments[1];
        String filename = arguments[2];

        Object value = DatabaseUtil.getValue(bean, fieldname);

        System.out.println("companion: " + fieldname + "," + filename + ", " + value);
        if (value == null) {
            return null;
        }
        String name1 = value.toString();

        List<CSVRecord> items = getFile(filename);

        int maxMatchChars = 0;
        String matchFilename = null;
        for (CSVRecord item : items) {
            String name2 = item.get(0).trim();
            int matchChars = matchingLastChars(name2, name1);
            if (matchChars > maxMatchChars) {
                maxMatchChars = matchChars;
                matchFilename = name2;
            }
        }

        System.out.println("companion: " + fieldname + "," + filename + ", " + maxMatchChars + "," + matchFilename);
        return matchFilename;
    }

    /**
     * 
     * @param str1
     * @param str2
     * @return
     */
    private final static int matchingLastChars(String str1, String str2) {
        int nchars = 0;
        for (int i = str1.length() - 1, j = str2.length() - 1; i >= 0 && j >= 0; i--, j--) {
            if (str1.charAt(i) != str2.charAt(j)) {
                break;
            }
            nchars = nchars + 1;
        }
        return nchars;
    }

    /**
     * @param bean
     * @param arguments
     * @return
     */
    public final static <T> Object randomCustom(T bean, String[] arguments) {
        if (arguments == null || arguments.length < 3) {
            return null;
        }
        return internalJexl(bean, arguments[1], arguments[2]);
    }

    /**
     * @param arguments
     * @return the randomly generated string which is an email address, although the
     *         email address will not necessarily exists.
     */
    public final static String randomEmail(String[] arguments) {
        if (arguments.length == 2) {
            return randomName(arguments);
        }

        String firstArgs[] = new String[] { "name", "firstnames.txt" };
        String lastArgs[] = new String[] { "name", "companies.txt" };
        return randomName(firstArgs) + "@" + randomName(lastArgs) + ".com";
    }

    /**
     * @param arguments
     * @param index
     * @return the sequentially generated string which is an email address, although
     *         the email address will not necessarily exists.
     */
    public final static String sequentialEmail(String[] arguments, int index) {
        if (arguments.length == 2) {
            return sequentialName(arguments, index);
        }

        String firstArgs[] = new String[] { "name", "firstnames.txt" };
        String lastArgs[] = new String[] { "name", "companies.txt" };
        return sequentialName(firstArgs, index) + "@" + sequentialName(lastArgs, index) + ".com";
    }

    /**
     * @param length
     * @return the randomly generated string, of specified length.
     */
    public final static String randomString(int length) {
        return randomString(0, length);
    }

    /**
     * @param minlen
     * @param maxlen
     * @return the randomly generated string, of random length between min and max
     *         len.
     */
    public final static String randomString(int minlen, int maxlen) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        StringBuilder buf = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int i1 = randomInt(0, 26);
            buf.append(TEMPLATE.charAt(i1));
        }
        return buf.toString();
    }

    /**
     * @param min
     * @param max
     * @return a randomly generated byte between specified values.
     */
    public final static byte randomByte(int min, int max) {
        return (byte) randomInt(min, max);
    }

    /**
     * @param minlen
     * @param maxlen
     * @return
     */
    public final static byte[] randomBytes(int minlen, int maxlen) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        byte[] buf = new byte[length];
        for (int i = 0; i < length; i++) {
            buf[i] = (byte) randomInt(-127, 127);
        }
        return buf;
    }

    /**
     * @param minlen
     * @param maxlen
     * @return
     */
    public final static char[] randomChars(int minlen, int maxlen) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = randomChar('a', 'z');
        }
        return buf;
    }

    /**
     * @param minlen
     * @param maxlen
     * @param minval
     * @param maxval
     * @return
     */
    public final static int[] randomInts(int minlen, int maxlen, int minval, int maxval) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        int[] buf = new int[length];
        for (int i = 0; i < length; i++) {
            buf[i] = randomInt(minval, maxval);
        }
        return buf;
    }

    /**
     * @param minlen
     * @param maxlen
     * @param minval
     * @param maxval
     * @return
     */
    public final static long[] randomLongs(int minlen, int maxlen, long minval, long maxval) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        long[] buf = new long[length];
        for (int i = 0; i < length; i++) {
            buf[i] = randomLong(minval, maxval);
        }
        return buf;
    }

    /**
     * @param minlen
     * @param maxlen
     * @param minval
     * @param maxval
     * @return
     */
    public final static double[] randomDoubles(int minlen, int maxlen, double minval, double maxval) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        double[] buf = new double[length];
        for (int i = 0; i < length; i++) {
            buf[i] = randomDouble(minval, maxval);
        }
        return buf;
    }

    /**
     * @param minlen
     * @param maxlen
     * @param minval
     * @param maxval
     * @return
     */
    public final static float[] randomFloats(int minlen, int maxlen, float minval, float maxval) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        float[] buf = new float[length];
        for (int i = 0; i < length; i++) {
            buf[i] = randomFloat(minval, maxval);
        }
        return buf;
    }

    /**
     * @param minlen
     * @param maxlen
     * @return
     */
    public final static boolean[] randomBooleans(int minlen, int maxlen) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        boolean[] buf = new boolean[length];
        for (int i = 0; i < length; i++) {
            buf[i] = randomBoolean(50.0);
        }
        return buf;
    }

    /**
     * @param mask
     * @return
     */
    public final static String randomString(String mask) {
        StringBuilder buf = new StringBuilder();
        for (char c : mask.toCharArray()) {
            switch (c) {
            case '9':
                buf.append(randomChar('0', '9'));
                break;
            case 'a':
                buf.append(randomChar('a', 'z'));
                break;
            case 'A':
                buf.append(randomChar('A', 'Z'));
                break;
            default:
                buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * @param minlen
     * @param maxlen
     * @param minval
     * @param maxval
     * @return
     */
    public final static String[] randomStrings(int minlen, int maxlen, int minslen, int maxslen) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        String[] buf = new String[length];
        for (int i = 0; i < length; i++) {
            buf[i] = randomString(minslen, maxslen);
        }
        return buf;
    }

    /**
     * @param min
     * @param max
     * @return
     */
    public final static Character randomChar(char min, char max) {
        int pos = randomInt(min, max);
        return (char) pos;
    }

    /**
     * @param typename
     * @param arguments
     * @return
     */
    public final static Object randomEnum(Class typename, String[] arguments) {
        String value = randomList(arguments);
        if (value != null) {
            if (typename != null) {
                return Enum.valueOf(typename, value);
            }
        }
        return (typename == null) ? null : randomValues(typename.getEnumConstants());
    }

    /**
     * @param typename
     * @param arguments
     * @param index
     * @return
     */
    public final static Object sequentialEnum(Class typename, String[] arguments, int index) {
        String value = sequentialList(arguments, index);
        if (value == null) {
            if (typename != null) {
                return sequentialList(typename.getEnumConstants(), index);
            }
        }
        if (value != null) {
            if (typename != null) {
                return Enum.valueOf(typename, value);
            }
        }
        return null;
    }

    /**
     * @param percentage
     * @return
     */
    public final static Boolean randomBoolean(double percentage) {
        double value = randomDouble(0.0, 100.0);
        return (value < 50.0) ? true : false;
    }

    /**
     * @param nlen
     * @return
     */
    public final static int randomInt(int nlen) {
        return (int) randomDouble(0.0, nlen);
    }

    /**
     * @param i1
     * @param i2
     * @return
     */
    public final static int randomInt(int i1, int i2) {
        if (i1 > i2) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        return (int) randomDouble(i1, i2);
    }

    /**
     * @param i1
     * @param i2
     * @return
     */
    public final static short randomShort(int i1, int i2) {
        if (i1 > i2) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        return (short) ((i2 - i1) * random.nextDouble() + i1);
    }

    /**
     * @param min
     * @param max
     * @return
     */
    public final static long randomLong(long min, long max) {
        return (long) randomDouble(min, max);
    }

    /**
     * @param min
     * @param max
     * @return
     */
    public final static double randomDouble(double min, double max) {
        return random.nextDouble() * (max - min) + min;
    }

    /**
     * @param min
     * @param max
     * @return
     */
    public final static float randomFloat(float min, float max) {
        return ((float) random.nextDouble() * (max - min) + min);
    }

    /**
     * @param min
     * @param max
     * @return
     */
    public final static BigDecimal randomBigDecimal(double min, double max) {
        return new BigDecimal(randomDouble(min, max));
    }

    /**
     * @param minlen
     * @param maxlen
     * @return
     */
    public final static BigInteger randomBigInteger(int minlen, int maxlen) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        StringBuilder buf = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char i1 = randomChar('0', '9');
            buf.append(i1);
        }
        return new BigInteger(buf.toString());
    }

    /**
     * @param values
     * @return
     */
    public final static <T> T randomValues(T[] values) {
        return (values == null || values.length == 0) ? null : values[randomInt(0, values.length)];
    }

    /**
     * @param arguments
     * @return
     */
    public final static String randomList(String[] arguments) {
        if (arguments != null && arguments.length > 1) {
            String[] array = toList(arguments[1]);
            if (array == null || array.length == 0) {
                return null;
            }
            return array[randomInt(0, array.length)];
        }
        return null;
    }

    /**
     * @param values
     * @param index
     * @return
     */
    public final static <T> T sequentialList(T[] values, int index) {
        return (values == null || values.length == 0) ? null : values[index % values.length];
    }

    /**
     * @param arguments
     * @param index
     * @return
     */
    public final static String sequentialList(String[] arguments, int index) {
        if (arguments == null) {
            return null;
        }
        String[] array = toList(arguments[1]);
        return array[index % array.length];
    }

    /**
     * @param argument
     * @return
     */
    public final static String[] toList(String argument) {
        if (argument == null) {
            return null;
        }
        return argument.split("[\\[\\],]");
    }

    /**
     * @param bean
     * @param table
     * @param arguments
     * @return
     */
    public final static <T> Object randomTable(T bean, Table table, String[] arguments) {
        String databasename = arguments[1];
        String tablename = arguments[2];
        String fieldname = arguments[3];

        String key = makeKey(databasename, tablename);
        List beans = tableCache.get(key);
        if (beans == null || beans.size() == 0) {
            log.severe("ERROR: table processing out of order: " + tablename + " SB before " + table.name());
            return null;
        }

        List<T> filteredBeans = filterBeans(bean, beans, arguments);

        int index = randomInt(0, filteredBeans.size());
        return DatabaseUtil.getValue(filteredBeans.get(index), fieldname);
    }

    /**
     * @param bean
     * @param table
     * @param arguments
     * @return
     */
    public final static <T> Object randomRef(T bean, Table table, String[] arguments) {
        String connectionName = arguments[1];
        String packagename = arguments[2];
        String databasename = arguments[3];
        String tablename = arguments[4];
        String fieldname = arguments[5];

        String key = makeKey(databasename, tablename);
        List beans = tableCache.get(key);
        if (beans == null) {
            try {
                DatabaseInterface dao = DatabaseFactory.getInstance(connectionName);
                tableCache.put(key, dao.queryAll(DatabaseUtil.toTableClass(packagename, tablename)));
                beans = tableCache.get(key);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        List<T> filteredBeans = filterBeans(bean, beans, arguments);

        int index = randomInt(0, filteredBeans.size());
        return DatabaseUtil.getValue(filteredBeans.get(index), fieldname);
    }

    /**
     * @param systime
     * @param minDt
     * @param maxDt
     * @return
     */
    public final static long randomLong(long systime, long minDt, long maxDt) {
        return randomLong(systime - minDt, systime + maxDt);
    }

    /**
     * @return
     */
    public final static long randomTimestamp() {
        return randomLong(System.currentTimeMillis() - ONE_YEAR, System.currentTimeMillis() + ONE_YEAR);
    }

    /**
     * @param systime
     * @param minDtStr
     * @param maxDtStr
     * @return
     */
    public final static Date randomDate(long systime, String minDtStr, String maxDtStr) {
        long minDt = Long.parseLong(minDtStr);
        long maxDt = Long.parseLong(maxDtStr);
        return new Date(randomLong(systime - minDt, systime + maxDt));
    }

    /**
     * @param systime
     * @param minDt
     * @param maxDt
     * @return
     */
    public final static Date randomDate(long systime, long minDt, long maxDt) {
        return new Date(randomLong(systime - minDt, systime + maxDt));
    }

    /**
     * @param systime
     * @param minDtStr
     * @param maxDtStr
     * @return
     */
    public final static Timestamp randomTimestamp(long systime, String minDtStr, String maxDtStr) {
        long minDt = Long.parseLong(minDtStr);
        long maxDt = Long.parseLong(maxDtStr);
        long time = randomLong(systime - minDt, systime + maxDt);
        return new Timestamp(time);
    }

    /**
     * @param systime
     * @param minDt
     * @param maxDt
     * @return
     */
    public final static Timestamp randomTimestamp(long systime, long minDt, long maxDt) {
        long time = randomLong(systime - minDt, systime + maxDt);
        return new Timestamp(time);
    }

    /**
     * @param systime
     * @param arguments
     * @return
     */
    public final static String randomDateTime(long systime, String[] arguments) {
        long minDt = parseLong((arguments.length < 2) ? null : arguments[1], -ONE_YEAR);
        long maxDt = parseLong((arguments.length < 3) ? null : arguments[2], ONE_YEAR);
        long time = randomLong(systime - minDt, systime + maxDt);
        if (arguments.length > 3 && arguments[3] != null && arguments[3].length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(arguments[3]);
            return sdf.format(new Date(time));
        }
        SimpleDateFormat df = new SimpleDateFormat(DF);
        return df.format(new Date(time));
    }

    /**
     * @param masks
     * @return
     */
    public final static String randomIP(String[] masks) {
        int index = randomInt(0, masks.length);
        return randomIP(masks[index]);
    }

    // randoMIP("10.X.X.X");
    /**
     * @param mask
     * @return
     */
    public final static String randomIP(String mask) {
        String value = mask;
        while (true) {
            int index = value.indexOf('X');
            if (index == -1) {
                break;
            }
            value = value.substring(0, index) + randomInt(0, 255) + value.substring(index + 1);
        }
        return value;
    }

    /**
     * @param mask
     * @return
     */
    public final static String randomMac(String str) {
        int length = Integer.parseInt(str);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            if (buf.length() > 0) {
                buf.append(":");
            }
            buf.append(String.format("%02X", randomInt(0, 255)));
        }
        return buf.toString();
    }

    /**
     * @param arguments
     * @return
     */
    public final static List<String> randomLatLon(String[] arguments) {
        String latlons[] = arguments[1].split(" ");
        double lat = Double.parseDouble(latlons[0].substring(0, latlons[0].length() - 1));
        if (latlons[0].indexOf('S') != -1) {
            lat = -lat;
        }
        double lon = Double.parseDouble(latlons[1].substring(0, latlons[1].length() - 1));
        if (latlons[1].indexOf('W') != -1) {
            lon = -lon;
        }
        double dlat = Double.parseDouble(latlons[2]);
        double dlon = Double.parseDouble(latlons[3]);

        double nlat = randomDouble(lat - dlat, lat + dlat);
        double nlon = randomDouble(lon - dlon, lon + dlon);

        List<String> results = new ArrayList<String>();
        results.add(Double.toString(nlat));
        results.add(Double.toString(nlon));

        return results;
    }

    /**
     * @param minlen
     * @param maxlen
     * @param minval
     * @param maxval
     * @return
     */
    public final static String randomPorts(int minlen, int maxlen, int minval, int maxval) {
        int length = (minlen == maxlen) ? maxlen : randomInt(minlen, maxlen);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(Integer.toString(randomInt(minval, maxval)));
        }
        return buf.toString();
    }

    /**
     * 
     * @param str
     * @return
     */

    public final static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    /**
     * 
     * @param number
     * @param defaultValue
     * @return
     */
    public final static int parseInt(String number, int defaultValue) {
        try {
            if (number != null && number.length() > 0) {
                return Integer.parseInt(number);
            }
        } catch (Exception ex) {
            log.throwing("Parsing " + number, "", ex);
        }
        return defaultValue;
    }

    /**
     * 
     * @param number
     * @param defaultValue
     * @return
     */
    public final static long parseLong(String number, long defaultValue) {
        try {
            if (number != null && number.length() > 0) {
                return Long.parseLong(number);
            }
        } catch (Exception ex) {
            log.throwing("Parsing " + number, "", ex);
        }
        return defaultValue;
    }

    /**
     * 
     * @param number
     * @param defaultValue
     * @return
     */
    public final static double parseDouble(String number, double defaultValue) {
        try {
            if (number != null && number.length() > 0) {
                return Double.parseDouble(number);
            }
        } catch (Exception ex) {
            log.throwing("Parsing " + number, "", ex);
        }
        return defaultValue;
    }

    /**
     * 
     * @param type
     * @return
     */
    public final static String[] parseTypes(String type) {
        return type.split(DELIMITERS);
    }

    /**
     * 
     * @param theBean
     * @param beans
     * @param arguments
     * @return
     */
    public final static <T> List<T> filterBeans(T theBean, List<T> beans, String[] arguments) {
        if (arguments == null || arguments.length < 5) {
            return beans;
        }
        String key1 = arguments[3];
        String key2 = arguments[4];
        Object value1 = DatabaseUtil.getValue(theBean, key1);

        List<T> items = new ArrayList<T>();
        for (T bean : beans) {
            Object value2 = DatabaseUtil.getValue(bean, key2);
            if (value1 == null && value2 == null) {
                items.add(bean);
            } else if (value1 == null) {

            } else if (value2 == null) {

            } else if (value1.toString().equalsIgnoreCase(value2.toString())) {
                items.add(bean);
            }
        }
        return items;
    }

    /**
     * @param filename
     * @return
     */
    private final static String toDataFilename(String filename) {
        if (filename.contains("/")) {
            return filename;
        }
        return "res:/etc/data/lists/" + filename;
    }

    /**
     * 
     * @param bean
     * @param generator
     * @throws Exception
     */
    public final static <T> void executeGenerator(T bean, String generator) throws Exception {
        if (generator == null) {
            return;
        }
        int length = generator.lastIndexOf('.');
        if (length == -1) {
            return;
        }

        String classname = generator.substring(0, length);
        String methodname = generator.substring(length + 1);

        Class cls = Class.forName(classname);
        Object obj = cls.newInstance();

        // call the printIt method
        Method method = cls.getDeclaredMethod(methodname, bean.getClass());
        method.invoke(obj, bean);
    }

    /**
     * 
     * @param filename
     * @param index
     * @return
     */
    private final static CSVRecord getRecord(String filename, int index) {
        List<CSVRecord> items = getFile(filename);
        if (items.size() == 0) {
            return null;
        }
        if (index == -1) {
            index = randomInt(1, items.size());
        }
        return items.get(index % items.size());
    }

    /**
     * 
     * @param filename
     * @return
     */
    private final static List<CSVRecord> getFile(String filename) {
        if (cache.get(filename) == null) {
            cache.put(filename, parseCSV(toDataFilename(filename)));
        }
        return cache.get(filename);
    }

    /**
     * 
     * @param bean
     * @param className
     * @param expr
     * @return
     */
    public final static <T> Object internalJexl(T bean, String className, String expr) {
        try {
            if (expr == null) {
                return null;
            }

            Map<String, Object> map = new HashMap<String, Object>();
            if (className != null) {
                map.put("custom", Class.forName(className).newInstance());
            }

            map.put("bean", bean);

            return JEXLUtil.getInstance().eval(expr, map);
        } catch (Exception ex) {
            System.err.println("internalJexl: expr=" + expr + ", " + bean.getClass());
            log.throwing("randomCustom " + className + ":" + expr, "", ex);
        }
        return null;
    }

    /**
     * 
     * @param generatorName
     * @return
     */
    public final static Object generateRandomValue(String generatorName) {
        if (generatorName == null) {
            return null;
        }
        int length = generatorName.lastIndexOf('.');
        if (length == -1) {
            return null;
        }

        String classname = generatorName.substring(0, length);
        String methodname = generatorName.substring(length + 1);

        try {

            Class cls = Class.forName(classname);
            return cls.getDeclaredMethod(methodname).invoke(cls.newInstance());
        } catch (Exception ex) {
            System.err.println("generateRandomValue: generatorName=" + generatorName);
        }
        return null;
    }

    private final static String makeKey(String databasename, String tablename) {
        return databasename + "." + tablename;
    }
}
