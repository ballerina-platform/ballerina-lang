/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.plugins.idea;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.annotations.NonNls;

/**
 * Contains constants related to Ballerina plugin.
 */
public class BallerinaConstants {

    private BallerinaConstants() {

    }

    public static final String BALLERINA = "Ballerina";
    public static final String MAIN = "main";
    public static final String PATH = "PATH";
    public static final String MODULE_TYPE_ID = "BALLERINA_MODULE";

    @NonNls
    public static final String BALLERINA_EXECUTABLE_NAME = "ballerina";

    @NonNls
    public static final String BALLERINA_VERSION_FILE_PATH = "bin/version.txt";

    public static final NotificationGroup BALLERINA_NOTIFICATION_GROUP =
            NotificationGroup.balloonGroup("Ballerina plugin notifications");

    public static final NotificationGroup BALLERINA_EXECUTION_NOTIFICATION_GROUP =
            NotificationGroup.toolWindowGroup("Ballerina Execution", ToolWindowId.RUN);

    public static final String BALLERINA_REPOSITORY = "BALLERINA_REPOSITORY";
    public static final String BALLERINA_REPOSITORY_SOURCE_DIRECTORY = "artifacts/src";
    public static final String BALLERINA_LIBRARIES_SERVICE_NAME = "BallerinaLibraries";
    public static final String BALLERINA_LIBRARIES_CONFIG_FILE = "BallerinaLibraries.xml";
    public static final String BALLERINA_MODULE_SESTTINGS_SERVICE_NAME = "Ballerina";

    public static final String BALLERINA_SDK_LIB_DIR = "/bre/lib";
    public static final String BALLERINA_SDK_LIB_FILE_EXTENTION = ".jar";

    public static final String IDEA_CONFIG_DIRECTORY = ".idea";

    public static final String BALLERINA_TEST_FUNCTION_PREFIX = "test";
    public static final String BALLERINA_TEST_FILE_SUFFIX = "_test.bal";
}
