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

import java.util.List;

/**
 * The following interface defines all routines in the database (dao) object. To
 * create a dao interface call the database factory.
 *
 */

public interface DatabaseInterface {

	// TODO get rid of these paging variables, build it into the queries
	public final static String PAGENO_KEY = "__pageno__";
	public final static String PAGESIZE_KEY = "__pagesize__";

	/**
	 * Release all resources for this database (dao) object, including memory,
	 * caching, and database connections.
	 * 
	 */
	public void release();

	/**
	 * Given the database name and table name, return the number of rows in the
	 * table.
	 * 
	 * @param databasename
	 *            the name of the database, underlying database can be schema,
	 *            catalog or filename based on implementation.
	 * @param tablename
	 *            the name of the table, for which the number of rows is desired.
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @return the number of rows in the specified table.
	 * 
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc.
	 */
	public <T> long size(Class<T> clazz) throws Exception;

	/**
	 * List all databases by name, for this dao connection.
	 * 
	 * @return the list of database names, empty list means no databases, or error
	 *         condition. Note Will NOT return null value.
	 */
	public List<String> listDatabases();

	/**
	 * List all tables by name, for this dao connection.
	 * 
	 * @param databaseName
	 *            the name of the database, underlying database can be schema,
	 *            catalog or filename based on implementation.
	 * @return the list of table names, empty list means no tables, or error
	 *         condition. Note Will NOT return null value.
	 */

	public List<String> listTables(String databaseName);

	/**
	 * List all columns by name, for the specified database and table names.
	 * 
	 * @param databasename
	 *            * the name of the database, underlying database can be schema,
	 *            catalog or filename based on implementation.
	 * @param tablename
	 *            the name of the table, for column names are desired.
	 * @return the list of column names, empty list means no columns, or error
	 *         condition. Note Will NOT return null value.
	 */

	public <T> List<String> listColumns(Class<T> clazz);

	/**
	 * List all columns by name, for the specified database and table names.
	 * 
	 * @param databasename
	 *            * the name of the database, underlying database can be schema,
	 *            catalog or filename based on implementation.
	 * @param tablename
	 *            the name of the table, for column names are desired.
	 * @return the list of column names, empty list means no columns, or error
	 *         condition. Note Will NOT return null value.
	 */

	public <T> boolean hasChanged(Class<T> clazz);

	/**
	 * Given the java database model class with annotations, create a table to match
	 * the java bean class. If the table is create the columns will be checked for
	 * missing columns which will automatically be added.
	 * 
	 * @param packagename
	 *            the name of the package containing the java models, which are to
	 *            all be created.
	 * 
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @throws Exception
	 *             database/table(s) were not created or altered.
	 * 
	 *             Note checks for column name matches does not alter if column data
	 *             type varies at this time.
	 */
	public <T> void createDatabase(String packagename) throws Exception;

	/**
	 * Given the java database model class with annotations, create a table to match
	 * the java bean class. If the table is create the columns will be checked for
	 * missing columns which will automatically be added.
	 * 
	 * @param tableClass
	 *            java database model, see annotations
	 * 
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @throws Exception
	 *             table was not created or altered.
	 * 
	 *             Note checks for column name matches does not alter if column data
	 *             type varies at this time.
	 */
	public <T> void create(Class<T> tableClass) throws Exception;

	/**
	 * Given the java table model, and the vararg of key value pairs, query the
	 * database, return match java bean object. Must be a single match.The vararg
	 * pairs represent "and" operations in the underlying SQL where clause.
	 * 
	 * @param tableClass
	 *            java table model, with annotations.
	 * @param keyValue
	 *            set of key value pairs.
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @return the value of the java bean found, if multiple matches no bean will be
	 *         returned. A null value indicates NO match, or a multiple match.
	 * @throws Exception
	 *             unable to query for the object, there was a failure other then
	 *             the object was simple not there. Note The following code will
	 *             return the employee whose name is "John Brown" and works in
	 *             department 257.
	 * 
	 *             <pre>
	 * {@code
	 *  DatabaseInterface dao = DatabaseFactory.getInstance(dbc);
	 *  Employee emp = dao.query(Employee.class, "name", "John Brown", "dept", 257);
	 * }
	 *             </pre>
	 */
	public <T> T query(Class<T> tableClass, Object... keyValue) throws Exception;

	/**
	 * Given the java table model, return all java beans in the corresponding
	 * database table. The annotations in the java table model, indicate database
	 * name and table name.
	 * 
	 * @param tableClass
	 *            java table model, with annotations.
	 * @param <T>
	 *            the class of the POJO database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @return list of java objects in the underlying table.
	 *
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc.
	 * 
	 *             Note the returned list will NOT be null, will be valid list of
	 *             zero to N length.
	 */

	public <T> List<T> queryAll(Class<T> tableClass) throws Exception;

	/**
	 * Given the java table model, and the vararg of key value pairs, query the
	 * database, return matching list of java bean objects. Can be single or
	 * multiple matches. The vararg pairs represent "and" operations in the
	 * underlying SQL where clause.
	 * 
	 * @param tableClass
	 *            java table model, with annotations.
	 * @param keyValue
	 *            set of key value pairs. 1. key name __pageno__ is the number of
	 *            page starting at zero 2. key_name __pagesize__ is the size of the
	 *            page in records.
	 * 
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @return the list of the java beans found, the list will always be NON-NULL,
	 *         but can be of zero length.
	 * 
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc. Note The following code will return the employees who work
	 *             in department 257, and are full time employees.
	 * 
	 *             <pre>
	 * {@code
	 *  DatabaseInterface dao = DatabaseFactory.getInstance(dbc);
	 *  Employee emp = dao.query(Employee.class, "dept", 257, "fulltime", true);
	 * }
	 *             </pre>
	 */
	public <T> List<T> queryList(Class<T> tableClass, Object... keyValue) throws Exception;

