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

package org.ballerinalang.debugadapter.launchrequest;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import org.ballerinalang.debugadapter.DebuggerAttachingVM;
import org.ballerinalang.debugadapter.terminator.OSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Launcher abstract implementation.
 */
public abstract class LauncherImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(LauncherImpl.class);
    private final Map<String, Object> args;
    private final String ballerinaHome;
    private String debuggeePort;

    LauncherImpl(Map<String, Object> args) {
        this.debuggeePort = args.get("debuggeePort") == null ? "" : args.get("debuggeePort").toString();
        if (debuggeePort.length() == 0) {
            LOGGER.error("Required param missing debuggeePort");
        }

        ballerinaHome = args.get("ballerina.home") == null ? "" : args.get("ballerina.home").toString();

        if (ballerinaHome.length() == 0) {
            LOGGER.error("Required param missing ballerina.home");
        }

        this.args = args;
    }

    ArrayList<String> getLauncherCommand(String balFile) {
        String ballerinaExec = ballerinaHome + File.separator + "bin" + File.separator + "ballerina";
        if (OSUtils.isWindows()) {
            ballerinaExec = ballerinaExec + ".bat";
        }

        String ballerinaCmd = args.get("ballerina.command") == null ? "" : args.get("ballerina.command").toString();

        // override ballerina exec if ballerina.command is provided.
        if (ballerinaCmd.length() > 0) {
            ballerinaExec = ballerinaCmd;
        }

        // TODO: validate file path
        ArrayList<String> command = new ArrayList<String>();
        command.add(ballerinaExec);
        boolean debugTests = args.get("debugTests") != null && (boolean) args.get("debugTests");
        if (debugTests) {
            command.add("test");
            command.add("--debug");
            command.add(debuggeePort);
        } else {
            command.add("run");
            command.add("--debug");
            command.add(debuggeePort);
        }

        command.add("--experimental");

        command.add(balFile);

        boolean networkLogs = args.get("networkLogs") != null && (boolean) args.get("networkLogs");
        if (networkLogs && !debugTests) {
            Double networkLogsPort = (Double) args.get("networkLogsPort");
            command.add("--b7a.http.tracelog.host=localhost");
            command.add("--b7a.http.tracelog.port=" + networkLogsPort.intValue());
        }

        ArrayList<String> commandOptions = (ArrayList<String>) args.get("commandOptions");
        commandOptions = commandOptions == null ? new ArrayList<>() : commandOptions;
        command.addAll(commandOptions);

        ArrayList<String> scriptArguments = (ArrayList<String>) args.get("scriptArguments");
        scriptArguments = scriptArguments == null ? new ArrayList<>() : scriptArguments;
        command.addAll(scriptArguments);
        return command;
    }

    public VirtualMachine attachToLaunchedProcess() {
        try {
            VirtualMachine debuggee = new DebuggerAttachingVM(Integer.parseInt(debuggeePort)).initialize();
            EventRequestManager erm = debuggee.eventRequestManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            return debuggee;
        } catch (IOException | IllegalConnectorArgumentsException e) {
            LOGGER.error("Debugger failed to attach");
        }
        return null;
    }

    public String getBallerinaHome() {
        return ballerinaHome;
    }
}
