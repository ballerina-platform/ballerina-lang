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

package org.ballerinalang.stdlib.file.utils;

import io.ballerina.runtime.api.Module;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Constants for file package functions.
 *
 * @since 0.995.0
 */
public class FileConstants {
    /**
     * Organization name.
     */
    public static final String ORG_NAME = "ballerina";

    /**
     * Package name.
     */
    public static final String PACKAGE_NAME = "file";

    /**
     * Package version.
     */
    public static final String PACKAGE_VERSION = "0.5.0";

    /**
     * Package path.
     */
    public static final Module FILE_PACKAGE_ID = new Module(BALLERINA_BUILTIN_PKG_PREFIX, PACKAGE_NAME,
                                                            PACKAGE_VERSION);
    static final String FILE_INFO_TYPE = "FileInfo";

    // File error type IDs
    public static final String INVALID_OPERATION_ERROR = "InvalidOperationError";
    public static final String PERMISSION_ERROR = "PermissionError";
    public static final String FILE_SYSTEM_ERROR = "FileSystemError";
    public static final String FILE_NOT_FOUND_ERROR = "FileNotFoundError";
    static final String ERROR_DETAILS = "Detail";
    static final String ERROR_MESSAGE = "message";

    // System constant fields
    public static final int DEFAULT_MAX_DEPTH = -1;

    // FileEvent struct field names
    public static final String FILE_EVENT_NAME = "name";

    public static final String FILE_EVENT_OPERATION = "operation";

    private FileConstants() {
    }
}
