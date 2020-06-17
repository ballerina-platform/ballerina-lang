/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.observe.trace.extension.choreo.client.error;

import java.io.Serializable;

/**
 * Holds information about an error occurred in the client.
 *
 * @since 2.0.0
 */
public class ChoreoError implements Serializable {
    private static final long serialVersionUID = -1L;

    private final Code code;
    private final String description;
    private final Throwable cause;

    public ChoreoError(Code code) {
        this(code, null, null);
    }

    public ChoreoError(Code code, String description, Throwable cause) {
        this.code = code;
        this.description = description;
        this.cause = cause;
    }

    /**
     * Publicly known client error codes.
     */
    public enum Code {
        /**
         * The caller does not have permission to execute the specified operation.
         */
        PERMISSION_DENIED(1),

        /**
         * Internal errors.
         */
        INTERNAL(2),

        /**
         * The service is currently unavailable.
         */
        UNAVAILABLE(3),

        /**
         * User input or config input is not valid.
         */
        VALIDATION_ERROR(4);

        private final int value;

        Code(int value) {
            this.value = value;
        }

        /**
         * The numerical value of the code.
         *
         * @return value
         */
        public int value() {
            return value;
        }
    }

    public String formatThrowableMessage(ChoreoError error) {
        if (error.description == null) {
            return error.code.toString();
        } else {
            return error.code + ": " + error.description;
        }
    }

    public Throwable getCause() {
        return cause;
    }

    /**
     * The error code.
     *
     * @return error code.
     */
    public Code getCode() {
        return code;
    }

    /**
     * A description of the error.
     *
     * @return error description.
     */
    public String getDescription() {
        return description;
    }
}
