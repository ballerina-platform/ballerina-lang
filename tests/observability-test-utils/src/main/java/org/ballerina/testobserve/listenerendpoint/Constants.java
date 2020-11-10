/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerina.testobserve.listenerendpoint;

import io.ballerina.runtime.api.Module;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Mock Listener related constants.
 */
public class Constants {
    public static final Module TEST_OBSERVE_PACKAGE = new Module(BALLERINA_BUILTIN_PKG_PREFIX, "testobserve",
                                                                 "0.0.0");
    public static final String MOCK_LISTENER_ERROR_TYPE = "MockListenerError";
    public static final String CALLER_TYPE_NAME = "Caller";

    // Keys used for storing native data inside Ballerina Objects
    public static final String WEB_SERVER_NATIVE_DATA_KEY = "_mock_listener_web_server";
    public static final String NETTY_CONTEXT_NATIVE_DATA_KEY = "_mock_listener_netty_context";
}
