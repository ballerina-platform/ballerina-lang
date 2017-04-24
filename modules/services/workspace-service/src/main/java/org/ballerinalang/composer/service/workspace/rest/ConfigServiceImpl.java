/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.rest;


import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Micro-service for exposing Ballerina Composer Configs.
 *
 * @since 0.1-SNAPSHOT
 */
@Path("/config")
public class ConfigServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(ConfigServiceImpl.class);

    private int apiPort;

    private int launcherPort;

    @GET
    @Produces("application/json")
    public Response handleGet(@Context Request request) {
        try {
            return Response.status(Response.Status.OK)
                    .entity(getComposerConfig(request))
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Throwable throwable) {
            log.error("Composer config api error.", throwable.getMessage());
            return getErrorResponse(throwable);
        }
    }

    /**
     * Generates an error message for the given exception
     * @param ex
     * @return Response
     */
    private Response getErrorResponse(Throwable ex) {
        JsonObject entity = new JsonObject();
        String errMsg = ex.getMessage();
        entity.addProperty("Error", errMsg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(entity)
                .header("Access-Control-Allow-Origin", '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * function to get the configuration for composer
     *
     * @param request
     * @return
     */
    public JsonObject getComposerConfig(Request request) {
        String apiPath = "http://" + request.getProperties().get("LOCAL_NAME") + ":" + apiPort;
        String launcherPath = "ws://" + request.getProperties().get("LOCAL_NAME") + ":" + launcherPort;

        JsonObject workspace = new JsonObject();
        workspace.addProperty("endpoint", apiPath + "/service/workspace");

        JsonObject packages = new JsonObject();
        packages.addProperty("endpoint", apiPath + "/ballerina/editor/packages");

        JsonObject swagger = new JsonObject();
        swagger.addProperty("endpoint", apiPath + "/service/swagger/");

        JsonObject parser = new JsonObject();
        parser.addProperty("endpoint", apiPath + "/ballerina/model/content");

        JsonObject validator = new JsonObject();
        validator.addProperty("endpoint", apiPath + "/ballerina/validate");

        JsonObject launcher = new JsonObject();
        launcher.addProperty("endpoint", launcherPath + "/launch");

        JsonObject services = new JsonObject();
        services.add("workspace", workspace);
        services.add("packages", packages);
        services.add("swagger", swagger);
        services.add("parser", parser);
        services.add("validator", validator);
        services.add("launcher", launcher);

        JsonObject config = new JsonObject();
        config.add("services", services);

        return config;
    }

    public int getApiPort() {
        return apiPort;
    }

    public void setApiPort(int apiPort) {
        this.apiPort = apiPort;
    }

    public int getLauncherPort() {
        return launcherPort;
    }

    public void setLauncherPort(int launcherPort) {
        this.launcherPort = launcherPort;
    }
}
