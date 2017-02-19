/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * This class hold the server information and manage the a server instance.
 */
public class ServerInstance implements Server {
    private static final Logger log = LoggerFactory.getLogger(ServerInstance.class);
    private String serverHome;
    private String serverDistribution;
    private String[] args;
    private Process process;
    private ServerLogReader serverInfoLogReader;
    private ServerLogReader serverErrorLogReader;
    private boolean isServerRunning;
    private int httpServerPort = Constant.DEFAULT_HTTP_PORT;

    public ServerInstance(String serverDistributionPath) {
        this.serverDistribution = serverDistributionPath;
    }

    public ServerInstance(String serverDistributionPath, int serverHttpPort) {
        this.serverDistribution = serverDistributionPath;
        this.httpServerPort = serverHttpPort;
    }

    /**
     * Start a server instance y extracting a server zip distribution.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        Utils.checkPortAvailability(httpServerPort);
        if (serverHome == null) {
            serverHome = setUpServerHome(serverDistribution);
            log.info("Server Home " + serverHome);
            configServer();
        }
        if (args == null | args.length == 0) {
            throw new IllegalArgumentException("No Argument provided for server startup.");
        }
        log.info("Starting server..");
        startProcess(args);
        serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
        serverInfoLogReader.start();
        serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());
        serverErrorLogReader.start();
        log.info("Waiting for port " + httpServerPort + " to open");
        Utils.waitForPort(httpServerPort, 1000 * 60 * 2, false, "localhost");
        log.info("Server Started Successfully.");
        isServerRunning = true;
    }

    /**
     * Stop the server instance which is started by start method.
     *
     * @throws InterruptedException
     */
    @Override
    public void stop() throws InterruptedException {
        log.info("Stopping server..");
        if (process != null) {
            String pid;
            try {
                pid = getServerPID();
                if (Utils.getOSName().toLowerCase().contains("windows")) {
                    Process killServer = Runtime.getRuntime().exec("TASKKILL -F /PID " + pid);
                    log.info(readProcessInputStream(killServer.getInputStream()));
                    killServer.waitFor(15, TimeUnit.SECONDS);
                    killServer.destroy();
                } else {
                    Process killServer = Runtime.getRuntime().exec("kill -9 " + pid);
                    killServer.waitFor(15, TimeUnit.SECONDS);
                    killServer.destroy();
                }
            } catch (IOException e) {
                log.error("Error while getting the server process id", e);
            }
            process.destroy();
            serverInfoLogReader.stop();
            serverErrorLogReader.stop();
            process = null;
            //wait until port to close
            Utils.waitForPortToClosed(httpServerPort, 30000);
            log.info("Server Stopped Successfully");
        }
    }

    /**
     * Restart the server instance.
     *
     * @throws Exception
     */
    @Override
    public void restart() throws Exception {
        log.info("Restarting Server...");
        stop();
        start();
        log.info("Server Restarted Successfully");
    }

    /**
     * Checking whether server instance is up and running.
     *
     * @return true if the server is up and running
     */
    @Override
    public boolean isRunning() {
        return isServerRunning;
    }

    /**
     * setting the list of command line argument while server startup.
     *
     * @param args list of service files
     */
    public void setArguments(String[] args) {
        this.args = args;
    }

    /**
     * to change the server configuration if required. This method can be overriding when initialising
     * the object of this class.
     */
    protected void configServer() throws Exception {
    }

    /**
     * Return server home path.
     *
     * @return absolute path of the server location
     */
    public String getServerHome() {
        return serverHome;
    }

    /**
     * Return the service URL.
     *
     * @param servicePath - http url of the given service
     * @return
     */
    public String getServiceURLHttp(String servicePath) {
        return "http://localhost:" + httpServerPort + "/" + servicePath;
    }

    /**
     * Unzip carbon zip file and return the carbon home. Based on the coverage configuration
     * in automation.xml.
     * This method will inject jacoco agent to the carbon server startup scripts.
     *
     * @param serverZipFile - Carbon zip file, which should be specified in test module pom
     * @return - carbonHome - carbon home
     * @throws IOException - If pack extraction fails
     */
    private String setUpServerHome(String serverZipFile)
            throws IOException {
        if (process != null) { // An instance of the server is running
            return serverHome;
        }
        int indexOfZip = serverZipFile.lastIndexOf(".zip");
        if (indexOfZip == -1) {
            throw new IllegalArgumentException(serverZipFile + " is not a zip file");
        }
        String fileSeparator = (File.separator.equals("\\")) ? "\\" : "/";
        if (fileSeparator.equals("\\")) {
            serverZipFile = serverZipFile.replace("/", "\\");
        }
        String extractedCarbonDir =
                serverZipFile.substring(serverZipFile.lastIndexOf(fileSeparator) + 1,
                                        indexOfZip);
        String extractDir = "ballerinatmp" + System.currentTimeMillis();
        String baseDir = (System.getProperty(Constant.SYSTEM_PROP_BASE_DIR, ".")) + File.separator + "target";
        log.info("Extracting ballerina zip file.. ");

        Utils.extractFile(serverZipFile, baseDir + File.separator + extractDir);
        String serverExtractedPath = new File(baseDir).getAbsolutePath() + File.separator
                                     + extractDir + File.separator +
                                     extractedCarbonDir;
        return serverExtractedPath;
    }

