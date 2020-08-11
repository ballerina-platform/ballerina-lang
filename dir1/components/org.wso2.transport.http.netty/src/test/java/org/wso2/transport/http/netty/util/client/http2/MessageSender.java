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
import org.wso2.transport.http.netty.contract.HttpClientConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.ResponseHandle;
import org.wso2.transport.http.netty.util.DefaultHttpConnectorListener;
import org.wso2.transport.http.netty.util.TestUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A utility class which sends HTTP/2.0 requests.
 */
public class MessageSender {

    private HttpClientConnector http2ClientConnector;

    public MessageSender(HttpClientConnector http2ClientConnector) {
        this.http2ClientConnector = http2ClientConnector;
    }

    /**
     * Sends httpMessages to the back-end and receive the response.
     *
     * @param httpCarbonMessage {@link HttpCarbonMessage} which should be sent to the remote server.
     * @return the response message
     */
    public HttpCarbonMessage sendMessage(HttpCarbonMessage httpCarbonMessage) {
        try {
            DefaultHttpConnectorListener listener = getDefaultHttpConnectorListener(httpCarbonMessage);
            return listener.getHttpResponseMessage();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while sending a message", e);
        }
        return null;
    }

    public Throwable sendMessageAndExpectError(HttpCarbonMessage httpCarbonMessage) {
        try {
            DefaultHttpConnectorListener listener = getDefaultHttpConnectorListener(httpCarbonMessage);
            return listener.getHttpErrorMessage();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while sending a message", e);
        }
        return null;
    }

    private DefaultHttpConnectorListener getDefaultHttpConnectorListener(HttpCarbonMessage httpCarbonMessage)
        throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        DefaultHttpConnectorListener listener = new DefaultHttpConnectorListener(latch);
        HttpResponseFuture responseFuture = http2ClientConnector.send(httpCarbonMessage);
        responseFuture.setHttpConnectorListener(listener);
        latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
        return listener;
    }

    /**
     * Submits a http message to the back-end and receive a handle to receive response asynchronously.
     *
     * @param httpCarbonMessage {@link HttpCarbonMessage} which should be sent to the remote server.
     * @return the response handle
     */
    public ResponseHandle submitMessage(HttpCarbonMessage httpCarbonMessage) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HttpResponseFuture responseFuture = http2ClientConnector.send(httpCarbonMessage);
            ResponseHandleListener listener = new ResponseHandleListener(latch);
            responseFuture.setResponseHandleListener(listener);
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
            return listener.getResponseHandle();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while submitting a message", e);
        }
        return null;
    }

    /**
     * Fetches the response related to the {@link ResponseHandle}.
     *
     * @param handle the Response Handle which represent the asynchronous service invocation
     * @return response message
     */
    public HttpCarbonMessage getResponse(ResponseHandle handle) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HttpResponseFuture responseFuture = http2ClientConnector.getResponse(handle);
            ResponseListener listener = new ResponseListener(latch);
            responseFuture.setHttpConnectorListener(listener);
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
            return listener.getResponse();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while retrieving the response", e);
        }
        return null;
    }

    /**
     * Checks whether a {@link Http2PushPromise} exists.
     *
     * @param handle the Response Handle which represent the asynchronous service invocation
     * @return whether a promise is available
     */
    public boolean checkPromiseAvailability(ResponseHandle handle) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HttpResponseFuture responseFuture = http2ClientConnector.hasPushPromise(handle);
            PromiseAvailabilityListener listener = new PromiseAvailabilityListener(latch);
            responseFuture.setPromiseAvailabilityListener(listener);
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
            return listener.isPromiseAvailable();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while checking for push promise availability", e);
        }
        return false;
    }

    /**
     * Gets the next available {@link Http2PushPromise} related to a {@link ResponseHandle}.
     *
     * @param handle the Response Handle which represent the asynchronous service invocation
     * @return the available push promise
     */
    public Http2PushPromise getNextPromise(ResponseHandle handle) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HttpResponseFuture responseFuture = http2ClientConnector.getNextPushPromise(handle);
            PromiseListener listener = new PromiseListener(latch);
            responseFuture.setPushPromiseListener(listener);
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
            return listener.getPushPromise();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while fetching a push promise", e);
        }
        return null;
    }

    /**
     * Gets the push response related to a particular promise.
     *
     * @param promise push promise related to the server push
     * @return returns the push response related to a particular promise
     */
    public HttpCarbonMessage getPushResponse(Http2PushPromise promise) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            HttpResponseFuture responseFuture = http2ClientConnector.getPushResponse(promise);
            PushResponseListener listener = new PushResponseListener(latch);
            responseFuture.setPushResponseListener(listener, promise.getPromisedStreamId());
            latch.await(TestUtil.HTTP2_RESPONSE_TIME_OUT, TimeUnit.SECONDS);
            return listener.getPushResponse();
        } catch (Exception e) {
            TestUtil.handleException("Exception occurred while fetching a push promise", e);
        }
        return null;
    }

    private class PushResponseListener implements HttpClientConnectorListener {

        private CountDownLatch latch;
        private HttpCarbonMessage pushResponse;

        PushResponseListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onPushResponse(int promiseId, HttpCarbonMessage pushResponse) {
            this.pushResponse = pushResponse;
            latch.countDown();
        }

        HttpCarbonMessage getPushResponse() {
            return pushResponse;
        }
    }

    private class ResponseHandleListener implements HttpClientConnectorListener {

        private CountDownLatch latch;
        private ResponseHandle responseHandle;

        private ResponseHandleListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onResponseHandle(ResponseHandle responseHandle) {
            this.responseHandle = responseHandle;
            latch.countDown();
        }

        private ResponseHandle getResponseHandle() {
            return responseHandle;
        }
    }

    private class ResponseListener implements HttpClientConnectorListener {

        private CountDownLatch latch;
        private HttpCarbonMessage response;

        ResponseListener(CountDownLatch latch) {
            this.latch = latch;
        }

        public void onMessage(HttpCarbonMessage response) {
            this.response = response;
            latch.countDown();
        }

        public HttpCarbonMessage getResponse() {
            return response;
        }
    }

    private class PromiseAvailabilityListener implements HttpClientConnectorListener {

        private CountDownLatch latch;
        private boolean promiseAvailable;

        private PromiseAvailabilityListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onPushPromiseAvailability(boolean promiseAvailable) {
            this.promiseAvailable = promiseAvailable;
            latch.countDown();
        }

        private boolean isPromiseAvailable() {
            return promiseAvailable;
        }
    }

    private class PromiseListener implements HttpClientConnectorListener {

        private CountDownLatch latch;
        private Http2PushPromise pushPromise;

        private PromiseListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onPushPromise(Http2PushPromise pushPromise) {
            this.pushPromise = pushPromise;
            latch.countDown();
        }

        private Http2PushPromise getPushPromise() {
            return pushPromise;
        }
    }
}
