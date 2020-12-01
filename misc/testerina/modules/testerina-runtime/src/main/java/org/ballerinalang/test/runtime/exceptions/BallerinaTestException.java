/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.test.runtime.exceptions;

/**
 * This is the runtime exception occurs when executing ballerina tests.
 *
 * @since 2.0.0
 */
public class BallerinaTestException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String detail = null;

    public BallerinaTestException() {
        super();
    }

    /**
     * Constructs a new {@link BallerinaTestException} with the specified error reason message.
     *
     * @param reason Error Reason
     */
    public BallerinaTestException(String reason) {
        super(reason);
    }

    /**
     * Constructs a new {@link BallerinaTestException} with the specified error reason and detail message.
     *
     * @param reason Error Reason
     * @param detail Error Detail Entry
     */
    public BallerinaTestException(String reason, String detail) {
        super(reason);
        this.detail = detail;
    }

    /**
     * Constructs a new {@link BallerinaTestException} with the specified detail message and cause.
     *
     * @param message Error message
     * @param cause   Cause
     */
    public BallerinaTestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link BallerinaTestException} with the cause.
     *
     * @param cause Throwable to wrap by a ballerina exception
     */
    public BallerinaTestException(Throwable cause) {
        super(cause);
    }

    public String getDetail() {
        return detail;
    }
}
