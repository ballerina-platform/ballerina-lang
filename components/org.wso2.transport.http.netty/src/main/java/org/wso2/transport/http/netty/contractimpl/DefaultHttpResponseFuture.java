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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of the response returnError future.
 */
public class DefaultHttpResponseFuture implements HttpResponseFuture {

    private HttpConnectorListener httpConnectorListener;
    private HttpClientConnectorListener responseHandleListener;
    private HttpClientConnectorListener promiseAvailabilityListener;
    private HttpConnectorListener pushPromiseListener;
    private ConcurrentHashMap<Integer, HttpConnectorListener> pushResponseListeners;
    private ConcurrentHashMap<Integer, Throwable> pushResponseListenerErrors;

    private HTTPCarbonMessage httpCarbonMessage;
    private ResponseHandle responseHandle;
    private OutboundMsgHolder outboundMsgHolder;

    private Throwable throwable, responseHandleError, returnError;
    private Semaphore executionWaitSem;

    // Lock to synchronize response related operations
    private Lock responseLock = new ReentrantLock();
    // Lock to synchronize response handle related operations
    private Lock responseHandleLock = new ReentrantLock();
    // Lock to synchronize promise availability related operations
    private Lock promiseAvailabilityLock = new ReentrantLock();
    // Lock to synchronize promise related operations
    private Lock promiseLock = new ReentrantLock();
    // Lock to synchronize push response related operations
    private Lock pushResponseLock = new ReentrantLock();

    public DefaultHttpResponseFuture(OutboundMsgHolder outboundMsgHolder) {
        this.outboundMsgHolder = outboundMsgHolder;
        pushResponseListeners = new ConcurrentHashMap<>();
        pushResponseListenerErrors = new ConcurrentHashMap<>();
    }

    public DefaultHttpResponseFuture() {
        this(null);
    }

    @Override
    public void setHttpConnectorListener(HttpConnectorListener connectorListener) {
        responseLock.lock();
        try {
            this.httpConnectorListener = connectorListener;
            if (httpCarbonMessage != null) {
                notifyHttpListener(httpCarbonMessage);
                httpCarbonMessage = null;
            }
            if (this.throwable != null) {
                notifyHttpListener(this.throwable);
                this.throwable = null;
            }
        } finally {
            responseLock.unlock();
        }
    }

    @Override
    public void removeHttpListener() {
        this.httpConnectorListener = null;
    }

    @Override
    public void notifyHttpListener(HTTPCarbonMessage httpCarbonMessage) {
        responseLock.lock();
        try {
            this.httpCarbonMessage = httpCarbonMessage;
            if (executionWaitSem != null) {
                executionWaitSem.release();
            }
            if (httpConnectorListener != null) {
                HttpConnectorListener listener = httpConnectorListener;
                removeHttpListener();
                listener.onMessage(httpCarbonMessage);
            }
        } finally {
            responseLock.unlock();
        }
    }

    @Override
    public void notifyHttpListener(Throwable throwable) {
        responseLock.lock();
        try {
            this.throwable = throwable;
            returnError = throwable;
            if (executionWaitSem != null) {
                executionWaitSem.release();
            }
            if (httpConnectorListener != null) {
                HttpConnectorListener listener = httpConnectorListener;
                removeHttpListener();
                listener.onError(throwable);
            }
        } finally {
            responseLock.unlock();
        }
    }

