/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.util.client.http2;

import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.exception.EndpointTimeOutException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.util.HTTPConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A utility class which sends HTTP/2.0 requests.
 */
public class MessageSender {

    public static HTTPCarbonMessage sendMessage(HTTPCarbonMessage httpCarbonMessage,
                                                HttpClientConnector http2ClientConnector) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HTTPConnectorListener listener = new HTTPConnectorListener(latch);
            HttpResponseFuture responseFuture = http2ClientConnector.send(httpCarbonMessage);
            responseFuture.setHttpConnectorListener(listener);
            if (!latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS)) {
                String errorMsg = "Request timed-out";
                TestUtil.handleException(errorMsg, new EndpointTimeOutException(errorMsg));
            }
            return listener.getHttpResponseMessage();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while sending a message", e);
        }
        return null;
    }
}
