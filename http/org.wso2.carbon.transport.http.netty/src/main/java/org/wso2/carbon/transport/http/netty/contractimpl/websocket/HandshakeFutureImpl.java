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

package org.wso2.carbon.transport.http.netty.contractimpl.websocket;

import io.netty.channel.ChannelFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.HandshakeListener;

import javax.websocket.Session;

/**
 * Implementation of WebSocket handshake future.
 */
public class HandshakeFutureImpl implements HandshakeFuture {

    private Throwable throwable = null;
    private Session session = null;
    private HandshakeListener handshakeListener;
    private ChannelFuture channelFuture;
    private boolean isSync = false;

    public HandshakeFutureImpl() {
    }

    public void setChannelFuture(ChannelFuture channelFuture) throws InterruptedException {
        this.channelFuture = channelFuture;
        if (isSync) {
            this.channelFuture.sync();
        }
    }

    @Override
    public HandshakeFuture setHandshakeListener(HandshakeListener handshakeListener) {
        this.handshakeListener = handshakeListener;
        if (throwable != null) {
            handshakeListener.onError(throwable);
        }
        if (session != null) {
            handshakeListener.onSuccess(session);
        }
        return this;
    }

    @Override
    public void notifySuccess(Session session) {
        this.session = session;
        if (handshakeListener == null || throwable != null) {
            return;
        }
        handshakeListener.onSuccess(session);
    }

    @Override
    public void notifyError(Throwable throwable) {
        this.throwable = throwable;
        if (handshakeListener == null) {
            return;
        }
        handshakeListener.onError(throwable);
    }

    @Override
    public void sync() throws InterruptedException {
        isSync = true;
        if (channelFuture != null) {
            channelFuture.sync();
        }
    }


}
