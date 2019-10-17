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

package org.ballerinalang.net.http.websocket;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.websocketx.CorruptedWebSocketFrameException;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpErrorType;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.client.FailoverContext;
import org.ballerinalang.net.http.websocket.client.RetryContext;
import org.ballerinalang.net.http.websocket.client.WebSocketClientHandshakeListener;
import org.ballerinalang.net.http.websocket.client.WebSocketClientListener;
import org.ballerinalang.net.http.websocket.client.WebSocketFailoverClientHandshakeListener;
import org.ballerinalang.net.http.websocket.client.WebSocketFailoverClientListener;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionManager;
import org.ballerinalang.net.http.websocket.server.WebSocketOpenConnectionInfo;
import org.ballerinalang.net.http.websocket.server.WebSocketServerService;
import org.ballerinalang.net.http.websocket.server.WebSocketService;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import static org.ballerinalang.net.http.websocket.WebSocketConstants.BACK_OF_FACTOR;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.CLIENT_CONNECTOR;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.CLIENT_LISTENER;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.CONNECTOR_FACTORY;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.COUNT_DOWN_LATCH;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.ErrorCode;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.FAILOVER_CONFIG;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.FAILOVER_INTEVAL;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.INTERVAL;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.MAX_COUNT;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.MAX_INTERVAL;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.RECONNECTING;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.STATEMENT_FOR_FAILOVDER_RECONNECT;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.STATEMENT_FOR_FAILOVER;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.STATEMENT_FOR_RECONNECT;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.WSS_SCHEME;
import static org.ballerinalang.stdlib.io.utils.IOConstants.IO_PACKAGE_ID;

/**
 * Utility class for WebSocket.
 */
