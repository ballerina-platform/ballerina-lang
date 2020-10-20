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

package org.ballerinalang.stdlib.system.utils;

import io.ballerina.runtime.api.Module;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.util.BLangConstants.ORG_NAME_SEPARATOR;

/**
 * Constants for system package functions.
 *
 * @since 0.995.0
 */
public class SystemConstants {
    /**
     * Organization name.
     */
    public static final String ORG_NAME = "ballerina";

    /**
     * Package name.
     */
    public static final String PACKAGE_NAME = "system";

    /**
     * Package version.
     */
    public static final String PACKAGE_VERSION = "0.6.0";

    /**
     * Package path.
     */
    static final String SYSTEM_PACKAGE_PATH = ORG_NAME + ORG_NAME_SEPARATOR + PACKAGE_NAME;

    static final Module SYSTEM_PACKAGE_ID = new Module(BALLERINA_BUILTIN_PKG_PREFIX, PACKAGE_NAME, PACKAGE_VERSION);

    static final String ERROR_REASON_PREFIX = "{ballerina/system}";

    static final String FILE_INFO_TYPE = "FileInfo";

    static final String PROCESS_TYPE = "Process";

    static final String PROCESS_FIELD = "ProcessField";

    // System error type names
    public static final String PROCESS_EXEC_ERROR = "ProcessExecError";

    // System constant fields
    public static final int DEFAULT_MAX_DEPTH = -1;

    private SystemConstants() {
    }
}
