/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.runtime.exceptions;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;

/**
 * Resource not found exception, thrown when a matching resource is not found in a service.
 *
 * @since 0.8.0
 */
public class ResourceNotFoundException extends BallerinaException {

    /**
     * Constructs a new {@link ResourceNotFoundException} with the specified detail message.
     *
     * @param message Error Message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link ResourceNotFoundException} with the specified detail message and cause.
     *
     * @param message Error message
     * @param cause   Cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link ResourceNotFoundException} with ballerina context.
     *
     * @param message Error message
     * @param context Ballerina context
     */
    public ResourceNotFoundException(String message, Context context) {
        super(message, context);
    }
}
