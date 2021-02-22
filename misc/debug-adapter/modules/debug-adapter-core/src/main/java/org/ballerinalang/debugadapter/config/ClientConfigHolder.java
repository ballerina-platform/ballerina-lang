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

    protected Map<String, Object> args;
    private final ClientConfigKind kind;
    // Source directory path of the currently opened file in the client side.
    private static String sourcePath;
    private static Integer debuggePort;

    protected static final String ARG_FILE_PATH = "script";
    protected static final String ARG_BALLERINA_HOME = "ballerina.home";
    protected static final String ARG_DEBUGGEE_HOST = "debuggeePort";
    protected static final String ARG_DEBUGGEE_PORT = "debuggeePort";
    protected static final String ARG_BALLERINA_COMMAND = "ballerina.command";
    protected static final String ARG_TEST_DEBUG = "debugTests";
    protected static final String ARG_NETWORK_LOGS = "networkLogs";
    protected static final String ARG_NETWORK_LOGS_PORT = "networkLogsPort";
    protected static final String ARG_COMMAND_OPTIONS = "commandOptions";
    protected static final String ARG_SCRIPT_ARGUMENTS = "scriptArguments";
    protected static final String ARG_NO_DEBUG = "noDebug";
    protected static final String ARG_ENV = "env";

    protected ClientConfigHolder(Map<String, Object> args, ClientConfigKind kind) {
        this.args = args;
        this.kind = kind;
    }

    public ClientConfigKind getKind() {
        return kind;
    }

    public String getSourcePath() throws ClientConfigurationException {
        if (sourcePath == null) {
            failIfMissing(ARG_FILE_PATH);
            sourcePath = args.get(ARG_FILE_PATH).toString();
        }
        return sourcePath;
    }

    public int getDebuggePort() throws ClientConfigurationException {
        if (debuggePort == null) {
            failIfMissing(ARG_DEBUGGEE_PORT);
            debuggePort = Integer.getInteger(args.get(ARG_DEBUGGEE_PORT).toString());
        }
        return debuggePort;
    }

    protected void failIfMissing(String configName) throws ClientConfigurationException {
        if (args.get(configName) == null) {
            throw new ClientConfigurationException("Required client configuration missing: '" + configName + "'");
        }
    }
}

