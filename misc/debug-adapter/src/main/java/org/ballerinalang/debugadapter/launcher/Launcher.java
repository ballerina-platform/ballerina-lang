package org.ballerinalang.debugadapter.launcher;/*
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

import org.ballerinalang.debugadapter.JBallerinaDebugServer;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Launcher {

    public static void main(String[] args){
        ServerSocket server = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        DataInputStream stdIn = new DataInputStream(System.in);
        Socket clientSocket = null;

        try {
            server = new ServerSocket(4711);
            System.out.println("Debug server started on 4711");
            clientSocket = server.accept();
            os = new DataOutputStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());

            JBallerinaDebugServer jBallerinaDebugServer = new JBallerinaDebugServer();
            org.eclipse.lsp4j.jsonrpc.Launcher<IDebugProtocolClient> serverLauncher = DSPLauncher.createServerLauncher(jBallerinaDebugServer, is, os);
            IDebugProtocolClient client = serverLauncher.getRemoteProxy();
            jBallerinaDebugServer.connect(client);
            serverLauncher.startListening();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
