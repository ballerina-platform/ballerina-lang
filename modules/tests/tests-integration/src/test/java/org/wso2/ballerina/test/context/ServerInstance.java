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
package org.wso2.ballerina.test.context;

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
import java.util.stream.Stream;

/**
 * This class hold the server information and manage the a server instance
 */
public class ServerInstance implements Server {
    private static final Logger log = LoggerFactory.getLogger(ServerInstance.class);
    private String serverHome;
    private String serverDistribution;
    private String[] serviceFiles;
    private Process process;
    private ServerLogReader serverInfoLogReader;
    private ServerLogReader serverErrorLogReader;
    private boolean isServerRunning;

    public ServerInstance(String serverDistributionPath) {
        this.serverDistribution = serverDistributionPath;
    }

    /**
     * Start a server instance y extracting a server zip distribution
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        Utils.checkPortAvailability(Constant.DEFAULT_HTTP_PORT);
        if (serverHome == null) {
            serverHome = setUpServerHome(serverDistribution);
            log.info("Server Home " + serverHome);
            configServer();
        }
        if (serviceFiles == null | serviceFiles.length == 0) {
            throw new IllegalArgumentException("No Service files are available to deploy.");
        }
        log.info("Starting server..");
        startProcess(serviceFiles);
        serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
        serverInfoLogReader.start();
        serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());
        serverErrorLogReader.start();
        log.info("Waiting for port " + Constant.DEFAULT_HTTP_PORT + " to open");
        Utils.waitForPort(Constant.DEFAULT_HTTP_PORT, 1000 * 60 * 2, false, "localhost");
        log.info("Server Started Successfully.");
        isServerRunning = true;
    }

    /**
     * Stop the server instance which is started by start method
     * @throws InterruptedException
     */
    @Override
    public void stop() throws InterruptedException {
        log.info("Stopping server..");
        if (process != null) {
            String pid;
            try {
                pid = getServerPID();
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    Runtime.getRuntime().exec("TASKKILL /PID " + pid);
                } else {
                    Runtime.getRuntime().exec("kill -9 " + pid);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            process.destroy();
            serverInfoLogReader.stop();
            serverErrorLogReader.stop();
            process = null;
            log.info("Server Stopped Successfully");
        }
    }

    /**
     * Restart the server instance
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
     * Checking whether server instance is up and running
     * @return true if the server is up and running
     */
    @Override
    public boolean isRunning() {
        return isServerRunning;
    }

    /**
     * setting the list of service files to be deployed while server startup
     * @param serviceFiles list of service files
     */
    public void setServiceFiles(String[] serviceFiles) {
        this.serviceFiles = serviceFiles;
    }

    /**
     * to change the server configuration if required. This method can be overriding when initialising
     * the object of this class
     */
    protected void configServer() {
    }

    /**
     * Return server home path
     * @return absolute path of the server location
     */
    public String getServerHome() {
        return serverHome;
    }

    /**
     *  Return the service URL
     * @param servicePath - http url of the given service
     * @return
     */
    public String getServiceURLHttp(String servicePath) {
        return "http://localhost:" + Constant.DEFAULT_HTTP_PORT + "/" + servicePath;
    }

    /**
     * Unzip carbon zip file and return the carbon home. Based on the coverage configuration
     * in automation.xml
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
     * Executing the sh or bat file to start the server
     * @param args - command line arguments to pass when executing the sh or bat file
     * @throws IOException
     */

    private void startProcess(String[] args) throws IOException {
        String scriptName = Constant.BALLERINA_SERVER_SCRIPT_NAME;
        String[] cmdArray;
        File commandDir = new File(serverHome);
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            commandDir = new File(serverHome + File.separator + "bin");
            cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat"};
            String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                    .toArray(String[]::new);
            process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);

        } else {
            cmdArray = new String[]{"sh", "bin/" + scriptName};
            String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                    .toArray(String[]::new);
            process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);
        }
    }

    /**
     * reading the server process id from the carbon.pid file
     * @return process id
     * @throws IOException
     */
    private String getServerPID() throws IOException {
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        String pid;
        try {
            //reading the pid form  carbon.pid file in server home dir
            fileReader = new FileReader(serverHome + File.separator + Constant.SERVER_PID_FILE_NAME);
            bufferedReader = new BufferedReader(fileReader);
            pid = bufferedReader.readLine();
            return pid;
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
