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
package org.ballerinalang.composer.server.core;

import java.util.Map;

/**
 * Configuration for composer backend server.
 */
public class ServerConfig {

    private int port;

    private String host;

    private String ballerinaHome;

    private String publicPath;

    private String debuggerPath;

    private Map<String, Map<String, String>> customConfigs;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBallerinaHome() {
        return ballerinaHome;
    }

    public void setBallerinaHome(String ballerinaHome) {
        this.ballerinaHome = ballerinaHome;
    }

    public String getPublicPath() {
        return publicPath;
    }

    public void setPublicPath(String publicPath) {
        this.publicPath = publicPath;
    }

    public String getDebuggerPath() {
        return debuggerPath;
    }

    public void setDebuggerPath(String debuggerPath) {
        this.debuggerPath = debuggerPath;
    }

    public Map<String, Map<String, String>> getCustomConfigs() {
        return customConfigs;
    }

    public void setCustomConfigs(Map<String, Map<String, String>> customConfigs) {
        this.customConfigs = customConfigs;
    }
}
