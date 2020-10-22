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

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;

import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_INFO_TYPE;
import static org.ballerinalang.stdlib.file.utils.FileConstants.FILE_PACKAGE_ID;
import static org.ballerinalang.stdlib.time.util.TimeUtils.createTimeRecord;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeRecord;
import static org.ballerinalang.stdlib.time.util.TimeUtils.getTimeZoneRecord;

/**
 * @since 0.94.1
 */
public class FileUtils {

    private static final BString UNKNOWN_MESSAGE = StringUtils.fromString("Unknown Error");

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
    public static BError getBallerinaError(String error, Throwable ex) {
        BString errorMsg = error != null && ex.getMessage() != null ? StringUtils.fromString(ex.getMessage()) :
                UNKNOWN_MESSAGE;
        return getBallerinaError(error, errorMsg);
    }

    /**
     * Returns error object for input reason and details.
     * Error type is generic ballerina error type. This utility to construct error object from message.
     *
     * @param error   The specific error type.
     * @param message Error message. "Unknown Error" is set to message by default.
     * @return Ballerina error object.
     */
    public static BError getBallerinaError(String error, BString message) {
        return ErrorCreator.createDistinctError(error, FILE_PACKAGE_ID, message != null ?
                message : UNKNOWN_MESSAGE);
    }

    public static BObject getFileInfo(File inputFile) throws IOException {
        BMap<BString, Object> lastModifiedInstance;
        FileTime lastModified = Files.getLastModifiedTime(inputFile.toPath());
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(lastModified.toString());
        lastModifiedInstance = createTimeRecord(getTimeZoneRecord(), getTimeRecord(),
                                                lastModified.toMillis(), StringUtils
                                                        .fromString(zonedDateTime.getZone().toString()));
        return ValueCreator.createObjectValue(FILE_PACKAGE_ID, FILE_INFO_TYPE,
                                              StringUtils.fromString(inputFile.getName()), inputFile.length(),
                                              lastModifiedInstance,
                                              inputFile.isDirectory(), StringUtils
                                                       .fromString(inputFile.getAbsolutePath()));
    }


    /**
     * Returns the system property which corresponds to the given key.
     *
     * @param key system property key
     * @return system property as a {@link String} or {@code Types.typeString.getZeroValue()} if the property does not
     * exist.
     */
    public static String getSystemProperty(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return PredefinedTypes.TYPE_STRING.getZeroValue();
        }
        return value;
    }

    private FileUtils() {
    }
}
