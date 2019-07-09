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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;

import static org.ballerinalang.stdlib.system.utils.SystemConstants.FILE_INFO_TYPE;
import static org.ballerinalang.stdlib.system.utils.SystemConstants.SYSTEM_PACKAGE_PATH;
import static org.ballerinalang.stdlib.time.util.TimeUtils.createTimeRecord;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeRecord;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeZoneRecord;

/**
 * @since 0.94.1
 */
public class SystemUtils {

    private static final String UNKNOWN_MESSAGE = "Unknown Error";
    private static final String UNKNOWN_REASON = "UNKNOWN";

    /**
     * Returns error object for input reason.
     * Error type is generic ballerina error type. This utility to construct error object from message.
     *
     * @param reason Reason for creating the error object. If the reason is null, "UNKNOWN" sets by
     *               default.
     * @param error  Java throwable object to capture description of error struct. If throwable object is null,
     *               "Unknown Error" sets to message by default.
     * @return Ballerina error object.
     */
    public static ErrorValue getBallerinaError(String reason, Throwable error) {
        String errorMsg = error != null && error.getMessage() != null ? error.getMessage() : UNKNOWN_MESSAGE;
        return getBallerinaError(reason, errorMsg);
    }

    /**
     * Returns error object for input reason and details.
     * Error type is generic ballerina error type. This utility to construct error object from message.
     *
     * @param reason  Reason for creating the error object. If the reason is null, value "UNKNOWN" is set by
     *                default.
     * @param details Java throwable object to capture description of error struct. If throwable object is null,
     *                "Unknown Error" is set to message by default.
     * @return Ballerina error object.
     */
    public static ErrorValue getBallerinaError(String reason, String details) {
        MapValue<String, Object> refData = new MapValueImpl<>(BTypes.typeError.detailType);
        if (reason != null) {
            reason = SystemConstants.ERROR_REASON_PREFIX + reason;
        } else {
            reason = SystemConstants.ERROR_REASON_PREFIX + UNKNOWN_REASON;
        }
        if (details != null) {
            refData.put("message", details);
        } else {
            refData.put("message", UNKNOWN_MESSAGE);
        }
        return new ErrorValue(BTypes.typeError, reason, refData);
    }

    public static ObjectValue getFileInfo(File inputFile) throws IOException {
        MapValue<String, Object> lastModifiedInstance;
        FileTime lastModified = Files.getLastModifiedTime(inputFile.toPath());
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(lastModified.toString());
        lastModifiedInstance = createTimeRecord(getTimeZoneRecord(), getTimeRecord(),
                lastModified.toMillis(), zonedDateTime.getZone().toString());
        return BallerinaValues.createObjectValue(SYSTEM_PACKAGE_PATH, FILE_INFO_TYPE, inputFile.getName(),
                inputFile.length(), lastModifiedInstance, inputFile.isDirectory());
    }

    /**
     * Returns the system property which corresponds to the given key.
     *
     * @param key system property key
     * @return system property as a {@link String} or {@code BTypes.typeString.getZeroValue()} if the property does not
     * exist.
     */
    public static String getSystemProperty(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return org.ballerinalang.jvm.types.BTypes.typeString.getZeroValue();
        }
        return value;
    }
}
