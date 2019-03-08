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

package com.viper.rest.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.glassfish.jersey.SslConfigurator;

import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.converters.Converters;
import com.viper.database.filters.Predicate;
import com.viper.database.model.ColumnParam;
import com.viper.database.model.LimitParam;
import com.viper.database.security.Encryptor;

public class RestStringClient {

    private MediaType acceptMediaType = MediaType.APPLICATION_JSON_TYPE;
    public static boolean debugOn = false;

    private String baseURL = System.getProperty("rest.service.url");
    private String authorizeURL = System.getProperty("authorize.service.url");
    private String sessionToken = null;
    private Client client = null;

    private boolean useSSL = false;
    private String trustStoreFile = null;
    private String trustStorePassword = null;
    private String keyStoreFile = null;
    private String keyPassword = null;

    public RestStringClient(String authorizeURL, String url) {
        if (url != null) {
            this.baseURL = url;
        }
        if (!this.baseURL.endsWith("/")) {
            this.baseURL = this.baseURL + "/";
        }
        if (authorizeURL != null) {
            this.authorizeURL = authorizeURL;
        }
        if (!this.authorizeURL.endsWith("/")) {
            this.authorizeURL = this.authorizeURL + "/";
        }
    }

    public void setSSLContext(String trustStoreFile, String trustStorePassword, String keyStoreFile, String keyPassword) {
        this.trustStoreFile = trustStoreFile;
        this.trustStorePassword = trustStorePassword;
        this.keyStoreFile = keyStoreFile;
        this.keyPassword = keyPassword;
        this.useSSL = true;
    }

    public void setAcceptMediaType(String type, String subtype, String key, String value) {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(key, value);

        MediaType mediaType = new MediaType(type, subtype, parameters);

        acceptMediaType = mediaType;
    }

    public Client getClient() throws Exception {
        if (client == null) {

            ClientBuilder clientBuilder = ClientBuilder.newBuilder();
            clientBuilder = clientBuilder.register(JohnzonProvider.class);
            // clientBuilder = clientBuilder.register(JacksonFeature.class);

            if (useSSL) {
                Encryptor encryptor = new Encryptor();
                SslConfigurator sslConfig = SslConfigurator.newInstance();
                sslConfig = sslConfig.trustStoreFile(trustStoreFile);
                sslConfig = sslConfig.trustStorePassword(encryptor.decrypt(trustStorePassword));

                SSLContext sslContext = sslConfig.createSSLContext();
                clientBuilder = clientBuilder.sslContext(sslContext);
                client = clientBuilder.build();

            } else {
                client = clientBuilder.build();
            }
        }
        return client;
    }

    public boolean authorize(String username, String password) throws Exception {

        String url = authorizeURL;
        try {
            WebTarget webTarget = getClient().target(url).queryParam("username", username).queryParam("password", password);

            Response response = webTarget.request(MediaType.TEXT_PLAIN).get();
            handleErrorResponse(webTarget, response);

            String str = response.readEntity(String.class);

            println(webTarget, response, str);

            response.close();

            return true; // TODO
        } catch (Exception ex) {
            throw new Exception("Failed to access " + url, ex);
        }
    }

    public String login(String username, String password) throws Exception {

        sessionToken = null;

        WebTarget webTarget = getClient().target(authorizeURL).path("login").queryParam("username", username)
                .queryParam("password", password);

        Response response = webTarget.request(acceptMediaType).get();
        handleErrorResponse(webTarget, response);

        String str = response.readEntity(String.class);

        println(webTarget, response, str);

        response.close();

        sessionToken = null; // getJsonField(str, "sessionToken");
        return sessionToken;
    }

    public String logout() throws Exception {

        WebTarget webTarget = getClient().target(authorizeURL).path("logout");

        Response response = webTarget.request(acceptMediaType).post(null);
        handleErrorResponse(webTarget, response);

        String str = response.readEntity(String.class);

        println(webTarget, response, str);

        response.close();

        sessionToken = null;

        return str;
    }

    public final void release() {

    }

    public <T> long size(Class<T> clazz) throws Exception {
        return 0L;
    }

    public final List<String> listDatabases() {
        List<String> items = new ArrayList<String>();
        return items;
    }

    public final List<String> listTables(String databaseName) {
        List<String> items = new ArrayList<String>();
        return items;
    }

    public final <T> List<String> listColumns(Class<T> clazz) {
        List<String> items = new ArrayList<String>();
        return items;
    }

    public <T> boolean hasChanged(Class<T> clazz) {
        return true;
    }

    public <T> void createDatabase(String packagename) throws Exception {

    }

    public final <T> void create(Class<T> clazz) throws Exception {

    }

    public <T> String query(Class<T> tableClass, Object... keyValue) throws Exception {

        WebTarget webTarget = null;
        if (keyValue.length > 2) {
            List<String> items = toList(keyValue);
            String path = tableClass.getSimpleName().toLowerCase();
            webTarget = getClient().target(baseURL).path(path).queryParam("where", items);

        } else if (keyValue.length == 2) {
            String path = tableClass.getSimpleName().toLowerCase() + "/" + keyValue[0] + "/" + keyValue[1];
            webTarget = getClient().target(baseURL).path(path);

        } else {
            return null;
        }

        Response response = webTarget.request(acceptMediaType).get();
        handleErrorResponse(webTarget, response);

        String item = response.readEntity(String.class);

        println(webTarget, response, "");
        response.close();

        return item;
    }

    public <T> String queryAll(Class<T> clazz) throws Exception {

        String path = clazz.getSimpleName().toLowerCase() + "/all";
        WebTarget webTarget = getClient().target(baseURL).path(path);

        Response response = webTarget.request(acceptMediaType).get();
        handleErrorResponse(webTarget, response);

        String json = response.readEntity(String.class);

        println(webTarget, response, json);

        return json;
    }

