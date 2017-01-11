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

/**
 * This class hold the server information and manage the a server instance
 */
package org.wso2.ballerina.test.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ServerInstance implements Server {
    private static final Logger log = LoggerFactory.getLogger(ServerInstance.class);
    protected String serverHome;
    protected String serverDistribution;
    private Process process;
    private ServerLogReader inputStreamReader;
    private boolean isServerRunning;

    public ServerInstance(String serverDistributionPath) {
        this.serverDistribution = serverDistributionPath;

    }

    @Override
    public void start() throws Exception {
        checkPortAvailability(9090);
        serverHome = setUpServerHome(serverDistribution);
        configServer();
        String[] bals = {serverHome + "/samples/passthroughService/passthroughService.bal"};
        startProcess(bals);
        waitForPort(9090, 1000 * 60 * 1, false, "localhost");
        isServerRunning = true;

    }

    @Override
    public void stop() throws InterruptedException {
        if (process != null) {
            //todo - destroying the process does not stop the server.
            process.destroyForcibly();
            inputStreamReader.stop();
        }
    }

    @Override
    public void restart() throws Exception {
        throw new Exception("Not implemented yet");
    }

    @Override
    public boolean isRunning() {
        return isServerRunning;
    }

    protected void configServer() {

    }

    public String getServerDistribution() {
        return serverDistribution;
    }

    public void setServerDistribution(String serverDistribution) {
        this.serverDistribution = serverDistribution;
    }

    public String getServerHome() {
        return serverHome;
    }

    public void setServerHome(String serverHome) {
        this.serverHome = serverHome;
    }

    private void checkPortAvailability(int port) throws Exception {

        //check whether http port is already occupied
        if (isPortOpen(port)) {
            throw new Exception("Unable to start carbon server on port " +
                                (port) + " : Port already in use");
        }
    }

    /**
     * Check whether the provided <code>port</code> is open
     *
     * @param port The port that needs to be checked
     * @return true if the <code>port</code> is open & false otherwise
     */
    public static boolean isPortOpen(int port) {
        Socket socket = null;
        boolean isPortOpen = false;
        try {
            InetAddress address = InetAddress.getLocalHost();
            socket = new Socket(address, port);
            isPortOpen = socket.isConnected();
            if (isPortOpen) {
                log.info("Successfully connected to the server on port " + port);
            }
        } catch (IOException e) {
            log.info("Port " + port + " is closed and available for use");
            isPortOpen = false;
        } finally {
            try {
                if ((socket != null) && (socket.isConnected())) {
                    socket.close();
                }
            } catch (IOException e) {
                log.error("Can not close the socket with is used to check the server status ", e);
            }
        }
        return isPortOpen;
    }

    private void extractZip() {
        if (serverHome == null) {
            if (serverDistribution == null) {
                throw new IllegalArgumentException("carbon zip file location lot found");
            }
        }

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
    public String setUpServerHome(String serverZipFile)
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
        String baseDir = (System.getProperty("basedir", ".")) + File.separator + "target";
        log.info("Extracting ballerina zip file.. ");

        extractFile(serverZipFile, baseDir + File.separator + extractDir);
        String serverExtractedPath = new File(baseDir).getAbsolutePath() + File.separator
                                     + extractDir + File.separator +
                                     extractedCarbonDir;


        return serverExtractedPath;
    }

    public void extractFile(String sourceFilePath, String extractedDir) throws IOException {
        FileOutputStream fileoutputstream = null;

        String fileDestination = extractedDir + File.separator;
        byte[] buf = new byte[1024];
        ZipInputStream zipinputstream = null;
        ZipEntry zipentry;
        try {
            zipinputstream = new ZipInputStream(new FileInputStream(sourceFilePath));

            zipentry = zipinputstream.getNextEntry();

            while (zipentry != null) {
                //for each entry to be extracted
                String entryName = fileDestination + zipentry.getName();
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);
                int n;

                File newFile = new File(entryName);
                boolean fileCreated = false;
                if (zipentry.isDirectory()) {
                    if (!newFile.exists()) {
                        fileCreated = newFile.mkdirs();
                    }
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                } else {
                    File resourceFile =
                            new File(entryName.substring(0, entryName.lastIndexOf(File.separator)));
                    if (!resourceFile.exists()) {
                        if (!resourceFile.mkdirs()) {
                            break;
                        }
                    }
                }

                fileoutputstream = new FileOutputStream(entryName);

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }

                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }
            zipinputstream.close();
        } catch (IOException e) {
            log.error("Error on archive extraction ", e);
            throw new IOException("Error on archive extraction ", e);

        } finally {
            if (fileoutputstream != null) {
                fileoutputstream.close();
            }
            if (zipinputstream != null) {
                zipinputstream.close();
            }
        }
    }

    private void startProcess(String[] args) throws IOException {
        log.info("Starting server............. ");
        String scriptName = "ballerinaserver";
        String[] cmdArray;

        File commandDir = new File(serverHome);
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            commandDir = new File(serverHome + File.separator + "bin");
            cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat"};
            String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                    .toArray(String[]::new);
            process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);

        } else {
            cmdArray = new String[]{"sh", "bin/" + scriptName + ".sh"};
            String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                    .toArray(String[]::new);
            process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);
        }
        inputStreamReader = new ServerLogReader("inputStream", process.getInputStream());
        inputStreamReader.start();
    }

    /**
     * @param port    The port that needs to be checked
     * @param timeout The timeout waiting for the port to open
     * @param verbose if verbose is set to true,
     * @throws RuntimeException if the port is not opened within the timeout
     */
    public static void waitForPort(int port, long timeout, boolean verbose, String hostName)
            throws RuntimeException {
        long startTime = System.currentTimeMillis();
        boolean isPortOpen = false;
        while (!isPortOpen && (System.currentTimeMillis() - startTime) < timeout) {
            Socket socket = null;
            try {
                InetAddress address = InetAddress.getByName(hostName);
                socket = new Socket(address, port);
                isPortOpen = socket.isConnected();
                if (isPortOpen) {
                    if (verbose) {
                        log.info("Successfully connected to the server on port " + port);
                    }
                    return;
                }
            } catch (IOException e) {
                if (verbose) {
                    log.info("Waiting until server starts on port " + port);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            } finally {
                try {
                    if ((socket != null) && (socket.isConnected())) {
                        socket.close();
                    }
                } catch (IOException e) {
                    log.error("Can not close the socket with is used to check the server status ",
                              e);
                }
            }
        }
        throw new RuntimeException("Port " + port + " is not open");
    }

    private class ServerLogReader implements Runnable {
        private String streamType;
        private InputStream inputStream;
        private StringBuilder stringBuilder;
        private static final String STREAM_TYPE_IN = "inputStream";
        private static final String STREAM_TYPE_ERROR = "errorStream";
        private Thread thread;
        private volatile boolean running = true;

        public ServerLogReader(String name, InputStream is) {
            this.streamType = name;
            this.inputStream = is;
            this.stringBuilder = new StringBuilder();
        }

        public void start() {
            thread = new Thread(this);
            thread.start();
        }

        public void stop() {
            running = false;
        }

        public void run() {
            log.info("*********");
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                inputStreamReader = new InputStreamReader(inputStream, Charset.defaultCharset());
                bufferedReader = new BufferedReader(inputStreamReader);
                while (running) {
                    if (bufferedReader.ready()) {
                        String s = bufferedReader.readLine();
                        stringBuilder.setLength(0);
                        if (s == null) {
                            break;
                        }
                        if (STREAM_TYPE_IN.equals(streamType)) {
                            stringBuilder.append(s).append("\n");
                            log.info(s);
                        } else if (STREAM_TYPE_ERROR.equals(streamType)) {
                            stringBuilder.append(s).append("\n");
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
