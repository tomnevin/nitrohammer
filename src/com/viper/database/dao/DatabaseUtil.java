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

package com.viper.database.dao;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;

import com.viper.database.annotations.Column;
import com.viper.database.annotations.ForeignKey;
import com.viper.database.annotations.Table;
import com.viper.database.dao.converters.Converters;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.filters.Predicate;
import com.viper.database.interfaces.BeanGeneratorInterface;
import com.viper.database.interfaces.ColumnValidatorInterface;
import com.viper.database.interfaces.SqlGeneratorInterface;
import com.viper.database.interfaces.TableConverterInterface;
import com.viper.database.interfaces.TableValidatorInterface;
import com.viper.database.model.Cell;
import com.viper.database.model.ColumnParam;
import com.viper.database.model.Database;
import com.viper.database.model.EnumItem;
import com.viper.database.model.Param;
import com.viper.database.model.Row;

/**
 *
 */
public class DatabaseUtil {

    private static final Logger log = Logger.getLogger(DatabaseUtil.class.getName());
    private final static PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    public static final String toPathname(String urlStr) {
        try {
            return new File(new URL(urlStr).getFile()).getAbsolutePath();
        } catch (Exception ex) {
            // Intentionally blank
        }
        return urlStr;
    }

    public static final String replaceTokens(String content, List<Param> replacements) {
        Map<String, String> map = new HashMap<String, String>();
        for (Param r : replacements) {
            map.put(r.getName(), r.getValue());
        }
        return replaceTokens(content, map);
    }

    public static final String replaceTokens(String content, Map<String, String> replacements) {
        Pattern pattern = Pattern.compile("#\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(content);
        // populate the replacements map ...
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            String replacement = replacements.get(matcher.group(1));
            builder.append(content.substring(i, matcher.start()));
            if (replacement == null) {
                builder.append(matcher.group(0));
            } else {
                builder.append(replacement);
            }
            i = matcher.end();
        }
        builder.append(content.substring(i, content.length()));

        return builder.toString();
    }

    /**
     * Escape a string, replacing escapable character with "\\"
     * 
     * @param str
     *            original string to be escaped
     * @param escapeChar
     *            character to be escaped.
     * @return escaped string
     */

    public static final String escape(String str, String escapeChar) {
        return str.replace(escapeChar, "\\" + escapeChar);
    }

