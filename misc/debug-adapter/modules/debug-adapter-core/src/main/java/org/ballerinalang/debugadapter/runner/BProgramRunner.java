/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.runner;

import org.ballerinalang.debugadapter.config.ClientConfigurationException;
import org.ballerinalang.debugadapter.config.ClientLaunchConfigHolder;
import org.ballerinalang.debugadapter.utils.OSUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract implementation for Ballerina program runners.
 */
public abstract class BProgramRunner {

    protected final String projectRoot;
    protected final ClientLaunchConfigHolder configHolder;

    private static final String BAL_RUN_CMD = "run";
    private static final String BAL_TEST_CMD = "test";
    private static final String CMD_OPTION_DEBUG = "--debug";
    private static final String CMD_OPTION_TESTS = "--tests";
    protected static final String ENV_JAVA_OPTS = "JAVA_OPTS";
    protected static final String ENV_OPTION_BAL_JAVA_DEBUG = "BAL_JAVA_DEBUG";
    protected static final String ENV_DEBUGGER_TEST_MODE = "BAL_DEBUGGER_TEST";

    protected BProgramRunner(ClientLaunchConfigHolder configHolder, String projectRoot) {
        this.configHolder = configHolder;
        this.projectRoot = projectRoot;
    }

    /**
     * Responsible for executing the target ballerina program in a new process.
     *
     * @return started process
     */
    public abstract Process start() throws Exception;

    protected ArrayList<String> getBallerinaCommand(String balFile) throws ClientConfigurationException {

        List<String> ballerinaExec = new ArrayList<>();
        if (OSUtils.isWindows()) {
            ballerinaExec.add("cmd.exe");
            ballerinaExec.add("/c");
            ballerinaExec.add(configHolder.getBallerinaHome() + File.separator + "bin" + File.separator + "bal.bat");
        } else {
            ballerinaExec.add("bash");
            ballerinaExec.add(configHolder.getBallerinaHome() + File.separator + "bin" + File.separator + "bal");
        }

        // override ballerina exec if ballerina.command is provided.
        if (configHolder.getBallerinaCommand().isPresent()) {
            ballerinaExec = Collections.singletonList(configHolder.getBallerinaCommand().get());
        }

        ArrayList<String> command = new ArrayList<>(ballerinaExec);
        boolean isTestDebugMode = configHolder.isTestDebug();
        command.add(isTestDebugMode ? BAL_TEST_CMD : BAL_RUN_CMD);

        // Adds debug args.
        command.add(CMD_OPTION_DEBUG);
        command.add(Integer.toString(configHolder.getDebuggePort()));
        // Adds command options.
        command.addAll(configHolder.getCommandOptions());

        // Adds test arguments, which is required to run individual tests.
        if (isTestDebugMode && !configHolder.getTestArgs().isEmpty()) {
            command.add(CMD_OPTION_TESTS);
            command.add(String.join(",", configHolder.getTestArgs()));
        }

        // Adds file name, only if the debug source is a single bal file.
        if (balFile != null) {
            command.add(balFile);
        }

        // Adds command options to enable trace logs.
        boolean networkLogs = configHolder.isNetworkLogsEnabled();
        if (networkLogs && !isTestDebugMode && configHolder.getNetworkLogsPort().isPresent()) {
            command.add("--b7a.http.tracelog.host=localhost");
            command.add("--b7a.http.tracelog.port=" + configHolder.getNetworkLogsPort());
        }

        // Adds program arguments.
        if (!configHolder.getProgramArgs().isEmpty()) {
            command.add("--");
            command.addAll(configHolder.getProgramArgs());
        }
        return command;
    }
}
