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

import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;

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
     * Package path.
     */
    static final String FILE_PACKAGE_PATH = ORG_NAME + ORG_NAME_SEPARATOR + PACKAGE_NAME;

    static final String FILE_INFO_TYPE = "FileInfo";

    // File error codes
    public static final String INVALID_OPERATION_ERROR = "{ballerina/file}InvalidOperationError";
    public static final String PERMISSION_ERROR = "{ballerina/file}PermissionError";
    public static final String FILE_SYSTEM_ERROR = "{ballerina/file}FileSystemError";
    public static final String FILE_NOT_FOUND_ERROR = "{ballerina/file}FileNotFoundError";
    static final String ERROR_DETAILS = "Detail";
    static final String ERROR_MESSAGE = "message";

    // System constant fields
    public static final int DEFAULT_MAX_DEPTH = -1;

    private FileConstants() {
    }
}
