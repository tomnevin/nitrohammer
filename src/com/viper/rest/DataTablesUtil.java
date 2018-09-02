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

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
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
import com.viper.database.rest.model.ColumnRequest;
import com.viper.database.rest.model.SearchRequest;
import com.viper.database.rest.model.TableColumn;
import com.viper.database.rest.model.TableColumnResponse;
import com.viper.database.rest.model.TableOptions;
import com.viper.database.rest.model.TableRequest;
import com.viper.database.rest.model.TableResponse;
import com.viper.database.rest.model.TableResponses;

public class DataTablesUtil {

	private static final String DependencyDirectory = "res:/templates/";
	private static Map<String, Database> cache = new HashMap<String, Database>();
	private static Map<String, JSONObject> cache1 = new HashMap<String, JSONObject>();

	public final static <T> TableColumnResponse response(Locale locale, String databaseName,
			TableColumnResponse response, Class<T> clazz, TableRequest request) throws Exception {

		DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(databaseName);

		List<T> data = new ArrayList<T>();
		if (request == null) {
			data = dao.queryAll(clazz);
		} else {
			String whereClause = toWhereClause(request, clazz);

			data = dao.querySQL(clazz, whereClause);
		}

		TableOptions options = new TableOptions();
		options.setOrdering(false);
		options.setOrderable(false);
		options.setSelectable(true);
		options.setSort(false);
		options.setAutoWidth(true);
		options.setPrimaryKey(getPrimaryKeyName(clazz));

		response.getColumns().addAll(headers(locale, databaseName, clazz));
		// response.getData().addAll(data);
		response.setOptions(options);

		return response;
	}

	public final static <T> List<TableColumn> headers(Locale locale, String databasename, Class<T> clazz)
			throws Exception {

		ResourceBundle resources = LocaleUtil.getBundle(locale);

		List<Column> columns = DatabaseUtil.getColumnAnnotations(clazz);

		List<TableColumn> dataTableColumns = new ArrayList<TableColumn>();

		for (Column column : columns) {
			TableColumn dataTableColumn = new TableColumn();
			dataTableColumn.setCellType("td");
			dataTableColumn.setClassName(null); // css classname
			dataTableColumn.setContentPadding(null);
			dataTableColumn.setCreatedCell(null); // function to calculate cell data
			dataTableColumn.setData(column.name());
			dataTableColumn.setDefaultContent("&nbsp;");
			dataTableColumn.setName(LocaleUtil.getString(resources, column.field()));
			dataTableColumn.setOrderable(false);
			dataTableColumn.getOrderData().add(dataTableColumns.size());
			dataTableColumn.setOrderDataType(null);
			dataTableColumn.setRender(null);
			dataTableColumn.setSearchable(true);
			dataTableColumn.setTitle(LocaleUtil.getString(resources, column.field()));
			dataTableColumn.setType(toType(column.javaType()));
			dataTableColumn.setVisible(true);
			dataTableColumn.setWidth(null);
			dataTableColumns.add(dataTableColumn);
		}

		return dataTableColumns;
	}

	public final static <T> TableResponse request(Locale locale, String databaseName, TableResponse response,
			Class<T> clazz, TableRequest request) throws Exception {

		DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(databaseName);

		List<T> data = null;
		if (request == null) {
			data = dao.queryAll(clazz);
		} else {
			String whereClause = toWhereClause(request, clazz);

			data = dao.querySQL(clazz, whereClause);
		}

		if (request != null) {
			response.setDraw(request.getDraw());
		}
		// response.setError(null);
		response.setRecordsFiltered(data.size());
		response.setRecordsTotal(data.size());
		response.getData().addAll(data);

		return response;
	}

	public final static TableResponses form(Locale locale, List beans) throws Exception {
		TableResponses responses = new TableResponses();

		for (Object bean : beans) {
			TableResponse response = new TableResponse();
			response.setName(DatabaseUtil.getTableName(bean.getClass()));
			response.getData().add(bean);
			responses.getTables().add(response);
		}
		return responses;
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
				component.put("validate", column.validator());
				component.put("logicalType", column.logicalType());
				component.put("dataType", column.dataType());
				
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

	public final static <T> String toWhereClause(TableRequest request, Class<T> clazz) {

		StringBuilder whereClause = new StringBuilder();

		if (request.getLength() > 0) {
			whereClause.append(" limit ");
			whereClause.append(request.getStart());
			whereClause.append(", ");
			whereClause.append(request.getLength());
		}

		// SELECT * FROM clients WHERE MATCH (shipping_name, billing_name, email)
		// AGAINST ('mary')

		SearchRequest searchRequest = request.getSearch();
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
				whereClause.append(searchRequest.getValue());
				whereClause.append(" ')");
			}
		}

		List<ColumnRequest> columnRequests = request.getColumns();
		if (columnRequests != null) {
			boolean isFirst = true;
			for (ColumnRequest columnRequest : columnRequests) {
				if (columnRequest.getSearchable()) {
					if (whereClause.length() == 0) {
						whereClause.append(" where ");
					}
					if (!isFirst) {
						whereClause.append(" or ");
					}
					whereClause.append(columnRequest.getName());
					whereClause.append(" like ");
					whereClause.append(columnRequest.getValue());
					isFirst = false;
				}
			}
		}

		return whereClause.toString();
	}

	public final static TableRequest toTableRequest(MultivaluedMap<String, String> formParams) {
		TableRequest request = new TableRequest();

		if (formParams != null) {
			if (formParams.containsKey("draw")) {
				request.setDraw(Integer.parseInt(formParams.getFirst("draw")));
			}
			if (formParams.containsKey("start")) {
				request.setStart(Integer.parseInt(formParams.getFirst("start")));
			}
			if (formParams.containsKey("length")) {
				request.setLength(Integer.parseInt(formParams.getFirst("length")));
			}
		}

		return request;
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
		return DependencyDirectory + "tables/" + formname.toLowerCase() + ".json";
	}
	
	private final static JSONObject loadTables(String filename) {
		if (cache1.containsKey(filename)) {
			return cache1.get(filename);
		}

		JSONObject columns = null;
		try {

			byte[] bytes = Files.readAllBytes(new File(filename).toPath());
			columns = new JSONObject(new String(bytes));

			cache1.put(filename, columns);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return columns;
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

}