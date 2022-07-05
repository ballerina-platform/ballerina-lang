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
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.ballerinalang.test.context.Constant.BALLERINA_AGENT_PATH;

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
    private boolean agentsAdded = false;
    private Process process;
    private ServerLogReader serverInfoLogReader;
    private ServerLogReader serverErrorLogReader;
    private Set<LogLeecher> tmpInfoLeechers = ConcurrentHashMap.newKeySet();
    private Set<LogLeecher> tmpErrorLeechers = ConcurrentHashMap.newKeySet();
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
        String balAgent = Paths.get(System.getProperty(BALLERINA_AGENT_PATH)).toString();

        if (balAgent == null || balAgent.isEmpty()) {
            throw new BallerinaTestException("Cannot start server, Ballerina agent not provided");
        }

        agentArgs = "-javaagent:" + balAgent + "=host=" + agentHost + ",port=" + agentPort
                + ",exitStatus=1,timeout=15000,killStatus=5 ";

        // add jacoco agent
        String jacocoArgLine = "-javaagent:" + Paths.get(balServer.getServerHome())
                .resolve("bre").resolve("lib").resolve("jacocoagent.jar").toString() + "=destfile=" +
                Paths.get(System.getProperty("user.dir"))
                        .resolve("build").resolve("jacoco").resolve("test.exec");

        agentArgs = jacocoArgLine + " " + agentArgs + " ";
    }

    @Override
    public void startServer(String balFile) throws BallerinaTestException {
        startServer(balFile, new int[]{});
    }

    @Override
    public void startServer(String balFile, boolean useBallerinaRunCommand) throws BallerinaTestException {
        assert requiredPorts.length < 20 : "test try to open too many ports : " + requiredPorts.length;
        startServer(balFile, new String[] {}, null, null, requiredPorts, true);
    }

    @Override
    public void startServer(String balFile, int[] requiredPorts) throws BallerinaTestException {
        assert requiredPorts.length < 20 : "test try to open too many ports : " + requiredPorts.length;
        startServer(balFile, new String[] { }, null, requiredPorts);
    }

    @Override
    public void startServer(String balFile,  String[] buildArgs, String[] runtimeArgs, int[] requiredPorts) 
            throws BallerinaTestException {
        startServer(balFile, buildArgs, runtimeArgs, null, requiredPorts);
    }

    @Override
    public void startServer(String balFile, String[] buildArgs, String[] runtimeArgs, Map<String, String> envProperties,
                            int[] requiredPorts) throws BallerinaTestException {
        startServer(balFile, buildArgs, runtimeArgs, envProperties, requiredPorts, false);
    }

    @Override
    public void startServer(String balFile, String[] buildArgs, String[] runtimeArgs, Map<String, String> envProperties,
                            int[] requiredPorts, boolean useBallerinaRunCommand) throws BallerinaTestException {
        if (balFile == null || balFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid ballerina program file name provided, name - " + balFile);
        }

        if (buildArgs == null) {
            buildArgs = new String[]{};
        }

        if (runtimeArgs == null) {
            runtimeArgs = new String[]{};
        }

        if (envProperties == null) {
            envProperties = new HashMap<>();
        }

        String[] newArgs = {balFile};
        newArgs = ArrayUtils.addAll(buildArgs, newArgs);

        addJavaAgents(envProperties);
        if (useBallerinaRunCommand) {
            runBalSource(newArgs, envProperties);
        } else {
            buildBalSource(newArgs);
            runJar(balFile, runtimeArgs, envProperties, requiredPorts);
        }
    }

    @Override
    public void startServer(String sourceRoot, String packagePath) throws BallerinaTestException {
        startServer(sourceRoot, packagePath, new int[]{});
    }

    @Override
    public void startServer(String sourceRoot, String packagePath, int[] requiredPorts) throws BallerinaTestException {
        startServer(sourceRoot, packagePath, new String[]{}, new String[]{}, requiredPorts);
    }

    @Override
    public void startServer(String sourceRoot, String packagePath, String[] buildArgs, String[] runtimeArgs,
                            int[] requiredPorts) throws BallerinaTestException {
        startServer(sourceRoot, packagePath, buildArgs, runtimeArgs, null, requiredPorts);
    }

    @Override
    public void startServer(String sourceRoot, String packagePath, String[] buildArgs, String[] runtimeArgs,
                            Map<String, String> envProperties, int[] requiredPorts) throws BallerinaTestException {
        startServer(sourceRoot, packagePath, buildArgs, runtimeArgs, envProperties, requiredPorts, false);
    }

    @Override
    public void startServer(String sourceRoot, String packagePath, String[] buildArgs, String[] runtimeArgs,
                            Map<String, String> envProperties, int[] requiredPorts, boolean useBallerinaRunCommand)
            throws BallerinaTestException {
        if (sourceRoot == null || sourceRoot.isEmpty()) {
            throw new IllegalArgumentException("Invalid ballerina program file provided, sourceRoot - " + sourceRoot);
        }

        if (buildArgs == null) {
            buildArgs = new String[] {};
        }

        if (runtimeArgs == null) {
            runtimeArgs = new String[] {};
        }

        if (envProperties == null) {
            envProperties = new HashMap<>();
        }
        addJavaAgents(envProperties);

        String[] newArgs = new String[] { sourceRoot };
        newArgs = ArrayUtils.addAll(buildArgs, newArgs);
        if (useBallerinaRunCommand) {
            runBalSource(newArgs, envProperties);
        } else {
            buildBalSource(newArgs);
            runJar(sourceRoot, packagePath, runtimeArgs, envProperties, requiredPorts);
        }
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
        //wait until port to close
        Utils.waitForPortsToClose(requiredPorts, 30000);
        log.info("Server Stopped Successfully");
    }

    private synchronized void addJavaAgents(Map<String, String> envProperties) {
        if (agentsAdded) {
            return;
        }
        String javaOpts = "";
        if (envProperties.containsKey(JAVA_OPTS)) {
            javaOpts = envProperties.get(JAVA_OPTS);
        }
        javaOpts = javaOpts + " " + agentArgs;
        envProperties.put(JAVA_OPTS, javaOpts);
        this.agentsAdded = true;
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
            tmpInfoLeechers.add(leecher);
            return;
        }
        serverInfoLogReader.addLeecher(leecher);
    }

    /**
     * Add a Leecher that listens to error stream.
     *
     * @param leecher The Leecher instance
     */
    public void addErrorLogLeecher(LogLeecher leecher) {
        if (serverErrorLogReader == null) {
            tmpErrorLeechers.add(leecher);
            return;
        }
        serverErrorLogReader.addLeecher(leecher);
    }

    /**
     * Removes all added log leechers from this instance.
     */
    public void removeAllLeechers() {
        serverInfoLogReader.removeAllLeechers();
        serverErrorLogReader.removeAllLeechers();
        tmpInfoLeechers.forEach(logLeecher -> tmpInfoLeechers.remove(logLeecher));
        tmpErrorLeechers.forEach(logLeecher -> tmpErrorLeechers.remove(logLeecher));
    }

    /**
     * Executing the sh or bat file to start the server.
     *
     * @param args     - command line arguments to pass when building bal program
     * @param envProperties - environmental properties to be appended to the environment
     * @throws BallerinaTestException if starting services failed
     */
    private void runBuildTool(String command, String[] args, Map<String, String> envProperties)
            throws BallerinaTestException {

        String[] cmdArray;
        File commandDir = new File(balServer.getServerHome());
        try {
            if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
                cmdArray = new String[]{"cmd.exe", "/c", "bin\\" + Constant.BALLERINA_SERVER_SCRIPT_NAME + ".bat",
                                        command};
            } else {
                cmdArray = new String[]{"bash", "bin/" + Constant.BALLERINA_SERVER_SCRIPT_NAME, command};
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
            tmpInfoLeechers.forEach(leecher -> serverInfoLogReader.addLeecher(leecher));
            serverInfoLogReader.start();
            serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());
            tmpErrorLeechers.forEach(leecher -> serverErrorLogReader.addLeecher(leecher));
            serverErrorLogReader.start();
            process.waitFor(10, TimeUnit.MINUTES);
        } catch (InterruptedException | IOException e) {
            throw new BallerinaTestException("Error starting services", e);
        }
    }

    /**
     * Build the bal source.
     *
     * @param args - command line arguments to pass when building bal program
     * @throws BallerinaTestException if starting services failed
     */
    private void buildBalSource(String[] args)
            throws BallerinaTestException {
        runBuildTool("build", args, null);
    }

    /**
     * Build and run the bal source with ballerina run.
     *
     * @param args          - command line arguments to pass when building bal program
     * @param envProperties - environmental properties to be appended to the environment
     * @throws BallerinaTestException if starting services failed
     */
    private void runBalSource(String[] args, Map<String, String> envProperties)
            throws BallerinaTestException {
        runBuildTool("run", args, envProperties);
    }

    /**
     * Executing jar file built for package.
     *
     * @param sourceRoot    path to the source root
     * @param packageName   package name
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param requiredPorts ports required for the server instance
     * @throws BallerinaTestException if starting services failed
     */
    private void runJar(String sourceRoot, String packageName, String[] args, Map<String, String> envProperties,
                        int[] requiredPorts)
            throws BallerinaTestException {
        File commandDir = new File(balServer.getServerHome());
        executeJarFile(Paths.get(sourceRoot, "target", "bin", packageName + ".jar").toFile().getPath(),
                       args, envProperties, commandDir, requiredPorts);
    }

    /**
     * Executing jar built for bal file.
     *
     * @param balFile       path to bal file
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param requiredPorts ports required for the server instance
     * @throws BallerinaTestException if starting services failed
     */
    private void runJar(String balFile, String[] args, Map<String, String> envProperties, int[] requiredPorts)
            throws BallerinaTestException {
        File commandDir = new File(balServer.getServerHome());
        String balFileName = Paths.get(balFile).getFileName().toString();
        String jarPath = Paths.get(commandDir.getAbsolutePath(), balFileName.substring(0, balFileName.length() -
                4) + ".jar").toString();
        executeJarFile(jarPath, args, envProperties, commandDir, requiredPorts);
    }

    /**
     * Executing jar file.
     *
     * @param jarPath       path to jar file location
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param commandDir    where to execute the command
     * @param requiredPorts ports required for the server instance
     * @throws BallerinaTestException if starting services failed
     */
    private void executeJarFile(String jarPath, String[] args, Map<String, String> envProperties, 
                                File commandDir, int[] requiredPorts) throws BallerinaTestException {
        try {
            if (this.requiredPorts == null) {
                this.requiredPorts = new int[]{};
            }
            this.requiredPorts = ArrayUtils.addAll(this.requiredPorts, requiredPorts);
            this.requiredPorts = ArrayUtils.addAll(this.requiredPorts, agentPort);

            //Check whether agent port is available.
            Utils.checkPortsAvailability(this.requiredPorts);

            log.info("Starting Ballerina server..");

            List<String> runCmdSet = new ArrayList<>();
            runCmdSet.add("java");
            if (envProperties.containsKey(JAVA_OPTS)) {
                String[] jvmOptions = envProperties.get(JAVA_OPTS).trim().split(" ");
                for (String jvmOption : jvmOptions) {
                    runCmdSet.add(jvmOption.trim());
                }
            }
            String tempBalHome = new File("src" + File.separator + "test" + File.separator +
                                                  "resources" + File.separator + "ballerina.home").getAbsolutePath();
            runCmdSet.add("-Dballerina.home=" + tempBalHome);
            runCmdSet.addAll(Arrays.asList("-jar", jarPath));
            runCmdSet.addAll(Arrays.asList(args));

            ProcessBuilder processBuilder = new ProcessBuilder(runCmdSet).directory(commandDir);
            Map<String, String> env = processBuilder.environment();
            for (Map.Entry<String, String> entry : envProperties.entrySet()) {
                env.put(entry.getKey(), entry.getValue());
            }
            process = processBuilder.start();

            serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
            tmpInfoLeechers.forEach(leecher -> serverInfoLogReader.addLeecher(leecher));
            serverInfoLogReader.start();
            serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());
            tmpErrorLeechers.forEach(leecher -> serverErrorLogReader.addLeecher(leecher));
            serverErrorLogReader.start();
            log.info("Waiting for port " + agentPort + " to open");
            //TODO: Need to reduce the timeout after build time improvements
            Utils.waitForPortsToOpen(new int[]{agentPort}, 1000 * 60 * 10, false, agentHost);
            log.info("Server Started Successfully.");
        } catch (IOException e) {
            throw new BallerinaTestException("Error starting services", e);
        }
    }
}
