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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import com.viper.database.model.DatabaseConnection;
import com.viper.database.security.Encryptor;

public class ConnectionFactory {
	
	private final static Logger log = Logger.getLogger(ConnectionFactory.class.getName());

	private final static Map<String, PoolingDataSource> dataSources = new HashMap<String, PoolingDataSource>();

	public final static synchronized DataSource getDataSource(DatabaseConnection dbc) throws Exception {

		if (dataSources.containsKey(dbc.getName())) {
			return dataSources.get(dbc.getName());
		}

		Class.forName(dbc.getDriver());

		DriverManagerConnectionFactory cf = null;

		log.fine("ConnectionFactory: database url: " + dbc.getDatabaseUrl());
		if (dbc.getUsername() != null && dbc.getPassword() != null) {
			Encryptor encryptor = new Encryptor();
			String password = encryptor.decryptPassword(dbc.getPassword());

			cf = new DriverManagerConnectionFactory(dbc.getDatabaseUrl(), dbc.getUsername(), password);
		} else {
			cf = new DriverManagerConnectionFactory(dbc.getDatabaseUrl());
		}

		PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, null);
		pcf.setValidationQuery(getValue(dbc.getPoolValidationQuery(), "select 1"));

		// create a generic pool
		GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(pcf);
		pool.setMaxTotal(getValue(dbc.getPoolMaxTotal(), 50));
		pool.setMaxIdle(getValue(dbc.getPoolMaxIdle(), 10));
		pool.setMinIdle(getValue(dbc.getPoolMinIdle(), 0));
		pool.setMaxWaitMillis(getValue(dbc.getPoolMaxWaitMillis(), -1));
		pool.setTimeBetweenEvictionRunsMillis(getValue(dbc.getPoolTimeBetweenEvictionRunsMillis(), 30000));
		pool.setMinEvictableIdleTimeMillis(getValue(dbc.getPoolMinEvictableIdleTimeMillis(), 60000));
		pool.setTestWhileIdle(dbc.isPoolTestWhileIdle());
		pool.setTestOnBorrow(dbc.isPoolTestOnBorrow());

		AbandonedConfig abandonedConfig = new AbandonedConfig();
		abandonedConfig.setRemoveAbandonedOnMaintenance(dbc.isPoolRemoveAbandoned());
		abandonedConfig.setRemoveAbandonedTimeout(getValue(dbc.getPoolRemoveAbandonedTimeoutSeconds(), 300));
		abandonedConfig.setLogAbandoned(dbc.isPoolLogAbandoned());

		pool.setAbandonedConfig(abandonedConfig);

		pcf.setPool(pool);

		PoolingDataSource ds = new PoolingDataSource(pool);

		dataSources.put(dbc.getName(), ds);

		return ds;
	}

	public final static synchronized void releaseAll() throws Exception {
		for (String key : dataSources.keySet()) {
			PoolingDataSource ds = dataSources.get(key);
			ds.close();
		}

		dataSources.clear();
	}

	private final static int getValue(int value, int defaultValue) {
		return (value == 0) ? defaultValue : value;
	}

	private final static String getValue(String value, String defaultValue) {
		return (value == null) ? defaultValue : value;
	}
}