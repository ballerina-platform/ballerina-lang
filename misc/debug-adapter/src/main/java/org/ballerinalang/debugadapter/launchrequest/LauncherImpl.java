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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Launcher abstract implementation.
 */
public abstract class LauncherImpl {
    private final Map<String, Object> args;
    private String debuggeePort;

    LauncherImpl(Map<String, Object> args) {
        String debuggeePort = args.get("debuggeePort").toString();
        this.debuggeePort = debuggeePort;
        this.args = args;
    }

    ArrayList<String> getLauncherCommand(String balFile) {
        String ballerinaHome = args.get("ballerina.home").toString();
        String debuggeePort = args.get("debuggeePort").toString();
        String ballerinaExec = ballerinaHome + File.separator + "bin" + File.separator + "ballerina";
        if (OSUtils.WINDOWS.equals(OSUtils.getOperatingSystem())) {
            ballerinaExec = ballerinaExec + ".bat";
        }

        // TODO: validate file path
        ArrayList<String> command = new ArrayList<String>();
        command.add(ballerinaExec);
        boolean debugTests = args.get("debugTests") != null && (boolean) args.get("debugTests");
        if (debugTests) {
            command.add("test");
        } else {
            command.add("run");
        }
        command.add("--debug");
        command.add(debuggeePort);
        ArrayList<String> commandOptions = (ArrayList<String>) args.get("commandOptions");
        commandOptions = commandOptions == null ? new ArrayList<>() : commandOptions;
        command.addAll(commandOptions);

        command.add("--experimental");

        boolean networkLogs = args.get("networkLogs") != null && (boolean) args.get("networkLogs");
        if (networkLogs) {
            Double networkLogsPort = (Double) args.get("networkLogsPort");
            command.add("-e");
            command.add("b7a.http.tracelog.host=localhost");
            command.add("-e");
            command.add("b7a.http.tracelog.port=" + networkLogsPort.intValue());
        }
        command.add(balFile);

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
        } catch (IOException e) {
        } catch (IllegalConnectorArgumentsException e) {
        }
        return null;
    }
}
