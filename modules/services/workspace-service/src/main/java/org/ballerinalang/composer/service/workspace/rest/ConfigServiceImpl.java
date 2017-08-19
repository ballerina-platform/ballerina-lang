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
import org.ballerinalang.composer.service.workspace.Constants;
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

    private int langserverPort;

    private int debuggerPort;

    private String apiPath;
    private String launcherPath;
    private String langserverPath;
    private String debuggerPath;
    private String startupFile;

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
     * @param ex throwable error
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
     * @param request to get host name
     * @return config json object
     */
    public JsonObject getComposerConfig(Request request) {
        String host = getHostName(request);
        if (apiPath == null || "".equals(apiPath)) {
            apiPath = "http://" + host + ":" + apiPort;
        }
        if (launcherPath == null || "".equals(launcherPath)) {
            launcherPath = "ws://" + host + ":" + launcherPort;
        }
        if (debuggerPath == null || "".equals(debuggerPath)) {
            debuggerPath = "ws://" + host + ":" + debuggerPort;
        }
        if (langserverPath == null || "".equals(langserverPath)) {
            langserverPath = "ws://" + host + ":" + langserverPort;
        }

        JsonObject workspace = new JsonObject();
        workspace.addProperty("endpoint", apiPath + "/service/workspace");

        JsonObject packages = new JsonObject();
        packages.addProperty("endpoint", apiPath + "/service/packages");

        JsonObject swagger = new JsonObject();
        swagger.addProperty("endpoint", apiPath + "/service/swagger");

        JsonObject parser = new JsonObject();
        parser.addProperty("endpoint", apiPath + "/ballerina/model/content");

        JsonObject fragmentParser = new JsonObject();
        fragmentParser.addProperty("endpoint", apiPath + "/ballerina/model/parse-fragment");

        JsonObject validator = new JsonObject();
        validator.addProperty("endpoint", apiPath + "/ballerina/validate");

        JsonObject launcher = new JsonObject();
        launcher.addProperty("endpoint", launcherPath + "/launch");

        JsonObject debugger = new JsonObject();
        debugger.addProperty("endpoint", debuggerPath + "/debug");

        JsonObject langserver = new JsonObject();
        langserver.addProperty("endpoint", langserverPath + "/blangserver");

        JsonObject programNativeTypes = new JsonObject();
        programNativeTypes.addProperty("endpoint", apiPath + "/service/program/native/types");

        JsonObject programPackages = new JsonObject();
        programPackages.addProperty("endpoint", apiPath + "/service/program/packages");

        JsonObject typeLattice = new JsonObject();
        typeLattice.addProperty("endpoint", apiPath + "/typelattice");
        
        JsonObject services = new JsonObject();
        services.add("workspace", workspace);
        services.add("packages", packages);
        services.add("swagger", swagger);
        services.add("parser", parser);
        services.add("fragmentParser", fragmentParser);
        services.add("validator", validator);
        services.add("launcher", launcher);
        services.add("debugger", debugger);
        services.add("langserver", langserver);
        services.add("programNativeTypes", programNativeTypes);
        services.add("programPackages", programPackages);
        services.add("typeLattice", typeLattice);

        JsonObject config = new JsonObject();
        config.add("services", services);
    
        String balHome = System.getProperty(Constants.SYS_BAL_COMPOSER_HOME);
        if (balHome == null) {
            balHome = System.getenv(Constants.SYS_BAL_COMPOSER_HOME);
        }
        
        if (null != balHome) {
            config.addProperty("balHome", balHome);
        }

        if (getStartupFile() != null) {
            config.addProperty("startupFile", getStartupFile());
        }
                
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

    public int getLangserverPort() {
        return langserverPort;
    }

    public void setLangserverPort(int langserverPort) {
        this.langserverPort = langserverPort;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getLauncherPath() {
        return launcherPath;
    }

    public void setLauncherPath(String launcherPath) {
        this.launcherPath = launcherPath;
    }

    public String getLangserverPath() {
        return langserverPath;
    }

    public void setLangserverPath(String langserverPath) {
        this.langserverPath = langserverPath;
    }

    public void setDebuggerPort(int debuggerPort) {
        this.debuggerPort = debuggerPort;
    }

    public int getDebuggerPort() {
        return debuggerPort;
    }

    public void setDebuggerPath(String debuggerPath) {
        this.debuggerPath = debuggerPath;
    }

    public String getDebuggerPath() {
        return debuggerPath;
    }

    public String getStartupFile() {
        return startupFile;
    }

    public void setStartupFile(String startupFile) {
        this.startupFile = startupFile;
    }

    public String getHostName(Request request) {
        String hostHeader = request.getHeader("Host");
        String[] split = hostHeader.split(":");
        return split[0];
    }
}
