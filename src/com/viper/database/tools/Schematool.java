/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
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

package com.viper.database.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.DifferenceEvaluator;

import com.viper.database.CustomXPathFunctions;
import com.viper.database.dao.DatabaseFactory;
import com.viper.database.dao.DatabaseInterface;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.dao.DatabaseWriter;
import com.viper.database.dao.SQLWriter;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Cell;
import com.viper.database.model.Column;
import com.viper.database.model.Database;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.DatabaseConnections;
import com.viper.database.model.Databases;
import com.viper.database.model.EnumItem;
import com.viper.database.model.Procedure;
import com.viper.database.model.Row;
import com.viper.database.model.Table;
import com.viper.database.model.Trigger;
import com.viper.database.params.model.ParamType;
import com.viper.database.params.model.Params;
import com.viper.database.security.Encryptor;
import com.viper.database.utils.FileUtil;
import com.viper.database.utils.JEXLUtil;
import com.viper.database.utils.RandomBean;
import com.viper.database.utils.SortedProperties;

public class Schematool {

    private final static Logger log = Logger.getLogger(Schematool.class.getName());
    private static final JEXLUtil jexl = JEXLUtil.getInstance();

    public void process(String args[]) throws Exception {

        final Map<String, Object> parameters = new HashMap<String, Object>();
        final List<Table> cache = new ArrayList<Table>();
        String user = null;
        String pwd = null;

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINEST);
        log.addHandler(ch);
        log.setLevel(Level.FINEST);

        for (int i = 0; i < args.length; i++) {
            if ("-username".equals(args[i])) {
                user = args[++i];

            } else if ("-password".equals(args[i])) {
                pwd = args[++i];

            } else if ("-param".equals(args[i])) {
                String key = args[++i];
                String value = args[++i];
                parameters.put(key, value);

            } else if ("-meta2database".equals(args[i]) || "-import".equals(args[i])) {

                String sourceUrl = args[++i];
                String packageName = args[++i];
                String vendor = args[++i];
                String outfile = args[++i];

                Databases databases = new Databases();
                for (i = i + 1; i < args.length; i++) {
                    if (args[i].startsWith("-")) {
                        i = i - 1;
                        break;
                    }
                    Databases databases1 = DatabaseMapper.readDatabasesInDirectory(args[i]);
                    databases.getDatabases().addAll(databases1.getDatabases());
                    databases.getPrivileges().addAll(databases1.getPrivileges());
                    databases.getUsers().addAll(databases1.getUsers());
                }
                DatabaseConnection connection = createConnection(sourceUrl);
                connection.getPackageNames().clear();
                connection.getPackageNames().add(packageName);

                SQLDriver driver = new SQLDriver(vendor);
                DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(connection);
                for (Database database : databases.getDatabases()) {
                    dao.write(driver.dropDatabase(database));
                }

                SqlConverter.write(new SQLWriter(outfile), vendor, databases);
                SqlConverter.write(new DatabaseWriter(dao), vendor, databases);

            } else if ("-database2meta".equals(args[i])) {
                String source = args[++i];
                String databaseName = args[++i];
                String outfile = args[++i];

                System.out.println("-database2meta: outfile=" + outfile);

                DatabaseConnection connection = createConnection(source);
                DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(connection);

                SQLDriver driver = new SQLDriver("mysql");
                Databases databases = driver.load(dao, databaseName, null);
                if (databases == null || databases.getDatabases().size() != 1) {
                    System.out.println("Database: " + databaseName + " not found.");
                }
                DatabaseMapper.writeDatabase(outfile, databases.getDatabases().get(0));

            } else if ("-sql2database".equals(args[i]) || "-sql".equals(args[i])) {
                String source = args[++i];
                String indir = args[++i];
                System.out.println("-SQL: indir=" + indir);

                DatabaseConnection connection = createConnection(source);

                importSQL2Database(connection, indir);

            } else if ("-database2sql".equals(args[i])) {
                String source = args[++i];
                String databaseName = args[++i];
                String outfile = args[++i];
                System.out
                        .println("-database2sql: connection=" + source + ", outfile=" + outfile + ", " + databaseName);

                DatabaseConnection connection = createConnection(source);
                DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(connection);

                SQLDriver driver = new SQLDriver("mysql");
                Databases databases = driver.load(dao, databaseName, null);

                SqlConverter.write(new FileWriter(outfile), dao, databases, null);

            } else if ("-model2sql".equals(args[i])) {
                String indir = args[++i];
                String databaseName = args[++i];
                String vendor = args[++i];
                String outfile = args[++i];

                System.out.println("-model2sql: indir=" + indir + ", outfile=" + outfile + ", " + databaseName);

                Databases databases = loadDatabases(indir);

                SqlConverter.write(new FileWriter(outfile), vendor, databases);

            } else if ("-cache".equalsIgnoreCase(args[i])) {
                final String indir = args[++i];
                final String packageName = args[++i];

                cache.addAll(loadTables(new File(indir), packageName));

                /**
                 * Create the model as POJO beans from the schema.
                 */
            } else if ("-pojo".equalsIgnoreCase(args[i])) {
                final String indir = args[++i];
                final String outdir = args[++i];
                final String template = args[++i];
                final String packageName = args[++i];

                final Map<String, Object> params = new HashMap<String, Object>();
                params.putAll(parameters);
                params.put("indir", indir);
                params.put("outdir", outdir + "/" + packageName.replace('.', '/'));
                params.put("java", CustomXPathFunctions.class);
                params.put("packagename", packageName);

                System.out.println("-pojo: modeldir=" + indir + ", outdir=>" + outdir + ", template=" + template
                        + ",packagename=" + packageName);

                generatePOJO(new File(indir), new File(outdir), template, ".xml", params, cache);

            } else if ("-pojos".equalsIgnoreCase(args[i])) {
                final String indir = args[++i];
                final String outdir = args[++i];
                final String template = args[++i];
                final String packageName = args[++i];

                final Map<String, Object> params = new HashMap<String, Object>();
                params.putAll(parameters);
                params.put("indir", indir);
                params.put("outdir", outdir + "/" + packageName.replace('.', '/'));
                params.put("java", CustomXPathFunctions.class);
                params.put("packagename", packageName);

                System.out.println("-pojos: modeldir=" + indir + ", outdir=>" + outdir + ", template=" + template
                        + ",packagename=" + packageName);
                generatePOJOs(indir, outdir, template, "", ".xml", params);

                /**
                 * Create the model as Annotation beans from the schema.
                 */
            } else if ("-annotation".equalsIgnoreCase(args[i])) {
                final String filename = args[++i];
                final String tagname = args[++i];
                final String outdir = args[++i];
                final String template = args[++i];

                final Map<String, Object> params = new HashMap<String, Object>();
                params.putAll(parameters);
                params.put("outdir", outdir.replace('.', '/') + "/");
                params.put("java", CustomXPathFunctions.class);

                System.out.println("-annotation: filename=" + filename + ", outdir=>" + outdir + ", tagname=>" + tagname
                        + ", template=" + template);

                generateAnnotation(filename, tagname, new File(outdir), template, params);

                /**
                 * Create the model as POJO beans from multiple tables
                 */
            } else if ("-bean-join".equalsIgnoreCase(args[i])) {
                final String modeldir = args[++i];
                final String indir = args[++i];
                final String outdir = args[++i];

                System.out.println("-bean-join: indir=" + indir + ", outdir=>" + outdir + ", modeldir=" + modeldir);

                generateBeanJoin(new File(modeldir), new File(indir), new File(outdir), null, null);

                /**
                 * Create the model as POJO beans from the schema.
                 */
            } else if ("-test".equalsIgnoreCase(args[i])) {
                final String indir = args[++i];
                final String outdir = args[++i];
                final String templateFilename = args[++i];
                final String packageName = args[++i];

                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("indir", indir + "/" + packageName.replace('.', '/'));
                params.put("outdir", outdir + "/" + packageName.replace('.', '/'));
                params.put("java", CustomXPathFunctions.class);
                params.put("Modifier", java.lang.reflect.Modifier.class);

                System.out.println("-test: modeldir=" + indir + ", outdir=>" + outdir);

                String template = readFile(templateFilename);
                generateTest(new File(indir), new File(outdir), template, packageName, ".java", params);

            } else if ("-seed".equalsIgnoreCase(args[i])) {
                final String url = args[++i];
                final String packageName = args[++i];

                System.out.println("-seed: url=" + url + ", packageName=>" + packageName);

                DatabaseConnection connection = createConnection(url);
                connection.getPackageNames().clear();
                connection.getPackageNames().add(packageName);

                for (i = i + 1; i < args.length; i++) {
                    if (args[i].startsWith("-")) {
                        i = i - 1;
                        break;
                    }
                    generateSeed(null, new File(args[i]), ".xml", connection);
                }

            } else if ("-simulation".equalsIgnoreCase(args[i])) {
                final String url = args[++i];
                final String packageName = args[++i];

                System.out.println("-SIMULATION: url=" + url + ", packageName=>" + packageName);

                DatabaseConnection connection = createConnection(url);
                connection.getPackageNames().clear();
                connection.getPackageNames().add(packageName);

                generateBeans(connection);

            } else if ("-export".equalsIgnoreCase(args[i])) {
                final String indir = args[++i];
                final String outfile = args[++i];

                System.out.println("-export: indir=" + indir + ", outfile=>" + outfile);
                exportDATA(indir, outfile, ".xml");

            } else if ("-import.table".equalsIgnoreCase(args[i])) {
                final String source = args[++i];
                final String filename = args[++i];
                final String classname = args[++i];

                System.out.println("-export: filename=" + filename + ", classname=>" + classname);

                DatabaseConnection connection = createConnection(source);
                DatabaseInterface dao = DatabaseFactory.getInstance(connection);

                Class clazz = DatabaseUtil.toTableClass(classname);
                DatabaseMapper.importTableByFile(dao, filename, clazz);

            } else if ("-resources".equalsIgnoreCase(args[i])) {
                final String indir = args[++i];
                final String infile = args[++i];
                final String outfile = args[++i];

                System.out.println("-resources: modeldir=" + indir + ", outfile=>" + outfile);
                Properties properties = load(infile);

                generateResources(new File(indir), properties, ".xml");

                save(outfile, properties);

            } else if ("-wiki".equalsIgnoreCase(args[i])) {
                final String infile = args[++i];
                final String outfile = args[++i];

                System.out.println("-resources: infile=" + infile + ", outfile=>" + outfile);

                generateDictionary(infile, outfile);

            } else if ("-template".equalsIgnoreCase(args[i])) {
                final String indir = args[++i];
                final String outdir = args[++i];
                final String packagename = args[++i];

                System.out.println("-template: modeldir=" + indir + ", outfile=>" + outdir);

                generateTemplates(packagename, new File(indir), outdir, ".xml");

            } else if ("-print".equalsIgnoreCase(args[i])) {
                final String indir = args[++i];
                final String outdir = args[++i];
                final String packagename = args[++i];

                System.out.println("-print: modeldir=" + indir + ", outfile=>" + outdir);

                printJsonTemplate(packagename, new File(indir), outdir, ".xml");

            } else if ("-grab".equalsIgnoreCase(args[i])) {
                final String url = args[++i];
                final String databaseName = args[++i];
                final String outdir = args[++i];

                System.out.println("-grab: databaseUrl=" + url + ", outdir=>" + outdir);

                DatabaseConnection connection = createConnection(url);
                DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(connection);

                importSchema(dao, databaseName, outdir);

            } else if ("-compare".equalsIgnoreCase(args[i])) {
                final String indir1 = args[++i];
                final String indir2 = args[++i];

                compareMetaDatabaseInfo(indir1, indir2);

            } else if ("-document".equalsIgnoreCase(args[i])) {
                final String indir = args[++i];
                final String outfile = args[++i];

                System.out.println("-document: modeldir=" + indir + ", outfile=>" + outfile);
                List<Database> databases = load(indir, ".xml");
                exportDocument(databases, outfile);

            } else if ("-password".equalsIgnoreCase(args[i])) {
                final String password = args[++i];
                System.out.println("Password: " + password + ", encrypted: " + encryptPassword(password));

            } else if ("-encrypt".equalsIgnoreCase(args[i])) {
                final String password = args[++i];
                final String algorithm = args[++i];
                final String infile = args[++i];
                final String outfile = args[++i];

                System.out.println("-encrypt: infile=" + infile + ", outfile=>" + outfile);

                encryptFile(password, algorithm, infile, outfile);

            } else if ("-decrypt".equalsIgnoreCase(args[i])) {
                final String password = args[++i];
                final String algorithm = args[++i];
                final String infile = args[++i];
                final String outfile = args[++i];

                System.out.println("-decrypt: infile=" + infile + ", outfile=>" + outfile);

                decryptFile(password, algorithm, infile, outfile);

            } else if ("-help".equalsIgnoreCase(args[i])) {
                displayHelp();

            } else {
                displayHelp();
                throw new IllegalArgumentException("Schematool: unknown command line argument: " + args[i]);
            }
        }

