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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * This server socket will use to mock the secure backend.
 */
public class MockSecureSocketServer {

    private static final Logger log = LoggerFactory.getLogger(MockSecureSocketServer.class);

    public static void main(String[] args) {
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        try (ServerSocket sslServerSocket = sslServerSocketFactory.createServerSocket(Integer.valueOf(args[0]))) {
            while (true) {
                try (Socket socket = sslServerSocket.accept();
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()))) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        out.println(line);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static Process start(String port) throws Exception {
        URL resource = MockSecureSocketServer.class.getClassLoader().
                getResource("datafiles/security/keyStore/ballerinaKeystore.p12");
        if (resource == null) {
            throw new Exception("Unable to find the keystore file.");
        }
        String keystorePath = Paths.get(resource.toURI()).toFile().getAbsolutePath();
        log.info("Keystore Path: " + keystorePath);
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = MockSecureSocketServer.class.getCanonicalName();
        String[] commands = new String[8];
        commands[0] = javaBin;
        commands[1] = "-cp";
        commands[2] = classpath;
        commands[3] = "-Djavax.net.ssl.keyStore=" + keystorePath;
        commands[4] = "-Djavax.net.ssl.keyStorePassword=ballerina";
        commands[5] = "-Djavax.net.ssl.keyStoreType=PKCS12";
        commands[6] = className;
        commands[7] = port;
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return builder.start();
    }
}
