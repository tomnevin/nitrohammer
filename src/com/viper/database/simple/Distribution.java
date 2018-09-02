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

import java.util.ArrayList;

/**
 * 
 * @author tnevin
 * 
 */

public class Distribution extends ArrayList<Distribution> {

	int rowno = 0;
	int count = 0;

	public Distribution(int numberOfBuckets) {
		super(numberOfBuckets);
		for (int i = 0; i < numberOfBuckets; i++) {
			add(new Distribution(i, 1));
		}
	}

	private Distribution(int rowno, int count) {
		this.rowno = rowno;
		this.count = count;
	}

	public void setDistributionBucket(int bucketNo, int rowno, int count) {
		Distribution c = (Distribution) get(bucketNo);
		c.rowno = rowno;
		c.count = count;
	}

	public int getTotalDistribution() {
		int counter = 0;
		for (Distribution c : this) {
			counter = counter + c.count;
		}
		return counter;
	}

	public int getRowNumber(int distributon) {
		int rowno = -1;
		int total = getTotalDistribution();
		distributon = distributon % total;
		for (Distribution c : this) {
			distributon = distributon - c.count;
			rowno = c.rowno;
			if (distributon < 0) {
				return c.rowno;
			}
		}
		return rowno;
	}
}