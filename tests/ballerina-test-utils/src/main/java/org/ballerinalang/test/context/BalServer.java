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
import java.nio.file.Path;

/**
 * This class hold the server location and manage the a server location.
 *
 * @since 0.982.0
 */
public class BalServer {
    private static final Logger log = LoggerFactory.getLogger(BalServer.class);
    private Path serverHome;

    /**
     * The parent directory which the ballerina runtime will be extracted to.
     */
    private Path extractDir;

    /**
     * This will unzip a new Ballerina server and create a new server location.
     *
     * @throws BallerinaTestException if something fails
     */
    public BalServer() throws BallerinaTestException {
        this(Path.of(System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP)));
    }

    public BalServer(Path serverZipFile) throws BallerinaTestException {
        setUpServerHome(serverZipFile.toAbsolutePath());
        log.info("Server Home " + serverHome);
    }

    /**
     * Unzip carbon zip file and return the carbon home. Based on the coverage configuration
     * in automation.xml.
     * This method will inject jacoco agent to the carbon server startup scripts.
     *
     * @param serverZipFile server zip file location
     * @throws BallerinaTestException if setting up the server fails
     */
    private void setUpServerHome(Path serverZipFile) throws BallerinaTestException {
        if (!serverZipFile.getFileName().toString().endsWith(".zip")) {
            throw new IllegalArgumentException(serverZipFile + " is not a zip file");
        }
        String extractedBalDir = serverZipFile.getFileName().toString().replace(".zip", "");
        String baseDir = System.getProperty("libdir", ".");

        extractDir = Path.of(baseDir, "ballerinatmp" + System.currentTimeMillis());

        log.info("Extracting ballerina zip file.. ");

        try {
            Utils.extractFile(serverZipFile.toFile(), extractDir.toFile());

            this.serverHome = extractDir.resolve(extractedBalDir);
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
     * Return server home path.
     *
     * @return absolute path of the server location
     */
    public Path getServerHome() {
        return serverHome;
    }

    /**
     * Delete the working directory with the extracted ballerina instance to cleanup data after execution is complete.
     */
    private void deleteWorkDir() {
        File workDir = extractDir.toFile();
        Utils.deleteFolder(workDir);
    }
}
