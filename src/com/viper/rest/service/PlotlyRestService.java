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

package com.viper.rest.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.utils.FileUtil;
import com.viper.rest.LocaleUtil;

public class PlotlyRestService implements RestServiceInterface {

    private final String getDatabaseName() throws Exception {
        return com.viper.database.utils.ResourceUtil.getResource("DATABASE_LOCATOR", "test");
    }

    @Override
    public <T> Response query(Class<T> clazz, Locale locale, List<String> whereClause) throws Exception {
        return new RestService().query(clazz, locale, whereClause);
    }

    @Override
    public <T> Response query(Class<T> clazz, Locale locale, String key, String value) throws Exception {

        String json = getDashboard(locale, getDatabaseName(), value);

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response queryList(Class<T> clazz, Locale locale, String key, String value) throws Exception {

        String json = getDashboard(locale, getDatabaseName(), null);

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response queryList(Class<T> clazz, Locale locale, MultivaluedMap<String, String> queryParams) throws Exception {

        String json = getDashboard(locale, getDatabaseName(), null);

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response queryAll(Class<T> clazz, Locale locale) throws Exception {

        String json = getDashboard(locale, getDatabaseName(), null);

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @Override
    public <T> Response update(Class<T> clazz, String request) throws Exception {
        return new RestService().update(clazz, request);
    }

    @Override
    public <T> Response update(Class<T> clazz, MultivaluedMap<String, String> queryParams) throws Exception {
        return new RestService().update(clazz, queryParams);
    }

    @Override
    public <T> Response createItem(Class<T> clazz, String request) throws Exception {
        return new RestService().createItem(clazz, request);
    }

    @Override
    public <T> Response createItems(Class<T> clazz, String request) throws Exception {
        return new RestService().createItems(clazz, request);
    }

    @Override
    public <T> Response deleteItem(Class<T> clazz, String key, String value) throws Exception {
        return new RestService().deleteItem(clazz, key, value);
    }

    @Override
    public <T> Response deleteItem(Class<T> clazz, String request) throws Exception {
        return new RestService().deleteItem(clazz, request);
    }

    public Response directory(Locale locale, String charts) throws Exception {

        String json = getDirectory(locale, getDatabaseName(), charts);

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    private static final String DependencyDirectory = "res:/templates/";
    private static Map<String, JSONObject> cache = new HashMap<String, JSONObject>();

    public final static String getDirectory(Locale locale, String datasource, String charts) {

        ResourceBundle resources = LocaleUtil.getBundle(locale);

        String filename = makeDashboardFilename(charts);
        JSONObject directory = loadDashboard(filename);

        return directory.toString();
    }

    public final static String getDashboard(Locale locale, String datasource, String chartname) {

        String filename = makeDashboardFilename(chartname);
        JSONObject dashboard = loadDashboard(filename);
        
        JSONArray charts = null;
        if (!dashboard.has("charts")) {
            dashboard.put("charts", new JSONArray());
        }
        charts = dashboard.getJSONArray("charts");
        
        for (int i = 0; i < charts.length(); i++) {
            JSONObject chart = charts.getJSONObject(i);

            String method = "chart";
            if (chart.has("method")) {
                method = chart.getString("method");
            }

            String chartname1 = chart.getString("name");
            if (chartname != null && !chartname.equalsIgnoreCase(chartname1)) {
                continue;
            }

            switch (method) {
            case "chart":
                generateData(locale, datasource, chart);
                break;
            case "topology":
                generateDirectedData(locale, datasource, chart);
                break;
            case "occurrences":
                generateOccurrenceData(locale, datasource, chart);
                break;
            }

        }

        return dashboard.toString();
    }

    private final static String makeDashboardFilename(String formname) {
        if (formname == null) {
            formname = "all";
        }
        return DependencyDirectory + "charts/" + formname.toLowerCase() + ".json";
    }

    private final static JSONObject loadDashboard(String filename) {
        if (cache.containsKey(filename)) {
            return cache.get(filename);
        }

        try {
            String str = FileUtil.readFile(filename);
            JSONObject dashboard = new JSONObject(str);

            cache.put(filename, dashboard);

            return dashboard;
        } catch (Exception ex) {
            System.out.println("ERROR: filename=" + filename);
            ex.printStackTrace();
        }

        return new JSONObject();
    }

    private final static <T> String toFieldName(String fieldname) {

        int index = fieldname.lastIndexOf('.');
        return (index == -1) ? fieldname : fieldname.substring(index + 1);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final List load(String source, String classname, String whereclause) {

        List items = new ArrayList();
        try {

            Class clazz = DatabaseUtil.toTableClass(classname);
            DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(source);
            if (whereclause == null) {
                items = dao.queryAll(clazz);
            } else {
                items = dao.querySQL(clazz, whereclause);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return items;
    }

    private static final void generateData(Locale locale, String datasource, JSONObject chart) {

        ResourceBundle resources = LocaleUtil.getBundle(locale);

        JSONArray sources = chart.getJSONArray("sources");
        for (int j = 0; j < sources.length(); j++) {
            JSONObject source = sources.getJSONObject(j);

            String classname = source.getString("classname");
            String whereclause = null;
            if (source.has("whereclause")) {
                whereclause = source.getString("whereclause");
            }

            List items = load(datasource, classname, whereclause);

            if (!chart.has("data")) {
                chart.put("data", new JSONObject());
            }
            JSONObject data = chart.getJSONObject("data");

            if (!data.has("columns")) {
                data.put("columns", new JSONArray());
            }
            JSONArray columns = data.getJSONArray("columns");

            JSONObject fields = source.getJSONObject("fields");
            String[] names = JSONObject.getNames(fields);
            for (int i = 0; i < names.length; i++) {
                String fieldname = fields.getString(names[i]);

                String columnName = LocaleUtil.getString(resources, fieldname);

                JSONArray values = new JSONArray();
                columns.put(i, values);

                values.put(0, columnName);
            }

            if (items != null) {
                int counter = 0;
                for (Object item : items) {
                    for (int i = 0; i < names.length; i++) {
                        String fieldname = fields.getString(names[i]);
                        String value = DatabaseUtil.getString(item, toFieldName(fieldname));

                        JSONArray values = (JSONArray) columns.get(i);
                        values.put(values.length(), value);
                    }

                    // Limit the number of points for performance
                    // MUST BE REMOVED FOR PRODUCTION
                    counter = counter + 1;
                    if (counter > 100) {
                        break;
                    }
                }
            }
        }
    }

    private static final void generateOccurrenceData(Locale locale, String datasource, JSONObject chart) {

        ResourceBundle resources = LocaleUtil.getBundle(locale);

        JSONArray sources = chart.getJSONArray("sources");
        for (int j = 0; j < sources.length(); j++) {
            JSONObject source = sources.getJSONObject(j);

            String classname = source.getString("classname");

            String whereclause = null;
            if (source.has("whereclause")) {
                whereclause = source.getString("whereclause");
            }

            if (!chart.has("data")) {
                chart.put("data", new JSONObject());
            }
            JSONObject data = chart.getJSONObject("data");

            if (!data.has("columns")) {
                data.put("columns", new JSONArray());
            }
            JSONArray columns = data.getJSONArray("columns");

            List items = load(datasource, classname, whereclause);

            Map<String, Integer> values = new HashMap<String, Integer>();
            if (items != null) {
                for (Object item : items) {

                    JSONObject fields = source.getJSONObject("fields");
                    String[] names = JSONObject.getNames(fields);
                    for (int i = 0; i < names.length; i++) {
                        String fieldname = fields.getString(names[i]);

                        String name = DatabaseUtil.getString(item, toFieldName(fieldname));
                        String columnName = LocaleUtil.getString(resources, name);

                        if (values.containsKey(columnName)) {
                            values.put(columnName, values.get(columnName) + 1);
                        } else {
                            values.put(columnName, 1);
                        }
                    }
                }
            }

            for (String property : values.keySet()) {
                JSONArray item = new JSONArray();
                item.put(0, property);
                item.put(1, values.get(property));
                columns.put(columns.length(), item);
            }
        }
    }

    private static final void generateDirectedData(Locale locale, String datasource, JSONObject chart) {

        ResourceBundle resources = LocaleUtil.getBundle(locale);

        Map<String, List> cache = new HashMap<String, List>();

        if (!chart.has("data")) {
            chart.put("data", new JSONObject());
        }
        JSONObject data = chart.getJSONObject("data");

        if (!data.has("edges")) {
            data.put("edges", new JSONArray());
        }

        Map<String, JSONObject> nodeMap = new HashMap<String, JSONObject>();
        Map<String, JSONObject> edgeMap = new HashMap<String, JSONObject>();

        JSONArray sources = chart.getJSONArray("sources");
        for (int j = 0; j < sources.length(); j++) {
            JSONObject source = sources.getJSONObject(j);

            String classname = source.getString("classname");

            String whereclause = null;
            if (source.has("whereclause")) {
                whereclause = source.getString("whereclause");
            }

            List items = load(datasource, classname, whereclause);

            if (items != null) {
                for (Object item : items) {
                    JSONObject fields = (source.has("fields")) ? source.getJSONObject("fields") : new JSONObject();
                    String[] names = JSONObject.getNames(fields);

                    if (names != null) {
                        JSONObject node = new JSONObject();

                        for (String name : names) {
                            String fieldname = fields.getString(name);
                            String value = DatabaseUtil.getString(item, toFieldName(fieldname));
                            if (value != null) {

                                node.put(name, value);

                                if ("id".equalsIgnoreCase(name)) {
                                    nodeMap.put(value, node);
                                }
                            }
                        }
                    }

                    String[] edgenames = new String[] { "from", "to" };
                    JSONObject edge = new JSONObject();
                    for (String edgename : edgenames) {
                        if (source.has(edgename)) {
                            String fieldname = source.getString(edgename);
                            String value = DatabaseUtil.getString(item, toFieldName(fieldname));

                            edge.put(edgename, value);
                        }
                    }
                    if (edge.has("from") && edge.has("to")) {
                        String edgeKey = edge.getString("from") + "." + edge.getString("to");
                        edgeMap.put(edgeKey, edge);
                    }
                }
            }
        }

        // Limit to 100 nodes for now till we get the performance issue resolved.
        // MUST REMOVE FOR PRODUCTION
        Collection<JSONObject> nodeList = nodeMap.values();
        if (nodeList.size() > 100) {
            nodeList = new ArrayList<JSONObject>(nodeList).subList(0, 100);
        }

        Collection<JSONObject> edgeList = edgeMap.values();
        if (edgeList.size() > 100) {
            edgeList = new ArrayList<JSONObject>(edgeList).subList(0, 100);
        }

        data.put("nodes", new JSONArray(nodeList));
        data.put("edges", new JSONArray(edgeList));
    }
}