        if (args == null || args.length == 0) {
            displayHelp();
        }
    }

    private static String[] connectionFilenames = new String[] { "./etc/databases.xml", "./databases.xml",
            "res:/databases.xml" };

    private DatabaseConnection createConnection(String source) throws Exception {

        for (String connectionFilename : connectionFilenames) {
            try {
                DatabaseConnections connections = DatabaseMapper.readConnections(connectionFilename);
                for (DatabaseConnection connection : connections.getConnections()) {
                    if (connection.getName().equalsIgnoreCase(source)) {
                        return connection;
                    }
                }
            } catch (Exception ex) {
                System.out.println("FY: Failed to read file " + connectionFilename + ", trying another location.");
            }
        }

        DatabaseConnection connection = new DatabaseConnection();
        connection.setName("dummy");
        connection.setDatabaseUrl(source);
        return connection;
    }

    private String encryptPassword(String password) throws Exception {

        Encryptor encryptor = new Encryptor();
        String encPassword = encryptor.encrypt(password);

        return "ENC:" + encPassword;
    }

    private void encryptFile(String password, String algorithm, String infile, String outfile) throws Exception {

        Encryptor encryptor = new Encryptor();
        String value = readFile(infile);
        String result = encryptor.encrypt(value, algorithm, password);

        Files.write(new File(outfile).toPath(), result.getBytes());
    }

    private void decryptFile(String password, String algorithm, String infile, String outfile) throws Exception {

        Encryptor encryptor = new Encryptor();
        encryptor.decrypt(infile, algorithm, password, outfile);
    }

    private void importSQL2Database(DatabaseConnection connection, String filename) {

        File file = new File(filename);
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                importSQL2Database(connection, f.getAbsolutePath());
            }
        } else if (filename.toLowerCase().endsWith(".sql")) {
            System.out.println("-SQL: file=" + filename);
            try {
                read(new FileReader(filename), connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Databases loadDatabases(String directory) throws Exception {
        Databases databases = DatabaseMapper.readDatabasesInDirectory(directory);

        // TODO FIlter database

        return databases;
    }

    /**
     * Given the input directory scan for all XML files, using the templateFilename
     * convert the XML file to a Java POJO bean.
     * 
     * @param indir
     *            the directory containing all the model XML files, which contain
     *            the database/table definitions.
     * @param outdir
     *            the directory to which all java bean files are to be written.
     * @param templateFilename
     *            the filename of the template to use to generate the java pojo
     *            beans.
     * @param databaseName
     *            the name of the database to be processed, (deprecated this should
     *            be in the model XML file).
     * @param filter
     *            the filter on the input files to be applied.
     * @param params
     *            hash map of params to feed into the template file processing.
     * 
     * @throws Exception
     */
    public void generatePOJO(File indir, File outdir, String templateFilename, String filter,
            Map<String, Object> params, List<Table> cache) throws Exception {

        String template = readFileWithIncludes(templateFilename);

        outdir.mkdirs();

        for (File file : indir.listFiles()) {
            if (file.isDirectory()) {
                generatePOJO(file, outdir, templateFilename, filter, params, cache);
                continue;
            }

            if (!file.getName().endsWith(filter)) {
                System.out.println("Skipping " + file.getName() + ", fails the filter test: " + filter);
                continue;
            }

            System.out.println("Processing: " + file.getPath());

            Database database = DatabaseMapper.read(Database.class, file.getPath());

            String packageName = (String) params.get("packagename");

            processImportTable(packageName, database, cache, params);

            params.put("root", database);
            params.put("database", database);
            params.put("infilename", file.getPath());
            database.setPackageName(packageName);

            jexl.evaluate(template, params, null);
        }
    }

    /**
     * Given the input directory scan for all XML files, using the templateFilename
     * convert the XML file to a Java POJO bean.
     * 
     * @param indir
     *            the directory containing all the model XML files, which contain
     *            the database/table definitions.
     * @param outdir
     *            the directory to which all java bean files are to be written.
     * @param templateFilename
     *            the filename of the template to use to generate the java pojo
     *            beans.
     * @param databaseName
     *            the name of the database to be processed, (deprecated this should
     *            be in the model XML file).
     * @param filter
     *            the filter on the input files to be applied.
     * @param params
     *            hash map of params to feed into the template file processing.
     * 
     * @throws Exception
     */
    public void generatePOJOs(String indir, String outdir, String templateFilename, String databaseName, String filter,
            Map<String, Object> params) throws Exception {

        String template = readFileWithIncludes(templateFilename);

        new File(outdir).mkdirs();

        List<Database> items = load(indir, filter);
        Databases databases = new Databases();
        databases.getDatabases().addAll(items);

        params.put("root", databases);
        params.put("database", databases);

        jexl.evaluate(template, params, null);
    }

    /**
     * Given the input directory scan for all XML files, using the templateFilename
     * convert the XML file to a Java POJO bean.
     * 
     * @param indir
     *            the directory containing all the model XML files, which contain
     *            the database/table definitions.
     * @param outdir
     *            the directory to which all java bean files are to be written.
     * @param templateFilename
     *            the filename of the template to use to generate the java pojo
     *            beans.
     * @param databaseName
     *            the name of the database to be processed, (deprecated this should
     *            be in the model XML file).
     * @param filter
     *            the filter on the input files to be applied.
     * @param params
     *            hash map of params to feed into the template file processing.
     * 
     * @throws Exception
     */
    public void generateAnnotation(String filename, String tagname, File outdir, String templateFilename,
            Map<String, Object> params) throws Exception {

        String template = readFile(templateFilename);

        outdir.mkdirs();

        System.out.println("Processing: " + filename);

        Document bean = parseToDocument(filename);
        NodeList complexTypes = bean.getDocumentElement().getElementsByTagName("xs:complexType");

        for (int i = 0; i < complexTypes.getLength(); i++) {
            Element column = (Element) complexTypes.item(i);
            if (tagname.equalsIgnoreCase(column.getAttribute("name"))) {

                params.put("root", column);
                params.put("classname", tagname);
                params.put("custom", this);

                jexl.evaluate(template, params, null);
            }
        }
    }

    // <xs:element name="enum-value" type="EnumItem" minOccurs="0"
    // maxOccurs="unbounded" />
    // <!-- UI Modeling -->
    // <xs:element name="converter" type="xs:string" />
    // <xs:element name="validators" type="xs:string" minOccurs="0"
    // maxOccurs="unbounded" />
    // <xs:element name="action" type="xs:string" />
    // <xs:element name="minimum-value" type="xs:string" />
    // <xs:element name="maximum-value" type="xs:string" />
    // <xs:element name="options" type="xs:string" />
    // <xs:element name="component-type" type="xs:string" />
    // <xs:element name="values-classname" type="xs:string" />
    // <xs:element name="validation-message" type="xs:string" />
    // <xs:element name="tooltip-message" type="xs:string" />
    // </xs:sequence>
    // <xs:attribute name="name" type="xs:string" use="required" />
    // <xs:attribute name="table-name" type="xs:string" />
    // <xs:attribute name="database-name" type="xs:string" />
    // <xs:attribute name="description" type="xs:string" />
    // <xs:attribute name="index-name" type="xs:string" />
    // <xs:attribute name="id-method" type="IdMethodType" default="none" />
    // <xs:attribute name="java-type" type="xs:string" />
    // <xs:attribute name="generic-type" type="xs:string" />
    // <xs:attribute name="logical-type" type="xs:string" />
    // <xs:attribute name="data-type" type="xs:string" />
    // <xs:attribute name="extra-data-type" type="xs:string" />
    // <xs:attribute name="decimal-size" type="xs:int" />
    // <xs:attribute name="default-value" type="xs:string" />
    // <xs:attribute name="size" type="xs:long" default="0" />
    // <xs:attribute name="order" type="xs:int" default="0" />
    // <!-- Flags -->
    // <xs:attribute name="natural-key" type="xs:boolean" default="false" />
    // <xs:attribute name="primary-key" type="xs:boolean" default="false" />
    // <xs:attribute name="persistent" type="xs:boolean" default="true" />
    // <xs:attribute name="optional" type="xs:boolean" default="false" />
    // <xs:attribute name="required" type="xs:boolean" default="false" />
    // <xs:attribute name="secure" type="xs:boolean" default="false" />
    // <xs:attribute name="unique" type="xs:boolean" default="false" />
    // <xs:attribute name="unsigned" type="xs:boolean" default="false" />
    // <xs:attribute name="zero-fill" type="xs:boolean" default="false" />
    // <xs:attribute name="binary" type="xs:boolean" default="false" />
    // <xs:attribute name="ascii" type="xs:boolean" default="false" />
    // <xs:attribute name="unicode" type="xs:boolean" default="false" />
    // <xs:attribute name="is-nullable" type="xs:boolean" default="false" />

    public String toAnnotationBody(Element element) {
        StringBuilder buf = new StringBuilder();

        NodeList seqs = element.getElementsByTagName("xs:sequence");
        NodeList attrs = element.getElementsByTagName("xs:attribute");

        for (int i = 0; i < seqs.getLength(); i++) {
            Element seq = (Element) seqs.item(i);
            NodeList elements = seq.getElementsByTagName("xs:element");

            for (int j = 0; j < elements.getLength(); j++) {
                Element el = (Element) elements.item(j);

                buf.append("        ");
                buf.append(toJavaTypeFromXsdType(el.getAttribute("type")));
                buf.append(isArray(el.getAttribute("maxOccurs")) ? "[]" : "");
                buf.append(" ");
                buf.append(CustomXPathFunctions.toJavaNameFromDBName(el.getAttribute("name"), false));
                buf.append("() default ");
                buf.append(toDefaultValue(el.getAttribute("type"), null));
                buf.append(";\n\n");
            }
        }

        for (int j = 0; j < attrs.getLength(); j++) {
            Element el = (Element) attrs.item(j);

            buf.append("        ");
            buf.append(toJavaTypeFromXsdType(el.getAttribute("type")));
            buf.append(isArray(el.getAttribute("maxOccurs")) ? "[]" : "");
            buf.append(" ");
            buf.append(CustomXPathFunctions.toJavaNameFromDBName(el.getAttribute("name"), false));
            buf.append("() default ");
            buf.append(toDefaultValue(el.getAttribute("type"), el.getAttribute("default")));
            buf.append(";");
            buf.append("\n\n");
        }

        return buf.toString();
    }

    /**
     * Given the input directory scan for all XML files, using the templateFilename
     * convert the XML file to a Java POJO bean.
     * 
     * @param indir
     *            the directory containing all the model XML files, which contain
     *            the database/table definitions.
     * @param outdir
     *            the directory to which all java bean files are to be written.
     * @param templateFilename
     *            the filename of the template to use to generate the java pojo
     *            beans.
     * @param databaseName
     *            the name of the database to be processed, (deprecated this should
     *            be in the model XML file).
     * @param filter
     *            the filter on the input files to be applied.
     * @param params
     *            hash map of params to feed into the template file processing.
     * 
     * @throws Exception
     */
    public void generateBeanJoin(File modeldir, File indir, File outdir, List<Table> cache, Map<String, Object> params)
            throws Exception {

        outdir.mkdirs();

        if (cache == null) {
            System.out.println("Processing model directory: " + modeldir.getPath());
            List<Database> items = load(modeldir.getPath(), ".xml");
            cache = flattenTables(items);
        }

        for (File file : indir.listFiles()) {
            if (file.isDirectory()) {
                generateBeanJoin(modeldir, file, outdir, cache, params);
                continue;
            }

            System.out.println("Processing: " + file.getPath());
            Database database = DatabaseMapper.read(Database.class, file.getPath());

            for (Table table : database.getTables()) {
                if (table.getImportTables().size() > 0) {
                    for (String tablename : table.getImportTables()) {
                        for (Table importTable : cache) {
                            if (importTable.getName().matches("(?i)" + tablename)) {
                                mergeColumns(table, importTable, params);
                            }
                        }
                    }

                    sortColumnsByName(table.getColumns());
                    String outfilename = generateOutFilename(outdir, database.getName(), table.getName());
                    DatabaseMapper.writeDatabase(outfilename, database);
                }
            }
        }
    }

    private void compareMetaDatabaseInfo(String indir1, String indir2) throws Exception {

        List<String> attrs = Arrays.asList("required", "enum-value");
        IgnoreAttributeDifferenceEvaluator ignoreAttributeDifferenceEvaluator = new IgnoreAttributeDifferenceEvaluator(
                attrs);

        Map<String, String> items1 = loadFiles(indir1, ".xml");
        Map<String, String> items2 = loadFiles(indir2, ".xml");

        if (items1.size() != items2.size()) {
            System.out.println("Different number of files in the two directories: " + indir1 + " vs" + indir2);
        }

        List<String> attrsList = new ArrayList<String>();

        for (String key1 : items1.keySet()) {
            String file1 = items1.get(key1);
            String file2 = items2.get(key1);

            if (file1 == null || file2 == null) {
                System.out.println("Missing file in the directories: " + key1);
                continue;
            }

            Diff diff = DiffBuilder.compare(file1).withTest(file2).ignoreWhitespace().build();

            Iterator<Difference> iter = diff.getDifferences().iterator();

            if (diff.hasDifferences()) {
                System.out.println("Differences: " + key1);

                while (iter.hasNext()) {
                    Difference d = iter.next();

                    String xpath = d.getComparison().getTestDetails().getXPath();
                    if (xpath != null) {
                        int index = xpath.lastIndexOf('@');
                        if (index != -1) {
                            String name = xpath.substring(index);
                            if (!attrsList.contains(name)) {
                                attrsList.add(name);
                            }
                        }

                        if (xpath.contains("/enum-value[")) {
                            continue;
                        }
                        if (xpath.endsWith("@display-size")) {
                            continue;
                        }
                        if (xpath.endsWith("@required")) {
                            continue;
                        }
                    }

                    if (d.getComparison().getType() == ComparisonType.ATTR_NAME_LOOKUP) {
                        if (d.getResult() == ComparisonResult.DIFFERENT) {
                            System.out.println("\tDIFFERENT: " + d.toString());
                        }
                        if (d.getResult() == ComparisonResult.SIMILAR) {
                            System.out.println("\tSIMILAR: " + d.toString());
                        }
                    } else if (d.getComparison().getType() == ComparisonType.ELEMENT_NUM_ATTRIBUTES) {
                        ; // Not interest in count differences, but which attrs are add or removed.

                    } else if (d.getComparison().getType() != ComparisonType.ATTR_VALUE) {
                        if (d.getResult() == ComparisonResult.DIFFERENT) {
                            System.out.println("\tDIFFERENT: " + d.toString());
                        }
                        if (d.getResult() == ComparisonResult.SIMILAR) {
                            System.out.println("\tSIMILAR: " + d.toString());
                        }
                    }
                }
            } else {
                System.out.println("NO Differences: " + key1);
            }
        }

        System.out.println("AFFECTED ATTRIBUTES: ");
        for (String name : attrsList) {
            System.out.println("\tATTR: " + name);
        }
    }

    private List<Table> loadTables(File indir, String packagename) throws Exception {

        System.out.println("Processing model directory: " + indir.getPath());
        List<Database> items = load(indir.getPath(), ".xml");
        List<Table> tables = flattenTables(items);
        for (Table table : tables) {
            for (Column column : table.getColumns()) {
                if (!hasColumn(table, column)) {
                    column.setTableName(table.getName());
                    table.getColumns().add(adjustEnumColumn(packagename, column));
                }
            }
        }
        return tables;
    }

    private void processImportTable(String modelpackage, Database database, List<Table> cache,
            Map<String, Object> params) throws Exception {

        for (Table table : database.getTables()) {
            if (table.getImportTables().size() > 0) {
                for (String tablename : table.getImportTables()) {
                    for (Table importTable : cache) {
                        if (importTable.getName().matches("(?i)" + tablename)) {
                            mergeColumns(table, importTable, params);
                        }
                    }
                }
                sortColumnsByName(table.getColumns());
            }
        }
    }

    public void generateTest(File infile, File outdir, String template, String packageName, String extension,
            Map<String, Object> params) throws Exception {

        if (!infile.exists()) {
            return;
        }

        if (infile.isDirectory()) {
            for (File file : infile.listFiles()) {
                if (file.isDirectory()) {
                    packageName = packageName + "." + file.getName();
                    generateTest(file, new File(outdir, file.getName()), template, packageName, extension, params);
                } else {
                    generateTest(file, outdir, template, packageName, extension, params);
                }
            }
        }

        if (infile.isFile()) {
            if (!infile.getName().endsWith(extension)) {
                System.out.println("Skipping " + infile.getName() + ", fails the filter test: " + extension);
                return;
            }

            outdir.mkdirs();

            String declaringPackage = packageName;
            String classname = "Test" + infile.getName().substring(0, infile.getName().length() - extension.length());
            String declaringClass = infile.getName().substring(0, infile.getName().length() - extension.length());

            System.out.println("*** Processing: " + infile.getPath() + ":" + declaringPackage + "." + declaringClass);

            Class clazz = Class.forName(declaringPackage + "." + declaringClass);

            params.put("root", clazz);
            params.put("infilename", infile.getPath());
            params.put("outfilename", new File(outdir, classname).getPath());
            params.put("packagename", packageName);
            params.put("classname", classname);
            params.put("declaringClass", declaringClass);
            params.put("declaringPackage", declaringPackage);

            String testStr = jexl.evaluate(template, params, null);

            File outfile = new File(outdir, "Test" + infile.getName());
            Files.write(outfile.toPath(), testStr.getBytes());
        }
    }

    /**
     * Convert the given XML Document object to a String object. If an error occurs,
     * log it and return a null object.
     * 
     * @param dao
     *            The database interface (DAO) object, which maybe null.
     * @param indir
     *            the input directory where files will be processed, read in.
     * @param filter
     *            a filter to be applied to the input files.
     * @param dbc
     *            the database connection object, contains database url, username,
     *            password, etc.
     * 
     * @throws Exception
     */
    public void generateSeed(DatabaseInterface dao, File indir, String filter, DatabaseConnection dbc)
            throws Exception {

        if (dao == null) {
            dao = DatabaseFactory.getInstance(dbc);
        }

        for (File file : indir.listFiles()) {
            if (file.isDirectory()) {
                generateSeed(dao, file, filter, dbc);
                continue;
            }

            if (!file.getName().endsWith(filter)) {
                System.out.println("Skipping " + file.getName() + ", fails the filter test: " + filter);
                continue;
            }

            System.out.println("Processing seeding: " + file.getPath());

            Database database = DatabaseMapper.read(Database.class, file.getPath());

            for (Table table : database.getTables()) {
                fillInColumnNames(table);
                Class clazz = DatabaseUtil.toTableClass(database.getPackageName(), database.getName(), table.getName());
                dao.insertAll(DatabaseMapper.toObjects(clazz, table.getRows()));
            }
        }
    }

    /**
     * Given a database connection, generate beans for all tables in the database
     * connection, could be the entire database.
     * 
     * @param connection
     *            the database connection object, contains database URL, username,
     *            password, etc.
     * @throws Exception
     */
    public void generateBeans(DatabaseConnection connection) throws Exception {
        int iteration = 1;

        DatabaseInterface database = DatabaseFactory.getInstance(connection);
        List<Class> clazzes = DatabaseUtil.getClasses(connection.getPackageNames());
        Class[] classes = new Class[clazzes.size()];
        int counter = 0;
        for (Class clazz : clazzes) {
            classes[counter++] = clazz;
        }

        Arrays.sort(classes, new BeanComparator());

        for (Class clazz : classes) {
            com.viper.database.annotations.Table table = (com.viper.database.annotations.Table) clazz
                    .getAnnotation(com.viper.database.annotations.Table.class);
            if (table == null) {
                continue;
            }
            if (table.iterations() != -1) {
                continue;
            }
            if (!"table".equalsIgnoreCase(table.tableType())) {
                continue;
            }

            List beans = database.queryAll(clazz);
            RandomBean.setTableData(table.databaseName(), table.name(), beans);
        }

        for (Class clazz : classes) {
            com.viper.database.annotations.Table table = (com.viper.database.annotations.Table) clazz
                    .getAnnotation(com.viper.database.annotations.Table.class);
            if (table.iterations() == -1) {
                continue;
            }
            if (!"table".equalsIgnoreCase(table.tableType())) {
                continue;
            }
            int nitems = table.iterations();
            if (nitems == 0) {
                nitems = 100;
            }

            System.out.println("Generating table : " + clazz.getName() + ", nitems: " + nitems);
            List beans = RandomBean.getRandomBeans(clazz, iteration, nitems);
            database.insertAll(beans);

            RandomBean.setTableData(table.databaseName(), table.name(), beans);
        }
    }

    public void generateBeans(String indir, DatabaseConnection connection) throws Exception {

        DatabaseInterface database = DatabaseFactory.getInstance(connection);

        List<Database> databases = load(indir, ".xml");

        for (Database item : databases) {
            String packagename = item.getPackageName();
            if (packagename == null || packagename.length() == 0) {
                packagename = connection.getPackageNames().get(0) + "." + item.getName();
            }
            List<Class> classes = DatabaseUtil.getDatabaseClasses(packagename);

            for (Class clazz : classes) {
                com.viper.database.annotations.Table table = (com.viper.database.annotations.Table) clazz
                        .getAnnotation(com.viper.database.annotations.Table.class);

                if (!"table".equalsIgnoreCase(table.tableType())) {
                    continue;
                }
                int nitems = table.iterations();
                if (nitems == 0) {
                    nitems = 100;
                }

                int iteration = 1;

                if (nitems != -1) {
                    List beans = RandomBean.getRandomBeans(clazz, iteration, nitems);
                    database.insertAll(beans);
                    iteration = iteration + 1;
                }
            }
        }
    }

    public void generateResources(File infile, Properties properties, String extension) throws Exception {

        if (!infile.exists()) {
            return;
        }

        if (infile.isDirectory()) {
            for (File file : infile.listFiles()) {
                generateResources(file, properties, extension);
            }
        }

        if (infile.isFile()) {
            if (!infile.getName().endsWith(extension)) {
                System.out.println("Skipping " + infile.getName() + ", fails the filter test: " + extension);
                return;
            }

            Database database = DatabaseMapper.read(Database.class, infile.getPath());

            for (Table table : database.getTables()) {
                for (Column column : table.getColumns()) {
                    if (column.getName() != null) {
                        String key = column.getName().toLowerCase();
                        if (!properties.containsKey(key)) {
                            properties.put(key, CustomXPathFunctions.toEnglishFromDBName(column.getName(), true));
                        }

                        String name = CustomXPathFunctions.toJavaVariableName(column);
                        if (name != null) {
                            key = name.toLowerCase();
                            if (!properties.containsKey(key)) {
                                properties.put(key, CustomXPathFunctions.toEnglishFromDBName(column.getName(), true));
                            }
                        }
                    }
                    if (column.getField() != null) {
                        String key = column.getField().toLowerCase();
                        if (!properties.containsKey(key)) {
                            properties.put(key, CustomXPathFunctions.toEnglishFromDBName(column.getField(), true));
                        }
                    }
                    if (column.getEnumValues() != null) {
                        for (EnumItem item : column.getEnumValues()) {
                            if (item.getName() != null) {
                                String key = item.getName().toLowerCase();
                                if (!properties.containsKey(key)) {
                                    String value = CustomXPathFunctions.toEnglishFromDBName(item.getName(), true);
                                    properties.put(key, value);
                                }
                            }
                            if (item.getValue() != null) {
                                String key = item.getValue().toLowerCase();
                                if (!properties.containsKey(key)) {
                                    String value = CustomXPathFunctions.toEnglishFromDBName(item.getValue(), true);
                                    properties.put(key, value);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void printJsonTemplate(String packagename, File infile, String outdir, String extension) throws Exception {

        if (!infile.exists()) {
            return;
        }

        if (infile.isDirectory()) {
            for (File file : infile.listFiles()) {
                printJsonTemplate(packagename, file, outdir, extension);
            }
        }

        if (infile.isFile()) {
            if (!infile.getName().endsWith(extension)) {
                System.out.println("Skipping " + infile.getName() + ", fails the filter test: " + extension);
                return;
            }

            Database database = DatabaseMapper.read(Database.class, infile.getPath());

            for (Table table : database.getTables()) {
                System.out.println("Processing " + infile.getAbsolutePath());

                String layoutName = table.getName().toLowerCase();

                String outfile = outdir + "/" + layoutName + ".json";
                JSONObject root = new JSONObject();
                JSONArray components = new JSONArray();

                putValue(root, "title", layoutName);
                putValue(root, "name", layoutName);
                putValue(root, "type", "form");
                putValue(root, "display", "form");
                putValue(root, "action", "{url-to-sendd-form-data}");
                putValue(root, "components", components);
                putValue(root, "_id", UUID.randomUUID().toString());
                // putValue(root, "path", "");
                // putValue(root, "tags", "{???}");
                // putValue(root, "project", "{???}");
                // putValue(root, "owner", "{???}");
                // putValue(root, "access", "{array of roles}");
                // putValue(root, "submissionAccess", "{array of roles}");

                JSONArray tableList = new JSONArray();
                root.put("tables", tableList);

                JSONObject tableObj = new JSONObject();
                putValue(tableObj, "name", table.getName());
                putValue(tableObj, "classname", packagename + "." + CustomXPathFunctions.toJavaName(table));
                putValue(tableObj, "packagename", database.getPackageName());
                putValue(tableObj, "databasename", database.getName());
                putValue(tableObj, "tablename", table.getName());
                tableList.put(tableObj);

                for (Column column : table.getColumns()) {

                    JSONObject component = new JSONObject();
                    components.put(components.length(), component);

                    String columnname = getFullColumnName(packagename, table, column.getName());

                    putValue(component, "label", columnname);
                    putValue(component, "key", columnname);
                    putValue(component, "placeHolder", "{prompt}");
                    putBoolean(component, "hidden", false);
                    putValue(component, "validate", "custom");
                    // putValue(component, "type", "textfield"); // column.getComponentType());
                    // putBoolean(component, "input", false);
                    // putBoolean(component, "tableView", false);
                    // putBoolean(component, "multiple", false);
                    // putBoolean(component, "protected", false);
                    // putValue(component, "prefix", "");
                    // putValue(component, "suffix", "");
                    // putValue(component, "defaultValue", "{value}");
                    // putBoolean(component, "clearOnHide", false);
                    // putBoolean(component, "unique", true);
                    // putBoolean(component, "persistent", true);
                    // putValue(component, "conditional", "{false}");
                    // putValue(component, "errors", "{strings}");
                    // putValue(component, "logic", "{strings}");

                    putValue(component, "converter", column.getConverter());
                    putValue(component, "description", column.getDescription());
                    // putValue(component, "format", "{format}");
                    // putInteger(component, "maximumsize", Integer.valueOf((int)column.getSize()));
                    // putValue(component, "maximumvalue", column.getMaximumValue());
                    // putValue(component, "minimumvalue", column.getMinimumValue());
                }

                for (int i = 0; i < 2; i++) {
                    JSONObject button = new JSONObject();
                    putValue(button, "label", "title");
                    putValue(button, "type", "button");
                    putValue(button, "size", "md");
                    putValue(button, "righticon", "fa-plus");
                    putBoolean(button, "block", false);
                    putValue(button, "action", "submit");
                    putValue(button, "theme", "primary");
                    components.put(components.length(), button);
                }

                Files.write(new File(outfile).toPath(), root.toString().getBytes());
            }
        }
    }

    public void generateTemplates(String packagename, File infile, String outdir, String extension) throws Exception {

        if (!infile.exists()) {
            return;
        }

        if (infile.isDirectory()) {
            for (File file : infile.listFiles()) {
                generateTemplates(packagename, file, outdir, extension);
            }
        }

        if (infile.isFile()) {
            if (!infile.getName().endsWith(extension)) {
                System.out.println("Skipping " + infile.getName() + ", fails the filter test: " + extension);
                return;
            }

            Database database = DatabaseMapper.read(Database.class, infile.getPath());

            for (Table table : database.getTables()) {

                String layoutName = table.getName().toLowerCase();

                String outfile = outdir + "/" + layoutName + ".xml";
                Params params = loadParams(outfile);

                if (params == null) {
                    params = new Params();
                    putValue(params.getAttrs(), "page", "formname", layoutName);
                    putValue(params.getAttrs(), "page", "title", "{Title}");
                    putValue(params.getAttrs(), "page", "validator", "{validator}");

                    for (int i = 0; i < 4; i++) {

                        putValue(params.getAttrs(), "button" + 1, "click", "{click}");
                        putValue(params.getAttrs(), "button" + 1, "css", "{css}");
                        putValue(params.getAttrs(), "button" + 1, "title", "{title}");
                    }

                }

                putValue(params.getTables(), table.getName(), "classname",
                        packagename + "." + CustomXPathFunctions.toJavaName(table));
                putValue(params.getTables(), table.getName(), "packagename", database.getPackageName());
                putValue(params.getTables(), table.getName(), "databasename", database.getName());
                putValue(params.getTables(), table.getName(), "tablename", table.getName());

                for (Column column : table.getColumns()) {
                    String columnname = getFullColumnName(packagename, table, column.getName());

                    putValue(params.getAttrs(), columnname, "name",
                            getFullColumnName(packagename, table, column.getName()));
                    putValue(params.getAttrs(), columnname, "type", column.getComponentType());
                    putValue(params.getAttrs(), columnname, "converter", column.getConverter());
                    putValue(params.getAttrs(), columnname, "description", column.getDescription());
                    putValue(params.getAttrs(), columnname, "mode", "display");
                    putValue(params.getAttrs(), columnname, "format", "{format}");
                    putValue(params.getAttrs(), columnname, "fieldName", column.getName());
                    putValue(params.getAttrs(), columnname, "prompt", "{prompt}");
                    putValue(params.getAttrs(), columnname, "maximumsize", Long.toString(column.getSize()));
                    putValue(params.getAttrs(), columnname, "dataType", "{data-type}");
                    putValue(params.getAttrs(), columnname, "css", "{css-class}");
                    putValue(params.getAttrs(), columnname, "maximumvalue", column.getMaximumValue());
                    putValue(params.getAttrs(), columnname, "minimumvalue", column.getMinimumValue());

                    putValue(params.getFields(), columnname, "value", "{value}");

                }

                DatabaseMapper.write(outfile, params, null);
            }
        }
    }

    private final static <T> String getFullColumnName(String packagename, Table table, String columnName) {

        return CustomXPathFunctions.toJavaName(table) + "." + columnName;
    }

    /**
     * 
     * @param indir
     * @param outfile
     * @param filter
     * @throws Exception
     */
    public void exportDATA(String indir, String outfile, String filter) throws Exception {

        List<Database> databases = load(indir, filter);

        String extension = getFileExtension(outfile);
        if ("json".equalsIgnoreCase(extension)) {
            exportJSON(databases, outfile);
        } else if ("xml".equalsIgnoreCase(extension)) {
            exportXML(databases, outfile);
        } else if ("java".equalsIgnoreCase(extension)) {
            exportBeans(databases, outfile);
        } else if ("csv".equalsIgnoreCase(extension)) {
            exportCSV(databases, outfile);
        }
    }

    /**
     * 
     * @param items
     * @param outfile
     * @throws Exception
     */
    public void exportJSON(List<Database> items, String outfile) throws Exception {

        new File(outfile).getParentFile().mkdirs();

        Databases databases = new Databases();
        databases.getDatabases().addAll(items);
        DatabaseMapper.writeDatabases(outfile, databases);

    }

    /**
     * 
     * @param databases
     * @param outfile
     * @throws Exception
     */
    public void exportXML(List<Database> databases, String outfile) throws Exception {

        new File(outfile).getParentFile().mkdirs();

        PrintStream out = new PrintStream(outfile);
        out.println("<?xml version='1.0' encoding='ISO-8859-1' standalone='no' ?>");
        out.println("<databases>");

        for (Database database : databases) {
            out.println("<database name=\"" + database.getName() + "\">");

            for (Table table : database.getTables()) {
                System.out.println("Processing: " + table.getName());

                out.println("<table name=\"" + table.getName() + "\">");

                for (Row row : table.getRows()) {
                    out.println("<row>");
                    for (Cell cell : row.getCells()) {
                        out.println("<cell field=\"" + cell.getName() + "\">" + cell.getValue() + "</cell>");
                    }
                    out.println("</row>");
                }
                out.println("</table>");
            }
            out.println("</database>");
        }
        out.println("</databases>");
        out.flush();
        out.close();
    }

    /**
     * 
     * @param databases
     * @param outfile
     * @throws Exception
     */
    public void exportBeans(List<Database> databases, String outfile) throws Exception {

        String packageName = getPackageName(outfile);
        String className = getClassName(outfile);

        new File(outfile).getParentFile().mkdirs();

        PrintStream out = new PrintStream(outfile);
        out.println("package " + packageName + ";");
        out.println("");
        out.println("import java.util.List;");
        out.println("import java.util.ArrayList;");
        out.println("");
        out.println("public class " + className + " {");
        for (Database database : databases) {
            out.println("// database \"" + database.getName() + "\"");

            for (Table table : database.getTables()) {
                System.out.println("Processing: " + table.getName());

                out.println("List<" + table.getName() + "> " + table.getName() + "List = new ArrayList<"
                        + table.getName() + ">();");

                for (Row row : table.getRows()) {
                    out.println("{");
                    out.println(table.getName() + " " + table.getName() + "Row = new " + table.getName() + "();");
                    for (Cell cell : row.getCells()) {
                        out.println(table.getName() + "Row.set" + cell.getName() + "(" + cell.getValue() + ");");
                    }
                    out.println(table.getName() + "List.add(" + table.getName() + "Row);");
                    out.println("}");
                }
            }
        }
        out.println("}");

        out.flush();
        out.close();
    }

    /**
     * 
     * @param databases
     * @param outfile
     * @throws Exception
     */
    public void exportCSV(List<Database> databases, String outfile) throws Exception {

        new File(outfile).getParentFile().mkdirs();

        int maxColumns = getMaxColumns(databases);

        PrintStream out = new PrintStream(outfile);
        out.print("Database,");
        out.print("Table");
        for (int i = 0; i < maxColumns; i++) {
            out.print(", Col-" + i);
        }
        out.println("");

        for (Database database : databases) {
            for (Table table : database.getTables()) {
                out.print(database.getName());
                out.print(", " + table.getName());
                for (Column column : table.getColumns()) {
                    out.print(", " + column.getName());
                }
                out.println("");
            }
        }
        out.flush();
        out.close();
    }

    /**
     * 
     */
    public void generateDictionary(String infile, String outfile) throws Exception {
        
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        WikiHandler handler = new WikiHandler(outfile);
        
        saxParser.parse(new File(infile), handler);
    }

    /**
     * @param dao
     *            the database connection object, contains database url, username,
     *            password, etc.
     * 
     * @param databaseName
     * 
     * @param outdir
     * 
     * @throws Exception
     */
    public void importSchema(DatabaseSQLInterface dao, String databaseName, String outdir) throws Exception {

        SQLDriver driver = new SQLDriver("mysql");

        Databases databases = driver.load(dao, databaseName, null);

        for (Database database : databases.getDatabases()) {
            if (database.getTables().size() == 0) {
                continue;
            }

            List<Trigger> triggers = new ArrayList<Trigger>(database.getTriggers());
            List<Procedure> procedures = new ArrayList<Procedure>(database.getProcedures());
            List<Table> tables = new ArrayList<Table>(database.getTables());
            for (Table table : tables) {
                database.getTables().clear();
                database.getTables().add(table);
                database.getTriggers().clear();
                database.getProcedures().clear();

                // Clear Table/Database names, overkill
                for (Column column : table.getColumns()) {
                    column.setDatabaseName(null);
                    column.setTableName(null);
                }

                String filename = outdir + "/" + databaseName + "/" + table.getName() + ".xml";
                database.setFilename(filename);

                FileUtil.mkPath(filename);

                System.out.println("Writing to file: " + database.getFilename() + ", columns="
                        + database.getTables().get(0).getColumns().size());

                try {
                    Database oldDatabase = DatabaseMapper.readDatabase(database.getFilename());
                    DatabaseMapper.mergeDatabase(oldDatabase, database);
                } catch (Exception ex) {
                    System.out.println("FYI: failed to read existin xml file: " + database.getFilename());
                }
                DatabaseMapper.writeDatabase(database.getFilename(), database);
            }

            // Triggers
            if (triggers != null && triggers.size() > 0) {
                database.getTables().clear();
                database.getProcedures().clear();
                database.getTriggers().addAll(triggers);
                database.setFilename(outdir + "/" + databaseName + "/triggers.xml");
                System.out.println("Writing to file: " + database.getFilename());

                DatabaseMapper.writeDatabase(database.getFilename(), database);
            }

            // Procedures
            if (procedures != null && procedures.size() > 0) {
                database.getTables().clear();
                database.getProcedures().addAll(procedures);
                database.getTriggers().clear();
                database.setFilename(outdir + "/" + databaseName + "/procedures.xml");
                System.out.println("Writing to file: " + database.getFilename());

                DatabaseMapper.writeDatabase(database.getFilename(), database);
            }
        }
    }

    /**
     * 
     * @param databases
     * @param outfile
     * @throws Exception
     */
    public void exportDocument(List<Database> databases, String outfile) throws Exception {

        new File(outfile).getParentFile().mkdirs();

        PrintStream out = new PrintStream(outfile);
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Databases</title>");
        out.println("<link rel='stylesheet' type='text/css' href='table.css'>");
        out.println("</head>");
        out.println("<body>");
        out.println("<p>");

        for (Database database : databases) {

            for (Table table : database.getTables()) {
                out.println("<table width='100%'>");
                out.println("<tr>");
                out.println("<th colspan='6'><b>" + database.getName() + "." + table.getName() + "</b></th>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<th colspan='6'>" + table.getDescription() + "</th>");
                out.println("</tr>");

                out.println("<tr>");
                out.println("<th>Field</th>");
                out.println("<th>isPrimaryKey</th>");
                out.println("<th>JavaType</th>");
                out.println("<th>Simulation</th>");
                out.println("<th>Description</th>");
                out.println("<th>Comments</th>");
                out.println("</tr>");

                for (Column column : table.getColumns()) {
                    out.println("<tr>");
                    out.println("<td><b>" + column.getName() + "</b></td>");
                    out.println("<td>" + toString(column.isPrimaryKey()) + "</td>");
                    out.println("<td>" + column.getJavaType() + "</td>");
                    out.println("<td>" + toString(column.getLogicalType()) + "</td>");
                    out.println("<td>" + toString(column.getDescription()) + "</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
                out.println("<p>");
                out.println("<br>");
                out.println("<br>");
            }
        }
        out.println("</body>");
        out.println("</html>");

        out.flush();
        out.close();
    }

    private String toString(Boolean flag) {
        return (!flag) ? "&nbsp;" : Boolean.toString(flag);
    }

    private String toString(String value) {
        return (value == null) ? "&nbsp;" : value;
    }

    private void mergeColumns(Table table, Table importTable, Map<String, Object> params) {
        if (importTable != null) {
            for (Column column : importTable.getColumns()) {
                if (!hasColumn(table, column)) {
                    column.setTableName(importTable.getName());

                    if (table.isFieldsAllNullable()) {
                        String tablename = importTable.getName();
                        if (importTable.getTableName() != null && !importTable.getTableName().isEmpty()) {
                            tablename = importTable.getTableName();
                        }
                        String javaType = SQLDriver.toJavaType(tablename, column.getName(), column.getDataType(),
                                false);
                        if (javaType != null) {
                            column.setJavaType(javaType);
                            column.setDefaultValue(null);
                        }
                    }
                    table.getColumns().add(column);

                } else {
                    Column column1 = findColumn(table, column);
                    if (column1 != null) {
                        if ("enum".equalsIgnoreCase(column1.getDataType())) {
                            column1.setJavaType(makeNewEnumType(column1.getJavaType(), table, params));
                            mergeEnumList(column1.getEnumValues(), column.getEnumValues());
                        }
                        column1.setTableName("ALL");
                    }
                }
            }
        }
    }

    private String makeNewEnumType(String oldname, Table table, Map<String, Object> params) {
        int index = oldname.lastIndexOf('.');
        if (index == -1) {
            return oldname;
        }
        if (oldname.toLowerCase().contains("." + table.getName().toLowerCase())) {
            return oldname;
        }
        String packagename = oldname.substring(0, index);
        if (params != null && params.containsKey("packagename")) {
            packagename = (String) params.get("packagename");
        }
        return table.getName() + oldname.substring(index + 1);
    }

    private void mergeEnumList(List<EnumItem> toItems, List<EnumItem> fromItems) {
        for (EnumItem fromItem : fromItems) {
            EnumItem toItem = find(toItems, fromItem);
            if (toItem == null) {
                toItems.add(fromItem);
            }
        }
    }

    private EnumItem find(List<EnumItem> items, EnumItem findItem) {
        for (EnumItem item : items) {
            if (findItem.getValue().equalsIgnoreCase(item.getValue())) {
                return item;
            }
        }
        return null;
    }

    private boolean hasColumn(Table table, Column column) {
        for (Column col : table.getColumns()) {
            if (col.getName().equalsIgnoreCase(column.getName())) {
                return true;
            }
        }
        return false;
    }

    private Database findDatabase(List<Database> items, String name) {
        for (Database database : items) {
            if (name.equalsIgnoreCase(database.getName())) {
                return database;
            }
        }
        return null;
    }

    private Table findTable(List<Table> items, String name) {
        for (Table table : items) {
            if (name.equalsIgnoreCase(table.getName())) {
                return table;
            }
        }
        return null;
    }

    private Column findColumn(Table table, Column column) {
        for (Column col : table.getColumns()) {
            if (col.getName().equalsIgnoreCase(column.getName())) {
                return col;
            }
        }
        return null;
    }

    private Column adjustEnumColumn(String packagename, Column column) {
        if (!"enum".equalsIgnoreCase(column.getDataType())) {
            return column;
        }
        if (column.getEnumValues().size() == 0) {
            return column;
        }
        column.setJavaType(packagename + ".enums." + column.getJavaType());
        column.setExtraDataType(null);
        column.getEnumValues().clear();
        return column;
    }

    /**
     * 
     * @param indir
     * @param outfile
     * @param filter
     * @throws Exception
     */
    private List<Database> load(String indir, String filter) throws Exception {

        List<Database> databases = new ArrayList<Database>();

        File inDirectory = new File(indir);
        for (File infile : inDirectory.listFiles()) {
            if (infile.isDirectory()) {
                databases.addAll(load(infile.getPath(), filter));
                continue;
            }

            if (!infile.getName().endsWith(filter)) {
                System.out.println("Skipping " + infile.getName() + ", fails the filter test: " + filter);
                continue;
            }

            Database database = DatabaseMapper.read(Database.class, infile.getPath());
            if (database != null) {
                databases.add(database);
            }
        }

        return databases;
    }

    /**
     * 
     * @param indir
     * @param outfile
     * @param filter
     * @throws Exception
     */
    private Map<String, String> loadFiles(String indir, String filter) throws Exception {

        Map<String, String> map = new HashMap<String, String>();

        File inDirectory = new File(indir);
        for (File infile : inDirectory.listFiles()) {
            if (infile.isDirectory()) {
                map.putAll(loadFiles(infile.getPath(), filter));
                continue;
            }

            if (!infile.getName().endsWith(filter)) {
                System.out.println("Skipping " + infile.getName() + ", fails the filter test: " + filter);
                continue;
            }

            String file = readFile(infile.getPath());
            map.put(infile.getName().toLowerCase(), file);
        }

        return map;
    }

    /**
     * 
     * @param filename
     * 
     * @return
     * 
     * @throws IOException
     */
    private InputStream getInputStream(String filename) throws Exception {
        if (filename == null || filename.length() == 0) {
            throw new Exception("No filename specified.");
        }

        InputStream in = null;
        if (filename.startsWith("http:")) {
            in = new URL(filename).openStream();
        } else if (filename.startsWith("classpath:")) {
            URL url = Schematool.class.getResource(filename.substring("classpath:".length()));
            in = url.openStream();
        } else if (filename.startsWith("res:")) {
            URL url = Schematool.class.getResource(filename.substring("res:".length()));
            in = url.openStream();
        } else {
            in = new FileInputStream(filename);
        }
        return in;
    }

    // -------------------------------------------------------------------------
    private String readFile(String filename) throws Exception {
        try {
            final InputStream fis = getInputStream(filename);
            final int len = fis.available();
            final byte[] bytes = new byte[len];
            final int actualLen = fis.read(bytes);

            if (actualLen != len) {
                System.out.println(filename + " wrong number of bytes: " + actualLen + " vs " + len);
            }
            fis.close();
            return new String(bytes);
        } catch (Exception ex) {
            throw new Exception("Unable to read filename: " + filename, ex);
        }
    }

    private String readFileWithIncludes(String filename) throws Exception {

        BufferedReader br = null;
        StringBuilder buf = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader(getInputStream(filename)));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                if (line.trim().startsWith("##") && line.trim().substring(2).trim().startsWith("import(")) {

                    int i1 = line.indexOf('(') + 1;
                    int i2 = line.indexOf(')');
                    String importFilename = line.substring(i1, i2);
                    String str = readFile(importFilename);
                    buf.append(str);

                } else {

                    buf.append(line);
                    buf.append('\n');
                }
            }
        } catch (Exception ex) {
            throw new Exception("Unable to read filename: " + filename, ex);
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return buf.toString();
    }

    private String getFileExtension(String filename) {
        String extension = null;

        int index = filename.lastIndexOf('.');
        if (index >= 0) {
            extension = filename.substring(index + 1);
        }
        return extension;
    }

    private String getPackageName(String filename) {
        String packageName = null;

        filename = filename.replace('\\', '/');

        int index1 = filename.indexOf("/srcgen/");
        if (index1 == -1) {
            return packageName;
        }
        int index2 = filename.lastIndexOf('/');
        if (index2 == -1) {
            return packageName;
        }
        return filename.substring(index1 + "srcgen".length(), index2).replace('/', '.');
    }

    private String getClassName(String filename) {
        String classname = null;

        filename = filename.replace('\\', '/');

        int index1 = filename.lastIndexOf('/');
        if (index1 == -1) {
            return classname;
        }
        int index2 = filename.lastIndexOf('.');
        if (index2 == -1) {
            return classname;
        }
        return filename.substring(index1 + 1, index2);
    }

    private void fillInColumnNames(Table table) {
        for (Row row : table.getRows()) {
            for (int i = 0; i < table.getColumns().size(); i++) {
                String name = CustomXPathFunctions.toJavaVariableNameFromField(table.getColumns().get(i).getName());
                if (i < row.getCells().size()) {
                    Cell cell = row.getCells().get(i);
                    if (cell.getName() == null) {
                        cell.setName(name);
                    }
                }
            }
        }
    }

    private void displayHelp() {
        System.out.println("java -jar schematool.jar <options> <commands>");
        System.out.println("  options:");
        System.out.println("    -source [source name]; see databases.xml");
        System.out.println("    -vendor [vendor name]; ie mysql, oracle");
        System.out.println("    -username [username]; username to access database");
        System.out.println("    -password [password]; password to access database");
        System.out.println("    -driver [driver classname]");
        System.out.println("    -url [database access url]");
        System.out.println("    -verbose");
        System.out.println("  commands:");
        System.out.println("    -validate [directory]; validates all xml files ");
        System.out.println("       in directory, and all children directories");
        System.out.println("    -merge [directory] [outputfilename]; merge all xml files ");
        System.out.println("       in directory, and all children directories, into single ");
        System.out.println("       output file ");
        System.out.println("    -import [directories]; given xml files in all directories and children ");
        System.out.println("       then generates sql, and writes the sql to the database ");
        System.out.println("    -export [database] [directory]; given the database name ");
        System.out.println("       generate the xml to the directory ");
        System.out.println("    -sql [directory]; given the direcctory subtree ");
        System.out.println("       write all sql files to the database ");
        System.out.println("    -simulation [model directory] [simulation directory] [output filename] ");
        System.out.println("       model directory: is subtree contains the xml model files ");
        System.out.println("       simulation directory: is subtree contains the xml simulation files ");
        System.out.println("       output filename: file to write sql scripts ");

    }

    private class BeanComparator<T> implements Comparator<T> {
        public int compare(T o1, T o2) {

            Class c1 = (Class) o1;
            Class c2 = (Class) o2;

            List<com.viper.database.annotations.Column> columns1 = DatabaseUtil.getColumnAnnotations(c1);
            com.viper.database.annotations.Table table1 = DatabaseUtil.getTableAnnotation(c1);

            List<com.viper.database.annotations.Column> columns2 = DatabaseUtil.getColumnAnnotations(c2);
            com.viper.database.annotations.Table table2 = DatabaseUtil.getTableAnnotation(c2);

            List<String> names1 = getLogicalTableNames(columns1);
            List<String> names2 = getLogicalTableNames(columns2);

            if (table1 == null && table2 == null) {
                return 0;
            }

            if (table1 == null) {
                log.severe("Table #1 is null: " + c1.getName());
                return -1;
            }

            if (table2 == null) {
                log.severe("Table #2 is null: " + c2.getName());
                return 1;
            }

            log.info("Compare #1" + table1.name() + "," + names1);
            log.info("Compare #2" + table2.name() + "," + names2);

            if (names2.contains(table1.name())) {
                return -1;
            }
            if (names1.contains(table2.name())) {
                return 1;
            }
            if (names1.size() == 0) {
                return -1;
            }
            if (names2.size() == 0) {
                return 1;
            }
            return names1.size() - names2.size();
        }

        public boolean equals(Object obj) {
            return this == obj;
        }
    }

    public void read(Reader reader, DatabaseConnection dbc) throws Exception {

        DatabaseSQLInterface dao = (DatabaseSQLInterface) DatabaseFactory.getInstance(dbc);
        BufferedReader in = new BufferedReader(reader);

        String line = null;
        while ((line = in.readLine()) != null) {
            dao.write(line);
        }
        in.close();
    }

    private String toJavaTypeFromXsdType(String xsd) {
        int index = xsd.indexOf(':');
        String str = xsd.substring(index + 1);

        switch (str) {
        case "anyURI":
            return "String";
        case "base64Binary":
            return "int";
        case "boolean":
            return "boolean";
        case "byte":
            return "byte";
        case "date":
            return "java.sql.Date";
        case "dateTime":
            return "java.sql.Timestamp";
        case "decimal":
            return "double";
        case "double":
            return "double";
        case "duration":
            return "long";
        case "float":
            return "double";
        case "gDay":
            return "String";
        case "gMonth":
            return "String";
        case "gMonthDay":
            return "String";
        case "gYear":
            return "String";
        case "gYearMonth":
            return "String";
        case "hexBinary":
            return "long";
        case "ID":
            return "long";
        case "IDREF":
            return "long";
        case "IDREFS":
            return "long";
        case "int":
            return "int";
        case "integer":
            return "int";
        case "language":
            return "String";
        case "long":
            return "long";
        case "Name":
            return "String";
        case "NCName":
            return "String";
        case "negativeInteger":
            return "long";
        case "NMTOKEN":
            return "String";
        case "NMTOKENS":
            return "String";
        case "nonNegativeInteger":
            return "int";
        case "nonPositiveInteger":
            return "int";
        case "normalizedString":
            return "String";
        case "positiveInteger":
            return "int";
        case "QName":
            return "String";
        case "short":
            return "short";
        case "string":
            return "String";
        case "time":
            return "java.sql.Time";
        case "token":
            return "String";
        case "unsignedByte":
            return "byte";
        case "unsignedInt":
            return "int";
        case "unsignedLong":
            return "long";
        case "unsignedShort":
            return "short";
        default:
            break;
        }

        return "String";
    }

    private String toDefaultValue(String xsd, String value) {
        int index = xsd.indexOf(':');
        String str = xsd.substring(index + 1);

        switch (str) {
        case "anyURI":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "base64Binary":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "boolean":
            return (value == null || value.isEmpty()) ? "false" : value;
        case "byte":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "date":
            return "java.sql.Date";
        case "dateTime":
            return "java.sql.Timestamp";
        case "decimal":
            return (value == null || value.isEmpty()) ? "0.0" : value;
        case "double":
            return (value == null || value.isEmpty()) ? "0.0" : value;
        case "duration":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "float":
            return (value == null || value.isEmpty()) ? "0.0" : value;
        case "gDay":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "gMonth":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "gMonthDay":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "gYear":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "gYearMonth":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "hexBinary":
            return (value == null) ? "0" : value;
        case "ID":
            return (value == null) ? "0" : value;
        case "IDREF":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "IDREFS":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "int":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "integer":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "language":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "long":
            return (value == null) ? "0" : value;
        case "Name":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "NCName":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "negativeInteger":
            return (value == null) ? "0" : value;
        case "NMTOKEN":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "NMTOKENS":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "nonNegativeInteger":
            return (value == null) ? "0" : value;
        case "nonPositiveInteger":
            return (value == null) ? "0" : value;
        case "normalizedString":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "positiveInteger":
            return (value == null) ? "0" : value;
        case "QName":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "short":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "string":
            return (value == null || value.isEmpty()) ? "\"\"" : "\"" + value + "\"";
        case "time":
            return "java.sql.Time";
        case "token":
            return (value == null) ? "\"\"" : "\"" + value + "\"";
        case "unsignedByte":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "unsignedInt":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "unsignedLong":
            return (value == null || value.isEmpty()) ? "0" : value;
        case "unsignedShort":
            return (value == null || value.isEmpty()) ? "0" : value;
        default:
            break;
        }

        return (value == null || value.isEmpty()) ? "\"\"" : "\"" + value + "\"";
    }

    private boolean isArray(String maxOccurs) {
        return (maxOccurs == null || maxOccurs.isEmpty() || "1".equals(maxOccurs)) ? false : true;
    }

    private Document parseToDocument(String filename) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new File(filename));
    }

    private List<String> getLogicalTableNames(List<com.viper.database.annotations.Column> columns) {
        List<String> items = new ArrayList<String>();
        for (com.viper.database.annotations.Column column : columns) {
            String logicalType = column.logicalType();
            if (logicalType.startsWith("table:")) {
                String tokens[] = logicalType.substring("table:".length()).split(",");
                items.add(tokens[1].trim());
            }
        }
        return items;
    }

    private final static int getMaxColumns(List<Database> databases) {

        int maxColumns = 0;
        for (Database database : databases) {
            for (Table table : database.getTables()) {
                if (table.getColumns().size() > maxColumns) {
                    maxColumns = table.getColumns().size();
                }
            }
        }
        return maxColumns;
    }

    private Properties load(String infile) {
        Properties env = new SortedProperties();

        File file = new File(infile);
        if (file != null && file.exists()) {
            try {
                env.load(new FileInputStream(file));
            } catch (Exception e) {
                ; // Ignore intentional
            }
        }
        return env;
    }

    private void save(String outfile, Properties props) {

        File file = new File(outfile);

        // outfile.mkdirs();

        try {
            props.store(new FileOutputStream(file), "");
        } catch (Exception e) {
            ; // Ignore intentional
        }
    }

    private List<Table> flattenTables(List<Database> items) {
        List<Table> cache = new ArrayList<Table>();

        for (Database item : items) {
            for (Table table : item.getTables()) {
                cache.add(table);
            }
        }
        return cache;
    }

    private static final void putValue(List<ParamType> params, String name, String attr, String value) {
        if (params != null) {
            for (ParamType param : params) {
                if (name.equalsIgnoreCase(param.getName()) && attr.equalsIgnoreCase(param.getAttribute())) {
                    param.setValue(value);
                    return;
                }
            }
        }
        ParamType param = new ParamType();
        param.setName(name);
        param.setAttribute(attr);
        param.setValue(value);
        params.add(param);
    }

    private static final void putValue(JSONObject obj, String name, Object value) {
        if (name != null && value != null) {
            obj.put(name, value);
        }
    }

    private static final void putBoolean(JSONObject obj, String name, boolean value) {
        if (name != null) {
            obj.put(name, value);
        }
    }

    private static final void putInteger(JSONObject obj, String name, Integer value) {
        if (name != null) {
            obj.put(name, value);
        }
    }

    private String generateOutFilename(File outdir, String databasename, String tablename) {
        return outdir.getAbsolutePath() + "/" + databasename + "/" + tablename + ".xml";
    }

    private Params loadParams(String filename) {

        try {
            return DatabaseMapper.read(Params.class, filename);
        } catch (Exception e) {
            ; // Ignore Intentionally
        }
        return null;
    }

    /**
     * Given the table class, and the database field name, return the column
     * annotation object.
     * 
     * @param clazz
     *            java table model, with annotations.
     * @param <T>
     *            the class of the pojo database bean, annotated with classes from
     *            package com.viper.database.annotations
     * @return the list of column annotations if defined with the class, zero size
     *         list otherwise.
     */
    private void sortColumnsByName(List<Column> columns) {
        Collections.sort(columns, new ColumnNameComparator());
    }

    private class ColumnNameComparator implements Comparator<Column> {
        @Override
        public int compare(Column o1, Column o2) {
            if (o1.getName() == null || o1.getName().length() == 0) {
                return o1.getField().compareToIgnoreCase(o2.getField());
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    private class IgnoreAttributeDifferenceEvaluator implements DifferenceEvaluator {

        private List<String> attributeNames;

        public IgnoreAttributeDifferenceEvaluator(List<String> attributeNames) {
            this.attributeNames = attributeNames;
        }

        @Override
        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
            if (outcome == ComparisonResult.EQUAL) {
                return outcome; // only evaluate differences.
            }
            final Node controlNode = comparison.getControlDetails().getTarget();
            if (controlNode instanceof Attr) {
                Attr attr = (Attr) controlNode;
                if (attributeNames.contains(attr.getName())) {
                    return ComparisonResult.SIMILAR; // will evaluate this difference as similar
                }
            }
            if (controlNode instanceof Element) {
                Element elem = (Element) controlNode;
                if (attributeNames.contains(elem.getTagName())) {
                    return ComparisonResult.SIMILAR; // will evaluate this difference as similar
                }
            }
            return outcome;
        }
    }

    public static void main(String args[]) {
        try {
            new Schematool().process(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
