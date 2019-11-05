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
package org.ballerinalang.jvm.values.api;

import java.io.PrintWriter;

import static org.ballerinalang.jvm.BallerinaErrors.ERROR_PRINT_PREFIX;

/**
 * <p>
 * Represent an error in ballerina.
 * </p>
 *
 * @since 1.1.0
 */
public abstract class BError extends RuntimeException implements BRefValue {

    public BError(String reason) {
        super(reason);
    }

    /**
     * Returns error reason.
     *
     * @return reason string
     */
    public abstract String getReason();

    /**
     * Returns error details.
     *
     * @return detail record
     */
    public abstract Object getDetails();

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

}
