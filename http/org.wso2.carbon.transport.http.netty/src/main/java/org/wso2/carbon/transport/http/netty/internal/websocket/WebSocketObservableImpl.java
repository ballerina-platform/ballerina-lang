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

package org.wso2.carbon.transport.http.netty.internal.websocket;

import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketObservable;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInboundObserver;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;

/**
 * Implementation for {@link WebSocketObservable}.
 */
public class WebSocketObservableImpl implements WebSocketObservable {

    private WebSocketInboundObserver observer = null;

    @Override
    public void setObserver(WebSocketInboundObserver observer) {
        this.observer = observer;
    }

    @Override
    public void removeObserver(WebSocketInboundObserver observer) {
        this.observer = null;
    }

    @Override
    public void notify(WebSocketInitMessage initMessage) {
        existsWebSocketObserver();
        observer.update(initMessage);
    }

    @Override
    public void notify(WebSocketTextMessage textMessage) {
        existsWebSocketObserver();
        observer.update(textMessage);

    }

    @Override
    public void notify(WebSocketBinaryMessage binaryMessage) {
        existsWebSocketObserver();
        observer.update(binaryMessage);
    }

    @Override
    public void notify(WebSocketControlMessage controlMessage) {
        existsWebSocketObserver();
        observer.update(controlMessage);
    }

    @Override
    public void notify(WebSocketCloseMessage closeMessage) {
        existsWebSocketObserver();
        observer.update(closeMessage);
    }

    @Override
    public void notifyError(Throwable throwable) {
        existsWebSocketObserver();
        observer.handleError(throwable);
    }

    private void existsWebSocketObserver() {
        if (observer == null) {
            throw new UnsupportedOperationException("WebSocket observer is not set.");
        }
    }
}
