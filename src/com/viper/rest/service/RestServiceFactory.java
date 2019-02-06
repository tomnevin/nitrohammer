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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class RestServiceFactory {

    private static final Map<String, RestServiceInterface> cache = new HashMap<String, RestServiceInterface>();
    static {
        cache.put("ag", new AgRestService());
        cache.put("plotly", new PlotlyRestService());
        cache.put("form", new FormRestService());
        cache.put("rest", new RestService());
    }

    public static final RestServiceInterface get(String serviceType) throws Exception {

        if (serviceType == null) {
            serviceType = "rest";
        }
        return cache.get(serviceType);
    }

    public static final RestServiceInterface find(HttpServletRequest httpServletRequest) throws Exception {
        
        if (httpServletRequest == null) {
            return cache.get( "rest");
        }

        String serviceType = "rest"; 
        String acceptType = httpServletRequest.getHeader("Accept");
        
        if (acceptType == null || acceptType.trim().length() == 0) {
            serviceType = "rest";
        } else if (acceptType.contains("subtype=ag")) {
            serviceType = "ag";
        } else if (acceptType.contains("subtype=plotly")) {
            serviceType = "plotly";
        } else if (acceptType.contains("subtype=form")) {
            serviceType = "form";
        } else if (acceptType.contains("subtype=rest")) {
            serviceType = "rest";
        }
        return cache.get(serviceType); 
    }
}
