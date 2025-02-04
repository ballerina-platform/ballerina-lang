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

import java.nio.file.Path;
import java.util.Map;

/**
 * Interface for test Server implementation.
 *
 * @since 0.982.0
 */
public interface BServer {

    /**
     * Start ballerina server passing provided .bal files.
     *
     * @param balFile   bal file path
     * @throws BallerinaTestException if services start fails
     */
    void startServer(Path balFile) throws BallerinaTestException;

    /**
     * Start ballerina server passing provided .bal files.
     *
     * @param balFile   bal file path
     * @param useBallerinaRunCommand   flag to use ballerina run to execute program.
     * @throws BallerinaTestException if services start fails
     */
    void startServer(Path balFile, boolean useBallerinaRunCommand) throws BallerinaTestException;

    /**
     * Start ballerina server passing provided .bal files.
     *
     * @param balFile   bal file path
     * @param requiredPorts ports required for the server instance
     * @throws BallerinaTestException if services start fails
     */
    void startServer(Path balFile, int[] requiredPorts) throws BallerinaTestException;

    /**
     * Start ballerina server passing provided .bal files.
     *
     * @param balFile   bal file path
     * @param buildArgs     arguments to parse for build
     * @param runtimeArgs   arguments to parse for runtime
     * @param requiredPorts ports required for the server instance
     * @throws BallerinaTestException if services start fails
     */
    void startServer(Path balFile, String[] buildArgs, String[] runtimeArgs, int[] requiredPorts)
            throws BallerinaTestException;

    /**
     * Start ballerina server passing provided .bal files.
     *
     * @param balFile       bal file path
     * @param buildArgs     arguments to parse for build
     * @param runtimeArgs   arguments to parse for runtime
     * @param envProperties environment properties
     * @param requiredPorts ports required for the server instance
     * @throws BallerinaTestException if services start fails
     */
    void startServer(Path balFile, String[] buildArgs, String[] runtimeArgs, Map<String, String> envProperties,
                     int[] requiredPorts) throws BallerinaTestException;

    /**
     * Start ballerina server passing provided .bal files.
     *
     * @param balFile       bal file path
     * @param buildArgs     arguments to parse for build
     * @param runtimeArgs   arguments to parse for runtime
     * @param envProperties environment properties
     * @param requiredPorts ports required for the server instance
     * @param useBallerinaRunCommand   flag to use ballerina run to execute program.
     * @throws BallerinaTestException if services start fails
     */
    void startServer(Path balFile, String[] buildArgs, String[] runtimeArgs, Map<String, String> envProperties,
                     int[] requiredPorts, boolean useBallerinaRunCommand) throws BallerinaTestException;

    /**
     * Start ballerina server passing provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @throws BallerinaTestException if starting fails
     */
    void startServer(Path sourceRoot, String packagePath) throws BallerinaTestException;

    /**
     * Start ballerina server passing provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @param requiredPorts ports required for the server instance
     * @throws BallerinaTestException if starting fails
     */
    void startServer(Path sourceRoot, String packagePath, int[] requiredPorts) throws BallerinaTestException;

    /**
     * Start ballerina server passing provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @param buildArgs     arguments to parse for build
     * @param runtimeArgs   arguments to parse for runtime
     * @param requiredPorts ports required for the server instance
     * @throws BallerinaTestException if starting fails
     */
    void startServer(Path sourceRoot, String packagePath, String[] buildArgs, String[] runtimeArgs,
                     int[] requiredPorts) throws BallerinaTestException;

    /**
     * Start ballerina server passing provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @param buildArgs     arguments to parse for build
     * @param runtimeArgs   arguments to parse for runtime
     * @param envProperties environment properties
     * @param requiredPorts ports required for the server instance
     * @param useBallerinaRunCommand   flag to use ballerina run to execute program.
     * @throws BallerinaTestException if starting fails
     */
    void startServer(Path sourceRoot, String packagePath, String[] buildArgs, String[] runtimeArgs,
                     Map<String, String> envProperties, int[] requiredPorts, boolean useBallerinaRunCommand)
            throws BallerinaTestException;

    /**
     * Start ballerina server passing provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @param buildArgs     arguments to parse for build
     * @param runtimeArgs   arguments to parse for runtime
     * @param envProperties environment properties
     * @param requiredPorts ports required for the server instance
     * @throws BallerinaTestException if starting fails
     */
    void startServer(Path sourceRoot, String packagePath, String[] buildArgs, String[] runtimeArgs,
                     Map<String, String> envProperties, int[] requiredPorts) throws BallerinaTestException;

    /**
     * Stops the server started by startServer method.
     *
     * @throws BallerinaTestException if service stop fails
     */
    void shutdownServer() throws BallerinaTestException;

    /**
     * Forcefully kills the server.
     *
     * @throws BallerinaTestException if service stop fails
     */
    void killServer() throws BallerinaTestException;

}
