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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Client configuration holder implementation for launch-mode.
 *
 * @since 2.0.0
 */
public class ClientLaunchConfigHolder extends ClientConfigHolder {

    private Boolean noDebug;
    private String ballerinaHome;
    private String ballerinaCommand;
    private Boolean testDebug;
    private Boolean networkLogsEnabled;
    private Integer networkLogsPort;
    private List<String> commandOptions;
    private List<String> programArguments;
    private Map<String, String> env;

    public ClientLaunchConfigHolder(Map<String, Object> args) {
        super(args, ClientConfigKind.LAUNCH_CONFIG);
    }

    public String getBallerinaHome() throws ClientConfigurationException {
        if (ballerinaHome == null) {
            failIfMissing(ARG_BALLERINA_HOME);
            ballerinaHome = args.get(ARG_BALLERINA_HOME).toString();
        }
        return ballerinaHome;
    }

    public Optional<String> getBallerinaCommand() throws ClientConfigurationException {
        if (ballerinaCommand == null) {
            if (!args.containsKey(ARG_BALLERINA_COMMAND)) {
                return Optional.empty();
            }
            ballerinaCommand = args.get(ARG_BALLERINA_COMMAND).toString();
        }
        return Optional.ofNullable(ballerinaCommand);
    }

    public boolean isNoDebug() {
        if (noDebug == null) {
            Object noDebugObject = args.get(ARG_NO_DEBUG);
            if (noDebugObject instanceof Boolean) {
                noDebug = (boolean) noDebugObject;
            } else if (noDebugObject instanceof String) {
                noDebug = Boolean.parseBoolean((String) noDebugObject);
            } else {
                noDebug = false;
            }
        }
        return noDebug;
    }

    public boolean isTestDebug() {
        if (testDebug == null) {
            Object testDebugObj = args.get(ARG_TEST_DEBUG);
            if (testDebugObj instanceof Boolean) {
                testDebug = (boolean) testDebugObj;
            } else if (testDebugObj instanceof String) {
                testDebug = Boolean.parseBoolean((String) testDebugObj);
            } else {
                testDebug = false;
            }
        }
        return testDebug;
    }

    public boolean networkLogsEnabled() {
        if (networkLogsEnabled == null) {
            Object networkLogs = args.get(ARG_NETWORK_LOGS);
            if (networkLogs instanceof Boolean) {
                networkLogsEnabled = (boolean) networkLogs;
            } else if (networkLogs instanceof String) {
                networkLogsEnabled = Boolean.parseBoolean((String) networkLogs);
            } else {
                networkLogsEnabled = false;
            }
        }
        return networkLogsEnabled;
    }

    public Optional<Integer> getNetworkLogsPort() throws ClientConfigurationException {
        if (networkLogsPort == null) {
            if (args.containsKey(ARG_NETWORK_LOGS_PORT)) {
                return Optional.empty();
            }
            networkLogsPort = Integer.getInteger(args.get(ARG_NETWORK_LOGS_PORT).toString());
        }
        return Optional.ofNullable(networkLogsPort);
    }

    public List<String> getCommandOptions() {
        if (commandOptions == null) {
            commandOptions = new ArrayList<>();
            if (args.get(ARG_COMMAND_OPTIONS) instanceof ArrayList) {
                commandOptions.addAll((ArrayList<String>) args.get(ARG_COMMAND_OPTIONS));
            }
        }
        return commandOptions;
    }

    public List<String> getProgramArguments() {
        if (programArguments == null) {
            programArguments = new ArrayList<>();
            if (args.get(ARG_SCRIPT_ARGUMENTS) instanceof ArrayList) {
                programArguments.addAll((ArrayList<String>) args.get(ARG_SCRIPT_ARGUMENTS));
            }
        }
        return programArguments;
    }

    public Optional<Map<String, String>> getEnv() {
        if (env == null) {
            Object envObject = args.get(ARG_ENV);
            if (envObject instanceof Map) {
                env = (Map<String, String>) envObject;
            }
        }
        return Optional.ofNullable(env);
    }
}
