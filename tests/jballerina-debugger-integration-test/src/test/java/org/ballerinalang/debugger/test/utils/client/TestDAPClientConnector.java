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
import org.ballerinalang.debugger.test.utils.client.connection.TestSocketStreamConnectionProvider;
import org.ballerinalang.debugger.test.utils.client.connection.TestStreamConnectionProvider;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.ballerinalang.debugger.test.utils.DebugUtils.JBAL_DEBUG_CMD_NAME;
import static org.ballerinalang.debugger.test.utils.DebugUtils.findFreePort;

/**
 * Used to communicate with debug server.
 */
public class TestDAPClientConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDAPClientConnector.class);

    private String balHome;
    private String projectPath;
    private String entryFilePath;
    private String host;
    private int port;
    private DAPClient debugClient;
    private IDebugProtocolServer debugServer;
    private DAPRequestManager requestManager;
    private TestStreamConnectionProvider streamConnectionProvider;
    private Future<Void> launcherFuture;
    private CompletableFuture<Capabilities> initializeFuture;
    private Capabilities initializeResult;
    private ConnectionState myConnectionState;
    private DebugServerEventHolder serverEventHolder;
    private int debugAdapterPort;

    private static final String CONFIG_SOURCE = "script";
    private static final String CONFIG_DEBUGEE_HOST = "debuggeeHost";
    private static final String CONFIG_DEBUGEE_PORT = "debuggeePort";
    private static final String CONFIG_BAL_HOME = "ballerina.home";
    private static final String CONFIG_IS_TEST_CMD = "debugTests";

    public TestDAPClientConnector(String balHome, String projectPath,
                                  String entryFilePath,
                                  int port) {
        this(balHome, projectPath, entryFilePath, "localhost", port);
    }

    public TestDAPClientConnector(String balHome, String projectPath,
                                  String entryFilePath, String host,
                                  int port) {
        this.balHome = balHome;
        this.projectPath = projectPath;
        this.entryFilePath = entryFilePath;
        this.host = host;
        this.port = port;
        this.debugAdapterPort = findFreePort();
        myConnectionState = ConnectionState.NOT_CONNECTED;
        serverEventHolder = new DebugServerEventHolder();
    }

    public DAPRequestManager getRequestManager() {
        return requestManager;
    }

    public String getProjectPath() {
        return projectPath;
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
                return;
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
                LOGGER.info("initialize response received from the debug server.");
                requestManager = new DAPRequestManager(this, debugClient, debugServer, initializeResult);
                debugClient.connect(requestManager);
                myConnectionState = ConnectionState.CONNECTED;
                return res;
            });

        } catch (Exception e) {
            myConnectionState = ConnectionState.NOT_CONNECTED;
            LOGGER.warn("Error occurred when trying to initialize connection with the debug server.", e);
        }
    }

    public String getAddress() {
        return String.format("host:%s and port: %d", host, port);
    }

    public void attachToServer() throws BallerinaTestException {
        try {
            Map<String, Object> requestArgs = new HashMap<>();
            requestArgs.put(CONFIG_SOURCE, entryFilePath);
            requestArgs.put(CONFIG_DEBUGEE_HOST, host);
            requestArgs.put(CONFIG_DEBUGEE_PORT, Integer.toString(port));
            requestManager.attach(requestArgs);
        } catch (Exception e) {
            LOGGER.error("Debuggee attach request failed.", e);
            throw new BallerinaTestException("Attaching to the debuggee program is failed.", e);
        }
    }

    public void launchServer(DebugUtils.DebuggeeExecutionKind launchKind) throws BallerinaTestException {
        try {
            Map<String, Object> requestArgs = new HashMap<>();
            requestArgs.put(CONFIG_SOURCE, entryFilePath);
            requestArgs.put(CONFIG_DEBUGEE_HOST, host);
            requestArgs.put(CONFIG_DEBUGEE_PORT, Integer.toString(port));
            requestArgs.put(CONFIG_BAL_HOME, balHome);
            if (launchKind == DebugUtils.DebuggeeExecutionKind.TEST) {
                requestArgs.put(CONFIG_IS_TEST_CMD, Boolean.toString(true));
            }
            requestManager.launch(requestArgs);
        } catch (Exception e) {
            LOGGER.warn("Debuggee launch request failed.", e);
            throw new BallerinaTestException("Launching the debug program is failed.", e);
        }
    }

    public void disconnectFromServer() throws Exception {
        try {
            DisconnectArguments disconnectArgs = new DisconnectArguments();
            disconnectArgs.setTerminateDebuggee(true);
            requestManager.disconnect(disconnectArgs);
            stop();
        } catch (Exception e) {
            throw e;
        }
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

    private TestStreamConnectionProvider createConnectionProvider(String balHome) {

        List<String> processArgs = new ArrayList<>();

        if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
            processArgs.add("cmd.exe");
            processArgs.add("/c");
            processArgs.add(balHome + File.separator + "bin" + File.separator + Constant.JBALLERINA_SERVER_SCRIPT_NAME +
                    ".bat");
        } else {
            processArgs.add("bash");
            processArgs.add(balHome + File.separator + "bin" + File.separator + Constant.JBALLERINA_SERVER_SCRIPT_NAME);
        }

        processArgs.add(JBAL_DEBUG_CMD_NAME);
        processArgs.add(Integer.toString(debugAdapterPort));
        return new TestSocketStreamConnectionProvider(processArgs, projectPath, host, debugAdapterPort);
    }

    private enum ConnectionState {
        NOT_CONNECTED, CONNECTING, CONNECTED, DISCONNECTED
    }
}
