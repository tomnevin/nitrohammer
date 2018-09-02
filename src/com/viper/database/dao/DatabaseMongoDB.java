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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.viper.database.annotations.Column;
import com.viper.database.dao.converters.ConverterUtils;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.security.Encryptor;

public class DatabaseMongoDB implements DatabaseInterface {

	private static final Logger log = Logger.getLogger(DatabaseMongoDB.class.getName());
	private static final String QL_CONTENTS = "contents";

	private MongoClient client = null;
	private DatabaseConnection dbc = new DatabaseConnection();

	public final static synchronized DatabaseInterface getInstance(DatabaseConnection dbc) {
		return new DatabaseMongoDB(dbc);
	}

	private DatabaseMongoDB(DatabaseConnection dbc) {

		this.dbc = dbc;

		open();
	}

	private void open() {
		if (client != null) {
			return;
		}
		try {
			Encryptor encryptor = new Encryptor();

			String databaseUrl = dbc.getDatabaseUrl();
			String username = dbc.getUsername();
			String password = encryptor.decryptPassword(dbc.getPassword());

			URI uri = new URI(databaseUrl);
			String databaseName = uri.getPath().substring(1);

			log.fine("Mongo Database opened.entering: " + databaseUrl + "," + username + "," + password + ","
					+ databaseName);

			if (username != null && password != null) {
				MongoCredential credential = MongoCredential.createMongoCRCredential(username, databaseName,
						password.toCharArray());
				client = new MongoClient(new ServerAddress(uri.getHost(), uri.getPort()), Arrays.asList(credential));
			} else {
				client = new MongoClient(new ServerAddress(uri.getHost(), uri.getPort()));
			}

			if (!isValidDatabase(client, databaseUrl, databaseName)) {
				// No worries if database not exist, getDatabase will create it.
				log.info("Can't find exiting Mongo Database " + databaseUrl + "," + databaseName);
			}

			log.fine("Mongo Database opened: " + databaseUrl + "," + client + ", " + databaseName);
			MongoDatabase database = client.getDatabase(databaseName);

			Document databaseInfo = database.runCommand(new Document("buildInfo", 1));
			log.info("Result from DatabaseInfo: " + username + ":" + databaseInfo);
			System.err.println("Document from buildInfo: " + databaseInfo);

			Document usersInfo = null;
			if (username != null) {
				usersInfo = database.runCommand(new Document("usersInfo", username));
				log.info("Result from usersInfo: " + username + ":" + usersInfo);
				System.err.println("Document from usersInfo: " + usersInfo);
			}

			if (usersInfo != null && (usersInfo.isEmpty() || ((BasicDBList) usersInfo.get("users")).isEmpty())) {
				BasicDBObject role = new BasicDBObject();
				role.put("role", "readWrite");
				role.put("db", databaseName);

				List<BasicDBObject> roles = new ArrayList<BasicDBObject>();
				roles.add(role);

				Map<String, Object> createUserCmd = new HashMap<String, Object>();
				createUserCmd.put("createUser", username);
				createUserCmd.put("pwd", password);
				createUserCmd.put("roles", roles);

				Document userCreated = database.runCommand(new Document(createUserCmd));
				log.fine("Result from createUser: " + username + ":" + userCreated);

				if (userCreated.isEmpty()) {
					throw new Exception("Mongo could not create user: " + databaseUrl + ":" + userCreated);
				}
			}

			boolean auth = true; // TODO
									// database.authenticate(uri.getUsername(),
									// uri.getPassword());
			if (!auth) {
				throw new Exception(
						"Mongo authenticate failed for user: " + databaseUrl + ":" + username + "," + password);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.throwing("ERROR: Unable to create table schema: ", "", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> long size(Class<T> clazz) throws Exception {

		String databasename = DatabaseUtil.getDatabaseName(clazz);
		String tablename = DatabaseUtil.getTableName(clazz);

		MongoCollection<Document> collection = client.getDatabase(databasename).getCollection(tablename);

		return collection.count();
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void release() {

		if (client != null) {
			client.close();
			client = null;
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public List<String> listDatabases() {
		List<String> items = new ArrayList<String>();
		MongoCursor<String> cursor = client.listDatabaseNames().iterator();
		while (cursor.hasNext()) {
			items.add(cursor.next());
		}
		return items;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public List<String> listTables(String databasename) {
		List<String> items = new ArrayList<String>();
		MongoCursor<String> cursor = client.getDatabase(databasename).listCollectionNames().iterator();
		while (cursor.hasNext()) {
			items.add(cursor.next());
		}
		return items;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> List<String> listColumns(Class<T> clazz) {
		return new ArrayList<String>();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Note: currently this method is not implemented.
	 */
	@Override
	public <T> boolean hasChanged(Class<T> clazz) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> void create(Class<T> tableClass) throws Exception {

	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> void createDatabase(String packagename) {

		List<Class> classes = DatabaseUtil.listDatabaseTableClasses(packagename, null);
		for (Class clazz : classes) {
			try {
				create(clazz);
			} catch (Exception ex) {
				log.throwing("", "Problems Creating Table: " + clazz, ex);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> T query(Class<T> clazz, Object... keyValue) throws Exception {
		List<String> json = read(clazz, 0, 1, keyValue);
		if (json == null || json.size() == 0) {
			return null;
		}
		return ConverterUtils.readJson(json.get(0), clazz);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> List<T> queryAll(Class<T> clazz) throws Exception {
		List<String> json = read(clazz, 0, Integer.MAX_VALUE, (Object[]) null);
		return ConverterUtils.readJsonListToList(json, clazz);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> List<T> queryList(Class<T> clazz, Object... keyValue) throws Exception {
		List<String> json = read(clazz, 0, Integer.MAX_VALUE, keyValue);
		return ConverterUtils.readJsonListToList(json, clazz);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> List<T> queryList(Class<T> clazz, Predicate<T> filter) throws Exception {
		List<T> results = new ArrayList<T>();

		List<T> beans = queryAll(clazz);
		for (T bean : beans) {
			if (filter.apply(bean)) {
				results.add(bean);
			}
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> void delete(Class<T> clazz, Object... keyValue) throws Exception {

		String databaseName = DatabaseUtil.getDatabaseName(clazz);
		String tableName = DatabaseUtil.getTableName(clazz);
		MongoCollection coll = client.getDatabase(databaseName).getCollection(tableName);
		Bson whereQuery = buildWhereQuery(keyValue);
		FindIterable<Document> iterable = coll.find(whereQuery);
		MongoCursor<Document> cursor = iterable.iterator();

		try {
			while (cursor.hasNext()) {
				Document obj = cursor.next();
				if (obj != null) {
					assertNoError(coll.deleteOne(obj));
				}
			}
		} finally {
			cursor.close();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> void delete(T bean) throws Exception {

		String databaseName = DatabaseUtil.getDatabaseName(bean.getClass());
		String tableName = DatabaseUtil.getTableName(bean.getClass());
		String primaryKeyName = DatabaseUtil.getPrimaryKeyName(bean.getClass());
		Bson query = new BasicDBObject(primaryKeyName, DatabaseUtil.getValue(bean, primaryKeyName));
		MongoCollection coll = client.getDatabase(databaseName).getCollection(tableName);
		assertNoError(coll.deleteMany(query));
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> void deleteAll(Class<T> clazz) throws Exception {

		String databaseName = DatabaseUtil.getDatabaseName(clazz);
		String tableName = DatabaseUtil.getTableName(clazz);
		MongoCollection coll = client.getDatabase(databaseName).getCollection(tableName);
		assertNoError(coll.deleteMany(new BasicDBObject()));
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> T update(T bean) throws Exception {
		return insert(bean);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> T insert(T bean) throws Exception {

		String databaseName = DatabaseUtil.getDatabaseName(bean.getClass());
		String tableName = DatabaseUtil.getTableName(bean.getClass());
		String primaryKeyName = DatabaseUtil.getPrimaryKeyName(bean.getClass());
		List<Column> primaryColumns = DatabaseUtil.getPrimaryKeyColumns(bean.getClass());
		List<String> columnFamilyNames = DatabaseUtil.getColumnFamilyNames(bean.getClass());
		List<String> qualifierNames = DatabaseUtil.getQualifierNames(bean.getClass());

		MongoCollection<Document> coll = client.getDatabase(databaseName).getCollection(tableName);

		Document obj = new Document();
		for (String columnFamilyName : columnFamilyNames) {
			for (String qualifierName : qualifierNames) {
				obj.put(qualifierName, DatabaseUtil.getValue(bean, qualifierName));
				if (DatabaseUtil.isUniqueColumn(bean.getClass(), qualifierName)) {
					// TODO coll.ensureIndex(" { \"" + qualifierName + "\": 1 },
					// { unique: true }
					// ");
				}
			}
			obj.put(QL_CONTENTS, ConverterUtils.writeJson(bean));
		}

		if (DatabaseUtil.isValidPrimaryKeyValue(bean)) {
			Bson query = new BasicDBObject(primaryKeyName, DatabaseUtil.getPrimaryKeyValue(bean));
			coll.replaceOne(query, obj);
		} else {
			obj.put(primaryKeyName, getPrimaryKeyValue(bean, primaryColumns.get(0), primaryKeyName, coll.count() + 1));
			coll.insertOne(obj);
		}

		return bean;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> void insertAll(List<T> beans) throws Exception {
		if (beans == null || beans.size() == 0) {
			return;
		}

		Class clazz = beans.get(0).getClass();
		String databaseName = DatabaseUtil.getDatabaseName(clazz);
		String tableName = DatabaseUtil.getTableName(clazz);
		String primaryKeyName = DatabaseUtil.getPrimaryKeyName(clazz);
		List<Column> primaryColumns = DatabaseUtil.getPrimaryKeyColumns(clazz);
		List<String> columnFamilyNames = DatabaseUtil.getColumnFamilyNames(clazz);
		List<String> qualifierNames = DatabaseUtil.getQualifierNames(clazz);

		MongoCollection coll = client.getDatabase(databaseName).getCollection(tableName);
		long counter = coll.count();

		System.err.println("InsertAll: initial size " + tableName + ", " + counter);

		for (T bean : beans) {

			Document obj = new Document();
			for (String columnFamilyName : columnFamilyNames) {
				for (String qualifierName : qualifierNames) {
					obj.put(qualifierName, DatabaseUtil.getString(bean, qualifierName));
					if (DatabaseUtil.isUniqueColumn(clazz, qualifierName)) {
						// TODO coll.ensureIndex(" { \"" + qualifierName + "\":
						// 1 }, { unique: true
						// } ");
					}
				}
				obj.put(QL_CONTENTS, ConverterUtils.writeJson(bean));
			}
			if (DatabaseUtil.isValidPrimaryKeyValue(bean)) {
				Bson query = new BasicDBObject(primaryKeyName, DatabaseUtil.getPrimaryKeyValue(bean));
				coll.replaceOne(query, obj);
			} else {
				counter = counter + 1;
				obj.put(primaryKeyName, getPrimaryKeyValue(bean, primaryColumns.get(0), primaryKeyName, counter));
				coll.insertOne(obj);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public <T> List<Object> uniqueValues(Class<T> clazz, String fieldName) throws Exception {

		String databaseName = DatabaseUtil.getDatabaseName(clazz);
		String tableName = DatabaseUtil.getTableName(clazz);
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(tableName);

		List<Object> items = new ArrayList<Object>();

		FindIterable<Document> iterable = collection.find();
		MongoCursor<Document> cursor = iterable.iterator();

		while (cursor.hasNext()) {
			Document obj = cursor.next();
			Object value = obj.get(fieldName);
			if (value != null && !items.contains(value)) {
				items.add("" + value);
			}
		}
		cursor.close();
		return items;
	}

	private <T> List<String> read(Class<T> clazz, int startRow, int numRows, Object[] keyValue) throws Exception {

		String databaseName = DatabaseUtil.getDatabaseName(clazz);
		String tableName = DatabaseUtil.getTableName(clazz);
		Bson whereQuery = buildWhereQuery(keyValue);

		FindIterable<Document> iterable = client.getDatabase(databaseName).getCollection(tableName).find(whereQuery);

		System.err.println("MONGO - read: " + whereQuery.toString());

		if (startRow > 0) {
			iterable.skip(startRow);
		}

		List<String> list = new ArrayList<String>();
		MongoCursor<Document> cursor = iterable.iterator();
		try {
			while (cursor.hasNext()) {
				Document obj = cursor.next();
				String json = (String) obj.get(QL_CONTENTS);
				if (json != null) {
					list.add(json);
				}
				numRows = numRows - 1;
				if (numRows <= 0) {
					break;
				}
			}
		} finally {
			cursor.close();
		}
		return list;
	}

	private <T> Bson buildWhereQuery(Object[] keyValue) {
		BasicDBObject query = new BasicDBObject();
		if (keyValue != null && keyValue.length > 0) {
			for (int i = 0; i < keyValue.length;) {
				String key = (String) keyValue[i++];
				Object value = keyValue[i++];

				query.put(key, value);
			}
		}
		return query;
	}

	/**
	 * Creates a select statement for SQL that can do multiple where and selections
	 * based on the keyValue pairs.
	 * 
	 * @param <T>
	 * 
	 * @param tableName
	 * @param keyValue
	 * @return
	 * @throws Exception
	 */
	private <T> BasicDBObject buildWhereClauseByMap(String primaryKeyName, Map<String, List<String>> params)
			throws Exception {

		BasicDBObject query = new BasicDBObject();
		if (params != null) {
			for (String key : params.keySet()) {
				List<String> values = params.get(key);
				if (values == null || values.size() == 0) {
					continue;
				}
				if (primaryKeyName.equals(key)) {
					// query.put(getPrimaryKeyName(), values.get(0));
				} else {
					query.put(key, values.get(0));
				}
			}
		}
		return query;
	}

	/**
	 * TODO This is a bad idea for mongo, just use a natural primary key, like user
	 * login name, timestamp, etc.
	 *
	 * @param bean
	 * @param primaryKeyName
	 * @param counter
	 * @return
	 */
	private <T> Object getPrimaryKeyValue(T bean, Column primaryColumn, String primaryKeyName, long counter) {
		if (primaryColumn.idMethod().equalsIgnoreCase("autoincrement")) {
			DatabaseUtil.setValue(bean, primaryKeyName, counter);
		} else if (DatabaseUtil.getValue(bean, primaryKeyName) == null
				|| DatabaseUtil.getString(bean, primaryKeyName).equals("0")) {
			DatabaseUtil.setValue(bean, primaryKeyName, counter);
		}
		return DatabaseUtil.getValue(bean, primaryKeyName);
	}

	private boolean isValidDatabase(MongoClient mongo, String url, String databaseName) {

		List<String> dbnames = mongo.getDatabaseNames();
		for (String dbname : dbnames) {
			log.info("Database name: " + dbname);
		}
		return dbnames.contains(databaseName);
	}

	private void assertNoError(DeleteResult result) throws Exception {
		// if (!result.getLastError().ok()) {
		// throw new Exception(result.toString());
		// }
	}

	private void assertNoError(WriteResult result) throws Exception {
		// if (!result.getLastError().ok()) {
		// throw new Exception(result.toString());
		// }
	}
}
