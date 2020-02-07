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
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpErrorType;
import org.ballerinalang.net.http.websocket.client.RetryContext;
import org.ballerinalang.net.http.websocket.client.WebSocketClientConnectorListener;
import org.ballerinalang.net.http.websocket.client.WebSocketClientHandshakeListener;
import org.ballerinalang.net.http.websocket.client.WebSocketClientHandshakeListenerForRetry;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionManager;
import org.ballerinalang.net.http.websocket.server.WebSocketServerService;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ClientHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

/**
 * Utility class for WebSocket.
 */
public class WebSocketUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketUtil.class);
    private static final String CLIENT_ENDPOINT_CONFIG = "config";

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
        //Observe new connection
        WebSocketObservabilityUtil.observeConnection(
                connectionManager.getConnectionInfo(webSocketConnection.getChannelId()));

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

    public static void readFirstFrame(WebSocketConnection webSocketConnection, ObjectValue wsClient) {
        webSocketConnection.readNextFrame();
        wsClient.set(WebSocketConstants.CONNECTOR_IS_READY_FIELD, true);
    }

    /**
     * Closes the connection with the unexpected failure status code.
     *
     * @param webSocketConnection - the webSocket connection to be closed.
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

    public static int findTimeoutInSeconds(MapValue<String, Object> config, String key, int defaultValue) {
        long timeout = config.getIntValue(key);
        if (timeout < 0) {
            return defaultValue;
        }
        try {
            return Math.toIntExact(timeout);
        } catch (ArithmeticException e) {
            logger.warn("The value set for {} needs to be less than {} .The {} value is set to {} ", key,
                    Integer.MAX_VALUE, key, Integer.MAX_VALUE);
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
     * @param throwable - the throwable to be represented in Ballerina.
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

    /**
     * Establish connection with the endpoint.
     *
     * @param webSocketClient - the WebSocket client.
     * @param wsService - the WebSocket service.
     */
    public static void establishWebSocketConnection(ObjectValue webSocketClient, WebSocketService wsService) {
        WebSocketClientConnectorListener clientConnectorListener = (WebSocketClientConnectorListener) webSocketClient.
                getNativeData(WebSocketConstants.CLIENT_LISTENER);
        WebSocketClientConnector clientConnector = (WebSocketClientConnector) webSocketClient.
                getNativeData(WebSocketConstants.CLIENT_CONNECTOR);
        boolean readyOnConnect = webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).getBooleanValue(
                WebSocketConstants.CLIENT_READY_ON_CONNECT);
        ClientHandshakeFuture handshakeFuture = clientConnector.connect();
        handshakeFuture.setWebSocketConnectorListener(clientConnectorListener);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        if (WebSocketUtil.hasRetryConfig(webSocketClient)) {
            handshakeFuture.setClientHandshakeListener(new WebSocketClientHandshakeListenerForRetry(webSocketClient,
                    wsService, clientConnectorListener, readyOnConnect, countDownLatch,
                    (RetryContext) webSocketClient.getNativeData(WebSocketConstants.RETRY_CONFIG)));
        } else {
            handshakeFuture.setClientHandshakeListener(new WebSocketClientHandshakeListener(webSocketClient, wsService,
                    clientConnectorListener, readyOnConnect, countDownLatch));
        }
        // Set the countDown latch for every handshake
        waitForHandshake(webSocketClient, countDownLatch);
    }

    /**
     * Check whether the client's config has the retryConfig property.
     *
     * @param webSocketClient - the WebSocket client.
     * @return If the client's config has the retry config, then return true.
     */
    public static boolean hasRetryConfig(ObjectValue webSocketClient) {
        return webSocketClient.getMapValue(CLIENT_ENDPOINT_CONFIG).
                getMapValue(WebSocketConstants.RETRY_CONFIG) != null;
    }

    private static void waitForHandshake(ObjectValue webSocketClient, CountDownLatch countDownLatch) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        long timeout = WebSocketUtil.findTimeoutInSeconds((MapValue<String, Object>) webSocketClient.getMapValue(
                CLIENT_ENDPOINT_CONFIG), "handShakeTimeoutInSeconds", 300);
        try {
            if (!countDownLatch.await(timeout, TimeUnit.SECONDS)) {
                throw new WebSocketException(WebSocketConstants.ErrorCode.WsGenericError,
                        "Waiting for WebSocket handshake has not been successful", WebSocketUtil.createErrorCause(
                        "Connection timeout", IOConstants.ErrorCode.ConnectionTimedOut.errorCode(),
                        IOConstants.IO_PACKAGE_ID));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException(WebSocketConstants.ERROR_MESSAGE + e.getMessage());
        }
    }

    /**
     * Reconnect when the WebSocket connection is lost.
     *
     * @param connectionInfo - information about the connection.
     * @return If attempts reconnection, then return true.
     */
    public static boolean reconnect(WebSocketConnectionInfo connectionInfo) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        RetryContext retryConnectorConfig = (RetryContext) webSocketClient.getNativeData(WebSocketConstants.
                RETRY_CONFIG);
        int interval = retryConnectorConfig.getInterval();
        int maxInterval = retryConnectorConfig.getMaxInterval();
        int maxAttempts = retryConnectorConfig.getMaxAttempts();
        int noOfReconnectAttempts = retryConnectorConfig.getReconnectAttempts();
        double backOfFactor = retryConnectorConfig.getBackOfFactor();
        WebSocketService wsService = connectionInfo.getService();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        if (noOfReconnectAttempts < maxAttempts || maxAttempts == 0) {
            retryConnectorConfig.setReconnectAttempts(noOfReconnectAttempts + 1);
            String time = formatter.format(date.getTime());
            logger.debug(WebSocketConstants.LOG_MESSAGE, time, "reconnecting...");
            createDelay(calculateWaitingTime(interval, maxInterval, backOfFactor, noOfReconnectAttempts));
            establishWebSocketConnection(webSocketClient, wsService);
            return true;
        }
        logger.debug(WebSocketConstants.LOG_MESSAGE, "Maximum retry attempts but couldn't connect to the server: ",
                webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG));
        return false;
    }

    /**
     * Set the time to wait before attempting to reconnect.
     *
     * @param interval - interval to wait before trying to reconnect.
     */
    private static void createDelay(int interval) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            if (!countDownLatch.await(interval, TimeUnit.MILLISECONDS)) {
                countDownLatch.countDown();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException(WebSocketConstants.ERROR_MESSAGE + e.getMessage());
        }
    }

    /**
     * Calculate the waiting time.
     *
     * @param interval- interval to wait before trying to reconnect.
     * @param maxInterval - maximum interval to wait before trying to reconnect.
     * @param backOfFactor - the rate of increase of the reconnect delay.
     * @param reconnectAttempts - the number of reconnecting attempts.
     * @return The time to wait before attempting to reconnect.
     */
    private static int calculateWaitingTime(int interval, int maxInterval, double backOfFactor,
                                            int reconnectAttempts) {
        interval = (int) (interval * Math.pow(backOfFactor, reconnectAttempts));
        if (interval > maxInterval) {
            interval = maxInterval;
        }
        return interval;
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

    private WebSocketUtil() {
    }

}
