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

package org.ballerinalang.debugadapter.launch;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import org.ballerinalang.debugadapter.DebugExecutionManager;
import org.ballerinalang.debugadapter.JBallerinaDebugServer;
import org.ballerinalang.debugadapter.terminator.OSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of Ballerina program launcher.
 */
public abstract class Launcher {

    protected final String projectRoot;
    protected final Map<String, Object> args;
    private final String debuggeePort;
    private final String ballerinaHome;

    private static final String ARG_BALLERINA_HOME = "ballerina.home";
    private static final String ARG_DEBUGGEE_PORT = "debuggeePort";
    private static final String ARG_BALLERINA_COMMAND = "ballerina.command";
    private static final String ARG_TEST_DEBUG = "debugTests";
    private static final String ARG_NETWORK_LOGS = "networkLogs";
    private static final String ARG_NETWORK_LOGS_PORT = "networkLogsPort";
    private static final String ARG_COMMAND_OPTIONS = "commandOptions";
    private static final String ARG_SCRIPT_ARGUMENTS = "scriptArguments";
    private static final String BAL_RUN_CMD_NAME = "run";
    private static final String BAL_TEST_CMD_NAME = "test";

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public abstract Process start() throws IOException;

    Launcher(String projectRoot, Map<String, Object> args) throws IllegalArgumentException {
        this.projectRoot = projectRoot;
        this.args = args;

        if (args.get(ARG_DEBUGGEE_PORT) == null) {
            throw new IllegalArgumentException("Required parameter missing:" + ARG_DEBUGGEE_PORT);
        }
        debuggeePort = args.get(ARG_DEBUGGEE_PORT).toString();

        if (args.get(ARG_BALLERINA_HOME) == null) {
            throw new IllegalArgumentException("Required parameter missing:" + ARG_BALLERINA_HOME);
        }
        ballerinaHome = args.get(ARG_BALLERINA_HOME).toString();
    }

    ArrayList<String> getLauncherCommand(String balFile) {

        List<String> ballerinaExec = new ArrayList<>();
        if (OSUtils.isWindows()) {
            ballerinaExec.add("cmd.exe");
            ballerinaExec.add("/c");
            ballerinaExec.add(ballerinaHome + File.separator + "bin" + File.separator + "ballerina.bat");
        } else {
            ballerinaExec.add("bash");
            ballerinaExec.add(ballerinaHome + File.separator + "bin" + File.separator + "ballerina");
        }

        String ballerinaCmd = args.get(ARG_BALLERINA_COMMAND) == null ? "" : args.get(ARG_BALLERINA_COMMAND).toString();
        // override ballerina exec if ballerina.command is provided.
        if (!ballerinaCmd.isEmpty()) {
            ballerinaExec = Collections.singletonList(ballerinaCmd);
        }

        boolean testDebugging = false;
        if (args.get(ARG_TEST_DEBUG) instanceof Boolean) {
            testDebugging = (boolean) args.get(ARG_TEST_DEBUG);
        } else if (args.get(ARG_TEST_DEBUG) instanceof String) {
            testDebugging = Boolean.parseBoolean((String) args.get(ARG_TEST_DEBUG));
        }

        ArrayList<String> command = new ArrayList<>(ballerinaExec);
        command.add(testDebugging ? BAL_TEST_CMD_NAME : BAL_RUN_CMD_NAME);
        command.add("--debug");
        command.add(debuggeePort);
        command.add("--experimental");

        // Adds file name, only if single file debugging.
        if (balFile != null) {
            command.add(balFile);
        }

        boolean networkLogs = false;
        if (args.get(ARG_NETWORK_LOGS) instanceof Boolean) {
            networkLogs = (boolean) args.get(ARG_NETWORK_LOGS);
        } else if (args.get(ARG_NETWORK_LOGS) instanceof String) {
            networkLogs = Boolean.parseBoolean((String) args.get(ARG_NETWORK_LOGS));
        }
        if (networkLogs && !testDebugging && args.get(ARG_NETWORK_LOGS_PORT) instanceof Double) {
            Double networkLogsPort = (Double) args.get(ARG_NETWORK_LOGS_PORT);
            command.add("--b7a.http.tracelog.host=localhost");
            command.add("--b7a.http.tracelog.port=" + networkLogsPort.intValue());
        }

        ArrayList<String> commandOptions = new ArrayList<>();
        if (args.get(ARG_COMMAND_OPTIONS) instanceof ArrayList) {
            commandOptions = (ArrayList<String>) args.get(ARG_COMMAND_OPTIONS);
        }
        command.addAll(commandOptions);

        ArrayList<String> scriptArguments = new ArrayList<>();
        if (args.get(ARG_SCRIPT_ARGUMENTS) instanceof ArrayList) {
            scriptArguments = (ArrayList<String>) args.get(ARG_SCRIPT_ARGUMENTS);
        }
        command.addAll(scriptArguments);
        return command;
    }

    public void attachToLaunchedProcess(JBallerinaDebugServer server) {
        try {
            DebugExecutionManager execManager = new DebugExecutionManager();
            VirtualMachine attachedVm = execManager.attach(debuggeePort);
            EventRequestManager erm = attachedVm.eventRequestManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            server.setDebuggeeVM(attachedVm);
            server.setExecutionManager(execManager);
        } catch (IOException | IllegalConnectorArgumentsException e) {
            LOGGER.error("Debugger failed to attach");
        }
    }

    public String getBallerinaHome() {
        return ballerinaHome;
    }
}
