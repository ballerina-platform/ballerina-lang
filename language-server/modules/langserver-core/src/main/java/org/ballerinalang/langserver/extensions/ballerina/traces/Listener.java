/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.extensions.ballerina.traces;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TraceLogs Listener.
 *
 */
public class Listener {

    private ServerSocket listenSocket;
    private BufferedReader logReader;
    private BallerinaTraceService ballerinaTraceService;

    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    public Listener(BallerinaTraceService traceService) {
        ballerinaTraceService = traceService;
    }

    /**
     * Start listening.
     */
    public void startListener() {
        Runnable listener = () -> {
            try {
                listenSocket = new ServerSocket(5010);
                while (!listenSocket.isClosed()) {
                    Socket dataSocket = listenSocket.accept();
                    logReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream(), "UTF-8"));
                    String line;
                    while ((line = logReader.readLine()) != null) {
                        JsonObject record = new JsonParser().parse(line).getAsJsonObject();
                        String rawMessage;
                        try {
                            rawMessage = record.get("message").getAsString();
                        } catch (Exception e) {
                            rawMessage = "";
                        }
                        TraceRecord traceRecord = new TraceRecord(LogParser.fromString(rawMessage), record, rawMessage);
                        ballerinaTraceService.pushLogToClient(traceRecord);
                    }
                }
            } catch (IOException e) {
                logger.error("Error starting trace logs listener", e);
            }
        };
        Thread serverThread = new Thread(listener);
        serverThread.start();
    }

    /**
     * stopListener.
     */
    public void stopListener() {
        try {
            if (logReader != null) {
                logReader.close();
            }
            if (listenSocket != null) {
                listenSocket.close();
            }
        } catch (Exception e) {
            logger.error("Error closing trace logs listener", e);
            // do nothing
        }
    }
}
