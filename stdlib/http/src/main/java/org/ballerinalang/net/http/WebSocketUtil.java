/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.observability.metrics.DefaultMetricRegistry;
import org.ballerinalang.jvm.observability.metrics.MetricId;
import org.ballerinalang.jvm.observability.metrics.MetricRegistry;
import org.ballerinalang.jvm.observability.metrics.Tag;
import org.ballerinalang.jvm.observability.metrics.Tags;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.net.http.exception.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.SERVER_CONNECTOR_WEBSOCKET;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.WebSocketConstants.ErrorCode.WsInvalidHandshakeError;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_CONNECTIONS;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_CONNECTIONS_DESC;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_ERRORS;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_ERRORS_DESC;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_MESSAGES_RECEIVED;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_MESSAGES_RECEIVED_DESC;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_MESSAGES_SENT;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_MESSAGES_SENT_DESC;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_REQUESTS;
import static org.ballerinalang.net.http.WebSocketConstants.METRIC_REQUESTS_DESC;
import static org.ballerinalang.net.http.WebSocketConstants.TAG_CONNECTION_ID;
import static org.ballerinalang.net.http.WebSocketConstants.TAG_KEY_RESULT;
import static org.ballerinalang.net.http.WebSocketConstants.TAG_MESSAGE_TYPE;
import static org.ballerinalang.net.http.WebSocketConstants.TAG_SERVICE;
import static org.ballerinalang.net.http.WebSocketConstants.WEBSOCKET_ERROR_DETAILS;

/**
 * Utility class for WebSocket.
 */
