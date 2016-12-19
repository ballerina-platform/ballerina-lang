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
 * Constants related to Ballerina runtime
 */
public class Constants {

    public static final String PROTOCOL = "PROTOCOL";

    // Name of the System property which contains the ballerina file path to be executed
    public static final String SYSTEM_PROP_BAL_FILE = "bal-file";

    // Name of the system property to hold the input arguments
    public static final String SYSTEM_PROP_BAL_ARGS = "bal-args";

    /**
     * Runtime modes of Ballerina engine
     */
    public enum RuntimeMode {
        RUN_FILE, SERVER
    };

}
