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

package com.viper.database.tools;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.viper.database.dao.DatabaseSQLInterface;
import com.viper.database.dao.drivers.SQLDriver;
import com.viper.database.model.Database;
import com.viper.database.model.Databases;
import com.viper.database.model.ForeignKey;
import com.viper.database.model.Index;
import com.viper.database.model.Procedure;
import com.viper.database.model.Row;
import com.viper.database.model.Table;
import com.viper.database.model.TableType;
import com.viper.database.model.Trigger;
import com.viper.database.model.User;

public class SqlConverter {

	// -------------------------------------------------------------------------

	public final static void write(Writer writer, DatabaseSQLInterface dao, Databases databases,
			Map<String, Object> properties) throws Exception {

		PrintWriter out = new PrintWriter(writer);
		SQLDriver driver = new SQLDriver();

		for (Database database : databases.getDatabases()) {
			for (Table table : database.getTables()) {
				System.out.println("Writing: " + database.getName() + ", " + table.getName());
				List<Row> rows = dao.readRows(driver.readRows(database.getName(), table.getName()));
				out.println(driver.insertRows(database, table, rows, 0, rows.size()));
			}
		}
		out.flush();
	}

	public final static void write(Writer writer, String vendor, Databases databases) throws Exception {
		write(writer, vendor, databases, true);
	}

	public final static void write(Writer writer, String vendor, Databases databases, boolean alwaysDropItem)
			throws Exception {

		SQLDriver driver = new SQLDriver(vendor);

		List<String> items = new ArrayList<String>();

		List<String> databaseCreated = new ArrayList<String>();
		for (Database database : databases.getDatabases()) {
			if (!databaseCreated.contains(database.getName())) {
				items.add(driver.createDatabase(database));
				databaseCreated.add(database.getName());
			}
		}

		if (alwaysDropItem) {
			for (Database database : databases.getDatabases()) {
				for (Trigger trigger : database.getTriggers()) {
					items.add(driver.dropTrigger(database, trigger));
				}

				for (Table table : database.getTables()) {
					for (ForeignKey foreignKey : table.getForeignKeys()) {
						items.add(driver.dropForeignKey(database, table, foreignKey));
					}
				}
			}

			for (Database database : databases.getDatabases()) {
				for (Table table : database.getTables()) {
					for (Index index : table.getIndices()) {
						items.add(driver.dropIndex(database, table, index));
					}
					items.add(driver.dropTable(database, table));
				}
			}
		}

		for (Database database : databases.getDatabases()) {

			for (Table table : database.getTables()) {
				if (table.getTableType() == TableType.DATA) {
				} else if (table.getTableType() == TableType.VIEW) {
				} else if (table.getTableType() == TableType.BEAN) {
				} else if (table.getTableType() == TableType.VIEWAPP) {
				} else if (table.getTableType() != null) {
					items.add(driver.createTable(database, table));
					items.add(driver.truncateTable(database, table));
				}
			}
		}

		for (Database database : databases.getDatabases()) {

			for (Table table : database.getTables()) {
				if (table.getTableType() != null && table.getTableType() == TableType.VIEW) {
					items.add(driver.createTable(database, table));
				}
			}
		}

		for (User user : databases.getUsers()) {
			items.add(driver.createUser(user));
		}

		for (Database database : databases.getDatabases()) {
			for (Table table : database.getTables()) {
				if (table.getTableType() != TableType.DATA) {
					for (Index index : table.getIndices()) {
						items.add(driver.createIndex(database, table, index));
					}
				}
			}

			for (Procedure procedure : database.getProcedures()) {
				items.add(driver.dropProcedure(database, procedure));
				items.add(driver.createProcedure(database, procedure));
			}

			for (Table table : database.getTables()) {
				for (ForeignKey foreignKey : table.getForeignKeys()) {
					items.add(driver.createForeignKey(database, table, foreignKey));
				}
			}

			for (Trigger trigger : database.getTriggers()) {
				items.add(driver.createTrigger(database, trigger));
			}
		}

		for (Database database : databases.getDatabases()) {
			for (Table table : database.getTables()) {
				if (table.getRows().size() > 0) {
					items.add(driver.insertRows(database, table, table.getRows(), 0, table.getRows().size()));
				}
			}
		}

		for (String item : items) {
			System.err.println("SqlConverter: " + item);
			try {
				writer.write(item + ";\n");
				writer.flush();
			} catch (Exception ex) {
				System.err.println("SqlConverter: " + item + "," + ex.toString());
				ex.printStackTrace();
			}
		}
	}

	public final static void write(Writer writer, String vendor, Database database, Table table) throws Exception {

		List<String> items = new ArrayList<String>();

		SQLDriver driver = new SQLDriver();
		driver.setIgnore(true);

		items.add(driver.dropTable(database, table));

		if (table.getTableType() == TableType.DATA) {
		} else if (table.getTableType() == TableType.BEAN) {
		} else if (table.getTableType() != null) {
			items.add(driver.createTable(database, table));
			items.add(driver.truncateTable(database, table));

			for (Index index : table.getIndices()) {
				items.add(driver.createIndex(database, table, index));
			}
		}

		for (ForeignKey foreignKey : table.getForeignKeys()) {
			items.add(driver.createForeignKey(database, table, foreignKey));
		}

		if (table.getRows().size() > 0) {
			items.add(driver.insertRows(database, table, table.getRows(), 0, table.getRows().size()));
		}

		for (String item : items) {
			writer.write(item);
			writer.write("\n");
		}
	}
}
