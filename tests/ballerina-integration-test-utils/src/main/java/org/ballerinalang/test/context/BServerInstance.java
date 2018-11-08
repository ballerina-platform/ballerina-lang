/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.context;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class hold the server information and manage the a server instance.
 *
 * @since 0.982.0
 */
public class BServerInstance implements BServer {
    private static final Logger log = LoggerFactory.getLogger(BServerInstance.class);
    private static final String JAVA_OPTS = "JAVA_OPTS";
    private String agentHost = "localhost";
    private BalServer balServer;
    private int agentPort;
    private String agentArgs;
    private boolean agentsAadded = false;
    private Process process;
    private ServerLogReader serverInfoLogReader;
    private ServerLogReader serverErrorLogReader;
    private ConcurrentHashSet<LogLeecher> tmpLeechers = new ConcurrentHashSet<>();
    private int[] requiredPorts;

    public BServerInstance(BalServer balServer) throws BallerinaTestException {
        this.balServer = balServer;
        initialize();
    }

    /**
     * Initialize the server instance with properties.
     *
     * @throws BallerinaTestException when an exception is thrown while initializing the server
     */
    private void initialize() throws BallerinaTestException {
        agentPort = AgentManager.getInstance().getNextPort();

        configureAgentArgs();
    }

    private void configureAgentArgs() throws BallerinaTestException {
        String balAgent = System.getProperty("ballerina.agent.path");

        if (balAgent == null || balAgent.isEmpty()) {
            throw new BallerinaTestException("Cannot start server, Ballerina agent not provided");
        }

        agentArgs = "-javaagent:" + balAgent + "=host=" + agentHost + ",port=" + agentPort
                + ",exitStatus=1,timeout=15000,killStatus=5 ";

        String jacocoArgLine = System.getProperty("jacoco.agent.argLine");
        if (jacocoArgLine == null || jacocoArgLine.isEmpty()) {
            log.warn("Running integration test without jacoco test coverage");
            return;
        }
        agentArgs = jacocoArgLine + " " + agentArgs + " ";
    }

    @Override
    public void startServer(String balFile) throws BallerinaTestException {
        startServer(balFile, new int[]{});
    }

    @Override
    public void startServer(String balFile, int[] requiredPorts) throws BallerinaTestException {
        startServer(balFile, new String[]{}, requiredPorts);
    }

    @Override
    public void startServer(String balFile, String[] args, int[] requiredPorts) throws BallerinaTestException {
        startServer(balFile, args, null, requiredPorts);
    }

    @Override
    public void startServer(String balFile, String[] args, Map<String, String> envProperties,
                            int[] requiredPorts) throws BallerinaTestException {
        if (balFile == null || balFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid ballerina program file name provided, name - " + balFile);
        }

        if (args == null) {
            args = new String[]{};
        }

        if (envProperties == null) {
            envProperties = new HashMap<>();
        }

        String[] newArgs = {balFile};
        newArgs = ArrayUtils.addAll(args, newArgs);

        addJavaAgents(envProperties);

        startServer(newArgs, envProperties, requiredPorts);
    }

    @Override
    public void startServer(String sourceRoot, String packagePath) throws BallerinaTestException {
        startServer(sourceRoot, packagePath, new int[]{});
    }

    @Override
    public void startServer(String sourceRoot, String packagePath, int[] requiredPorts) throws BallerinaTestException {
        startServer(sourceRoot, packagePath, new String[]{}, requiredPorts);
    }

    @Override
    public void startServer(String sourceRoot, String packagePath, String[] args,
                            int[] requiredPorts) throws BallerinaTestException {
        startServer(sourceRoot, packagePath, args, null, requiredPorts);
    }

    @Override
    public void startServer(String sourceRoot, String packagePath, String[] args,
                            Map<String, String> envProperties, int[] requiredPorts) throws BallerinaTestException {
        if (sourceRoot == null || sourceRoot.isEmpty() || packagePath == null || packagePath.isEmpty()) {
            throw new IllegalArgumentException("Invalid ballerina program file provided, sourceRoot - "
                    + sourceRoot + " packagePath - " + packagePath);
        }

        if (args == null) {
            args = new String[]{};
        }

        if (envProperties == null) {
            envProperties = new HashMap<>();
        }

        String[] newArgs = new String[]{"--sourceroot", sourceRoot, packagePath};
        newArgs = ArrayUtils.addAll(args, newArgs);

        addJavaAgents(envProperties);

        startServer(newArgs, envProperties, requiredPorts);
    }

    /**
     * Stop the server instance which is started by start method.
     *
     * @throws BallerinaTestException if service stop fails
     */
    @Override
    public void shutdownServer() throws BallerinaTestException {
        log.info("Stopping server..");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        try {
            HttpResponse response = HttpClientRequest.doPost(getServiceURLHttp(agentPort, "shutdown"),
                    "shutdown", headers);
            if (response.getResponseCode() != 200) {
                throw new BallerinaTestException("Error shutting down the server, invalid response - "
                        + response.getData());
            }
            cleanupServer();
        } catch (IOException e) {
            throw new BallerinaTestException("Error shutting down the server, error - " + e.getMessage(), e);
        }
    }

