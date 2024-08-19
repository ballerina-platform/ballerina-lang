/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package io.ballerina.cli.launcher;

/**
 * Thrown to indicate a non zero exit code from Ballerina executable.
 *
 * @since 2.0.0
 */
public class RuntimePanicException extends RuntimeException {

    private final int exitCode;

    /**
     * Constructs a new {@link RuntimePanicException} with the specified exit code.
     *
     * @param exitCode Exit code from the ballerina program
     */
    public RuntimePanicException(int exitCode) {
        super("Ballerina program execution exited with the exit code " + exitCode);
        this.exitCode = exitCode;
    }

    /**
     * Returns the exit code from the Ballerina program execution.
     *
     * @return int
     */
    public int getExitCode() {
        return this.exitCode;
    }
}
