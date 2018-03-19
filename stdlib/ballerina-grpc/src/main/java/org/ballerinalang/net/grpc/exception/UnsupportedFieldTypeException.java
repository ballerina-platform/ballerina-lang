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
package org.ballerinalang.net.grpc.exception;

/**
 * Thrown to indicate that the requested field type is not supported.
 *
 * @since 1.0.0
 */
public class UnsupportedFieldTypeException extends RuntimeException {

    /**
     * Constructs an UnsupportedFieldTypeException with the specified detail message.
     *
     * @param message the detail message
     */
    public UnsupportedFieldTypeException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param  message the detail message
     * @param  cause the cause
     */
    public UnsupportedFieldTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param  cause the cause
     */
    public UnsupportedFieldTypeException(Throwable cause) {
        super(cause);
    }
}
