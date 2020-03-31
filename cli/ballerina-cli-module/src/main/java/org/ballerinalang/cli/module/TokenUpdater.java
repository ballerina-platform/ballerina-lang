// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.cli.module;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.ballerinalang.cli.module.util.ErrorUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;

import javax.ws.rs.core.HttpHeaders;

import static org.ballerinalang.cli.module.util.CliModuleConstants.SETTINGS_TOML_FILE;

/**
 * This class has a service which updates the access token in the `Settings.toml` file.
 *
 * @since 1.2.0
 */
public class TokenUpdater {

    private static PrintStream errStream = System.err;
    private static PrintStream outStream = System.out;

    private TokenUpdater() {
    }

    /**
     * Creating the server which updates the access token in the `Settings.toml` file.
     *
     * @param settingsTomlFilePath `Settings.toml` file path
     */
    public static void execute(String settingsTomlFilePath) {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(9295), 0);
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("error occurred while creating the server: " + e.getMessage() +
                    "Access token is missing in " + settingsTomlFilePath +
                    "\nAuto update failed. Please visit https://central.ballerina.io, get token and add it to the" +
                    settingsTomlFilePath + " file.");
        }
        server.createContext("/update-settings", new TokenUpdateHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    /**
     * Http handler of the server.
     */
    static class TokenUpdateHandler implements HttpHandler {

        @Override public void handle(HttpExchange httpExchange) {
            String token = getToken(httpExchange.getRequestURI().getPath());
            String currentUsersHomeDir = System.getProperty("user.home");
            String settingsTomlPath = String.valueOf(Paths.get(currentUsersHomeDir, ".ballerina", SETTINGS_TOML_FILE));
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(settingsTomlPath);
                String str = "[central]\naccesstoken=\"" + token + "\"";
                outputStream.write(str.getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                throw ErrorUtil.createCommandException("Settings.toml file could not be found: " + settingsTomlPath);
            } catch (IOException e) {
                throw ErrorUtil.createCommandException(
                        "error occurred while writing to the Settings.toml file: " + e.getMessage());
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    errStream.println("error occurred while closing the output stream: " + e.getMessage());
                }
            }
            outStream.println("token updated");

            OutputStream os = null;
            try {
                String response = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>";
                httpExchange.getResponseHeaders()
                        .put(HttpHeaders.CONTENT_TYPE, Collections.singletonList("image/svg+xml"));
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK,
                        response.getBytes(StandardCharsets.UTF_8).length);
                os = httpExchange.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw ErrorUtil
                        .createCommandException("error occurred while generating the response: " + e.getMessage());
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    errStream.println("error occurred while closing the output stream: " + e.getMessage());
                }
            }
        }

        /**
         * Get access token from url.
         *
         * @param uri url with access token
         * @return access token
         */
        private static String getToken(String uri) {
            String[] uriParts = uri.split("/");
            return uriParts[uriParts.length - 1];
        }
    }
}
