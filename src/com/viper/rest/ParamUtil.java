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

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.stream.StreamSource;

import com.viper.database.annotations.Column;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.params.model.ParamType;
import com.viper.database.params.model.Params;
import com.viper.database.utils.FileUtil;
import com.viper.database.utils.JAXBUtil;

public class ParamUtil {

	private static final String FormDirectory = "res:/templates/";
	private static Map<String, Params> cache = new HashMap<String, Params>();

	private static List<String> booleanValues = new ArrayList<String>();
	static {
		booleanValues.add("true");
		booleanValues.add("false");
	}

	public final static <T> Params form(Locale locale, String formname, List<T> beans) throws Exception {

		ResourceBundle resources = LocaleUtil.getBundle(locale);

		Params params = loadForm(formname);
		if (params == null) {
			return new Params();
		}

		for (Object bean : beans) {

			List<Column> columns = DatabaseUtil.getColumnAnnotations(bean.getClass());
			for (Column column : columns) {

				String columnName = getFullColumnName(bean.getClass(), column);

				String value = DatabaseUtil.getString(bean, column.field());
				
				putValue(params.getFields(), columnName, null, value);
				
				putValue(params.getAttrs(), columnName, "title", LocaleUtil.getString(resources, column.field()));
				putValue(params.getAttrs(), columnName, "name", columnName);
				putValue(params.getAttrs(), columnName, "fieldName", columnName);
				putValue(params.getAttrs(), columnName, "required", Boolean.toString(column.required()));

				// put(col, "hidden", (columnLayout == null) ? true : false);
				// put(col, "enum", column.logicalType());
				// put(col, "type", "");// Component checkbox etc
				// put(col, "dateFormat", "HH:mm:ss");
				// put(col, "slider", false);
				// put(col, "helper", "");
				// put(col, "placeholder", "");

				
				if (column.dataType().equalsIgnoreCase("enum")) {

					putValue(params.getAttrs(), columnName, "dataType", column.dataType());
					Class enumClass = DatabaseUtil.toPropertyClass(bean.getClass(), column.field());
					List<String> enumValues = new ArrayList<String>();
					for (Object o : enumClass.getEnumConstants()) {
						enumValues.add((o == null) ? "" : o.toString());
					}
					putValue(params.getAttrs(), columnName, "dataType", enumValues.toString());

				} else if (column.javaType().equalsIgnoreCase("boolean")) {
					putValue(params.getAttrs(), columnName, "dataType", column.dataType());
					putValue(params.getAttrs(), columnName, "dataType", booleanValues.toString());

				} else if (column.javaType().equalsIgnoreCase("int")) {
					putValue(params.getAttrs(), columnName, "dataType", "number");
					putValue(params.getAttrs(), columnName, "maximumValue", column.minimumValue());
					putValue(params.getAttrs(), columnName, "minimumValue", column.maximumValue());

				} else if (column.javaType().equalsIgnoreCase("long")) {
					putValue(params.getAttrs(), columnName, "dataType", "number");
					putValue(params.getAttrs(), columnName, "maximumValue", column.minimumValue());
					putValue(params.getAttrs(), columnName, "minimumValue", column.maximumValue());

				} else {
					putValue(params.getAttrs(), columnName, "dataType", column.javaType().toLowerCase());
				}
				if (column.size() > 0) {
					putValue(params.getAttrs(), columnName, "maximumSize", Integer.toString((int)column.size()));
				}
				if (column.defaultValue() != null && !column.defaultValue().isEmpty()) {
					putValue(params.getAttrs(), columnName, "defaultValue", column.defaultValue());
				}
				// putValue(params.getAttrs(), columnName, "validators", column.validators());

			}
		}

		return params;
	}

	private final static <T> String getFullColumnName(Class<T> clazz, Column column) {

		return clazz.getSimpleName() + "." + column.name();
	}

	public final static List makeBeans(String formname, MultivaluedMap<String, String> formParams) {

		System.out.println("makeBeans: formname = " + formname);
		for (String key : formParams.keySet()) {
			System.out.println("makeBeans: " + key + " " + formParams.getFirst(key));
		}

		Params formLayout = loadForm(formname);
		if (formLayout == null) {
			return new ArrayList();
		}

		Map<String, Object> beans = new HashMap<String, Object>();

		for (String key : formParams.keySet()) {
			int index1 = key.indexOf('[');
			int index2 = key.indexOf("]");
			int indexDot = key.lastIndexOf('.', index2 - 1);

			// TODO add checks on the indexes, make sure they are correct.

			String tablename = key.substring(index1 + 1, indexDot).trim();
			String fieldname = key.substring(indexDot + 1, index2).trim();
			String value = formParams.getFirst(key);

			String classname = getClassname(formLayout, tablename);

			Object bean = beans.get(classname);
			if (bean == null) {
				try {
					bean = Class.forName(classname).newInstance();
					beans.put(classname, bean);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (bean != null) {
				DatabaseUtil.setValue(bean, fieldname, value);
			}
		}
		return new ArrayList(beans.values());
	}

	public static final String getClassname(Params params, String tablename) {
		ParamType param = findTable(params, tablename);
		return (param == null) ? null : param.getValue();
	}

	private static final ParamType findTable(Params params, String name) {
		if (params != null) {
			for (ParamType param : params.getTables()) {
				if (name.equalsIgnoreCase(param.getName())) {
					return param;
				}
			}
		}
		return null;
	}

	private static final void putValue(List<ParamType> params, String name, String attr, String value) {
		if (params != null) {
			for (ParamType param : params) {
				if (name.equalsIgnoreCase(param.getName()) && attr.equalsIgnoreCase(param.getAttribute())) {
					param.setValue(value);
					return;
				}
			}
		}
		ParamType param = new ParamType();
		param.setName(name);
		param.setAttribute(attr);
		param.setValue(value);
		params.add(param);
	}

	public final static Params loadForm(String formname) {

		if (cache.containsKey(formname)) {
			return cache.get(formname);
		}

		String filename = FormDirectory + formname.toLowerCase() + ".xml";
		Params formLayout = readForm(filename);
		cache.put(formname, formLayout);

		return formLayout;
	}

	public final static String makeSaveFormURL(URI path) {
		return path.toString().replace("/response", "/save");
	}

	/**
	 * Given the filename load from the file, all the database connections.
	 * 
	 * @param filename
	 *            the file of XML data contain the connection data.
	 * @return the object containing the database connection information.
	 * @throws Exception
	 *             The transfer from file failed. File access problem or JAXB
	 *             conversion issue.
	 */
	public final static Params readForm(String filename) {
		try {
			System.out.println("Reading file: " + filename);
			return readValueFromXml(FileUtil.getInputStream(Params.class, filename), Params.class);
		} catch (Exception jaxerr) {
			new Exception("Unable to parse for file: " + filename, jaxerr).printStackTrace();
			return new Params();
		}
	}

	private final static <T> T readValueFromXml(InputStream inputstream, Class<T> clazz) throws Exception {
		return JAXBUtil.createXmlUnmarshaller(clazz, null).unmarshal(new StreamSource(inputstream), clazz).getValue();
	}
}