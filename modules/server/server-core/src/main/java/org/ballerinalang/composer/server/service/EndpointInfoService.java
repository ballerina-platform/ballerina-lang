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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Micro service which provides information about endpoints.
 */
@Path(ServerConstants.CONTEXT_ROOT + "/" + EndpointInfoService.PATH)
public class EndpointInfoService implements ComposerService {
    static final String NAME = "endpointinfo";
    static final String PATH = "endpoint";
    private final Map<String, ServiceInfo> serviceEPMap;
    private final ServerConfig serverConfig;

    public EndpointInfoService(ServerConfig serverConfig, List<ComposerService> serviceList) {
        this.serverConfig = serverConfig;
        this.serviceEPMap = new HashMap<>();
        for (ComposerService service: serviceList) {
            serviceEPMap.put(service.getServiceInfo().getName(), service.getServiceInfo());
        }
    }
    @GET
    @Path("/info")
    @Produces("application/json")
    public Response root(@QueryParam("name") String endpointName) {
        JsonObject entity = new JsonObject();
        entity.addProperty("endpoint", getEndpoint(endpointName));
        return Response.status(Response.Status.OK)
                .entity(entity)
                .header("Access-Control-Allow-Origin", '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return new ServiceInfo(NAME, PATH, ServiceType.HTTP);
    }

    private String getEndpoint(String endpointName) {
        ServiceInfo serviceInfo = serviceEPMap.get(endpointName);
        String protocol = serviceInfo.getType() == ServiceType.HTTP ? "http" : "ws";
        return protocol + "://localhost:" + serverConfig.getServerPort()
                + ServerConstants.CONTEXT_ROOT
                + "/" + serviceInfo.getContextPath();

    }
}
