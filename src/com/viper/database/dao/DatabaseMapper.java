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

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.viper.database.dao.converters.Converters;
import com.viper.database.model.Cell;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.DatabaseConnections;
import com.viper.database.model.Databases;
import com.viper.database.model.Row;
import com.viper.database.model.Table;
import com.viper.database.utils.FileUtil;
import com.viper.database.utils.JAXBUtil;
import com.viper.database.utils.JSONUtil;

public class DatabaseMapper {

    private final static Logger log = Logger.getLogger(DatabaseMapper.class.getName());

    /**
     * Given a map, convert the map into a bean, where the map's key values are the
     * same as the beans properties.
     * 
     * @param clazz
     *            the class of the bean, the bean will be created by this method.
     * @param map
     *            the map whose properties are to be converted to a bean
     * @return the map of the beans properties
     * @throws Exception
     *             failed to allocate object if class <T>
     */
    public final static <T> T toObject(Class<T> clazz, Map<String, Object> map) throws Exception {
        T bean = clazz.newInstance();
        for (String name : map.keySet()) {
            DatabaseUtil.setValue(bean, name, map.get(name));
        }
        return bean;
    }

    public final static <T> T toObject(Class<T> clazz, Row row) throws Exception {
        if (clazz == null) {
            return null;
        }
        T bean = clazz.newInstance();
        for (Cell cell : row.getCells()) {
            DatabaseUtil.setValue(bean, cell.getName(),
                    Converters.convert(DatabaseUtil.toPropertyClass(clazz, cell.getName()), cell.getValue()));
        }
        return bean;
    }

    public final static <T> List<T> toObjects(Class<T> clazz, List<Row> rows) throws Exception {
        List<T> list = new ArrayList<T>();
        for (Row row : rows) {
            list.add(toObject(clazz, row));
        }
        return list;
    }

    /**
     * 
     * @param indir
     * @param outfile
     * @param filter
     * @throws Exception
     */
    public final static <T> void importTableByFile(DatabaseInterface dao, String filename, Class<T> clazz)
            throws Exception {

        String extension = getFileExtension(filename);
        if ("json".equalsIgnoreCase(extension)) {
            importTableAsJSON(clazz, dao, filename);
        } else if ("xml".equalsIgnoreCase(extension)) {
            // importXML(dao, infile, classname);
        } else if ("csv".equalsIgnoreCase(extension)) {
            importTableAsCSV(clazz, dao, filename);
        }
    }

    /**
     * 
     * @param indir
     * @param outfile
     * @param filter
     * @throws Exception
     */
    public final static <T> void exportTableByFile(DatabaseSQLInterface dao, String filename, Class<T> clazz)
            throws Exception {

        String extension = getFileExtension(filename);
        if ("json".equalsIgnoreCase(extension)) {
            exportTableAsJSON(clazz, dao, filename);
        } else if ("xml".equalsIgnoreCase(extension)) {
            // exportTableAsXML(dao, infile, classname);
        } else if ("csv".equalsIgnoreCase(extension)) {
            exportTableAsCSV(clazz, dao, filename);
        }
    }

    /**
     * Given the dao interface, and the model of the database, transfer the data in
     * the database object to the database.
     * 
     * @param dao
     *            the object for read/writing to/from the database.
     * @param database
     *            the in memory database object containing the cached data.
     * 
     * @throws Exception
     *             the transfer to database failed, failure to get table data, no
     *             database, no table, bad connection, etc.
     */
    public final static void importTable(DatabaseInterface dao, Database database) throws Exception {

        for (Table table : database.getTables()) {
            log.fine("Processing table: " + table.getName());

            Class tableClass = DatabaseUtil.toTableClass(database.getPackageName(), table.getName());
            if (tableClass == null) {
                throw new Exception("Unable to find class which belongs to table name:" + database.getName() + "."
                        + table.getName());
            }

            for (Row row : table.getRows()) {
                Object bean = tableClass.newInstance();
                for (Cell cell : row.getCells()) {
                    DatabaseUtil.setValue(bean, cell.getName(), cell.getValue());
                }
                dao.insert(bean);
            }
        }
    }