    public <T> String queryList(Class<T> clazz, Object... keyValue) throws Exception {

        String path = clazz.getSimpleName().toLowerCase() + "/list/" + keyValue[0] + "/" + keyValue[1];
        WebTarget webTarget = getClient().target(baseURL).path(path);

        Response response = webTarget.request(acceptMediaType).get();
        handleErrorResponse(webTarget, response);

        String json = response.readEntity(String.class);

        println(webTarget, response, json);

        return json;
    }

    public <T> String queryList(Class<T> clazz, Predicate<T> filter, List<ColumnParam> columnParams, LimitParam limitParam,
            Map<String, String> parameters) throws Exception {

        String str = queryAll(clazz);

        return str;
    }

    public <T> String update(T item) throws Exception {

        String path = item.getClass().getSimpleName().toLowerCase();
        WebTarget webTarget = getClient().target(baseURL).path(path);
        String requestStr = Converters.convert(String.class, item);

        Response response = webTarget.request(acceptMediaType).put(Entity.entity(requestStr, MediaType.TEXT_PLAIN_TYPE));
        handleErrorResponse(webTarget, response);

        String json = response.readEntity(String.class);
 
        response.close();

        return json;
    }

    public <T> String insert(T item) throws Exception {

        String path = item.getClass().getSimpleName().toLowerCase();
        WebTarget webTarget = getClient().target(baseURL).path(path);
        String requestStr = Converters.convert(String.class, item);

        Response response = webTarget.request(acceptMediaType).post(Entity.entity(requestStr, MediaType.TEXT_PLAIN_TYPE));
        handleErrorResponse(webTarget, response);

        String msg  = response.readEntity(String.class); 

        println(webTarget, response, msg);

        response.close();
        return msg;
    }

    public <T> void insertAll(List<T> beans) throws Exception {

        if (beans == null || beans.size() == 0) {
            return;
        }

        Class<T> clazz = (Class<T>) beans.get(0).getClass();
        String path = clazz.getSimpleName().toLowerCase() + "/list";
        String requestStr = Converters.convertFromList(beans);

        WebTarget webTarget = getClient().target(baseURL).path(path);
        Response response = webTarget.request(acceptMediaType).post(Entity.entity(requestStr, MediaType.TEXT_PLAIN_TYPE));
        handleErrorResponse(webTarget, response);

        if (response.getStatus() == 200) {
            String json = response.readEntity(String.class);
            println(webTarget, response, json);

            List<T> results = Converters.convertToList(clazz, json);
            // TODO transfer primary key to insert values.

        } else {

            String msg = response.readEntity(String.class);
            println(webTarget, response, msg);
        }

        response.close();
    }

    public <T> void delete(Class<T> tableClass, Object... keyValue) throws Exception {

        // TODO Need more for the value aside from toString, Arrays dont work so
        // well
        String key = (keyValue.length < 1 || keyValue[0] == null) ? "" : keyValue[0].toString();
        String val = (keyValue.length < 2 || keyValue[1] == null) ? "" : keyValue[1].toString();

        String path = tableClass.getSimpleName().toLowerCase() + "/" + key + "/" + val;
        WebTarget webTarget = getClient().target(baseURL).path(path);

        Response response = webTarget.request().delete();
        handleErrorResponse(webTarget, response);

        String msg = response.readEntity(String.class);

        println(webTarget, response, msg);

        response.close();
    }

    public <T> void delete(T bean) throws Exception {

        String path = bean.getClass().getSimpleName().toLowerCase() + "/" + DatabaseUtil.getPrimaryKeyName(bean.getClass()) + "/"
                + DatabaseUtil.getPrimaryKeyValue(bean);
        WebTarget webTarget = getClient().target(baseURL).path(path);

        Response response = webTarget.request().delete();
        handleErrorResponse(webTarget, response);

        String msg = response.readEntity(String.class);

        println(webTarget, response, msg);

        response.close();
    }

    public <T> void deleteAll(Class<T> tableClass) throws Exception {

        String path = tableClass.getSimpleName().toLowerCase() + "/all";
        WebTarget webTarget = getClient().target(baseURL).path(path);

        Response response = webTarget.request(acceptMediaType).delete();
        handleErrorResponse(webTarget, response);

        String msg = response.readEntity(String.class);

        println(webTarget, response, msg);

        response.close();
    }

    public <T> List<Object> uniqueValues(Class<T> tableClass, String fieldname) throws Exception {
        return new ArrayList<Object>();
    }

    private List<String> toList(Object[] pairs) {
        List<String> items = new ArrayList<String>();
        for (Object pair : pairs) {
            items.add(pair.toString());
        }
        return items;
    }

    private void handleErrorResponse(WebTarget webTarget, Response response) throws Exception {
        if (response.getStatus() != 200) {
            String msg = response.readEntity(String.class);
            throw new Exception("queryAll: " + webTarget.getUri().toString() + ", " + response.getStatus() + ":" + msg);
        }
    }

    public final void get(String url) throws Exception {

        WebTarget webTarget = getClient().target(baseURL).path(url);

        String msg = null;
        Response response = webTarget.request(acceptMediaType).get();
        if (response.getStatus() != 200) {
            msg = response.readEntity(String.class);
        }

        println(webTarget, response, msg);

        response.close();
    }

    private void println(WebTarget target, Response response, String json) {
        if (debugOn) {
            System.out.println("Path: " + target.getUri());
            System.out.println("Status: " + response.getStatus());
            System.out.println("JSON: " + json);
        }
    }
}
