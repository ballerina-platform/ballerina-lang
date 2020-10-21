/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.util.exceptions;

/**
 * {@code BallerinaConnectorException} This current annotation value types.
 *
 * @since 0.995.0
 */
public class BallerinaConnectorException extends BallerinaException {

    private static final long serialVersionUID = 381055783364464822L;

    /**
     * Constructs a new {@link BallerinaConnectorException} with the specified detail message.
     *
     * @param message Error Message
     */
    public BallerinaConnectorException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link BallerinaConnectorException} with the specified detail message and cause.
     *
     * @param message Error message
     * @param cause   Cause
     */
    public BallerinaConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link BallerinaConnectorException} with the cause.
     *
     * @param cause Throwable to wrap by a ballerina exception
     */
    public BallerinaConnectorException(Throwable cause) {
        super(cause);
    }
}
