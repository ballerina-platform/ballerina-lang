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

package org.ballerinalang.swagger.model;

import io.swagger.oas.models.Components;
import io.swagger.oas.models.ExternalDocumentation;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.info.Info;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.security.SecurityRequirement;
import io.swagger.oas.models.servers.Server;
import io.swagger.oas.models.tags.Tag;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for <code>io.swagger.oas.models.OpenAPI</code>
 * <p>This class can be used to push additional context variables for handlebars</p>
 */
public class BallerinaOpenApi implements BallerinaSwaggerObject<BallerinaOpenApi, OpenAPI> {
    private String apiPackage;
    private String modelPackage;
    private String openapi = "3.0.0";
    private Info info = null;
    private ExternalDocumentation externalDocs = null;
    private List<BallerinaServer> servers = null;
    private Set<Map.Entry<String, String>> security = null;
    private List<Tag> tags = null;
    private Set<Map.Entry<String, PathItem>> paths = null;
    private Set<Map.Entry<String, BallerinaSchema>> schemas = null;
    private Components components = null;
    private Map<String, Object> extensions = null;

    /**
     * Build a {@link BallerinaOpenApi} object from a {@link OpenAPI} object.
     * All non iterable objects using handlebars library is converted into
     * supported iterable object types.
     *
     * @param openAPI {@link OpenAPI} type object to be converted
     * @return Converted {@link BallerinaOpenApi} object
     * @throws BallerinaOpenApiException when OpenAPI to BallerinaOpenApi parsing failed
     */
    @Override
    public BallerinaOpenApi buildContext(OpenAPI openAPI) throws BallerinaOpenApiException {
        this.openapi = openAPI.getOpenapi();
        this.info = openAPI.getInfo();
        this.externalDocs = openAPI.getExternalDocs();
        this.tags = openAPI.getTags();
        this.components = openAPI.getComponents();
        this.extensions = openAPI.getExtensions();
        this.paths = openAPI.getPaths().entrySet();

        setSecurityRequirements(openAPI);
        setServers(openAPI);
        setSchemas(openAPI);
        return this;
    }

    @Override
    public BallerinaOpenApi getDefaultValue() {
        return null;
    }

    /**
     * Populate schemas into a "Set".
     *
     * @param openAPI <code>OpenAPI</code> definition object with schema definition
     */
    private void setSchemas(OpenAPI openAPI) {
        this.schemas = new LinkedHashSet<>();
        Map<String, Schema> schemaMap;
        if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) {
            return;
        }

        schemaMap = openAPI.getComponents().getSchemas();
        for (Map.Entry entry : schemaMap.entrySet()) {
            try {
                BallerinaSchema schema = new BallerinaSchema().buildContext((Schema) entry.getValue());
                schemas.add(new AbstractMap.SimpleEntry<>((String) entry.getKey(), schema));
            } catch (BallerinaOpenApiException e) {
                // Ignore exception and try to build next schema. No need to break the flow for a failure of one schema.
            }
        }
    }

    /**
     * Extract endpoint information from OpenAPI server list.
     * If no servers were found, default {@link BallerinaServer} will be set as the server
     *
     * @param openAPI <code>OpenAPI</code> definition object with server details
     * @throws BallerinaOpenApiException on failure to parse {@code Server} list
     */
    private void setServers(OpenAPI openAPI) throws BallerinaOpenApiException {
        this.servers = new ArrayList<>();
        List<Server> serverList = openAPI.getServers();
        if (serverList == null) {
            BallerinaServer server = new BallerinaServer().getDefaultValue();
            servers.add(server);
            return;
        }

        serverList.forEach(server -> {
            try {
                // Note that only one base path is allowed. Though we extract base path per each server
                // defined in the Open Api definition, only the base path of first server will be used
                // in ballerina code generation. Ballerina all endpoints to be in a single base path
                BallerinaServer balServer = new BallerinaServer().buildContext(server);
                servers.add(balServer);
            } catch (BallerinaOpenApiException e) {
                // Ignore the exception and move to other servers
            }
        });
    }

    /**
     * Extract security requirements as a set.
     *
     * @param openAPI <code>OpenAPI</code> definition object with security definition
     */
    private void setSecurityRequirements(OpenAPI openAPI) {
        this.security = new LinkedHashSet<>();
        List<SecurityRequirement> requirements = openAPI.getSecurity();
        if (requirements == null || requirements.isEmpty()) {
            return;
        }

        requirements.forEach(r -> r.forEach((key, value) -> {
            Map.Entry entry = new AbstractMap.SimpleEntry<>(key, value);
            security.add(entry);
        }));
    }

    public BallerinaOpenApi apiPackage(String apiPackage) {
        if (apiPackage != null) {
            this.apiPackage = apiPackage.replaceFirst("\\.", "/");
        }
        return this;
    }

    public BallerinaOpenApi modelPackage(String modelPackage) {
        if (modelPackage != null) {
            this.modelPackage = modelPackage.replaceFirst("\\.", "/");
        }
        return this;
    }

    public String getApiPackage() {
        return apiPackage;
    }

    public String getModelPackage() {
        return modelPackage;
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

    public List<BallerinaServer> getServers() {
        return servers;
    }

    public Set<Map.Entry<String, String>> getSecurity() {
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

    public Set<Map.Entry<String, BallerinaSchema>> getSchemas() {
        return schemas;
    }
}
