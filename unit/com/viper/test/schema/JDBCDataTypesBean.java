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

package com.viper.test.schema;

import com.viper.database.annotations.Column;
import com.viper.database.annotations.Table;

@SuppressWarnings("serial")
@Table(databaseName = "test", name = "JDBCDataTypesBean", tableType = "table", iterations = 0)
public class JDBCDataTypesBean implements java.io.Serializable {

	private boolean booleanField = true;
	private byte byteField = 11;
	private short shortField = 12;
	private Integer integerField = 13;
	private Float floatField = 1.23450F;
	private Double doubleField = 123.45670;
	private char charField = 'Z';
	private String stringField = "Z";
	private java.sql.Date dateField = java.sql.Date.valueOf("2005-05-14"); // yyyy-[m]m-[d]d
	private java.sql.Time timeField = java.sql.Time.valueOf("11:23:21");
	private com.viper.demo.beans.model.enums.MyColor myColorField = new com.viper.demo.beans.model.enums.MyColor("RED");
	private java.sql.Timestamp timeStampField = java.sql.Timestamp.valueOf("1991-12-07 08:30:00");
	private java.math.BigInteger bigIntegerField = java.math.BigInteger.valueOf(450L);
	private java.math.BigDecimal bigDecimalField = java.math.BigDecimal.valueOf(1234567.00000);

	private int id;

	@Column(field = "id", name = "id", primaryKey = true, idMethod = "autoincrement", required = true, size = 10)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(field = "booleanField", name = "booleanField", size = 10)
	public boolean isBooleanField() {
		return booleanField;
	}

	public void setBooleanField(boolean booleanField) {
		this.booleanField = booleanField;
	}

	@Column(field = "byteField", name = "byteField")
	public byte getByteField() {
		return byteField;
	}

	public void setByteField(byte byteField) {
		this.byteField = byteField;
	}

	@Column(field = "shortField", name = "shortField")
	public short getShortField() {
		return shortField;
	}

	public void setShortField(short shortField) {
		this.shortField = shortField;
	}

	@Column(field = "integerField", name = "integerField")
	public Integer getIntegerField() {
		return integerField;
	}

	public void setIntegerField(Integer integerField) {
		this.integerField = integerField;
	}

	@Column(field = "floatField", name = "floatField")
	public Float getFloatField() {
		return floatField;
	}

	public void setFloatField(Float floatField) {
		this.floatField = floatField;
	}

	@Column(field = "doubleField", name = "doubleField")
	public Double getDoubleField() {
		return doubleField;
	}

	public void setDoubleField(Double doubleField) {
		this.doubleField = doubleField;
	}

	@Column(field = "charField", name = "charField")
	public char getCharField() {
		return charField;
	}

	public void setCharField(char charField) {
		this.charField = charField;
	}

	@Column(field = "stringField", name = "stringField")
	public String getStringField() {
		return stringField;
	}

	public void setStringField(String stringField) {
		this.stringField = stringField;
	}

	@Column(field = "dateField", name = "dateField")
	public java.sql.Date getDateField() {
		return dateField;
	}

	public void setDateField(java.sql.Date dateField) {
		this.dateField = dateField;
	}

	@Column(field = "timeField", name = "timeField")
	public java.sql.Time getTimeField() {
		return timeField;
	}

	public void setTimeField(java.sql.Time timeField) {
		this.timeField = timeField;
	}

	@Column(field = "myColorField", name = "myColorField")
	public com.viper.demo.beans.model.enums.MyColor getMyColorField() {
		return myColorField;
	}

	public void setMyColorField(com.viper.demo.beans.model.enums.MyColor myColorField) {
		this.myColorField = myColorField;
	}

	@Column(field = "timeStampField", name = "timeStampField")
	public java.sql.Timestamp getTimeStampField() {
		return timeStampField;
	}

	public void setTimeStampField(java.sql.Timestamp timeStampField) {
		this.timeStampField = timeStampField;
	}

	@Column(field = "bigIntegerField", name = "bigIntegerField")
	public java.math.BigInteger getBigIntegerField() {
		return bigIntegerField;
	}

	public void setBigIntegerField(java.math.BigInteger bigIntegerField) {
		this.bigIntegerField = bigIntegerField;
	}

	@Column(field = "bigDecimalField", name = "bigDecimalField")
	public java.math.BigDecimal getBigDecimalField() {
		return bigDecimalField;
	}

	public void setBigDecimalField(java.math.BigDecimal bigDecimalField) {
		this.bigDecimalField = bigDecimalField;
	}
}
