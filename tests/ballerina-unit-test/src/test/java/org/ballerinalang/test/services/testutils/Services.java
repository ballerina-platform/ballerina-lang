/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.services.testutils;

import org.ballerinalang.test.util.CompileResult;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * This contains test utils related to Ballerina service invocations.
 *
 * @since 0.8.0
 */
public class Services {


    public static HttpCarbonMessage invokeNew(CompileResult compileResult, String endpointName,
                                              HTTPTestRequest request) {
        return invokeNew(compileResult, ".", Names.EMPTY.value, endpointName, request);
    }

    public static HttpCarbonMessage invokeNew(CompileResult compileResult, String pkgName, String endpointName,
                                              HTTPTestRequest request) {
        return invokeNew(compileResult, pkgName, Names.DEFAULT_VERSION.value, endpointName, request);
    }

    public static HttpCarbonMessage invokeNew(CompileResult compileResult, String pkgName, String version,
                                              String endpointName, HTTPTestRequest request) {
        // module is no longer used
        return null;
    }
}
