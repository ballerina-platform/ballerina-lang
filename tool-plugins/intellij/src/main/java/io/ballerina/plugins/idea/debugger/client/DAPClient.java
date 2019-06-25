/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.plugins.idea.debugger.client;

import com.intellij.openapi.diagnostic.Logger;
import io.ballerina.plugins.idea.debugger.dto.Message;
import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.CapabilitiesEventArguments;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.LoadedSourceEventArguments;
import org.eclipse.lsp4j.debug.ModuleEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.ProcessEventArguments;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.eclipse.lsp4j.debug.StoppedEventArgumentsReason.BREAKPOINT;

/**
 * IDebugProtocolClient implementation.
 */
public class DAPClient implements IDebugProtocolClient {

    private static final Logger LOG = Logger.getInstance(DAPClient.class);
    private DAPRequestManager requestManager;

    @Override
    public void initialized() {

    }

    @Override
    public void stopped(StoppedEventArguments args) {
        if (args.getReason().equals(BREAKPOINT)) {
            if (requestManager.checkStatus()) {
                StackTraceArguments stackTraceArgs = new StackTraceArguments();
                stackTraceArgs.setThreadId(args.getThreadId());
                try {
                    StackTraceResponse stackTraceResponse = requestManager.stackTrace(stackTraceArgs);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOG.warn("Error occurred when fetching stack frames", e);
                }
            }
        }
        Message message = new Message();
    }

    @Override
    public void continued(ContinuedEventArguments args) {
    }

    @Override
    public void exited(ExitedEventArguments args) {
    }

    @Override
    public void terminated(TerminatedEventArguments args) {
    }

    @Override
    public void thread(ThreadEventArguments args) {
    }

    @Override
    public void output(OutputEventArguments args) {
    }

    @Override
    public void breakpoint(BreakpointEventArguments args) {
    }

    @Override
    public void module(ModuleEventArguments args) {
    }

    @Override
    public void loadedSource(LoadedSourceEventArguments args) {
    }

    @Override
    public void process(ProcessEventArguments args) {
    }

    @Override
    public void capabilities(CapabilitiesEventArguments args) {
    }

    public void connect(DAPRequestManager requestManager) {
        this.requestManager = requestManager;
    }

}
