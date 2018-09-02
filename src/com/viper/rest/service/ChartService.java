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

package com.viper.rest.service;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.viper.rest.ChartsUtil;
import com.viper.rest.LocaleUtil;

@Path("charts")
public class ChartService {
	
    private static final Logger log = Logger.getLogger(ChartService.class.getName());
	
    @Context 
    private HttpServletRequest httpServletRequest;

    public String getDatabaseName() throws Exception {
        return com.viper.database.utils.ResourceUtil.getResource("DATABASE_LOCATOR", "test");
    }

	@GET
	@Path("dashboard")
	@Produces("application/json")
	public Response dashboard(@QueryParam("charts") String charts) throws Exception {

		String json = ChartsUtil.getDashboard(LocaleUtil.getCurrentLocale(httpServletRequest), getDatabaseName(), charts);

		return Response.ok(json, MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("directory")
	@Produces("application/json")
	public Response directory(@QueryParam("charts") String charts) throws Exception {

		String json = ChartsUtil.getDirectory(LocaleUtil.getCurrentLocale(httpServletRequest), getDatabaseName(), charts);

		return Response.ok(json, MediaType.APPLICATION_JSON).build();
	}
}