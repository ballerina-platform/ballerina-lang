/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.contractimpl;

import org.wso2.transport.http.netty.contract.Http2ConnectorListener;
import org.wso2.transport.http.netty.contract.Http2ResponseFuture;
import org.wso2.transport.http.netty.message.Http2Response;

import java.util.concurrent.Semaphore;

/**
 * Implementation of the response returnError future.
 */
public class DefaultHttp2ResponseFuture implements Http2ResponseFuture {

    private Http2Response http2Response;
    private Http2ConnectorListener httpConnectorListener;
    private Throwable throwable, returnError;
    private Semaphore executionWaitSem;

    @Override
    public void notifyHttpListener(Http2Response httpCarbonMessage) {
        this.http2Response = httpCarbonMessage;
        if (executionWaitSem != null) {
            executionWaitSem.release();
        }
        if (httpConnectorListener != null) {
            httpConnectorListener.onMessage(httpCarbonMessage);
        }
    }

    @Override
    public void notifyHttpListener(Throwable throwable) {
        this.throwable = throwable;
        if (executionWaitSem != null) {
            executionWaitSem.release();
        }
        if (httpConnectorListener != null) {
            httpConnectorListener.onError(throwable);
        }
    }

    public Http2ResponseFuture sync() throws InterruptedException {
        executionWaitSem = new Semaphore(0);
        if (this.http2Response == null && this.throwable == null) {
            executionWaitSem.acquire();
        }
        if (http2Response != null) {
            returnError = null;
            http2Response = null;
        }
        if (throwable != null) {
            returnError = throwable;
            throwable = null;
        }
        return this;
    }

    public DefaultOperationStatus getStatus() {
        return this.returnError != null ? new DefaultOperationStatus(this.returnError)
                                        : new DefaultOperationStatus(null);
    }

    @Override
    public void setHttpConnectorListener(Http2ConnectorListener connectorListener) {
        this.httpConnectorListener = connectorListener;
        if (http2Response != null) {
            notifyHttpListener(http2Response);
            http2Response = null;
        }
        if (this.throwable != null) {
            notifyHttpListener(this.throwable);
            this.throwable = null;
        }
    }

    @Override
    public void removeHttpListener() {
        this.httpConnectorListener = null;
    }
}
