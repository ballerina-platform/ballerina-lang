/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime;

/**
 * Constants related to Ballerina runtime.
 */
public class Constants {

    // Name of the System property which contains the ballerina file path to be executed
    public static final String SYSTEM_PROP_RUN_FILE = "run-file";

    public static final String SYSTEM_PROP_RUN_MODE = "run-mode";

    public static final String SYSTEM_PROP_BASE_DIR = "base-dir";

    public static final String SYSTEM_PROP_RUN_MODE_SERVER = "server";
    public static final String SYSTEM_PROP_RUN_MODE_RUN = "run";

    // Name of the system property to hold the input arguments
    public static final String SYSTEM_PROP_BAL_ARGS = "bargs";

    // Name of the directory where Bal files are stored.
    public static final String SYSTEM_PROP_DIR = "bal-dir";

    // Name of the main function
    public static final String MAIN_FUNCTION_NAME = "main";

    // Intermediate headers added to the ballerina message
    public static final String INTERMEDIATE_HEADERS = "INTERMEDIATE_HEADERS";

    // Name of the map that holds Resource arguments which are populated from server connector.
    public static final String RESOURCE_ARGS = "RESOURCE_ARGS";

    // Ballerina version system property name
    public static final String BALLERINA_VERSION = "ballerina.version";


    /**
     * Runtime modes of Ballerina engine.
     */
    public enum RuntimeMode {
        // Run File Mode.
        RUN_FILE,
        // Run Ballerina Server Mode.
        SERVER,
        // Represents ERROR Condition.
        ERROR
    }

}
