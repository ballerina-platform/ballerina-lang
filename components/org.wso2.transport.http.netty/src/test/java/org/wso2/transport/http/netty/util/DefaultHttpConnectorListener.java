/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.util;

import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.concurrent.CountDownLatch;

/**
 * A connector listener for HTTP
 */
public class DefaultHttpConnectorListener implements HttpConnectorListener {

    private HttpCarbonMessage httpMessage;
    private Throwable throwable;
    private CountDownLatch latch;

    public DefaultHttpConnectorListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onMessage(HttpCarbonMessage httpMessage) {
        this.httpMessage = httpMessage;
        latch.countDown();
    }

    @Override
    public void onError(Throwable throwable) {
        this.throwable = throwable;
        latch.countDown();
    }

    public HttpCarbonMessage getHttpResponseMessage() {
        return httpMessage;
    }

    public Throwable getHttpErrorMessage() {
        return throwable;
    }
}
