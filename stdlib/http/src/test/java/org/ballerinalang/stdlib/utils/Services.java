/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.utils;

import io.ballerina.runtime.api.BRuntime;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.util.exceptions.BallerinaConnectorException;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.netty.handler.codec.http.HttpContent;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpDispatcher;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.mock.nonlistening.MockHTTPConnectorListener;
import org.ballerinalang.net.http.mock.nonlistening.MockHTTPConnectorListener.RegistryHolder;
import org.ballerinalang.test.util.CompileResult;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * This contains test utils related to Ballerina service invocations.
 *
 * @since 0.990.3
 */
public class Services {

    @Deprecated
    public static HttpCarbonMessage invokeNew(CompileResult compileResult, String endpointName,
                                              HTTPTestRequest request) {
        return invokeNew(compileResult, ".", Names.EMPTY.value, endpointName, request);
    }

    @Deprecated
    public static HttpCarbonMessage invokeNew(CompileResult compileResult, String pkgName, String endpointName,
                                              HTTPTestRequest request) {
        return invokeNew(compileResult, pkgName, Names.DEFAULT_VERSION.value, endpointName, request);
    }

    @Deprecated
    public static HttpCarbonMessage invokeNew(CompileResult compileResult, String pkgName, String version,
                                              String endpointName, HTTPTestRequest request) {
        return invoke(0, request);
    }

    public static HttpCarbonMessage invoke(int listenerPort, HTTPTestRequest request) {
        RegistryHolder registryHolder = MockHTTPConnectorListener.getInstance()
                                                            .getHttpServicesRegistry(listenerPort);
        if (registryHolder == null) {
            return null;
        }
        TestCallableUnitCallback callback = new TestCallableUnitCallback(request);
        request.setCallback(callback);
        HttpResource resource = null;
        try {
            resource = HttpDispatcher.findResource(registryHolder.getRegistry(), request);
        } catch (BallerinaException ex) {
            HttpUtil.handleFailure(request, new BallerinaConnectorException(ex.getMessage()));
        }
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

        Object[] signatureParams = HttpDispatcher.getSignatureParameters(resource,
                request, registryHolder.getEndpointConfig());
        callback.setRequestStruct(signatureParams[0]);

        BObject service = resource.getParentService().getBalService();

        BRuntime runtime = registryHolder.getRegistry().getRuntime();
        runtime.invokeMethodAsync(service, resource.getName(), null, null, callback, properties, signatureParams);
        callback.sync();

        HttpCarbonMessage originalMsg = callback.getResponseMsg();
        LinkedList<HttpContent> list = new LinkedList<>();
        while (!originalMsg.isEmpty()) {
            HttpContent httpContent = originalMsg.getHttpContent();
            list.add(httpContent);
        }
        while (!list.isEmpty()) {
            originalMsg.addHttpContent(list.pop());
        }
        request.getTestHttpResponseStatusFuture().notifyHttpListener(HttpUtil.createHttpCarbonMessage(false));
        return callback.getResponseMsg();
    }
}
