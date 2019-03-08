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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

import com.viper.database.annotations.Column;
import com.viper.database.annotations.Table;
import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.DatabaseConnections;
import com.viper.database.rest.model.FormResponse;
import com.viper.database.rest.model.Plotly;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.junit.AbstractTestCase;
import com.viper.database.utils.junit.BenchmarkRule;
import com.viper.demo.beans.model.Bean;
import com.viper.demo.beans.model.Bean3;
import com.viper.demo.beans.model.enums.NamingField;
import com.viper.demo.unit.model.Employee;
import com.viper.demo.unit.model.Organization;
import com.viper.demo.unit.model.States;
import com.viper.demo.unit.model.Types;
import com.viper.demo.unit.model.User;
import com.viper.demo.unit.model.enums.MyColor;

public class TestDatabaseUtil extends AbstractTestCase {

    private static final String PACKAGE_NAME = "com.viper.demo.unit.model";
    private static final String PACKAGE_NAME1 = "com.viper.demo.beans.model";

    @Rule
    public TestRule benchmarkRule = new BenchmarkRule();

    @BeforeClass
    public static void initializeClass() throws Exception {

        Logger.getGlobal().setLevel(Level.INFO);
    }

    @Test
    public void testToPathname() throws Exception {
        String urlstr = "http://localhost:8080/viper/nitrohammer";
        String expected = "/viper/nitrohammer";

        String actual = DatabaseUtil.toPathname(urlstr);

        assertNotNull(getCallerMethodName() + " : did not get value ", actual);
        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testEscape() throws Exception {
        String str = "http://localhost:8080/viper/nitrohammer";
        String expected = "http:////localhost:8080///viper///nitrohammer";

        String actual = DatabaseUtil.escape(str, "/");

        assertNotNull(getCallerMethodName() + " : did not get value ", actual);
        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testPojoGetValue() throws Exception {

        String expected = "mysql";
        DatabaseConnection connection = new DatabaseConnection();
        connection.setName(expected);

        String value = (String) DatabaseUtil.getValue(connection, "name");

        assertNotNull(getCallerMethodName() + " : did not get value ", value);
        assertEquals(getCallerMethodName() + " : value do not match ", expected, value);
    }

    @Test
    public void testPojoSetValue() throws Exception {

        String expected = "mysql";
        DatabaseConnection connection = new DatabaseConnection();

        DatabaseUtil.setValue(connection, "name", expected);

        assertNotNull(getCallerMethodName() + " : did not get value ", connection.getName());
        assertEquals(getCallerMethodName() + " : value do not match ", expected, connection.getName());
    }

    @Test
    public void testGet() throws Exception {

        String expected = "mysql";
        DatabaseConnection connection = new DatabaseConnection();
        connection.setName(expected);

        String value = (String) DatabaseUtil.get(connection, "name");

        assertNotNull(getCallerMethodName() + " : did not get value ", value);
        assertEquals(getCallerMethodName() + " : value do not match ", expected, value);
    }

    @Test
    public void testSet() throws Exception {

        String expected = "mysql";
        DatabaseConnection connection = new DatabaseConnection();

        DatabaseUtil.set(connection, "name", expected);

        assertNotNull(getCallerMethodName() + " : did not get value ", connection.getName());
        assertEquals(getCallerMethodName() + " : value do not match ", expected, connection.getName());
    }

    @Test
    public void testPojoSet() throws Exception {

        String expected = "mysql";
        DatabaseConnection connection = new DatabaseConnection();

        DatabaseUtil.set(connection, "name", expected);

        assertNotNull(getCallerMethodName() + " : did not get value ", connection.getName());
        assertEquals(getCallerMethodName() + " : value do not match ", expected, connection.getName());
    }

    @Test
    public void testCopy() throws Exception {

        Bean connection1 = new Bean();
        connection1.setId(20);

        Bean connection2 = new Bean();

        DatabaseUtil.copy(connection1, connection2);

        assertEquals(getCallerMethodName() + " : value do not match ", connection1.getId(), connection2.getId());
    }

    @Test
    public void testCopyFields() throws Exception {

        Bean connection1 = new Bean();
        connection1.setId(20);

        Bean connection2 = new Bean();

        DatabaseUtil.copyFields(connection1, connection2);

        assertEquals(getCallerMethodName() + " : value do not match ", connection1.getId(), connection2.getId());
    }

    @Test
    public void testCopyProperties() throws Exception {

        DatabaseConnection connection1 = new DatabaseConnection();
        connection1.setName("mysql");

        DatabaseConnection connection2 = new DatabaseConnection();

        DatabaseUtil.copyProperties(connection1, connection2);

        assertEquals(getCallerMethodName() + " : value do not match ", connection1.getName(), connection2.getName());
    }

    @Test
    public void testToPropertyName() throws Exception {

        String expected = "name";
        DatabaseConnection connection = new DatabaseConnection();

        String propertyName = DatabaseUtil.toPropertyName(connection.getClass(), "name");

        assertNotNull(getCallerMethodName() + " : did not get value ", propertyName);
        assertEquals(getCallerMethodName() + " : value do not match ", expected, propertyName);
    }

    @Test
    public void testToList() throws Exception {

        List<Bean> beans = RandomBean.getRandomBeans(Bean.class, 10, 10);

        List<String> names = DatabaseUtil.toList(beans, "stringField");

        assertNotNull(getCallerMethodName() + " : did not get value ", names);
        assertEquals(getCallerMethodName() + " : value do not match ", 10, names.size());
    }

    @Test
    public void testToTableClass() throws Exception {

        Class clazz = DatabaseUtil.toTableClass(PACKAGE_NAME1, "bean");

        assertNotNull(getCallerMethodName() + " : did not get value ", clazz);
    }

    @Test
    public void testToTableClass2() throws Exception {

        Class clazz = DatabaseUtil.toTableClass(PACKAGE_NAME1 + ".Bean");

        assertNotNull(getCallerMethodName() + " : did not get value ", clazz);
    }

    @Test
    public void testGetPrimaryKeyValue() throws Exception {

        List<Bean> beans = RandomBean.getRandomBeans(Bean.class, 10, 10);

        Object key = DatabaseUtil.getPrimaryKeyValue(beans.get(0));

        assertNotNull(getCallerMethodName() + " : did not get value ", key);
        assertEquals(getCallerMethodName() + " : value do not match ", beans.get(0).getId(), key);
    }

    @Test
    public void testGetPrimaryKeyColumn() throws Exception {

        List<Column> columns = DatabaseUtil.getPrimaryKeyColumns(Bean.class);

        assertNotNull(getCallerMethodName() + " : did not get value ", columns);
        assertTrue(getCallerMethodName() + " : did not get value ", columns.size() > 0);
        assertEquals(getCallerMethodName() + " : value do not match ", "id", columns.get(0).name());
    }

    @Test
    public void testIsUniqueColumn() throws Exception {

        boolean uniqueColumnExists = DatabaseUtil.isUniqueColumn(Employee.class, "name");

        assertTrue(getCallerMethodName() + " : isUniqueColumn ", uniqueColumnExists);
    }

    @Test
    public void testGetColumnAnnotations() throws Exception {
        List<Column> items = DatabaseUtil.getColumnAnnotations(Bean.class);

        assertNotNull(getCallerMethodName() + " : tables not found ", items);
        assertTrue(getCallerMethodName() + " :  no tables not found ", items.size() > 0);
    }

    @Test
    public void testGetColumnNames() throws Exception {
        List<String> items = DatabaseUtil.getColumnNames(Bean.class);

        assertNotNull(getCallerMethodName() + " : tables not found ", items);
        assertTrue(getCallerMethodName() + " :  no tables not found ", items.size() > 0);
    }

    @Test
    public void testIndexOf() throws Exception {

        List<Bean> beans = RandomBean.getRandomBeans(Bean.class, 10, 10);

        String key = "id";
        int expected = 9;
        int value = beans.get(expected).getId();

        int actual = DatabaseUtil.indexOf(beans, key, value);

        assertEquals(getCallerMethodName() + " : test databaseExists ", expected, actual);
    }

    @Test
    public void testIsDatabaseExist() throws Exception {

        DatabaseInterface dao = DatabaseFactory.getInstance("test");
        boolean databaseExists = DatabaseUtil.isDatabaseExist(dao, "test");

        assertTrue(getCallerMethodName() + " : test databaseExists ", databaseExists);
    }

    @Test
    public void testFindConnection1() throws Exception {

        String name = "mysql";
        DatabaseConnections connections = DatabaseMapper.readConnections("res:/databases.xml");
        System.err.println("Databases.xml: connections size: " + connections.getConnections().size() + ":" + name);

        DatabaseConnection connection = DatabaseUtil.findOneItem(connections.getConnections(), "name", name);

        assertNotNull(getCallerMethodName() + " : connections were not found ", connections.getConnections());
        assertTrue(getCallerMethodName() + " : not enought connections were not found ", connections.getConnections().size() > 0);
        assertNotNull(getCallerMethodName() + " : connection was not found ", connection);
    }

    @Test
    public void testFindConnection2() throws Exception {

        String name = "test";
        DatabaseConnections connections = DatabaseMapper.readConnections("res:/databases.xml");
        System.err.println("Databases.xml: connections size: " + connections.getConnections().size() + ":" + name);

        assertNotNull(getCallerMethodName() + " : connections were not found ", connections.getConnections());
        assertTrue(getCallerMethodName() + " : not enought connections were not found ", connections.getConnections().size() > 0);

        DatabaseConnection connection = DatabaseUtil.findOneItem(connections.getConnections(), "name", name);

        assertNotNull(getCallerMethodName() + " : connection was not found ", connection);
    }

    @Test
    public void testFindMany() throws Exception {

        String name = "test";
        DatabaseConnections connections = DatabaseMapper.readConnections("res:/databases.xml");
        assertNotNull(getCallerMethodName() + " : connections were not found ", connections.getConnections());
        assertTrue(getCallerMethodName() + " : not enought connections were not found " + connections.getConnections().size(),
                connections.getConnections().size() > 0);

        List<String> list = new ArrayList<String>();
        list.add("mysql");

        List<DatabaseConnection> items = DatabaseUtil.findManyItems(connections.getConnections(), "vendor", list);

        assertNotNull(getCallerMethodName() + " : connections was not found ", items);
        assertTrue(getCallerMethodName() + " : connections was not found ", items.size() > 0);
    }

    @Test
    public void testListDatabaseTableClasses() throws Exception {
        List<Class<?>> items = DatabaseUtil.listDatabaseTableClasses(PACKAGE_NAME, null);

        assertNotNull(getCallerMethodName() + " : tables not found ", items);
        assertTrue(getCallerMethodName() + " :  no tables not found ", items.size() > 0);
    }

    @Test
    public void testListTableClasses() throws Exception {
        List<Class<?>> items = DatabaseUtil.listTableClasses(PACKAGE_NAME, "test");

        assertNotNull(getCallerMethodName() + " : tables not found ", items);
        assertTrue(getCallerMethodName() + " :  no tables not found ", items.size() > 0);
    }

    @Test
    public void testGetAssignedColumn() throws Exception {

        Column assignedColumn = DatabaseUtil.getAssignedColumn(States.class);
        assertNotNull(getCallerMethodName() + " : tables not found ", assignedColumn);
    }

    @Test
    public void testGetClasses() throws Exception {
        List<Class<?>> items = DatabaseUtil.getClasses(PACKAGE_NAME);

        assertNotNull(getCallerMethodName() + " : tables not found ", items);
        assertTrue(getCallerMethodName() + " :  no tables not found ", items.size() > 0);
    }

    @Test
    public void testGetClasses2() throws Exception {

        int expected = 100;
        List<String> packageNames = new ArrayList<String>();
        packageNames.add("com.viper.demo.unit.model");
        packageNames.add("com.viper.demo.beans.model");

        List<Class<?>> clazzes = DatabaseUtil.getClasses(packageNames);

        assertNotNull(getCallerMethodName() + " : no classes  found:", clazzes);
        assertEquals(getCallerMethodName() + " : wrong number of classes ", expected, clazzes.size());
    }

    @Test
    public void testGetTableClasses() throws Exception {
        Class item = DatabaseUtil.toTableClass(PACKAGE_NAME, "PEOPLE");

        assertNotNull(getCallerMethodName() + " : tables not found ", item);
    }

    @Test
    public void testGetClassesWithAnnotation() throws Exception {
        List<Class<?>> items = DatabaseUtil.getClassesWithAnnotation(PACKAGE_NAME, Table.class);

        assertNotNull(getCallerMethodName() + " : classes not found ", items);
        assertTrue(getCallerMethodName() + " :  no classes not found ", items.size() > 0);
    }

    @Test
    public void testGetNestedColumnAnnotations() throws Exception {
        Map<String, Column> items = DatabaseUtil.getNestedColumnAnnotations(Bean3.class);

        for (String key : items.keySet()) {
            System.err.println("testGetNestedColumnAnnotations: " + key + "," + items.get(key).name());
        }

        assertNotNull(getCallerMethodName() + " : classes not found ", items);
        assertTrue(getCallerMethodName() + " :  no classes not found ", items.size() > 0);
    }

    @Test
    public void testGetNaturalKeyValues() throws Exception {

        String expected = "";
        String actual = DatabaseUtil.getNaturalKeyValues(new Organization());

        assertNotNull(getCallerMethodName() + " : classes not found ", actual);
        assertEquals(getCallerMethodName() + " :  no classes not found ", expected, actual);
    }

    @Test
    public void testHasProprtyName() throws Exception {
        assertTrue(getCallerMethodName() + " : connections were not found ",
                DatabaseUtil.hasPropertyName(DatabaseConnection.class, "name"));
    }

    @Test
    public void testIsMatch() throws Exception {

        DatabaseConnection c1 = new DatabaseConnection();
        c1.setName("test");

        boolean isMatch = DatabaseUtil.isMatch(c1, new Object[] { "name", "test" });

        assertEquals(getCallerMethodName() + " :not found connection name of 'test' ", true, isMatch);
    }

    @Test
    public void testIsMatch2() throws Exception {

        Bean bean = RandomBean.getRandomBean(Bean.class, 10);

        boolean isMatch = DatabaseUtil.isMatch(bean, new Object[] { "id", bean.getId() });

        assertEquals(getCallerMethodName() + " :found bean of id " + bean.getId(), true, isMatch);
    }

    @Test
    public void testIsMatch3() throws Exception {

        Bean bean = RandomBean.getRandomBean(Bean.class, 10);

        boolean isMatch = DatabaseUtil.isMatch(bean, new Object[] { "id", bean.getId() + 1 });

        assertEquals(getCallerMethodName() + " :found bean of id " + bean.getId(), false, isMatch);
    }

    @Test
    public void testIsMatch4() throws Exception {

        Bean bean = RandomBean.getRandomBean(Bean.class, 10);

        boolean isMatch = DatabaseUtil.isMatch(bean, new Object[] { "id1", bean.getId() });

        assertEquals(getCallerMethodName() + " :found bean of id " + bean.getId(), false, isMatch);
    }

    @Test
    public void testIsMatch5() throws Exception {

        Bean bean = RandomBean.getRandomBean(Bean.class, 10);

        boolean isMatch = DatabaseUtil.isMatch(bean, new Object[] { "stringField", null });

        assertEquals(getCallerMethodName() + " :found bean of id " + bean.getStringField(), false, isMatch);
    }

    @Test
    public void testIsMatch6() throws Exception {

        Bean bean = RandomBean.getRandomBean(Bean.class, 10);
        bean.setStringField(null);

        boolean isMatch = DatabaseUtil.isMatch(bean, new Object[] { "stringField", null });

        assertEquals(getCallerMethodName() + " :found bean of id " + bean.getStringField(), true, isMatch);
    }

    @Test
    public void testGetValue() throws Exception {

        User user = new User();
        List<String> expected = new ArrayList<String>();
        expected.add("John");
        expected.add("Bob");
        expected.add("Sally");
        user.setFriends(expected);

        Object value = DatabaseUtil.getValue(user, "friends");

        assertNotNull(getCallerMethodName() + " : did not get friends value ", value);
        assertTrue(getCallerMethodName() + " : friends not instance of List<String> ", (value instanceof List));
        assertEquals(getCallerMethodName() + " : friends value do not match ", expected, (List<String>) value);
    }

    @Test
    public void testSetValue() throws Exception {

        User user = new User();
        String str = "[\"John\",\"Bob\",\"Sally\"]";

        DatabaseUtil.setValue(user, "friends", str);

        assertNotNull(getCallerMethodName() + " : did not get friends value ", user.getFriends());
        assertEquals(getCallerMethodName() + " : did not get friends value size ", 3, user.getFriends().size());
        assertEquals(getCallerMethodName() + " : did not get friends value[0] ", "John", user.getFriends().get(0));
        assertEquals(getCallerMethodName() + " : did not get friends value[1] ", "Bob", user.getFriends().get(1));
        assertEquals(getCallerMethodName() + " : did not get friends value[2] ", "Sally", user.getFriends().get(2));
    }

    @Test
    public void testPropertyGenericClass() throws Exception {

        List<String> friends = new ArrayList<String>();
        friends.add("John");
        friends.add("Bob");
        friends.add("Sally");

        User user = new User();
        user.setFriends(friends);

        Class clazz = DatabaseUtil.toPropertyGenericClass(User.class, "friends");

        assertNotNull(getCallerMethodName() + " : did not get friends class ", clazz);
        assertEquals(getCallerMethodName() + " : friends class do not match ", String.class, clazz);
    }

    @Test
    public void testPropertyGenericType() throws Exception {

        List<String> friends = new ArrayList<String>();
        friends.add("John");
        friends.add("Bob");
        friends.add("Sally");

        User user = new User();
        user.setFriends(friends);

        Column column = DatabaseUtil.getColumnAnnotation(User.class, "friends");
        assertNotNull(getCallerMethodName() + " : did not get friends class in user:", column);
        assertEquals(getCallerMethodName() + " : did not get friends column:", "friends", column.name());

        Field field = User.class.getDeclaredField(column.name());
        assertNotNull(getCallerMethodName() + " : did not get friends field in user:", field);
        assertEquals(getCallerMethodName() + " : did not get friends field:", "friends", field.getName());

        Type type = field.getGenericType();
        System.err.println("field name: " + field.getName());
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            System.err.println("-raw type:" + ptype.getRawType());
            System.err.println("-type arg: " + ptype.getActualTypeArguments()[0]);
        } else {
            System.err.println("-field type: " + field.getType());
        }
    }

    @Test
    public void testGetValueInt() throws Exception {

        Types item = new Types();
        item.setId(1020304);

        Integer actual = (Integer) DatabaseUtil.getValue(item, "id");
        Integer expected = 1020304;

        assertNotNull(getCallerMethodName() + " : did not get value ", actual);
        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueInt() throws Exception {

        Types item = new Types();
        Integer expected = 1020304;

        DatabaseUtil.setValue(item, "id", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, (Integer) item.getId());
    }

    @Test
    public void testGetValueChar() throws Exception {

        char expected = 'A';

        Types item = new Types();
        item.setCharType(expected);

        char actual = (Character) DatabaseUtil.getValue(item, "CHAR_TYPE");

        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueChar() throws Exception {

        Types item = new Types();
        char expected = 'A';

        DatabaseUtil.setValue(item, "CHAR_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getCharType());
    }

    @Test
    public void testGetValueCharacter() throws Exception {

        Character expected = 'A';

        Types item = new Types();
        item.setCharType(expected);

        Character actual = (Character) DatabaseUtil.getValue(item, "CHARACTER_TYPE");

        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueCharacter() throws Exception {

        Types item = new Types();
        Character expected = 'A';

        DatabaseUtil.setValue(item, "CHARACTER_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getCharacterType());
    }

    @Test
    public void testGetValueVARCHAR_TYPE() throws Exception {

        String expected = "ABC";

        Types item = new Types();
        item.setVarcharType(expected);

        String actual = (String) DatabaseUtil.getValue(item, "VARCHAR_TYPE");
        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueVARCHAR_TYPE() throws Exception {

        Types item = new Types();
        String expected = "ABC";

        DatabaseUtil.setValue(item, "VARCHAR_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getVarcharType());
    }

    @Test
    public void testGetValuevarcharType() throws Exception {

        String expected = "ABC";

        Types item = new Types();
        item.setVarcharType(expected);

        String actual = (String) DatabaseUtil.getValue(item, "varcharType");
        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValuevarcharType() throws Exception {

        Types item = new Types();
        String expected = "ABC";

        DatabaseUtil.setValue(item, "varcharType", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getVarcharType());
    }

    @Test
    public void testGetValueIntType() throws Exception {

        Integer expected = 102030405;

        Types item = new Types();
        item.setIntType(expected);

        Integer actual = (Integer) DatabaseUtil.getValue(item, "int_TYPE");
        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueIntType() throws Exception {

        Types item = new Types();
        Integer expected = 102030405;

        DatabaseUtil.setValue(item, "int_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, (Integer) item.getIntType());
    }

    @Test
    public void testGetValueDateType() throws Exception {

        java.sql.Date expected = new java.sql.Date(System.currentTimeMillis());

        Types item = new Types();
        item.setDateType(expected);

        java.sql.Date actual = (java.sql.Date) DatabaseUtil.getValue(item, "DATE_TYPE");
        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueDateType() throws Exception {

        Types item = new Types();
        java.util.Date expected = new java.util.Date(System.currentTimeMillis());

        DatabaseUtil.setValue(item, "DATE_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getDateType());
    }

    @Test
    public void testGetValueTimeType() throws Exception {

        java.sql.Time expected = new java.sql.Time(System.currentTimeMillis());

        Types item = new Types();
        item.setTimeType(expected);

        java.sql.Time actual = (java.sql.Time) DatabaseUtil.getValue(item, "TIME_TYPE");
        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueTimeType() throws Exception {

        Types item = new Types();
        java.util.Date expected = new java.util.Date(System.currentTimeMillis());

        DatabaseUtil.setValue(item, "TIME_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getTimeType());
    }

    @Test
    public void testGetValueTimestampType() throws Exception {

        java.sql.Timestamp expected = new java.sql.Timestamp(System.currentTimeMillis());

        Types item = new Types();
        item.setTimestampType(expected);

        java.sql.Timestamp actual = (java.sql.Timestamp) DatabaseUtil.getValue(item, "TIMESTAMP_TYPE");
        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueTimestampType() throws Exception {

        Types item = new Types();
        java.util.Date expected = new java.util.Date(System.currentTimeMillis());

        DatabaseUtil.setValue(item, "TIMESTAMP_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getTimestampType());
    }

    @Test
    public void testGetValueByte() throws Exception {

        Byte expected = 'A';

        Types item = new Types();
        item.setTinyintType(expected);

        Byte actual = (Byte) DatabaseUtil.getValue(item, "TINYINT_TYPE");

        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueByte() throws Exception {

        Types item = new Types();
        Byte expected = 'A';

        DatabaseUtil.setValue(item, "TINYINT_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, (Byte) item.getTinyintType());
    }

    @Test
    public void testGetValueBigInteger() throws Exception {

        java.math.BigInteger expected = new java.math.BigInteger("123456");

        Types item = new Types();
        item.setBigintType(expected);

        java.math.BigInteger actual = (java.math.BigInteger) DatabaseUtil.getValue(item, "BIGINT_TYPE");

        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueBigInteger() throws Exception {

        Types item = new Types();
        java.math.BigInteger expected = new java.math.BigInteger("123456");

        DatabaseUtil.setValue(item, "BIGINT_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getBigintType());
    }

    @Test
    public void testGetValueFloat() throws Exception {

        Float expected = 1234.679F;

        Types item = new Types();
        item.setFloatType(expected);

        Float actual = (Float) DatabaseUtil.getValue(item, "FLOAT_TYPE");

        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueFloat() throws Exception {

        Types item = new Types();
        Float expected = 1234.679F;

        DatabaseUtil.setValue(item, "FLOAT_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getFloatType());
    }

    @Test
    public void testGetValueDouble() throws Exception {

        Double expected = 1234.679;

        Types item = new Types();
        item.setDoubleType(expected);

        Double actual = (Double) DatabaseUtil.getValue(item, "DOUBLE_TYPE");

        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueDouble() throws Exception {

        Types item = new Types();
        Double expected = 1234.679;

        DatabaseUtil.setValue(item, "DOUBLE_TYPE", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getDoubleType());
    }

    /*
     * private java.math.BigDecimal decimalType;
     * 
     * @Column(field = "DECIMAL_TYPE", name = "decimalType", type = "java.math.BigDecimal") public
     * java.math.BigDecimal getDecimalType() {
     * 
     * return decimalType; }
     * 
     * private byte[] binaryType;
     * 
     * @Column(field = "BINARY_TYPE", name = "binaryType", type = "byte[]") public byte[]
     * getBinaryType() {
     * 
     * return binaryType; }
     * 
     * private int[] varbinaryType;
     * 
     * @Column(field = "VARBINARY_TYPE", name = "varbinaryType", type = "int[]") public int[]
     * getVarbinaryType() {
     * 
     * return varbinaryType; }
     * 
     * private long[] longvarbinaryType;
     * 
     * @Column(field = "LONGVARBINARY_TYPE", name = "longvarbinaryType", type = "long[]") public
     * long[] getLongvarbinaryType() {
     * 
     * return longvarbinaryType; }
     */

    @Test
    public void testGetValueEnum() throws Exception {

        MyColor expected = MyColor.GREEN;

        Types item = new Types();
        // TODO item.setEnumType(expected);

        String actual = DatabaseUtil.getString(item, "enumType");

        assertEquals(getCallerMethodName() + " : value do not match ", expected, actual);
    }

    @Test
    public void testSetValueEnum() throws Exception {

        MyColor expected = MyColor.RED;

        Types item = new Types();
        DatabaseUtil.setValue(item, "enumType", expected);

        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getEnumType());
    }

    @Test
    public void testEnumValueOf() throws Exception {

        MyColor expected = MyColor.RED;

        MyColor actual = MyColor.RED;
        MyColor actual1 = MyColor.RED;

        assertEquals(getCallerMethodName() + " : values not matched ", expected, actual);
    }

    @Test
    public void testStaticInvoke() throws Exception {

        Object expected = NamingField.A_1;

        NamingField actual1 = NamingField.valueOf("A.1");
        NamingField actual2 = NamingField.valueOf("A.1");

        assertEquals(getCallerMethodName() + " : values not matched ", expected, actual1);
        assertEquals(getCallerMethodName() + " : values not matched ", expected, actual2);
    }

    @Test
    public void testGetValueNamedEnum() throws Exception {

        Object expected = NamingField.A_1;

        Bean item = new Bean();
        // TODO item.setNamingField(expected);

        NamingField actual = (NamingField) DatabaseUtil.getValue(item, "namingField");

        assertEquals(getCallerMethodName() + " : S/B A.1 ", "A.1", actual.toString());
        assertEquals(getCallerMethodName() + " : value do not match ", expected.toString(), actual.toString());
    }

    @Test
    public void testSetValueNamedEnum() throws Exception {

        Object expected = NamingField.B_2;

        Bean item = new Bean();
        DatabaseUtil.setValue(item, "namingField", expected);

        assertEquals(getCallerMethodName() + " : S/B B.2 ", "B.2", item.getNamingField().toString());
        assertEquals(getCallerMethodName() + " : values not matched ", expected, item.getNamingField());
    }

    @Test
    public void testNamedEnumValueOf() throws Exception {

        Object expected = NamingField.C_3;

        Object actual = NamingField.C_3;
        Object actual1 = NamingField.C_3;

        assertEquals(getCallerMethodName() + " : S/B C.3 ", "C.3", actual.toString());
        assertEquals(getCallerMethodName() + " : values not matched ", expected, actual);
    }

    @Test
    public void testClassesForPackage() throws Exception {

        List<Class<?>> clazzes = DatabaseUtil.getClasses("com.viper.database.annotations");

        for (Class<?> clazz : clazzes) {
            System.err.println("testClassesForPackage clazz=" + clazz);
        }

        assertEquals(getCallerMethodName() + " : values not matched ", 4, clazzes.size());
    }

    @Test
    public void testGetString() throws Exception {

        Bean bean = RandomBean.getRandomBean(Bean.class, 10);
        bean.setStringField("help");

        String item = DatabaseUtil.getString(bean, "stringField");

        assertNotNull(getCallerMethodName() + " : item is null ", item);
        assertEquals(getCallerMethodName() + " : values not matched ", "help", item);
    }

    @Test
    public void testGetValueStringList() throws Exception {

        Bean bean = RandomBean.getRandomBean(Bean.class, 123344556);
        List<Object> items = (List<Object>) DatabaseUtil.getValue(bean, "stringList");

        assertNotNull(getCallerMethodName() + " : items is null ", items);
    }

    @Test
    public void testGetValueUser() throws Exception {

        User bean = RandomBean.getRandomBean(User.class, 123344556);
        List<Object> items = (List<Object>) DatabaseUtil.getValue(bean, "friends");

        assertNotNull(getCallerMethodName() + " : items is null ", items);
    }

    @Test
    public void testGetAllColumnAnnotationsPlotly() throws Exception {

        List<Column> items = DatabaseUtil.getAllColumnAnnotations(Plotly.class);

        assertNotNull(getCallerMethodName() + " : items is null ", items);
        assertEquals(getCallerMethodName() + " : all annotations not found ", 2, items.size());
    }

    @Test
    public void testGetAllColumnAnnotationsFormResponse() throws Exception {

        List<Column> items = DatabaseUtil.getAllColumnAnnotations(FormResponse.class);

        assertNotNull(getCallerMethodName() + " : items is null ", items);
        assertEquals(getCallerMethodName() + " : all annotations not found ", 5, items.size());
    }

    @Test
    public void testReplaceTokens() throws Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("TEMPLATE", "test");

        String contents = "foo #{TEMPLATE} bar";
        String expected = "foo test bar";
        String results = DatabaseUtil.replaceTokens(contents, replacements);

        assertNotNull(getCallerMethodName() + " : results is null ", results);
        assertEquals(getCallerMethodName() + " : results not equals ", expected, results);
    }

    @Test
    public void testReplaceTokens1() throws Exception {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("TEMPLATE", "test");

        String contents = "foo #{TEMPLATE1} bar";
        String expected = "foo #{TEMPLATE1} bar";
        String results = DatabaseUtil.replaceTokens(contents, replacements);

        assertNotNull(getCallerMethodName() + " : results is null ", results);
        assertEquals(getCallerMethodName() + " : results not equals ", expected, results);
    }

    @Test
    public void testReplaceRegex() throws Exception {

        String inputSQL = " select *, CONCAT(CO.ENTRYDATE, ' ', CO.ENTRYTIME) AS ENTRYDATETIME,  CONCAT(AFWT.PROVDATE, ' ', AFWT.PROVTIME) AS PROVDATETIME,  CONCAT(AFWT.DEPROVDATE, ' ', AFWT.DEPROVTIME) AS DEPROVDATETIME,  CONCAT(SURVEILLANCE.STARTDATE, ' ', SURVEILLANCE.STARTTIME) AS STARTDATETIME, CONCAT(SURVEILLANCE.STOPDATE, ' ', SURVEILLANCE.STOPTIME) AS STOPDATETIME, CONCAT(CO.RCVDATE, ' ', CO.RCVTIME) AS RCVDATETIME, IF (COGRP.NAME IS NULL or COGRP.name = '', CO.COID, COGRP.name) as COURT_ORDER_NAME FROM XCDB.AFWT as AFWT LEFT JOIN XCDB.SURVEILLANCE as SURVEILLANCE on (SURVEILLANCE.COID = AFWT.COID) LEFT JOIN XCDB.TARGET as TARGET on (TARGET.COID = AFWT.COID and TARGET.TID = AFWT.TID) LEFT JOIN XCDB.CO as CO on (CO.COID = AFWT.COID) LEFT JOIN XCDB.COGRP as COGRP on (COGRP.GID = CO.GID)";
        String expected = "SELECT COUNT(*) FROM XCDB.AFWT as AFWT LEFT JOIN XCDB.SURVEILLANCE as SURVEILLANCE on (SURVEILLANCE.COID = AFWT.COID) LEFT JOIN XCDB.TARGET as TARGET on (TARGET.COID = AFWT.COID and TARGET.TID = AFWT.TID) LEFT JOIN XCDB.CO as CO on (CO.COID = AFWT.COID) LEFT JOIN XCDB.COGRP as COGRP on (COGRP.GID = CO.GID)";

        String results = inputSQL.trim().replaceAll("(?i)SELECT (.*) FROM ", "SELECT COUNT(*) FROM ");

        assertNotNull(getCallerMethodName() + " : results is null ", results);
        assertEquals(getCallerMethodName() + " : results not equals ", expected, results);
    }

}
