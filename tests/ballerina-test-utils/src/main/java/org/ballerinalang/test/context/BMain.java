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

import java.util.Map;

/**
 * Interface for test Server implementation.
 *
 * @since 0.982.0
 */
public interface BMain {

    // ********************* Begin bal file run methods. *******************
    /**
     * Run ballerina main function provided .bal file.
     *
     * @param balFile   bal file path
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String balFile) throws BallerinaTestException;

    /**
     * Run ballerina main function provided .bal file.
     *
     * @param balFile   bal file path
     * @param leechers  log leechers to check the log if any
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String balFile, LogLeecher[] leechers) throws BallerinaTestException;

    /**
     * Run ballerina main function provided .bal file.
     *
     * @param balFile   bal file path
     * @param flags     flags to pass
     * @param args      arguments to pass
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String balFile, String[] flags, String[] args) throws BallerinaTestException;

    /**
     * Run ballerina main function provided .bal file.
     *
     * @param balFile   bal file path
     * @param flags     flags to pass
     * @param args      arguments to pass
     * @param leechers  log leechers to check the log if any
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String balFile, String[] flags, String[] args, LogLeecher[] leechers) throws BallerinaTestException;

    /**
     * Run ballerina main function provided .bal file.
     *
     * @param balFile       bal file path
     * @param flags         flags to pass
     * @param args          arguments to pass
     * @param envProperties environment properties
     * @param clientArgs    arguments which program expects
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String balFile, String[] flags, String[] args, Map<String, String> envProperties,
                 String[] clientArgs) throws BallerinaTestException;

    /**
     * Run ballerina main function provided .bal file.
     *
     * @param balFile       bal file path
     * @param flags         flags to pass
     * @param args          arguments to pass
     * @param envProperties environment properties
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String balFile, String[] flags, String[] args, Map<String, String> envProperties,
                 String[] clientArgs, LogLeecher[] leechers) throws BallerinaTestException;
    
    /**
     * Run ballerina main function provided .bal file.
     *
     * @param balFile       bal file path
     * @param flags         flags to pass
     * @param args          arguments to pass
     * @param envProperties environment properties
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @param sourceRoot    source root of the bal file
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String balFile, String[] flags, String[] args, Map<String, String> envProperties,
                 String[] clientArgs, LogLeecher[] leechers, String sourceRoot) throws BallerinaTestException;
    // ********************* End bal file run methods. *******************

    // ********************* Begin bal package run methods. *******************

    /**
     * Run ballerina main function provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String sourceRoot, String packagePath) throws BallerinaTestException;

    /**
     * Run ballerina main function provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @param leechers      log leechers to check the log if any
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String sourceRoot, String packagePath, LogLeecher[] leechers) throws BallerinaTestException;

    /**
     * Run ballerina main function provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @param flags         flags to pass
     * @param args          arguments to parse
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String sourceRoot, String packagePath, String[] flags, String[] args) throws BallerinaTestException;

    /**
     * Run ballerina main function provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @param flags         flags to pass
     * @param args          arguments to parse
     * @param leechers      log leechers to check the log if any
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String sourceRoot, String packagePath, String[] flags, String[] args,
                 LogLeecher[] leechers) throws BallerinaTestException;

    /**
     * Run ballerina main function provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @param flags         flags to pass
     * @param args          arguments to parse
     * @param envProperties environment properties
     * @param clientArgs    arguments which program expects
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String sourceRoot, String packagePath, String[] flags, String[] args,
                 Map<String, String> envProperties, String[] clientArgs) throws BallerinaTestException;

    /**
     * Run ballerina main function provided bal package.
     *
     * @param sourceRoot    source root directory
     * @param packagePath   package path
     * @param flags         flags to pass
     * @param args          arguments to parse
     * @param envProperties environment properties
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @throws BallerinaTestException if any exception is thrown when running the main function
     */
    void runMain(String sourceRoot, String packagePath,
                 String[] flags, String[] args, Map<String, String> envProperties,
                 String[] clientArgs, LogLeecher[] leechers) throws BallerinaTestException;
    // ********************* End bal package run methods. *******************
}
