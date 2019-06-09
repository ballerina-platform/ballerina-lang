/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.system.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;

import static org.ballerinalang.stdlib.system.utils.SystemConstants.FILE_INFO_IS_DIR_FIELD;
import static org.ballerinalang.stdlib.system.utils.SystemConstants.FILE_INFO_MODIFIED_TIME_FIELD;
import static org.ballerinalang.stdlib.system.utils.SystemConstants.FILE_INFO_NAME_FIELD;
import static org.ballerinalang.stdlib.system.utils.SystemConstants.FILE_INFO_SIZE_FIELD;
import static org.ballerinalang.stdlib.system.utils.SystemConstants.FILE_INFO_TYPE;
import static org.ballerinalang.stdlib.system.utils.SystemConstants.PACKAGE_PATH;
import static org.ballerinalang.stdlib.time.util.TimeUtils.createTimeStruct;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeStructInfo;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeZoneStructInfo;

/**
 * @since 0.94.1
 */
public class SystemUtils {

    /**
     * Returns the system property which corresponds to the given key.
     *
     * @param key system property key
     * @return system property as a {@link BValue} or {@code BTypes.typeString.getZeroValue()} if the property does not
     * exist.
     */
    public static BValue getSystemProperty(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return BTypes.typeString.getZeroValue();
        }
        return new BString(value);
    }

    private static final String UNKNOWN_MESSAGE = "Unknown Error";
    private static final String UNKNOWN_REASON = "UNKNOWN";

    /**
     * Returns error object for input reason.
     * Error type is generic ballerina error type. This utility to construct error object from message.
     *
     * @param reason    Reason for creating the error object. If the reason is null, "UNKNOWN" sets by
     *                  default.
     * @param error     Java throwable object to capture description of error struct. If throwable object is null,
     *                  "Unknown Error" sets to message by default.
     * @return      Ballerina error object.
     */
    public static BError getBallerinaError(String reason, Throwable error) {
        String errorMsg = error != null && error.getMessage() != null ? error.getMessage() : UNKNOWN_MESSAGE;
        return getBallerinaError(reason, errorMsg);
    }

    /**
     * Returns error object for input reason and details.
     * Error type is generic ballerina error type. This utility to construct error object from message.
     *
     * @param reason    Reason for creating the error object. If the reason is null, value "UNKNOWN" is set by
     *                  default.
     * @param details     Java throwable object to capture description of error struct. If throwable object is null,
     *                  "Unknown Error" is set to message by default.
     * @return      Ballerina error object.
     */
    public static BError getBallerinaError(String reason, String details) {
        BMap<String, BValue> refData = new BMap<>(BTypes.typeError.detailType);
        if (reason != null) {
            reason = SystemConstants.ERROR_REASON_PREFIX + reason;
        } else {
            reason = SystemConstants.ERROR_REASON_PREFIX + UNKNOWN_REASON;
        }
        if (details != null) {
            refData.put("message", new BString(details));
        } else {
            refData.put("message", new BString(UNKNOWN_MESSAGE));
        }
        return new BError(BTypes.typeError, reason, refData);
    }

    public static BMap<String, BValue> createStruct(Context context, String structName) {
        PackageInfo systemPackageInfo = context.getProgramFile()
                .getPackageInfo(PACKAGE_PATH);
        StructureTypeInfo structInfo = systemPackageInfo.getStructInfo(structName);
        BStructureType structType = structInfo.getType();
        return new BMap<>(structType);
    }

    public static BMap<String, BValue> getFileInfo(Context context, File inputFile) throws IOException {
        BMap<String, BValue> lastModifiedInstance;
        FileTime lastModified = Files.getLastModifiedTime(inputFile.toPath());
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(lastModified.toString());
        lastModifiedInstance = createTimeStruct(getTimeZoneStructInfo(context), getTimeStructInfo(context),
                lastModified.toMillis(), zonedDateTime.getZone().toString());


        BMap<String, BValue> fileInfo = SystemUtils.createStruct(context, FILE_INFO_TYPE);
        fileInfo.put(FILE_INFO_NAME_FIELD, new BString(inputFile.getName()));
        fileInfo.put(FILE_INFO_SIZE_FIELD, new BInteger(inputFile.length()));
        fileInfo.put(FILE_INFO_MODIFIED_TIME_FIELD, lastModifiedInstance);
        fileInfo.put(FILE_INFO_IS_DIR_FIELD, new BBoolean(inputFile.isDirectory()));

        return fileInfo;
    }

    /**
     * Returns the system property which corresponds to the given key.
     *
     * @param key system property key
     * @return system property as a {@link String} or {@code BTypes.typeString.getZeroValue()} if the property does not
     * exist.
     */
    //TODO rename method to getSystemProperty once bvm value implemetation is removed
    public static String getProperty(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return org.ballerinalang.jvm.types.BTypes.typeString.getZeroValue();
        }
        return value;
    }
}
