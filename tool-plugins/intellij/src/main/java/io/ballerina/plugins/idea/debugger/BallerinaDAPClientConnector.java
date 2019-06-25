/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.ballerina.plugins.idea.debugger.client.DAPClient;
import io.ballerina.plugins.idea.debugger.client.DAPRequestManager;
import io.ballerina.plugins.idea.debugger.client.connection.BallerinaSocketStreamConnectionProvider;
import io.ballerina.plugins.idea.debugger.client.connection.BallerinaStreamConnectionProvider;
import io.ballerina.plugins.idea.debugger.protocol.Command;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Used to communicate with debug server.
 */
public class BallerinaDAPClientConnector {

    private static final Logger LOG = Logger.getInstance(BallerinaDAPClientConnector.class);

    private Project project;
    private String host;
    private int port;
    private DAPClient debugClient;
    private IDebugProtocolServer debugServer;
    private DAPRequestManager requestManager;
    private BallerinaStreamConnectionProvider streamConnectionProvider;
    private Future<Void> launcherFuture;
    private CompletableFuture<Capabilities> initializeFuture;
    private Capabilities initializeResult;
    private ConnectionState myConnectionState;

    private static final int DEBUG_ADAPTOR_PORT = 4711;
    private static final String CONFIG_SOURCEROOT = "sourceRoot";
    private static final String CONFIG_DEBUGEE_PORT = "debuggeePort";
    private static final long TIMEOUT_INIT = 5000;


    public BallerinaDAPClientConnector(@NotNull Project project, @NotNull String host, int port) {
        this.project = project;
        this.host = host;
        this.port = port;
        myConnectionState = ConnectionState.NOT_CONNECTED;
    }

    public DAPRequestManager getRequestManager() {
        return requestManager;
    }

    public Project getProject() {
        return project;
    }

    public int getPort() {
        return port;
    }

    void createConnection() {
        try {
            myConnectionState = ConnectionState.CONNECTING;
            streamConnectionProvider = createConnectionProvider(project.getBasePath());
            streamConnectionProvider.start();
            Pair<InputStream, OutputStream> streams = new ImmutablePair<>(streamConnectionProvider.getInputStream(),
                    streamConnectionProvider.getOutputStream());
            InputStream inputStream = streams.getKey();
            OutputStream outputStream = streams.getValue();

            if (inputStream == null || outputStream == null) {
                LOG.warn("Unable to establish connection with the debug server.");
            }

            debugClient = new DAPClient();
            Launcher<IDebugProtocolServer> clientLauncher = DSPLauncher.createClientLauncher(debugClient,
                    inputStream, outputStream);
            debugServer = clientLauncher.getRemoteProxy();
            launcherFuture = clientLauncher.startListening();

            InitializeRequestArguments initParams = new InitializeRequestArguments();
            initParams.setAdapterID("BallerinaDebugClient");

            initializeFuture = debugServer.initialize(initParams).thenApply(res -> {
                initializeResult = res;
                LOG.info("Debug server initialize result received.");
                debugClient.initialized();
                requestManager = new DAPRequestManager(this, debugClient, debugServer, initializeResult);
                debugClient.connect(requestManager);
                myConnectionState = ConnectionState.CONNECTED;
                return res;
            });

        } catch (IOException e) {
            myConnectionState = ConnectionState.NOT_CONNECTED;
            LOG.warn("Connecting to the DAP server failed.", e);
        }
    }

    @NotNull
    public String getAddress() {
        return String.format("host:%s and port: %d", host, port);
    }

    void sendCommand(Command command) {
        if (isConnected()) {
            if (command == Command.ATTACH) {
                Map<String, Object> requestArgs = new HashMap<>();
                requestArgs.put(CONFIG_SOURCEROOT, project.getBasePath());
                requestArgs.put(CONFIG_DEBUGEE_PORT, Integer.toString(port));
                try {
                    requestManager.attach(requestArgs);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOG.warn("Attaching to the debug adapter failed", e);
                }
            } else if (command == Command.STOP) {
                DisconnectArguments disconnectArgs = new DisconnectArguments();
                disconnectArgs.setTerminateDebuggee(false);
                try {
                    requestManager.disconnect(disconnectArgs);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOG.warn("Disconnecting from the debug adapter failed", e);
                }
            }
        }
    }

    void sendCommand(Command command, String threadId) {
        if (isConnected()) {
//            debugClient.sendText(generateRequest(command, threadId));
        }
    }

    private String generateRequest(Command command) {
        return "{\"command\":\"" + command + "\"}";
    }

    private String generateRequest(Command command, String threadId) {
        return "{\"command\":\"" + command + "\", \"threadId\":\"" + threadId + "\"}";
    }

    void send(String json) {
        if (isConnected()) {
//            debugClient.sendText(json);
        }
    }

    boolean isConnected() {
        return debugClient != null && isActive();
    }

    void close() {
        if (debugClient != null) {
//            debugClient.shutDown();
        }
    }

    String getState() {
        if (myConnectionState == ConnectionState.NOT_CONNECTED) {
            return "Not connected. Waiting for a connection.";
        } else if (myConnectionState == ConnectionState.CONNECTED) {
            return "Connected to " + getAddress() + ".";
        } else if (myConnectionState == ConnectionState.DISCONNECTED) {
            return "Disconnected.";
        } else if (myConnectionState == ConnectionState.CONNECTING) {
            return "Connecting to " + getAddress() + ".";
        }
        return "Unknown";
    }

    private enum ConnectionState {
        NOT_CONNECTED, CONNECTING, CONNECTED, DISCONNECTED
    }

    public BallerinaStreamConnectionProvider createConnectionProvider(String workingDir) {
        return new BallerinaSocketStreamConnectionProvider(new ArrayList<>(Collections.singleton(
                "/home/nino/Desktop/ballerina/distribution/zip/jballerina-tools/build/extracted-distributions/" +
                        "jballerina-tools-0.992.0-m2-SNAPSHOT/lib/tools/debug-adapter/launcher/" +
                        "debug-adapter-launcher.sh")), workingDir, host, DEBUG_ADAPTOR_PORT);
    }

    public boolean isActive() {
        return launcherFuture != null && !launcherFuture.isDone() && !launcherFuture.isCancelled()
                && myConnectionState == ConnectionState.CONNECTED;
    }
}
