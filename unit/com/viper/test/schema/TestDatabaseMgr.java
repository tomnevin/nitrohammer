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

import org.junit.Test;

import com.viper.database.dao.DatabaseMapper;
import com.viper.database.managers.DatabaseMgr;
import com.viper.database.model.Databases;
import com.viper.database.utils.junit.AbstractTestCase;

public class TestDatabaseMgr extends AbstractTestCase {

    @Test
    public void testDatabaseMgrFinds() throws Exception {

        Databases databases = DatabaseMapper.read(Databases.class, "res:/com/viper/test/schema/JDBCSample001.xml");

        assertNotNull("Could not find column (ID) ", DatabaseMgr.findColumn(databases, "test", "basic_table", "ID"));
        assertNotNull("Could not find foreign key (local-key-name) ",
                DatabaseMgr.findForeignKey(databases, "test", "basic_table", "local-key-name"));
        assertNotNull("Could not find foreign key (NAME) ",
                DatabaseMgr.findForeignKeyByLocalColumnName(databases, "test", "basic_table", "name"));
        assertNotNull("Could not find index (indexname) ", DatabaseMgr.findIndex(databases, "test", "basic_table", "indexname"));
        assertNotNull("Could not find database (test) ", DatabaseMgr.findTable(databases, "test", "basic_table"));
    }
}