    /**
     * Kill the server instance which is started by start method.
     *
     * @throws BallerinaTestException if service stop fails
     */
    @Override
    public void killServer() throws BallerinaTestException {
        log.info("Stopping server..");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        try {
            HttpResponse response = HttpClientRequest.doPost(getServiceURLHttp(agentPort, "kill"),
                    "kill", headers);
            if (response.getResponseCode() != 200) {
                throw new BallerinaTestException("Error killing the server, invalid response - "
                        + response.getData());
            }
            cleanupServer();
        } catch (IOException e) {
            throw new BallerinaTestException("Error shutting down the server, error - " + e.getMessage(), e);
        }
    }

    private void cleanupServer() {
        process.destroy();
        serverInfoLogReader.stop();
        serverErrorLogReader.stop();
        process = null;
        //wait until port to close
        Utils.waitForPortsToClose(requiredPorts, 30000);
        log.info("Server Stopped Successfully");

        if (serverInfoLogReader != null) {
            serverInfoLogReader.stop();
            serverErrorLogReader.removeAllLeechers();
            serverInfoLogReader = null;
        }

        if (serverErrorLogReader != null) {
            serverErrorLogReader.stop();
            serverErrorLogReader.removeAllLeechers();
            serverErrorLogReader = null;
        }
    }

    private synchronized void addJavaAgents(Map<String, String> envProperties) throws BallerinaTestException {
        if (agentsAadded) {
            return;
        }
        String javaOpts = "";
        if (envProperties.containsKey(JAVA_OPTS)) {
            javaOpts = envProperties.get(JAVA_OPTS);
        }
        javaOpts = agentArgs + javaOpts;
        envProperties.put(JAVA_OPTS, javaOpts);
        this.agentsAadded = true;
    }

    /**
     * Return server home path.
     *
     * @return absolute path of the server location
     */
    public String getServerHome() {
        return balServer.getServerHome();
    }

    /**
     * A utility method to construct and return the HTTP service URL by using the given port.
     *
     * @param port        - the port to be used to create the service url.
     * @param servicePath -  http url of the given service.
     * @return The HTTP service URL.
     */
    public String getServiceURLHttp(int port, String servicePath) {
        return "http://" + getServiceUrl(port, servicePath);
    }

    /**
     * A utility method to construct and return the HTTPS service URL by using the given port.
     *
     * @param port        - the port to be used to create the service url.
     * @param servicePath -  http url of the given service.
     * @return The HTTPS service URL.
     */
    public String getServiceURLHttps(int port, String servicePath) {
        return "https://" + getServiceUrl(port, servicePath);
    }

    /**
     * A utility method to construct and return the service URL without scheme by using the given port.
     *
     * @param port        - the port to be used to create the service url.
     * @param servicePath -  http url of the given service.
     * @return The service URL without scheme.
     */
    private String getServiceUrl(int port, String servicePath) {
        return "localhost:" + port + "/" + servicePath;
    }

    /**
     * Add a Leecher which is going to listen to an expected text.
     *
     * @param leecher The Leecher instance
     */
    public void addLogLeecher(LogLeecher leecher) {
        if (serverInfoLogReader == null) {
            tmpLeechers.add(leecher);
            return;
        }
        serverInfoLogReader.addLeecher(leecher);
    }

    /**
     * Removes all added log leechers from this instance.
     */
    public void removeAllLeechers() {
        serverInfoLogReader.removeAllLeechers();
        serverErrorLogReader.removeAllLeechers();
        tmpLeechers.forEach(logLeecher -> tmpLeechers.remove(logLeecher));
    }

    /**
     * Executing the sh or bat file to start the server.
     *
     * @param args          - command line arguments to pass when executing the sh or bat file
     * @param envProperties - environmental properties to be appended to the environment
     * @param requiredPorts - ports required for the server instance
     * @throws BallerinaTestException if starting services failed
     */
    private void startServer(String[] args, Map<String, String> envProperties,
                             int[] requiredPorts) throws BallerinaTestException {
        if (requiredPorts == null) {
            requiredPorts = new int[]{};
        }
        this.requiredPorts = ArrayUtils.addAll(requiredPorts, agentPort);

        //Check whether agent port is available.
        Utils.checkPortsAvailability(requiredPorts);

        log.info("Starting Ballerina server..");

        String scriptName = Constant.BALLERINA_SERVER_SCRIPT_NAME;
        String[] cmdArray;
        File commandDir = new File(balServer.getServerHome());
        try {
            if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
                commandDir = new File(balServer.getServerHome() + File.separator + "bin");
                cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat", "run"};

            } else {
                cmdArray = new String[]{"bash", "bin/" + scriptName, "run"};
            }
            String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args)).toArray(String[]::new);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).directory(commandDir);
            if (envProperties != null) {
                Map<String, String> env = processBuilder.environment();
                for (Map.Entry<String, String> entry : envProperties.entrySet()) {
                    env.put(entry.getKey(), entry.getValue());
                }
            }
            process = processBuilder.start();

            serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
            tmpLeechers.forEach(leacher -> serverInfoLogReader.addLeecher(leacher));
            serverInfoLogReader.start();
            serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());
            serverErrorLogReader.start();
            log.info("Waiting for port " + agentPort + " to open");
            Utils.waitForPortsToOpen(new int[]{agentPort}, 1000 * 60 * 2, false, agentHost);
            log.info("Server Started Successfully.");
        } catch (IOException e) {
            throw new BallerinaTestException("Error starting services", e);
        }
    }
}
