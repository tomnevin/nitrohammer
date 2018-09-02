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

package com.viper.database.dao;

import java.io.IOException;
import java.io.Writer;

public class DatabaseWriter extends Writer {

	private StringBuilder out = new StringBuilder();
	private DatabaseSQLInterface dao;

	public DatabaseWriter(DatabaseSQLInterface dao) {
		this.dao = dao;
	}

	@Override
	public void write(char buf[], int off, int len) throws IOException {
		if (len == 1 && buf[off] == '\n') {
			writeln();
		} else {
			out.append(buf, off, len);
		}
	}

	@Override
	public void write(int c) throws IOException {
		out.append(c);
	}

	@Override
	public void write(String str) {
		out.append(str);
	}

	@Override
	public void flush() throws IOException {
		if (out.length() > 0) {
			writeln();
		}
	}

	@Override
	public void close() throws IOException {

	}

	public void writeln(String str) throws IOException {
		out.append(str);
		writeln();
	}

	public void writeln() throws IOException {
		try {
			writedao(out.toString());
		} finally {
			out.setLength(0);
		}
	}

	private void writedao(String str) throws IOException {
		try {
			dao.write(str);
		} catch (Exception e) {
			System.err.println("DatabaseWriter: DAO write: " + str);
			throw new IOException(e);
		}
	}
}