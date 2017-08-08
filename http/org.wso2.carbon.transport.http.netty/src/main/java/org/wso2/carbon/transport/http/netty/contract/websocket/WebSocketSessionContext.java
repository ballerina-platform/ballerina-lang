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

package org.wso2.carbon.transport.http.netty.contract.websocket;

import java.util.List;
import javax.websocket.Session;

/**
 * This interface represents the necessary methods for WebSocket context.
 * <b>Note: Use this interface in the application level only and only if the user needs only the session details
 * of a WebSocket message otherwise use the extensions of this interface.</b>
 */
public interface WebSocketSessionContext {

    /**
     * Retrieve the server session of the connection.
     *
     * @return the server session of the connection.
     */
    Session getChannelSession();

    /**
     * Retrieve the client session array relates to the listener channel.
     *
     * @return the client session array relates to the listener channel.
     */
    List<Session> getClientSessions();
}
