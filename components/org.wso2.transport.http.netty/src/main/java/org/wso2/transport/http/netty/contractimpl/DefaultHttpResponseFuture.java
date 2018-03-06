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

package org.wso2.transport.http.netty.contractimpl;

import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.ResponseHandle;
import org.wso2.transport.http.netty.sender.http2.OutboundMsgHolder;

import java.util.concurrent.Semaphore;

/**
 * Implementation of the response returnError future.
 */
public class DefaultHttpResponseFuture implements HttpResponseFuture {

    private HTTPCarbonMessage httpCarbonMessage;
    private HttpConnectorListener httpConnectorListener;
    private Throwable throwable, returnError;
    private Semaphore executionWaitSem;
    private OutboundMsgHolder outboundMsgHolder;
    private HttpConnectorListener promiseAvailabilityListener;
    private HttpConnectorListener pushPromiseListener;
    private ResponseHandle responseHandle;
    private HttpConnectorListener responseHandleListener;

    public DefaultHttpResponseFuture(OutboundMsgHolder outboundMsgHolder) {
        this.outboundMsgHolder = outboundMsgHolder;
    }

    public DefaultHttpResponseFuture() {
    }

    @Override
    public void notifyHttpListener(HTTPCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
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

    @Override
    public void notifyPushResponse(int promisedId, HTTPCarbonMessage pushResponse) {
        this.httpCarbonMessage = pushResponse;
        if (executionWaitSem != null) {
            executionWaitSem.release();
        }
        if (httpConnectorListener != null) {
            httpConnectorListener.onPushResponse(promisedId, httpCarbonMessage);
        }
    }

    public HttpResponseFuture sync() throws InterruptedException {
        executionWaitSem = new Semaphore(0);
        if (this.httpCarbonMessage == null && this.throwable == null) {
            executionWaitSem.acquire();
        }
        if (httpCarbonMessage != null) {
            returnError = null;
            httpCarbonMessage = null;
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
    public void setHttpConnectorListener(HttpConnectorListener connectorListener) {
        this.httpConnectorListener = connectorListener;
        if (httpCarbonMessage != null) {
            notifyHttpListener(httpCarbonMessage);
            httpCarbonMessage = null;
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

    @Override
    public void notifyPromiseAvailability() {
        if (promiseAvailabilityListener != null) {
            HttpConnectorListener listener = promiseAvailabilityListener;
            if (outboundMsgHolder.hasPromise()) {
                removePromiseAvailabilityListener();
                listener.onPushPromiseAvailability(true);
            } else if (outboundMsgHolder.isAllPromisesReceived()) {
                removePromiseAvailabilityListener();
                listener.onPushPromiseAvailability(false);
            }
        }
    }

    @Override
    public void setPromiseAvailabilityListener(HttpConnectorListener promiseAvailabilityListener) {
        this.promiseAvailabilityListener = promiseAvailabilityListener;
        notifyPromiseAvailability();
        if (this.throwable != null) {
            notifyHttpListener(this.throwable);
            this.throwable = null;
        }
    }

    @Override
    public void removePromiseAvailabilityListener() {
        this.promiseAvailabilityListener = null;
    }

    @Override
    public void notifyPushPromise() {
        if (pushPromiseListener != null) {
            pushPromiseListener.onPushPromise(outboundMsgHolder.getNextPromise());
            removePushPromiseListener();
        }
    }

    @Override
    public void setPushPromiseListener(HttpConnectorListener pushPromiseListener) {
        this.pushPromiseListener = pushPromiseListener;
        if (outboundMsgHolder.hasPromise()) {
            notifyPushPromise();
        }
        if (this.throwable != null) {
            notifyHttpListener(this.throwable);
            this.throwable = null;
        }
    }

    @Override
    public void removePushPromiseListener() {
        this.pushPromiseListener = null;
    }

    @Override
    public void notifyResponseHandle(ResponseHandle responseHandle) {
        this.responseHandle = responseHandle;
        if (responseHandleListener != null) {
            responseHandleListener.onResponseHandle(responseHandle);
            removeResponseHandleListener();
        }
    }

    @Override
    public void setResponseHandleListener(HttpConnectorListener connectorListener) {
        this.responseHandleListener = connectorListener;
        if (responseHandle != null) {
            notifyResponseHandle(responseHandle);
            responseHandle = null;
        }
        if (this.throwable != null) {
            notifyHttpListener(this.throwable);
            this.throwable = null;
        }
    }

    @Override
    public void removeResponseHandleListener() {
        this.responseHandleListener = null;
    }


}
