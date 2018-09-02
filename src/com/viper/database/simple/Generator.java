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

package com.viper.database.simple;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * @author tnevin
 */

public class Generator {

	private final static int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

	private String listDirectory = "";

	private final Random random = new Random(123456789);
	private Map<String, List<String>> cache = new HashMap<String, List<String>>();
	private static String externalRefs = "ABCDEF0123456789012345678901234567890123456789";
	private static String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz -_+=0123456789";

	public Generator(String listDirectory) {
		this.listDirectory = listDirectory;
	}

	public int randomInt(int minValue, int maxValue) {
		return random.nextInt(maxValue - minValue) + minValue;
	}

	public int randomIntByUnits(int minValue, int maxValue, int rounding) {
		int value = randomInt(minValue, maxValue);
		return value - value % rounding;
	}

	public Row randomRow(List<Row> list) {
		if (list == null || list.size() == 0) {
			return null;
		}
		return list.get(random.nextInt(list.size()));
	}

	public Row randomRowUnique(List<Row> list) {
		if (list == null || list.size() == 0) {
			return null;
		}
		return list.remove(random.nextInt(list.size()));
	}

	public BigInteger randomBigInteger(long minValue, long maxValue) {
		return BigInteger.valueOf((long) (random.nextDouble() * (maxValue - minValue) + minValue));
	}

	public BigDecimal randomBigDecimal(double minValue, double maxValue, int decimalDigits) {
		return new BigDecimal(random.nextDouble() * (maxValue - minValue) + minValue, new MathContext(decimalDigits));
	}

	public BigDecimal randomBigDecimalByUnits(double minValue, double maxValue, double rounding) {
		return randomBigDecimal(minValue, maxValue, 2); // TODO remove hard-code
														// decimal size
	}

	// -------------------------------------------------------------------------

	public String randomHoursMinutes(int minHours, int maxHours) {
		int hours = randomInt(minHours, maxHours);
		int minutes = randomInt(0, 59);
		String hrs = (hours == 0) ? "" : "" + hours;
		String mns = (minutes < 10) ? "0" + minutes : "" + minutes;
		return hrs + ":" + mns;
	}

	public String randomSize(int minWidth, int maxWidth, int minHeight, int maxHeight) {
		int width = randomInt(minWidth, maxWidth);
		int height = randomInt(minHeight, maxHeight);
		return width + " x " + height;
	}

	public String randomAspectRatio(int minWidth, int maxWidth, int minHeight, int maxHeight) {
		int width = randomInt(minWidth, maxWidth);
		int height = randomInt(minHeight, maxHeight);
		return width + ":" + height;
	}

	public java.sql.Time randomTime(int minSeconds, int maxSeconds, int modSeconds) {
		int seconds = randomInt(minSeconds, maxSeconds);
		seconds = seconds - (seconds % modSeconds);
		return new java.sql.Time(seconds * 1000);
	}

	// -------------------------------------------------------------------------

	public java.sql.Timestamp randomTimestamp(int minDays, int maxDays) {
		return randomTimestamp(System.currentTimeMillis(), minDays, maxDays);
	}

	public java.sql.Timestamp randomTimestamp(java.sql.Timestamp startDate, int minDays, int maxDays) {
		return randomTimestamp(startDate.getTime(), minDays, maxDays);
	}

	public java.sql.Timestamp randomTimestamp(long startTime, int minDays, int maxDays) {
		long startDays = startTime / MILLISECONDS_PER_DAY;
		long dtime = (startDays + random.nextInt(maxDays - minDays) + minDays) * MILLISECONDS_PER_DAY;
		dtime = dtime + randomInt(0, MILLISECONDS_PER_DAY);

		return new java.sql.Timestamp(dtime);
	}

	// -------------------------------------------------------------------------

	public java.sql.Date randomDate(int minDays, int maxDays) {
		return randomDate(System.currentTimeMillis(), minDays, maxDays);
	}

	public java.sql.Date randomDate(java.sql.Date startDate, int minDays, int maxDays) {
		return randomDate(startDate.getTime(), minDays, maxDays);
	}

	public java.sql.Date randomDate(java.sql.Date startDate, java.sql.Date endDate) {
		long startTime = startDate.getTime();
		int maxDays = (int) ((endDate.getTime() - startTime) / MILLISECONDS_PER_DAY);

		return randomDate(startTime, 0, maxDays);
	}

