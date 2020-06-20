/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.filepath;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.values.ErrorValue;


/**
 * A utility class for OS Path related tasks.
 *
 * @since 0.995.0
 */
public class Utils {

    private static final String UNKNOWN_MESSAGE = "Unknown Error";


    /**
     * Returns error record for input reason and details. This utility to construct error struct from the reason and
     * message description.
     *
     * @param reason  Valid error reason. If the reason is null, "{ballerina/filepath}GenericError" is set as the reason
     *                by default
     * @param details Description of the error message. If the message is null, "Unknown Error" is set to message by
     *                default.
     * @return Ballerina error object.
     */
    public static ErrorValue getPathError(String reason, String details) {
        if (reason == null) {
            reason = Constants.GENERIC_ERROR;
        }
        if (details == null) {
            details = UNKNOWN_MESSAGE;
        }
        return BallerinaErrors.createDistinctError(reason, Constants.PACKAGE_ID, details);
    }

    private Utils() {
    }
}
