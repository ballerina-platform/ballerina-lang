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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

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
     * @param context Represent ballerina context
     * @param msg     Error message in string form
     * @return Ballerina struct with compression error
     */
    public static BError createCompressionError(Context context, String msg) {
        BMap<String, BValue> compressionErrorRecord = createCompressionErrorRecord(context);
        compressionErrorRecord.put(COMPRESSION_ERROR_MESSAGE, new BString(msg));
        return BLangVMErrors.createError(context, true, BTypes.typeError,
                                         COMPRESSION_ERROR_CODE, compressionErrorRecord);
    }

    private static BMap<String, BValue> createCompressionErrorRecord(Context context) {
        return BLangConnectorSPIUtil.createBStruct(context, PROTOCOL_PACKAGE_COMPRESSION, COMPRESSION_ERROR_RECORD);
    }
}
