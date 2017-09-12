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
import org.ballerinalang.connector.api.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * HTTP connector listener for Ballerina.
 */
public class BallerinaHTTPConnectorListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaHTTPConnectorListener.class);


    @Override
    public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
        Executor.submit(HttpDispatcher.findResource(httpCarbonMessage), httpCarbonMessage,
                HttpDispatcher.getCallback(httpCarbonMessage));
        Resource resource = HttpDispatcher.findResource(httpCarbonMessage);
        Executor.submit(resource, HttpDispatcher.getSignatureParameters(resource, httpCarbonMessage));
        ConnectorFuture future = Executor.submit(HttpDispatcher.findResource(httpCarbonMessage),
                httpCarbonMessage);
        ConnectorFutureListener futureListener = new HttpConnectorFutureListener(HttpDispatcher
                .getCallback(httpCarbonMessage));
        future.setConnectorFutureListener(futureListener);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error in http server connector", throwable);
    }
}
