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

package org.ballerinalang.runtime;

/**
 * Constants related to Ballerina runtime.
 */
public class Constants {

    public static final String SYS_PROP_ENABLE_NONBLOCKING = "enable.nonblocking";

    // Intermediate headers added to the ballerina message
    public static final String INTERMEDIATE_HEADERS = "INTERMEDIATE_HEADERS";

    // Name of the map that holds Resource arguments which are populated from server connector.
    public static final String RESOURCE_ARGS = "RESOURCE_ARGS";

    // Ballerina version system property name
    public static final String BALLERINA_VERSION = "ballerina.version";

    // logger names.
    public static final String BAL_LINKED_INTERPRETER_LOGGER = "BLinkedInterpreter";

    // Name of the system property to hold the debug port
    public static final String SYSTEM_PROP_BAL_DEBUG = "ballerina.debug";

}
