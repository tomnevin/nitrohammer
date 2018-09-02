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

package com.viper.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.viper.database.annotations.Column;
import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.model.Database;
import com.viper.database.utils.FileUtil;

public class AgTablesUtil {

	private static final String DependencyDirectory = "res:/templates/";
	private static Map<String, Database> cache = new HashMap<String, Database>();
	private static Map<String, String> cache1 = new HashMap<String, String>();

	public final static <T> String header(Locale locale, String databasename, Class<T> clazz) throws Exception {
		return headerInternal(locale, databasename, clazz).toString();
	}

	private final static <T> JSONObject headerInternal(Locale locale, String databasename, Class<T> clazz)
			throws Exception {

		ResourceBundle resources = LocaleUtil.getBundle(locale);

		String tablename = DatabaseUtil.getTableName(clazz);
		List<Column> columns = DatabaseUtil.getColumnAnnotations(clazz);

		JSONObject headers = loadTables(makeTableFilename(tablename));

		// If no definition provided use all the columns for the table.
		boolean useAllColumns = false;
		if (!headers.has("columnDefs") || headers.getJSONArray("columnDefs").length() == 0) {
			useAllColumns = true;
			headers.put("columnDefs", new JSONArray());
		}
		JSONArray headerDefs = headers.getJSONArray("columnDefs");

		for (Column column : columns) {
			String columnName = toFullColumnName(clazz, column);
			String headerName = LocaleUtil.getString(resources, column.field());

			JSONObject columnDef = findColumnDef(headers, columnName);
			if (useAllColumns) {
				columnDef = new JSONObject();
				columnDef.put("headerName", headerName);
				columnDef.put("field", columnName);
				headerDefs.put(headerDefs.length(), columnDef);

			} else if (columnDef != null) {
				columnDef.put("headerName", headerName);
			}
		}
		return headers;
	}

	public final static <T> String data(Locale locale, String databaseName, Class<T> clazz,
			MultivaluedMap<String, String> formParams) throws Exception {
		return dataInternal(locale, databaseName, clazz, formParams).toString();
	}

	public final static <T> JSONObject dataInternal(Locale locale, String databaseName, Class<T> clazz,
			MultivaluedMap<String, String> formParams) throws Exception {

		DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(databaseName);

		List<T> beans;
		if (formParams == null) {
			beans = dao.queryAll(clazz);
		} else {
			beans = dao.querySQL(clazz, toWhereClause(formParams, clazz));
		}

		JSONObject data = new JSONObject();
		JSONArray items = new JSONArray();
		data.put("rowData", items);

		for (T bean : beans) {
			JSONObject table = new JSONObject();
			JSONObject item = new JSONObject();
			table.put(clazz.getSimpleName(), item);

			List<Column> columns = DatabaseUtil.getColumnAnnotations(bean.getClass());
			for (Column column : columns) {
				String name = column.field();
				String value = DatabaseUtil.getString(bean, column.name());

				item.put(name, value);
			}
			items.put(items.length(), table);
		}

		// figure out the actual number of rows in table.
		data.put("lastRow", -1);
		return data;
	}

	public final static <T> String table(Locale locale, String databasename, Class<T> clazz,
			MultivaluedMap<String, String> formParams) throws Exception {

		JSONObject table = headerInternal(locale, databasename, clazz);
		table.put("rowData", dataInternal(locale, databasename, clazz, formParams).get("rowData"));

		return table.toString();
	}

