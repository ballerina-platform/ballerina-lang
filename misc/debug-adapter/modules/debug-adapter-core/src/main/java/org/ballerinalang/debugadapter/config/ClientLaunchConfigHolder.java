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
    private List<String> programArgs;
    private List<String> testArgs;
    private Map<String, String> env;

    private static final String ARG_BALLERINA_HOME = "ballerina.home";
    private static final String ARG_BALLERINA_COMMAND = "ballerina.command";
    private static final String ARG_TEST_DEBUG = "debugTests";
    private static final String ARG_NETWORK_LOGS = "networkLogs";
    private static final String ARG_NETWORK_LOGS_PORT = "networkLogsPort";
    private static final String ARG_COMMAND_OPTIONS = "commandOptions";
    private static final String ARG_SCRIPT_ARGUMENTS = "scriptArguments";
    private static final String ARG_TEST_ARGUMENTS = "tests";
    private static final String ARG_NO_DEBUG = "noDebug";
    private static final String ARG_ENV = "env";

    public ClientLaunchConfigHolder(Map<String, Object> args) {
        super(args, ClientConfigKind.LAUNCH_CONFIG);
    }

    public String getBallerinaHome() throws ClientConfigurationException {
        if (ballerinaHome == null) {
            failIfConfigMissing(ARG_BALLERINA_HOME);
            ballerinaHome = clientRequestArgs.get(ARG_BALLERINA_HOME).toString();
        }
        return ballerinaHome;
    }

    public Optional<String> getBallerinaCommand() {
        if (ballerinaCommand == null) {
            if (!clientRequestArgs.containsKey(ARG_BALLERINA_COMMAND)) {
                return Optional.empty();
            }
            ballerinaCommand = clientRequestArgs.get(ARG_BALLERINA_COMMAND).toString();
        }
        return Optional.ofNullable(ballerinaCommand);
    }

    public boolean isNoDebugMode() {
        if (noDebug == null) {
            Object noDebugObject = clientRequestArgs.get(ARG_NO_DEBUG);
            if (noDebugObject instanceof Boolean b) {
                noDebug = b;
            } else if (noDebugObject instanceof String s) {
                noDebug = Boolean.parseBoolean(s);
            } else {
                noDebug = false;
            }
        }
        return noDebug;
    }

    public boolean isTestDebug() {
        if (testDebug == null) {
            Object testDebugObj = clientRequestArgs.get(ARG_TEST_DEBUG);
            if (testDebugObj instanceof Boolean b) {
                testDebug = b;
            } else if (testDebugObj instanceof String s) {
                testDebug = Boolean.parseBoolean(s);
            } else {
                testDebug = false;
            }
        }
        return testDebug;
    }

    public boolean isNetworkLogsEnabled() {
        if (networkLogsEnabled == null) {
            Object networkLogs = clientRequestArgs.get(ARG_NETWORK_LOGS);
            if (networkLogs instanceof Boolean b) {
                networkLogsEnabled = b;
            } else if (networkLogs instanceof String s) {
                networkLogsEnabled = Boolean.parseBoolean(s);
            } else {
                networkLogsEnabled = false;
            }
        }
        return networkLogsEnabled;
    }

    public Optional<Integer> getNetworkLogsPort() throws ClientConfigurationException {
        if (networkLogsPort == null) {
            if (clientRequestArgs.containsKey(ARG_NETWORK_LOGS_PORT)) {
                return Optional.empty();
            }
            networkLogsPort = Integer.getInteger(clientRequestArgs.get(ARG_NETWORK_LOGS_PORT).toString());
        }
        return Optional.ofNullable(networkLogsPort);
    }

    public List<String> getCommandOptions() {
        if (commandOptions == null) {
            commandOptions = new ArrayList<>();
            if (clientRequestArgs.get(ARG_COMMAND_OPTIONS) instanceof ArrayList) {
                commandOptions.addAll((ArrayList<String>) clientRequestArgs.get(ARG_COMMAND_OPTIONS));
            }
        }
        return commandOptions;
    }

    public List<String> getProgramArgs() {
        if (programArgs == null) {
            programArgs = new ArrayList<>();
            if (clientRequestArgs.get(ARG_SCRIPT_ARGUMENTS) instanceof ArrayList) {
                programArgs.addAll((ArrayList<String>) clientRequestArgs.get(ARG_SCRIPT_ARGUMENTS));
            }
        }
        return programArgs;
    }

    public List<String> getTestArgs() {
        if (testArgs == null) {
            testArgs = new ArrayList<>();
            if (clientRequestArgs.get(ARG_TEST_ARGUMENTS) instanceof ArrayList) {
                testArgs.addAll((ArrayList<String>) clientRequestArgs.get(ARG_TEST_ARGUMENTS));
            }
        }
        return testArgs;
    }

    public Optional<Map<String, String>> getEnv() {
        if (env == null) {
            Object envObject = clientRequestArgs.get(ARG_ENV);
            if (envObject instanceof Map) {
                env = (Map<String, String>) envObject;
            }
        }
        return Optional.ofNullable(env);
    }
}
