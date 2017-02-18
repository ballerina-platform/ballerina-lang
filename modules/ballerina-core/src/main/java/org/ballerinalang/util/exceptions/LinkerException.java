/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.util.exceptions;

/**
 * Class to represent errors occur at linking phase.
 */
public class LinkerException extends BallerinaException {
    /**
     * Constructs a new {@link LinkerException} with the specified detail message.
     *
     * @param message Error Message
     */
    public LinkerException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link LinkerException} with the specified detail message and cause.
     *
     * @param message Error message
     * @param cause   Cause
     */
    public LinkerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link LinkerException} with the cause.
     *
     * @param cause
     */
    public LinkerException(Throwable cause) {
        super(cause);
    }
}
