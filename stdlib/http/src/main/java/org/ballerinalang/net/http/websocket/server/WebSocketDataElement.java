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

package org.ballerinalang.net.http.websocket.server;

import org.ballerinalang.net.http.websocket.WebSocketException;
import org.ballerinalang.net.uri.parser.DataElement;
import org.ballerinalang.net.uri.parser.DataReturnAgent;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;

/**
 * Data element for WebSocket URI template.
 */
public class WebSocketDataElement implements DataElement<WebSocketServerService, WebSocketMessage> {

    private WebSocketServerService webSocketService;
    private boolean isFirstTraverse = true;

    @Override
    public boolean hasData() {
        return true;
    }

    @Override
    public void setData(WebSocketServerService webSocketService) {
        if (isFirstTraverse && webSocketService == null) {
            throw new WebSocketException("Service has not been registered");
        }
        if (isFirstTraverse) {
            isFirstTraverse = false;
            this.webSocketService = webSocketService;
        } else if (webSocketService == null) {
            isFirstTraverse = true;
            this.webSocketService = null;
        } else {
            throw new WebSocketException("Two services have the same addressable URI");
        }
    }

    @Override
    public boolean getData(WebSocketMessage inboundMessage, DataReturnAgent<WebSocketServerService> dataReturnAgent) {
        if (webSocketService == null) {
            return false;
        }
        dataReturnAgent.setData(webSocketService);
        return true;
    }
}
