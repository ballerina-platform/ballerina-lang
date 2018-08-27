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

import java.io.File;
import java.io.IOException;

/**
 * This class hold the server information and manage the a server instance.
 */
public class BalServer {
    private static final Logger log = LoggerFactory.getLogger(BalServer.class);
    private String serverHome;

    /**
     * The parent directory which the ballerina runtime will be extracted to.
     */
    private String extractDir;

    /**
     * This will unzip a new Ballerina server and create a new server location.
     *
     * @throws BallerinaTestException if something fails
     */
    public BalServer() throws BallerinaTestException {
        setUpServerHome();
        configServer();
        log.info("Server Home " + serverHome);
    }

    /**
     * Unzip carbon zip file and return the carbon home. Based on the coverage configuration
     * in automation.xml.
     * This method will inject jacoco agent to the carbon server startup scripts.
     *
     * @throws BallerinaTestException if setting up the server fails
     */
    private void setUpServerHome()
            throws BallerinaTestException {
        String serverZipFile = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);

        int indexOfZip = serverZipFile.lastIndexOf(".zip");
        if (indexOfZip == -1) {
            throw new IllegalArgumentException(serverZipFile + " is not a zip file");
        }
        String fileSeparator = (File.separator.equals("\\")) ? "\\" : "/";
        if (fileSeparator.equals("\\")) {
            serverZipFile = serverZipFile.replace("/", "\\");
        }
        String extractedBalDir = serverZipFile.substring(serverZipFile.lastIndexOf(fileSeparator) + 1, indexOfZip);
        String baseDir = (System.getProperty(Constant.SYSTEM_PROP_BASE_DIR, ".")) + File.separator + "target";

        extractDir = new File(baseDir).getAbsolutePath() +
                File.separator + "ballerinatmp" + System.currentTimeMillis();

        log.info("Extracting ballerina zip file.. ");

        try {
            Utils.extractFile(serverZipFile, extractDir);

            this.serverHome = extractDir + File.separator + extractedBalDir;
        } catch (IOException e) {
            throw new BallerinaTestException("Error extracting server zip file", e);
        }
    }

    /**
     * Clean up this server instance by removing the work directory.
     */
    public void cleanup() {
        deleteWorkDir();
    }

    /**
     * to change the server configuration if required. This method can be overriding when initialising
     * the object of this class.
     *
     * @throws BallerinaTestException if configuring server failed
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
     * Delete the working directory with the extracted ballerina instance to cleanup data after execution is complete.
     */
    private void deleteWorkDir() {
        File workDir = new File(extractDir);
        Utils.deleteFolder(workDir);
    }
}
