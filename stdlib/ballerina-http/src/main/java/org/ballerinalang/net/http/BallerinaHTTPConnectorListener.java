/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.mime.util.Constants;
import org.ballerinalang.model.values.BValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Collections;
import java.util.Map;

/**
 * HTTP connector listener for Ballerina.
 */
public class BallerinaHTTPConnectorListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaHTTPConnectorListener.class);
    private static final String HTTP_RESOURCE = "httpResource";

    private final HTTPServicesRegistry httpServicesRegistry;

    public BallerinaHTTPConnectorListener(HTTPServicesRegistry httpServicesRegistry) {
        this.httpServicesRegistry = httpServicesRegistry;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
        HttpResource httpResource;
        if (!isAccessed(httpCarbonMessage)) {
            httpResource = HttpDispatcher.findResource(httpServicesRegistry, httpCarbonMessage);
            if(HttpDispatcher.isDiffered(httpResource)) {
                httpCarbonMessage.setProperty(HTTP_RESOURCE, httpResource);
            } else {
                extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
            }
        } else {
            httpResource = (HttpResource) httpCarbonMessage.getProperty(HTTP_RESOURCE);
            extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error in http server connector", throwable);
    }

    private void extractPropertiesAndStartResourceExecution(HTTPCarbonMessage httpCarbonMessage, HttpResource httpResource) {
        Map<String, Object> properties = null;
        //TODO below should be fixed properly
        //basically need to find a way to pass information from server connector side to client connector side
        if (httpCarbonMessage.getProperty(HttpConstants.SRC_HANDLER) != null) {
            Object srcHandler = httpCarbonMessage.getProperty(HttpConstants.SRC_HANDLER);
            properties = Collections.singletonMap(HttpConstants.SRC_HANDLER, srcHandler);
        }
        BValue[] signatureParams = HttpDispatcher.getSignatureParameters(httpResource, httpCarbonMessage);
        ConnectorFuture future = Executor.submit( httpResource.getBalResource(), properties, signatureParams);
        ConnectorFutureListener futureListener = new HttpConnectorFutureListener(httpCarbonMessage);
        future.setConnectorFutureListener(futureListener);
    }

    private boolean isAccessed(HTTPCarbonMessage httpCarbonMessage) {
        return httpCarbonMessage.getProperty(HTTP_RESOURCE) != null;
    }
}
