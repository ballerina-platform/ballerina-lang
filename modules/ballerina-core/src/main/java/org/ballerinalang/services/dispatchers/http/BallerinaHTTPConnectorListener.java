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

package org.ballerinalang.services.dispatchers.http;

import org.ballerinalang.runtime.threadpool.RequestWorkerThread;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

/**
 * HTTP connector listener for Ballerina.
 */
public class BallerinaHTTPConnectorListener implements HttpConnectorListener {

    private static final Logger breLog = LoggerFactory.getLogger(BallerinaHTTPConnectorListener.class);


    @Override
    public void onMessage(HTTPCarbonMessage httpCarbonMessage) {

        ThreadPoolFactory.getInstance().getExecutor().
                execute(new RequestWorkerThread(httpCarbonMessage, (cMsg) -> {
                    HTTPCarbonMessage carbonMessage = HTTPMessageUtil.convertCarbonMessage(cMsg);
                    try {
                        httpCarbonMessage.respond(carbonMessage);
                    } catch (ServerConnectorException e) {
                        throw new BallerinaException("Error occurred during response", e);
                    }
                }));
    }

    @Override
    public void onError(Throwable throwable) {
    }
}
