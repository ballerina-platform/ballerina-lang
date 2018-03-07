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

import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpResponseFuture;
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

    private Http2ClientChannel http2ClientChannel;

    private BlockingQueue<Http2PushPromise> promises;
    private ConcurrentHashMap<Integer, HttpCarbonResponse> pushResponsesMap;
    private HttpCarbonResponse response;

    private boolean allPromisesReceived = false;

    public OutboundMsgHolder(HTTPCarbonMessage httpCarbonMessage, Http2ClientChannel http2ClientChannel) {
        this.requestCarbonMessage = httpCarbonMessage;
        this.http2ClientChannel = http2ClientChannel;
        promises = new LinkedBlockingQueue();
        pushResponsesMap = new ConcurrentHashMap<>();
        responseFuture = new DefaultHttpResponseFuture(this);
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

    public Http2ClientChannel getHttp2ClientChannel() {
        return http2ClientChannel;
    }

    public void addPromise(Http2PushPromise pushPromise) {
        promises.add(pushPromise);
        responseFuture.notifyPromiseAvailability();
        responseFuture.notifyPushPromise();
    }

    public void addPushResponse(int streamId, HttpCarbonResponse pushResponse) {
        pushResponsesMap.put(streamId, pushResponse);
        responseFuture.notifyPushResponse(streamId, pushResponse);
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
        this.response = response;
        allPromisesReceived = true;
        responseFuture.notifyPromiseAvailability();      // Response received so no more promises allowed
        responseFuture.notifyHttpListener(response);
    }

    public boolean hasPromise() {
        return !promises.isEmpty();
    }

    public Http2PushPromise getNextPromise() {
        return promises.poll();
    }
}
