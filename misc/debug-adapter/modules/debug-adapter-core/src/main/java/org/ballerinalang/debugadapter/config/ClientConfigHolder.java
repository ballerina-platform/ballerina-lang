/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.config;

import java.util.Map;

/**
 * Ballerina DAP Client Configuration holder.
 *
 * @since 2.0.0
 */
public class ClientConfigHolder {

    protected Map<String, Object> clientRequestArgs;
    private final ClientConfigKind kind;
    private String sourcePath;
    private Integer debuggePort;

    protected static final String ARG_FILE_PATH = "script";
    protected static final String ARG_DEBUGGEE_HOST = "debuggeeHost";
    protected static final String ARG_DEBUGGEE_PORT = "debuggeePort";

    protected ClientConfigHolder(Map<String, Object> clientRequestArgs, ClientConfigKind kind) {
        this.clientRequestArgs = clientRequestArgs;
        this.kind = kind;
    }

    public ClientConfigKind getKind() {
        return kind;
    }

    public String getSourcePath() throws ClientConfigurationException {
        if (sourcePath == null) {
            failIfConfigMissing(ARG_FILE_PATH);
            sourcePath = clientRequestArgs.get(ARG_FILE_PATH).toString();
        }
        return sourcePath;
    }

    public int getDebuggePort() throws ClientConfigurationException {
        if (debuggePort == null) {
            failIfConfigMissing(ARG_DEBUGGEE_PORT);
            debuggePort = Integer.parseInt(clientRequestArgs.get(ARG_DEBUGGEE_PORT).toString());
        }
        return debuggePort;
    }

    protected void failIfConfigMissing(String configName) throws ClientConfigurationException {
        if (clientRequestArgs.get(configName) == null) {
            throw new ClientConfigurationException("Required client configuration missing: '" + configName + "'");
        }
    }

    /**
     * Defines the possible types of client(user) configurations.
     */
    public enum ClientConfigKind {
        ATTACH_CONFIG,
        LAUNCH_CONFIG
    }
}
