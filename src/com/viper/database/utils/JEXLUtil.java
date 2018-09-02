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

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.UnifiedJEXL;

//Unified JEXL
//$$ for(var x : [1, 3, 5, 42, 169]) {
//$$   if (x == 42) {
//Life, the universe, and everything
//$$   } else if (x > 42) {
//The value $(x} is over fourty-two
//$$   } else {
//The value ${x} is under fourty-two
//$$   }
//$$ }

public class JEXLUtil {

    private static JEXLUtil instance = new JEXLUtil();

    private JEXLUtil() {

    }

    /**
     * 
     * @return
     */
    public static JEXLUtil getInstance() {
        return instance;
    }

    /**
     * 
     * Given the expression and a map of parameters, evaluate the expression returning the result.
     * 
     * @param expression
     *            the JEXL expression e.g. (A + B / C)
     * 
     * @param bean
     *            the key value map if parameters (A = 2, B = 4, C = 2)
     * 
     * @return the result of evaluating the expression against the parameters e.g.( 4 )
     * 
     * @throws Exception
     */
    public <T> Object evaluate(String expression, T bean) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("bean", bean);

        return evaluate1(bugFixExpression(expression), params);
    }

    /**
     * 
     * Given the expression and a map of parameters, evaluate the expression returning the result.
     * 
     * @param expression
     *            the JEXL expression e.g. (A + B / C)
     * 
     * @param params
     *            the key value map if parameters (A = 2, B = 4, C = 2)
     * 
     * @return the result of evaluating the expression against the parameters e.g.( 4 )
     * 
     * @throws Exception
     */
    public String evaluate(String expression, Map<String, Object> params) throws Exception {
        return evaluate(expression, params, null);
    }

    /**
     * 
     * Given the expression and a map of parameters, evaluate the expression returning the result.
     * 
     * @param expression
     *            the JEXL expression e.g. (A + B / C)
     * 
     * @param params
     *            the key value map if parameters (A = 2, B = 4, C = 2)
     * 
     * @return the result of evaluating the expression against the parameters e.g.( 4 )
     * 
     * @throws Exception
     */

    public String evaluate(String expression, Map<String, Object> params, String defaultValue) throws Exception {
        Object result = evaluate1(bugFixExpression(expression), params);
        return (result == null) ? defaultValue : result.toString();
    }

    public Object evaluate1(String expression, Map<String, Object> params) throws Exception {

        StringWriter writer = new StringWriter();
        params.put("writer", writer);

        JexlContext context = createContext(params);
        JexlEngine engine = new JexlEngine();
        engine.setDebug(true);
        engine.setSilent(false);

        StringReader source = new StringReader(expression);
        UnifiedJEXL.Template template = new UnifiedJEXL(engine).createTemplate("##", source);
        template.evaluate(context, writer);
        return writer.toString();
    }

    /**
     * 
     * @param expression
     * @param params
     * @return
     * @throws Exception
     */
    public Object eval(String expression, Map<String, Object> params) throws Exception {

        if (expression == null) {
            return null;
        }

        try {
            JexlEngine engine = new JexlEngine();
            engine.setDebug(true);
            engine.setSilent(false);

            JexlContext context = createContext(params);
            return engine.createExpression(expression).evaluate(context);
        } catch (Throwable t) {
            throw new Exception("ERROR: failed to parse expression " + expression, t);
        }
    }

    /**
     * 
     * @param expression
     * @return
     */
    public String bugFixExpression(String expression) {

        // BugFix for UnifiedJexl, exists in JEXL.1.2.1, fixed in next release.
        expression = expression.replaceAll("[\\t]", "    ");
        expression = expression.replaceAll("(?m)^[ \\t]*$", "#{_jexl.noop();}");
        expression = expression.replaceAll("(?m)^[ \\t]+[#][#]", "##");
        expression = expression.replaceAll("(?m)^(.+)$", "$1\n##");
        return expression;
    }

    /**
     * 
     * @param namespace
     * @return
     */

    private JexlContext createContext(Map<String, Object> namespace) {

        JexlContext context = new MapContext();
        context.set("String", java.lang.String.class);
        context.set("Context", context);
        context.set("util", this);
        context.set("_jexl", this);
        context.set("this", this);

        if (namespace != null) {
            for (String key : namespace.keySet()) {
                context.set(key, namespace.get(key));
            }
        }
        return context;
    }

    /**
     * 
     * @param filename
     * @param writer
     * @throws Exception
     */
    public void write(String filename, StringWriter writer) throws Exception {

        File file = new File(filename);
        file.getParentFile().mkdirs();
        Files.write(file.toPath(), writer.toString().getBytes());
        writer.getBuffer().setLength(0);
    }

    /**
     * 
     * @param filename
     * @param writer
     * @throws Exception
     */
    public String insert(String filename) throws Exception {

        File file = new File(filename);
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes);
    }

    /**
     * 
     * @param writer
     */
    public void newline(StringWriter writer) {
        writer.write("\n");
    }

    /**
     * 
     */
    public void noop() {
    }

    /**
     * 
     * @return
     */
    public String YYYYMMDD() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd");
        return format1.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 
     * @return
     */
    public String YYYY() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy");
        return format1.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 
     * @param str
     */
    public void print(String str) {
        System.out.println("print: " + str);
    }
}
