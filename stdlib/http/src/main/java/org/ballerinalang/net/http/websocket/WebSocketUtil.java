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
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionManager;
import org.ballerinalang.net.http.websocket.server.WebSocketServerService;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

/**
 * Utility class for WebSocket.
 */
public class WebSocketUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketUtil.class);
    private static final String STATEMENT_FOR_RECONNECT = "Maximum retry attempts but couldn't connect " +
            "to the server: ";
    private static final String RECONNECTING = "reconnecting...";
    private static final String CLIENT_ENDPOINT_CONFIG = "config";
    private static final String ERROR_MESSAGE = "Error occurred: ";
    private static final String STATEMENT_FOR_FAILOVER = "Couldn't connect to one of the server in" +
            " the targets: ";
    private static final String WEBSOCKET_FAILOVER_CLIENT_NAME = WebSocketConstants.PACKAGE_HTTP +
            WebSocketConstants.SEPARATOR + WebSocketConstants.FAILOVER_WEBSOCKET_CLIENT;
    private static final String STATEMENT_FOR_FAILOVDER_RECONNECT = "Maximum retry attempts but couldn't connect " +
            "to the one of the server in the targets: ";
    private static final String STATEMENT_FOR_BACK_OF_FACTOR = "The decay's value set for the configuration needs " +
            "to be greater than -1. ";
    private static final String STATEMENT_FOR_MAX_INTERVAL = "The maxInterval's value set for the configuration" +
            " needs to be greater than -1. ";
    private static final String STATEMENT_FOR_MAX_ATTEPTS = "The maximum doReconnect attempt's value set for the" +
            " configuration needs to be greater than -1. ";
    private static final String STATEMENT_FOR_INTEVAL = "The interval's value set for the configuration needs to be " +
            "greater than -1. ";
    private static final String MAX_COUNT = "maxCount";
    private static final String INTERVAL = "intervalInMillis";
    private static final String BACK_OF_FACTOR = "backOffFactor";
    private static final String MAX_INTERVAL = "maxWaitIntervalInMillis";

    public static ObjectValue createAndPopulateWebSocketCaller(WebSocketConnection webSocketConnection,
                                                               WebSocketServerService wsService,
                                                               WebSocketConnectionManager connectionManager) {
        ObjectValue webSocketCaller = BallerinaValues.createObjectValue(HttpConstants.PROTOCOL_HTTP_PKG_ID,
                                                                        WebSocketConstants.WEBSOCKET_CALLER);
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(
                HttpConstants.PROTOCOL_HTTP_PKG_ID, WebSocketConstants.WEBSOCKET_CONNECTOR);

        webSocketCaller.set(WebSocketConstants.LISTENER_CONNECTOR_FIELD, webSocketConnector);
        populateWebSocketEndpoint(webSocketConnection, webSocketCaller);
        WebSocketConnectionInfo connectionInfo =
                new WebSocketConnectionInfo(wsService, webSocketConnection, webSocketCaller);
        connectionManager.addConnection(webSocketConnection.getChannelId(), connectionInfo);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO,
                                         connectionInfo);
        return webSocketCaller;
    }

    public static void populateWebSocketEndpoint(WebSocketConnection webSocketConnection, ObjectValue webSocketCaller) {
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

    public static void readFirstFrame(WebSocketConnection webSocketConnection, ObjectValue wsConnector) {
        webSocketConnection.readNextFrame();
        wsConnector.set(WebSocketConstants.CONNECTOR_IS_READY_FIELD, true);
    }

    /**
     * Closes the connection with the unexpected failure status code.
     *
     * @param webSocketConnection the websocket connection to be closed.
     */
    public static void closeDuringUnexpectedCondition(WebSocketConnection webSocketConnection) {
        webSocketConnection.terminateConnection(1011, "Unexpected condition");

    }

    public static void setListenerOpenField(WebSocketConnectionInfo connectionInfo) throws IllegalAccessException {
        connectionInfo.getWebSocketEndpoint().set(WebSocketConstants.LISTENER_IS_OPEN_FIELD,
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

    static String getErrorMessage(Throwable err) {
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
        WebSocketConstants.ErrorCode errorCode = WebSocketConstants.ErrorCode.WsGenericError;
        ErrorValue cause = null;
        String message = getErrorMessage(throwable);
        if (throwable instanceof CorruptedWebSocketFrameException) {
            WebSocketCloseStatus status = ((CorruptedWebSocketFrameException) throwable).closeStatus();
            if (status == WebSocketCloseStatus.MESSAGE_TOO_BIG) {
                errorCode = WebSocketConstants.ErrorCode.WsPayloadTooBigError;
            } else {
                errorCode = WebSocketConstants.ErrorCode.WsProtocolError;
            }
        } else if (throwable instanceof SSLException) {
            cause = createErrorCause(throwable.getMessage(), HttpErrorType.SSL_ERROR.getReason(),
                                     HttpConstants.PROTOCOL_HTTP_PKG_ID);
            message = "SSL/TLS Error";
        } else if (throwable instanceof IllegalStateException) {
            if (throwable.getMessage().contains("frame continuation")) {
                errorCode = WebSocketConstants.ErrorCode.WsInvalidContinuationFrameError;
            } else if (throwable.getMessage().toLowerCase(Locale.ENGLISH).contains("close frame")) {
                errorCode = WebSocketConstants.ErrorCode.WsConnectionClosureError;
            }
        } else if (throwable instanceof IllegalAccessException &&
                throwable.getMessage().equals(WebSocketConstants.THE_WEBSOCKET_CONNECTION_HAS_NOT_BEEN_MADE)) {
            errorCode = WebSocketConstants.ErrorCode.WsConnectionError;
        } else if (throwable instanceof TooLongFrameException) {
            errorCode = WebSocketConstants.ErrorCode.WsPayloadTooBigError;
        } else if (throwable instanceof CodecException) {
            errorCode = WebSocketConstants.ErrorCode.WsProtocolError;
        } else if (throwable instanceof WebSocketHandshakeException) {
            errorCode = WebSocketConstants.ErrorCode.WsInvalidHandshakeError;
        } else if (throwable instanceof IOException) {
            errorCode = WebSocketConstants.ErrorCode.WsConnectionError;
            cause = createErrorCause(throwable.getMessage(), IOConstants.ErrorCode.GenericError.errorCode(),
                    IOConstants.IO_PACKAGE_ID);
            message = "IO Error";
        }
        return new WebSocketException(errorCode, message, cause);
    }

    private static ErrorValue createErrorCause(String message, String reason, BPackage packageName) {

        MapValue<String, Object> detailRecordType = BallerinaValues.createRecordValue(
                packageName, WebSocketConstants.WEBSOCKET_ERROR_DETAILS);
        MapValue<String, Object> detailRecord = BallerinaValues.createRecord(detailRecordType, message, null);
        return BallerinaErrors.createError(reason, detailRecord);
    }

    static MapValue<String, Object> createDetailRecord(String errMsg) {
        return createDetailRecord(errMsg, null);
    }

    static MapValue<String, Object> createDetailRecord(String errMsg, ErrorValue cause) {
        MapValue<String, Object> detail = BallerinaValues.createRecordValue(HttpConstants.PROTOCOL_HTTP_PKG_ID,
                                                                            WebSocketConstants.WEBSOCKET_ERROR_DETAILS);
        return BallerinaValues.createRecord(detail, errMsg, cause);
    }

    /**
     * Check whether client's config has retryConfig ot not.
     *
     * @param webSocketClient webSocket client
     * @return if client's config has retry config, return true
     */
    public static boolean hasRetryConfig(ObjectValue webSocketClient) {
        return webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).
                getMapValue(WebSocketConstants.RETRY_CONFIG) != null;
    }

    /**
     * Waits for 5 minutes before timeout.
     *
     * @param countDownLatch countDown latch
     */
    private static void waitForHandshake(CountDownLatch countDownLatch) {
        try {
            // Wait for 5 minutes before timeout
            if (!countDownLatch.await(60 * 5L, TimeUnit.SECONDS)) {
                throw new WebSocketException(WebSocketConstants.ErrorCode.WsGenericError,
                        "Waiting for WebSocket handshake has not been successful", WebSocketUtil.createErrorCause(
                        "Connection timeout", IOConstants.ErrorCode.ConnectionTimedOut.errorCode(),
                        IOConstants.IO_PACKAGE_ID));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException(ERROR_MESSAGE + e.getMessage());
        }
    }

    /**
     * Establishes the ClientHandshakeFuture for failover client and set listeners into this.
     *
     * @param webSocketClient webSocket client
     * @param wsService webSocket service
     */
    public static void establishWebSocketConnection(ObjectValue webSocketClient, WebSocketService wsService) {
        WebSocketClientListener clientConnectorListener = (WebSocketClientListener) webSocketClient.
                getNativeData(WebSocketConstants.CLIENT_LISTENER);
        WebSocketClientConnector clientConnector = (WebSocketClientConnector) webSocketClient.
                getNativeData(WebSocketConstants.CLIENT_CONNECTOR);
        boolean readyOnConnect = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getBooleanValue(
                WebSocketConstants.CLIENT_READY_ON_CONNECT);
        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        if (webSocketClient.getNativeData(WebSocketConstants.RETRY_CONFIG) != null &&
                ((RetryContext) webSocketClient.getNativeData(WebSocketConstants.RETRY_CONFIG)).
                        isFirstConnectionMadeSuccessfully()) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            handshakeFuture.setClientHandshakeListener(new WebSocketClientHandshakeListener(webSocketClient, wsService,
                    clientConnectorListener, readyOnConnect, countDownLatch));
            waitForHandshake(countDownLatch);
        } else {
            handshakeFuture.setClientHandshakeListener(new WebSocketClientHandshakeListener(webSocketClient, wsService,
                    clientConnectorListener, readyOnConnect, null));
            waitForHandshake(webSocketClient);
        }
    }

    /**
     * Reconnects when webSocket connection is lost.
     *
     * @param connectionInfo information about the connection
     * @return if attempts reconnection, return true
     */
    public static boolean reconnect(WebSocketConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        RetryContext retryConnectorConfig = (RetryContext) webSocketClient.getNativeData(WebSocketConstants.
                RETRY_CONFIG);
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
            String time = formatter.format(date.getTime());
            logger.debug(WebSocketConstants.LOG_MESSAGE, time, RECONNECTING);
            setCountDownLatch(calculateWaitingTime(interval, maxInterval, backOfFactor, noOfReconnectAttempts));
            establishWebSocketConnection(webSocketClient, wsService);
            return true;
        }
        logger.debug(WebSocketConstants.LOG_MESSAGE, STATEMENT_FOR_RECONNECT , webSocketClient.
                getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
        return false;
    }

    /**
     * Sets waiting time before attempting to next reconnect.
     *
     * @param interval interval
     */
    private static void setCountDownLatch(int interval) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            if (!countDownLatch.await(interval, TimeUnit.MILLISECONDS)) {
                countDownLatch.countDown();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException(ERROR_MESSAGE + e.getMessage());
        }
    }

    /**
     * Calculates the waiting time.
     *
     * @param interval interval
     * @param maxInterval maximum Interval
     * @param backOfFactor back of factor
     * @param reconnectAttempts no of doReconnect attempts
     * @return waiting time
     */
    private static int calculateWaitingTime(int interval, int maxInterval, float backOfFactor, int reconnectAttempts) {
        interval = (int) (interval * Math.pow(backOfFactor, reconnectAttempts));
        if (interval > maxInterval) {
            interval = maxInterval;
        }
        return interval;
    }

    /**
     * Failover when webSocket connection lost.
     *
     * @param connectionInfo information about the connection
     * @return if attempts failover, return true
     */
    public static boolean failover(WebSocketConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        FailoverContext failoverConfig = (FailoverContext)
                webSocketClient.getNativeData(WebSocketConstants.FAILOVER_CONFIG);
        int currentIndex = failoverConfig.getCurrentIndex();
        List targets = failoverConfig.getTargetUrls();
        int failoverInterval = failoverConfig.getFailoverInterval();
        WebSocketService wsService = connectionInfo.getService();
        currentIndex = getCurrentIndex(currentIndex, targets);
        // Check the current url index equals with previous connected url index or not.
        // If it isn't equal, call the initialiseWebSocketConnection()
        // if it equals, return false
        if (currentIndex != failoverConfig.getInitialIndex()) {
            failoverConfig.setCurrentIndex(currentIndex);
            setCountDownLatch(failoverInterval);
            establishFailoverConnection(createWebSocketClientConnector(targets.get(currentIndex).toString(),
                    webSocketClient), webSocketClient, wsService);
            return true;
        }
        logger.debug(WebSocketConstants.LOG_MESSAGE, STATEMENT_FOR_FAILOVER, targets);
        return false;
    }

    /**
     * Establishes the ClientHandshakeFuture for failover client and set listeners into this.
     *
     * @param clientConnector client connector
     * @param webSocketClient webSocket client
     * @param wsService webSocket service
     */
    public static void establishFailoverConnection(WebSocketClientConnector clientConnector,
                                                   ObjectValue webSocketClient, WebSocketService wsService) {
        WebSocketFailoverClientListener clientConnectorListener = new WebSocketFailoverClientListener();
        boolean readyOnConnect = webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG).getBooleanValue(WebSocketConstants.CLIENT_READY_ON_CONNECT);
        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        if (webSocketClient.getNativeData(WebSocketConstants.FAILOVER_CONFIG) != null &&
                ((FailoverContext) webSocketClient.getNativeData(WebSocketConstants.FAILOVER_CONFIG)).
                        isFirstConnectionMadeSuccessfully()) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            handshakeFuture.setClientHandshakeListener(new WebSocketFailoverClientHandshakeListener(webSocketClient,
                    wsService, clientConnectorListener, readyOnConnect, countDownLatch));
            waitForHandshake(countDownLatch);
        } else {
            handshakeFuture.setClientHandshakeListener(new WebSocketFailoverClientHandshakeListener(webSocketClient,
                    wsService, clientConnectorListener, readyOnConnect, null));
            waitForHandshake(webSocketClient);
        }
    }

    /**
     * Waits until call the countDown().
     *
     * @param webSocketClient webSocket client
     */
    private static void waitForHandshake(ObjectValue webSocketClient) {
        if (webSocketClient.getNativeData(WebSocketConstants.COUNT_DOWN_LATCH) == null) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            webSocketClient.addNativeData(WebSocketConstants.COUNT_DOWN_LATCH, countDownLatch);
            try {
                // Wait to call countDown()
                countDownLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new WebSocketException(ERROR_MESSAGE + e.getMessage());
            }
        }
    }

    public static WebSocketClientConnector createWebSocketClientConnector(String remoteUrl,
                                                                          ObjectValue webSocketClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig, remoteUrl);
        // Get the connector factory from the native data
        HttpWsConnectorFactory connectorFactory = ((HttpWsConnectorFactory) webSocketClient.
                getNativeData(WebSocketConstants.CONNECTOR_FACTORY));
        // Create the client connector
        return connectorFactory.createWsClientConnector(clientConnectorConfig);
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
        } else if (scheme.equals(WebSocketConstants.WSS_SCHEME)) {
            clientConnectorConfig.useJavaDefaults();
        }
        clientConnectorConfig.setWebSocketCompressionEnabled(
                clientEndpointConfig.getBooleanValue(WebSocketConstants.COMPRESSION_ENABLED_CONFIG));
    }

    private static Map<String, String> getCustomHeaders(MapValue<String, Object> headers) {
        Map<String, String> customHeaders = new HashMap<>();
        headers.entrySet().forEach(
                entry -> customHeaders.put(entry.getKey(), headers.get(entry.getKey()).toString())
        );
        return customHeaders;
    }

    /**
     * Validate and create the webSocket service.
     *
     * @param clientEndpointConfig a client endpoint config
     * @param strand a strand
     * @return webSocketService
     */
    public static WebSocketService validateAndCreateWebSocketService(MapValue<String, Object> clientEndpointConfig,
                                                                     Strand strand) {
        Object clientService = clientEndpointConfig.get(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        if (clientService != null) {
            BType param = ((ObjectValue) clientService).getType().getAttachedFunctions()[0].getParameterType()[0];
            if (param == null || !(WebSocketConstants.WEBSOCKET_CLIENT_NAME.equals(
                    param.toString()) || WEBSOCKET_FAILOVER_CLIENT_NAME.equals(
                    param.toString()))) {
                throw new WebSocketException("The callback service should be a WebSocket Client Service");
            }
            return new WebSocketService((ObjectValue) clientService, strand.scheduler);
        } else {
            return new WebSocketService(strand.scheduler);
        }
    }

    public static void dispatchOnClose(WebSocketConnectionInfo connectionInfo,
                                       WebSocketCloseMessage webSocketCloseMessage) {
        try {
            WebSocketResourceDispatcher.dispatchOnClose(connectionInfo, webSocketCloseMessage);
        } catch (IllegalAccessException e) {
            // Ignore as it is not possible have an Illegal access
        }
    }

    public static WebSocketConnectionInfo getWebSocketOpenConnectionInfo(WebSocketConnection webSocketConnection,
                                                                         ObjectValue webSocketConnector,
                                                                         ObjectValue webSocketClient,
                                                                         WebSocketService wsService) {
        WebSocketConnectionInfo connectionInfo = new WebSocketConnectionInfo(
                wsService, webSocketConnection, webSocketClient);
        webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO, connectionInfo);
        webSocketClient.set(WebSocketConstants.CLIENT_CONNECTOR_FIELD, webSocketConnector);
        return connectionInfo;
    }

    public static ObjectValue createWebSocketConnector(boolean readyOnConnect) {
        ObjectValue webSocketConnector = BallerinaValues.createObjectValue(HttpConstants.PROTOCOL_HTTP_PKG_ID,
                WebSocketConstants.WEBSOCKET_CONNECTOR);
        // Sets readyOnConnect's value to the created webSocketConnector's isReady field
        // It uses to check whether readNextFrame function already called or not When call the ready() function
        webSocketConnector.set(WebSocketConstants.CONNECTOR_IS_READY_FIELD, readyOnConnect);
        return webSocketConnector;
    }

    /**
     * Call the readNextFrame().
     *
     * @param readyOnConnect ready on connect
     * @param webSocketClient webSocket client
     * @param webSocketConnection webSocket connection
     */
    public static void readNextFrame(boolean readyOnConnect, ObjectValue webSocketClient,
                                     WebSocketConnection webSocketConnection) {
        if (readyOnConnect || (webSocketClient.get(WebSocketConstants.CLIENT_CONNECTOR_FIELD) != null &&
                ((boolean) ((ObjectValue) webSocketClient.get(WebSocketConstants.CLIENT_CONNECTOR_FIELD)).
                        get(WebSocketConstants.CONNECTOR_IS_READY_FIELD)))) {
            webSocketConnection.readNextFrame();
        }
    }

    private static void countDownForHandshake(ObjectValue webSocketClient) {
        if (webSocketClient.getNativeData(WebSocketConstants.COUNT_DOWN_LATCH) != null) {
            ((CountDownLatch) webSocketClient.getNativeData(WebSocketConstants.COUNT_DOWN_LATCH)).countDown();
            webSocketClient.addNativeData(WebSocketConstants.COUNT_DOWN_LATCH, null);
        }
    }

    public static void dispatchOnError(WebSocketConnectionInfo connectionInfo, Throwable throwable) {
        logger.error(ERROR_MESSAGE, throwable);
        countDownForHandshake(connectionInfo.getWebSocketEndpoint());
        WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
    }

    public static void countDownForSuccess(CountDownLatch countDownLatch, ObjectValue webSocketClient) {
        if (countDownLatch == null) {
            WebSocketUtil.countDownForHandshake(webSocketClient);
        } else {
            countDownLatch.countDown();
        }
    }

    public static void determineFailoverOrReconnect(WebSocketConnectionInfo connectionInfo, Throwable throwable,
                                                    WebSocketCloseMessage webSocketCloseMessage) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        if (hasRetryConfig(webSocketClient)) {
            // When connection lost, do the failover to the remaining server urls.
            // If failover fails, do the reconnection
            if (!failoverAndRetry(connectionInfo)) {
                setDispatcher(connectionInfo, throwable, webSocketCloseMessage);
            }
        } else {
            // When connection lost, do the failover to the remaining server urls.
            if (!failover(connectionInfo)) {
                setDispatcher(connectionInfo, throwable, webSocketCloseMessage);
            }
        }
    }

    private static void setDispatcher(WebSocketConnectionInfo connectionInfo, Throwable throwable,
                                      WebSocketCloseMessage webSocketCloseMessage) {
        ObjectValue webSocketCaller = connectionInfo.getWebSocketEndpoint();
        countDownForHandshake(webSocketCaller);
        dispatchIntoResource(connectionInfo, throwable, webSocketCloseMessage);
    }

    private static void dispatchIntoResource(WebSocketConnectionInfo connectionInfo, Throwable throwable,
                                             WebSocketCloseMessage webSocketCloseMessage) {
        if (throwable != null) {
            WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
        } else {
            dispatchOnClose(connectionInfo, webSocketCloseMessage);
        }
    }

    /**
     * Failover and Reconnect when webSocket connection is lost.
     *
     * @param connectionInfo information about the connection
     * @return if failover or retry attempts, return true
     */
    private static boolean failoverAndRetry(WebSocketConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        FailoverContext failoverConfig = (FailoverContext) webSocketClient.
                getNativeData(WebSocketConstants.FAILOVER_CONFIG);
        List targets = failoverConfig.getTargetUrls();
        // Check whether failover attempt finished or not.
        // If it isn't finished, call the establishFailoverConnection()
        // if it is finished and has retry config, call the reconnectForFailover()
        if (!failoverConfig.isFailoverFinished()) {
            if (!failover(connectionInfo)) {
                failoverConfig.setFailoverFinished(true);
            } else {
                return true;
            }
        }
        if (failoverConfig.isFailoverFinished()) {
            return reconnectForFailover(connectionInfo, failoverConfig, targets);
        }
        return false;
    }

    /**
     * Do the reconnection when webSocket connection will be lost.
     *
     * @param connectionInfo a connection info
     */
    private static boolean reconnectForFailover(WebSocketConnectionInfo connectionInfo,
                                                FailoverContext failoverConfig, List targets) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        RetryContext retryConnectorConfig = (RetryContext) webSocketClient.
                getNativeData(WebSocketConstants.RETRY_CONFIG);
        int interval = retryConnectorConfig.getInterval();
        int maxInterval = retryConnectorConfig.getMaxInterval();
        int maxAttempts = retryConnectorConfig.getMaxAttempts();
        int noOfReconnectAttempts = retryConnectorConfig.getReconnectAttempts();
        float backOfFactor = retryConnectorConfig.getBackOfFactor();
        int currentIndex = failoverConfig.getCurrentIndex();
        WebSocketService wsService = connectionInfo.getService();
        Date date = new Date();
        currentIndex = getCurrentIndex(currentIndex, targets);
        // Check the current url index equals with previous connected url index or not.
        // If it is equal, update the no of reconnection attempt by one
        if (currentIndex == failoverConfig.getInitialIndex()) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String time = formatter.format(date.getTime());
            logger.debug(WebSocketConstants.LOG_MESSAGE, time, RECONNECTING);
            noOfReconnectAttempts++;
            retryConnectorConfig.setReconnectAttempts(noOfReconnectAttempts);
        }
        // Check the no of reconnection attempt equals with maximum reconnection attempt or not.
        // If it isn't equal, call the initialiseWebSocketConnection()
        // if it equals, return false
        if (((noOfReconnectAttempts < maxAttempts) && maxAttempts > 0) || maxAttempts == 0) {
            failoverConfig.setCurrentIndex(currentIndex);
            setCountDownLatch(calculateWaitingTime(interval, maxInterval, backOfFactor, noOfReconnectAttempts));
            establishFailoverConnection(createWebSocketClientConnector(targets.get(currentIndex).toString(),
                    webSocketClient), webSocketClient, wsService);
            return true;
        }
        logger.debug(WebSocketConstants.LOG_MESSAGE, STATEMENT_FOR_FAILOVDER_RECONNECT, targets);
        return false;
    }

    public static void dispatchOnError(Throwable throwable, WebSocketConnectionInfo connectionInfo) {
        logger.error(WebSocketConstants.ERROR_MESSAGE, throwable);
        WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
    }

    /**
     * Populate the retry config.
     *
     * @param retryConfig a retry config
     * @param retryConnectorConfig retry connector config
     */
    public static void populateRetryConnectorConfig(MapValue<String, Object> retryConfig,
                                                    RetryContext retryConnectorConfig) {
        setInterval(Integer.parseInt(retryConfig.get(INTERVAL).toString()), retryConnectorConfig);
        setBackOfFactor(Float.parseFloat(retryConfig.get(BACK_OF_FACTOR).toString()),
                retryConnectorConfig);
        setMaxInterval(Integer.parseInt(retryConfig.get(MAX_INTERVAL).toString()),
                retryConnectorConfig);
        setMaxAttempts(Integer.parseInt(retryConfig.get(MAX_COUNT).toString()),
                retryConnectorConfig);
    }

    /**
     * Set value of the interval in the retry config.
     *
     * @param interval retry interval
     * @param retryConnectorConfig retry connector config
     */
    private static void setInterval(int interval, RetryContext retryConnectorConfig) {
        if (interval < 0) {
            logger.warn("{} The interval[{}] value is set to 1000.", STATEMENT_FOR_INTEVAL, interval);
            interval = 1000;
        }
        retryConnectorConfig.setInterval(interval);
    }

    /**
     * Set value of the backOfFactor in the retry config.
     *
     * @param backOfFactor a rate of increase of the reconnect delay
     * @param retryConnectorConfig retry connector config
     */
    private static void setBackOfFactor(float backOfFactor, RetryContext retryConnectorConfig) {
        if (backOfFactor < 0) {
            logger.warn("{} The backOfFactor[{}] value is set to 1.0", STATEMENT_FOR_BACK_OF_FACTOR,
                    backOfFactor);
            backOfFactor = (float) 1.0;
        }
        retryConnectorConfig.setBackOfFactor(backOfFactor);
    }

    /**
     * Set value of the maxInterval in the retry config.
     *
     * @param maxInterval a maximum interval
     * @param retryConnectorConfig retry connector config
     */
    private static void setMaxInterval(int maxInterval, RetryContext retryConnectorConfig) {
        if (maxInterval < 0) {
            logger.warn("{} The maxInterval[{}] value is set to 30000", STATEMENT_FOR_MAX_INTERVAL,
                    maxInterval);
            maxInterval =  30000;
        }
        retryConnectorConfig.setMaxInterval(maxInterval);
    }

    /**
     * Set value of the maxAttempts in the retry config.
     *
     * @param maxAttempts a maximum number of retry attempts
     * @param retryConnectorConfig retry connector config
     */
    private static void setMaxAttempts(int maxAttempts, RetryContext retryConnectorConfig) {
        if (maxAttempts < 0) {
            logger.warn("{} The maxAttempts[{}] value is set to 0", STATEMENT_FOR_MAX_ATTEPTS,
                    maxAttempts);
            maxAttempts = 0;
        }
        retryConnectorConfig.setMaxAttempts(maxAttempts);
    }

    private static int getCurrentIndex (int currentIndex, List targets) {
        // Set next url index
        currentIndex++;
        // Check current url index equals to target size or not. if equal, set the currentIndex = 0
        if (currentIndex == targets.size()) {
            currentIndex = 0;
        }
        return currentIndex;
    }

    private WebSocketUtil() {
    }

}
