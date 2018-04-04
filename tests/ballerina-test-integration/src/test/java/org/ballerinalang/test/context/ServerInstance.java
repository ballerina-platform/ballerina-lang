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

import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
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
    private int httpServerPort = 9099; //Constant.DEFAULT_HTTP_PORT;
    private ConcurrentHashSet<LogLeecher> tmpLeechers = new ConcurrentHashSet<>();

    /**
     * The parent directory which the ballerina runtime will be extracted to.
     */
    private String extractDir;

    public ServerInstance(String serverDistributionPath) throws BallerinaTestException {
        this.serverDistribution = serverDistributionPath;

        initialize();
    }

    public ServerInstance(String serverDistributionPath, int serverHttpPort) throws BallerinaTestException {
        this.serverDistribution = serverDistributionPath;
        this.httpServerPort = serverHttpPort;

        initialize();
    }

    /**
     * Method to start Ballerina server given the port and bal file.
     *
     * @param port                  In which server starts.
     * @return ballerinaServer      Started server instance.
     * @throws BallerinaTestException
     */
    public static ServerInstance initBallerinaServer(int port) throws BallerinaTestException {
        String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        ServerInstance ballerinaServer = new ServerInstance(serverZipPath, port);

        return ballerinaServer;
    }

    /**
     * Method to start Ballerina server in default port 9092 with given bal file.
     *
     * @return ballerinaServer      Started server instance.
     * @throws BallerinaTestException
     */
    public static ServerInstance initBallerinaServer() throws BallerinaTestException {
        int defaultPort = Constant.DEFAULT_HTTP_PORT;
        String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        ServerInstance ballerinaServer = new ServerInstance(serverZipPath, defaultPort);

        return ballerinaServer;
    }

    public void startBallerinaServer(String balFile) throws BallerinaTestException {
        String[] args = {balFile};
        setArguments(args);

        startServer();
    }

    /**
     * Start the server pointing to the ballerina.conf path
     *
     * @param balFile ballerina file path
     * @param ballerinaConfPath ballerina.conf file path
     * @throws BallerinaTestException if an error occurs while starting the server
     */
    public void startBallerinaServerWithConfigPath(String balFile, String ballerinaConfPath) throws
            BallerinaTestException {
        String balConfigPathArg = "--config ";
        String balConfigPathVal = ballerinaConfPath;
        String[] args = {balConfigPathArg, balConfigPathVal, balFile};
        setArguments(args);

        startServer();
    }

    /**
     * Start a server instance y extracting a server zip distribution.
     *
     * @throws Exception if server start fails
     */
    @Override
    public void startServer() throws BallerinaTestException {

        if (args == null | args.length == 0) {
            throw new IllegalArgumentException("No Argument provided for server startup.");
        }

        Utils.checkPortAvailability(httpServerPort);

        log.info("Starting server..");

        startServer(args);

        serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
        tmpLeechers.forEach(leacher -> serverInfoLogReader.addLeecher(leacher));
        serverInfoLogReader.start();
        serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());
        serverErrorLogReader.start();
        log.info("Waiting for port " + httpServerPort + " to open");
        Utils.waitForPort(httpServerPort, 1000 * 60 * 2, false, "localhost");
        log.info("Server Started Successfully.");
        isServerRunning = true;
    }

    /**
     * Initialize the server instance with properties.
     * @throws BallerinaTestException when an exception is thrown while initializing the server
     */
    private void initialize() throws BallerinaTestException {
        if (serverHome == null) {
            setUpServerHome(serverDistribution);
            log.info("Server Home " + serverHome);
            configServer();
        }
    }

    /**
     * Stop the server instance which is started by start method.
     *
     * @throws BallerinaTestException if service stop fails
     */
    @Override
    public void stopServer() throws BallerinaTestException {
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
                log.error("Error getting process id for the server in port - " + httpServerPort
                        + " error - " + e.getMessage(), e);
                throw new BallerinaTestException("Error while getting the server process id", e);
            } catch (InterruptedException e) {
                log.error("Error stopping the server in port - " + httpServerPort + " error - " + e.getMessage(), e);
                throw new BallerinaTestException("Error waiting for services to stop", e);
            }
            process.destroy();
            serverInfoLogReader.stop();
            serverErrorLogReader.stop();
            process = null;
            //wait until port to close
            Utils.waitForPortToClosed(httpServerPort, 30000);
            log.info("Server Stopped Successfully");

            deleteWorkDir();
        }
    }

    /**
     * Restart the server instance.
     *
     * @throws BallerinaTestException if the services could not be started
     */
    @Override
    public void restartServer() throws BallerinaTestException {
        log.info("Restarting Server...");
        stopServer();
        startServer();
        log.info("Server Restarted Successfully");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runMain(String[] args) throws BallerinaTestException {

        initialize();

        String scriptName = Constant.BALLERINA_SERVER_SCRIPT_NAME;
        String[] cmdArray;
        File commandDir = new File(serverHome);

        Process process;

        try {
            if (Utils.getOSName().toLowerCase().contains("windows")) {
                commandDir = new File(serverHome + File.separator + "bin");
                cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat", "run"};
                String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                        .toArray(String[]::new);
                process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);

            } else {
                cmdArray = new String[]{"bash", "bin/" + scriptName, "run"};
                String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                        .toArray(String[]::new);
                process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);
            }
            serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
            tmpLeechers.forEach(leacher -> serverInfoLogReader.addLeecher(leacher));
            serverInfoLogReader.start();
            serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());
            serverErrorLogReader.start();

            process.waitFor();
            deleteWorkDir();
        } catch (IOException e) {
            throw new BallerinaTestException("Error executing ballerina", e);
        } catch (InterruptedException e) {
            throw new BallerinaTestException("Error waiting for execution to finish", e);
        }
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
    protected void configServer() throws BallerinaTestException {
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
     * @return The service URL
     */
    public String getServiceURLHttp(String servicePath) {
        return "http://localhost:" + httpServerPort + "/" + servicePath;
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
     * Unzip carbon zip file and return the carbon home. Based on the coverage configuration
     * in automation.xml.
     * This method will inject jacoco agent to the carbon server startup scripts.
     *
     * @param serverZipFile - Carbon zip file, which should be specified in test module pom
     * @throws BallerinaTestException if setting up the server fails
     */
    private void setUpServerHome(String serverZipFile)
            throws BallerinaTestException {
        if (process != null) { // An instance of the server is running
            return;
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
        String baseDir = (System.getProperty(Constant.SYSTEM_PROP_BASE_DIR, ".")) + File.separator + "target";

        extractDir = new File(baseDir).getAbsolutePath() + File.separator + "ballerinatmp" + System.currentTimeMillis();

        log.info("Extracting ballerina zip file.. ");

        try {
            Utils.extractFile(serverZipFile, extractDir);

            this.serverHome = extractDir + File.separator + extractedCarbonDir;
        } catch (IOException e) {
            throw new BallerinaTestException("Error extracting server zip file", e);
        }
    }

    /**
     * Executing the sh or bat file to start the server.
     *
     * @param args - command line arguments to pass when executing the sh or bat file
     * @throws BallerinaTestException if starting services failed
     */
    private void startServer(String[] args) throws BallerinaTestException {
        String scriptName = Constant.BALLERINA_SERVER_SCRIPT_NAME;
        String[] cmdArray;
        File commandDir = new File(serverHome);

        try {
            if (Utils.getOSName().toLowerCase().contains("windows")) {
                commandDir = new File(serverHome + File.separator + "bin");
                cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat", "run"};
                String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                        .toArray(String[]::new);
                process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);

            } else {
                cmdArray = new String[]{"bash", "bin/" + scriptName, "run"};
                String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args))
                        .toArray(String[]::new);
                process = Runtime.getRuntime().exec(cmdArgs, null, commandDir);
            }
        } catch (IOException e) {
            throw new BallerinaTestException("Error starting services", e);
        }
    }

    /**
     * reading the server process id.
     *
     * @return process id
     * @throws BallerinaTestException if pid could not be retrieved
     */
    private String getServerPID() throws BallerinaTestException {
        String pid = null;
        if (Utils.getOSName().toLowerCase().contains("windows")) {
            //reading the process id from netstat
            Process tmp;
            try {
                tmp = Runtime.getRuntime().exec("netstat -a -n -o");
            } catch (IOException e) {
                throw new BallerinaTestException("Error retrieving netstat data", e);
            }

            String outPut = readProcessInputStream(tmp.getInputStream());
            String[] lines = outPut.split("\r\n");
            for (String line : lines) {
                String[] column = line.trim().split("\\s+");
                if (column.length < 5) {
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

            //reading the process id from ss
            Process tmp = null;
            try {
                String[] cmd = { "bash", "-c",
                        "ss -ltnp \'sport = :" + httpServerPort + "\' | grep LISTEN | awk \'{print $6}\'" };
                tmp = Runtime.getRuntime().exec(cmd);
                String outPut = readProcessInputStream(tmp.getInputStream());
                log.info("Output of the PID extraction command : " + outPut);
                /* The output of ss command is "users:(("java",pid=24522,fd=161))" in latest ss versions
                 But in older versions the output is users:(("java",23165,116))
                 TODO : Improve this OS dependent logic */
                if (outPut.contains("pid=")) {
                    pid = outPut.split("pid=")[1].split(",")[0];
                } else {
                    pid = outPut.split(",")[1];
                }

            } catch (Exception e) {
                log.warn("Error occurred while extracting the PID with ss " + e.getMessage());
                // If ss command fails trying with lsof. MacOS doesn't have ss by default
                pid = getPidWithLsof(httpServerPort);
            } finally {
                if (tmp != null) {
                    tmp.destroy();
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
     * Delete the working directory with the extracted ballerina instance to cleanup data after execution is complete.
     */
    private void deleteWorkDir() {
        File workDir = new File(extractDir);
        Utils.deleteFolder(workDir);
    }

    /**
     * This method returns the pid of the service which is using the provided port
     * @param httpServerPort port of the service running
     * @return the pid of the service
     * @throws BallerinaTestException
     */
    private String getPidWithLsof(int httpServerPort) throws BallerinaTestException {
        String pid;
        Process tmp = null;
        try {
            String[] cmd = { "bash", "-c", "lsof -Pi tcp:" + httpServerPort + " | grep LISTEN | awk \'{print $2}\'" };
            tmp = Runtime.getRuntime().exec(cmd);
            pid = readProcessInputStream(tmp.getInputStream());

        } catch (Exception err) {
            throw new BallerinaTestException("Error retrieving the PID : ", err);
        } finally {
            if (tmp != null) {
                tmp.destroy();
            }
        }
        return pid;
    }
}
