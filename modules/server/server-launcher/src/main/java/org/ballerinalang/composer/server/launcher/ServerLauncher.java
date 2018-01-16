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
package org.ballerinalang.composer.server.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.ballerinalang.composer.server.core.Server;
import org.ballerinalang.composer.server.core.ServerConfig;
import org.ballerinalang.composer.server.launcher.command.ServerCommand;
import org.ballerinalang.composer.server.launcher.log.LogManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Launcher for ballerina composer backend server.
 */
public class ServerLauncher {

    private static Logger logger;

    static {
        try {
            // Update the default log manager.
            LogManagerUtils composerLogManagerUtils = new LogManagerUtils();
            composerLogManagerUtils.updateLogManager();
            logger = LoggerFactory.getLogger(ServerLauncher.class);
        } catch (IOException e) {
            logger.error("Error occurred while setting logging properties.", e);
        }
    }

    public static void main(String[] args) {
        ServerCommand commandArgs = new ServerCommand();
        JCommander jCommander = new JCommander(commandArgs);
        jCommander.setProgramName("composer");
        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            PrintStream err = System.err;
            err.println("Invalid argument passed.");
            printUsage();
            return;
        }
        if (commandArgs.helpFlag) {
            PrintStream out = System.out;
            out.println("Ballerina composer, helps you to visualize and edit ballerina programs.");
            out.println();
            out.println("Find more information at http://ballerinalang.org");
            printUsage();
            return;
        }
        ServerConfig config = new ServerConfig();
        config.setServerPort(commandArgs.port);
        Server server = new Server(config);
        try {
            server.start();
        } catch (Exception e) {
            logger.error("Error while starting Composer Backend Server.", e);
        }
    }

    private static void printUsage() {
        PrintStream out = System.out;
        out.println("");
        out.println("Usage: composer [options]");
        out.println("  Options:");
        out.println("    --port <port_number>      Specify a custom port for file server to start.");
        out.println("    --file <file path>        Specify a Ballerina program file to open at the startup.");
        out.println("    --help -h help            for more information.");
        out.println("");
    }
}
