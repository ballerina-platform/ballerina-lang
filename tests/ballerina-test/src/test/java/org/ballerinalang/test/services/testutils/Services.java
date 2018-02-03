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


import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.BallerinaHttpServerConnector;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpDispatcher;
import org.ballerinalang.net.http.HttpResource;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Collections;
import java.util.Map;

/**
 * This contains test utils related to Ballerina service invocations.
 *
 * @since 0.8.0
 */
public class Services {

    public static HTTPCarbonMessage invokeNew(CompileResult compileResult, HTTPTestRequest request) {
        BallerinaHttpServerConnector httpServerConnector = (BallerinaHttpServerConnector) ConnectorUtils.
                getBallerinaServerConnector(compileResult.getProgFile(), Constants.HTTP_PACKAGE_PATH);
        TestHttpFutureListener futureListener = new TestHttpFutureListener(request);
        request.setFutureListener(futureListener);
        HttpResource resource = HttpDispatcher.findResource(httpServerConnector.getHttpServicesRegistry(), request);
        if (resource == null) {
            return futureListener.getResponseMsg();
        }
        //TODO below should be fixed properly
        //basically need to find a way to pass information from server connector side to client connector side
        Map<String, Object> properties = null;
        if (request.getProperty(Constants.SRC_HANDLER) != null) {
            Object srcHandler = request.getProperty(Constants.SRC_HANDLER);
            properties = Collections.singletonMap(Constants.SRC_HANDLER, srcHandler);
        }
        BValue[] signatureParams = HttpDispatcher.getSignatureParameters(resource, request);
        ConnectorFuture future = Executor.submit(resource.getBalResource(), properties, signatureParams);
        futureListener.setRequestStruct(signatureParams[0]);
        future.setConnectorFutureListener(futureListener);
        futureListener.sync();
        return futureListener.getResponseMsg();
    }
}
