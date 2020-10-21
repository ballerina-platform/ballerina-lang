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
package io.ballerina.runtime.util.exceptions;

/**
 * This is the runtime exception occurs at executing ballerina code at jvm.
 *
 * @since 0.955.0
 */
public class BallerinaException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String detail = null;

    public BallerinaException() {
        super();
    }

    /**
     * Constructs a new {@link BallerinaException} with the specified error reason message.
     *
     * @param reason Error Reason
     */
    public BallerinaException(String reason) {
        super(reason);
    }

    /**
     * Constructs a new {@link BallerinaException} with the specified error reason and detail message.
     *
     * @param reason Error Reason
     * @param detail Error Detail Entry
     */
    public BallerinaException(String reason, String detail) {
        super(reason);
        this.detail = detail;
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
     * Constructs a new {@link BallerinaException} with the cause.
     *
     * @param cause Throwable to wrap by a ballerina exception
     */
    public BallerinaException(Throwable cause) {
        super(cause);
    }

    public String getDetail() {
        return detail;
    }
}