public class WebSocketUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketUtil.class);

    static MapValue getServiceConfigAnnotation(ObjectValue service) {
        return (MapValue) service.getType().getAnnotation(HttpConstants.PROTOCOL_PACKAGE_HTTP,
                WebSocketConstants.WEBSOCKET_ANNOTATION_CONFIGURATION);
    }

    public static void handleHandshake(WebSocketService wsService, WebSocketConnectionManager connectionManager,
                                       HttpHeaders headers, WebSocketHandshaker webSocketHandshaker,
                                       NonBlockingCallback callback) {
        String[] subProtocols = wsService.getNegotiableSubProtocols();
        int idleTimeoutInSeconds = wsService.getIdleTimeoutInSeconds();
        int maxFrameSize = wsService.getMaxFrameSize();
        ServerHandshakeFuture future = webSocketHandshaker.handshake(subProtocols, idleTimeoutInSeconds * 1000, headers,
                maxFrameSize);

        future.setHandshakeListener(new ServerHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection) {

                ObjectValue webSocketEndpoint = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID,
                        WebSocketConstants.WEBSOCKET_CALLER);
                ObjectValue webSocketConnector = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID,
                        WebSocketConstants.WEBSOCKET_CONNECTOR);

                webSocketEndpoint.set(WebSocketConstants.LISTENER_CONNECTOR_FIELD, webSocketConnector);
                populateEndpoint(webSocketConnection, webSocketEndpoint);
                WebSocketOpenConnectionInfo connectionInfo =
                        new WebSocketOpenConnectionInfo(wsService, webSocketConnection, webSocketEndpoint);
                connectionManager.addConnection(webSocketConnection.getChannelId(), connectionInfo);
                webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO,
                        connectionInfo);
                if (callback != null) {
                    callback.setReturnValues(webSocketEndpoint);
                    callback.notifySuccess();
                } else {
                    AttachedFunction onOpenResource = wsService.getResourceByName(
                            WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                    if (onOpenResource != null) {
                        executeOnOpenResource(wsService, onOpenResource, webSocketEndpoint,
                                              webSocketConnection);
                    } else {
                        readFirstFrame(webSocketConnection, webSocketConnector);
                    }
                }
                observeConnection(connectionInfo);

            }

            @Override
            public void onError(Throwable throwable) {
                if (callback != null) {
                    callback.notifyFailure(new WebSocketException(WsInvalidHandshakeError,
                                                                  "Unable to complete handshake:" +
                                                                          throwable.getMessage()));
                } else {
                    throw new WebSocketException(WsInvalidHandshakeError, "Unable to complete handshake");
                }
                logger.error("Unable to complete handshake", throwable);
            }

        });
    }

    static void executeOnOpenResource(WebSocketService wsService, AttachedFunction onOpenResource,
                                             ObjectValue webSocketEndpoint, WebSocketConnection webSocketConnection) {
        BType[] parameterTypes = onOpenResource.getParameterType();
        Object[] bValues = new Object[parameterTypes.length * 2];
        bValues[0] = webSocketEndpoint;
        bValues[1] = true;
        ObjectValue webSocketConnector =
                (ObjectValue) webSocketEndpoint.get(WebSocketConstants.LISTENER_CONNECTOR_FIELD);

        CallableUnitCallback onOpenCallableUnitCallback = new CallableUnitCallback() {
            @Override
            public void notifySuccess() {
                boolean isReady = (boolean) webSocketConnector.get(WebSocketConstants.CONNECTOR_IS_READY_FIELD);
                if (!isReady) {
                    readFirstFrame(webSocketConnection, webSocketConnector);
                }
            }

            @Override
            public void notifyFailure(ErrorValue error) {
                boolean isReady = (boolean) webSocketConnector.get(WebSocketConstants.CONNECTOR_IS_READY_FIELD);
                if (!isReady) {
                    readFirstFrame(webSocketConnection, webSocketConnector);
                }
                ErrorHandlerUtils.printError("error: " + error.getPrintableStackTrace());
                closeDuringUnexpectedCondition(webSocketConnection);
            }
        };

        Executor.submit(wsService.getScheduler(), wsService.getBalService(), onOpenResource.getName(),
                onOpenCallableUnitCallback,
                null, bValues);
    }

    static void populateEndpoint(WebSocketConnection webSocketConnection, ObjectValue webSocketEndpoint) {
        webSocketEndpoint.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        String negotiatedSubProtocol = webSocketConnection.getNegotiatedSubProtocol();
        webSocketEndpoint.set(WebSocketConstants.LISTENER_NEGOTIATED_SUBPROTOCOLS_FIELD, negotiatedSubProtocol);
        webSocketEndpoint.set(WebSocketConstants.LISTENER_IS_SECURE_FIELD, webSocketConnection.isSecure());
        webSocketEndpoint.set(WebSocketConstants.LISTENER_IS_OPEN_FIELD, webSocketConnection.isOpen());
    }

    public static void handleWebSocketCallback(NonBlockingCallback callback,
                                               ChannelFuture webSocketChannelFuture, Logger log) {
        webSocketChannelFuture.addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                log.error("Error occurred ", cause);
                callback.setReturnValues(new WebSocketException(cause));

            } else {
                callback.setReturnValues(null);
            }
            callback.notifySuccess();
        });
    }

    public static void readFirstFrame(WebSocketConnection webSocketConnection, ObjectValue webSocketConnector) {
        webSocketConnection.readNextFrame();
        webSocketConnector.set(WebSocketConstants.CONNECTOR_IS_READY_FIELD, true);
    }

    /**
     * Closes the connection with the unexpected failure status code.
     *
     * @param webSocketConnection the websocket connection to be closed.
     */
    static void closeDuringUnexpectedCondition(WebSocketConnection webSocketConnection) {
        webSocketConnection.terminateConnection(1011, "Unexpected condition");

    }

    public static void setListenerOpenField(WebSocketOpenConnectionInfo connectionInfo) throws IllegalAccessException {
        connectionInfo.getWebSocketEndpoint().set(WebSocketConstants.LISTENER_IS_OPEN_FIELD,
                connectionInfo.getWebSocketConnection().isOpen());
    }

    public static int findMaxFrameSize(MapValue<String, Object> annotation) {
        long size = annotation.getIntValue(WebSocketConstants.ANNOTATION_ATTR_MAX_FRAME_SIZE);
        if (size <= 0) {
            return WebSocketConstants.DEFAULT_MAX_FRAME_SIZE;
        }
        try {
            return Math.toIntExact(size);
        } catch (ArithmeticException e) {
            logger.warn("The value set for maxFrameSize needs to be less than " + Integer.MAX_VALUE +
                    ". The maxFrameSize value is set to " + Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }

    }

    public static int findIdleTimeoutInSeconds(MapValue<String, Object> annAttrIdleTimeout) {
        long timeout = annAttrIdleTimeout.getIntValue(WebSocketConstants.ANNOTATION_ATTR_IDLE_TIMEOUT);
        if (timeout <= 0) {
            return 0;
        }
        try {
            return Math.toIntExact(timeout);
        } catch (ArithmeticException e) {
            logger.warn("The value set for idleTimeoutInSeconds needs to be less than" + Integer.MAX_VALUE +
                    ". The idleTimeoutInSeconds value is set to " + Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }

    public static String[] findNegotiableSubProtocols(MapValue<String, Object> annAttrSubProtocols) {
        String[] subProtocolsInAnnotation = annAttrSubProtocols.getArrayValue(
                WebSocketConstants.ANNOTATION_ATTR_SUB_PROTOCOLS).getStringArray();
        if (subProtocolsInAnnotation == null) {
            return new String[0];
        }
        return Arrays.stream(subProtocolsInAnnotation).map(Object::toString)
                .toArray(String[]::new);
    }

    public static String getErrorMessage(Throwable err) {
        if (err.getMessage() == null) {
            return "Unexpected error occurred";
        }
        return err.getMessage();
    }

    public static MapValue<String, Object> createDetailRecord(String errMsg) {
        MapValue<String, Object> detail = BallerinaValues.createRecordValue(PROTOCOL_HTTP_PKG_ID,
                WEBSOCKET_ERROR_DETAILS);
        // The cause is null here. When there is a cause override the method to pass the proper cause
        return BallerinaValues.createRecord(detail, errMsg, null);
    }

    public static void observeRequest(WebSocketOpenConnectionInfo connectionInfo, String result) {
        if (ObserveUtils.isObservabilityEnabled()) {
            ObserverContext observerContext = new ObserverContext();
            Map<String, Object> properties = new HashMap<>();
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);

            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);
            // TODO: extract span context as a map and add to the observer context
            try {
                observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: Handle Exception
            }
            observerContext.addTag(TAG_SERVICE, connectionInfo.getService().getBasePath());
            observerContext.addTag(TAG_KEY_RESULT, result);
            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();
            metricRegistry.counter(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_REQUESTS,
                                                METRIC_REQUESTS_DESC, allTags)).increment();
        }


    }

    public static void observeConnection(WebSocketOpenConnectionInfo connectionInfo) {
        if (ObserveUtils.isObservabilityEnabled()) {

            ObserverContext observerContext = new ObserverContext();
            Map<String, Object> properties = new HashMap<>();
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);

            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);

            try {
                observerContext.addTag(TAG_CONNECTION_ID,
                                       connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: handle exception
            }
            observerContext.addTag(TAG_SERVICE, connectionInfo.getService().getBasePath());

            // TODO: extract span context as a map and add to the observer context
            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();
            metricRegistry.gauge(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_CONNECTIONS,
                                              METRIC_CONNECTIONS_DESC, allTags)).increment();

        }
    }

    public static void observePush(String type, String result, WebSocketOpenConnectionInfo connectionInfo) {
        if (ObserveUtils.isObservabilityEnabled()) {

            ObserverContext observerContext = new ObserverContext();
            Map<String, Object> properties = new HashMap<>();
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
            observerContext.addTag(TAG_MESSAGE_TYPE, type);
            observerContext.addTag(TAG_KEY_RESULT, result);
            try {
                observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: Handle exception
            }
            observerContext.addTag(TAG_SERVICE, connectionInfo.getService().getBasePath());

            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);
            // TODO: extract span context as a map and add to the observer context

            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();
            metricRegistry.counter(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_MESSAGES_SENT,
                                                METRIC_MESSAGES_SENT_DESC, allTags)).increment();

        }
    }

    public static void observeOnMessage(String type, WebSocketOpenConnectionInfo connectionInfo) {
        ObserverContext observerContext = new ObserverContext();
        Map<String, Object> properties = new HashMap<>();
        properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
        observerContext.addTag(TAG_MESSAGE_TYPE, type);
        try {
            observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
        } catch (IllegalAccessException e) {
            //TODO: Handle Exception
        }
        observerContext.addTag(TAG_SERVICE, connectionInfo.getService().getBasePath());

        Map<String, String> tags = observerContext.getTags();
        Set<Tag> allTags = new HashSet<>(tags.size());
        Tags.tags(allTags, observerContext.getTags());

        MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();

        metricRegistry.counter(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_MESSAGES_RECEIVED,
                                            METRIC_MESSAGES_RECEIVED_DESC, allTags)).increment();
    }

    public static void observeClose(WebSocketOpenConnectionInfo connectionInfo) {
        ObserverContext observerContext = new ObserverContext();
        Map<String, Object> properties = new HashMap<>();
        properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
        if (ObserveUtils.isObservabilityEnabled()) {
            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);

            try {
                observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: Handle Exception
            }

            observerContext.addTag(TAG_SERVICE, connectionInfo.getService().getBasePath());

            // TODO: extract span context as a map and add to the observer context
            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();
            metricRegistry.gauge(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_CONNECTIONS,
                                              METRIC_CONNECTIONS_DESC, allTags)).decrement();
        }
    }

    public static void observeError(WebSocketOpenConnectionInfo connectionInfo) {
        ObserverContext observerContext = new ObserverContext();
        Map<String, Object> properties = new HashMap<>();
        properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
        if (ObserveUtils.isObservabilityEnabled()) {
            observerContext.setConnectorName(SERVER_CONNECTOR_WEBSOCKET);

            try {
                observerContext.addTag(TAG_CONNECTION_ID, connectionInfo.getWebSocketConnection().getChannelId());
            } catch (IllegalAccessException e) {
                //TODO: Handle Exception
            }

            observerContext.addTag(TAG_SERVICE, connectionInfo.getService().getBasePath());

            // TODO: extract span context as a map and add to the observer context
            Map<String, String> tags = observerContext.getTags();
            Set<Tag> allTags = new HashSet<>(tags.size());
            Tags.tags(allTags, observerContext.getTags());

            MetricRegistry metricRegistry = DefaultMetricRegistry.getInstance();
            metricRegistry.counter(new MetricId(SERVER_CONNECTOR_WEBSOCKET + "_" + METRIC_ERRORS,
                                              METRIC_ERRORS_DESC, allTags)).increment();
        }
    }


    private WebSocketUtil() {
    }
}
