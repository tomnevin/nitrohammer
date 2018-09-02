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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.viper.database.dao.DatabaseMapper;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.model.DatabaseConnections;

import net.sourceforge.jtds.jdbcx.JtdsDataSource;
import oracle.jdbc.pool.OracleDataSource;

public class DatabaseRegistry {

	private final static Logger log = Logger.getLogger(DatabaseRegistry.class.getName());

	public static final String DEFAULT_DATABASE_FILENAME = "res:/databases.xml";
	public static final String DEFAULT_PROVIDER_URL = "file:///tmp/JNDI";
	public static final String DEFAULT_FACTORY_CLASSNAME = "com.sun.jndi.fscontext.RefFSContextFactory";

	// "com.sun.jndi.ldap.LdapCtxFactory");
	// "com.sun.jndi.fscontext.RefFSContextFactory");
	// "com.sun.enterprise.naming.SerialInitContextFactory"

	private static Map<String, DatabaseRegistry> cache = new HashMap<String, DatabaseRegistry>();
	private Context ctx = null;

	public DatabaseRegistry() {

	}

	public static DatabaseRegistry getInstance() {
		return getInstance(DEFAULT_DATABASE_FILENAME);
	}

	public static DatabaseRegistry getInstance(String filename) {
		DatabaseRegistry registry = cache.get(filename);
		if (registry == null) {
			registry = new DatabaseRegistry();
			registry.bind(filename);
			cache.put(filename, registry);
		}
		return registry;
	}

	public void bind(String filename) {
		String url = null;
		try {
			DatabaseConnections connections = DatabaseMapper.readConnections(filename);
			String resourceName = connections.getResource();

			Properties env = new Properties();
			if (resourceName != null && resourceName.length() > 0) {
				env.load(getClass().getClassLoader().getResourceAsStream(resourceName));

			} else {

				env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_FACTORY_CLASSNAME);
				env.put(Context.PROVIDER_URL, DEFAULT_PROVIDER_URL);
				// env.put(Context.SECURITY_PRINCIPAL, "<user>");
				// env.put(Context.SECURITY_CREDENTIALS, "<password>");
			}

			ctx = new InitialContext(env);

			log.info("BINDING DataSource Url SIZE = " + connections.getConnections().size());

			for (DatabaseConnection connection : connections.getConnections()) {
				url = connection.getDatabaseUrl();

				log.info("BINDING DataSource Url = " + connection.getName() + "=" + url);

				if (url == null) {

				} else if (url.startsWith("jdbc:mysql:")) {
					MysqlConnectionPoolDataSource mds = new MysqlConnectionPoolDataSource();
					mds.setUrl(connection.getDatabaseUrl());
					mds.setUser(connection.getUsername());
					if (connection.getPassword() != null && connection.getPassword().length() > 0) {
						mds.setPassword(connection.getPassword());
					}
					ctx.rebind(connection.getName(), mds);

				} else if (url.startsWith("jdbc:oracle:")) {
					OracleDataSource ods = new OracleDataSource();
					ods.setURL(connection.getDatabaseUrl());
					ods.setUser(connection.getUsername());
					if (connection.getPassword() != null && connection.getPassword().length() > 0) {
						ods.setPassword(connection.getPassword());
					}
					ctx.rebind(connection.getName(), ods);

				} else if (url.startsWith("jdbc:jtds:sqlserver:") || url.startsWith("jdbc:jtds:sybase:")) {
					JtdsDataSource ds = new JtdsDataSource();
					StringTokenizer tokens = new StringTokenizer(url, ":/");
					tokens.nextToken();
					tokens.nextToken();
					tokens.nextToken();
					ds.setServerName(tokens.nextToken());
					ds.setPortNumber(Integer.parseInt(tokens.nextToken()));
					ds.setDatabaseName(tokens.nextToken());
					ds.setUser(connection.getUsername());
					if (connection.getPassword() != null && connection.getPassword().length() > 0) {
						ds.setPassword(connection.getPassword());
					}
					ctx.rebind(connection.getName(), ds);
				} else {
					log.severe("WARNING: UNKnown DataSource Url = " + url);
				}
			}

			NamingEnumeration<NameClassPair> list = ctx.list("");
			while (list.hasMore()) {
				System.out.println(list.next().getName());
			}
		} catch (Exception ex) {
			log.throwing("ERROR: DataSource Url = " + url, "", ex);
		}
	}

	public void bind() {
		try {
			Properties env = new Properties();
			env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_FACTORY_CLASSNAME);
			env.put(Context.PROVIDER_URL, DEFAULT_PROVIDER_URL);

			ctx = new InitialContext(env);

		} catch (Exception e) {
			log.throwing("ERROR: bind", "", e);
		}
	}

	public void unbind() {
	}

	public DataSource lookupDataSource(String dataSourceName) throws NamingException {
		return (DataSource) ctx.lookup(dataSourceName);
	}

	public Object lookup(String name) throws NamingException {
		return ctx.lookup(name);
	}

	public NamingEnumeration<Binding> list() throws NamingException {
		return list("");
	}

	public NamingEnumeration<Binding> list(String name) throws NamingException {
		return ctx.listBindings(name);
	}

	/**
	 * A Simple DataSource sample with JNDI. This is tested using File System
	 * based reference implementation of JNDI SPI driver from JavaSoft. You need
	 * to download fscontext1_2beta2.zip from JavaSoft site. Include
	 * providerutil.jar and fscontext.jar extracted from the above ZIP in the
	 * classpath. Create a directory /tmp/JNDI/jdbc
	 * 
	 * @param args
	 *            command line arguments
	 * @throws NamingException
	 */

	// You need to import the java.sql package to use JDBC
	public static void main(String args[]) throws NamingException {
		String instanceName = "";
		for (int i = 0; i < args.length; i++) {
			if ("-instance".equals(args[i])) {
				instanceName = args[++i];
			}
		}

		DatabaseRegistry instance = new DatabaseRegistry();
		instance.bind();
		instance.lookup(instanceName);
		NamingEnumeration<Binding> list = instance.list(instanceName);

		System.out.println("Bindings for " + instanceName);

		while (list.hasMore()) {
			Binding binding = list.next();
			System.out.println("Binding: " + binding.getName() + ", " + binding);
		}
	}
}
