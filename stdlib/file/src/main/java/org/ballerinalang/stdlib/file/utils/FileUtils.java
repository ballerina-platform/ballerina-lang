/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.file.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_PACKAGE;

/**
 * Utils class for file module.
 *
 * @since 0.985.0
 */
public class FileUtils {

    private static final String FILE_ERROR_CODE = "{ballerina/file}FileError";
    private static final String FILE_ERROR = "FileError";

    /**
     * Creates an error message.
     *
     * @param context context which is invoked.
     * @param errMsg  the cause for the error.
     * @return an error which will be propagated to ballerina user.
     */
    public static BError createError(Context context, String errMsg) {
        BMap<String, BValue> errorRecord = BLangConnectorSPIUtil.createBStruct(context, FILE_PACKAGE, FILE_ERROR);
        errorRecord.put("message", new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, FILE_ERROR_CODE, errorRecord);
    }

    public static ErrorValue createError(String errMsg) {
        return BallerinaErrors.createError(FILE_ERROR_CODE, errMsg);
    }
}
