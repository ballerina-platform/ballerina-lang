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

package org.ballerinalang.stdlib.encoding;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.values.ErrorValue;

/**
 * Utility functions relevant to encoding operations.
 *
 * @since 0.990.3
 */
public class EncodingUtil {

    // Error code for encoding error
    public static final String ENCODING_ERROR_CODE = "{ballerina/encoding}Error";

    private EncodingUtil() {

    }

    /**
     * Create encoding error.
     *
     * @param errMsg Error description
     * @return conversion error
     */
    public static ErrorValue createError(String errMsg) {
        return BallerinaErrors.createError(ENCODING_ERROR_CODE, errMsg);
    }
}
