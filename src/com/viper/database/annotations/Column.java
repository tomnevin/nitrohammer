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
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Column {

        String[] enumValue() default "";

        String converter() default "";
        
        String renderer() default "";

        String validator() default "";

        String action() default "";

        String minimumValue() default "";

        String maximumValue() default "";

        String options() default "";

        String componentType() default "";

        String valuesClassname() default "";

        String validationMessage() default "";

        String tooltipMessage() default "";

        String name() default "";

        String tableName() default "";

        String databaseName() default "";

        String description() default "";

        String indexName() default "";

        String idMethod() default "none";

        String javaType() default "";

        String field() default "";

        String genericType() default "";

        String logicalType() default "";

        String dataType() default "";

        String extraDataType() default "";

        int decimalSize() default 0;

        String defaultValue() default "";

        String columnVisibilty() default "";

        long size() default 0;

        int order() default 1;

        boolean naturalKey() default false;

        boolean primaryKey() default false;

        boolean persistent() default true;

        boolean optional() default false;

        boolean required() default false;

        boolean secure() default false;

        boolean unique() default false;

        boolean unsigned() default false;

        boolean zeroFill() default false;

        boolean binary() default false;

        boolean ascii() default false;

        boolean unicode() default false;

        boolean isNullable() default false;



}
