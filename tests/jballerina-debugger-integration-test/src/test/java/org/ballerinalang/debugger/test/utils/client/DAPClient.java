/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.debugger.test.utils.client;

import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.CapabilitiesEventArguments;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.LoadedSourceEventArguments;
import org.eclipse.lsp4j.debug.ModuleEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.ProcessEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;

/**
 * IDebugProtocolClient implementation.
 */
public class DAPClient implements IDebugProtocolClient {

    private DAPRequestManager requestManager;

    @Override
    public void initialized() {
    }

    @Override
    public void stopped(StoppedEventArguments args) {
        requestManager.stopped(args);
    }

    @Override
    public void continued(ContinuedEventArguments args) {
        requestManager.continued(args);
    }

    @Override
    public void exited(ExitedEventArguments args) {
        requestManager.exited(args);
    }

    @Override
    public void terminated(TerminatedEventArguments args) {
        requestManager.terminated(args);
    }

    @Override
    public void thread(ThreadEventArguments args) {
        requestManager.thread(args);
    }

    @Override
    public void output(OutputEventArguments args) {
        requestManager.output(args);
    }

    @Override
    public void breakpoint(BreakpointEventArguments args) {
        requestManager.breakpoint(args);
    }

    @Override
    public void module(ModuleEventArguments args) {
        requestManager.module(args);
    }

    @Override
    public void loadedSource(LoadedSourceEventArguments args) {
        requestManager.loadedSource(args);
    }

    @Override
    public void process(ProcessEventArguments args) {
        requestManager.process(args);
    }

    @Override
    public void capabilities(CapabilitiesEventArguments args) {
        requestManager.capabilities(args);
    }

    public void connect(DAPRequestManager requestManager) {
        this.requestManager = requestManager;
    }

}
