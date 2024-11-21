/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.launchers.stdio;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.apispec.ApiSpecGenerator;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Entry point of the stdio launcher.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        LogManager.getLogManager().reset();
        Logger globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        globalLogger.setLevel(Level.OFF);
        startServer(System.in, System.out);
    }

    public static void startServer(InputStream in, OutputStream out)
            throws InterruptedException, ExecutionException {
        // Disable writing ballerina central calls to stdout to avoid conflicting with LS client communicating
        // over stdout with the LS
        System.getProperty("enableOutputStream", "false");

        BallerinaLanguageServer server = new BallerinaLanguageServer();
        Launcher<ExtendedLanguageClient> launcher = Launcher.createLauncher(server,
                ExtendedLanguageClient.class, in, out);
        ExtendedLanguageClient client = launcher.getRemoteProxy();
        server.connect(client);
        Future<?> startListening = launcher.startListening();
        startListening.get();
    }

    /**
     * Generates the API specification for the supported JSON-RPC methods.
     *
     * @return a list of JSON objects representing the API specification.
     */
    public static List<JsonObject> generateApiDoc() {
        System.getProperty("enableOutputStream", "false");
        BallerinaLanguageServer langServer = new BallerinaLanguageServer();
        Map<String, JsonRpcMethod> jsonRpcMethodMap = langServer.supportedMethods();
        return jsonRpcMethodMap.values().stream().map(ApiSpecGenerator::generate).toList();
    }
}

