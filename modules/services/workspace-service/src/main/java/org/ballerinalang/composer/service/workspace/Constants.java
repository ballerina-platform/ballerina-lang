/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.composer.service.workspace;

/**
 * Constants for the workspace service.
 *
 */
public class Constants {
    /**
     * Arguments for Service Runner
     */
    public static final String CLOUD_MODE_INDICATOR_ARG = "cloudMode";
    public static final String CLOUD_MODE_INDICATOR_ARG_DESC = "Enable Cloud Mode.";

    public static final String FILE_CONTEXT_RESOURCE = "resources";
    public static final String FILE_CONTEXT_RESOURCE_COMPOSER = "composer";
    public static final String FILE_CONTEXT_RESOURCE_COMPOSER_WEB = "web";

    public static final String SYS_BAL_HOME = "ballerina.home";
    public static final String SYS_FILE_WEB_PORT = "composer.port";
    public static final String SYS_WORKSPACE_ENABLE_CLOUD = "enableCloud";
    public static final String SYS_WORKSPACE_PORT = "workspace.port";

    public static final int DEFAULT_FILE_WEB_PORT = 9091;
    public static final int DEFAULT_WORKSPACE_PORT = 8289;


    public static final String SYS_BAL_COMPOSER_HOME = "bal.composer.home";

    public static final String COMPOSER_HOME_NOT_FOUND_ERROR_MESSAGE = "Error: Unable to find ballerina composer home.";

    public static final int DIRECTORY_DEPTH = 30;
}


