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

import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketMessageSessionContext;

import java.util.LinkedList;
import java.util.List;
import javax.websocket.Session;

/**
 * WebSocket channel handler context with Session context.
 */
public class WebSocketMessageContextImpl extends BasicWebSocketMessageContextImpl implements
                                                                                  WebSocketMessageSessionContext {

    protected Session serverSession = null;
    protected final Session channelSession;
    protected final List<Session> clientSessions = new LinkedList<>();
    protected final BasicWebSocketMessageContextImpl channelContext;

    public WebSocketMessageContextImpl(Session channelSession, BasicWebSocketMessageContextImpl channelContext) {
        super(channelContext.subProtocol, channelContext.target, channelContext.listenerPort,
              channelContext.isConnectionSecured, channelContext.isServerMessage, channelContext.connectionManager,
              channelContext.listenerConfiguration);
        this.channelSession = channelSession;
        this.channelContext = channelContext;
    }

    public void addClientSession(Session clientSession) {
        clientSessions.add(clientSession);
    }

    public void setServerSession(Session serverSession) {
        this.serverSession = serverSession;
    }

    public BasicWebSocketMessageContextImpl getChannelContext() {
        return channelContext;
    }

    @Override
    public Session getChannelSession() {
        return channelSession;
    }

    @Override
    public Session getServerSession() {
        if (isServerMessage) {
            return channelSession;
        }
        return serverSession;
    }

    @Override
    public List<Session> getClientSessions() {
        if (isServerMessage) {
            return clientSessions;
        }
        throw new UnsupportedOperationException("Only applicable for server messages");
    }
}
