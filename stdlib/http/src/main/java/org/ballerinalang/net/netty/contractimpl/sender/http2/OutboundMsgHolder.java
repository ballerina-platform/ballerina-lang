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

package org.ballerinalang.net.netty.contractimpl.sender.http2;

import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contractimpl.DefaultHttpResponseFuture;
import org.ballerinalang.net.netty.message.BackPressureObservable;
import org.ballerinalang.net.netty.message.DefaultBackPressureObservable;
import org.ballerinalang.net.netty.message.Http2PushPromise;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.ballerinalang.net.netty.message.HttpCarbonResponse;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code OutboundMsgHolder} holds data related to a single outbound invocation.
 */
public class OutboundMsgHolder {

    // Outbound request HttpCarbonMessage
    private org.ballerinalang.net.netty.message.HttpCarbonMessage requestCarbonMessage;
    private BlockingQueue<org.ballerinalang.net.netty.message.Http2PushPromise> promises;
    private ConcurrentHashMap<Integer, org.ballerinalang.net.netty.message.HttpCarbonResponse> pushResponsesMap;
    private org.ballerinalang.net.netty.message.HttpCarbonResponse response;

    // Future which is used to notify the response listener upon response receive
    private org.ballerinalang.net.netty.contract.HttpResponseFuture responseFuture;
    private Http2ClientChannel http2ClientChannel;

    private boolean allPromisesReceived = false;
    private long lastReadWriteTime;
    private boolean requestWritten;
    private boolean firstContentWritten;
    private AtomicBoolean streamWritable = new AtomicBoolean(true);
    private final org.ballerinalang.net.netty.message.BackPressureObservable
            backPressureObservable = new DefaultBackPressureObservable();

    public OutboundMsgHolder(org.ballerinalang.net.netty.message.HttpCarbonMessage httpOutboundRequest) {
        this.requestCarbonMessage = httpOutboundRequest;
        promises = new LinkedBlockingQueue<>();
        pushResponsesMap = new ConcurrentHashMap<>();
        responseFuture = new DefaultHttpResponseFuture(this);
    }

    public void setHttp2ClientChannel(Http2ClientChannel http2ClientChannel) {
        this.http2ClientChannel = http2ClientChannel;
    }

    /**
     * Gets the outbound request {@code HttpCarbonMessage}.
     *
     * @return request HttpCarbonMessage
     */
    public HttpCarbonMessage getRequest() {
        return requestCarbonMessage;
    }

    /**
     * Gets the Future which is used to notify the response listener upon response receive.
     *
     * @return the Future used to notify the response listener
     */
    public HttpResponseFuture getResponseFuture() {
        return responseFuture;
    }

    /**
     * Gets the associated {@link Http2ClientChannel}.
     *
     * @return the associated Http2ClientChannel
     */
    public Http2ClientChannel getHttp2ClientChannel() {
        return http2ClientChannel;
    }

    /**
     * Adds a {@link org.ballerinalang.net.netty.message.Http2PushPromise} message.
     *
     * @param pushPromise push promise message
     */
    public void addPromise(org.ballerinalang.net.netty.message.Http2PushPromise pushPromise) {
        promises.add(pushPromise);
        responseFuture.notifyPromiseAvailability();
        responseFuture.notifyPushPromise();
    }

    /**
     * Adds a push response message.
     *
     * @param streamId     id of the stream in which the push response received
     * @param pushResponse push response message
     */
    public void addPushResponse(int streamId, org.ballerinalang.net.netty.message.HttpCarbonResponse pushResponse) {
        pushResponsesMap.put(streamId, pushResponse);
        responseFuture.notifyPushResponse(streamId, pushResponse);
    }

    /**
     * Checks whether all push promises received.
     *
     * @return whether all push promises received
     */
    public boolean isAllPromisesReceived() {
        return allPromisesReceived;
    }

    /**
     * Mark no push promises received.
     */
    public void markNoPromisesReceived() {
        allPromisesReceived = true;
        responseFuture.notifyPromiseAvailability();
    }

    /**
     * Gets a push response received over a particular stream.
     *
     * @param steamId id of the stream in which the push response is received
     * @return the push response
     */
    public org.ballerinalang.net.netty.message.HttpCarbonResponse getPushResponse(int steamId) {
        return pushResponsesMap.get(steamId);
    }

    /**
     * Gets the response {@code HttpCarbonResponse} message.
     *
     * @return the response {@code HttpCarbonResponse} message.
     */
    public org.ballerinalang.net.netty.message.HttpCarbonResponse getResponse() {
        return response;
    }

    /**
     * Sets the response {@code HttpCarbonResponse} message.
     *
     * @param response the {@code HttpCarbonResponse} message
     */
    public void setResponse(HttpCarbonResponse response) {
        this.response = response;
        allPromisesReceived = true;
        responseFuture.notifyPromiseAvailability();      // Response received so no more promises allowed
        responseFuture.notifyHttpListener(response);
    }

    /**
     * Checks whether a push promise exists.
     *
     * @return whether a push promise exists
     */
    public boolean hasPromise() {
        return !promises.isEmpty();
    }

    /**
     * Gets the next available push promise.
     *
     * @return next available push promise
     */
    public Http2PushPromise getNextPromise() {
        return promises.poll();
    }

    /**
     * Gets last read or write operation execution time.
     *
     * @return the last read or write operation execution time
     */
    long getLastReadWriteTime() {
        return lastReadWriteTime;
    }

    /**
     * Sets the last read or write operation execution time.
     *
     * @param lastReadWriteTime the last read or write operation execution time
     */
    void setLastReadWriteTime(long lastReadWriteTime) {
        this.lastReadWriteTime = lastReadWriteTime;
    }

    /**
     * Checks whether the request is written.
     *
     * @return whether the request is written
     */
    boolean isRequestWritten() {
        return requestWritten;
    }

    /**
     * Sets request is completely written.
     *
     * @param requestWritten whether request is written
     */
    public void setRequestWritten(boolean requestWritten) {
        this.requestWritten = requestWritten;
    }

    boolean isFirstContentWritten() {
        return firstContentWritten;
    }

    void setFirstContentWritten(boolean firstContentWritten) {
        this.firstContentWritten = firstContentWritten;
    }

    boolean isStreamWritable() {
        return streamWritable.get();
    }

    public void setStreamWritable(boolean streamWritable) {
        this.streamWritable.set(streamWritable);
    }

    public BackPressureObservable getBackPressureObservable() {
        return backPressureObservable;
    }
}