	/**
	 * Given the java table model, the pageno, the number of rows in a page, and the
	 * vararg of key value pairs, query the database, return matching list of java
	 * bean objects. Can be single or multiple matches. The vararg pairs represent
	 * "and" operations in the underlying SQL where clause.
	 * 
	 * @param tableClass
	 *            java table model, with annotations.
	 * @param filter
	 *            the filter for the rows, all rows read and then filter is applied,
	 *            can be slow.
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @return the list of the java beans found, the list will always be NON-NULL,
	 *         but can be of zero length.
	 * 
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc. Note The following code will return a page of up to 20
	 *             employees who are full time employees.
	 * 
	 *             <pre>
	 * {@code
	 *  DatabaseInterface dao = DatabaseFactory.getInstance(dbc);
	 *  List<Employee> emps = dao.queryPage(Employee.class, new Predicate<Employee>() {
	 *  	&#64;Override
	 *  	public boolean apply(Employee e) {
	 *  		return e.getWeeklyHours() >= 36;
	 *  	}
	 * }
	 *             </pre>
	 */
	public <T> List<T> queryList(Class<T> tableClass, Predicate<T> filter) throws Exception;

	/**
	 * Given the java table model bean, insert or update the bean into the database.
	 * If the primary key is specified in the model, and the value of the primary
	 * key is not null or not zero, then update the database using the primary key
	 * value. If the primary key is null or zero, then insert the bean into the
	 * database. If the primary key value is auto increment, then the bean's primary
	 * key value will be populated on return.
	 * 
	 * @param bean
	 *            java table model bean, with annotations.
	 * 
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc.
	 * 
	 */
	public <T> T update(T bean) throws Exception;

	/**
	 * Given the java table model bean, insert or update the bean into the database.
	 * If the primary key is specified in the model, and the value of the primary
	 * key is not null or not zero, then update the database using the primary key
	 * value. If the primary key is null or zero, then insert the bean into the
	 * database. If the primary key value is auto increment, then the bean's primary
	 * key value will be populated on return.
	 * 
	 * @param bean
	 *            java table model bean, with annotations.
	 * 
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc.
	 * 
	 */
	public <T> T insert(T bean) throws Exception;

	/**
	 * Given the java table model beans, insert or update the beans into the
	 * database. If the primary key is specified in the model, and the value of the
	 * primary key is not null or not zero, then update the database using the
	 * primary key value. If the primary key is null or zero, then insert the bean
	 * into the database. If the primary key value is auto increment, then all of
	 * the bean's primary key value will be populated on return.
	 * 
	 * @param beans
	 *            java table model beans, with annotations.
	 * 
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc.
	 * 
	 */
	public <T> void insertAll(List<T> beans) throws Exception;

	/**
	 * Given the java table model, and the vararg of key value pairs, query the
	 * database, delete any matching java bean objects, physically from table.
	 * 
	 * @param tableClass
	 *            java table model, with annotations.
	 * @param keyValue
	 *            set of key value pairs.
	 * 
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc. Note The following code will delete the employee(s) whose
	 *             name is "John Brown" and works in department 257.
	 * 
	 *             <pre>
	 * {@code
	 *  DatabaseInterface dao = DatabaseFactory.getInstance(dbc);
	 *  Employee emp = dao.query(Employee.class, "name", "John Brown", "dept", 257);
	 * }
	 *             </pre>
	 */
	public <T> void delete(Class<T> tableClass, Object... keyValue) throws Exception;

	/**
	 * Given the java table model bean, delete the matching java bean objects,
	 * physically from table. The bean will be deleted using primary key, if
	 * available, otherwise deletion will fail.
	 * 
	 * @param bean
	 *            java bean of table model class, with annotations.
	 * 
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc.
	 * 
	 */
	public <T> void delete(T bean) throws Exception;

	/**
	 * Given the java table model, delete all objects physically from table. The
	 * beans will be deleted using primary key, if available, otherwise deletion
	 * will fail.
	 * 
	 * @param tableClass
	 *            java table model, with annotations.
	 * 
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc.
	 * 
	 */
	public <T> void deleteAll(Class<T> tableClass) throws Exception;

	/**
	 * Given the java table model, and a field name. Return all available values for
	 * that field that exist in the database table. This requires a read of the
	 * entire table. The list will return unique values, which will be sorted alpha
	 * numerically.
	 * 
	 * @param tableClass
	 *            java table model, with annotations.
	 * @param fieldname
	 *            he name of the field for which possible values is being asked for.
	 * @param <T>
	 *            the class of the pojo database bean, annotated with classes from
	 *            package com.viper.database.annotations
	 * @return list of values for the specified field.
	 * 
	 * @throws Exception
	 *             failure to get table data, no database, no table, bad connection,
	 *             etc. Note the returned list will NOT be null, will be valid list
	 *             of zero to N length.
	 *
	 *             Note The following call will return all the ages for the
	 *             employees which actually exist in the database. The list of ages
	 *             will be unique and sorted.
	 * 
	 *             <pre>
	 * {@code
	 *  DatabaseInterface dao = DatabaseFactory.getInstance(dbc);
	 *  List<Object> values = dao.uniqueValues(Employee.class, "age");
	 * }
	 *             </pre>
	 * 
	 */
	public <T> List<Object> uniqueValues(Class<T> tableClass, String fieldname) throws Exception;

}