    /**
     * Look for key value pair matches in the supplied java bean.
     * 
     * @param item
     * @param keyValue
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return a match if bean contains all key value pairs
     */
    public static final <T> boolean isMatch(T item, Object[] keyValue) {

        if (keyValue != null && (keyValue.length % 2) == 0) {
            for (int j = 0; j < (keyValue.length - 1); j = j + 2) {
                String key = (String) keyValue[j];
                Object value = keyValue[j + 1];

                if (DatabaseInterface.PAGESIZE_KEY.equals(key)) {
                    continue;
                }

                if (!hasPropertyName(item.getClass(), key)) {
                    return false;
                }

                Object beanValue = getValue(item, key);
                if (beanValue == null) {
                    if (value != null) {
                        return false;
                    }
                } else if (value == null) {
                    if (beanValue != null) {
                        return false;
                    }
                } else if (beanValue instanceof Collection) {
                    if (!((Collection<?>) beanValue).contains(value)) {
                        return false;
                    }
                } else if (beanValue instanceof String && value instanceof String) {
                    if (!((String) beanValue).equalsIgnoreCase((String) value)) {
                        return false;
                    }
                } else {
                    // System.out.println("IsMatch: " + beanValue + "," +
                    // value);
                    if (!beanValue.toString().equalsIgnoreCase(value.toString())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Given a list of objects (database beans) scan the list for matches in the
     * keyValue pairs. Returning a new sublist of only the matches. Between pairs an
     * "AND" operation is performed.
     * 
     * @param list
     *            the list of beans to be scanned (filtered).
     * @param keyValue
     *            the VarArgs key value pairs, the key is the beans property name
     * @param <T>
     *            the class of the POJO database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the sublist if beans which match the key value pair filter.
     */
    public static final <T> List<T> findAllItems(List<T> list, Object... keyValue) {
        List<T> matches = new ArrayList<T>();
        if (list != null) {
            for (T item : list) {
                if (isMatch(item, keyValue)) {
                    matches.add(item);
                }
            }
        }
        return matches;
    }

    /**
     * Given a list of objects (database beans) scan the list for matches in the
     * keyValue pairs. Returning a new sublist of only the matches.
     * 
     * @param list
     *            the list of beans to be scanned (filtered).
     * @param key
     *            the name of the property which is to be filtered.
     * @param value
     *            the value of the property for a match
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the sublist if beans which match the key value pair filter.
     */
    public static final <T> T findOneItem(List<T> list, String key, Object value) {
        if (list != null) {
            Object[] keyValue = new Object[] { key, value };
            for (T item : list) {
                if (isMatch(item, keyValue)) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Given a list of objects (database beans) scan the list for matches in the
     * keyValue pairs. Returning a new sublist of only the matches.
     * 
     * @param list
     *            the list of beans to be scanned (filtered).
     * @param key
     *            the name of the property which is to be filtered.
     * @param value
     *            the value of the property for a match
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the sublist if beans which match the key value pair filter.
     */
    public static final <T> int indexOf(List<T> list, String key, Object value) {
        if (list != null) {
            Object[] keyValue = new Object[] { key, value };
            for (int i = 0; i < list.size(); i++) {
                T item = list.get(i);
                if (isMatch(item, keyValue)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Given a list of objects (database beans) scan the list for matches in the key
     * value pair. Returning a new sublist of only the matches.
     * 
     * @param list
     *            the list of beans to be scanned (filtered).
     * @param key
     *            the name of the property which is to be filtered.
     * @param values
     *            the possible values of the property for a match
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the sublist if beans which match the key value pair filter.
     */
    public static final <T> List<T> findManyItems(List<T> list, String key, List<String> values) {
        List<T> results = new ArrayList<T>();
        if (values != null) {
            for (String value : values) {
                List<T> result = findAllItems(list, key, value);
                if (result != null) {
                    results.addAll(result);
                }
            }
        }
        return results;
    }

    public static final Object findValue(Row row, String name) {
        return findValue(row.getCells(), name);
    }

    public static final Object findValue(List<Cell> list, String name) {
        if (list != null && name != null) {
            for (Cell item : list) {
                if (name.equalsIgnoreCase(item.getName())) {
                    return item.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 
     */
    public static final Column findColumnAnnotation(List<Column> columns, String tablename, String fieldname) {
        for (Column column : columns) {
            if (tablename.equalsIgnoreCase(column.tableName())) {
                if (fieldname.equalsIgnoreCase(column.field())) {
                    return column;
                }
            }
        }
        for (Column column : columns) {
            if (column.tableName() == null || column.tableName().isEmpty()) {
                if (fieldname.equalsIgnoreCase(column.field())) {
                    return column;
                }
            }
        }
        for (Column column : columns) {
            if (column.tableName() == null || column.tableName().isEmpty()) {
                if (fieldname.equalsIgnoreCase(column.name())) {
                    return column;
                }
            }
        }
        return null;
    }

    /**
     * @param items
     * @param name
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return
     */
    public static final <T> List<String> toList(List<T> items, String name) {
        List<String> list = new ArrayList<String>();
        for (T item : items) {
            list.add("" + getValue(item, name));
        }
        return list;
    }

    /**
     * Returns first loaded Class found in the searchPackages
     * 
     * @param classname
     *            the simple class name (e.g. "String")
     * @param searchPackages
     *            String[] of packages to search.
     *            <li>Place the more important packages at the top since the first
     *            Class found is returned</li> <code>//Example
     *                        public static final String[] searchPackages = {
     *                          "java.lang",
     *                          "java.util",
     *                          "my.company",
     *                          "my.company.other" };
     *                       </code>
     * @return the loaded Class or null if not found
     */
    public static final Class<?> findClassBySimpleName(String classname, List<String> searchPackages) {
        try {
            return Class.forName(classname);
        } catch (ClassNotFoundException e) {
            // simplename is not a fullname, try the package list
        }
        for (String searchPackage : searchPackages) {
            try {
                return Class.forName(searchPackage + "." + classname);
            } catch (ClassNotFoundException e) {
                // not in this package, try another
            }
        }
        return null;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to
     * the given packages.
     * 
     * @param packageName
     *            The base package
     * @return list of classes in the package which are table annotated.
     * 
     */
    public static final List<Class<?>> getClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        if (packageName != null) {

            try {
                String path = packageName.replace('.', '/');
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Enumeration<URL> resources = classLoader.getResources(path);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();

                    List<Class<?>> clazzes = null;
                    if ("file".equalsIgnoreCase(resource.getProtocol())) {
                        clazzes = findClassesInFile(classLoader, new File(resource.getFile()), packageName);
                    }
                    if ("jar".equalsIgnoreCase(resource.getProtocol())) {
                        clazzes = findClassesInJar(classLoader, resource, packageName);
                    }
                    if (clazzes != null) {
                        for (Class<?> clazz : clazzes) {
                            if (!classes.contains(clazz)) {
                                classes.add(clazz);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ; // intentionally blank
            }
        }
        return classes;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to
     * the given packages.
     * 
     * @param packageName
     *            The base package
     * @return list of classes in the package which are table annotated.
     * 
     */
    public static final List<Class<?>> getClasses(List<String> packageNames) {
        List<Class<?>> items = new ArrayList<Class<?>>();
        for (String packagename : packageNames) {
            List<Class<?>> list = getClasses(packagename);
            if (list != null) {
                items.addAll(list);
            }
        }
        return items;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     * 
     * @param classLoader
     * 
     * @param directory
     *            The base directory
     * @param packageName
     *            The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static final List<Class<?>> findClassesInFile(ClassLoader classLoader, File directory, String packageName)
            throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) { 
                try {
                    classes.addAll(findClassesInFile(classLoader, file, packageName + "." + file.getName()));
                } catch (Throwable ex) {
                    log.severe("findClassesInFile: directory=" + file.getAbsolutePath() + ", " + ex);

                }
            } else if (file.getName().endsWith(".class")) { 
                try {
                    classes.add(Class
                            .forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                } catch (Throwable ex) {
                    log.severe("findClassesInFile: directory=" + file.getAbsolutePath() + ", " + ex);

                }
            }
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     * 
     * @param classLoader
     * @param resource
     *            The base directory
     * @param packageName
     *            The package name for classes found inside the base directory
     * @return The classes
     * @throws Exception
     */
    private static final List<Class<?>> findClassesInJar(ClassLoader classLoader, URL resource, String packageName)
            throws Exception {

        List<Class<?>> classes = new ArrayList<Class<?>>();

        packageName = packageName.replace('.', '/');

        JarURLConnection uc = (JarURLConnection) resource.openConnection();
        JarFile jarFile = uc.getJarFile();

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry == null) {
                break;
            }
            if (jarEntry.getName().startsWith(packageName) && jarEntry.getName().endsWith(".class")) {
                String classname = null;
                try {
                    String name = jarEntry.getName();
                    classname = name.replace('/', '.').substring(0, name.length() - ".class".length());
                    classes.add(classLoader.loadClass(classname));
                } catch (Throwable t) {
                    log.severe("findClassesInJar: Cant find class: " + classname);
                }
            }
        }

        jarFile.close();
        return classes;
    }

    /**
     * Given the class loader, the package name and the database name find all table
     * classes for this database.
     * 
     * 
     * @param packageName
     *            - package name where database classes can be scanned for.
     * @param annotationClass
     *            - the name of the database in which to scan for classes containing
     *            Table annotations, if null return all classes which map to a
     *            table, for all databases.
     * @return the list of all classes in the package.
     */
    public static final List<Class<?>> getClassesWithAnnotation(String packageName, Class annotationClass) {

        List<Class<?>> items = new ArrayList<Class<?>>();
        List<Class<?>> classes = getClasses(packageName);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotationClass)) {
                items.add(clazz);
            }
        }
        return items;
    }

    /**
     * Given the class loader, the package name and the database name find all table
     * classes for this database.
     * 
     * @param packageName
     *            - package name where database classes can be scanned for.
     * 
     * @return the list of all classes in the package.
     */
    public static final List<Class<?>> getDatabaseClasses(String packageName) {

        List<Class<?>> dbClasses = new ArrayList<Class<?>>();

        List<Class<?>> classes = getClasses(packageName);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Table.class)) {
                dbClasses.add(clazz);
            }
        }
        return dbClasses;
    }

    public static final Object newInstance(String classname) throws Exception {
        return Class.forName(classname).newInstance();
    }

    public static final <T, S> T newInstance(Class<T> clazz, S value) throws Exception {
        return clazz.getConstructor(value.getClass()).newInstance(value);
    }

    /**
     * Given the table name, return the bean, which is a java model of the table.
     * 
     * @param packagename
     *            the name of the database, underlying database can be schema,
     *            catalog or filename based on implementation.
     * @param databasename
     *            the name of the database, underlying database can be schema,
     *            catalog or filename based on implementation.
     * @param tablename
     *            the name of the table, for java model is desired.
     * @return the java class which contains annotations which model the table, null
     *         if not defined.
     *
     */
    public static final Class<?> toTableClass(String packagename, String tablename) {

        List<Class<?>> classes = getClasses(packagename);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Table.class)) {
                Table table = (Table) clazz.getAnnotation(Table.class);
                if (table.name().equalsIgnoreCase(tablename)) {
                    return clazz;
                }
            }
        }
        return null;
    }

    /**
     * Given the table name, return the bean, which is a java model of the table.
     * 
     * @param packagename
     *            the name of the database, underlying database can be schema,
     *            catalog or filename based on implementation.
     * @param databasename
     *            the name of the database, underlying database can be schema,
     *            catalog or filename based on implementation.
     * @param tablename
     *            the name of the table, for java model is desired.
     * @return the java class which contains annotations which model the table, null
     *         if not defined.
     *
     */
    public static final Class<?> toTableClass(List<String> packagenames, String tablename) {

        for (String packagename : packagenames) {
            Class<?> clazz = toTableClass(packagename, tablename);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    /**
     * Given the table name, return the bean, which is a java model of the table.
     * 
     * @param packagename
     *            the name of the database, underlying database can be schema,
     *            catalog or filename based on implementation.
     * @param databasename
     *            the name of the database, underlying database can be schema,
     *            catalog or filename based on implementation.
     * @param tablename
     *            the name of the table, for java model is desired.
     * @return the java class which contains annotations which model the table, null
     *         if not defined.
     *
     */
    public static final Class<?> toTableClass(String classname) throws Exception {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = classLoader.loadClass(classname);
        return (clazz.isAnnotationPresent(Table.class)) ? clazz : null;
    }

    /**
     * Given the class loader and the package name find all database classes in the
     * package.
     * 
     * @param packageName
     * @param regexFilter
     * @return the list of classes which are part of the package after being
     *         filtered, zero size array if not classes not found.
     */
    public static final List<Class<?>> listDatabaseTableClasses(String packageName, String regexFilter) {

        List<Class<?>> classes = DatabaseUtil.getDatabaseClasses(packageName);
        List<Class<?>> items = new ArrayList<Class<?>>();
        for (Class<?> item : classes) {
            Table table = (Table) item.getAnnotation(Table.class);
            if (table != null
                    && ("table".equalsIgnoreCase(table.tableType()) || "view".equalsIgnoreCase(table.tableType()))) {
                if (regexFilter == null || table.name().matches(regexFilter)) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * Given the packagename and the database name list all table classes which are
     * found.
     * 
     * @param packageName
     *            the name of the package, separated by dots.
     * @param databaseName
     *            the name of the database.
     * 
     * @return the list of classes which are part of the package and the database by
     *         name, zero size array if not classes not found.
     */
    public static final List<Class<?>> listTableClasses(String packageName, String databaseName) {

        List<Class<?>> classes = getDatabaseClasses(packageName);
        List<Class<?>> items = new ArrayList<Class<?>>();
        for (Class<?> item : classes) {
            Table table = (Table) item.getAnnotation(Table.class);
            if (table != null && databaseName.equalsIgnoreCase(table.databaseName())) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Given the packagename and the database name list all table classes which are
     * found.
     * 
     * @param packageName
     *            the name of the package, separated by dots.
     * @param databaseName
     *            the name of the database.
     * 
     * @return the list of classes which are part of the package and the database by
     *         name, zero size array if not classes not found.
     */
    public static final Map<String, Class<?>> mapTableClasses(String packageName) {

        Map<String, Class<?>> map = new HashMap<String, Class<?>>();

        List<Class<?>> classes = getDatabaseClasses(packageName);
        for (Class<?> item : classes) {
            Table table = (Table) item.getAnnotation(Table.class);
            if (table != null) {
                map.put(table.name().toLowerCase(), item);
            }
        }
        return map;
    }

    /**
     * Given the table class, retrieve the table database name, from the Table
     * annotation.
     * 
     * @param tableClass
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the name of the database, based on the table class (contains table
     *         annotation), null if no database name defined.
     */
    public static <T> String getDatabaseName(Class<T> tableClass) {
        Table table = tableClass.getAnnotation(Table.class);
        if (table == null) {
            System.err.println("Class does not contain a Table annotation: " + tableClass.getName());
            return null;
        }
        String name = table.databaseName();
        if (name == null || name.length() == 0) {
            System.err.println("Database table name is empty: " + tableClass.getName());
            return null;
        }
        return name;
    }

    /**
     * Given the table class, retrieve the table database name, from the Table
     * annotation.
     * 
     * @param tableClass
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the name of the table defined by the table annotation, null if name
     *         not defined.
     */
    public static <T> String getTableName(Class<T> tableClass) {
        Table table = tableClass.getAnnotation(Table.class);
        if (table == null) {
            System.err.println("Class does not contain a Table annotation: " + tableClass.getName());
            return null;
        }
        if (table.tableName() != null && table.tableName().length() > 0) {
            return table.tableName();
        }
        String name = table.name();
        if (name == null || name.length() == 0) {
            System.err.println("Database table name is empty: " + tableClass);
            return null;
        }
        return name;
    }

    /**
     * Given the table class, retrieve the column family names, currently the only
     * supported column family name is the primary key column.
     * 
     * @param tableClass
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column names, defined by the column annotations
     */
    public static <T> List<String> getColumnFamilyNames(Class<T> tableClass) {
        List<String> names = new ArrayList<String>();
        names.add(getTableName(tableClass));
        return names;
    }

    /**
     * Given the table class, retrieve the column names, which have been specified
     * as indexed.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column names if defined by annotation, zero length list
     *         otherwise.
     */
    public static <T> List<String> getQualifierNames(Class<T> tableClazz) {
        List<String> names = new ArrayList<String>();
        Method[] methods = tableClazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column != null && (column.naturalKey() || column.primaryKey() || column.unique())) {
                    names.add(column.field());
                }
            }
        }
        if (names.size() == 0) {
            System.err.println("At least one Index Column must be defined: " + tableClazz);
        }
        return names;
    }

    /**
     * Given the table class, retrieve the primary key column name.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the name of the primary key field, null otherwise.
     */
    public static <T> String getPrimaryKeyName(Class<T> tableClazz) {
        Method[] methods = tableClazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column != null && column.primaryKey()) {
                    return column.name(); // WAS field()
                }
            }
        }
        System.err.println("No primary key name defined for : " + tableClazz); 
        return null;
    }

    /**
     * Given the table class, retrieve the primary key column name.
     * 
     * @param bean
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the value of the primary key field
     * @throws Exception
     */
    public static <T> Object getPrimaryKeyValue(T bean) throws Exception {
        return getValue(bean, getPrimaryKeyName(bean.getClass()));
    }

    /**
     * Given the table class, validate if the primayr key value, is a valid value.
     * 
     * @param bean
     *            java table model, with annotations.
     * @param <T>
     *            the class of the POJO database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return whether the primary key value is valid.
     */
    public static <T> boolean isValidPrimaryKeyValue(T bean) {
        try {
            List<Column> primaryKeyColumns = DatabaseUtil.getPrimaryKeyColumns(bean.getClass());
            Column primaryKeyColumn = primaryKeyColumns.get(0);

            Object value = DatabaseUtil.getValue(bean, primaryKeyColumn.field());
            if ("assigned".equalsIgnoreCase(primaryKeyColumn.idMethod())) {
                return true;
            }
            if (value == null) {
                return false;
            } else if (value instanceof Integer && (Integer) value == 0) {
                return false;
            } else if (value instanceof Long && (Long) value == 0L) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.throwing("isValidPrimaryKeyValue", "", ex);
        }
        return false;
    }

    /**
     * Given the table class, retrieve the primary key column name.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the column annotation defined for the primary key, null otherwise.
     */
    public static <T> List<Column> getPrimaryKeyColumns(Class<T> tableClazz) {
        List<Column> items = new ArrayList<Column>();

        Method[] methods = tableClazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column != null && column.primaryKey()) {
                    items.add(column);
                }
            }
        }
        // System.err.println("No primary key column defined for : " +
        // tableClazz);
        return items;
    }

    /**
     * Given the table class, and the database field names, return whether column
     * data is unique.
     * 
     * @param tableClass
     *            java table model, with annotations.
     * @param fieldname
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the result if the column is defined as unique.
     * 
     */
    public static <T> boolean isUniqueColumn(Class<T> tableClass, String fieldname) {
        Column column = getColumnAnnotation(tableClass, fieldname);
        return (column == null) ? false : (column.unique() || column.primaryKey() || column.naturalKey());
    }

    /**
     * Given the table class, retrieve the primary key column name.
     * 
     * @param bean
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the value of the primary key field
     * @throws Exception
     */
    public static <T> String getNaturalKeyValues(T bean) throws Exception {

        StringBuilder buf = new StringBuilder();

        List<Column> columns = getColumnAnnotations(bean.getClass());

        for (Column column : columns) {
            if (!column.persistent()) {
                continue;
            }
            if (!(column.unique() || column.primaryKey() || column.naturalKey())) {
                continue;
            }

            String value = DatabaseUtil.getString(bean, column.field());
            if (buf.length() > 0) {
                buf.append(".");
            }
            buf.append(value);
        }

        return buf.toString();
    }

    /**
     * 
     * @param dao
     * @param database
     * @return whether the database exists or not.
     */
    public static List<EnumItem> getEnumValues(DatabaseSQLInterface dao, String databaseName, String tableName,
            String columnName) throws Exception {

        List<com.viper.database.model.Column> columns = new SQLDriver().loadColumns(dao, databaseName, tableName,
                columnName);
        for (com.viper.database.model.Column column : columns) {
            return column.getEnumValues();
        }
        return new ArrayList<EnumItem>();
    }

    /**
     * 
     * @param dao
     * @param database
     * @return whether the database exists or not.
     */
    public static boolean isDatabaseExist(DatabaseInterface dao, String database) {
        List<String> names = dao.listDatabases();
        return (names == null || names.contains(database.toLowerCase()));
    }

    /**
     * 
     * @param dao
     * @param database
     * @param table
     * @return whether the table exists or not.
     */

    public static boolean isTableExist(DatabaseInterface dao, String database, String table) {
        List<String> names = dao.listTables(database);
        return (names == null || names.contains(table.toLowerCase()));
    }

    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param fieldname
     * @param <T>
     *            the class of the POJO database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the result the column annotation, null if not annotated field, or
     *         method.
     * 
     * @note the loop here can be a significant performance issue.
     * 
     */
    public static <T> Column getColumnAnnotation(Class<T> clazz, String fieldname) {
        if (clazz == null || fieldname == null) {
            return null;
        } 
        Method[] methods = clazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column == null) {
                    continue;
                }
                if (fieldname.equalsIgnoreCase(column.field()) || fieldname.equalsIgnoreCase(column.name())) {
                    return column;
                }
            }
        }
        return null;
    }
 

    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the table annotation if defined with the class, null otherwise.
     * 
     */
    public static <T> Table getTableAnnotation(Class<T> tableClazz) {
        return tableClazz.getAnnotation(Table.class);
    }


    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the table annotation if defined with the class, null otherwise.
     * 
     */
    public static <T> boolean isTableClass(Class<T> tableClazz) {
        return (tableClazz.getAnnotation(Table.class) != null);
    }

    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    public static <T> List<Column> getColumnAnnotations(Class<T> tableClazz) {
        List<Column> columns = new ArrayList<Column>();

        Method[] methods = tableClazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column != null) {
                    columns.add(column);
                }
            }
        }
        return columns;
    }
 

    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    public static <T> Map<String, Column> getColumnAnnotationsMap(Class<T> clazz) {
        Map<String, Column> columns = new HashMap<String, Column>();

        Method[] methods = clazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column != null) {
                    columns.put(toFullColumnName(clazz, column), column);
                }
            }
        }
        return columns;
    }

    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    public static <T> Map<String, Column> getNestedColumnAnnotations(Class<T> clazz) throws Exception {

        Map<String, Column> columns = new TreeMap<String, Column>();

        String path = clazz.getSimpleName();
        makeNestedColumnAnnotationsInternal(columns, clazz, path);

        return columns;
    }

    private static final <T> void makeNestedColumnAnnotationsInternal(Map<String, Column> columns, Class<T> clazz,
            String path) throws Exception {

        Field[] fields = clazz.getDeclaredFields();
        if (fields != null) {
            for (Field field : fields) {
                Column annotation = field.getAnnotation(Column.class);
                if (annotation == null) {
                    continue;
                }

                String key = path + "." + annotation.name();

                columns.put(key, annotation);

                // Check for generic parameter and generic array
                Type type = field.getGenericType();
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    for (Type gtype : pType.getActualTypeArguments()) {
                        Class<?> clazz1 = Class.forName(gtype.getTypeName());
                        makeNestedColumnAnnotationsInternal(columns, clazz1, key);
                    }
                } else if (type instanceof GenericArrayType) {
                    GenericArrayType pType = (GenericArrayType) type;
                    Class<?> clazz1 = Class.forName(pType.getGenericComponentType().getTypeName());
                    makeNestedColumnAnnotationsInternal(columns, clazz1, key);

                } else {
                    makeNestedColumnAnnotationsInternal(columns, field.getType(), key);
                }
            }
        }
    }

    public static final <T> String toFullColumnName(Class<T> clazz, Column column) {
        String name = clazz.getSimpleName();
        return name + "." + column.name();
    }

    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param clazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    public static void sortColumnsByOrder(List<Column> columns) {
        Collections.sort(columns, new ColumnOrderComparator());
    }

    public static class ColumnOrderComparator implements Comparator<Column> {
        @Override
        public int compare(Column o1, Column o2) {
            return o1.order() - o2.order();
        }
    }

    /**
     * Given the table class, and the database field name, return the foreign key
     * annotation object.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of foreign key annotations if defined with the class, zero
     *         size list otherwise.
     */
    public static <T> List<ForeignKey> getForeignKeyAnnotations(Class<T> tableClazz) {
        List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

        ForeignKey[] keys = tableClazz.getAnnotationsByType(ForeignKey.class);
        for (ForeignKey key : keys) {
            System.out.println("ForeignKey: " + key.name());
            foreignKeys.add(key);
        }
        return foreignKeys;
    }

    /**
     * Given the table class, return all the column field names.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    public static <T> List<String> getColumnFieldNames(Class<T> tableClazz) {
        List<String> names = new ArrayList<String>();
        Method[] methods = tableClazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column != null) {
                    names.add(column.field());
                }
            }
        }
        return names;
    }

    /**
     * Given the table class, return all the column field names.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    public static <T> String getColumnFieldName(Class<T> tableClazz, String name) {
        Method[] methods = tableClazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column != null) {
                    if (name.equalsIgnoreCase(column.name()) || name.equalsIgnoreCase(column.field())) {
                        return (isEmpty(column.field())) ? column.name() : column.field();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Given the table class, return all the column field names.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    public static <T> List<String> getColumnNames(Class<T> tableClazz) {
        List<String> names = new ArrayList<String>();
        if (tableClazz != null) {
            Method[] methods = tableClazz.getMethods();
            if (methods != null) {
                for (Method method : methods) {
                    Column column = method.getAnnotation(Column.class);
                    if (column != null) {
                        names.add(column.name());
                    }
                }
            }
        }
        Collections.sort(names);
        return names;
    }

    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    public static <T> Column getAutoIncrementColumn(Class<T> tableClazz) {

        Method[] methods = tableClazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column != null) {
                    if ("autoincrement".equalsIgnoreCase(column.idMethod())) {
                        return column;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param tableClazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    public static <T> Column getAssignedColumn(Class<T> tableClazz) {

        Method[] methods = tableClazz.getMethods();
        if (methods != null) {
            for (Method method : methods) {
                Column column = method.getAnnotation(Column.class);
                if (column != null) {
                    if ("assigned".equalsIgnoreCase(column.idMethod())) {
                        return column;
                    }
                }
            }
        }
        return null;
    }
 

    /**
     * Call the beans generator class from the annotation.
     * 
     * @param bean
     *            the bean upon which the generator will operate, should NOT be
     *            null.
     * 
     *            On return the bean will be populated with the fields which the
     *            generator is written to update.
     * 
     */
    public static <T> String callSqlGenerator(Class<T> clazz) throws Exception {
        if (clazz == null) {
            return null;
        }

        Table table = getTableAnnotation(clazz);
        if (table == null || table.sqlGenerator() == null || table.sqlGenerator().trim().length() == 0) {
            return null;
        }

        SqlGeneratorInterface api = (SqlGeneratorInterface) newInstance(table.sqlGenerator());
        if (api == null) {
            return null;
        }

        return api.generate(clazz);
    }

    /**
     * Call the beans generator class from the annotation.
     * 
     * @param bean
     *            the bean upon which the generator will operate, should NOT be
     *            null.
     * 
     *            On return the bean will be populated with the fields which the
     *            generator is written to update.
     * 
     */
    public static <T> List<T> callBeanGenerator(List<T> beans) throws Exception {
        if (beans == null || beans.size() == 0) {
            return beans;
        }

        Table table = getTableAnnotation(beans.get(0).getClass());
        if (table == null || table.beanGenerator() == null || table.beanGenerator().trim().length() == 0) {
            return beans;
        }

        BeanGeneratorInterface api = (BeanGeneratorInterface) newInstance(table.beanGenerator());
        if (api == null) {
            return beans;
        }

        for (T bean : beans) {
            api.generate(bean);
        }
        return beans;
    }

    /**
     * Call the beans generator class from the annotation.
     * 
     * @param bean
     *            the bean upon which the generator will operate, should NOT be
     *            null.
     * 
     *            On return the bean will be populated with the fields which the
     *            generator is written to update.
     * 
     */
    public static <T> void callTableConverter(T bean) throws Exception {
        if (bean == null) {
            return;
        }

        Table table = getTableAnnotation(bean.getClass());
        if (table == null || table.converter() == null || table.converter().trim().length() == 0) {
            return;
        }

        TableConverterInterface api = (TableConverterInterface) newInstance(table.converter());
        if (api == null) {
            return;
        }

        api.convert(bean);
    }

    /**
     * Call the bean validation class, and all the column validation classes. Table
     * validation is for overall validation, column validation for each column.
     * There is a custom validation possible per column and a predefined validation
     * per column. String will be validated for length, and possible pattaern match.
     * Numbers can be validated against a range, etc.
     * 
     * @param bean
     *            the bean which is to be validated
     * 
     *            On return a list of errors will be returned, these errors should
     *            be looked up in a locale properties file for translation, even for
     *            english.
     * 
     */
    public static <T> List<Param> callTableValidation(T bean) throws Exception {
        List<Param> errors = new ArrayList<Param>();
        if (bean == null) {
            return errors;
        }

        Table table = getTableAnnotation(bean.getClass());
        if (table != null && table.validator() != null && !table.validator().trim().isEmpty()) {

            TableValidatorInterface api = (TableValidatorInterface) newInstance(table.validator());
            if (api != null) {
                errors.addAll(api.validateErrors(bean));
            }
        }
        List<Column> columns = getAllColumnAnnotations(bean.getClass());
        for (Column column : columns) {
            if (column == null) {
                continue;
            }

            // ***** TODO Perform predefined validation on the column.

            if (column.validator() != null && !column.validator().trim().isEmpty()) {

                ColumnValidatorInterface api = (ColumnValidatorInterface) newInstance(column.validator());
                if (api != null) {
                    errors.addAll(api.validateErrors(bean, column));
                }
            }
        }

        return errors;
    }

    public static <T> List<Param> callTableValidation(List<T> beans) throws Exception {
        List<Param> errors = new ArrayList<Param>();
        for (T bean : beans) {
            errors.addAll(callTableValidation(bean));
        }
        return errors;
    }

    /**
     * Copy all values from source object to destination object, where the names
     * match.
     * 
     * @param bean1
     * 
     * @param bean2
     * 
     */

    public static void copyFields(Object from, Object to) {
        try {
            propertyUtilsBean.copyProperties(to, from);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Copy all values from source object to destination object, where the names
     * match.
     * 
     * @param bean1
     * 
     * @param bean2
     * 
     */

    public static <T, S> void copy(T bean1, S bean2) {
        if (bean1 == null || bean2 == null) {
            return;
        }
        List<Column> columns = getColumnAnnotations(bean1.getClass());
        for (Column column : columns) {
            if (!hasPropertyName(bean2.getClass(), column.name())) {
                log.info("--- Property " + bean2.getClass().getName() + "." + column.name() + " does not exist.");
                continue;
            }
            setValue(bean2, column.name(), getValue(bean1, column.name()));
        }
    }

    /**
     * Copy all values from source object to destination object, where the names
     * match.
     * 
     * @param bean1
     * 
     * @param bean2
     * 
     */

    public static <T, S> void copy(T bean1, S bean2, List<Column> columns) {
        if (bean1 == null || bean2 == null) {
            return;
        }
        if (columns == null) {
            columns = getColumnAnnotations(bean1.getClass());
        }
        for (Column column : columns) {
            boolean hasField = false;
            try {
                hasField = (bean2.getClass().getDeclaredField(column.name()) != null);
            } catch (NoSuchFieldException e) {
                ; // intentionally blank
            }

            if (!hasField) {
                log.info("--- Property " + bean2.getClass().getName() + "." + column.name() + " does not exist.");
                continue;
            }
            String fieldname = (column.name() == null) ? column.field() : column.name();

            setValue(bean2, column.name(), get(bean1, fieldname));
        }
    }

    /**
     * Copy all values from source object to destination object, where the names
     * match.
     * 
     * @param bean1
     * 
     * @param bean2
     * 
     */

    public static <T, S> void copyProperties(T bean1, S bean2) throws Exception {
        if (bean1 == null || bean2 == null) {
            return;
        }

        Field[] fields = bean1.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.set(bean2, field.get(bean1));
        }
    }

    public static final <T> List<T> applyFilter(List<T> beans, Predicate predicate) {
        List<T> results = new ArrayList<T>();
        if (beans.size() > 0) {
            for (T bean : beans) {
                if (predicate.apply(bean)) {
                    results.add(bean);
                }
            }
        }
        return results;
    }

    public static final <T> List<T> applyGroupBy(List<T> beans, List<ColumnParam> columnParams) {
        Map<Object, T> results = new HashMap<Object, T>();
        if (beans.size() > 0) {
            boolean hasGroupBy = false;
            for (ColumnParam param : columnParams) {
                if (param.isGroupBy()) {
                    hasGroupBy = true;
                }
            }
            if (!hasGroupBy) {
                return beans;
            }

            for (T bean : beans) {
                List<Object> key = new ArrayList<Object>();
                for (ColumnParam param : columnParams) {
                    if (param.isGroupBy()) {
                        String name = param.getName().substring(param.getName().lastIndexOf('.') + 1);
                        Object value = getValue(bean, name);
                        key.add(value);
                    }
                }

                if (results.containsKey(key)) {
                    T bean1 = results.get(key);
                    setValue(bean1, "count", (Integer) (getValue(bean1, "count")) + 1);
                } else {
                    setValue(bean, "count", 1);
                    results.put(key, bean);
                }
            }
        }
        return new ArrayList<T>(results.values());
    }

    /**
     * Copy all values from source object to destination object, where the names
     * match.
     * 
     * @param bean1
     * 
     * @param bean2
     * 
     */

    public static <T> List<String> names(List<T> beans) {
        List<String> items = new ArrayList<String>();
        if (beans == null) {
            return items;
        }
        for (T bean : beans) {
            List<Column> columns = getColumnAnnotations(bean.getClass());
            for (Column column : columns) {
                if (!hasPropertyName(bean.getClass(), column.name())) {
                    log.info("--- Property " + bean.getClass().getName() + "." + column.name() + " does not exist.");
                    continue;
                }
                Object value = getValue(bean, column.name());
                if (value != null && !items.contains(column.name())) {
                    items.add(column.name());
                }
            }
        }
        return items;
    }

    /**
     * Given the table class and the column field name, return the java property
     * name.
     *
     * @param clazz
     *            java table model, with annotations.
     * @param fieldname
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * 
     * @return the name of the java property corresponding to the fieldname.
     * 
     */
    public static <T> String toPropertyName(Class<T> clazz, String fieldname) {
        Column column = getColumnAnnotation(clazz, fieldname);
        if (column == null) {
            return fieldname;
        }
        return (column.name() == null) ? column.field() : column.name();
    }

    /**
     * Given the table class and the column field name, return the java property
     * name.
     *
     * @param clazz
     *            java table model, with annotations.
     * @param fieldname
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * 
     * @return the name of the java property corresponding to the fieldname.
     * 
     */
    public static <T> String toPropertyName(Class<T> clazz, String tablename, String fieldname) {
        Column column = getColumnAnnotation(clazz, fieldname);
        if (column == null) {
            return fieldname;
        }
        return (column.name() == null) ? column.field() : column.name();
    }

    /**
     * 
     * @param bean
     * @param column
     * @return
     */
    public static <T> Class<?> toPropertyType(T bean, Column column) {
        String propertyName = null;
        try {
            if (column != null) {
                if (column.name() != null) {
                    propertyName = column.name();
                } else {
                    propertyName = column.field();
                }
                return PropertyUtils.getPropertyType(bean, propertyName);
            }
        } catch (Exception ex) {
            log.severe("toPropertyType failed for " + bean.getClass() + "." + propertyName + ":" + ex);
        }
        return null;
    }

    /**
     * Given the table class and the column field name, return the java property
     * name.
     *
     * @param clazz
     *            java table model, with annotations.
     * @param fieldname
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * 
     * @return the name of the java property corresponding to the fieldname.
     * 
     */
    public static <T> String toPropertyName(Column column) {
        return (column.name() == null || column.name().isEmpty()) ? column.field() : column.name();
    }

    public static <T> boolean hasPropertyName(Class<T> clazz, String fieldname) {
        Column column = getColumnAnnotation(clazz, fieldname);
        if (column != null) {
            return true;
        }
        try {
            return (clazz.getDeclaredField(fieldname) != null);
        } catch (NoSuchFieldException e) {
            ; // intentionally blank
        }
        return false;
    }

    public static <T> boolean hasColumn(Class<T> clazz, String fieldname) {
        Column column = getColumnAnnotation(clazz, fieldname);
        return (column != null);
    }

    /**
     * 
     * @param clazz
     *            java table model, with annotations.
     * @param fieldname
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the class of the java property corresponding to the fieldname.
     */
    public static <T> Class toPropertyClass(Class<T> clazz, String fieldname) {
        try {
            Column column = getColumnAnnotation(clazz, fieldname);
            if (column == null) {
                return clazz.getDeclaredField(fieldname).getType();
            }
            return clazz.getDeclaredField(column.name()).getType();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param clazz
     *            the name of the java property corresponding to the fieldname.
     * @param fieldname
     * @return the generic class of the java property corresponding to the
     *         fieldname, null if doesn't exists.
     */
    public static <T> Class toPropertyGenericClass(Class<T> clazz, String fieldname) {
        try {
            Column column = getColumnAnnotation(clazz, fieldname);
            Field field = clazz.getDeclaredField(column.name());
            Type type = field.getGenericType();
            if (!(type instanceof ParameterizedType)) {
                System.err.println("-field type: " + field.getType());
                return null;
            }
            ParameterizedType ptype = (ParameterizedType) type;
            return (Class) ptype.getActualTypeArguments()[0];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param bean
     *            java table model, with annotations.
     * @param fieldname
     * @return
     */
    public static <T> Object getValue(T bean, String fieldname) {
        if (bean == null || fieldname == null) {
            log.info("ERROR: getValue field or bean is null.");
            return null;
        }
        String propertyName = toPropertyName(bean.getClass(), fieldname);
        if (propertyName == null) {
            log.info("ERROR: getValue propertyName is null: " + bean.getClass() + "." + fieldname);
            return null;
        }
        return get(bean, propertyName);
    }

    /**
     * Given the bean and the fieldname, set the property value to the value
     * specified.
     * 
     * @param bean
     *            java table model, with annotations.
     * @param fieldname
     * @param value
     */
    public static <T> void setValue(T bean, String fieldname, Object value) {
        try {
            Class<?> fieldClazz = toPropertyClass(bean.getClass(), fieldname);
            if (fieldClazz == null) {
                System.err.println("DatabaseJDBC.read: " + bean.getClass().getName() + ", " + fieldname);
                return;
            }
            String propertyName = toPropertyName(bean.getClass(), fieldname);
            Object value1 = Converters.convert(fieldClazz, value);
            if (value1 != null) {
                set(bean, propertyName, value1);
            }
        } catch (Throwable ex) {
            log.fine("Conversion problem?: " + bean.getClass().getName() + "," + fieldname + "," + ex);
        }
    }

    /**
     * 
     * @param bean
     *            the java bean object (pojo).
     * @param fieldname
     *            the name of the field to set value of.
     * @return the string value of the field in the bean.
     */
    public static <T> String getString(T bean, String fieldname) {
        Object value = getValue(bean, fieldname);
        return (value == null) ? null : value.toString();
    }

    /**
     * 
     * @param bean
     *            the java bean object (pojo).
     * @param fieldname
     *            the name of the field to set value of.
     * @param fieldValue
     * @return the status of whether the field was set.
     */
    public static boolean set(Object bean, String fieldname, Object fieldValue) {
        try {
            propertyUtilsBean.setNestedProperty(bean, fieldname, fieldValue);
            return true;
        } catch (Throwable e) {
            log.fine("set failed for " + bean.getClass() + "." + fieldname + ":" + e);
        }
        return false;
    }

    /**
     * Given the bean object and the field name, retrieve the value of the
     * fieldname. Look at the super classes. Ignoring the java and javax classes.
     * 
     * @param bean
     *            the java bean object (pojo).
     * @param fieldname
     *            the name of the field to retrieve value of.
     * @return the value of the field in the bean at the fieldname, null value is
     *         actually null value of the field.
     * 
     * @note is it really good idea to recurse thru subclasses?
     * 
     * @throws IllegalStateException
     *             if the fieldname not found.
     */
    public static <E> E get(Object bean, String fieldname) {
        try {
            // TODO check for non-existing readMethod
            return (E) propertyUtilsBean.getNestedProperty(bean, fieldname);
        } catch (Throwable e) {
            log.fine("get failed for " + bean.getClass() + "." + fieldname + ": " + e);
            throw new IllegalStateException(e);
        }
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    /**
     * 
     * @param type
     * @return
     */

    public static List<Column> getAllColumnAnnotations(Class<?> type) {
        List<Column> columns = new ArrayList<Column>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            Field fields[] = c.getDeclaredFields();
            if (fields != null) {
                for (Field field : fields) {
                    Column cols[] = field.getAnnotationsByType(Column.class);
                    if (cols != null) {
                        for (Column col : cols) {
                            columns.add(col);
                        }
                    }
                }
            }
        }
        return columns;
    }

    public static Object convert(String converter , Class clazz, Object value) throws Exception {

        final String DELIMITERS = "\\s*[\\)\\(,]\\s*";
 
        String[] args = converter.split(DELIMITERS);

        int index = args[0].lastIndexOf('.');
        String classname = args[0].substring(0, index);
        String methodname = args[0].substring(index + 1);

        Class<?> c = Class.forName(classname);
        Class[] argTypes = new Class[] { Column.class, Object.class, String[].class };
        Method method = c.getDeclaredMethod(methodname, argTypes);

        String[] methodArgs = Arrays.copyOfRange(args, 1, args.length);
        return method.invoke(null, clazz, value, methodArgs);
    } 

    public static <T> T invoke(Class<T> clazz, String methodname, Object... args) throws Exception {

        if (args == null || args.length == 0) {
            return (T) clazz.getDeclaredMethod(methodname).invoke(null, args);
        }

        Class[] clazzes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            clazzes[i] = args[i].getClass();
        }

        Method method = clazz.getDeclaredMethod(methodname, clazzes);
        return (T) method.invoke(null, args);
    }

    public final static <T> List getBeansByForeignKeys(DatabaseInterface dao, Class<T> tableClazz, String key,
            String value) throws Exception {

        List beans = new ArrayList();

        T bean = dao.query(tableClazz, key, value);
        if (bean != null) {
            beans.add(bean);
        }

        List<ForeignKey> foreignKeys = getForeignKeyAnnotations(tableClazz);
        for (ForeignKey foreignKey : foreignKeys) {

            String[] localKeyName = foreignKey.localColumns();
            Object localValue = getValue(bean, localKeyName[0]);

            String foreignDatabase = foreignKey.foreignDatabase();
            String foreignTable = foreignKey.foreignTable();
            String[] foreignKeyName = foreignKey.foreignColumns();
            Class foreignClass = toTableClass(tableClazz.getPackage().getName(), foreignTable);

            Object child = dao.query(foreignClass, foreignKeyName[0], localValue);
            if (child != null) {
                beans.add(child);
            }
        }

        return beans;
    }

    public final static <T> List getBeansByForeignKeys(DatabaseInterface dao, Database database, Class<T> tableClazz,
            String key, String value) throws Exception {

        List beans = new ArrayList();

        T bean = dao.query(tableClazz, key, value);
        if (bean != null) {
            beans.add(bean);

            if (database != null && database.getTables() != null && database.getTables().size() > 0) {
                List<com.viper.database.model.ForeignKey> foreignKeys = database.getTables().get(0).getForeignKeys();
                for (com.viper.database.model.ForeignKey foreignKey : foreignKeys) {

                    String localDatabase = foreignKey.getLocalDatabase();
                    String localTable = foreignKey.getLocalTable();

                    List localBeans = getBeans(beans, localDatabase, localTable);

                    for (Object localBean : localBeans) {
                        String localKeyName = foreignKey.getForeignKeyReferences().get(0).getLocalColumn();
                        Object localValue = getValue(localBean, localKeyName);

                        String foreignDatabase = foreignKey.getForeignDatabase();
                        String foreignTable = foreignKey.getForeignTable();
                        String foreignKeyName = foreignKey.getForeignKeyReferences().get(0).getForeignColumn();
                        Class foreignClass = toTableClass(tableClazz.getPackage().getName(), foreignTable);

                        Object child = dao.query(foreignClass, foreignKeyName, localValue);
                        if (child != null) {
                            beans.add(child);
                        }
                    }
                }
            }
        }

        return beans;
    }

    private final static List getBeans(List beans, String databasename, String tablename) {
        List items = new ArrayList();
        for (Object bean : beans) {
            if (databasename.equalsIgnoreCase(DatabaseUtil.getDatabaseName(bean.getClass()))) {
                if (tablename.equalsIgnoreCase(DatabaseUtil.getTableName(bean.getClass()))) {
                    items.add(bean);
                }
            }
        }
        return items;
    }

    public final static String getValue(List<Param> params, String name) {
        for (Param param : params) {
            if (name.equalsIgnoreCase(param.getName())) {
                return param.getValue();
            }
        }
        return null;
    }

    public final static void putValue(List<Param> params, String name, String value) {
        for (Param param : params) {
            if (name.equalsIgnoreCase(param.getName())) {
                param.setValue(value);
                return;
            }
        }

        Param param = new Param();
        param.setName(name);
        param.setValue(value);
        params.add(param);
    }

    private static final boolean isEmpty(String str) {
        return (str == null || str.trim().isEmpty());
    }
}