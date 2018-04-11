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


import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpDispatcher;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.util.codegen.ProgramFile;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Collections;
import java.util.Map;

/**
 * This contains test utils related to Ballerina service invocations.
 *
 * @since 0.8.0
 */
public class Services {


    public static HTTPCarbonMessage invokeNew(CompileResult compileResult, String endpointName,
                                              HTTPTestRequest request) {
        return invokeNew(compileResult, ".", endpointName, request);
    }

    public static HTTPCarbonMessage invokeNew(CompileResult compileResult, String pkgName, String endpointName,
                                              HTTPTestRequest request) {
        ProgramFile programFile = compileResult.getProgFile();
        BStruct connectorEndpoint = BLangConnectorSPIUtil.getPackageEndpoint(programFile, pkgName, endpointName);

        HTTPServicesRegistry httpServicesRegistry =
                (HTTPServicesRegistry) connectorEndpoint.getNativeData("HTTP_SERVICE_REGISTRY");
        TestCallableUnitCallback callback = new TestCallableUnitCallback(request);
        request.setCallback(callback);
        HttpResource resource = HttpDispatcher.findResource(httpServicesRegistry, request);
        if (resource == null) {
            return callback.getResponseMsg();
        }
        //TODO below should be fixed properly
        //basically need to find a way to pass information from server connector side to client connector side
        Map<String, Object> properties = null;
        if (request.getProperty(HttpConstants.SRC_HANDLER) != null) {
            Object srcHandler = request.getProperty(HttpConstants.SRC_HANDLER);
            properties = Collections.singletonMap(HttpConstants.SRC_HANDLER, srcHandler);
        }
        BValue[] signatureParams = HttpDispatcher.getSignatureParameters(resource, request);
        callback.setRequestStruct(signatureParams[0]);
        Executor.submit(resource.getBalResource(), callback, properties, null, signatureParams);
        callback.sync();
        return callback.getResponseMsg();
    }
}
