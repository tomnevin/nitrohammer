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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class HttpServletRequestImpl implements HttpServletRequest {

    private final Map<String, String> headers = new HashMap<String, String>();
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private final Map<String, String[]> parameters = new HashMap<String, String[]>();

    @Override
    public AsyncContext getAsyncContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getAttribute(String arg0) {
        return attributes.get(arg0);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getContentLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLocalPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String arg0) {
        return parameters.get(arg0);
    }

    @Override
    public String getProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRealPath(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteHost() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRemotePort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getScheme() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getServerPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeAttribute(String arg0) {
        attributes.remove(arg0);
    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
        attributes.put(arg0, arg1);
    }

    @Override
    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub

    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getAuthType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContextPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getDateHeader(String arg0) {
        return Long.parseLong(getHeader(arg0));
    }

    @Override
    public String getHeader(String arg0) {
        return headers.get(arg0);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public Enumeration<String> getHeaders(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getIntHeader(String arg0) {
        return Integer.parseInt(getHeader(arg0));
    }

    @Override
    public String getMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Part getPart(String arg0) throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPathInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPathTranslated() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getQueryString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteUser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestURI() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServletPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpSession getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpSession getSession(boolean arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUserInRole(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void login(String arg0, String arg1) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void logout() throws ServletException {
        // TODO Auto-generated method stub

    }

    public void setHeader(String arg0, String arg1) {
        headers.put(arg0, arg1);
    }

    public void setAttribute(String arg0, String arg1) {
        attributes.put(arg0, arg1);
    }
}
