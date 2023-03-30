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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * This class hold the server information and manage the a server instance.
 *
 * @since 0.982.0
 */
public class BMainInstance implements BMain {
    private static final Logger log = LoggerFactory.getLogger(BMainInstance.class);
    private static final String JAVA_OPTS = "JAVA_OPTS";
    private String agentArgs = "";
    private BalServer balServer;

    private static class StreamGobbler extends Thread {
        private InputStream inputStream;
        private PrintStream printStream;

        public StreamGobbler(InputStream inputStream, PrintStream printStream) {
            this.inputStream = inputStream;
            this.printStream = printStream;
        }

        @Override
        public void run() {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String lineContent = null;
            while (true) {
                try {
                    lineContent = bufferedReader.readLine();
                    if (lineContent == null) {
                        break;
                    }
                    printStream.println(lineContent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


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
        // add jacoco agent
        String jacocoArgLine = "-javaagent:" + Paths.get(balServer.getServerHome())
                .resolve("bre").resolve("lib").resolve("jacocoagent.jar").toString() + "=destfile=" +
                Paths.get(System.getProperty("user.dir"))
                        .resolve("build").resolve("jacoco").resolve("test.exec");
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

        if (flags == null) {
            flags = new String[]{};
        }

        if (envProperties == null) {
            envProperties = new HashMap<>();
        }
        addJavaAgents(envProperties);

        runMain("build", new String[]{balFile}, envProperties, null, leechers, balServer.getServerHome());
        runJar(balFile, ArrayUtils.addAll(flags, args), envProperties, clientArgs, leechers, balServer.getServerHome());
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

        if (flags == null) {
            flags = new String[]{};
        }

        if (args == null) {
            args = new String[]{};
        }

        if (envProperties == null) {
            envProperties = new HashMap<>();
        }
        addJavaAgents(envProperties);

        runMain("build", new String[]{packagePath}, envProperties, null, leechers, sourceRoot);
        runJar(Paths.get(sourceRoot, packagePath).toString(), packagePath, ArrayUtils.addAll(flags, args),
                envProperties, clientArgs, leechers, sourceRoot);
    }

    public synchronized void addJavaAgents(Map<String, String> envProperties) throws BallerinaTestException {
        String javaOpts = "";
        if (envProperties.containsKey(JAVA_OPTS)) {
            javaOpts = envProperties.get(JAVA_OPTS);
        }
        if (javaOpts.contains("jacoco.agent")) {
            return;
        }
        javaOpts = agentArgs + javaOpts;
        if ("".equals(javaOpts)) {
            return;
        }
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
     * Executing the sh or bat file to start the server in debug mode.
     *
     * @param command       command to run
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @param commandDir    where to execute the command
     * @param timeout       timeout for the process waiting time, in seconds.
     * @param isAttachMode  check debuggee started on attach mode
     * @return parent instance process
     * @throws BallerinaTestException if starting services failed
     */
    public Process debugMain(String command, String[] args, Map<String, String> envProperties, String[] clientArgs,
                             LogLeecher[] leechers, String commandDir, int timeout, boolean isAttachMode)
            throws BallerinaTestException {
        String scriptName = Constant.BALLERINA_SERVER_SCRIPT_NAME;
        String[] cmdArray;
        String[] cmdArgs = new String[0];
        Process process = null;

        if (envProperties != null) {
            addJavaAgents(envProperties);
        }

        try {
            if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
                cmdArray = new String[]{"cmd.exe", "/c", balServer.getServerHome() +
                        File.separator + "bin" + File.separator + scriptName + ".bat", command};
            } else {
                cmdArray = new String[]{"bash", balServer.getServerHome() +
                        File.separator + "bin/" + scriptName, command};
            }

            cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args)).toArray(String[]::new);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).directory(new File(commandDir));
            if (envProperties != null) {
                Map<String, String> env = processBuilder.environment();
                for (Map.Entry<String, String> entry : envProperties.entrySet()) {
                    env.put(entry.getKey(), entry.getValue());
                }
            }
            process = processBuilder.start();

            ServerLogReader infoReader = new ServerLogReader("inputStream", process.getInputStream());
            ServerLogReader errorReader = new ServerLogReader("errorStream", process.getErrorStream());
            if (leechers == null) {
                leechers = new LogLeecher[]{};
            }
            for (LogLeecher leecher : leechers) {
                switch (leecher.getLeecherType()) {
                    case INFO:
                        infoReader.addLeecher(leecher);
                        break;
                    case ERROR:
                        errorReader.addLeecher(leecher);
                        break;
                }
            }

            infoReader.start();
            errorReader.start();
            if (clientArgs != null && clientArgs.length > 0) {
                writeClientArgsToProcess(clientArgs, process);
            }

            // process.waitFor() method cannot be used here as the debug process gets suspended waiting for a remote
            // client to be attached, resulting the method waits forever. Therefore we have to manually check whether
            // expected results are received for all the leechers, and forcefully terminate the process if so.
            boolean processFinished = false;
            boolean leechingDone = false;
            int elapsedTime = 0;
            while (elapsedTime < timeout && !processFinished && !leechingDone) {
                processFinished = process.waitFor(1000, TimeUnit.MILLISECONDS);
                // Checks whether all the leechers have received expected logs.
                leechingDone = isLeechingDone(infoReader, errorReader);
                elapsedTime++;
            }

            infoReader.stop();
            infoReader.removeAllLeechers();
            errorReader.stop();
            errorReader.removeAllLeechers();

            // Terminate the process if it is still alive, or either the program is suspended in debug mode
            // (as expected), or still being executed, in launch mode. Do not terminate if the debuggee is in attach
            // mode at this point.
            if (!isAttachMode) {
                terminateProcessWithDescendants(process);
            }

            if (elapsedTime >= timeout) {
                throw new BallerinaTestException("Timeout expired waiting for matching logs in debug mode.");
            } else if (!leechingDone) {
                throw new BallerinaTestException("Program execution in debug mode is finished without receiving the " +
                        "suspend log.");
            } else if (processFinished) {
                throw new BallerinaTestException("Program unexpectedly terminated without being suspended in debug " +
                        "mode.");
            }

        } catch (IOException e) {
            throw new BallerinaTestException("Error executing ballerina", e);
        } catch (InterruptedException e) {
            throw new BallerinaTestException("Error waiting for execution to finish", e);
        } finally {
            // Terminate the process if it is still alive, or either the program is suspended in debug mode
            // (as expected), or still being executed, in launch mode. Do not terminate if the debuggee is in attach
            // mode at this point.
            if (!isAttachMode) {
                terminateProcessWithDescendants(process);
            }
        }
        return process;
    }

