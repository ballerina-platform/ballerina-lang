/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api.values;

import java.io.PrintWriter;

/**
 * <p>
 * Represent an error in ballerina.
 * </p>
 *
 * @since 1.1.0
 */
public abstract class BError extends RuntimeException implements BValue {

    public static final String ERROR_PRINT_PREFIX = "error: ";

    public static final String CALL_STACK_ELEMENT = "CallStackElement";

    @Deprecated
    public BError(String message) {
        super(message);
    }

    public BError(BString message) {
        super(message.getValue());
    }
    /**
     * Returns error reason.
     *
     * @return reason string
     */
    public abstract BString getErrorMessage();

    /**
     * Returns error details.
     *
     * @return detail record
     */
    public abstract Object getDetails();

    /**
     * Returns error cause.
     *
     * @return error cause
     */
    public abstract BError getCause();

    /**
     * Print error stack trace to the given {@code PrintWriter}.
     *
     * @param printWriter {@code PrintWriter} to be used
     */
    public void printStackTrace(PrintWriter printWriter) {
        printWriter.print(ERROR_PRINT_PREFIX + getPrintableStackTrace());
    }

    /**
     * Returns error stack trace as a string.
     *
     * @return stack trace string
     */
    public abstract String getPrintableStackTrace();

    /**
     * Returns error stack trace as a array.
     *
     * @return ballerina error stacktrace
     */
    public abstract BArray getCallStack();

}
