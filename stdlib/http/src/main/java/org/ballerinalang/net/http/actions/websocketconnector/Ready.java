/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.actions.websocketconnector;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketException;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityConstants;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
public class Ready {
    private static final Logger log = LoggerFactory.getLogger(Ready.class);

    public static Object ready(ObjectValue wsClient) {
        ObjectValue wsConnection = (ObjectValue) wsClient.get(WebSocketConstants.CLIENT_CONNECTOR_FIELD);
        WebSocketConnectionInfo connectionInfo = (WebSocketConnectionInfo) wsConnection
                .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
        WebSocketObservabilityUtil.observeResourceInvocation(Scheduler.getStrand(), connectionInfo,
                                                             WebSocketConstants.RESOURCE_NAME_READY);
        try {
            boolean isReady = wsClient.getBooleanValue(WebSocketConstants.CONNECTOR_IS_READY_FIELD);
            if (!isReady) {
                WebSocketUtil.readFirstFrame(connectionInfo.getWebSocketConnection(), wsClient);
            } else {
                return new WebSocketException("Already started reading frames");
            }
        } catch (Exception e) {
            log.error("Error occurred when calling ready", e);
            WebSocketObservabilityUtil.observeError(WebSocketObservabilityUtil.getConnectionInfo(wsConnection),
                                                    WebSocketObservabilityConstants.ERROR_TYPE_READY,
                                                    e.getMessage());
            return WebSocketUtil.createErrorByType(e);
        }
        return null;
    }

    private Ready() {
    }
}
