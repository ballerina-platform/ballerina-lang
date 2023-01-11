/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.debugger.test.utils.client;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.utils.DebugServerEventHolder;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.debugger.test.utils.client.connection.SocketStreamConnectionProvider;
import org.ballerinalang.debugger.test.utils.client.connection.StreamConnectionProvider;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.Utils;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Used to communicate with debug server.
 */
public class DAPClientConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DAPClientConnector.class);

    private final String balHome;
    private final Path projectPath;
    private final Path entryFilePath;
    private final String host;
    private final int port;
    private DAPClient debugClient;
    private IDebugProtocolServer debugServer;
    private DAPRequestManager requestManager;
    private StreamConnectionProvider streamConnectionProvider;
    private Future<Void> launcherFuture;
    private Capabilities initializeResult;
    private ConnectionState myConnectionState;
    private final DebugServerEventHolder serverEventHolder;
    private final int debugAdapterPort;
    private final boolean supportsRunInTerminalRequest;

    private static final String CONFIG_SOURCE = "script";
    private static final String CONFIG_DEBUGEE_HOST = "debuggeeHost";
    private static final String CONFIG_DEBUGEE_PORT = "debuggeePort";
    private static final String CONFIG_BAL_HOME = "ballerina.home";
    private static final String CONFIG_IS_TEST_CMD = "debugTests";

    public DAPClientConnector(String balHome, Path projectPath, Path entryFilePath, int port,
                              boolean supportsRunInTerminalRequest) {
        this(balHome, projectPath, entryFilePath, "localhost", port, supportsRunInTerminalRequest);
    }

    public DAPClientConnector(String balHome, Path projectPath, Path entryFilePath, String host, int port,
                              boolean supportsRunInTerminalRequest) {
        this.balHome = balHome;
        this.projectPath = projectPath;
        this.entryFilePath = entryFilePath;
        this.host = host;
        this.port = port;
        this.supportsRunInTerminalRequest = supportsRunInTerminalRequest;
        this.debugAdapterPort = DebugUtils.findFreePort();
        myConnectionState = ConnectionState.NOT_CONNECTED;
        serverEventHolder = new DebugServerEventHolder();
    }

    public DAPRequestManager getRequestManager() {
        return requestManager;
    }

    public Path getProjectPath() {
        return projectPath;
    }

    public Path getEntryFilePath() {
        return entryFilePath;
    }

    public int getPort() {
        return port;
    }

    public DebugServerEventHolder getServerEventHolder() {
        return serverEventHolder;
    }

    public void createConnection() {
        try {
            myConnectionState = ConnectionState.CONNECTING;
            streamConnectionProvider = createConnectionProvider(balHome);
            streamConnectionProvider.start();

            Pair<InputStream, OutputStream> streams = new ImmutablePair<>(streamConnectionProvider.getInputStream(),
                    streamConnectionProvider.getOutputStream());
            InputStream inputStream = streams.getKey();
            OutputStream outputStream = streams.getValue();

            if (inputStream == null || outputStream == null) {
                LOGGER.warn("Unable to establish connection with the debug server.");
                streamConnectionProvider.stop();
                return;
            }

            debugClient = new DAPClient();
            Launcher<IDebugProtocolServer> clientLauncher = DSPLauncher.createClientLauncher(debugClient,
                    inputStream, outputStream);
            debugServer = clientLauncher.getRemoteProxy();
            launcherFuture = clientLauncher.startListening();

            InitializeRequestArguments initParams = new InitializeRequestArguments();
            initParams.setAdapterID("BallerinaDebugClient");
            initParams.setSupportsRunInTerminalRequest(this.supportsRunInTerminalRequest);

            debugServer.initialize(initParams).thenApply(res -> {
                initializeResult = res;
                LOGGER.info("initialize response received from the debug server.");
                requestManager = new DAPRequestManager(this, debugServer);
                debugClient.connect(requestManager);
                myConnectionState = ConnectionState.CONNECTED;
                return res;
            }).get();

        } catch (RuntimeException e) {
            myConnectionState = ConnectionState.NOT_CONNECTED;
            LOGGER.warn("Runtime error occurred when trying to initialize connection with the debug server.", e);
        } catch (Exception e) {
            myConnectionState = ConnectionState.NOT_CONNECTED;
            LOGGER.warn("Internal error occurred when trying to initialize connection with the debug server.", e);
        }
    }

    public String getAddress() {
        return String.format("host:%s and port: %d", host, port);
    }

    public void attachToServer() throws BallerinaTestException {
        try {
            Map<String, Object> requestArgs = new HashMap<>();
            requestArgs.put(CONFIG_SOURCE, entryFilePath.toString());
            requestArgs.put(CONFIG_DEBUGEE_HOST, host);
            requestArgs.put(CONFIG_DEBUGEE_PORT, Integer.toString(port));
            requestManager.attach(requestArgs);
        } catch (Exception e) {
            LOGGER.error("Debuggee attach request failed.", e);
            throw new BallerinaTestException("Attaching to the debuggee program is failed.", e);
        }
    }

    public void launchServer(DebugUtils.DebuggeeExecutionKind launchKind, Map<String, Object> args)
            throws BallerinaTestException {
        try {
            Map<String, Object> requestArgs = new HashMap<>(args);
            requestArgs.put(CONFIG_SOURCE, entryFilePath.toString());
            requestArgs.put(CONFIG_DEBUGEE_HOST, host);
            requestArgs.put(CONFIG_DEBUGEE_PORT, Integer.toString(port));
            requestArgs.put(CONFIG_BAL_HOME, balHome);
            if (launchKind == DebugUtils.DebuggeeExecutionKind.TEST) {
                requestArgs.put(CONFIG_IS_TEST_CMD, true);
            }
            requestManager.launch(requestArgs);
        } catch (Exception e) {
            LOGGER.warn("Debuggee launch request failed.", e);
            throw new BallerinaTestException("Launching the debug program is failed.", e);
        }
    }

    public void disconnectFromServer() throws Exception {
        DisconnectArguments disconnectArgs = new DisconnectArguments();
        disconnectArgs.setTerminateDebuggee(true);
        requestManager.disconnect(disconnectArgs);
        stop();
    }

    public boolean isConnected() {
        return debugClient != null && launcherFuture != null && !launcherFuture.isCancelled()
                && myConnectionState == ConnectionState.CONNECTED;
    }

    void stop() {
        streamConnectionProvider.stop();
        myConnectionState = ConnectionState.NOT_CONNECTED;
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

    private StreamConnectionProvider createConnectionProvider(String balHome) {

        List<String> processArgs = new ArrayList<>();

        if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
            processArgs.add("cmd.exe");
            processArgs.add("/c");
            processArgs.add(balHome + File.separator + "bin" + File.separator + Constant.BALLERINA_SERVER_SCRIPT_NAME +
                    ".bat");
        } else {
            processArgs.add("bash");
            processArgs.add(balHome + File.separator + "bin" + File.separator + Constant.BALLERINA_SERVER_SCRIPT_NAME);
        }

        processArgs.add(DebugUtils.JBAL_DEBUG_CMD_NAME);
        processArgs.add(Integer.toString(debugAdapterPort));
        return new SocketStreamConnectionProvider(processArgs, projectPath.toString(), host, debugAdapterPort,
                balHome);
    }

    private enum ConnectionState {
        NOT_CONNECTED, CONNECTING, CONNECTED, DISCONNECTED
    }
}