    /**
     * Given the dao interface, and the model of the database, transfer the data
     * from the CSV file to the database.
     * 
     * @param dao
     *            the object for read/writing to/from the database.
     * @param clazz
     *            the class of the bean matching the CSV data, should contain
     *            database annotations.
     * @param filename
     *            the filename of the location of the CSV data to import
     * 
     * @throws Exception
     *             the transfer to database failed, failure to get table data, no
     *             database, no table, bad connection, etc.
     * @note org.apache.commons.csv is used to read the files.
     */
    public final static <T> void importTableAsCSV(Class<T> clazz, DatabaseInterface dao, String filename)
            throws Exception {

        log.info("importTableAsCSV#1: " + filename);
        List<String> header = new ArrayList<String>();

        try {
            String str = FileUtil.readFile(clazz, filename);
            if (str == null || str.length() == 0) {
                return;
            }
            StringReader reader = new StringReader(str);

            log.info("importTableAsCSV#2: " + filename);
            Iterator<CSVRecord> iterator = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces().parse(reader).iterator();
            while (iterator.hasNext()) {
                CSVRecord result = iterator.next();

                log.info("importTableAsCSV#2B: " + result.size());

                if (header.size() == 0) {
                    for (int i = 0; i < result.size(); i++) {
                        header.add(result.get(i));
                    }
                    continue;
                }

                log.info("importTableAsCSV#2C: " + clazz.getName());

                T bean = clazz.newInstance();
                for (int i = 0; i < result.size(); i++) {
                    log.info("importTableAsCSV#2D: " + i + "]:" + header.get(i));
                    DatabaseUtil.setValue(bean, header.get(i), result.get(i));
                }
                dao.insert(bean);
            }

            log.info("importTableAsCSV#3: " + filename + "," + header.size());
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Given the dao interface, and the model of the database, transfer the data
     * from the CSV file to the database.
     * 
     * @param dao
     *            the object for read/writing to/from the database.
     * @param clazz
     *            the class of the bean matching the CSV data, should contain
     *            database annotations.
     * @param filename
     *            the filename of the location of the CSV data to import
     * 
     * @throws Exception
     *             the transfer to database failed, failure to get table data, no
     *             database, no table, bad connection, etc.
     * @note org.apache.commons.csv is used to read the files.
     */
    public final static <T> void importTableAsJSON(Class<T> clazz, DatabaseInterface dao, String filename)
            throws Exception {

        String str = FileUtil.readFile(clazz, filename);

        List<T> beans = JSONUtil.fromJSONList(clazz, str);

        dao.insertAll(beans);
    }

    /**
     * Given the dao interface, and the model of the database, transfer the data
     * from the CSV file to the database.
     * 
     * @param dao
     *            the object for read/writing to/from the database.
     * @param clazz
     *            the class of the bean matching the CSV data, should contain
     *            database annotations.
     * @param filename
     *            the filename of the location of the CSV data to import
     * 
     * @throws Exception
     *             the transfer to database failed, failure to get table data, no
     *             database, no table, bad connection, etc.
     * @note org.apache.commons.csv is used to read the files.
     */
    public final static <T> void exportTableAsJSON(Class<T> clazz, DatabaseInterface dao, String filename)
            throws Exception {

        List<T> beans = dao.queryAll(clazz);

        String str = JSONUtil.toJSON(beans);

        FileUtil.writeFile(filename, str);

    }

    /**
     * Given the dao interface, read the data corresponding to the class object, and
     * write to a file in the outdir directory.
     * 
     * @param dao
     *            the object for read/writing to/from the database.
     * @param clazz
     *            the class of the bean matching the CSV data, should contain
     *            database annotations.
     * @param filename
     *            the filename which will contain the CSV formatted data.
     * 
     * @throws Exception
     *             the transfer from database failed, failure to get table data, no
     *             database, no table, bad connection, etc. Or the transfer to file
     *             failed.
     * @note the org.apache.commons.csv is used to write the files.
     */
    public final static <T> void exportTableAsCSV(Class<T> clazz, DatabaseSQLInterface dao, String filename)
            throws Exception {

        String databasename = DatabaseUtil.getDatabaseName(clazz);
        String tablename = DatabaseUtil.getTableName(clazz);
        
        String sql = "select * from " + databasename + "." + tablename;
        
        List<Row> items = dao.readRows(sql);

        new File(filename).getAbsoluteFile().getParentFile().mkdirs();

        PrintStream out = new PrintStream(new File(filename));
        boolean isFirst = true;

        for (Row row : items) {
            if (row == null) {
                continue;
            }
            if (isFirst) {
                List<String> header = new ArrayList<String>();
                for (Cell cell : row.getCells()) {
                    header.add(cell.getName());
                }
                out.println(CSVFormat.EXCEL.withIgnoreSurroundingSpaces().format(header.toArray()));
            }
            isFirst = false;
           
            List data = new ArrayList<>();
            for (Cell cell : row.getCells()) {
                data.add(cell.getValue());
            }
            out.println(CSVFormat.EXCEL.format(data.toArray()));
        }
        out.flush();
        out.close();
    }

    /**
     * Given the dao interface, read the data corresponding to the class object, and
     * write to a file in the outdir directory.
     * 
     * @param dao
     *            the object for read/writing to/from the database.
     * @param clazz
     *            the class of the bean matching the CSV data, should contain
     *            database annotations.
     * @param filename
     *            the filename which will contain the CSV formatted data.
     * 
     * @throws Exception
     *             the transfer from database failed, failure to get table data, no
     *             database, no table, bad connection, etc. Or the transfer to file
     *             failed.
     * @note the org.apache.commons.csv is used to write the files.
     */
    public final static <T> void exportCollectionAsCSV(List<T> items, String filename) throws Exception {

        new File(filename).getAbsoluteFile().getParentFile().mkdirs();

        PrintStream out = new PrintStream(new File(filename));
        List<String> header = null;
        boolean isFirst = true;

        for (T obj : items) {
            if (obj == null) {
                continue;
            }
            if (isFirst) {
                header = DatabaseUtil.getColumnFieldNames(obj.getClass());
                out.println(CSVFormat.EXCEL.format(header.toArray()));
            }
            isFirst = false;
            Object data[] = new Object[header.size()];
            for (int i = 0; i < header.size(); i++) {
                Object value = DatabaseUtil.getValue(obj, header.get(i));
                data[i] = value;
            }
            out.println(CSVFormat.EXCEL.format(data));
        }
        out.flush();
        out.close();
    }

    /**
     * Given the list of connections, write the contents as xml to te specified
     * filename.
     * 
     * @param dbcs
     *            the connections object, should contain XML annotations.
     * @param filename
     *            the disk file where the connections are to be written as XML file.
     * @throws Exception
     *             The transfer to file failed. File access problem or JAXB
     *             conversion to XML issue.
     */
    public final static void writeConnections(String filename, DatabaseConnections dbcs) throws Exception {
        if (filename == null) {
            filename = dbcs.getFilename();
        }
        try {
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,
                    "http://www.vipersoftwareservices.com/schemas/database.xsd");

            writeValueToXml(new File(filename), dbcs, properties);
        } catch (Exception ioe) {
            throw new Exception("Unable to write file: " + filename, ioe);
        }
    }

    /**
     * Given the filename load from the file, all the database connections.
     * 
     * @param filename
     *            the file of XML data contain the connection data.
     * @return the object containing the database connection information.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static DatabaseConnections readConnections(String filename) throws Exception {
        try {
            return readValueFromXml(FileUtil.getInputStream(DatabaseConnections.class, filename),
                    DatabaseConnections.class);
        } catch (Exception jaxerr) {
            throw new Exception("Unable to readConnections for file: " + filename, jaxerr);
        }
    }

    /**
     * Given the list of connections, write the contents as xml to te specified
     * filename.
     *
     * @param filename
     *            the disk file where the connections are to be written as XML file.
     *
     * @param databases
     *            the databases object, should contain annotattions from package
     *            com.viper.database.annotations
     * @throws Exception
     *             The transfer to file failed. File access problem or JAXB
     *             conversion to XML issue.
     */
    public final static void writeDatabases(String filename, Databases databases) throws Exception {
        try {
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,
                    "http://www.vipersoftwareservices.com/schemas/database.xsd");

            writeValueToXml(new File(filename), databases, properties);
        } catch (Exception ioe) {
            throw new Exception("Unable to write file: " + filename, ioe);
        }
    }

