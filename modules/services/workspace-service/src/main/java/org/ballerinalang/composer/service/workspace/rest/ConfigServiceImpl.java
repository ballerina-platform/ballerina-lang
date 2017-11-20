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
import org.apache.commons.lang3.SystemUtils;
import org.ballerinalang.composer.service.workspace.Constants;
import org.ballerinalang.composer.service.workspace.PluginConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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
            log.error("Composer config api error.", throwable.getMessage(), throwable);
            return getErrorResponse(throwable);
        }
    }

    /**
     * Generates an error message for the given exception.
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
     * function to get the configuration for composer.
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
        parser.addProperty("endpoint", apiPath + "/ballerina/file/validate-and-parse");

        JsonObject fragmentParser = new JsonObject();
        fragmentParser.addProperty("endpoint", apiPath + "/ballerina/model/parse-fragment");

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
    
        JsonObject tryItService = new JsonObject();
        tryItService.addProperty("endpoint", apiPath + "/try-it");

        JsonObject imageUtil = new JsonObject();
        imageUtil.addProperty("endpoint", apiPath + "/file/connector/icon");

        JsonObject documentation = new JsonObject();
        documentation.addProperty("endpoint", apiPath + "/file/docs/api");
        
        JsonObject services = new JsonObject();
        services.add("workspace", workspace);
        services.add("packages", packages);
        services.add("swagger", swagger);
        services.add("parser", parser);
        services.add("fragmentParser", fragmentParser);
        services.add("launcher", launcher);
        services.add("debugger", debugger);
        services.add("langserver", langserver);
        services.add("programNativeTypes", programNativeTypes);
        services.add("programPackages", programPackages);
        services.add("typeLattice", typeLattice);
        services.add("tryItService", tryItService);
        services.add("imageutil", imageUtil);
        services.add("documentation", documentation);

        JsonObject config = new JsonObject();
        config.add("services", services);
    
        config.addProperty("pathSeparator", File.separator);
    
        // Configurations for plugins.
        JsonObject pluginConfigs = new JsonObject();
        config.add(PluginConstants.PLUGIN_CONFIGS, pluginConfigs);
        this.setWelcomeTabPluginConfigs(pluginConfigs);
        this.setWorkspaceManagerConfigs(pluginConfigs);
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
    
    /**
     * Setting configs related to welcome tab plugin.
     * @param pluginConfigs The config for all plugins..
     */
    public void setWelcomeTabPluginConfigs(JsonObject pluginConfigs) {
        JsonObject welcomeTabPluginConfig = new JsonObject();
        
        String balHome = System.getProperty(Constants.SYS_BAL_COMPOSER_HOME);
        if (balHome == null) {
            balHome = System.getenv(Constants.SYS_BAL_COMPOSER_HOME);
        }
        if (null != balHome) {
            // On windows, the path comes as a short path with some pieces are replaced
            // by ~ char. Using this path from client side causes issues with some derived
            // paths for further actions
            if (SystemUtils.IS_OS_WINDOWS) {
                try {
                    balHome = Paths.get(balHome).toRealPath().toString();
                } catch (IOException e) {
                    log.error("Error while resolving long path from short path", e);
                }
            }
            welcomeTabPluginConfig.addProperty("balHome", balHome);
        }
    
        pluginConfigs.add(PluginConstants.WELCOME_TAB_PLUGIN_ID, welcomeTabPluginConfig);
    }

    /**
     * Setting configs related to workspace manager plugin.
     * @param pluginConfigs The config for all plugins..
     */
    public void setWorkspaceManagerConfigs(JsonObject pluginConfigs) {
        JsonObject workspaceManagerPluginConfig = new JsonObject();
        if (getStartupFile() != null) {
            workspaceManagerPluginConfig.addProperty("startupFile", getStartupFile());
        }
        pluginConfigs.add(PluginConstants.WORKSPACE_MANAGER_PLUGIN_ID, workspaceManagerPluginConfig);
    }
}
