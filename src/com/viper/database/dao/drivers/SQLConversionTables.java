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

package com.viper.database.dao.drivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SQLConversionTables {
    
    private final static Logger log = Logger.getLogger(SQLConversionTables.class.getName());

    public static class SQLConversionItem {

        Class javaClass;
        String dataType;

        public SQLConversionItem(Class javaClass, String dataType) {
            this.javaClass = javaClass;
            this.dataType = dataType;
        }
    }

    public final static List<String> getJavaTypes() {
        return new ArrayList<String>(builtInMap.keySet());
    }

    public final static List<String> getDataTypes() {
        return new ArrayList<String>(DataTypeMapping.keySet());
    }

    public static class ConversionType {

        String nullTypeName;
        String noNullTypeName;

        public ConversionType(String nullTypeName, String noNullTypeName) {
            this.nullTypeName = nullTypeName;
            this.noNullTypeName = noNullTypeName;
        }
    }

    // List of Java primitives for reference
    // byte 0
    // short 0
    // int 0
    // long 0L
    // float 0.0f
    // double 0.0d
    // char '\u0000'
    // String (or any object) null
    // boolean false

    private final static List<SQLConversionItem> toDataType = new ArrayList<SQLConversionItem>();

    static {
        toDataType.add(new SQLConversionItem(byte.class, "tinyint(<size>)"));
        toDataType.add(new SQLConversionItem(byte[].class, "blob"));
        toDataType.add(new SQLConversionItem(Byte.class, "tinyint(<size>)"));
        toDataType.add(new SQLConversionItem(Byte[].class, "blob"));
        toDataType.add(new SQLConversionItem(boolean.class, "tinyint(<size>)"));
        toDataType.add(new SQLConversionItem(boolean[].class, "blob"));
        toDataType.add(new SQLConversionItem(Boolean.class, "tinyint(<size>)"));
        toDataType.add(new SQLConversionItem(Boolean[].class, "blob"));
        toDataType.add(new SQLConversionItem(char.class, "varchar(1)"));
        toDataType.add(new SQLConversionItem(char[].class, "varchar(<size>)"));
        toDataType.add(new SQLConversionItem(Character.class, "char(1)"));
        toDataType.add(new SQLConversionItem(Character[].class, "varchar(<size>)"));
        toDataType.add(new SQLConversionItem(double.class, "double(<size>,<decimal>)"));
        toDataType.add(new SQLConversionItem(double[].class, "blob"));
        toDataType.add(new SQLConversionItem(Double.class, "double(<size>,<decimal>)"));
        toDataType.add(new SQLConversionItem(Double[].class, "blob"));
        toDataType.add(new SQLConversionItem(float.class, "float(<size>,<decimal>)"));
        toDataType.add(new SQLConversionItem(float[].class, "blob"));
        toDataType.add(new SQLConversionItem(Float.class, "float(<size>,<decimal>)"));
        toDataType.add(new SQLConversionItem(Float[].class, "blob"));
        toDataType.add(new SQLConversionItem(int.class, "int(<size>)"));
        toDataType.add(new SQLConversionItem(int[].class, "blob"));
        toDataType.add(new SQLConversionItem(Integer.class, "int(<size>)"));
        toDataType.add(new SQLConversionItem(Integer[].class, "blob"));
        toDataType.add(new SQLConversionItem(long.class, "bigint"));
        toDataType.add(new SQLConversionItem(long[].class, "blob"));
        toDataType.add(new SQLConversionItem(Long.class, "bigint"));
        toDataType.add(new SQLConversionItem(Long[].class, "blob"));
        toDataType.add(new SQLConversionItem(short.class, "smallint"));
        toDataType.add(new SQLConversionItem(short[].class, "blob"));
        toDataType.add(new SQLConversionItem(Short.class, "smallint"));
        toDataType.add(new SQLConversionItem(Short[].class, "blob"));
        toDataType.add(new SQLConversionItem(String.class, "varchar(<size>)"));
        toDataType.add(new SQLConversionItem(String[].class, "text"));
        toDataType.add(new SQLConversionItem(Enum.class, "enum"));
        toDataType.add(new SQLConversionItem(Enum[].class, "blob"));

        toDataType.add(new SQLConversionItem(java.math.BigInteger.class, "bigint(<size>)"));
        toDataType.add(new SQLConversionItem(java.math.BigDecimal.class, "decimal(<size>,<decimal>)"));
        toDataType.add(new SQLConversionItem(java.sql.Array.class, "varchar(<size>)"));
        toDataType.add(new SQLConversionItem(java.sql.Blob.class, "blob"));
        toDataType.add(new SQLConversionItem(java.sql.Clob.class, "text"));
        toDataType.add(new SQLConversionItem(java.sql.Date.class, "date"));
        toDataType.add(new SQLConversionItem(java.sql.NClob.class, "longtext"));
        toDataType.add(new SQLConversionItem(java.sql.Ref.class, "varchar(<size>)"));
        toDataType.add(new SQLConversionItem(java.sql.Struct.class, "blob"));
        toDataType.add(new SQLConversionItem(java.sql.Time.class, "time"));
        toDataType.add(new SQLConversionItem(java.sql.Timestamp.class, "timestamp"));
        toDataType.add(new SQLConversionItem(java.util.Date.class, "date"));
        toDataType.add(new SQLConversionItem(java.util.List.class, "text(<size>)"));
        toDataType.add(new SQLConversionItem(java.util.Map.class, "text(<size>)"));
    }

    private final static Map<String, Class> builtInMap = new HashMap<String, Class>();

    static {
        builtInMap.put("boolean", Boolean.TYPE);
        builtInMap.put("byte", Byte.TYPE);
        builtInMap.put("char", Character.TYPE);
        builtInMap.put("double", Double.TYPE);
        builtInMap.put("float", Float.TYPE);
        builtInMap.put("int", Integer.TYPE);
        builtInMap.put("long", Long.TYPE);
        builtInMap.put("short", Short.TYPE);
        builtInMap.put("void", Void.TYPE);
        builtInMap.put("Boolean", Boolean.class);
        builtInMap.put("Byte", Byte.class);
        builtInMap.put("Character", String.class);
        builtInMap.put("Double", Double.class);
        builtInMap.put("Float", Float.class);
        builtInMap.put("Integer", Integer.class);
        builtInMap.put("Long", Long.class);
        builtInMap.put("Short", Short.class);
        builtInMap.put("String", String.class);

        builtInMap.put("boolean[]", boolean[].class);
        builtInMap.put("byte[]", byte[].class);
        builtInMap.put("char[]", String.class);
        builtInMap.put("double[]", double[].class);
        builtInMap.put("float[]", float[].class);
        builtInMap.put("int[]", int[].class);
        builtInMap.put("long[]", long[].class);
        builtInMap.put("short[]", short[].class);

        builtInMap.put("Date", java.sql.Date.class);
        builtInMap.put("Time", java.sql.Time.class);
        builtInMap.put("Timestamp", java.sql.Timestamp.class);
    }

    public final static Map<String, ConversionType> DataTypeMapping = new HashMap<String, ConversionType>();

    static {
        DataTypeMapping.put("array", null);
        DataTypeMapping.put("bit", new ConversionType("Boolean", "boolean"));
        DataTypeMapping.put("boolean", new ConversionType("Boolean", "boolean"));
        DataTypeMapping.put("bigint", new ConversionType("Long", "long"));
        DataTypeMapping.put("binary", new ConversionType("Byte[]", "byte[]"));
        DataTypeMapping.put("blob", new ConversionType("java.sql.Blob", "java.sql.Blob"));
        DataTypeMapping.put("char", new ConversionType("String", "String"));
        DataTypeMapping.put("clob", new ConversionType("java.sql.Clob", "java.sql.Clob"));
        DataTypeMapping.put("datalink", null);
        DataTypeMapping.put("date", new ConversionType("Long", "long"));
        DataTypeMapping.put("datetime", new ConversionType("Long", "long"));
        DataTypeMapping.put("decimal", new ConversionType("Double", "double"));
        DataTypeMapping.put("distinct", null);
        DataTypeMapping.put("double", new ConversionType("Double", "double"));
        DataTypeMapping.put("enum", new ConversionType("enum", "enum"));
        DataTypeMapping.put("float", new ConversionType("Float", "float"));
        DataTypeMapping.put("int", new ConversionType("Integer", "int"));
        DataTypeMapping.put("integer", new ConversionType("Integer", "int"));
        DataTypeMapping.put("longblob", new ConversionType("Byte[]", "byte[]"));
        DataTypeMapping.put("longnvarchar", new ConversionType("String", "String"));
        DataTypeMapping.put("longtext", new ConversionType("String", "String"));
        DataTypeMapping.put("longvarchar", new ConversionType("String", "String"));
        DataTypeMapping.put("longvarbinary", new ConversionType("Byte[]", "byte[]"));
        DataTypeMapping.put("mediumblob", new ConversionType("Byte[]", "byte[]"));
        DataTypeMapping.put("mediumint", new ConversionType("Integer", "int"));
        DataTypeMapping.put("mediumtext", new ConversionType("String", "String"));
        DataTypeMapping.put("nchar", new ConversionType("String", "String"));
        DataTypeMapping.put("null", null);
        DataTypeMapping.put("numeric", null);
        DataTypeMapping.put("nvarchar", new ConversionType("String", "String"));
        DataTypeMapping.put("nclob", new ConversionType("String", "String"));
        DataTypeMapping.put("other", null);
        DataTypeMapping.put("object", new ConversionType("Object", "Object"));
        DataTypeMapping.put("real", new ConversionType("Double", "double"));
        DataTypeMapping.put("ref", null);
        DataTypeMapping.put("rowid", null);
        DataTypeMapping.put("set", new ConversionType("Object", "Object"));
        DataTypeMapping.put("smallint", new ConversionType("Integer", "int"));
        DataTypeMapping.put("sqlxml", null);
        DataTypeMapping.put("struct", null);
        DataTypeMapping.put("time", new ConversionType("Long", "long"));
        DataTypeMapping.put("timestamp", new ConversionType("Long", "long"));
        DataTypeMapping.put("tinyint", new ConversionType("Integer", "int"));
        DataTypeMapping.put("tinytext", new ConversionType("String", "String"));
        DataTypeMapping.put("text", new ConversionType("String", "String"));
        DataTypeMapping.put("varchar", new ConversionType("String", "String"));
        DataTypeMapping.put("varbinary", new ConversionType("Byte[]", "byte[]"));
    }

    private final static Map<String, String> JavaClassnameMap = new HashMap<String, String>();

    static {
        JavaClassnameMap.put("boolean", "java.lang.Boolean");
        JavaClassnameMap.put("byte", "java.lang.Byte");
        JavaClassnameMap.put("char", "java.lang.Character");
        JavaClassnameMap.put("double", "java.lang.Double");
        JavaClassnameMap.put("float", "java.lang.Float");
        JavaClassnameMap.put("int", "java.lang.Integer");
        JavaClassnameMap.put("long", "java.lang.Long");
        JavaClassnameMap.put("short", "java.lang.Short");
        JavaClassnameMap.put("Boolean", "java.lang.Boolean");
        JavaClassnameMap.put("Byte", "java.lang.Byte");
        JavaClassnameMap.put("Character", "java.lang.String");
        JavaClassnameMap.put("Double", "java.lang.Double");
        JavaClassnameMap.put("Float", "java.lang.Float");
        JavaClassnameMap.put("Integer", "java.lang.Integer");
        JavaClassnameMap.put("Long", "java.lang.Long");
        JavaClassnameMap.put("Short", "java.lang.Short");
        JavaClassnameMap.put("String", "java.lang.String");

        JavaClassnameMap.put("boolean[]", "[Ljava.lang.Boolean");
        JavaClassnameMap.put("byte[]", "[Ljava.lang.Byte");
        JavaClassnameMap.put("char[]", "java.lang.String");
        JavaClassnameMap.put("double[]", "[Ljava.lang.Double");
        JavaClassnameMap.put("float[]", "[Ljava.lang.Float");
        JavaClassnameMap.put("int[]", "[Ljava.lang.Integer");
        JavaClassnameMap.put("long[]", "[Ljava.lang.Long");
        JavaClassnameMap.put("short[]", "[Ljava.lang.Short");
        
        JavaClassnameMap.put("Boolean[]", "[Ljava.lang.Boolean");
        JavaClassnameMap.put("Byte[]", "[Ljava.lang.Byte");
        JavaClassnameMap.put("Char[]", "java.lang.String");
        JavaClassnameMap.put("Double[]", "[Ljava.lang.Double");
        JavaClassnameMap.put("Float[]", "[Ljava.lang.Float");
        JavaClassnameMap.put("Integer[]", "[Ljava.lang.Integer");
        JavaClassnameMap.put("Long[]", "[Ljava.lang.Long");
        JavaClassnameMap.put("Short[]", "[Ljava.lang.Short");

        JavaClassnameMap.put("Date", "java.util.Date");
        JavaClassnameMap.put("Time", "java.util.Date");
        JavaClassnameMap.put("Timestamp", "java.util.Date");
    }

    public static String getDataTypeString(Class clazz) {
        if (clazz != null) {
            for (SQLConversionItem item : SQLConversionTables.toDataType) {
                if (item.javaClass == clazz) {
                    return item.dataType;
                }
            }
            for (SQLConversionItem item : SQLConversionTables.toDataType) {
                if (clazz.isAssignableFrom(item.javaClass)) {
                    return item.dataType;
                }
            }
        }
        return "text(<size>)";
    }

    public static String getDatabaseType(String classname) {

        try {
            Class clazz = null;
            if (builtInMap.containsKey(classname)) {
                clazz = builtInMap.get(classname);
            } else {
                clazz = Class.forName(classname);
            }
            if (clazz == null) {
                return "blob";
            }
            if (clazz.isEnum()) {
                return "enum<enums>";
            }
            return getDataTypeString(clazz);
        } catch (Exception ex) {
            log.warning("SQLConversionTables: " + classname + " class not found.");
            log.throwing("", "getDatabaseType", ex);
        }
        return "blob";
    }

    public static String getJavaClassName(String classname) {

        String name = classname;
        try {
            if (JavaClassnameMap.containsKey(classname)) {
                name = JavaClassnameMap.get(classname);
            } else {
                name = Class.forName(classname).getName();
            }
        } catch (Exception ex) {
            log.warning("SQLConversionTables: " + classname + " class not found.");
            log.throwing("", "getJavaClassName", ex);
        }
        return name;
    }

    public static String getJavaTypeNoNulls(String datatype) {

        if (SQLConversionTables.DataTypeMapping.get(datatype) == null) {
            log.warning("SQLConversionTables.getJavaTypeNoNulls: " + datatype + " data type not found.");
            return "String";
        }
        return SQLConversionTables.DataTypeMapping.get(datatype).noNullTypeName;
    }

    public static String getJavaTypeNull(String datatype) {

        if (SQLConversionTables.DataTypeMapping.get(datatype) == null) {
            log.warning("SQLConversionTables.getJavaTypeNull: " + datatype + " data type not found.");
            return "String";
        }
        return SQLConversionTables.DataTypeMapping.get(datatype).nullTypeName;
    }
}