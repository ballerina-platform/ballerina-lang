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

/**
 * Interface for test Server implementation.
 */
public interface Server {

    /**
     * Start ballerina server passing provided .bal files.
     *
     * @throws BallerinaTestException if services start fails
     */
    void startServer() throws BallerinaTestException;

    /**
     * Stops the server started by startServer method.
     *
     * @throws BallerinaTestException if service stop fails
     */
    void stopServer() throws BallerinaTestException;

    /**
     * Stop the server and start it again.
     *
     * @throws BallerinaTestException if restart fails
     */
    void restartServer() throws BallerinaTestException;

    /**
     * Executes main function of ballerina files.
     *
     * @param args Ballerina files to be passed as arguments
     * @param envVariables Environment variables passed
     * @param command Command to be executed
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String[] args, String[] envVariables, String command) throws BallerinaTestException;

    /**
     * Checks if the server is already running.
     *
     * @return True if the server is running
     */
    boolean isRunning();
}
