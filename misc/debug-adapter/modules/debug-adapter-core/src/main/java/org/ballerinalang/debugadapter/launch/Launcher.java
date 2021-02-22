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
import org.ballerinalang.debugadapter.config.ClientConfigurationException;
import org.ballerinalang.debugadapter.config.ClientLaunchConfigHolder;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;
import org.ballerinalang.debugadapter.utils.OSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract implementation of Ballerina program launcher.
 */
public abstract class Launcher {

    protected final String projectRoot;
    protected final ClientLaunchConfigHolder configHolder;

    private static final String BAL_RUN_CMD_NAME = "run";
    private static final String BAL_TEST_CMD_NAME = "test";
    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public abstract Process start() throws Exception;

    Launcher(ClientLaunchConfigHolder configHolder, String projectRoot) throws IllegalArgumentException {
        this.configHolder = configHolder;
        this.projectRoot = projectRoot;
    }

    ArrayList<String> getLauncherCommand(String balFile) throws ClientConfigurationException {

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

        boolean isTestDebugMode = configHolder.isTestDebug();

        ArrayList<String> command = new ArrayList<>(ballerinaExec);
        command.add(isTestDebugMode ? BAL_TEST_CMD_NAME : BAL_RUN_CMD_NAME);
        command.add("--debug");
        command.add(Integer.toString(configHolder.getDebuggePort()));

        // Adds file name, only if single file debugging.
        if (balFile != null) {
            command.add(balFile);
        } else if (!isTestDebugMode) {
            command.add(".");
        }

        boolean networkLogs = configHolder.networkLogsEnabled();
        if (networkLogs && !isTestDebugMode && configHolder.getNetworkLogsPort().isPresent()) {
            command.add("--b7a.http.tracelog.host=localhost");
            command.add("--b7a.http.tracelog.port=" + configHolder.getNetworkLogsPort());
        }

        command.addAll(configHolder.getCommandOptions());

        command.addAll(configHolder.getProgramArguments());
        return command;
    }

    public void attachToLaunchedProcess(JBallerinaDebugServer server) {
        try {
            DebugExecutionManager execManager = new DebugExecutionManager(server);
            VirtualMachine attachedVm = execManager.attach(configHolder.getDebuggePort());
            EventRequestManager erm = attachedVm.eventRequestManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            server.getContext().setDebuggee(new VirtualMachineProxyImpl(attachedVm));
            server.setExecutionManager(execManager);
        } catch (IOException | IllegalConnectorArgumentsException | ClientConfigurationException e) {
            LOGGER.error("Debugger failed to attach");
        }
    }
}