	public final static String form(Locale locale, String formname, List beans) throws Exception {

		JSONObject root = new JSONObject();

		root.put("title", formname);
		if (!root.has("tables")) {
			root.put("tables", new JSONArray());
		}
		JSONArray tables = root.getJSONArray("tables");

		if (!root.has("components")) {
			root.put("components", new JSONArray());
		}
		JSONArray components = root.getJSONArray("components");

		for (Object bean : beans) {

			List<Column> columns = DatabaseUtil.getColumnAnnotations(bean.getClass());
			for (Column column : columns) {

				String classname = bean.getClass().getName();
				String primnarykeyname = DatabaseUtil.getPrimaryKeyName(bean.getClass());
				Object primnarykeyvalue = DatabaseUtil.getPrimaryKeyValue(bean);
				String databasename = DatabaseUtil.getDatabaseName(bean.getClass());
				String tablename = DatabaseUtil.getTableName(bean.getClass());
				String columnName = toFullColumnName(bean.getClass(), column);
				String value = DatabaseUtil.getString(bean, column.name());
				Object obj = DatabaseUtil.get(bean, column.name());
				String url = "classes/" + formname + "/js/datatable/form";

				JSONObject component = new JSONObject();

				component.put("key", columnName);
				component.put("hidden", false);
				component.put("defaultValue", value);
				component.put("key", columnName);
				component.put("type", toComponentType(column));
				component.put("label", columnName);
				component.put("placeHolder", "{prompt}");
				component.put("maximumLength", column.size());
				component.put("minimumValue", column.minimumValue());
				component.put("maximumValue", column.maximumValue());
				component.put("required", column.required());
				component.put("persistent", column.persistent());
				component.put("logicalType", column.logicalType());
				component.put("dataType", column.dataType());
				component.put("validate", column.validator());
				component.put("autocomplete", "on");
				component.put("checked", "");
				component.put("pattern", "");
				component.put("cellRenderer", "");
				component.put("filter", "");

				if (obj != null && obj.getClass().isEnum()) {
					Object[] enumList = obj.getClass().getEnumConstants();
					component.put("options", toString(enumList));
				}

				components.put(components.length(), component);

				JSONObject table = findJSONObject(tables, "classname", classname);
				if (table == null) {
					table = new JSONObject();

					table.put("classname", classname);
					table.put("databasename", databasename);
					table.put("tablename", tablename);
					table.put("url", url);
					table.put("primaryKey", primnarykeyname);
					table.put("primaryValue", primnarykeyvalue);

					tables.put(tables.length(), table);
				}
			}
		}
		return root.toString();
	}

	public final static <T> String toWhereClause(MultivaluedMap<String, String> formParams, Class<T> clazz) {

		StringBuilder whereClause = new StringBuilder();

		if (formParams.size() > 0) {
			if (formParams.containsKey("startRow") && formParams.containsKey("endRow")) {
				int startRow = Integer.parseInt(formParams.getFirst("startRow"));
				int endRow = Integer.parseInt(formParams.getFirst("endRow"));
				int length = endRow - startRow;
				whereClause.append(" limit ");
				whereClause.append(Integer.toString(startRow));
				whereClause.append(", ");
				whereClause.append(Integer.toString(length));
			}
		}

		List<String> searchRequest = formParams.get("search");
		if (searchRequest != null) {

			boolean isFirst = true;
			List<Column> columns = DatabaseUtil.getColumnAnnotations(clazz);
			for (Column column : columns) {
				if (column.naturalKey() || column.unique()) {
					if (whereClause.length() == 0) {
						whereClause.append(" where match (");
					}
					whereClause.append(column.field());
					isFirst = false;
				}
			}

			if (!isFirst) {
				whereClause.append(") against ");
				whereClause.append(" ('");
				whereClause.append(searchRequest);
				whereClause.append(" ')");
			}
		}

		// List<String> columnRequests = formParams.get("columns");
		// if (columnRequests != null) {
		// boolean isFirst = true;
		// for (String columnRequest : columnRequests) {
		// if (columnRequest.getSearchable()) {
		// if (whereClause.length() == 0) {
		// whereClause.append(" where ");
		// }
		// if (!isFirst) {
		// whereClause.append(" or ");
		// }
		// whereClause.append(columnRequest.getName());
		// whereClause.append(" like ");
		// whereClause.append(columnRequest.getValue());
		// isFirst = false;
		// }
		// }
		// }

		return whereClause.toString();
	}