    /**
     * Executing the sh or bat file to start the server.
     *
     * @param args - command line arguments to pass when executing the sh or bat file
     * @throws IOException
     */

    private void startProcess(String[] args) throws IOException {
        String scriptName = Constant.BALLERINA_SERVER_SCRIPT_NAME;
        String[] cmdArray;
        File commandDir = new File(serverHome);
        if (Utils.getOSName().toLowerCase().contains("windows")) {
            commandDir = new File(serverHome + File.separator + "bin");
            cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat" , "run", "service"};
            String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                    .toArray(String[]::new);
            process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);

        } else {
            cmdArray = new String[]{"bash", "bin/" + scriptName, "run", "service"};
            String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                    .toArray(String[]::new);
            process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);
        }
    }

    /**
     * reading the server process id.
     *
     * @return process id
     * @throws IOException
     */
    private String getServerPID() throws IOException {
        String pid = null;
        if (Utils.getOSName().toLowerCase().contains("windows")) {
            //reading the process id from netstat
            Process tmp = Runtime.getRuntime().exec("netstat -a -n -o");
            String outPut = readProcessInputStream(tmp.getInputStream());
            String[] lines = outPut.split("\r\n");
            for (String line : lines) {
                String[] column = line.trim().split("\\s+");
                if (column != null && column.length < 5) {
                    continue;
                }
                if (column[1].contains(":" + httpServerPort) && column[3].contains("LISTENING")) {
                    log.info(line);
                    pid = column[4];
                    break;
                }
            }
            tmp.destroy();
        } else {
            BufferedReader bufferedReader = null;
            FileReader fileReader = null;

            try {
                //reading the pid form  carbon.pid file in server home dir
                fileReader = new FileReader(serverHome + File.separator + Constant.SERVER_PID_FILE_NAME);
                bufferedReader = new BufferedReader(fileReader);
                pid = bufferedReader.readLine();
            } finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }

        }
        log.info("Server process id in " + Utils.getOSName() + " : " + pid);
        return pid;
    }

    /**
     * Reading output from input stream.
     *
     * @param inputStream input steam of a process
     * @return the output string generated by java process
     */
    private String readProcessInputStream(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            inputStreamReader = new InputStreamReader(inputStream, Charset.defaultCharset());
            bufferedReader = new BufferedReader(inputStreamReader);
            int x;
            while ((x = bufferedReader.read()) != -1) {
                stringBuilder.append((char) x);
            }
        } catch (Exception ex) {
            log.error("Error reading process id", ex);
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStream.close();
                    inputStreamReader.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing stream: " + e.getMessage(), e);
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing stream: " + e.getMessage(), e);
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * This class will read the log messages of the server started by this listener.
     */
    private class ServerLogReader implements Runnable {
        private final Logger log = LoggerFactory.getLogger(ServerLogReader.class);
        private String streamType;
        private InputStream inputStream;
        private static final String STREAM_TYPE_IN = "inputStream";
        private static final String STREAM_TYPE_ERROR = "errorStream";
        private Thread thread;
        private volatile boolean running = true;

        public ServerLogReader(String name, InputStream is) {
            this.streamType = name;
            this.inputStream = is;
        }

        public void start() {
            thread = new Thread(this);
            thread.start();
        }

        public void stop() {
            running = false;
        }

        public void run() {
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                inputStreamReader = new InputStreamReader(inputStream, Charset.defaultCharset());
                bufferedReader = new BufferedReader(inputStreamReader);
                while (running) {
                    if (bufferedReader.ready()) {
                        String s = bufferedReader.readLine();
                        if (s == null) {
                            break;
                        }
                        if (STREAM_TYPE_IN.equals(streamType)) {
                            log.info(s);
                        } else if (STREAM_TYPE_ERROR.equals(streamType)) {
                            log.error(s);
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("Problem reading the [" + streamType + "] due to: " + ex.getMessage(), ex);
            } finally {
                if (inputStreamReader != null) {
                    try {
                        inputStream.close();
                        inputStreamReader.close();
                    } catch (IOException e) {
                        log.error("Error occurred while closing the server log stream: " + e.getMessage(), e);
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        log.error("Error occurred while closing the server log stream: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
}
