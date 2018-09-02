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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.viper.database.model.DatabaseConnection;
import com.viper.database.security.Encryptor;

public class DatabaseTunnel {

	private static Session session = null;

	public void doSshTunnel(DatabaseConnection dbc) throws Exception {

		if (session != null && session.isConnected()) {
			return;
		}

		Encryptor encryptor = new Encryptor();

		final JSch jsch = new JSch();
		int sshPort = dbc.getSsh().getSshPort();
		int remotePort = dbc.getSsh().getRemotePort();
		int localPort = dbc.getSsh().getLocalPort();
		String username = dbc.getSsh().getSshUsername();
		String password = encryptor.decryptPassword(dbc.getSsh().getSshPassword());
		String remoteHost = dbc.getSsh().getRemoteHost();
		String host = dbc.getSsh().getSshHost();

		session = jsch.getSession(username, host, sshPort);
		session.setPassword(password);

		final Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.connect();
		session.setPortForwardingL(localPort, remoteHost, remotePort);
	}

	public void doSshTunnel(String strSshUser, String strSshPassword, String strSshHost, int nSshPort, String strRemoteHost, int nLocalPort,
	        int nRemotePort) throws JSchException {

		final JSch jsch = new JSch();
		Session session = jsch.getSession(strSshUser, strSshHost, nSshPort);
		session.setPassword(strSshPassword);

		final Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);

		session.connect();
		session.setPortForwardingL(nLocalPort, strRemoteHost, nRemotePort);
	}

	public static void main(String[] args) {
		try {

			Encryptor encryptor = new Encryptor();

			// SSH loging username
			String strSshUser = "ssh_user_name";
			// SSH login password
			String strSshPassword = encryptor.decryptPassword("abcd1234");
			// hostname or ip or SSH server
			String strSshHost = "your.ssh.hostname.com";
			// remote SSH host port number
			int nSshPort = 22;
			// hostname or ip of your database server
			String strRemoteHost = "your.database.hostname.com";
			// local port number use to bind SSH tunnel
			int nLocalPort = 3366;
			// remote port number of your database
			int nRemotePort = 3306;
			// database loging username
			String strDbUser = "db_user_name";
			// database login password
			String strDbPassword = encryptor.decryptPassword("4321dcba");

			new DatabaseTunnel().doSshTunnel(strSshUser, strSshPassword, strSshHost, nSshPort, strRemoteHost, nLocalPort, nRemotePort);

			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:" + nLocalPort, strDbUser, strDbPassword);
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}
