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

import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.scheduling.Scheduler;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
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

    public static Object ready(BObject wsConnector) {
        WebSocketConnectionInfo connectionInfo = (WebSocketConnectionInfo) wsConnector
                    .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
        WebSocketObservabilityUtil.observeResourceInvocation(Scheduler.getStrand(), connectionInfo,
                WebSocketConstants.RESOURCE_NAME_READY);
        try {
            boolean isReady = wsConnector.getBooleanValue(WebSocketConstants.CONNECTOR_IS_READY_FIELD);
            if (!isReady) {
                WebSocketUtil.readFirstFrame(connectionInfo.getWebSocketConnection(), wsConnector);
                connectionInfo.getWebSocketEndpoint().getMapValue(WebSocketConstants.CLIENT_ENDPOINT_CONFIG).
                        put(WebSocketConstants.CLIENT_READY_ON_CONNECT, true);
            } else {
                return WebSocketUtil.getWebSocketException("Already started reading frames", null,
                        WebSocketConstants.ErrorCode.WsGenericError.errorCode(), null);
            }
        } catch (Exception e) {
            log.error("Error occurred when calling ready", e);
            WebSocketObservabilityUtil.observeError(connectionInfo, WebSocketObservabilityConstants.ERROR_TYPE_READY,
                                                    e.getMessage());
            return WebSocketUtil.createErrorByType(e);
        }
        return null;
    }

    private Ready() {
    }
}
