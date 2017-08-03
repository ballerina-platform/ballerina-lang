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

import org.wso2.carbon.connector.framework.websocket.WebSocketChannelContext;
import org.wso2.carbon.connector.framework.websocket.WebSocketSessionContext;

import java.util.LinkedList;
import java.util.List;
import javax.websocket.Session;

/**
 * WebSocket channel handler context with Session context.
 */
public class WebSocketChannelContextImpl extends BasicWebSocketChannelContextImpl implements WebSocketSessionContext {

    protected final Session serverSession;
    protected final List<Session> clientSessions = new LinkedList<>();
    protected final BasicWebSocketChannelContextImpl channelContext;

    public WebSocketChannelContextImpl(Session serverSession, BasicWebSocketChannelContextImpl channelContext) {
        super(channelContext.subProtocol, channelContext.target, channelContext.listenerPort,
              channelContext.isConnectionSecured, channelContext.isServerMessage, channelContext.connectionManager,
              channelContext.listenerConfiguration);
        this.serverSession = serverSession;
        this.channelContext = channelContext;
    }

    public void addClientSession(Session clientSession) {
        clientSessions.add(clientSession);
    }

    public BasicWebSocketChannelContextImpl getChannelContext() {
        return channelContext;
    }

    @Override
    public Session getServerSession() {
        return serverSession;
    }

    @Override
    public List<Session> getClientSessions() {
        return clientSessions;
    }
}
