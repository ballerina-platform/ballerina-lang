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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Launch debugger adapter protocol server instance.
 */
public class Launcher {

    private static final int DEFAULT_PORT = 4711;
    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        ServerSocket server;
        DataOutputStream os;
        DataInputStream is;
        Socket clientSocket;

        try {
            int port = args.length != 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
            server = new ServerSocket(port);
            PrintStream out = System.out;
            out.println("Debug server started on " + port);
            clientSocket = server.accept();
            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());
            JBallerinaDebugServer jBallerinaDebugServer = new JBallerinaDebugServer();
            org.eclipse.lsp4j.jsonrpc.Launcher<IDebugProtocolClient> serverLauncher = DSPLauncher.createServerLauncher(
                    jBallerinaDebugServer, is, os);
            IDebugProtocolClient client = serverLauncher.getRemoteProxy();
            jBallerinaDebugServer.connect(client);
            serverLauncher.startListening();
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
