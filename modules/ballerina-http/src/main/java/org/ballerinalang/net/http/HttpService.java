/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.uri.URITemplate;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.uri.parser.Literal;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.List;

/**
 * {@code HttpService} This is the http wrapper for the {@code Service} implementation.
 *
 * @since 0.94
 */
public class HttpService {
    private Service balService;
    private List<HttpResource> resources;
    private List<String> allAllowMethods;
    private String basePath;
    private CorsHeaders corsHeaders;
    private URITemplate<HttpResourceElement, HttpResource, HTTPCarbonMessage> uriTemplate;

    public HttpService(Service service) {
        this.balService = service;
    }

    public String getName() {
        return balService.getName();
    }

    public String getPackage() {
        return balService.getPackage();
    }

    public Service getBalService() {
        return balService;
    }

    public List<HttpResource> getResources() {
        return resources;
    }

    public void setResources(List<HttpResource> resources) {
        this.resources = resources;
    }

    public List<String> getAllAllowMethods() {
        return allAllowMethods;
    }

    public void setAllAllowMethods(List<String> allAllowMethods) {
        this.allAllowMethods = allAllowMethods;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public CorsHeaders getCorsHeaders() {
        return corsHeaders;
    }

    public void setCorsHeaders(CorsHeaders corsHeaders) {
        this.corsHeaders = corsHeaders;
    }

    public URITemplate<HttpResourceElement, HttpResource, HTTPCarbonMessage> getUriTemplate()
            throws URITemplateException {
        if (uriTemplate == null) {
            HttpResourceElementCreator elementCreator = new HttpResourceElementCreator();
            uriTemplate = new URITemplate<>(new Literal<>(elementCreator.createDataElement(), "/"), elementCreator);
        }
        return uriTemplate;
    }
}
