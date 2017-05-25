/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.composer.service.workspace.app;

/**
 * Model class for workspace-service-config.yaml
 * Eg:
 * apiPath: http://localhost:8289
 * launcherPath: ws://localhost:8290
 * debuggerPath: ws://localhost:5006
 * rootDirectories: /Users/ballerina/Desktop
 * tryServiceURL: http://localhost:9090
 */
public class WorkspaceServiceConfig {

    // base url for the composer services
    private String apiPath;
    // base url for the launcher service
    private String launcherPath;
    // base url for the debugger websocket connection
    private String debuggerPath;
    // base url for the language server websocket connection
    private String langserverPath;
    // we can mount any given directory path as composer workspace root. This property is to provide that directory path
    private String rootDirectories;
    // URL to invoke the started services by composer
    private String tryServiceURL;

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

    public String getDebuggerPath() {
        return debuggerPath;
    }

    public void setDebuggerPath(String debuggerPath) {
        this.debuggerPath = debuggerPath;
    }

    public String getLangserverPath() {
        return debuggerPath;
    }

    public void setLangserverPath(String debuggerPath) {
        this.debuggerPath = debuggerPath;
    }

    public String getRootDirectories() {
        return rootDirectories;
    }

    public void setRootDirectories(String rootDirectories) {
        this.rootDirectories = rootDirectories;
    }

    public String getTryServiceURL() {
        return tryServiceURL;
    }

    public void setTryServiceURL(String tryServiceURL) {
        this.tryServiceURL = tryServiceURL;
    }


}
