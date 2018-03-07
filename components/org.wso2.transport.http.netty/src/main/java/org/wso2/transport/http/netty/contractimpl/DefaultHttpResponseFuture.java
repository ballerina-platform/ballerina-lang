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

import org.wso2.transport.http.netty.contract.HttpClientConnectorListener;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.ResponseHandle;
import org.wso2.transport.http.netty.sender.http2.OutboundMsgHolder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Implementation of the response returnError future.
 */
public class DefaultHttpResponseFuture implements HttpResponseFuture {

    private HttpConnectorListener httpConnectorListener;
    private HttpClientConnectorListener responseHandleListener;
    private HttpClientConnectorListener promiseAvailabilityListener;
    private HttpConnectorListener pushPromiseListener;
    private ConcurrentHashMap<Integer, HttpConnectorListener> pushResponseListeners;

    private HTTPCarbonMessage httpCarbonMessage;
    private ResponseHandle responseHandle;
    private OutboundMsgHolder outboundMsgHolder;

    private Throwable throwable, returnError;
    private Semaphore executionWaitSem;

    public DefaultHttpResponseFuture(OutboundMsgHolder outboundMsgHolder) {
        this.outboundMsgHolder = outboundMsgHolder;
        pushResponseListeners = new ConcurrentHashMap<>();
    }

    public DefaultHttpResponseFuture() {
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
    public void setResponseHandleListener(HttpClientConnectorListener responseHandleListener) {
        this.responseHandleListener = responseHandleListener;
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

    @Override
    public void notifyResponseHandle(ResponseHandle responseHandle) {
        this.responseHandle = responseHandle;
        if (responseHandleListener != null) {
            responseHandleListener.onResponseHandle(responseHandle);
            removeResponseHandleListener();
        }
    }

    @Override
    public void setPromiseAvailabilityListener(HttpClientConnectorListener promiseAvailabilityListener) {
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
    public void notifyPromiseAvailability() {
        if (promiseAvailabilityListener != null) {
            HttpClientConnectorListener listener = promiseAvailabilityListener;
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
    public void notifyPushPromise() {
        if (pushPromiseListener != null) {
            HttpConnectorListener listener = pushPromiseListener;
            removePushPromiseListener();
            listener.onPushPromise(outboundMsgHolder.getNextPromise());
        }
    }

    @Override
    public void setPushResponseListener(HttpConnectorListener pushResponseListener, int promiseId) {
        this.pushResponseListeners.put(promiseId, pushResponseListener);
        HTTPCarbonMessage pushResponse = outboundMsgHolder.getPushResponse(promiseId);
        if (pushResponse != null) {
            notifyPushResponse(promiseId, pushResponse);
        }
        if (this.throwable != null) {
            notifyHttpListener(this.throwable);
            this.throwable = null;
        }
    }

    @Override
    public void removePushResponseListener(int promisedId) {
        this.pushResponseListeners.remove(promisedId);
    }

    @Override
    public void notifyPushResponse(int promisedId, HTTPCarbonMessage pushResponse) {
        if (pushResponseListeners.get(promisedId) != null) {
            pushResponseListeners.get(promisedId).onPushResponse(promisedId, pushResponse);
            pushResponseListeners.remove(promisedId);
        }
    }

}
