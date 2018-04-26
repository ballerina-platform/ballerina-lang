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

package org.wso2.transport.http.netty.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

/**
 * WebSocket connector listener to identify the properties of a message.
 */
public class WebSocketMessagePropertiesConnectorListener implements WebSocketConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessagePropertiesConnectorListener.class);

    @Override
    public void onMessage(WebSocketInitMessage initMessage) {
        // Assert properties
        Assert.assertFalse(initMessage.isConnectionSecured());
        Assert.assertEquals(initMessage.getTarget(), "/test");

        // Assert custom headers
        String checkSubProtocol = initMessage.getHeader("check-sub-protocol");
        Assert.assertEquals(initMessage.getHeader("message-type"), "websocket");
        Assert.assertEquals(initMessage.getHeader("message-sender"), "wso2");
        if ("true".equals(checkSubProtocol)) {
            String[] subProtocols = {"xml"};
            initMessage.handshake(subProtocols, true).setHandshakeListener(new HandshakeListener() {
                @Override
                public void onSuccess(WebSocketConnection webSocketConnection) {
                    webSocketConnection.startReadingFrames();
                }

                @Override
                public void onError(Throwable t) {
                    Assert.assertTrue(false, t.getMessage());
                }
            });
        }

    }

    @Override
    public void onMessage(WebSocketTextMessage textMessage) {
        // Assert properties
        Assert.assertEquals(textMessage.getSubProtocol(), "xml");
        Assert.assertFalse(textMessage.isConnectionSecured());
        Assert.assertEquals(textMessage.getTarget(), "/test");

        // Assert custom headers
        Assert.assertEquals(textMessage.getHeader("message-type"), "websocket");
        Assert.assertEquals(textMessage.getHeader("message-sender"), "wso2");
    }

    @Override
    public void onMessage(WebSocketBinaryMessage binaryMessage) {
        // Do nothing.
    }

    @Override
    public void onMessage(WebSocketControlMessage controlMessage) {
        // Do nothing.
    }

    @Override
    public void onMessage(WebSocketCloseMessage closeMessage) {
        // Do nothing.
    }

    @Override
    public void onError(Throwable throwable) {
        // Do nothing.
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        // Do nothing.
    }
}
