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

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JAXBUtil {

	private final static Hashtable<String, JAXBContext> cache = new Hashtable<String, JAXBContext>();

	public final static <T> Marshaller createXmlMarshaller(Class<T> clazz, Map<String, Object> properties)
			throws Exception {

		Marshaller m = getJAXBContextXml(clazz).createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		if (properties != null) {
			for (Entry<String, Object> entry : properties.entrySet()) {
				m.setProperty(entry.getKey(), entry.getValue());
			}
		}
		return m;
	}

	public final static <T> Unmarshaller createXmlUnmarshaller(Class<T> clazz, Map<String, Object> properties)
			throws Exception {

		Unmarshaller unmarshaller = getJAXBContextXml(clazz).createUnmarshaller();
		if (properties != null) {
			for (Entry<String, Object> entry : properties.entrySet()) {
				unmarshaller.setProperty(entry.getKey(), entry.getValue());
			}
		}
		return unmarshaller;
	}
	
	private final static <T> JAXBContext getJAXBContextXml(Class<T> clazz) throws Exception {
		String key1 = clazz.getPackage().getName();
		JAXBContext context = cache.get(key1);
		if (context != null) {
			return context;
		}
		String key2 = clazz.getName();
		context = cache.get(key2);
		if (context != null) {
			return context;
		}
		if (classExists(clazz.getPackage().getName() + ".ObjectFactory")) {
			context = JAXBContext.newInstance(clazz.getPackage().getName());
			cache.put(key1, context);
		} else {
			context = JAXBContext.newInstance(clazz);
			cache.put(key2, context);
		}
		if (context == null) {
			throw new Exception("No JAXBContext XML for package " + clazz.getName());
		}
		return context;
	}

	private final static boolean classExists(String classname) {
		try {
			return (Class.forName(classname) != null);
		} catch (Exception ex) {
			return false;
		}
	}
}
