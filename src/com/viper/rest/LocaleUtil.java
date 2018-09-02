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

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

public class LocaleUtil {

	public static ResourceBundle getBundle(Locale locale) {
		ResourceBundle resources = ResourceBundle.getBundle("/lang/resources", locale);
		return resources;
	}

	public static String getString(ResourceBundle bundle, String key) {
		if (key != null) {
			String keyLC = key.toLowerCase();
			if (bundle.containsKey(keyLC)) {
				return bundle.getString(keyLC);
			}
		}
		return key;
	}

	public static String getString(ResourceBundle bundle, String prefix, String key) {
		if (prefix != null && key != null) {
			String keyLC = (prefix +"." + key).toLowerCase();
			if (bundle.containsKey(keyLC)) {
				return bundle.getString(keyLC);
			}
		}
		return key;
	}

	public static Locale getCurrentLocale(HttpServletRequest request) {
		Locale locale = Locale.getDefault();
		if (request != null) {
			locale = request.getLocale();
		}
		return locale;
	}
}