public class WebSocketUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketUtil.class);

    public static ObjectValue createAndPopulateWebSocketCaller(WebSocketConnection webSocketConnection,
                                                               WebSocketServerService wsService,
                                                               WebSocketConnectionManager connectionManager) {
        ObjectValue webSocketCaller = BallerinaValues.createObjectValue(HttpConstants.PROTOCOL_HTTP_PKG_ID,
                WebSocketConstants.WEBSOCKET_CALLER);
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(
                HttpConstants.PROTOCOL_HTTP_PKG_ID, WebSocketConstants.WEBSOCKET_CONNECTOR);

        webSocketCaller.set(WebSocketConstants.LISTENER_CONNECTOR_FIELD, webSocketConnector);
        populateWebSocketCaller(webSocketConnection, webSocketCaller);
        WebSocketOpenConnectionInfo connectionInfo =
                new WebSocketOpenConnectionInfo(wsService, webSocketConnection, webSocketCaller);
        connectionManager.addConnection(webSocketConnection.getChannelId(), connectionInfo);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO,
                connectionInfo);
        return webSocketCaller;
    }

    public static void populateWebSocketCaller(WebSocketConnection webSocketConnection, ObjectValue webSocketCaller) {
        webSocketCaller.set(WebSocketConstants.LISTENER_ID_FIELD, webSocketConnection.getChannelId());
        String negotiatedSubProtocol = webSocketConnection.getNegotiatedSubProtocol();
        webSocketCaller.set(WebSocketConstants.LISTENER_NEGOTIATED_SUBPROTOCOLS_FIELD, negotiatedSubProtocol);
        webSocketCaller.set(WebSocketConstants.LISTENER_IS_SECURE_FIELD, webSocketConnection.isSecure());
        webSocketCaller.set(WebSocketConstants.LISTENER_IS_OPEN_FIELD, webSocketConnection.isOpen());
    }

    public static void handleWebSocketCallback(NonBlockingCallback callback,
                                               ChannelFuture webSocketChannelFuture, Logger log) {
        webSocketChannelFuture.addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                log.error("Error occurred ", cause);
                callback.notifyFailure(WebSocketUtil.createErrorByType(cause));
            } else {
                // This is needed because since the same strand is used in all actions if an action is called before
                // this one it will cause this action to return the return value of the previous action.
                callback.setReturnValues(null);
                callback.notifySuccess();
            }
        });
    }

    public static void readFirstFrame(WebSocketConnection webSocketConnection, ObjectValue wsClient) {
        webSocketConnection.readNextFrame();
        wsClient.set(WebSocketConstants.CONNECTOR_IS_READY_FIELD, true);
    }

    /**
     * Closes the connection with the unexpected failure status code.
     *
     * @param webSocketConnection the websocket connection to be closed.
     */
    public static void closeDuringUnexpectedCondition(WebSocketConnection webSocketConnection) {
        webSocketConnection.terminateConnection(1011, "Unexpected condition");

    }

    public static void setListenerOpenField(WebSocketOpenConnectionInfo connectionInfo) throws IllegalAccessException {
        connectionInfo.getWebSocketCaller().set(WebSocketConstants.LISTENER_IS_OPEN_FIELD,
                connectionInfo.getWebSocketConnection().isOpen());
    }

    public static int findMaxFrameSize(MapValue<String, Object> configs) {
        long size = configs.getIntValue(WebSocketConstants.ANNOTATION_ATTR_MAX_FRAME_SIZE);
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

    public static int findIdleTimeoutInSeconds(MapValue<String, Object> configs) {
        long timeout = configs.getIntValue(WebSocketConstants.ANNOTATION_ATTR_IDLE_TIMEOUT);
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

    public static String[] findNegotiableSubProtocols(MapValue<String, Object> configs) {
        return configs.getArrayValue(WebSocketConstants.ANNOTATION_ATTR_SUB_PROTOCOLS).getStringArray();
    }

    public static String getErrorMessage(Throwable err) {
        if (err.getMessage() == null) {
            return "Unexpected error occurred";
        }
        return err.getMessage();
    }

    /**
     * Creates the appropriate ballerina errors using for the given throwable.
     *
     * @param throwable the throwable to be represented in Ballerina.
     * @return the relevant WebSocketException with proper error code.
     */
    public static WebSocketException createErrorByType(Throwable throwable) {
        ErrorCode errorCode = ErrorCode.WsGenericError;
        ErrorValue cause = null;
        String message = getErrorMessage(throwable);
        if (throwable instanceof CorruptedWebSocketFrameException) {
            WebSocketCloseStatus status = ((CorruptedWebSocketFrameException) throwable).closeStatus();
            if (status == WebSocketCloseStatus.MESSAGE_TOO_BIG) {
                errorCode = ErrorCode.WsPayloadTooBigError;
            } else {
                errorCode = ErrorCode.WsProtocolError;
            }
        } else if (throwable instanceof SSLException) {
            cause = createErrorCause(throwable.getMessage(), HttpErrorType.SSL_ERROR.getReason(),
                    HttpConstants.PROTOCOL_HTTP_PKG_ID);
            message = "SSL/TLS Error";
        } else if (throwable instanceof IllegalStateException) {
            if (throwable.getMessage().contains("frame continuation")) {
                errorCode = ErrorCode.WsInvalidContinuationFrameError;
            } else if (throwable.getMessage().toLowerCase(Locale.ENGLISH).contains("close frame")) {
                errorCode = ErrorCode.WsConnectionClosureError;
            }
        } else if (throwable instanceof IllegalAccessException &&
                throwable.getMessage().equals(WebSocketConstants.THE_WEBSOCKET_CONNECTION_HAS_NOT_BEEN_MADE)) {
            errorCode = ErrorCode.WsConnectionError;
        } else if (throwable instanceof TooLongFrameException) {
            errorCode = ErrorCode.WsPayloadTooBigError;
        } else if (throwable instanceof CodecException) {
            errorCode = ErrorCode.WsProtocolError;
        } else if (throwable instanceof WebSocketHandshakeException) {
            errorCode = ErrorCode.WsInvalidHandshakeError;
        } else if (throwable instanceof IOException) {
            errorCode = ErrorCode.WsConnectionError;
            cause = createErrorCause(throwable.getMessage(), IOConstants.ErrorCode.GenericError.errorCode(),
                    IO_PACKAGE_ID);
            message = "IO Error";
        }
        return new WebSocketException(errorCode, message, cause);
    }

    public static ErrorValue createErrorCause(String message, String reason, BPackage packageName) {

        MapValue<String, Object> detailRecordType = BallerinaValues.createRecordValue(
                packageName, WebSocketConstants.WEBSOCKET_ERROR_DETAILS);
        MapValue<String, Object> detailRecord = BallerinaValues.createRecord(detailRecordType, message, null);
        return BallerinaErrors.createError(reason, detailRecord);
    }

    public static MapValue<String, Object> createDetailRecord(String errMsg) {
        return createDetailRecord(errMsg, null);
    }

    public static MapValue<String, Object> createDetailRecord(String errMsg, ErrorValue cause) {
        MapValue<String, Object> detail = BallerinaValues.createRecordValue(HttpConstants.PROTOCOL_HTTP_PKG_ID,
                WebSocketConstants.WEBSOCKET_ERROR_DETAILS);
        return BallerinaValues.createRecord(detail, errMsg, cause);
    }

    public static void populateClientConnectorConfig(MapValue<String, Object> clientEndpointConfig,
                                                      WebSocketClientConnectorConfig clientConnectorConfig,
                                                      String scheme) {
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
        } else if (scheme.equals(WSS_SCHEME)) {
            clientConnectorConfig.useJavaDefaults();
        }
        clientConnectorConfig.setWebSocketCompressionEnabled(
                clientEndpointConfig.getBooleanValue(WebSocketConstants.COMPRESSION_ENABLED_CONFIG));
    }

    private static Map<String, String> getCustomHeaders(MapValue<String, Object> headers) {
        Map<String, String> customHeaders = new HashMap<>();
        headers.keySet().forEach(
                key -> customHeaders.put(key, headers.get(key).toString())
        );
        return customHeaders;
    }

    /**
     * Populate the retry config.
     * @param retryConfig a retry config
     * @param retryConnectorConfig a doReconnect connector config
     */
    public static void populateRetryConnectorConfig(MapValue<String, Object> retryConfig,
                                                    RetryContext retryConnectorConfig) {
        int interval = Integer.parseInt(retryConfig.get(INTERVAL).toString());
        float backOfFactor = Float.parseFloat(retryConfig.get(BACK_OF_FACTOR).toString());
        int maxAttempts = Integer.parseInt(retryConfig.get(MAX_COUNT).toString());
        int maxInterval = Integer.parseInt(retryConfig.get(MAX_INTERVAL).toString());
        if (interval < 0) {
            logger.warn("The interval's value set for the configuration needs to be " +
                    "greater than -1. The interval[" + interval + "] value is set to 1000");
            interval = 1000;
        }
        if (backOfFactor < 0) {
            logger.warn("The decay's value set for the configuration needs to be " +
                    "greater than -1. The backOfFactor[" + backOfFactor + "] value is set to 1.0");
            backOfFactor = (float) 1.0;
        }
        if (maxInterval < 0) {
            logger.warn("The maxInterval's value set for the configuration needs to be " +
                    "greater than -1. The maxInterval[" + maxInterval + "] value is set to 30000");
            maxInterval =  30000;
        }
        if (maxAttempts < 0) {
            logger.warn("The maximum doReconnect attempt's value set for the configuration needs to be " +
                    "greater than -1. The maxAttempts[ " + maxAttempts + "] value is set to 0");
            maxAttempts = 0;
        }
        retryConnectorConfig.setInterval(interval);
        retryConnectorConfig.setMaxAttempts(maxAttempts);
        retryConnectorConfig.setBackOfFactor(backOfFactor);
        retryConnectorConfig.setMaxInterval(maxInterval);
    }

    /**
     * Populate the failover config.
     * @param clientEndpointConfig a client endpoint config
     * @param failoverClientConnectorConfig a failover client connector config
     * @param targetUrls target urls
     */
    public static void populateFailoverConnectorConfig(MapValue<String, Object> clientEndpointConfig,
                                                       FailoverContext failoverClientConnectorConfig,
                                                       ArrayList<String> targetUrls) {
        int failoverInterval = Integer.parseInt(clientEndpointConfig.get(FAILOVER_INTEVAL).toString());
        if (failoverInterval < 0) {
            logger.warn("The maxInterval's value set for the configuration needs to be " +
                    "greater than -1. The " + failoverInterval + "value is set to 1.0");
            failoverInterval = 1000;
        }
        failoverClientConnectorConfig.setFailoverInterval(failoverInterval);
        failoverClientConnectorConfig.setTargetUrls(targetUrls);
    }

    /**
     * Get the webSocket service.
     * @param clientEndpointConfig a client endpoint config
     * @param strand a strand
     * @return webSocketService
     */
    public static WebSocketService getWebSocketService(MapValue<String, Object> clientEndpointConfig, Strand strand) {
        Object clientService = clientEndpointConfig.get(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        if (clientService != null) {
            BType param = ((ObjectValue) clientService).getType().getAttachedFunctions()[0].getParameterType()[0];
            if (param == null || !(WebSocketConstants.WEBSOCKET_CLIENT_NAME.equals(
                    param.toString()) || WebSocketConstants.WEBSOCKET_FAILOVER_CLIENT_NAME.equals(
                    param.toString()))) {
                throw new WebSocketException("The callback service should be a WebSocket Client Service");
            }
            return new WebSocketService((ObjectValue) clientService, strand.scheduler);
        } else {
            return new WebSocketService(strand.scheduler);
        }
    }

    public static void initialiseWebSocketConnection(String remoteUrl, ObjectValue webSocketClient,
                                                     WebSocketService wsService) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        String scheme = URI.create(remoteUrl).getScheme();
        populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig, scheme);
        // Get the connector factory from the native data
        HttpWsConnectorFactory connectorFactory = ((HttpWsConnectorFactory) webSocketClient.
                getNativeData(CONNECTOR_FACTORY));
        // Create the client connector
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(clientConnectorConfig);
        WebSocketClientListener clientConnectorListener = new WebSocketClientListener();
        // Add the client connector as the native data, when client is not as a failover client
        // Because Here, using one url So no need to create the client connector again
        webSocketClient.addNativeData(CLIENT_CONNECTOR, clientConnector);
        webSocketClient.addNativeData(CLIENT_LISTENER, clientConnectorListener);
        establishInitialWebSocketConnection(webSocketClient, wsService);
    }

    public static WebSocketClientConnector getWebSocketClientConnector(String remoteUrl,
                                                                       ObjectValue webSocketClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig, remoteUrl);
        // Get the connector factory from the native data
        HttpWsConnectorFactory connectorFactory = ((HttpWsConnectorFactory) webSocketClient.
                getNativeData(CONNECTOR_FACTORY));
        // Create the client connector
        return connectorFactory.createWsClientConnector(clientConnectorConfig);
    }

    public static void establishInitialFailoverConnection(WebSocketClientConnector clientConnector,
                                                          ObjectValue webSocketClient, WebSocketService wsService) {
        establishFailoverConnection(clientConnector, webSocketClient, wsService);
        waitingForHandShake(webSocketClient);
    }

    private static void establishFailoverConnection(WebSocketClientConnector clientConnector,
                                                    ObjectValue webSocketClient, WebSocketService wsService) {
        WebSocketFailoverClientListener clientConnectorListener = new WebSocketFailoverClientListener();
        boolean readyOnConnect = webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG).getBooleanValue(WebSocketConstants.CLIENT_READY_ON_CONNECT);
        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        handshakeFuture.setClientHandshakeListener(new WebSocketFailoverClientHandshakeListener(webSocketClient,
                wsService, clientConnectorListener, readyOnConnect));
    }

    private static void establishInitialWebSocketConnection(ObjectValue webSocketClient, WebSocketService wsService) {
        establishWebSocketConnection(webSocketClient, wsService);
        waitingForHandShake(webSocketClient);
    }

    private static void establishWebSocketConnection(ObjectValue webSocketClient, WebSocketService wsService) {
        WebSocketClientListener clientConnectorListener = (WebSocketClientListener) webSocketClient.
                getNativeData(CLIENT_LISTENER);
        WebSocketClientConnector clientConnector = (WebSocketClientConnector) webSocketClient.
                getNativeData(CLIENT_CONNECTOR);
        boolean readyOnConnect = webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG).getBooleanValue(WebSocketConstants.CLIENT_READY_ON_CONNECT);
        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        handshakeFuture.setClientHandshakeListener(new WebSocketClientHandshakeListener(webSocketClient, wsService,
                clientConnectorListener, readyOnConnect));
    }

    private static void waitingForHandShake(ObjectValue  webSocketClient) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        webSocketClient.addNativeData(COUNT_DOWN_LATCH, countDownLatch);
        try {
            // Wait for 5 minutes before timeout
            if (!countDownLatch.await(60 * 5L, TimeUnit.SECONDS)) {
                throw new WebSocketException(WebSocketConstants.ErrorCode.WsGenericError,
                        "Waiting for WebSocket handshake has not been successful", WebSocketUtil.createErrorCause(
                                "Connection timeout", IOConstants.ErrorCode.ConnectionTimedOut.errorCode(),
                        IO_PACKAGE_ID));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException("Error occurred: " + e.getMessage());
        }
    }

    /**
     * Do the doReconnect when webSocket connection will be lost.
     *
     * @param connectionInfo a connection info
     * @return if do the reconnection, return true
     */
    public static boolean reconnect(WebSocketOpenConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketCaller();
        RetryContext retryConnectorConfig = (RetryContext) webSocketClient.getNativeData(RETRY_CONFIG);
        int interval = retryConnectorConfig.getInterval();
        int maxInterval = retryConnectorConfig.getMaxInterval();
        int maxAttempts = retryConnectorConfig.getMaxAttempts();
        int noOfReconnectAttempts = retryConnectorConfig.getReconnectAttempts();
        float backOfFactor = retryConnectorConfig.getBackOfFactor();
        WebSocketService wsService = connectionInfo.getService();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        // Check the no of reconnection attempt equals with maximum reconnection attempt or not.
        // If it isn't equal, call the initialiseWebSocketConnection()
        // if it equals, return false
        if (((noOfReconnectAttempts < maxAttempts) && maxAttempts > 0) || maxAttempts == 0) {
            retryConnectorConfig.setReconnectAttempts(noOfReconnectAttempts + 1);
            logger.debug(formatter.format(date.getTime()) + " " + RECONNECTING);
            setCountDownLatch(getWaitTime(interval, maxInterval, backOfFactor, noOfReconnectAttempts));
            establishWebSocketConnection(webSocketClient, wsService);
            return true;
        }
        logger.debug(STATEMENT_FOR_RECONNECT + webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
        return false;
    }

    /**
     * Do the failover when webSocket connection will be lost.
     *
     * @param connectionInfo a connection info
     * @return return true, if attempts the failover function
     */
    private static boolean failoverAndRetry(WebSocketOpenConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketCaller();
        FailoverContext failoverConfig = (FailoverContext) webSocketClient.getNativeData(FAILOVER_CONFIG);
        int currentIndex = failoverConfig.getCurrentIndex();
        ArrayList targets = failoverConfig.getTargetUrls();
        WebSocketService wsService = connectionInfo.getService();
        int failoverInterval = failoverConfig.getFailoverInterval();
        // Set next url index
        currentIndex++;
        // Check current url index equals to target size or not. if equal, set the currentIndex = 0
        if (currentIndex == targets.size()) {
            currentIndex = 0;
        }
        failoverConfig.setCurrentIndex(currentIndex);
        // Check whether failover attempt finished or not.
        // If it isn't finished, call the initialiseWebSocketConnection()
        // if it is finished and has retry config, call the doReconnectForFailover()
        if (!failoverConfig.isFailoverFinished()) {
            // Check the current url index equals with previous connected url index or not.
            // If it isn't equal, call the initialiseWebSocketConnection()
            // if it equals, call the doReconnectForFailover()
            if (currentIndex != failoverConfig.getInitialIndex()) {
                setCountDownLatch(failoverInterval);
                establishFailoverConnection(getWebSocketClientConnector(targets.get(currentIndex).toString(),
                        webSocketClient), webSocketClient, wsService);
                return true;
            } else {
                failoverConfig.setFailoverFinished(true);
                return reconnectForFailover(connectionInfo, currentIndex);
            }
        } else if (hasRetryConfig(webSocketClient)) {
            return reconnectForFailover(connectionInfo, currentIndex);
        }
        return false;
    }

    /**
     * Do the failover when webSocket connection will be lost.
     *
     * @param connectionInfo a connection info
     * @return return true, if attempts the failover function
     */
    private static boolean doFailover(WebSocketOpenConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketCaller();
        FailoverContext failoverConfig = (FailoverContext)
                webSocketClient.getNativeData(FAILOVER_CONFIG);
        int currentIndex = failoverConfig.getCurrentIndex();
        ArrayList targets = failoverConfig.getTargetUrls();
        int failoverInterval = failoverConfig.getFailoverInterval();
        WebSocketService wsService = connectionInfo.getService();
        // Set next url index
        currentIndex++;
        // Check current url index equals to target size or not. if equal, set the currentIndex = 0
        if (currentIndex == targets.size()) {
            currentIndex = 0;
        }
        // Check the current url index equals with previous connected url index or not.
        // If it isn't equal, call the initialiseWebSocketConnection()
        // if it equals, return false
        if (currentIndex != failoverConfig.getInitialIndex()) {
            failoverConfig.setCurrentIndex(currentIndex);
            setCountDownLatch(failoverInterval);
            establishFailoverConnection(getWebSocketClientConnector(targets.get(currentIndex).toString(),
                    webSocketClient), webSocketClient, wsService);
            return true;
        }
        logger.debug(STATEMENT_FOR_FAILOVER + targets);
        return false;
    }

    /**
     * Do the reconnection when webSocket connection will be lost.
     *
     * @param connectionInfo a connection info
     */
    private static boolean reconnectForFailover(WebSocketOpenConnectionInfo connectionInfo, int currentIndex) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketCaller();
        RetryContext retryConnectorConfig = (RetryContext) webSocketClient.getNativeData(RETRY_CONFIG);
        FailoverContext failoverConfig = (FailoverContext) webSocketClient.getNativeData(FAILOVER_CONFIG);
        int interval = retryConnectorConfig.getInterval();
        int maxInterval = retryConnectorConfig.getMaxInterval();
        int maxAttempts = retryConnectorConfig.getMaxAttempts();
        int noOfReconnectAttempts = retryConnectorConfig.getReconnectAttempts();
        float backOfFactor = retryConnectorConfig.getBackOfFactor();
        WebSocketService wsService = connectionInfo.getService();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        ArrayList targets = failoverConfig.getTargetUrls();
        // Check the current url index equals with previous connected url index or not.
        // If it is equal, update the no of reconnection attempt by one
        if (currentIndex == failoverConfig.getInitialIndex()) {
            logger.debug(formatter.format(date.getTime()) + " " + RECONNECTING);
            noOfReconnectAttempts++;
            retryConnectorConfig.setReconnectAttempts(noOfReconnectAttempts);
        }
        // Check the no of reconnection attempt equals with maximum reconnection attempt or not.
        // If it isn't equal, call the initialiseWebSocketConnection()
        // if it equals, return false
        if (((noOfReconnectAttempts < maxAttempts) && maxAttempts > 0) || maxAttempts == 0) {
            setCountDownLatch(getWaitTime(interval, maxInterval, backOfFactor, noOfReconnectAttempts));
            establishFailoverConnection(getWebSocketClientConnector(targets.get(currentIndex).toString(),
                    webSocketClient), webSocketClient, wsService);
            return true;
        }
        logger.debug(STATEMENT_FOR_FAILOVDER_RECONNECT + targets);
        return false;
    }

    /**
     * Set waiting time before attempting to next doReconnect/failover.
     * @param interval interval
     */
    private static void setCountDownLatch(int interval) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        if (wait(countDownLatch, interval)) {
            countDownLatch.countDown();
        }
    }

    public static boolean hasRetryConfig(ObjectValue webSocketClient) {
        return webSocketClient.getMapValue(WebSocketConstants.CLIENT_ENDPOINT_CONFIG).
                getMapValue(RETRY_CONFIG) != null;
    }

    private static boolean wait(CountDownLatch countDownLatch, int interval) {
        try {
            return countDownLatch.await(interval, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException("Error occurred: " + e.getMessage());
        }
    }

    /**
     * Calculate the waiting time.
     * @param interval interval
     * @param maxInterval maximum Interval
     * @param backOfFactor back of factor
     * @param reconnectAttempts no of doReconnect attempts
     * @return waiting time
     */
    private static int getWaitTime(int interval, int maxInterval, float backOfFactor, int reconnectAttempts) {
        interval = (int) (interval * Math.pow(backOfFactor, reconnectAttempts));
        if (interval > maxInterval) {
            interval = maxInterval;
        }
        return interval;
    }

    public static void determineAction(WebSocketOpenConnectionInfo connectionInfo, Throwable throwable,
                                WebSocketCloseMessage webSocketCloseMessage) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketCaller();
        if (hasRetryConfig(webSocketClient)) {
            // When connection lost, do the failover to the remaining server urls.
            // If failover fails, do the reconnection
            if (failoverAndRetry(connectionInfo)) {
                return;
            }
        } else {
            // When connection lost, do the failover to the remaining server urls.
            if (doFailover(connectionInfo)) {
                return;
            }
        }
        setError(connectionInfo, throwable, webSocketCloseMessage);
    }

    public static void handleExceptionAndDispatchCloseMessage(WebSocketOpenConnectionInfo connectionInfo,
                                                       WebSocketCloseMessage webSocketCloseMessage) {
        try {
            WebSocketResourceDispatcher.dispatchOnClose(connectionInfo, webSocketCloseMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    private static void closeConnection(WebSocketOpenConnectionInfo connectionInfo, Throwable throwable,
                                        WebSocketCloseMessage webSocketCloseMessage) {
        if (throwable != null) {
            WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
        } else {
            handleExceptionAndDispatchCloseMessage(connectionInfo, webSocketCloseMessage);
        }
    }

    private static void setError(WebSocketOpenConnectionInfo connectionInfo, Throwable throwable,
                                 WebSocketCloseMessage webSocketCloseMessage) {
        ObjectValue getWebSocketCaller = connectionInfo.getWebSocketCaller();
        countDownForHandshake(getWebSocketCaller);
        closeConnection(connectionInfo, throwable, webSocketCloseMessage);
    }

    public static void countDownForHandshake(ObjectValue webSocketClient) {
        if (webSocketClient.getNativeData(COUNT_DOWN_LATCH) != null) {
            ((CountDownLatch) webSocketClient.getNativeData(COUNT_DOWN_LATCH)).countDown();
            webSocketClient.addNativeData(COUNT_DOWN_LATCH, null);
        }
    }

    private WebSocketUtil() {
    }
}