    /**
     * Executing the sh or bat file to start the server in debug mode.
     *
     * @param command       command to run
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @param commandDir    where to execute the command
     * @param timeout       timeout for the process waiting time, in seconds.
     * @throws BallerinaTestException if starting services failed
     */
    public void debugMain(String command, String[] args, Map<String, String> envProperties, String[] clientArgs,
                          LogLeecher[] leechers, String commandDir, int timeout) throws BallerinaTestException {
        boolean isAttachMode = false;
        debugMain(command, args, envProperties, clientArgs, leechers, commandDir, timeout, isAttachMode);
    }

    /**
     * checks whether all the expected logs are received.
     *
     * @param infoReader  ServerLogReader instance attached to the process input stream.
     * @param errorReader ServerLogReader instance attached to the  error stream.
     * @return true if all the expected texts are already found, or else false.
     */
    private boolean isLeechingDone(ServerLogReader infoReader, ServerLogReader errorReader) {
        for (LogLeecher leecher : infoReader.getAllLeechers()) {
            if (!leecher.isTextFound()) {
                return false;
            }
        }
        for (LogLeecher leecher : errorReader.getAllLeechers()) {
            if (!leecher.isTextFound()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cleans up all the (sub)processes spawn during ballerina command execution.
     *
     * @param process parent process instance.
     */
    public void terminateProcessWithDescendants(Process process) {
        try {
            // Kills the descendants of the process. The descendants of a process are the children
            // of the process and the descendants of those children, recursively.
            process.descendants().forEach(processHandle -> {
                boolean successful = processHandle.destroy();
                if (!successful) {
                    processHandle.destroyForcibly();
                }
            });

            // Kills the parent process. Whether the process represented by this Process object will be normally
            // terminated or not, is implementation dependent.
            process.destroyForcibly();
            process.waitFor();
        } catch (Exception ignored) {
        }
    }

    /**
     * Executing jar file built for package.
     *
     * @param sourceRoot    path to the source root
     * @param packageName   package name
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @param commandDir    where to execute the command
     * @throws BallerinaTestException if starting services failed
     */
    private void runJar(String sourceRoot, String packageName, String[] args, Map<String, String> envProperties,
                        String[] clientArgs, LogLeecher[] leechers, String commandDir) throws BallerinaTestException {
        executeJarFile(Paths.get(sourceRoot, "target", "bin", packageName + ".jar").toFile().getPath(),
                args, envProperties, clientArgs, leechers, commandDir);
    }

    /**
     * Executing jar built for bal file.
     *
     * @param balFile       path to bal file
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @param commandDir    where to execute the command
     * @throws BallerinaTestException if starting services failed
     */
    private void runJar(String balFile, String[] args, Map<String, String> envProperties, String[] clientArgs,
                        LogLeecher[] leechers, String commandDir) throws BallerinaTestException {
        String balFileName = Paths.get(balFile).getFileName().toString();
        String jarPath = Paths.get(Paths.get(commandDir).toString(), balFileName.substring(0, balFileName.length() -
                4) + ".jar").toString();
        executeJarFile(jarPath, args, envProperties, clientArgs, leechers, commandDir);
    }

    /**
     * Executing jar file.
     *
     * @param jarPath       path to jar file location
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @param commandDir    where to execute the command
     * @throws BallerinaTestException if starting services failed
     */
    private void executeJarFile(String jarPath, String[] args, Map<String, String> envProperties, String[] clientArgs,
                                LogLeecher[] leechers, String commandDir) throws BallerinaTestException {
        try {
            List<String> runCmdSet = new ArrayList<>();
            runCmdSet.add("java");
            if (envProperties.containsKey(JAVA_OPTS)) {
                runCmdSet.add(envProperties.get(JAVA_OPTS).trim());
            }
            String tempBalHome = new File("src" + File.separator + "test" + File.separator +
                    "resources" + File.separator + "ballerina.home").getAbsolutePath();
            runCmdSet.add("-Dballerina.home=" + tempBalHome);
            runCmdSet.addAll(Arrays.asList("-jar", jarPath));
            runCmdSet.addAll(Arrays.asList(args));

            ProcessBuilder processBuilder = new ProcessBuilder(runCmdSet).directory(new File(commandDir));
            Map<String, String> env = processBuilder.environment();
            for (Map.Entry<String, String> entry : envProperties.entrySet()) {
                env.put(entry.getKey(), entry.getValue());
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
        } catch (InterruptedException | IOException e) {
            throw new BallerinaTestException("Error starting services", e);
        }
    }

    /**
     * Executing the sh or bat file to start the server and returns the logs printed to stdout.
     *
     * @param command    command to run
     * @param args       command line arguments to pass when executing the sh or bat file
     * @param commandDir where to execute the command
     * @return logs printed to std out
     * @throws BallerinaTestException if starting services failed or if an error occurs when reading the stdout
     */
    public String runMainAndReadStdOut(String command, String[] args, String commandDir) throws BallerinaTestException {
        return runMainAndReadStdOut(command, args, new HashMap<>(), commandDir, false);
    }

    /**
     * Executing the sh or bat file to start the server and returns the logs printed to stdout.
     *
     * @param command       command to run
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param commandDir    where to execute the command
     * @param readErrStream whether to read the error stream or input stream
     * @return logs printed to std out
     * @throws BallerinaTestException if starting services failed or if an error occurs when reading the stdout
     */
    public String runMainAndReadStdOut(String command, String[] args, Map<String, String> envProperties,
                                       String commandDir, boolean readErrStream) throws BallerinaTestException {

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
            ProcessBuilder processBuilder =
                    new ProcessBuilder(cmdArgs).directory(new File(commandDir)).redirectErrorStream(true);
            addJavaAgents(envProperties);
            Map<String, String> env = processBuilder.environment();
            env.putAll(envProperties);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(baos);
            Process process = processBuilder.start();
            StreamGobbler outputGobbler =
                    new StreamGobbler(process.getInputStream(), out);
            outputGobbler.start();
            process.waitFor();
            outputGobbler.join();
            String output = baos.toString();
            if (output.endsWith("\n")) {
                output = output.substring(0, output.length() - 1);
            }
            return output;
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
