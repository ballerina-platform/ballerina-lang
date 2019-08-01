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
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.net.http.exception.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.WebSocketConstants.BACK_OF_FACTOR;
import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.http.WebSocketConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTED_SERVER_URLS_INDEX;
import static org.ballerinalang.net.http.WebSocketConstants.CONNECTOR_FACTORY;
import static org.ballerinalang.net.http.WebSocketConstants.ErrorCode.WsGenericError;
import static org.ballerinalang.net.http.WebSocketConstants.ErrorCode.WsInvalidHandshakeError;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_INTEVAL;
import static org.ballerinalang.net.http.WebSocketConstants.FULL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.WebSocketConstants.INTERVAL;
import static org.ballerinalang.net.http.WebSocketConstants.IS_PRINT;
import static org.ballerinalang.net.http.WebSocketConstants.MAX_COUNT;
import static org.ballerinalang.net.http.WebSocketConstants.MAX_INTERVAL;
import static org.ballerinalang.net.http.WebSocketConstants.NO_OF_RECONNECT_ATTEMPTS;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECTING;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.SUB_TARGET_URLS;
import static org.ballerinalang.net.http.WebSocketConstants.SUB_TARGET_URLS_INDEX;
import static org.ballerinalang.net.http.WebSocketConstants.TARGET_URLS;
import static org.ballerinalang.net.http.WebSocketConstants.WEBSOCKET_ERROR_DETAILS;


/**
 * Utility class for WebSocket.
 */
