/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.swagger.utils;

import io.swagger.oas.models.Components;
import io.swagger.oas.models.ExternalDocumentation;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.info.Info;
import io.swagger.oas.models.security.SecurityRequirement;
import io.swagger.oas.models.servers.Server;
import io.swagger.oas.models.tags.Tag;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for <code>io.swagger.oas.models.OpenAPI</code>
 * <p>This class can be used to push additional context variables for handlebars</p>
 */
public class OpenApiWrapper {

    private String apiPackage;
    private String openapi = "3.0.0";
    private Info info = null;
    private ExternalDocumentation externalDocs = null;
    private List<Server> servers = null;
    private List<SecurityRequirement> security = null;
    private List<Tag> tags = null;
    private Set<Map.Entry<String, PathItem>> paths = null;
    private Components components = null;
    private Map<String, Object> extensions = null;
    private String host = null;
    private int port = 0;
    private int httpsPort = 0;
    private String basePath = null;

    public OpenApiWrapper buildFromOpenAPI(OpenAPI openAPI) throws MalformedURLException {
        this.openapi = openAPI.getOpenapi();
        this.info = openAPI.getInfo();
        this.externalDocs = openAPI.getExternalDocs();
        this.servers = openAPI.getServers();
        this.security = openAPI.getSecurity();
        this.tags = openAPI.getTags();
        this.components = openAPI.getComponents();
        this.extensions = openAPI.getExtensions();
        this.paths = openAPI.getPaths().entrySet();

        // Swagger parser returns a server object with "/" url when no servers are defined
        // this check is to overcome possible errors due to that
        if (servers.size() > 1 || !"/".equals(servers.get(0).getUrl())) {

            // We select the first server in the list as the Host of generated service
            // Other servers will be kept as extra information but will not be used within the service
            URL url = new URL(servers.get(0).getUrl());
            host = url.getHost();
            basePath = url.getPath();
            boolean isHttps = "https".equalsIgnoreCase(url.getProtocol());

            if (isHttps) {
                httpsPort = url.getPort();
                httpsPort = httpsPort == -1 ? 443 : httpsPort;
            } else {
                port = url.getPort();
                port = port == -1 ? 80 : port;
            }
        }

        return this;
    }

    public String getApiPackage() {
        return apiPackage;
    }

    public OpenApiWrapper apiPackage(String apiPackage) {
        this.apiPackage = apiPackage;
        return this;
    }

    public String getOpenapi() {
        return openapi;
    }

    public Info getInfo() {
        return info;
    }

    public ExternalDocumentation getExternalDocs() {
        return externalDocs;
    }

    public List<Server> getServers() {
        return servers;
    }

    public List<SecurityRequirement> getSecurity() {
        return security;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Set<Map.Entry<String, PathItem>> getPaths() {
        return paths;
    }

    public Components getComponents() {
        return components;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getBasePath() {
        return basePath;
    }

    public int getHttpsPort() {
        return httpsPort;
    }
}
