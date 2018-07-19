/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.composer.server.service;

import com.google.gson.JsonObject;
import org.ballerinalang.composer.server.core.ServerConfig;
import org.ballerinalang.composer.server.core.ServerConstants;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ServiceInfo;
import org.ballerinalang.composer.server.spi.ServiceType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Micro service which exposes configs to via http.
 */
@Path(ServerConstants.CONTEXT_ROOT + "/" + ConfigService.PATH)
public class ConfigService implements ComposerService {
    static final String NAME = "config";
    static final String PATH = "config";
    private static final String COMPOSER_SAMPLES_DIR = "composer.samples.dir";
    private final Map<String, ServiceInfo> serviceEPMap;
    private final ServerConfig serverConfig;

    public ConfigService(ServerConfig serverConfig, List<ComposerService> serviceList) {
        this.serverConfig = serverConfig;
        this.serviceEPMap = new HashMap<>();
        for (ComposerService service: serviceList) {
            serviceEPMap.put(service.getServiceInfo().getName(), service.getServiceInfo());
        }
    }

    @GET
    @Path("/endpoint/info")
    @Produces("application/json")
    public Response endpoint(@QueryParam("name") String endpointName) {
        JsonObject entity = new JsonObject();
        entity.addProperty("endpoint", getEndpoint(endpointName));
        return Response.status(Response.Status.OK)
                .entity(entity)
                .header("Access-Control-Allow-Origin", '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("/endpoints")
    @Produces("application/json")
    public Response endpoints() {
        return Response.status(Response.Status.OK)
                .entity(getEndpoints())
                .header("Access-Control-Allow-Origin", '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Produces("application/json")
    public Response config() {
        return Response.status(Response.Status.OK)
                .entity(getConfig())
                .header("Access-Control-Allow-Origin", '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return new ServiceInfo(NAME, PATH, ServiceType.HTTP);
    }

    private JsonObject getConfig() {
        JsonObject config = new JsonObject();
        config.add("services", getEndpoints());
        config.addProperty("pathSeparator", File.separator);
        // TODO Provide a config contributing mechanism so that the other
        // modules are to contribute returned config to front-end
        // DO NOT put all configs(related to other modules) here
        // move below configs to a proper module
        config.addProperty("balHome", serverConfig.getBallerinaHome());
        config.addProperty("debuggerPath", serverConfig.getDebuggerPath());
        String samplesDir = System.getProperty(COMPOSER_SAMPLES_DIR);
        if (samplesDir == null) {
            samplesDir = serverConfig.getBallerinaHome() + File.separator + "docs" + File.separator + "examples";
        }
        config.addProperty("samplesDir", samplesDir);
        return config;
    }

    private JsonObject getEndpoints() {
        JsonObject endpoints = new JsonObject();
        Set<Map.Entry<String, ServiceInfo>> entries = serviceEPMap.entrySet();
        for (Map.Entry<String, ServiceInfo> entry: entries) {
            JsonObject endpoint = new JsonObject();
            endpoint.addProperty("endpoint", getEndpoint(entry.getKey()));
            endpoints.add(entry.getKey(), endpoint);
        }
        return endpoints;
    }

    private String getEndpoint(String endpointName) {
        if (!serviceEPMap.containsKey(endpointName)) {
            return null;
        }
        ServiceInfo serviceInfo = serviceEPMap.get(endpointName);
        String protocol = serviceInfo.getType() == ServiceType.HTTP ? "http" : "ws";
        return protocol + "://" + serverConfig.getHost() + ":" + serverConfig.getPort()
                + ServerConstants.CONTEXT_ROOT
                + "/" + serviceInfo.getPath();

    }
}
