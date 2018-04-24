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
import org.ballerinalang.composer.server.launcher.browser.BrowserLauncher;
import org.ballerinalang.composer.server.launcher.command.ServerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Launcher for ballerina composer backend server.
 */
public class ServerLauncher {
    private static final String PROP_BALLERINA_HOME = "ballerina.home";
    private static final String PROP_COMPOSER_CONFIG_PATH = "composer.config.path";
    private static final String PROP_COMPOSER_PUBLIC_PATH = "composer.public.path";
    private static final String PROP_OPEN_BROWSER = "open.browser";
    private static final String PROP_MSF4J_HOST = "msf4j.host";
    private static final String DEFAULT_INTERFACE = "127.0.0.1";
    private static final int DEFAULT_PORT = 9091;

    private static Logger logger;

    static {
        initLogger();
        logger = LoggerFactory.getLogger(ServerLauncher.class);
    }

    public static void initLogger() {
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.OFF);
    }

    public static void main(String[] args) {
        ServerCommand cmdLineArgs = new ServerCommand();
        JCommander jCommander = new JCommander(cmdLineArgs);
        jCommander.setProgramName("composer");
        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            PrintStream err = System.err;
            err.println("Invalid argument passed.");
            printUsage();
            return;
        }
        if (cmdLineArgs.helpFlag) {
            PrintStream out = System.out;
            out.println("Ballerina composer, helps you to visualize and edit ballerina programs.");
            out.println();
            out.println("Find more information at http://ballerinalang.org");
            printUsage();
            return;
        }
        ServerConfig config = new ServerConfig();
        // get ballerina home - give precedence to ballerina home defined in config file
        // by reading from props or env before reading from config file
        String balHome = System.getProperty(PROP_BALLERINA_HOME);
        if (balHome == null) {
            balHome = System.getenv(PROP_BALLERINA_HOME);
        }
        config.setBallerinaHome(balHome);
        // read public path prop if available - this is the approach with least precedence
        String publicPath = System.getProperty(PROP_COMPOSER_PUBLIC_PATH);
        if (publicPath != null) {
            config.setPublicPath(publicPath);
        }
        // reading configurations from config yaml file
        String configFilePath = System.getProperty(PROP_COMPOSER_CONFIG_PATH);
        if (configFilePath != null) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            File configFile = new File(configFilePath);
            if (configFile.exists()) {
                try {
                    config = mapper.readValue(configFile, ServerConfig.class);
                } catch (IOException e) {
                    logger.debug("Error while reading config file.", e);
                }
            }
        }
        // give precedence to command line arg for port
        if (cmdLineArgs.port != null) {
            config.setPort(cmdLineArgs.port);
        }
        // if no port is provided via cmd args or config file, try to grab an open port
        if (config.getPort() == 0) {
            config.setPort(getAvailablePort(DEFAULT_PORT));
        }
        // if the selected port is not available, print an error & exit
        if (!isPortAvailable(config.getPort())) {
            PrintStream err = System.err;
            err.println("Error: Looks like you may be running the Ballerina composer already ?");
            err.println(String.format("In any case, it appears someone is already using port %d, " +
                                              "please kick them out or tell me a different port to use.",
                                      config.getPort()));
            printUsage();
            System.exit(1);
        }
        // give precedence to interface provided via cmd args
        if (cmdLineArgs.host != null) {
            config.setHost(cmdLineArgs.host);
        }
        // if interface is not provided via config file or cmd args, bind to default interface
        if (config.getHost() == null) {
            config.setHost(DEFAULT_INTERFACE);
        }
        // set msf4j host property
        System.setProperty(PROP_MSF4J_HOST, config.getHost());
        // give precedence to command line arg for public folder
        if (cmdLineArgs.publicPath != null) {
            config.setPublicPath(cmdLineArgs.publicPath);
        }
        Server server = new Server(config);
        try {
            server.start();
            String uRL = "http://" + config.getHost() + ":" + config.getPort();
            PrintStream out = System.out;
            out.println("Composer started successfully at " + uRL);
            if (System.getProperty(PROP_OPEN_BROWSER).equals("true")) {
                BrowserLauncher.startInDefaultBrowser(uRL);
            }
        } catch (Exception e) {
            logger.error("Error while starting Composer Backend Server.", e);
        }
    }

    private static void printUsage() {
        PrintStream out = System.out;
        out.println("");
        out.println("Usage: composer [options]");
        out.println("  Options:");
        out.println("    --port <port_number>           Specify a custom port for file server to start.");
        out.println("    --host <interface>             Specify a custom interface to bind the server.");
        out.println("    --publicPath <public_path>     Specify a custom path to server the public content from.");
        out.println("    --help -h help                 for more information.");
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
     * return a available port from the seed port. If the seed port is available it will return that.
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
