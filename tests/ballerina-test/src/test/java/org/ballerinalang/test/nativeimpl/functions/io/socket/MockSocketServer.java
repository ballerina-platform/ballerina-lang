/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.nativeimpl.functions.io.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This server socket will use to mock the backend server.
 */
public class MockSocketServer {

    private static final Logger log = LoggerFactory.getLogger(MockSocketServer.class);

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int port;

    public MockSocketServer(int port) {
        this.port = port;
    }

    public void start() {
        executorService.execute(() -> {
            try {
                serverSocket = new ServerSocket(port);
                clientSocket = serverSocket.accept();
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    out.println(inputLine);
                    if (inputLine.equals("Bye")) {
                        break;
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
        waitTillPortOpen();
    }

    public void stop() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                //Do nothing.
            }
        }
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Do nothing.
            }
        }
        if (out != null) {
            out.close();
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                // Do nothing.
            }
        }
        executorService.shutdownNow();
    }

    private void waitTillPortOpen() {
        for (int counter = 0; !(serverSocket != null && serverSocket.isBound()); counter++) {
            if (counter > 10) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                // Do nothing.
            }
        }
    }
}
