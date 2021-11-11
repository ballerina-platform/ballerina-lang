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

package org.ballerinalang.debugadapter.launcher;

import org.ballerinalang.debugadapter.JBallerinaDebugServer;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Launch debugger adapter protocol server instance.
 */
public class DebugAdapterLauncher {

    private static final int DEFAULT_PORT = 4711;
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugAdapterLauncher.class);

    public static void main(String[] args) {
        // Configures debug server port.
        int debugServerPort = args.length != 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        try {
            ServerSocket serverSocket = new ServerSocket(debugServerPort);
            PrintStream out = System.out;
            out.println("Debug server started on " + debugServerPort);

            // Initializes I/O streams.
            Socket clientSocket = serverSocket.accept();
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

            // Initializes LSP debug server launcher.
            JBallerinaDebugServer debugServer = new JBallerinaDebugServer();
            Launcher<IDebugProtocolClient> serverLauncher = DSPLauncher.createServerLauncher(debugServer,
                    inputStream, outputStream);
            IDebugProtocolClient client = serverLauncher.getRemoteProxy();
            debugServer.connect(client);
            serverLauncher.startListening();
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to create debug server socket connection with port: %d, due to: %s",
                    debugServerPort, e.getMessage()), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
