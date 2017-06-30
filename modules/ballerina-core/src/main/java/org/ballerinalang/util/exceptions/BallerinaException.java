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

import org.ballerinalang.bre.Context;

/**
 * This is the runtime exception occurs at executing ballerina code.
 *
 * @since 0.8.0
 */
public class BallerinaException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private transient Context context;

    public BallerinaException() {
        super();
    }

    /**
     * Constructs a new {@link BallerinaException} with the specified detail message.
     *
     * @param message Error Message
     */
    public BallerinaException(String message) {
        super(message);
    }




    /**
     * Constructs a new {@link BallerinaException} with ballerina context.
     *
     * @param message Error message
     * @param context Ballerina context
     */
    public BallerinaException(String message, Context context) {
        super(message);
        this.context = context;
    }


    /**
     * Constructs a new {@link BallerinaException} with the specified detail message and cause.
     *
     * @param message Error message
     * @param cause   Cause
     */
    public BallerinaException(String message, Throwable cause) {
        super(message, cause);
    }


    /**
     * Constructs a new {@link BallerinaException} with the specified detail message, cause and ballerina context.
     *
     * @param message Error message
     * @param cause   Cause
     * @param context Ballerina context
     */
    public BallerinaException(String message, Throwable cause, Context context) {
        super(message, cause);
        this.context = context;
    }


    /**
     * Constructs a new {@link BallerinaException} with the cause.
     *
     * @param cause Throwable to wrap by a ballerina exception
     */
    public BallerinaException(Throwable cause) {
        super(cause);
    }

    public BallerinaException(Context stack) {
        this.context = stack;
    }

    public Context getContext() {
        return this.context;
    }
}