    /**
     * Given the filename load from the file, all the database connections.
     * 
     * @param filename
     *            the file of xml data contain the connection data.
     * @return the object containing the database connection information.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static Databases readDatabases(String filename) throws Exception {
        try {
            return readValueFromXml(FileUtil.getInputStream(Databases.class, filename), Databases.class);
        } catch (Exception jaxerr) {
            throw new Exception("Unable to parse for file: " + filename, jaxerr);
        }
    }

    /**
     * Given the list of connections, write the contents as xml to te specified
     * filename.
     * 
     * @param filename
     *            the disk file where the connections are to be written as XML file.
     * @param database
     *            the database object, should contain annotattions from package
     *            com.viper.database.annotations
     * @throws Exception
     *             The transfer to file failed. File access problem or JAXB
     *             conversion to XML issue.
     */
    public final static void writeDatabase(String filename, Database database) throws Exception {
        try {
            new File(filename).getParentFile().mkdirs();

            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,
                    "http://www.vipersoftwareservices.com/schemas/database.xsd");

            writeValueToXml(new File(filename), database, null);
        } catch (Exception ioe) {
            ioe.printStackTrace();
            throw new Exception("Unable to write file: " + filename, ioe);
        }
    }

    public final static void mergeDatabase(Database oldDatabase, Database newDatabase) throws Exception {
        if (oldDatabase == null) {
            return;
        }

        for (Table newTable : newDatabase.getTables()) {
            Table oldTable = DatabaseUtil.findOneItem(oldDatabase.getTables(), "name", newTable.getName());
            if (oldTable == null) {
                continue;
            }
            newTable.setIsRestService(oldTable.isIsRestService());
            newTable.setValidator(oldTable.getValidator());
            newTable.setBeanGenerator(oldTable.getBeanGenerator());
            newTable.setConverter(oldTable.getConverter());
            newTable.setIterations(oldTable.getIterations());
            newTable.setFilter(oldTable.getFilter());

            for (Column newColumn : newTable.getColumns()) {
                Column oldColumn = DatabaseUtil.findOneItem(oldTable.getColumns(), "name", newColumn.getName());
                if (oldColumn == null) {
                    continue;
                }
                newColumn.setValidator(oldColumn.getValidator());
                newColumn.setConverter(newColumn.getConverter());
                newColumn.setComponentType(oldColumn.getComponentType());
                newColumn.setLogicalType(oldColumn.getLogicalType());
                newColumn.setPersistent(oldColumn.isPersistent());
                newColumn.setConverter(oldColumn.getConverter());
                newColumn.setOrder(oldColumn.getOrder());
            }
        }
    }

    /**
     * Given the filename load from the file, all the database connections.
     * 
     * @param filename
     *            the file of XML data contain the connection data.
     * @return the object containing the database connection information.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static Database readDatabase(String filename) throws Exception {
        try {
            return readValueFromXml(FileUtil.getInputStream(Database.class, filename), Database.class);
        } catch (Exception jaxerr) {
            throw new Exception("Unable to read/parse for file: " + filename, jaxerr);
        }
    }

    public static Databases readDatabasesInDirectory(String filename) throws Exception {
        Databases databases = new Databases();
        File file = new File(filename);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                Databases databases1 = readDatabasesInDirectory(f.getAbsolutePath());
                databases.getDatabases().addAll(databases1.getDatabases());
                databases.getPrivileges().addAll(databases1.getPrivileges());
                databases.getUsers().addAll(databases1.getUsers());
            }
        } else if (filename.endsWith(".xml")) {
            Database database = readDatabase(filename);
            databases.getDatabases().add(database);
        }
        return databases;
    }

    /**
     * Given the filename and the marshaling bean class, load the file into a bean.
     * 
     * @param filename
     *            the file of XML data contain the bean data.
     * @param clazz
     *            the class of the bean, the bean will be created by this method.
     * @return the object containing the database connection information.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static <T> T read(Class<T> clazz, String filename) throws Exception {
        return readValueFromXml(FileUtil.getInputStream(clazz, filename), clazz);
    }

    /**
     * Given the filename and the marshaling bean class, load the file into a bean.
     * 
     * @param filename
     *            the file of XML data contain the bean data.
     * @param clazz
     *            the class of the bean, the bean will be created by this method.
     * @return the object containing the database connection information.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static <T> T readValueFromXML(Class<T> clazz, String text) throws Exception {
        return readValueFromXml(new StringReader(text), clazz);
    }

    /**
     * Given the Reader stream and the marshaling bean class, load the file into a
     * bean.
     * 
     * @param reader
     *            the reader stream of XML data contain the bean data.
     * @param clazz
     *            the class of the bean, the bean will be created by this method.
     * @return the object containing the database connection information.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static <T> T read(Class<T> clazz, Reader reader) throws Exception {
        return readValueFromXml(reader, clazz);
    }

    /**
     * Given the Reader stream and the marshaling bean class, load the file into a
     * bean.
     * 
     * @param url
     *            the URL of XML data contain the bean data.
     * @param clazz
     *            the class of the bean, the bean will be created by this method.
     * @return the object containing the database connection information.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static <T> T read(Class<T> clazz, URL url) throws Exception {
        return readValueFromXml(url, clazz);
    }

    /**
     * Given the filename and the bean class, store the file from the serialized
     * bean (XML).
     * 
     * @param filename
     *            the file where bean data is to be written.
     * @param item
     *            the bean, the bean will be serialized and written to file.
     * @param properties
     *            a map of properties for the JAXB serialization.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static <T> void write(String filename, T item, Map<String, Object> properties) throws Exception {
        new File(filename).getParentFile().mkdirs();

        FileWriter writer = new FileWriter(filename);
        write(writer, item, properties);
        writer.flush();
        writer.close();
    }

    /**
     * Given the writer and the bean, store into the writer the serialized bean
     * (XML).
     * 
     * @param writer
     *            the writer where bean data is to be written.
     * @param item
     *            the bean, the bean will be serialized and written to file.
     * @param properties
     *            a map of properties for the JAXB serialization.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static <T> void write(Writer writer, T item, Map<String, Object> properties) throws Exception {
        writeValueToXml(writer, item, properties);
    }

    /**
     * Given the writer and the bean, store into the writer the serialized bean
     * (XML).
     * 
     * @param writer
     *            the writer where bean data is to be written.
     * @param item
     *            the bean, the bean will be serialized and written to file.
     * @param properties
     *            a map of properties for the JAXB serialization.
     * @throws Exception
     *             The transfer from file failed. File access problem or JAXB
     *             conversion issue.
     */
    public final static <T> String write(T item, Map<String, Object> properties) throws Exception {
        StringWriter writer = new StringWriter();
        writeValueToXml(writer, item, properties);

        return writer.toString();
    }

    // -------------------------------------------------------------------------

    private final static <T> T readValueFromXml(InputStream inputstream, Class<T> clazz) throws Exception {
        return JAXBUtil.createXmlUnmarshaller(clazz, null).unmarshal(new StreamSource(inputstream), clazz).getValue();
    }

    private final static <T> T readValueFromXml(Reader reader, Class<T> clazz) throws Exception {
        return JAXBUtil.createXmlUnmarshaller(clazz, null).unmarshal(new StreamSource(reader), clazz).getValue();
    }

    private final static <T> T readValueFromXml(URL url, Class<T> clazz) throws Exception {
        return (T) JAXBUtil.createXmlUnmarshaller(clazz, null).unmarshal(url.openStream());
    }

    private final static <T> void writeValueToXml(Writer writer, T bean, Map<String, Object> properties)
            throws Exception {
        if (bean == null) {
            throw new Exception("Passed object is null");
        }
        JAXBUtil.createXmlMarshaller(bean.getClass(), properties).marshal(bean, writer);
        writer.flush();
    }

    private final static <T> void writeValueToXml(File file, T bean, Map<String, Object> properties) throws Exception {
        if (bean == null) {
            throw new Exception("Passed object is null");
        }
        JAXBUtil.createXmlMarshaller(bean.getClass(), properties).marshal(bean, file);
    }

    private final static String getFileExtension(String filename) {
        String extension = null;

        int index = filename.lastIndexOf('.');
        if (index != -1) {
            extension = filename.substring(index + 1);
        }
        return extension;
    }

}