	public java.sql.Date randomDate(long startTime, int minDays, int maxDays) {
		long startDays = startTime / MILLISECONDS_PER_DAY;
		long dtime = (startDays + random.nextInt(maxDays - minDays) + minDays) * MILLISECONDS_PER_DAY;

		return new java.sql.Date(dtime);
	}

	// -------------------------------------------------------------------------

	public String randomPhone() {
		int areacode = randomInt(100, 999);
		int prefix = randomInt(100, 999);
		int number = randomInt(1000, 9999);
		return "1-" + areacode + "-" + prefix + "-" + number;
	}

	public String randomEmail(String name, String companyname) {
		StringBuffer buf = new StringBuffer();
		String str = name + "@" + companyname + ".com";
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isLetterOrDigit(c) || c == '.' || c == '@') {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	public String randomExternalRef(int grouping, int length) {
		StringBuffer buffer = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			if ((i % grouping) == (grouping - 1)) {
				buffer.append("-");
			} else {
				buffer.append(externalRefs.charAt(randomInt(0, externalRefs.length())));
			}
		}
		return buffer.toString();
	}

	public String randomString(int length) {
		StringBuffer buffer = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			buffer.append(characters.charAt(randomInt(0, characters.length())));
		}
		return buffer.toString();
	}

	public int randomList(int list[]) {
		return list[randomInt(0, list.length)];
	}

	public String randomList(String list[]) {
		return list[randomInt(0, list.length)];
	}

	public String randomList(List<String> list) {
		return list.get(randomInt(0, list.size()));
	}

	public String randomListFromFile(String filename) {
		return randomList(getListFromFile(filename, false));
	}

	public String randomListUnique(List<String> list) {
		if (list == null || list.size() == 0) {
			return null;
		}
		return list.remove(randomInt(0, list.size()));
	}

	// -------------------------------------------------------------------------

	public String nextList(List<String> list, int index) {
		index = index % list.size();
		return list.get(index);
	}

	public Row nextRow(List<Row> list, int index) {
		index = index % list.size();
		return list.get(index);
	}

	public Row nextRowByDistribution(List<Row> list, Distribution distribution, int index) {
		int rowNumber = distribution.getRowNumber(index);
		if (rowNumber != -1) {
			return nextRow(list, rowNumber);
		}
		return randomRow(list);
	}

	public Row randomRowByDistribution(List<Row> list, Distribution distribution) {
		int total = distribution.getTotalDistribution();
		int index = random.nextInt(total);
		return nextRowByDistribution(list, distribution, index);
	}

	public String nextListFromFile(String filename, int index) {
		return nextList(getListFromFile(filename, false), index);
	}

	// -------------------------------------------------------------------------

	public int listSizeFromFile(String filename) {
		return getListFromFile(filename, false).size();
	}

	public String getFieldFromCSV(String str, int fieldno) {
		StringTokenizer tokens = new StringTokenizer(str, ",", false);
		String token = "";
		while (tokens.hasMoreTokens()) {
			fieldno = fieldno - 1;
			token = tokens.nextToken();
			if (fieldno <= 0) {
				break;
			}
		}
		return token.trim();
	}

	public List<String> getListFromFile(String filename, boolean doCopy) {
		filename = listDirectory + "/" + filename;
		List<String> list = cache.get(filename);
		if (list == null) {
			list = readFileViaLines(filename);
			cache.put(filename, list);
		}
		if (doCopy) {
			List<String> clone = new ArrayList<String>();
			for (int i = 0; i < list.size(); i++) {
				clone.add(list.get(i));
			}
			list = clone;
		}
		return list;
	}

	/**
	 * @param filename
	 * @return
	 */

	private List<String> readFileViaLines(String filename) {
		List<String> list = new ArrayList<String>();
		BufferedReader f = null;
		try {
			f = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
			while (f.ready()) {
				String str = f.readLine();
				if (str == null || str.length() == 0) {
					break;
				}
				list.add(str);
			}
			f.close();
		} catch (IOException e) {
			System.err.println("ERROR openFile: " + filename);
			e.printStackTrace(System.err);
		}
		return list;
	}
}