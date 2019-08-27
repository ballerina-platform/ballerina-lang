/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.stdlib.internal.compression;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;

/**
 * Util class for compression related operations.
 */
public class CompressionUtils {
    private static final String PROTOCOL_PACKAGE_COMPRESSION = "ballerina/internal";
    private static final String COMPRESSION_ERROR_CODE = "{ballerina/internal}CompressionError";
    private static final String COMPRESSION_ERROR_RECORD = "CompressionError";
    private static final String COMPRESSION_ERROR_MESSAGE = "message";

    /**
     * Get compression error as a ballerina struct.
     *
     * @param msg     Error message in string form
     * @return ErrorValue struct with compression error
     */
    public static ErrorValue createCompressionError(String msg) {
        MapValue<String, Object> compressionErrorRecord = BallerinaValues.createRecordValue(
                PROTOCOL_PACKAGE_COMPRESSION, COMPRESSION_ERROR_RECORD);
        compressionErrorRecord.put(COMPRESSION_ERROR_MESSAGE, msg);
        return BallerinaErrors.createError(
                COMPRESSION_ERROR_CODE, compressionErrorRecord);
    }
}
