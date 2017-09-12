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

package org.ballerinalang.nativeimpl.actions.ws;

import org.ballerinalang.bre.Context;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.services.dispatchers.ws.Constants;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.List;
import javax.websocket.Session;

/**
 * Abstract class for WebSocket native actions.
 */
public abstract class AbstractWebSocketAction extends AbstractNativeAction {

    protected Session getServerSession(Context context) {
        CarbonMessage carbonMessage = context.getCarbonMessage();
        return (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
    }

    protected List<Session> getClientSessions(Context context) {
        CarbonMessage carbonMessage = context.getCarbonMessage();
        return (List<Session>) carbonMessage.getProperty(Constants.WEBSOCKET_CLIENT_SESSIONS_LIST);
    }
}
