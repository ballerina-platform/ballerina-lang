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

package org.wso2.transport.http.netty.contractimpl.websocket;

import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;

/**
 * Implementation of {@link WebSocketMessage}.
 */
public class DefaultWebSocketMessage implements WebSocketMessage {

    protected String target;
    protected boolean isServerMessage;
    protected WebSocketConnection webSocketConnection;

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String getTarget() {
        return target;
    }

    public void setIsServerMessage(boolean isServerMessage) {
        this.isServerMessage = isServerMessage;
    }

    @Override
    public boolean isServerMessage() {
        return isServerMessage;
    }

    public void setWebSocketConnection(WebSocketConnection webSocketConnection) {
        this.webSocketConnection = webSocketConnection;
    }

    @Override
    public WebSocketConnection getWebSocketConnection() {
        return webSocketConnection;
    }
}