	public final static <T> List getBeansByForeignKeys(DatabaseInterface dao, String formname, Class<T> tableClazz,
			String key, String value) throws Exception {

		Database database = loadForm(makeDependenciesFilename(formname));

		return DatabaseUtil.getBeansByForeignKeys(dao, database, tableClazz, key, value);

	}

	private final static String makeDependenciesFilename(String formname) {
		return DependencyDirectory + "dependencies/" + formname.toLowerCase() + ".dependencies.xml";
	}

	private final static String makeTableFilename(String formname) {
		String filename = DependencyDirectory + "tables/" + formname.toLowerCase() + ".json";
		return filename;
	}

	private final static JSONObject loadTables(String filename) {
		if (cache1.containsKey(filename)) {
			return new JSONObject(cache1.get(filename));
		}

		try {

			String str = FileUtil.readFile(filename);
			cache1.put(filename, str);

			return new JSONObject(str);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new JSONObject();
	}

	private final static Database loadForm(String filename) {
		if (cache.containsKey(filename)) {
			return cache.get(filename);
		}

		Database database = null;
		try {

			database = DatabaseMapper.readDatabase(filename);

			cache.put(filename, database);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return database;
	}

	public final static <T> String getPrimaryKeyName(Class<T> clazz) {

		List<Column> columns = DatabaseUtil.getColumnAnnotations(clazz);
		if (columns != null && columns.size() > 0) {
			for (Column column : columns) {
				if (column.primaryKey()) {
					return column.name();
				}
			}
			return columns.get(0).name();
		}
		return null;
	}

	private static final JSONObject findJSONObject(JSONArray arr, String name, String value) {
		for (int i = 0; i < arr.length(); i++) {
			JSONObject field = arr.getJSONObject(i);
			if (field.has(name)) {
				if (field.getString(name).equalsIgnoreCase(value)) {
					return field;
				}
			}
		}
		return null;
	}

	private final static String toType(String javatype) {
		if (javatype.toLowerCase().contains("date") || javatype.toLowerCase().contains("time")) {
			return "date";
		}
		if (javatype.toLowerCase().contains("int")) {
			return "num";
		}
		if (javatype.toLowerCase().contains("float")) {
			return "num";
		}
		if (javatype.toLowerCase().contains("double")) {
			return "num";
		}
		return "string";
	}

	// DataTypeMapping.put("array", null);
	// DataTypeMapping.put("bit", new ConversionType("Boolean", "boolean"));
	// DataTypeMapping.put("boolean", new ConversionType("Boolean", "boolean"));
	// DataTypeMapping.put("bigint", new ConversionType("Long", "long"));
	// DataTypeMapping.put("binary", new ConversionType("Byte[]", "byte[]"));
	// DataTypeMapping.put("blob", new ConversionType("java.sql.Blob",
	// "java.sql.Blob"));
	// DataTypeMapping.put("char", new ConversionType("String", "String"));
	// DataTypeMapping.put("clob", new ConversionType("java.sql.Clob",
	// "java.sql.Clob"));
	// DataTypeMapping.put("datalink", null);
	// DataTypeMapping.put("date", new ConversionType("Long", "long"));
	// DataTypeMapping.put("datetime", new ConversionType("Long", "long"));
	// DataTypeMapping.put("decimal", new ConversionType("Double", "double"));
	// DataTypeMapping.put("distinct", null);
	// DataTypeMapping.put("double", new ConversionType("Double", "double"));
	// DataTypeMapping.put("enum", new ConversionType("enum", "enum"));
	// DataTypeMapping.put("float", new ConversionType("Float", "float"));
	// DataTypeMapping.put("int", new ConversionType("Integer", "int"));
	// DataTypeMapping.put("integer", new ConversionType("Integer", "int"));
	// DataTypeMapping.put("longblob", new ConversionType("Byte[]", "byte[]"));
	// DataTypeMapping.put("longnvarchar", new ConversionType("String", "String"));
	// DataTypeMapping.put("longtext", new ConversionType("String", "String"));
	// DataTypeMapping.put("longvarchar", new ConversionType("String", "String"));
	// DataTypeMapping.put("longvarbinary", new ConversionType("Byte[]", "byte[]"));
	// DataTypeMapping.put("mediumint", new ConversionType("Integer", "int"));
	// DataTypeMapping.put("nchar", new ConversionType("String", "String"));
	// DataTypeMapping.put("null", null);
	// DataTypeMapping.put("numeric", null);
	// DataTypeMapping.put("nvarchar", new ConversionType("String", "String"));
	// DataTypeMapping.put("nclob", new ConversionType("String", "String"));
	// DataTypeMapping.put("other", null);
	// DataTypeMapping.put("object", new ConversionType("Object", "Object"));
	// DataTypeMapping.put("real", new ConversionType("Double", "double"));
	// DataTypeMapping.put("ref", null);
	// DataTypeMapping.put("rowid", null);
	// DataTypeMapping.put("set", new ConversionType("Object", "Object"));
	// DataTypeMapping.put("smallint", new ConversionType("Integer", "int"));
	// DataTypeMapping.put("sqlxml", null);
	// DataTypeMapping.put("struct", null);
	// DataTypeMapping.put("time", new ConversionType("Long", "long"));
	// DataTypeMapping.put("timestamp", new ConversionType("Long", "long"));
	// DataTypeMapping.put("tinyint", new ConversionType("Integer", "int"));
	// DataTypeMapping.put("text", new ConversionType("String", "String"));
	// DataTypeMapping.put("varchar", new ConversionType("String", "String"));
	// DataTypeMapping.put("varbinary", new ConversionType("Byte[]", "byte[]"));

	private final static String toComponentType(Column column) {
		if (column.logicalType() != null && column.logicalType().length() > 0) {

			if (column.logicalType().contains("password")) {
				return "password";
			}
			if (column.logicalType().contains("password")) {
				return "password";
			}
			if (column.logicalType().contains("color")) {
				return "color";
			}
			if (column.logicalType().contains("email")) {
				return "email";
			}
			if (column.logicalType().contains("phone")) {
				return "tel";
			}
			if (column.logicalType().contains("url")) {
				return "url";
			}
			if (column.logicalType().contains("month")) {
				return "month";
			}
			if (column.logicalType().contains("week")) {
				return "week";
			}

			// number
			// range
			// search

		}
		String datatype = column.dataType();
		if (datatype.toLowerCase().contains("date")) {
			return "date";
		}
		if (datatype.toLowerCase().contains("time")) {
			return "time";
		}
		if (datatype.toLowerCase().contains("timestamp")) {
			return "datetime-local";
		}
		if (datatype.toLowerCase().contains("enum")) {
			return "combobox";
		}
		if (datatype.toLowerCase().contains("boolean")) {
			return "checkbox";
		}
		if (datatype.toLowerCase().contains("radio")) {
			return "radio";
		}

		return "text";
	}

	private final static <T> String toFullColumnName(Class<T> clazz, Column column) {

		return clazz.getSimpleName() + "." + column.field();
	}

	private final static String toString(Object[] values) {
		StringBuilder buf = new StringBuilder();

		for (Object value : values) {
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append(value.toString());
		}
		return buf.toString();
	}

	private final static JSONObject findColumnDef(JSONObject headers, String columnName) {

		if (headers != null) {
			JSONArray columnDefs = headers.getJSONArray("columnDefs");
			for (int i = 0; i < columnDefs.length(); i++) {
				if (columnName.equalsIgnoreCase(columnDefs.getJSONObject(i).getString("field"))) {
					return columnDefs.getJSONObject(i);
				}
			}
		}
		return null;
	}
}