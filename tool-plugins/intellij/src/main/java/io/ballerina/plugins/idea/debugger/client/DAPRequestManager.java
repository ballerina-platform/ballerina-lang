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

import io.ballerina.plugins.idea.debugger.BallerinaDAPClientConnector;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinueResponse;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Debugger Adaptor protocol based client request handler implementation.
 */
public class DAPRequestManager {

    private BallerinaDAPClientConnector clientConnector;
    private final DAPClient client;
    private final IDebugProtocolServer server;
    private final Capabilities serverCapabilities;

    private static final int TIMEOUT_SET_BREAKPOINTS = 1000;
    private static final int TIMEOUT_CONFIG_DONE = 1000;
    private static final int TIMEOUT_THREADS = 2000;
    private static final int TIMEOUT_STACK_TRACE = 2000;
    private static final int TIMEOUT_SCOPES = 2000;
    private static final int TIMEOUT_VARIABLES = 2000;
    private static final int TIMEOUT_STEP_OVER = 2000;
    private static final int TIMEOUT_STEP_IN = 2000;
    private static final int TIMEOUT_STEP_OUT = 2000;
    private static final int TIMEOUT_RESUME = 2000;
    private static final int TIMEOUT_DISCONNECT = 1000;

    public DAPRequestManager(BallerinaDAPClientConnector clientConnector, DAPClient client, IDebugProtocolServer server,
                             Capabilities serverCapabilities) {
        this.clientConnector = clientConnector;
        this.client = client;
        this.server = server;
        this.serverCapabilities = serverCapabilities;
    }

    BallerinaDAPClientConnector getClientConnector() {
        return clientConnector;
    }

    public SetBreakpointsResponse setBreakpoints(SetBreakpointsArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<SetBreakpointsResponse> resp = server.setBreakpoints(args);
            return resp.get(TIMEOUT_SET_BREAKPOINTS, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void configurationDone(ConfigurationDoneArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.configurationDone(args);
            resp.get(TIMEOUT_CONFIG_DONE, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void attach(Map<String, Object> args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.attach(args);
            resp.get(TIMEOUT_CONFIG_DONE, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public ThreadsResponse threads() throws Exception {
        if (checkStatus()) {
            CompletableFuture<ThreadsResponse> resp = server.threads();
            return resp.get(TIMEOUT_THREADS, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public StackTraceResponse stackTrace(StackTraceArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<StackTraceResponse> resp = server.stackTrace(args);
            return resp.get(TIMEOUT_STACK_TRACE, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    @JsonRequest
    public ScopesResponse scopes(ScopesArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<ScopesResponse> resp = server.scopes(args);
            return resp.get(TIMEOUT_SCOPES, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public VariablesResponse variables(VariablesArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<VariablesResponse> resp = server.variables(args);
            return resp.get(TIMEOUT_VARIABLES, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void next(NextArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.next(args);
            resp.get(TIMEOUT_STEP_OVER, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void stepIn(StepInArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.stepIn(args);
            resp.get(TIMEOUT_STEP_IN, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void stepOut(StepOutArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.stepOut(args);
            resp.get(TIMEOUT_STEP_OUT, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public ContinueResponse resume(ContinueArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<ContinueResponse> resp = server.continue_(args);
            return resp.get(TIMEOUT_RESUME, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void disconnect(DisconnectArguments args) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.disconnect(args);
            resp.get(TIMEOUT_DISCONNECT, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    private boolean checkStatus() {
        return clientConnector != null && clientConnector.isConnected();
    }
}
