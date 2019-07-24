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
import org.ballerinalang.debugadapter.PackageUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Launches a ballerina module.
 */
public class LaunchModule implements Launch {
    private final String ballerinaExec;
    private final String balFile;
    private final String debuggeePort;

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

    LaunchModule(String ballerinaExec, String balFile, String debuggeePort) {
        this.ballerinaExec = ballerinaExec;
        this.balFile = balFile;
        this.debuggeePort = debuggeePort;
    }

    @Override
    public Process start() {
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command(ballerinaExec, "run", "--debug", debuggeePort, "--experimental",
                PackageUtils.getModuleName(balFile));

        Path projectRoot = PackageUtils.findProjectRoot(Paths.get(balFile));

        processBuilder.directory(projectRoot.toFile());
        try {
            return processBuilder.start();
        } catch (IOException e) {
            return null;
        }
    }
}