    public HttpResponseFuture sync() throws InterruptedException {
        // sync operation is not synchronized with locks as it might cause a deadlock.
        // We may have to refactor this using conditions in ReentrantLock later.
        executionWaitSem = new Semaphore(0);
        if (this.httpCarbonMessage == null && this.throwable == null && this.returnError == null) {
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

    public void resetStatus() {
        this.returnError = null;
    }

    @Override
    public void setResponseHandleListener(HttpClientConnectorListener responseHandleListener) {
        responseHandleLock.lock();
        try {
            this.responseHandleListener = responseHandleListener;
            if (responseHandle != null) {
                notifyResponseHandle(responseHandle);
                responseHandle = null;
            }
            if (responseHandleError != null) {
                notifyHttpListener(responseHandleError);
                this.responseHandleError = null;
            }
        } finally {
            responseHandleLock.unlock();
        }
    }

    @Override
    public void removeResponseHandleListener() {
        this.responseHandleListener = null;
    }

    @Override
    public void notifyResponseHandle(ResponseHandle responseHandle) {
        responseHandleLock.lock();
        try {
            this.responseHandle = responseHandle;
            if (responseHandleListener != null) {
                HttpClientConnectorListener listener = responseHandleListener;
                removeResponseHandleListener();
                this.responseHandle = null;
                listener.onResponseHandle(responseHandle);
            }
        } finally {
            responseHandleLock.unlock();
        }
    }

    @Override
    public void notifyResponseHandle(Throwable throwable) {
        responseHandleLock.lock();
        try {
            responseHandleError = throwable;
            if (responseHandleListener != null) {
                HttpConnectorListener listener = responseHandleListener;
                removeResponseHandleListener();
                responseHandleError = null;
                listener.onError(throwable);
            }
        } finally {
            responseHandleLock.unlock();
        }
    }

    @Override
    public void setPromiseAvailabilityListener(HttpClientConnectorListener promiseAvailabilityListener) {
        promiseAvailabilityLock.lock();
        try {
            this.promiseAvailabilityListener = promiseAvailabilityListener;
            notifyPromiseAvailability();
        } finally {
            promiseAvailabilityLock.unlock();
        }
    }

    @Override
    public void removePromiseAvailabilityListener() {
        this.promiseAvailabilityListener = null;
    }

    @Override
    public void notifyPromiseAvailability() {
        promiseAvailabilityLock.lock();
        try {
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
        } finally {
            promiseAvailabilityLock.unlock();
        }
    }

    @Override
    public void setPushPromiseListener(HttpConnectorListener pushPromiseListener) {
        promiseLock.lock();
        try {
            this.pushPromiseListener = pushPromiseListener;
            if (outboundMsgHolder.hasPromise()) {
                notifyPushPromise();
            }
        } finally {
            promiseLock.unlock();
        }
    }

    @Override
    public void removePushPromiseListener() {
        this.pushPromiseListener = null;
    }

    @Override
    public void notifyPushPromise() {
        promiseLock.lock();
        try {
            if (pushPromiseListener != null) {
                HttpConnectorListener listener = pushPromiseListener;
                removePushPromiseListener();
                listener.onPushPromise(outboundMsgHolder.getNextPromise());
            }
        } finally {
            promiseLock.unlock();
        }
    }

    @Override
    public void setPushResponseListener(HttpConnectorListener pushResponseListener, int promiseId) {
        pushResponseLock.lock();
        try {
            pushResponseListeners.put(promiseId, pushResponseListener);
            HTTPCarbonMessage pushResponse = outboundMsgHolder.getPushResponse(promiseId);
            if (pushResponse != null) {
                notifyPushResponse(promiseId, pushResponse);
            }
            if (pushResponseListenerErrors.get(promiseId) != null) {
                notifyPushResponse(promiseId, pushResponseListenerErrors.get(promiseId));
            }
        } finally {
            pushResponseLock.unlock();
        }
    }

    @Override
    public void removePushResponseListener(int promisedId) {
        this.pushResponseListeners.remove(promisedId);
    }

    @Override
    public void notifyPushResponse(int streamId, HTTPCarbonMessage pushResponse) {
        pushResponseLock.lock();
        try {
            HttpConnectorListener listener = pushResponseListeners.get(streamId);
            if (listener != null) {
                pushResponseListeners.remove(streamId);
                listener.onPushResponse(streamId, pushResponse);
            }
        } finally {
            pushResponseLock.unlock();
        }
    }

    @Override
    public void notifyPushResponse(int streamId, Throwable throwable) {
        pushResponseLock.lock();
        try {
            pushResponseListenerErrors.put(streamId, throwable);
            HttpConnectorListener listener = pushResponseListeners.get(streamId);
            if (listener != null) {
                pushResponseListeners.remove(streamId);
                pushResponseListenerErrors.remove(streamId);
                listener.onError(throwable);
            }
        } finally {
            pushResponseLock.unlock();
        }
    }
}
