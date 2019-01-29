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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.actions.httpclient.AbstractHTTPAction;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.ServerHandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;

import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;


/**
 * Utility class for websockets.
 */
public class WebSocketUtil {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHTTPAction.class);

    public static ProgramFile getProgramFile(Resource resource) {
        return resource.getResourceInfo().getPackageInfo().getProgramFile();
    }

    static Annotation getServiceConfigAnnotation(Service service) {
        List<Annotation> annotationList = service
                .getAnnotationList(HttpConstants.PROTOCOL_PACKAGE_HTTP,
                                   WebSocketConstants.WEBSOCKET_ANNOTATION_CONFIGURATION);

        if (annotationList == null) {
            return null;
        }
        return annotationList.isEmpty() ? null : annotationList.get(0);
    }

    public static void handleHandshake(WebSocketService wsService, WebSocketConnectionManager connectionManager,
                                       HttpHeaders headers, WebSocketHandshaker webSocketHandshaker, Context context,
                                       CallableUnitCallback callback) {
        String[] subProtocols = wsService.getNegotiableSubProtocols();
        int idleTimeoutInSeconds = wsService.getIdleTimeoutInSeconds();
        int maxFrameSize = wsService.getMaxFrameSize();
        ServerHandshakeFuture future = webSocketHandshaker.handshake(subProtocols, idleTimeoutInSeconds * 1000, headers,
                                                                     maxFrameSize);
        future.setHandshakeListener(new ServerHandshakeListener() {
            @Override
            public void onSuccess(WebSocketConnection webSocketConnection) {
                BMap<String, BValue> webSocketEndpoint = BLangConnectorSPIUtil.createObject(
                        wsService.getServiceInfo().getPackageInfo().getProgramFile(), PROTOCOL_PACKAGE_HTTP,
                        WebSocketConstants.WEBSOCKET_CALLER);
                BMap<String, BValue> webSocketConnector = BLangConnectorSPIUtil.createObject(
                        wsService.getServiceInfo().getPackageInfo().getProgramFile(), PROTOCOL_PACKAGE_HTTP,
                        WebSocketConstants.WEBSOCKET_CONNECTOR);

                webSocketEndpoint.put(WebSocketConstants.LISTENER_CONNECTOR_FIELD, webSocketConnector);
                populateEndpoint(webSocketConnection, webSocketEndpoint);
                WebSocketOpenConnectionInfo connectionInfo =
                        new WebSocketOpenConnectionInfo(wsService, webSocketConnection, webSocketEndpoint, context);
                connectionManager.addConnection(webSocketConnection.getChannelId(), connectionInfo);
                webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO,
                                                 connectionInfo);
                if (context != null && callback != null) {
                    context.setReturnValues(webSocketEndpoint);
                    callback.notifySuccess();
                } else {
                    Resource onOpenResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                    if (onOpenResource != null) {
                        executeOnOpenResource(onOpenResource, webSocketEndpoint, webSocketConnection);
                    } else {
                        readFirstFrame(webSocketConnection, webSocketConnector);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (context != null && callback != null) {
                    callback.notifyFailure(HttpUtil.getError(
                            context, "Unable to complete handshake:" + throwable.getMessage()));
                } else {
                    throw new BallerinaConnectorException("Unable to complete handshake", throwable);
                }
            }
        });
    }

    public static void executeOnOpenResource(Resource onOpenResource, BMap<String, BValue> webSocketEndpoint,
                                             WebSocketConnection webSocketConnection) {
        List<ParamDetail> paramDetails =
                onOpenResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = webSocketEndpoint;
        BMap<String, BValue> webSocketConnector =
                (BMap<String, BValue>) webSocketEndpoint.get(WebSocketConstants.LISTENER_CONNECTOR_FIELD);

        CallableUnitCallback onOpenCallableUnitCallback = new CallableUnitCallback() {
            @Override
            public void notifySuccess() {
                boolean isReady = ((BBoolean) webSocketConnector.get(WebSocketConstants.CONNECTOR_IS_READY_FIELD))
                        .booleanValue();
                if (!isReady) {
                    readFirstFrame(webSocketConnection, webSocketConnector);
                }
            }

            @Override
            public void notifyFailure(BError error) {
                boolean isReady = ((BBoolean) webSocketConnector.get(WebSocketConstants.CONNECTOR_IS_READY_FIELD))
                        .booleanValue();
                if (!isReady) {
                    readFirstFrame(webSocketConnection, webSocketConnector);
                }
                ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
                closeDuringUnexpectedCondition(webSocketConnection);
            }
        };
        Executor.submit(onOpenResource, onOpenCallableUnitCallback, null, null, bValues);
    }

    public static void populateEndpoint(WebSocketConnection webSocketConnection,
                                        BMap<String, BValue> webSocketEndpoint) {
        webSocketEndpoint.put(WebSocketConstants.LISTENER_ID_FIELD, new BString(webSocketConnection.getChannelId()));
        webSocketEndpoint.put(WebSocketConstants.LISTENER_NEGOTIATED_SUBPROTOCOLS_FIELD,
                              new BString(webSocketConnection.getNegotiatedSubProtocol()));
        webSocketEndpoint.put(WebSocketConstants.LISTENER_IS_SECURE_FIELD,
                              new BBoolean(webSocketConnection.isSecure()));
        webSocketEndpoint.put(WebSocketConstants.LISTENER_IS_OPEN_FIELD,
                              new BBoolean(webSocketConnection.isOpen()));
    }

    public static void handleWebSocketCallback(Context context, CallableUnitCallback callback,
                                               ChannelFuture webSocketChannelFuture) {
        webSocketChannelFuture.addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                context.setReturnValues(HttpUtil.getError(context, cause));
            } else {
                context.setReturnValues();
            }
            callback.notifySuccess();
        });
    }

    public static void readFirstFrame(WebSocketConnection webSocketConnection,
                                      BMap<String, BValue> webSocketConnector) {
        webSocketConnection.readNextFrame();
        webSocketConnector.put(WebSocketConstants.CONNECTOR_IS_READY_FIELD, new BBoolean(true));
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
        connectionInfo.getWebSocketEndpoint().put(WebSocketConstants.LISTENER_IS_OPEN_FIELD,
                                                  new BBoolean(connectionInfo.getWebSocketConnection().isOpen()));
    }

    public static int findMaxFrameSize(Struct annotation) {
        long size = annotation.getIntField(WebSocketConstants.ANNOTATION_ATTR_MAX_FRAME_SIZE);
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

    public static int findIdleTimeoutInSeconds(Struct annAttrIdleTimeout) {
        long timeout = annAttrIdleTimeout.getIntField(WebSocketConstants.ANNOTATION_ATTR_IDLE_TIMEOUT);
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

    public static String[] findNegotiableSubProtocols(Struct annAttrSubProtocols) {
        Value[] subProtocolsInAnnotation = annAttrSubProtocols.getArrayField(
                WebSocketConstants.ANNOTATION_ATTR_SUB_PROTOCOLS);
        if (subProtocolsInAnnotation == null) {
            return new String[0];
        }
        return  Arrays.stream(subProtocolsInAnnotation).map(Value::getStringValue)
                .toArray(String[]::new);
    }

    private WebSocketUtil() {
    }
}
