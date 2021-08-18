/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langlib.runtime;

import io.ballerina.runtime.api.Module;

/**
 * Runtime related constants.
 */
public class Constants {

    private static final String PACKAGE_NAME = "lang.runtime";
    public static final String CALL_STACK_ELEMENT = "CallStackElement";
    public static final String BALLERINA_BUILTIN_PKG_PREFIX = "ballerina";
    private static final String PACKAGE_VERSION = "0";
    public static final Module BALLERINA_RUNTIME_PKG_ID = new Module(BALLERINA_BUILTIN_PKG_PREFIX,
            PACKAGE_NAME, PACKAGE_VERSION);

    private Constants() {

    }
}
