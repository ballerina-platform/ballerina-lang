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
 */

package org.ballerinalang.services.dispatchers.ws;

import org.ballerinalang.util.codegen.ServiceInfo;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;

/**
 * Ballerina Connector listener for WebSocket.
 */
public class BallerinaWSServerConnectorListener implements WebSocketConnectorListener {
    @Override
    public void onMessage(WebSocketInitMessage webSocketInitMessage) {

    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {

    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {

    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {

    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {

    }

    @Override
    public void onError(Throwable throwable) {

    }
}
