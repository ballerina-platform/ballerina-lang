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

package org.wso2.transport.http.netty.sender.http2;

import org.wso2.transport.http.netty.contract.HttpPushPromiseAvailabilityFuture;
import org.wso2.transport.http.netty.contract.HttpPushPromiseFuture;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpResponseHandleFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpPushPromiseAvailabilityFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpPushPromiseFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpResponseFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpResponseHandleFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * {@code OutboundMsgHolder} holds data related to a single outbound invocation
 */
public class OutboundMsgHolder {

    // Outbound request HTTPCarbonMessage
    private HTTPCarbonMessage requestCarbonMessage;
    // Future which is used to notify the response listener upon response receive
    private HttpResponseFuture responseFuture;
    private HttpPushPromiseFuture pushPromiseFuture;
    private HttpPushPromiseAvailabilityFuture pushPromiseAvailabilityFuture;
    private HttpResponseHandleFuture responseHandleFuture;
    private ConcurrentHashMap<Integer, HttpResponseFuture> pushResponseFutures;

    private TargetChannel targetChannel;

    private BlockingQueue<HttpCarbonResponse> pushResponses;
    private BlockingQueue<Http2PushPromise> promises;
    private ConcurrentHashMap<Integer, HttpCarbonResponse> pushResponsesMap;
    private HttpCarbonResponse response;

    private boolean allPromisesReceived = false;
    private int promisesCount = 0;
    private int pushResponsesCount = 0;

    public OutboundMsgHolder(HTTPCarbonMessage httpCarbonMessage, TargetChannel targetChannel) {
        this.requestCarbonMessage = httpCarbonMessage;
        this.targetChannel = targetChannel;
        pushResponses = new LinkedBlockingQueue();
        promises = new LinkedBlockingQueue();
        pushResponsesMap = new ConcurrentHashMap<>();
        pushResponseFutures = new ConcurrentHashMap<>();
        responseFuture = new DefaultHttpResponseFuture();
        responseHandleFuture = new DefaultHttpResponseHandleFuture();
        pushPromiseFuture = new DefaultHttpPushPromiseFuture(this);
        pushPromiseAvailabilityFuture = new DefaultHttpPushPromiseAvailabilityFuture(this);
    }

    /**
     * Get Outbound request HTTPCarbonMessage
     *
     * @return request HTTPCarbonMessage
     */
    public HTTPCarbonMessage getRequest() {
        return requestCarbonMessage;
    }

    /**
     * Get the Future which is used to notify the response listener upon response receive
     *
     * @return the Future used to notify the response listener
     */
    public HttpResponseFuture getResponseFuture() {
        return responseFuture;
    }

    public HttpPushPromiseFuture getPushPromiseFuture() {
        return pushPromiseFuture;
    }

    public HttpPushPromiseAvailabilityFuture getPushPromiseAvailabilityFuture() {
        return pushPromiseAvailabilityFuture;
    }

    public HttpResponseHandleFuture getResponseHandleFuture() {
        return responseHandleFuture;
    }

    public TargetChannel getTargetChannel() {
        return targetChannel;
    }

    public void addPromise(Http2PushPromise pushPromise) {
        promises.add(pushPromise);
        promisesCount++;
        pushPromiseAvailabilityFuture.notifyHttpListener();
        pushPromiseFuture.notifyHttpListener();
    }

    public void addPushResponse(int streamId, HttpCarbonResponse pushResponse) {
        pushResponsesMap.put(streamId, pushResponse);
        pushResponses.add(pushResponse);
        pushResponsesCount++;
        pushResponseFutures.get(streamId).notifyHttpListener(pushResponse);
    }

    public boolean isAllPromisesReceived() {
        return allPromisesReceived;
    }

    public HttpCarbonResponse getPushResponse(int steamId) {
        return pushResponsesMap.get(steamId);
    }

    public HttpCarbonResponse getResponse() {
        return response;
    }

    public void setResponse(HttpCarbonResponse response) {
        allPromisesReceived = true;
        pushPromiseAvailabilityFuture.notifyHttpListener();
        responseFuture.notifyHttpListener(response);
        this.response = response;
    }

    public boolean hasPromise() {
        return !promises.isEmpty();
    }

    public boolean hasPushResponse() {
        return !pushResponses.isEmpty();
    }

    public Http2PushPromise getNextPromise() {
        return promises.poll();
    }

    public HttpCarbonResponse getNextPushResponse() {
        return pushResponses.poll();
    }

    public int getPromisesCount() {
        return promisesCount;
    }

    public int getPushResponsesCount() {
        return pushResponsesCount;
    }

    public HttpResponseFuture getPushResponseFuture(Http2PushPromise promise) {
        HttpResponseFuture pushResponseFuture = new DefaultHttpResponseFuture();
        pushResponseFutures.put(promise.getPromisedStreamId(), pushResponseFuture);
        return pushResponseFuture;
    }

}
