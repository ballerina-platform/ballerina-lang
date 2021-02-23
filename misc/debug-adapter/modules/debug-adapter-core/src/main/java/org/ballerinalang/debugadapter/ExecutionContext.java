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

package org.ballerinalang.debugadapter;

import com.sun.jdi.request.EventRequestManager;
import io.ballerina.projects.Project;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Context holder for debug execution state related information.
 */
public class ExecutionContext {

    private IDebugProtocolClient client;
    private final JBallerinaDebugServer adapter;
    private VirtualMachineProxyImpl debuggee;
    private Project sourceProject;
    private Process launchedProcess;
    private DebugInstruction lastInstruction;

    ExecutionContext(JBallerinaDebugServer adapter) {
        this.adapter = adapter;
        this.client = null;
        this.debuggee = null;
        this.launchedProcess = null;
    }

    public Optional<Process> getLaunchedProcess() {
        return Optional.ofNullable(launchedProcess);
    }

    public void setLaunchedProcess(Process launchedProcess) {
        this.launchedProcess = launchedProcess;
    }

    public IDebugProtocolClient getClient() {
        return client;
    }

    public void setClient(IDebugProtocolClient client) {
        this.client = client;
    }

    public JBallerinaDebugServer getAdapter() {
        return adapter;
    }

    public VirtualMachineProxyImpl getDebuggee() {
        return debuggee;
    }

    public void setDebuggee(VirtualMachineProxyImpl debuggee) {
        this.debuggee = debuggee;
    }

    public Project getSourceProject() {
        return sourceProject;
    }

    public void setSourceProject(Project sourceProject) {
        this.sourceProject = sourceProject;
    }

    public EventRequestManager getEventManager() {
        return debuggee.eventRequestManager();
    }

    public BufferedReader getInputStream() {
        return new BufferedReader(new InputStreamReader(launchedProcess.getInputStream(), StandardCharsets.UTF_8));
    }

    public BufferedReader getErrorStream() {
        return new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream(), StandardCharsets.UTF_8));
    }

    public DebugInstruction getLastInstruction() {
        return lastInstruction;
    }

    public void setLastInstruction(DebugInstruction lastInstruction) {
        this.lastInstruction = lastInstruction;
    }
}
