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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
public class BMainInstance implements BMain {
    private static final Logger log = LoggerFactory.getLogger(BMainInstance.class);
    private static final String JAVA_OPTS = "JAVA_OPTS";
    private BalServer balServer;
    private String agentArgs;

    public BMainInstance(BalServer balServer) throws BallerinaTestException {
        this.balServer = balServer;
        initialize();
    }

    /**
     * Initialize the server instance with properties.
     *
     * @throws BallerinaTestException when an exception is thrown while initializing the server
     */
    private void initialize() throws BallerinaTestException {
        configureAgentArgs();
    }

    private void configureAgentArgs() throws BallerinaTestException {
        String jacocoArgLine = System.getProperty("jacoco.agent.argLine");
        if (jacocoArgLine == null || jacocoArgLine.isEmpty()) {
            log.warn("Running integration test without jacoco test coverage");
            return;
        }
        agentArgs = jacocoArgLine + " ";
    }

    @Override
    public void runMain(String balFile) throws BallerinaTestException {
        runMain(balFile, new String[]{}, new String[]{});
    }

    @Override
    public void runMain(String balFile, LogLeecher[] leechers) throws BallerinaTestException {
        runMain(balFile, new String[]{}, new String[]{}, leechers);
    }

    @Override
    public void runMain(String balFile, String[] flags, String[] args) throws BallerinaTestException {
        runMain(balFile, flags, args, null, null);
    }

    @Override
    public void runMain(String balFile, String[] flags,
                        String[] args, LogLeecher[] leechers) throws BallerinaTestException {
        runMain(balFile, flags, args, null, new String[]{}, leechers);
    }

    @Override
    public void runMain(String balFile, String[] flags, String[] args, Map<String, String> envProperties,
                        String[] clientArgs) throws BallerinaTestException {
        runMain(balFile, flags, args, envProperties, clientArgs, null);
    }

    @Override
    public void runMain(String balFile, String[] flags, String[] args, Map<String, String> envProperties,
                        String[] clientArgs, LogLeecher[] leechers) throws BallerinaTestException {
        if (balFile == null || balFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid ballerina program file name provided, name - " + balFile);
        }

        if (args == null) {
            args = new String[]{};
        }

        if (envProperties == null) {
            envProperties = new HashMap<>();
        }

        String[] newArgs = ArrayUtils.addAll(flags, balFile);
        newArgs = ArrayUtils.addAll(newArgs, args);

        addJavaAgents(envProperties);

        runMain("run", newArgs, envProperties, clientArgs, leechers, balServer.getServerHome());
    }

    @Override
    public void runMain(String sourceRoot, String packagePath) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, new String[]{}, new String[]{});
    }

    @Override
    public void runMain(String sourceRoot, String packagePath, LogLeecher[] leechers) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, new String[]{}, new String[]{}, leechers);
    }

    @Override
    public void runMain(String sourceRoot, String packagePath,
                        String[] flags, String[] args) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, flags, args, null, null);
    }

    @Override
    public void runMain(String sourceRoot, String packagePath, String[] flags, String[] args,
                        LogLeecher[] leechers) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, flags, args, null, new String[]{}, leechers);
    }

    @Override
    public void runMain(String sourceRoot, String packagePath, String[] flags, String[] args,
                        Map<String, String> envProperties, String[] clientArgs) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, flags, args, envProperties, clientArgs, null);
    }

    @Override
    public void runMain(String sourceRoot, String packagePath,
                        String[] flags, String[] args, Map<String, String> envProperties,
                        String[] clientArgs, LogLeecher[] leechers) throws BallerinaTestException {
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

        String[] newArgs = ArrayUtils.addAll(flags, "--sourceroot", sourceRoot, packagePath);
        newArgs = ArrayUtils.addAll(newArgs, args);

        addJavaAgents(envProperties);

        runMain("run", newArgs, envProperties, clientArgs, leechers, balServer.getServerHome());
    }

    private synchronized void addJavaAgents(Map<String, String> envProperties) throws BallerinaTestException {
        String javaOpts = "";
        if (envProperties.containsKey(JAVA_OPTS)) {
            javaOpts = envProperties.get(JAVA_OPTS);
        }
        if (javaOpts.contains("jacoco.agent")) {
            return;
        }
        javaOpts = agentArgs + javaOpts;
        envProperties.put(JAVA_OPTS, javaOpts);
    }

    /**
     * Executing the sh or bat file to start the server.
     *
     * @param command       command to run
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @param commandDir    where to execute the command
     * @throws BallerinaTestException if starting services failed
     */
    public void runMain(String command, String[] args, Map<String, String> envProperties, String[] clientArgs,
                         LogLeecher[] leechers, String commandDir) throws BallerinaTestException {
        String scriptName = Constant.BALLERINA_SERVER_SCRIPT_NAME;
        String[] cmdArray;
        try {

            if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
                cmdArray = new String[]{"cmd.exe", "/c", balServer.getServerHome() +
                        File.separator + "bin" + File.separator + scriptName + ".bat", command};
            } else {
                cmdArray = new String[]{"bash", balServer.getServerHome() +
                        File.separator + "bin/" + scriptName, command};
            }

            String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args)).toArray(String[]::new);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).directory(new File(commandDir));
            if (envProperties != null) {
                Map<String, String> env = processBuilder.environment();
                for (Map.Entry<String, String> entry : envProperties.entrySet()) {
                    env.put(entry.getKey(), entry.getValue());
                }
            }

            Process process = processBuilder.start();

            ServerLogReader serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
            ServerLogReader serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());
            if (leechers == null) {
                leechers = new LogLeecher[]{};
            }
            for (LogLeecher leecher : leechers) {
                switch (leecher.getLeecherType()) {
                    case INFO:
                        serverInfoLogReader.addLeecher(leecher);
                        break;
                    case ERROR:
                        serverErrorLogReader.addLeecher(leecher);
                        break;
                }
            }
            serverInfoLogReader.start();
            serverErrorLogReader.start();
            if (clientArgs != null && clientArgs.length > 0) {
                writeClientArgsToProcess(clientArgs, process);
            }
            process.waitFor();

            serverInfoLogReader.stop();
            serverInfoLogReader.removeAllLeechers();

            serverErrorLogReader.stop();
            serverErrorLogReader.removeAllLeechers();
        } catch (IOException e) {
            throw new BallerinaTestException("Error executing ballerina", e);
        } catch (InterruptedException e) {
            throw new BallerinaTestException("Error waiting for execution to finish", e);
        }
    }

    /**
     * Write client clientArgs to process.
     *
     * @param clientArgs client clientArgs
     * @param process    process executed
     * @throws IOException if something goes wrong
     */
    private void writeClientArgsToProcess(String[] clientArgs, Process process) throws IOException {
        try {
            // Wait until the options are prompted TODO find a better way
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            //Ignore
        }
        OutputStream stdin = process.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

        for (String arguments : clientArgs) {
            writer.write(arguments);
        }
        writer.flush();
        writer.close();
    }
}
