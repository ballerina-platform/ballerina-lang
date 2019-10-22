/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.CorruptedFrameException;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpDispatcher;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.websocket.server.OnUpgradeResourceCallback;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionManager;
import org.ballerinalang.net.http.websocket.server.WebSocketOpenConnectionInfo;
import org.ballerinalang.net.http.websocket.server.WebSocketServerService;
import org.ballerinalang.net.http.websocket.server.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.ballerinalang.net.http.websocket.WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE;
import static org.ballerinalang.net.http.websocket.WebSocketConstants.STATUS_CODE_FOR_NO_STATUS_CODE_PRESENT;

/**
 * {@code WebSocketDispatcher} This is the web socket request dispatcher implementation which finds best matching
 * resource for incoming web socket request.
 *
 * @since 0.94
 */
public class WebSocketResourceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketResourceDispatcher.class);

    private WebSocketResourceDispatcher() {
    }

    public static void dispatchUpgrade(WebSocketHandshaker webSocketHandshaker, WebSocketServerService wsService,
                                       MapValue httpEndpointConfig, WebSocketConnectionManager connectionManager) {
        HttpResource onUpgradeResource = wsService.getUpgradeResource();
        webSocketHandshaker.getHttpCarbonRequest().setProperty(HttpConstants.RESOURCES_CORS,
                                                               onUpgradeResource.getCorsHeaders());
        AttachedFunction balResource = onUpgradeResource.getBalResource();
        Object[] signatureParams = HttpDispatcher.getSignatureParameters(onUpgradeResource, webSocketHandshaker
                .getHttpCarbonRequest(), httpEndpointConfig);

        ObjectValue httpCaller = (ObjectValue) signatureParams[0];
        httpCaller.addNativeData(WebSocketConstants.WEBSOCKET_MESSAGE, webSocketHandshaker);
        httpCaller.addNativeData(WebSocketConstants.WEBSOCKET_SERVICE, wsService);
        httpCaller.addNativeData(HttpConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER, connectionManager);

        Executor.submit(
                wsService.getScheduler(), onUpgradeResource.getParentService().getBalService(),
                balResource.getName(), new OnUpgradeResourceCallback(
                        webSocketHandshaker, wsService, connectionManager), new HashMap<>(), signatureParams);
    }

    public static void dispatchOnOpen(WebSocketConnection webSocketConnection, ObjectValue webSocketCaller,
                                       WebSocketServerService wsService) {
        AttachedFunction onOpenResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_OPEN);
        if (onOpenResource != null) {
            executeOnOpenResource(wsService, onOpenResource, webSocketCaller,
                                  webSocketConnection);
        } else {
            webSocketConnection.readNextFrame();
        }
    }

    public static void executeOnOpenResource(WebSocketService wsService, AttachedFunction onOpenResource,
                                             ObjectValue webSocketEndpoint, WebSocketConnection webSocketConnection) {
        BType[] parameterTypes = onOpenResource.getParameterType();
        Object[] bValues = new Object[parameterTypes.length * 2];
        bValues[0] = webSocketEndpoint;
        bValues[1] = true;

        CallableUnitCallback onOpenCallableUnitCallback = new CallableUnitCallback() {
            @Override
            public void notifySuccess() {
                webSocketConnection.readNextFrame();
            }

            @Override
            public void notifyFailure(ErrorValue error) {
                webSocketConnection.readNextFrame();
                ErrorHandlerUtils.printError("error: " + error.getPrintableStackTrace());
                WebSocketUtil.closeDuringUnexpectedCondition(webSocketConnection);
            }
        };

        Executor.submit(wsService.getScheduler(), wsService.getBalService(), onOpenResource.getName(),
                        onOpenCallableUnitCallback,
                        null, bValues);
    }
    public static void dispatchOnText(WebSocketOpenConnectionInfo connectionInfo, WebSocketTextMessage textMessage)
            throws IllegalAccessException {
        WebSocketConnection webSocketConnection = connectionInfo.getWebSocketConnection();
        WebSocketService wsService = connectionInfo.getService();
        AttachedFunction onTextMessageResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_TEXT);
        if (onTextMessageResource == null) {
            webSocketConnection.readNextFrame();
            return;
        }
        BType[] parameterTypes = onTextMessageResource.getParameterType();
        Object[] bValues = new Object[parameterTypes.length * 2];
        bValues[0] = connectionInfo.getWebSocketCaller();
        bValues[1] = true;
        boolean finalFragment = textMessage.isFinalFragment();
        BType dataType = parameterTypes[1];
        int dataTypeTag = dataType.getTag();
        if (dataTypeTag == TypeTags.STRING_TAG) {
            bValues[2] = textMessage.getText();
            bValues[3] = true;
            if (parameterTypes.length == 3) {
                bValues[4] = textMessage.isFinalFragment();
                bValues[5] = true;
            }
            Executor.submit(wsService.getScheduler(), wsService.getBalService(), onTextMessageResource.getName(),
                            new WebSocketResourceCallback(webSocketConnection), null, bValues);
        } else if (dataTypeTag == TypeTags.JSON_TAG || dataTypeTag == TypeTags.RECORD_TYPE_TAG ||
                dataTypeTag == TypeTags.XML_TAG || dataTypeTag == TypeTags.ARRAY_TAG) {
            if (finalFragment) {
                connectionInfo.appendAggregateString(textMessage.getText());
                Object aggregate = getAggregatedObject(webSocketConnection, dataType,
                                                       connectionInfo.getAggregateString());
                if (aggregate != null) {
                    bValues[2] = aggregate;
                    bValues[3] = true;
                    Executor.submit(wsService.getScheduler(), wsService.getBalService(),
                                    WebSocketConstants.RESOURCE_NAME_ON_TEXT,
                                    new WebSocketResourceCallback(webSocketConnection), null, bValues);
                }
                connectionInfo.resetAggregateString();
            } else {
                connectionInfo.appendAggregateString(textMessage.getText());
                webSocketConnection.readNextFrame();
            }

        }
    }

    private static Object getAggregatedObject(WebSocketConnection webSocketConnection, BType dataType,
                                              String aggregateString) {
        try {
            switch (dataType.getTag()) {
                case TypeTags.JSON_TAG:
                    return JSONParser.parse(aggregateString);
                case TypeTags.XML_TAG:
                    XMLValue bxml = XMLFactory.parse(aggregateString);
                    if (bxml.getNodeType() != XMLNodeType.ELEMENT) {
                        throw new WebSocketException("Invalid XML data");
                    }
                    return bxml;
                case TypeTags.RECORD_TYPE_TAG:
                    return JSONUtils.convertJSONToRecord(JSONParser.parse(aggregateString),
                                                         (BStructureType) dataType);
                case TypeTags.ARRAY_TAG:
                    if (((BArrayType) dataType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                        return new ArrayValue(
                                aggregateString.getBytes(StandardCharsets.UTF_8));
                    }
                    break;
                default:
                    //Throw an exception because a different type is invalid.
                    //Cannot reach here because of compiler plugin validation.
                    throw new WebSocketException("Invalid resource signature.");
            }
        } catch (WebSocketException ex) {
            webSocketConnection.terminateConnection(1003, ex.detailMessage());
        } catch (Exception ex) {
            webSocketConnection.terminateConnection(1003, WebSocketUtil.getErrorMessage(ex));
            log.error("Data binding failed. Hence connection terminated. ", ex);
        }
        return null;
    }

    public static void dispatchOnBinary(WebSocketOpenConnectionInfo connectionInfo,
                                        WebSocketBinaryMessage binaryMessage)
            throws IllegalAccessException {
        WebSocketConnection webSocketConnection = connectionInfo.getWebSocketConnection();
        WebSocketService wsService = connectionInfo.getService();
        AttachedFunction onBinaryMessageResource = wsService.getResourceByName(
                WebSocketConstants.RESOURCE_NAME_ON_BINARY);
        if (onBinaryMessageResource == null) {
            webSocketConnection.readNextFrame();
            return;
        }
        BType[] paramDetails = onBinaryMessageResource.getParameterType();
        Object[] bValues = new Object[paramDetails.length * 2];
        bValues[0] = connectionInfo.getWebSocketCaller();
        bValues[1] = true;
        bValues[2] = new ArrayValue(binaryMessage.getByteArray());
        bValues[3] = true;
        if (paramDetails.length == 3) {
            bValues[4] = binaryMessage.isFinalFragment();
            bValues[5] = true;
        }
        Executor.submit(wsService.getScheduler(), wsService.getBalService(), WebSocketConstants.RESOURCE_NAME_ON_BINARY,
                        new WebSocketResourceCallback(webSocketConnection), null, bValues);

    }

    public static void dispatchOnPingOnPong(WebSocketOpenConnectionInfo connectionInfo,
                                            WebSocketControlMessage controlMessage)
            throws IllegalAccessException {
        if (controlMessage.getControlSignal() == WebSocketControlSignal.PING) {
            WebSocketResourceDispatcher.dispatchOnPing(connectionInfo, controlMessage);
        } else if (controlMessage.getControlSignal() == WebSocketControlSignal.PONG) {
            WebSocketResourceDispatcher.dispatchOnPong(connectionInfo, controlMessage);
        }
    }

    private static void dispatchOnPing(WebSocketOpenConnectionInfo connectionInfo,
                                       WebSocketControlMessage controlMessage) throws IllegalAccessException {
        WebSocketConnection webSocketConnection = connectionInfo.getWebSocketConnection();
        WebSocketService wsService = connectionInfo.getService();
        AttachedFunction onPingMessageResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_PING);
        if (onPingMessageResource == null) {
            pongAutomatically(controlMessage);
            return;
        }
        BType[] paramTypes = onPingMessageResource.getParameterType();
        Object[] bValues = new Object[paramTypes.length * 2];
        bValues[0] = connectionInfo.getWebSocketCaller();
        bValues[1] = true;
        bValues[2] = new ArrayValue(controlMessage.getByteArray());
        bValues[3] = true;
        Executor.submit(wsService.getScheduler(), wsService.getBalService(), WebSocketConstants.RESOURCE_NAME_ON_PING,
                        new WebSocketResourceCallback(webSocketConnection), null, bValues);
    }

    private static void dispatchOnPong(WebSocketOpenConnectionInfo connectionInfo,
                                       WebSocketControlMessage controlMessage) throws IllegalAccessException {
        WebSocketConnection webSocketConnection = connectionInfo.getWebSocketConnection();
        WebSocketService wsService = connectionInfo.getService();
        AttachedFunction onPongMessageResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_PONG);
        if (onPongMessageResource == null) {
            webSocketConnection.readNextFrame();
            return;
        }
        BType[] paramDetails = onPongMessageResource.getParameterType();
        Object[] bValues = new Object[paramDetails.length * 2];
        bValues[0] = connectionInfo.getWebSocketCaller();
        bValues[1] = true;
        bValues[2] = new ArrayValue(controlMessage.getByteArray());
        bValues[3] = true;
        Executor.submit(wsService.getScheduler(), wsService.getBalService(), WebSocketConstants.RESOURCE_NAME_ON_PONG,
                        new WebSocketResourceCallback(webSocketConnection), null, bValues);
    }

    public static void dispatchOnClose(WebSocketOpenConnectionInfo connectionInfo, WebSocketCloseMessage closeMessage)
            throws IllegalAccessException {
        WebSocketUtil.setListenerOpenField(connectionInfo);
        WebSocketConnection webSocketConnection = connectionInfo.getWebSocketConnection();
        WebSocketService wsService = connectionInfo.getService();
        AttachedFunction onCloseResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_CLOSE);
        int closeCode = closeMessage.getCloseCode();
        String closeReason = closeMessage.getCloseReason();
        if (onCloseResource == null) {
            if (webSocketConnection.isOpen()) {
                if (closeCode == STATUS_CODE_FOR_NO_STATUS_CODE_PRESENT) {
                    webSocketConnection.finishConnectionClosure();
                } else {
                    webSocketConnection.finishConnectionClosure(closeCode, null);
                }
            }
            return;
        }
        BType[] paramDetails = onCloseResource.getParameterType();
        Object[] bValues = new Object[paramDetails.length * 2];
        bValues[0] = connectionInfo.getWebSocketCaller();
        bValues[1] = true;
        bValues[2] = closeCode;
        bValues[3] = true;
        bValues[4] = closeReason == null ? "" : closeReason;
        bValues[5] = true;
        CallableUnitCallback onCloseCallback = new CallableUnitCallback() {
            @Override
            public void notifySuccess() {
                if (closeMessage.getCloseCode() != STATUS_CODE_ABNORMAL_CLOSURE
                        && webSocketConnection.isOpen()) {
                    ChannelFuture finishFuture;
                    if (closeCode == STATUS_CODE_FOR_NO_STATUS_CODE_PRESENT) {
                        finishFuture = webSocketConnection.finishConnectionClosure();
                    } else {
                        finishFuture = webSocketConnection.finishConnectionClosure(closeCode, null);
                    }
                    finishFuture.addListener(closeFuture -> connectionInfo.getWebSocketCaller()
                            .set(WebSocketConstants.LISTENER_IS_SECURE_FIELD, false));
                }
            }

            @Override
            public void notifyFailure(ErrorValue error) {
                ErrorHandlerUtils.printError(error.getPrintableStackTrace());
                WebSocketUtil.closeDuringUnexpectedCondition(webSocketConnection);
            }
        };

        Executor.submit(wsService.getScheduler(), wsService.getBalService(), WebSocketConstants.RESOURCE_NAME_ON_CLOSE,
                        onCloseCallback, null, bValues);
    }

    public static void dispatchOnError(WebSocketOpenConnectionInfo connectionInfo, Throwable throwable) {
        try {
            WebSocketUtil.setListenerOpenField(connectionInfo);
        } catch (IllegalAccessException e) {
            connectionInfo.getWebSocketCaller().set(WebSocketConstants.LISTENER_IS_OPEN_FIELD, false);
        }
        WebSocketService webSocketService = connectionInfo.getService();
        AttachedFunction onErrorResource = webSocketService.getResourceByName(
                WebSocketConstants.RESOURCE_NAME_ON_ERROR);
        if (isUnexpectedError(throwable)) {
            log.error("Unexpected error", throwable);
        }
        if (onErrorResource == null) {
            ErrorHandlerUtils.printError(throwable);
            return;
        }
        Object[] bValues = new Object[onErrorResource.getParameterType().length * 2];
        bValues[0] = connectionInfo.getWebSocketCaller();
        bValues[1] = true;
        bValues[2] = WebSocketUtil.createErrorByType(throwable);
        bValues[3] = true;
        CallableUnitCallback onErrorCallback = new CallableUnitCallback() {
            @Override
            public void notifySuccess() {
                // Do nothing.
            }

            @Override
            public void notifyFailure(ErrorValue error) {
                ErrorHandlerUtils.printError(error.getPrintableStackTrace());
            }
        };

        Executor.submit(webSocketService.getScheduler(), webSocketService.getBalService(),
                        WebSocketConstants.RESOURCE_NAME_ON_ERROR, onErrorCallback, null, bValues);
    }

    private static boolean isUnexpectedError(Throwable throwable) {
        return !(throwable instanceof CorruptedFrameException);
    }

    public static void dispatchOnIdleTimeout(WebSocketOpenConnectionInfo connectionInfo) throws IllegalAccessException {
        WebSocketConnection webSocketConnection = connectionInfo.getWebSocketConnection();
        WebSocketService wsService = connectionInfo.getService();
        AttachedFunction onIdleTimeoutResource = wsService.getResourceByName(
                WebSocketConstants.RESOURCE_NAME_ON_IDLE_TIMEOUT);
        if (onIdleTimeoutResource == null) {
            webSocketConnection.readNextFrame();
            return;
        }
        BType[] paramDetails = onIdleTimeoutResource.getParameterType();
        Object[] bValues = new Object[paramDetails.length * 2];
        bValues[0] = connectionInfo.getWebSocketCaller();
        bValues[1] = true;

        CallableUnitCallback onIdleTimeoutCallback = new CallableUnitCallback() {
            @Override
            public void notifySuccess() {
                // Do nothing.
            }

            @Override
            public void notifyFailure(ErrorValue error) {
                ErrorHandlerUtils.printError(error.getPrintableStackTrace());
                WebSocketUtil.closeDuringUnexpectedCondition(webSocketConnection);
            }
        };

        Executor.submit(wsService.getScheduler(), wsService.getBalService(),
                        WebSocketConstants.RESOURCE_NAME_ON_IDLE_TIMEOUT, onIdleTimeoutCallback, null, bValues);
    }

    private static void pongAutomatically(WebSocketControlMessage controlMessage) {
        WebSocketConnection webSocketConnection = controlMessage.getWebSocketConnection();
        webSocketConnection.pong(controlMessage.getByteBuffer()).addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                ErrorHandlerUtils.printError(cause);
            }
            webSocketConnection.readNextFrame();
        });
    }
}
