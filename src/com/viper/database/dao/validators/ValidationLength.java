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

package com.viper.database.dao.validators;

import java.util.ArrayList;
import java.util.List;

import com.viper.database.annotations.Column;
import com.viper.database.dao.DatabaseUtil;
import com.viper.database.interfaces.ColumnValidatorInterface;
import com.viper.database.model.Param;

public class ValidationLength implements ColumnValidatorInterface {

    @Override
    public final <T> boolean isValid(T bean, Column column) {
        return validateErrors(bean, column).size() == 0;
    }

    @Override
    public final <T> List<Param> validateErrors(T bean, Column column) {
        List<Param> errors = new ArrayList<Param>();

        if (column.javaType().equals("String")) {
            String value = DatabaseUtil.getString(bean, column.field());
            if (value.length() > column.size()) {
                errors.add(newParam(column.name(),
                        "String field too long S/B less then " + column.size() + " was " + value.length()));
            }
        }
        return errors;
    }

    private final Param newParam(String name, String value) {
        Param param = new Param();
        param.setName(name);
        param.setValue(value);
        return param;
    }
}