public class WebSocketUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketUtil.class);
    private static final PrintStream console = System.out;

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
                ObjectValue webSocketEndpoint = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_HTTP,
                        WebSocketConstants.WEBSOCKET_CALLER);
                ObjectValue webSocketConnector = BallerinaValues.createObjectValue(
                        PROTOCOL_PACKAGE_HTTP, WebSocketConstants.WEBSOCKET_CONNECTOR);

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
            }

            @Override
            public void onError(Throwable throwable) {
                if (callback != null) {
                    callback.notifyFailure(createWebSocketError(WsInvalidHandshakeError ,
                            "Unable to complete handshake:" + throwable.getMessage()));
                } else {
                    throw new WebSocketException("Unable to complete handshake", throwable);
                }
            }
        });
    }

    public static void executeOnOpenResource(WebSocketService wsService, AttachedFunction onOpenResource,
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
        //TODO this is temp fix till we get the service.start() API
        Executor.submit(wsService.getScheduler(), wsService.getBalService(), onOpenResource.getName(),
                onOpenCallableUnitCallback,
                null, bValues);
    }

    public static void populateEndpoint(WebSocketConnection webSocketConnection, ObjectValue webSocketEndpoint) {
        webSocketEndpoint.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        String negotiatedSubProtocol = webSocketConnection.getNegotiatedSubProtocol();
        webSocketEndpoint.set(WebSocketConstants.LISTENER_NEGOTIATED_SUBPROTOCOLS_FIELD,
                negotiatedSubProtocol != null ? negotiatedSubProtocol : "");
        webSocketEndpoint.set(WebSocketConstants.LISTENER_IS_SECURE_FIELD, webSocketConnection.isSecure());
        webSocketEndpoint.set(WebSocketConstants.LISTENER_IS_OPEN_FIELD, webSocketConnection.isOpen());
    }

    public static void handleWebSocketCallback(NonBlockingCallback callback,
                                               ChannelFuture webSocketChannelFuture) {
        webSocketChannelFuture.addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                //TODO Temp fix to get return values. Remove
                callback.setReturnValues(HttpUtil.getError(cause));
            } else {
                //TODO Temp fix to get return values. Remove
                callback.setReturnValues(null);
            }
            //TODO remove this call back
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

    /**
     * Create Generic webSocket error with given error message.
     *
     * @param errMsg the error message
     * @return ErrorValue instance which contains the error details
     */
    public static ErrorValue createWebSocketError(String errMsg) {
        return BallerinaErrors.createError(WsGenericError.errorCode(), createDetailRecord(errMsg, null));
    }

    /**
     * Create webSocket error with given error code and message.
     *
     * @param code   the error code which cause for this error
     * @param errMsg the error message
     * @return ErrorValue instance which contains the error details
     */
    public static ErrorValue createWebSocketError(WebSocketConstants.ErrorCode code, String errMsg) {
        return BallerinaErrors.createError(code.errorCode(), createDetailRecord(errMsg, null));
    }

    private static MapValue<String, Object> createDetailRecord(Object... values) {
        MapValue<String, Object> detail = BallerinaValues.createRecordValue(FULL_PACKAGE_HTTP,
                WEBSOCKET_ERROR_DETAILS);
        return BallerinaValues.createRecord(detail, values);
    }

    public static void initialiseWebSocketConnection(String remoteUrl, ObjectValue webSocketClient,
                                                     WebSocketService wsService) {
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig);
        HttpWsConnectorFactory connectorFactory = (HttpWsConnectorFactory) webSocketClient.
                getNativeData(CONNECTOR_FACTORY);
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(clientConnectorConfig);
        webSocketClient.addNativeData(CLIENT_CONNECTOR, clientConnector);
        establishWebSocketConnection(clientConnector, webSocketClient, wsService);
    }

    private static void establishWebSocketConnection(WebSocketClientConnector clientConnector,
                                                     ObjectValue webSocketClient, WebSocketService wsService) {
        WebSocketClientConnectorListener clientConnectorListener = new WebSocketClientConnectorListener();
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        boolean readyOnConnect = ((MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG)).getBooleanValue(WebSocketConstants.CLIENT_READY_ON_CONNECT);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        handshakeFuture.setClientHandshakeListener(
                new WebSocketClientHandshakeListener(webSocketClient, wsService, clientConnectorListener,
                        readyOnConnect, countDownLatch));
        try {
            if (!countDownLatch.await(60, TimeUnit.SECONDS)) {
                throw new WebSocketException("Waiting for WebSocket handshake has not been successful");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException("Error occurred: " + e.getMessage());
        }
    }

    private static void populateClientConnectorConfig(MapValue<String, Object> clientEndpointConfig,
                                                      WebSocketClientConnectorConfig clientConnectorConfig) {
        clientConnectorConfig.setAutoRead(false); // Frames are read sequentially in ballerina.
        clientConnectorConfig.setSubProtocols(WebSocketUtil.findNegotiableSubProtocols(clientEndpointConfig));
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> headerValues = (MapValue<String, Object>) clientEndpointConfig.getMapValue(
                WebSocketConstants.CLIENT_CUSTOM_HEADERS_CONFIG);
        if (headerValues != null) {
            clientConnectorConfig.addHeaders(getCustomHeaders(headerValues));
        }

        long idleTimeoutInSeconds = WebSocketUtil.findIdleTimeoutInSeconds(clientEndpointConfig);
        if (idleTimeoutInSeconds > 0) {
            clientConnectorConfig.setIdleTimeoutInMillis((int) (idleTimeoutInSeconds * 1000));
        }

        clientConnectorConfig.setMaxFrameSize(WebSocketUtil.findMaxFrameSize(clientEndpointConfig));

        MapValue secureSocket = clientEndpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        if (secureSocket != null) {
            HttpUtil.populateSSLConfiguration(clientConnectorConfig, secureSocket);
        } else {
            HttpUtil.setDefaultTrustStore(clientConnectorConfig);
        }
    }

    private static Map<String, String> getCustomHeaders(MapValue<String, Object> headers) {
        Map<String, String> customHeaders = new HashMap<>();
        headers.keySet().forEach(
                key -> customHeaders.put(key, headers.get(key).toString())
        );
        return customHeaders;
    }

    public static int getIntegerValue(Long value) {
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException e) {
            throw new WebSocketException("Error occurred when casting the value to Integer");
        }
    }

    private static void wait(CountDownLatch countDownLatch, int maxReconnectInterval) {
        try {
            countDownLatch.await(maxReconnectInterval, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException("Error occurred: " + e.getMessage());
        }
    }

    public static WebSocketService getWebSocketService(MapValue<String, Object> clientEndpointConfig,
                                                       Strand strand) {
        Object clientService = clientEndpointConfig.get(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        if (clientService != null) {
            BType param = ((ObjectValue) clientService).getType().getAttachedFunctions()[0].getParameterType()[0];
            if (param == null || !((WebSocketConstants.WEBSOCKET_CLIENT_NAME.equals(
                    param.toString())) || (WebSocketConstants.WEBSOCKET_FAILOVER_CLIENT_NAME.equals(
                            param.toString())))) {
                throw new WebSocketException("The callback service should be a WebSocket Client Service");
            }
            return new WebSocketService((ObjectValue) clientService, strand.scheduler);
        } else {
            return new WebSocketService(strand.scheduler);
        }
    }

    static boolean reconnect(WebSocketOpenConnectionInfo connectionInfo) {
        ObjectValue clientEndpointConfig = connectionInfo.getWebSocketEndpoint();
        MapValueImpl reconnectConfig = clientEndpointConfig.getMapValue(CLIENT_ENDPOINT_CONFIG).
                getMapValue(RETRY_CONFIG);
        int maxAttempts = getIntegerValue(reconnectConfig.getIntValue(MAX_COUNT));
        WebSocketService wsService = connectionInfo.getService();
        int noOfReconnectAttempts = getIntegerValue(Long.valueOf(clientEndpointConfig.
                getNativeData(NO_OF_RECONNECT_ATTEMPTS).toString()));
        WebSocketClientConnector clientConnector = (WebSocketClientConnector) clientEndpointConfig.
                getNativeData(CLIENT_CONNECTOR);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        boolean doReconnectAttempt = false;
        if (((noOfReconnectAttempts < maxAttempts) && maxAttempts > 0) || maxAttempts  == 0) {
            doReconnectAttempt = true;
            clientEndpointConfig.addNativeData(NO_OF_RECONNECT_ATTEMPTS, noOfReconnectAttempts + 1);
            console.println(formatter.format(date.getTime()) + " " + RECONNECTING);
            setcountDownLatch(getWaitTime(clientEndpointConfig));
            establishWebSocketConnection(clientConnector, clientEndpointConfig, wsService);
        }
        return doReconnectAttempt;
    }

    static Boolean doFailover(WebSocketOpenConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        List subTargetUrls = (List) webSocketClient.getNativeData(SUB_TARGET_URLS);
        boolean doFailoverAttempt = false;
        if (subTargetUrls.size() > 0) {
            MapValueImpl clientConfig = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG);
            WebSocketService wsService = connectionInfo.getService();
            int failoverInterval = getIntegerValue(Long.valueOf(clientConfig.get(FAILOVER_INTEVAL).toString()));
            int subTargetsUrlIndex = (int) webSocketClient.getNativeData(SUB_TARGET_URLS_INDEX);
            webSocketClient.addNativeData(SUB_TARGET_URLS_INDEX, subTargetsUrlIndex + 1);
            if (subTargetsUrlIndex < subTargetUrls.size()) {
                doFailoverAttempt = true;
                setcountDownLatch(failoverInterval);
                initialiseWebSocketConnection(subTargetUrls.get(subTargetsUrlIndex).toString(), webSocketClient,
                        wsService);
            }
        }
        return doFailoverAttempt;
    }

    static boolean reconnectForFailoverClient(WebSocketOpenConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        List subTargetUrls = (List) webSocketClient.getNativeData(SUB_TARGET_URLS);
        boolean doReconnectAttempt = false;
        if (subTargetUrls.size() > 0) {
            MapValueImpl clientConfig = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG);
            WebSocketService wsService = connectionInfo.getService();
            MapValueImpl reconnectConfig = clientConfig.getMapValue(RETRY_CONFIG);
            int subTargetsUrlIndex = (int) webSocketClient.getNativeData(SUB_TARGET_URLS_INDEX);
            int maxAttempts = getIntegerValue(reconnectConfig.getIntValue(MAX_COUNT));
            int noOfReconnectAttempts = getIntegerValue(Long.valueOf(webSocketClient.
                    getNativeData(NO_OF_RECONNECT_ATTEMPTS).toString()));
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            if (webSocketClient.getNativeData(IS_PRINT) == null) {
                console.println(formatter.format(date.getTime()) + " " + RECONNECTING);
                webSocketClient.addNativeData(IS_PRINT, true);
            }
            if (subTargetsUrlIndex >= subTargetUrls.size()) {
                console.println(formatter.format(date.getTime()) + " " + RECONNECTING);
                webSocketClient.addNativeData(NO_OF_RECONNECT_ATTEMPTS, noOfReconnectAttempts + 1);
                webSocketClient.addNativeData(SUB_TARGET_URLS_INDEX, 0);
            }
            if (((noOfReconnectAttempts < maxAttempts) && maxAttempts > 0) || maxAttempts == 0) {
                subTargetsUrlIndex = (int) webSocketClient.getNativeData(SUB_TARGET_URLS_INDEX);
                webSocketClient.addNativeData(SUB_TARGET_URLS_INDEX, subTargetsUrlIndex + 1);
                setcountDownLatch(getWaitTime(webSocketClient));
                initialiseWebSocketConnection(subTargetUrls.get(subTargetsUrlIndex).toString(), webSocketClient,
                        wsService);
                doReconnectAttempt = true;
            }
        }
        return doReconnectAttempt;
    }

    public static int getWaitTime(ObjectValue webSocketClient) {
        MapValueImpl config = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG);
        int interval = getIntegerValue(config.getIntValue(INTERVAL));
        Double backOfFactor = config.getFloatValue(BACK_OF_FACTOR);
        int maxInterval = getIntegerValue(config.getIntValue(MAX_INTERVAL));
        int reconnectAttempts = getIntegerValue(Long.valueOf(webSocketClient.getNativeData(NO_OF_RECONNECT_ATTEMPTS).
                toString()));
        interval = (int) (interval * Math.pow(backOfFactor, reconnectAttempts));
        if (interval > maxInterval) {
            interval = maxInterval;
        }
        return interval;
    }

    static void setSubTargetUrlList(ObjectValue webSocketClient, int urlIndex) {
        ArrayList<String> subTargetUrls = new ArrayList<>();
        int initiateValue = urlIndex + 1;
        int endValue, j;
        ArrayValue targetUrls = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).
                    getArrayValue(WebSocketConstants.TARGET_URLS);
        int size = targetUrls.size();
        if (webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG) == null) {
            endValue = size + urlIndex;
        } else {
            endValue = size + urlIndex + 1;
        }
        for (int i = initiateValue; i < endValue; i++) {
            if (i >= size) {
                j = i - size;
            } else {
                j = i;
            }
            if (targetUrls.get(j) != "") {
                subTargetUrls.add((String) targetUrls.get(j));
            }
        }
        webSocketClient.addNativeData(SUB_TARGET_URLS_INDEX, 0);
        webSocketClient.addNativeData(SUB_TARGET_URLS, subTargetUrls);
        webSocketClient.addNativeData(TARGET_URLS, targetUrls);
    }

    static void removeUrlInTarget(ObjectValue webSocketClient) {
        int connectedServerUrlIndex = (int) webSocketClient.getNativeData(CONNECTED_SERVER_URLS_INDEX);
        List subTargetUrls = (List) webSocketClient.getNativeData(SUB_TARGET_URLS);
        int subtargetUrlsIndex = (int) webSocketClient.getNativeData(SUB_TARGET_URLS_INDEX);
        ArrayValue targetUrls = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).
                getArrayValue(WebSocketConstants.TARGET_URLS);
        subTargetUrls.remove(targetUrls.get(connectedServerUrlIndex));
        targetUrls.add(connectedServerUrlIndex, "");
        webSocketClient.addNativeData(SUB_TARGET_URLS_INDEX, 0);
        if (subtargetUrlsIndex == subTargetUrls.size() - 1) {
            webSocketClient.addNativeData(SUB_TARGET_URLS_INDEX, subtargetUrlsIndex);
        }
        console.println("Urls " + subTargetUrls + " " + subtargetUrlsIndex + " " + webSocketClient.
                getMapValue(CLIENT_ENDPOINT_CONFIG).getArrayValue(WebSocketConstants.TARGET_URLS));
        webSocketClient.addNativeData(SUB_TARGET_URLS, subTargetUrls);
        webSocketClient.addNativeData(TARGET_URLS, targetUrls);
    }

    static void setcountDownLatch(int interval) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        wait(countDownLatch, interval);
        countDownLatch.countDown();
    }

    public static void checkParameter(ObjectValue webSocketClient) {
        MapValue<?, ?> reconnectConfig = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).
                getMapValue(RETRY_CONFIG);
        int interval = getIntegerValue(reconnectConfig.getIntValue(INTERVAL));
        Double decay = reconnectConfig.getFloatValue(BACK_OF_FACTOR);
        int maxInterval = getIntegerValue(reconnectConfig.getIntValue(MAX_INTERVAL));
        int maxAttempts = getIntegerValue(reconnectConfig.getIntValue(MAX_COUNT));
        if (maxAttempts < 0) {
            logger.warn("The maximum reconnect attempt's value set for the configuration needs to be " +
                    "greater than -1. The maxAttempts[ " + maxAttempts + "] value is set to 0");
            webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG).
                    put(MAX_COUNT, 0);
        }
        if (interval < 0) {
            logger.warn("The interval's value set for the configuration needs to be " +
                    "greater than -1. The interval[" + interval + "] value is set to 1000");
            webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG).
                    put(INTERVAL, 1000);
        }
        if (decay < 0) {
            logger.warn("The decay's value set for the configuration needs to be " +
                    "greater than -1. The decay[" + decay + "] value is set to 1.0");
            webSocketClient.getMapValue(HttpConstants.CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG)
                    .put(BACK_OF_FACTOR, 1.0);
        }
        if (maxInterval < 0) {
            logger.warn("The maxInterval's value set for the configuration needs to be " +
                    "greater than -1. The maxInterval[" + maxInterval + "] value is set to 30000");
            webSocketClient.getMapValue(HttpConstants.CLIENT_ENDPOINT_CONFIG).getMapValue(RETRY_CONFIG)
                    .put(MAX_INTERVAL, 30000);
        }
    }

    private WebSocketUtil() {
    }
}
