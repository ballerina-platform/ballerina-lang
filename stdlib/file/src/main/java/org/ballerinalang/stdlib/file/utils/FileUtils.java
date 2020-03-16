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
package org.ballerinalang.stdlib.file.utils;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_INFO_TYPE;
import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_PACKAGE_ID;
import static org.ballerinalang.stdlib.time.util.TimeUtils.createTimeRecord;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeRecord;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeZoneRecord;

/**
 * @since 0.94.1
 */
public class FileUtils {

    private static final String UNKNOWN_MESSAGE = "Unknown Error";

    /**
     * Returns error object for input reason.
     * Error type is generic ballerina error type. This utility to construct error object from message.
     *
     * @param error Reason for creating the error object. If the reason is null, "UNKNOWN" sets by
     *              default.
     * @param ex    Java throwable object to capture description of error struct. If throwable object is null,
     *              "Unknown Error" sets to message by default.
     * @return Ballerina error object.
     */
    public static ErrorValue getBallerinaError(String error, Throwable ex) {
        String errorMsg = error != null && ex.getMessage() != null ? ex.getMessage() : UNKNOWN_MESSAGE;
        return getBallerinaError(error, errorMsg);
    }

    /**
     * Returns error object for input reason and details.
     * Error type is generic ballerina error type. This utility to construct error object from message.
     *
     * @param error   The specific error type.
     * @param details Java throwable object to capture description of error struct. If throwable object is null,
     *                "Unknown Error" is set to message by default.
     * @return Ballerina error object.
     */
    public static ErrorValue getBallerinaError(String error, String details) {
        return BallerinaErrors.createError(error, populateSystemErrorRecord(details));
    }

    private static MapValue populateSystemErrorRecord(String message) {
        Map<String, Object> valueMap = new HashMap<>();
        if (message != null) {
            valueMap.put(FileConstants.ERROR_MESSAGE, message);
        } else {
            valueMap.put(FileConstants.ERROR_MESSAGE, UNKNOWN_MESSAGE);
        }
        return BallerinaValues.createRecordValue(FILE_PACKAGE_ID, FileConstants.ERROR_DETAILS, valueMap);
    }

    public static ObjectValue getFileInfo(File inputFile) throws IOException {
        MapValue<String, Object> lastModifiedInstance;
        FileTime lastModified = Files.getLastModifiedTime(inputFile.toPath());
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(lastModified.toString());
        lastModifiedInstance = createTimeRecord(getTimeZoneRecord(), getTimeRecord(),
                lastModified.toMillis(), zonedDateTime.getZone().toString());
        return BallerinaValues.createObjectValue(FILE_PACKAGE_ID, FILE_INFO_TYPE, inputFile.getName(),
                inputFile.length(), lastModifiedInstance, inputFile.isDirectory(), inputFile.getAbsolutePath());
    }

    public static ObjectValue getFileInfo(ZipEntry entry) throws IOException {
        MapValue<String, Object> lastModifiedInstance;
        FileTime lastModified = entry.getLastModifiedTime();
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(lastModified.toString());
        lastModifiedInstance = createTimeRecord(getTimeZoneRecord(), getTimeRecord(),
                lastModified.toMillis(), zonedDateTime.getZone().toString());
        return BallerinaValues.createObjectValue(FILE_PACKAGE_ID, FILE_INFO_TYPE, entry.getName(),
                entry.getSize(), lastModifiedInstance, entry.isDirectory(), entry.getName());
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

    private FileUtils() {
    }
}
