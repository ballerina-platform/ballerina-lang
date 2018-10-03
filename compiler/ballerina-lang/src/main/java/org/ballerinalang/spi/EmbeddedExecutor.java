/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.spi;

import org.ballerinalang.util.EmbeddedExecutorError;

import java.util.Optional;

/**
 * This represents the Java SPI interface for the resource runner.
 *
 * @since 0.964
 */
public interface EmbeddedExecutor {
    /**
     * Executes a function of a balx file.
     * @param programArg Path of the balx.
     * @param functionName The name of the function.
     * @param args The arguments for the function.
     * @return Program execution output.
     */
    Optional<EmbeddedExecutorError> executeFunction(String programArg, String functionName, String... args);
    
    /**
     * Executes a service of a balx file.
     * @param balxPath Path of the balx.
     */
    void executeService(String balxPath);
}
