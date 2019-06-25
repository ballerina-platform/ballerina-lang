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
import io.ballerina.plugins.idea.debugger.BallerinaDAPClientConnector;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.DefaultRequestManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Debugger Adaptor protocol based client request handler implementation.
 */
public class DAPRequestManager {

    private static final Logger LOG = Logger.getInstance(DefaultRequestManager.class);
    private BallerinaDAPClientConnector clientConnector;
    private final DAPClient client;
    private final IDebugProtocolServer server;
    private final Capabilities serverCapabilities;

    private static final int TIMEOUT_SET_BREAKPOINTS = 1000;
    private static final int TIMEOUT_STACK_FRAME = 1000;
    private static final int TIMEOUT_CONFIG_DONE = 1000;
    private static final int TIMEOUT_DISCONNECT = 5000;

    public DAPRequestManager(BallerinaDAPClientConnector clientConnector, DAPClient client, IDebugProtocolServer server,
                             Capabilities serverCapabilities) {
        this.clientConnector = clientConnector;
        this.client = client;
        this.server = server;
        this.serverCapabilities = serverCapabilities;
    }

    public SetBreakpointsResponse setBreakpoints(SetBreakpointsArguments args) throws InterruptedException,
            ExecutionException, TimeoutException {

        if (checkStatus()) {
            CompletableFuture<SetBreakpointsResponse> resp = server.setBreakpoints(args);
            return resp.get(TIMEOUT_SET_BREAKPOINTS, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void configurationDone(ConfigurationDoneArguments args) throws InterruptedException,
            ExecutionException, TimeoutException {

        if (checkStatus()) {
            CompletableFuture<Void> resp = server.configurationDone(args);
            resp.get(TIMEOUT_CONFIG_DONE, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void attach(Map<String, Object> args) throws InterruptedException, ExecutionException, TimeoutException {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.attach(args);
            resp.get(TIMEOUT_CONFIG_DONE, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public StackTraceResponse stackTrace(StackTraceArguments args) throws InterruptedException,
            ExecutionException, TimeoutException {

        if (checkStatus()) {
            CompletableFuture<StackTraceResponse> resp = server.stackTrace(args);
            return resp.get(TIMEOUT_STACK_FRAME, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void disconnect(DisconnectArguments args) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.disconnect(args);
            resp.get(TIMEOUT_DISCONNECT, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public boolean checkStatus() {
        return clientConnector != null && clientConnector.isActive();
    }

}

