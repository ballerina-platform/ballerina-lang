/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.observability;

import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.client.FailoverContext;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.ballerinalang.net.http.websocket.server.WebSocketServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Providing observability functionality to WebSockets (Logging and Metrics).
 *
 * @since 1.1.0
 */
public class WebSocketObservabilityUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketObservabilityUtil.class);

    /**
     * Observes successful WebSocket connections.
     *
     * @param connectionInfo information regarding connection.
     */
    public static void observeConnection(WebSocketConnectionInfo connectionInfo) {
        WebSocketObserverContext observerContext = new WebSocketObserverContext(connectionInfo);
        WebSocketMetricsUtil.reportConnectionMetrics(observerContext);

        LOGGER.debug("WebSocket new connection established. connectionID: {}, service/url: {}",
                     observerContext.getConnectionId(), observerContext.getServicePathOrClientUrl());
    }

    /**
     * Observes messages pushed (sent).
     *
     * @param type           type of message pushed (text, binary, control, close).
     * @param connectionInfo information regarding connection.
     */
    public static void observeSend(String type, WebSocketConnectionInfo connectionInfo) {
        WebSocketObserverContext observerContext = new WebSocketObserverContext(connectionInfo);
        WebSocketMetricsUtil.reportSendMetrics(observerContext, type);

        LOGGER.debug("WebSocket message sent. connectionID: {}, service/url: {}, type: {}",
                     observerContext.getConnectionId(), observerContext.getServicePathOrClientUrl(), type);
    }

    /**
     * Observes messages received.
     *
     * @param type           type of message pushed (text, binary, control, close).
     * @param connectionInfo information regarding connection.
     */
    public static void observeOnMessage(String type, WebSocketConnectionInfo connectionInfo) {
        WebSocketObserverContext observerContext = new WebSocketObserverContext(connectionInfo);
        WebSocketMetricsUtil.reportReceivedMetrics(observerContext, type);

        LOGGER.debug("WebSocket message received. connectionID: {}, service/url: {}, type:{}",
                     observerContext.getConnectionId(), observerContext.getServicePathOrClientUrl(), type);
    }

    /**
     * Observes WebSocket connection closures.
     *
     * @param connectionInfo information regarding connection.
     */
    public static void observeClose(WebSocketConnectionInfo connectionInfo) {
        WebSocketObserverContext observerContext = new WebSocketObserverContext(connectionInfo);
        WebSocketMetricsUtil.reportCloseMetrics(observerContext);

        LOGGER.debug("WebSocket connection closed. connectionID: {}, service/url: {}",
                     observerContext.getConnectionId(), observerContext.getServicePathOrClientUrl());
    }

    /**
     * Observes WebSocket errors where the errorType is not related to a message being sent or received, or the type of
     * the message is unknown.
     *
     * @param connectionInfo information regarding connection.
     * @param errorType      type of error (connection, closure, message sent/received).
     * @param errorMessage   error message.
     */
    public static void observeError(WebSocketConnectionInfo connectionInfo, String errorType,
                                    String errorMessage) {
        observeError(connectionInfo, errorType, null, errorMessage);
    }

    /**
     * Observes WebSocket errors where the errorType is related to a message being sent or received. and the type of the
     * message is known.
     *
     * @param connectionInfo information regarding connection.
     * @param errorType      type of error (connection, closure, message sent/received).
     * @param messageType    type of message (text, binary, control, close).
     * @param errorMessage   error message.
     */
    public static void observeError(WebSocketConnectionInfo connectionInfo, String errorType,
                                    String messageType, String errorMessage) {
        WebSocketObserverContext observerContext = new WebSocketObserverContext(connectionInfo);
        WebSocketMetricsUtil.reportErrorMetrics(observerContext, errorType, messageType);

        if (errorMessage == null) {
            errorMessage = WebSocketObservabilityConstants.UNKNOWN;
        }

        if (messageType == null) {
            LOGGER.debug("WebSocket type:{}, message: {}, connectionId: {}, service/url: {}",
                         errorType, errorMessage, observerContext.getConnectionId(),
                         observerContext.getServicePathOrClientUrl());
        } else {
            LOGGER.debug("WebSocket type:{}/{}, message: {}, connectionId: {}, service/url: {}",
                         errorType, messageType, errorMessage, observerContext.getConnectionId(),
                         observerContext.getServicePathOrClientUrl());
        }
    }

    /**
     * Observes WebSocket errors before the connection is initialized (handshake errors etc).
     *
     * @param errorType      type of error (connection, closure, message sent/received).
     * @param errorMessage   error message.
     * @param url            service base path or remote url.
     * @param clientOrServer current context of connection.
     */
    public static void observeError(String errorType, String errorMessage, String url, String clientOrServer) {
        WebSocketMetricsUtil.reportErrorMetrics(errorType, url, clientOrServer);

        LOGGER.debug("WebSocket type:{}, message: {}, service/url: {}", errorType, errorMessage, url);
    }

    /**
     * Observes when a new resource is invoked. In addition to metrics and logging, relevant tags are added to the
     * span in the trace as well.
     *
     * @param strand            current strand
     * @param connectionInfo    information regarding the connection.
     * @param resource          name of the resource invoked.
     */
    public static void observeResourceInvocation(Strand strand, WebSocketConnectionInfo connectionInfo,
                                                 String resource) {
        WebSocketTracingUtil.traceResourceInvocation(strand, connectionInfo);
        observeResourceInvocation(connectionInfo, resource);
    }

    /**
     * Observes when a new resource is invoked.
     *
     * @param connectionInfo    information regarding the connection.
     * @param resource          name of the resource invoked.
     */
    public static void observeResourceInvocation(WebSocketConnectionInfo connectionInfo, String resource) {
        WebSocketObserverContext observerContext = new WebSocketObserverContext(connectionInfo);
        WebSocketMetricsUtil.reportResourceInvocationMetrics(observerContext, resource);

        LOGGER.debug("WebSocket resource invoked. connectionID: {}, service/url: {}, resource: {}",
                     observerContext.getConnectionId(),
                     observerContext.getServicePathOrClientUrl(),
                     resource);
    }

    /**
     * Determines whether the current context is server or client.
     *
     * @param connectionInfo information regarding connection.
     * @return whether the current context is client or server.
     */
    static String getClientOrServerContext(WebSocketConnectionInfo connectionInfo) {
        WebSocketService service = connectionInfo.getService();
        if (service instanceof WebSocketServerService) {
            return WebSocketObservabilityConstants.CONTEXT_SERVER;
        } else {
            return WebSocketObservabilityConstants.CONTEXT_CLIENT;
        }
    }

    /**
     * Determines whether the current context is server or client and returns the relevant base bath (if server) or
     * remote URL (if client).
     *
     * @param connectionInfo information of the current connection.
     * @return if server context, service base path else (in client context), the remote URL
     */
    static String getServicePathOrClientUrl(WebSocketConnectionInfo connectionInfo) {
        WebSocketService service = connectionInfo.getService();
        if (service instanceof WebSocketServerService) {
            return ((WebSocketServerService) service).getBasePath();
        } else {
            if (WebSocketUtil.isFailoverClient(connectionInfo.getWebSocketEndpoint())) {
                FailoverContext failoverConfig = (FailoverContext) connectionInfo.getWebSocketEndpoint().
                        getNativeData(WebSocketConstants.FAILOVER_CONTEXT);
                return failoverConfig.getTargetUrls().get(failoverConfig.getCurrentIndex());
            } else {
                return connectionInfo.getWebSocketEndpoint().getStringValue(WebSocketConstants.CLIENT_URL_CONFIG)
                        .getValue();
            }
        }
    }

    public static WebSocketConnectionInfo getConnectionInfo(BObject wsConnection) {
        return (WebSocketConnectionInfo) wsConnection.getNativeData(
                WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
    }

    static String getConnectionId(WebSocketConnectionInfo connectionInfo) {
        try {
            return connectionInfo.getWebSocketConnection().getChannelId();
        } catch (Exception e) {
            return WebSocketObservabilityConstants.UNKNOWN;
        }
    }

    private WebSocketObservabilityUtil() {
    }
}
