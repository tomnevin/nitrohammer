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

package com.viper.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

        String[] column() default "";

        String[] importTable() default "";

        String[] foreignKey() default "";

        String filter() default "";

        String[] index() default "";

        String sqlSelect() default "";

        String sqlUpdate() default "";

        String sqlInsert() default "";

        String sqlDelete() default "";

        String queryClassName() default "";

        String[] row() default "";

        String name() default "";

        String tableName() default "";

        String javaName() default "";

        String databaseName() default "";

        String tableType() default "table";

        String baseClass() default "";

        String charsetName() default "";

        String collationName() default "";

        String dataDirectory() default "";

        String description() default "";

        boolean delayKeyWrite() default false;

        String engine() default "";

        String validator() default "";
        
        String generator() default "";

        String converter() default "";

        boolean hasChecksum() default false;

        String indexDirectory() default "";

        String interfce() default "";

        boolean isSchemaUpdatable() default false;

        boolean isRestService() default false;

        boolean isAbstract() default false;

        boolean isDefault() default false;

        boolean isFinal() default true;

        boolean isLargeTable() default false;

        boolean isMonitorChanges() default false;

        int maximumRows() default 0;

        int minimumRows() default 0;

        String packKeys() default "";

        String password() default "";

        String raidType() default "";

        String raidChunks() default "";

        int raidChunkSize() default 0;

        String rowFormat() default "";

        boolean skipSql() default false;

        String union() default "";

        int iterations() default 0;

}