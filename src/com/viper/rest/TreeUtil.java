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
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONObject;

import com.viper.database.utils.FileUtil;

public class TreeUtil {

	private static final String DependencyDirectory = "res:/templates/";
	private static Map<String, JSONObject> cache = new HashMap<String, JSONObject>();

	public final static String getTree(Locale locale, String treename) {

		if (treename == null) {
			return "";
		}

		ResourceBundle resources = LocaleUtil.getBundle(locale);

		String filename = makeTreeFilename(treename);
		JSONObject tree = loadTree(filename);

		return tree.toString();
	}

	private final static String makeTreeFilename(String formname) {
		return DependencyDirectory + "trees/" + formname.toLowerCase() + ".json";
	}

	private final static JSONObject loadTree(String filename) {
		if (cache.containsKey(filename)) {
			return cache.get(filename);
		}

		try {
			String str = FileUtil.readFile(filename);
			JSONObject tree = new JSONObject(str);

			cache.put(filename, tree);

			return tree;
		} catch (Exception ex) {
			System.out.println("ERROR: filename=" + filename);
			ex.printStackTrace();
		}

		return new JSONObject();
	}
}