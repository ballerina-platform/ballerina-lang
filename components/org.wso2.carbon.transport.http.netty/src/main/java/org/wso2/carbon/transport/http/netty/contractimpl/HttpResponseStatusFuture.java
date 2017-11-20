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
 *
 */

package org.wso2.carbon.transport.http.netty.contractimpl;

import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.concurrent.Semaphore;

/**
 * Implementation of the response status future.
 */
public class HttpResponseStatusFuture implements HttpResponseFuture {

    private HTTPCarbonMessage httpCarbonMessage;
    private Throwable throwable, status;
    private Semaphore executionWaitSem;

    @Override
    public void notifyHttpListener(HTTPCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
        if (executionWaitSem != null) {
            executionWaitSem.release();
        }
    }

    @Override
    public void notifyHttpListener(Throwable throwable) {
        this.throwable = throwable;
        if (executionWaitSem != null) {
            executionWaitSem.release();
        }
    }

    public HttpResponseStatusFuture sync() throws InterruptedException {
        executionWaitSem = new Semaphore(0);
        if (this.httpCarbonMessage == null && this.throwable == null) {
            executionWaitSem.acquire();
        }
        if (httpCarbonMessage != null) {
            status = null;
            httpCarbonMessage = null;
        }
        if (throwable != null) {
            status = throwable;
            throwable = null;
        }
        return this;
    }

    public Throwable getStatus() {
        return status;
    }

    @Override
    public void setHttpConnectorListener(HttpConnectorListener connectorListener) {
    }

    @Override
    public void removeHttpListener() {
    }
}
