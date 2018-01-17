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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.ballerinalang.composer.server.core.Server;
import org.ballerinalang.composer.server.core.ServerConfig;
import org.ballerinalang.composer.server.launcher.command.ServerCommand;
import org.ballerinalang.composer.server.launcher.log.LogManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * Launcher for ballerina composer backend server.
 */
public class ServerLauncher {

    private static final String SYS_BAL_COMPOSER_HOME = "bal.composer.home";
    private static final int DEFAULT_PORT = 8089;


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
        String balHome = System.getProperty(SYS_BAL_COMPOSER_HOME);
        if (balHome == null) {
            balHome = System.getenv(SYS_BAL_COMPOSER_HOME);
        }
        if (balHome == null) {
            // this condition will never reach if the app is started with 'composer' script.
            logger.error("Unable to find ballerina composer home.");
            return;
        }
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
        // reading configurations from server-config.yaml
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File configFile = new File(balHome + "/resources/composer/server/service-config.yaml");
        ServerConfig config = new ServerConfig();
        if (configFile.exists()) {
            try {
                config = mapper.readValue(configFile, ServerConfig.class);
            } catch (IOException e) {
                logger.error("Error while reading server-config.yaml", e);
            }
        }
        // give precedence to server-config.yaml (is this the correct approach?)
        if (config.getServerPort() == 0) {
            config.setServerPort(commandArgs.port);
        }
        // if no port is provided via args or config, grab a free available port
        if (config.getServerPort() == 0) {
            config.setServerPort(getAvailablePort(DEFAULT_PORT));
        }
        // if the selected port is not available, print an error & exit
        if (!isPortAvailable(config.getServerPort())) {
            PrintStream err = System.err;
            err.println("Error: Looks like you may be running the Ballerina composer already ?");
            err.println(String.format("In any case, it appears someone is already using port %d, " +
                    "please kick them out or tell me a different port to use.", config.getServerPort()));
            printUsage();
            System.exit(1);
        }
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

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port number to check for availability
     * @return <tt>true</tt> if the port is available, or <tt>false</tt> if not
     * @throws IllegalArgumentException is thrown if the port number is out of range
     */
    private static boolean isPortAvailable(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // Do nothing
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    /**
     * return a available port from the seed port.
     * If the seed port is available it will return that.
     *
     * @param seed to check port
     * @return seed
     */
    private static int getAvailablePort(int seed) {
        while (!isPortAvailable(seed)) {
            seed++;
        }
        return seed;
    }
}
