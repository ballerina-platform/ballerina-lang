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
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

import java.util.List;
import javax.websocket.Session;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;


/**
 * Utility class for websockets.
 */
public abstract class WebSocketUtil {
    public static BMap getQueryParams(Context context) {
        BStruct wsConnection = (BStruct) context.getRefArgument(0);
        Object queryParams = wsConnection.getNativeData(WebSocketConstants.NATIVE_DATA_QUERY_PARAMS);
        if (queryParams != null && queryParams instanceof BMap) {
            return (BMap) wsConnection.getNativeData(WebSocketConstants.NATIVE_DATA_QUERY_PARAMS);
        }
        return new BMap<>();
    }

    public static ProgramFile getProgramFile(Resource resource) {
        return resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();
    }

    public static Annotation getServiceConfigAnnotation(Service service, String pkgPath) {
        List<Annotation> annotationList = service
                .getAnnotationList(pkgPath, WebSocketConstants.WEBSOCKET_ANNOTATION_CONFIGURATION);

        if (annotationList == null) {
            return null;
        }

        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple service configuration annotations found in service: " + service.getName());
        }

        return annotationList.isEmpty() ? null : annotationList.get(0);
    }

    public static void handleHandshake(WebSocketService wsService, WebSocketConnectionManager connectionManager,
                                       HttpHeaders headers, WebSocketInitMessage initMessage, Context context,
                                       CallableUnitCallback callback) {
        String[] subProtocols = wsService.getNegotiableSubProtocols();
        int idleTimeoutInSeconds = wsService.getIdleTimeoutInSeconds();
        HandshakeFuture future = initMessage.handshake(subProtocols, true, idleTimeoutInSeconds * 1000, headers);
        future.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                // TODO: Need to create new struct
                BStruct webSocketEndpoint = BLangConnectorSPIUtil.createObject(
                        wsService.getResources()[0].getResourceInfo().getServiceInfo().getPackageInfo()
                                .getProgramFile(), PROTOCOL_PACKAGE_HTTP, WebSocketConstants.WEBSOCKET_ENDPOINT);
                BStruct webSocketConnector = BLangConnectorSPIUtil.createObject(
                        wsService.getResources()[0].getResourceInfo().getServiceInfo().getPackageInfo()
                                .getProgramFile(), PROTOCOL_PACKAGE_HTTP, WebSocketConstants.WEBSOCKET_CONNECTOR);

                webSocketEndpoint.setRefField(1, webSocketConnector);
                populateEndpoint(session, webSocketEndpoint);
                webSocketConnector.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_SESSION, session);
                WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(wsService,
                                                                                             webSocketEndpoint);
                connectionManager.addConnection(session.getId(), connectionInfo);
                webSocketConnector.addNativeData(WebSocketConstants.WEBSOCKET_CONNECTION_MANAGER, connectionManager);
                if (context != null && callback != null) {
                    context.setReturnValues(webSocketEndpoint);
                    callback.notifySuccess();
                } else {
                    Resource onOpenResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                    if (onOpenResource != null) {
                        List<ParamDetail> paramDetails = onOpenResource.getParamDetails();
                        BValue[] bValues = new BValue[paramDetails.size()];
                        bValues[0] = webSocketEndpoint;
                        //TODO handle BallerinaConnectorException
                        Executor.submit(onOpenResource, new WebSocketEmptyCallableUnitCallback(), null, null, bValues);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (context != null) {
                    context.setReturnValues();
                }
                if (callback != null) {
                    callback.notifyFailure(BLangConnectorSPIUtil
                                                   .createBStruct(context, HttpConstants.PROTOCOL_PACKAGE_HTTP,
                                                                  WebSocketConstants.WEBSOCKET_CONNECTOR_ERROR,
                                                                  "Unable to complete handshake: " +
                                                                          throwable.getMessage()));
                }
                throw new BallerinaConnectorException("Unable to complete handshake", throwable);
            }
        });
    }

    public static void populateEndpoint(Session session, BStruct webSocketEndpoint) {
        webSocketEndpoint.setStringField(0, session.getId());
        webSocketEndpoint.setStringField(1, session.getNegotiatedSubprotocol());
        webSocketEndpoint.setBooleanField(0, session.isSecure() ? 1 : 0);
        webSocketEndpoint.setBooleanField(1, session.isOpen() ? 1 : 0);
    }

    public static void getWebSocketError(Context context, CallableUnitCallback callback,
                                         ChannelFuture webSocketChannelFuture, String message)
            throws InterruptedException {
        webSocketChannelFuture.addListener((ChannelFutureListener) future1 -> {
            Throwable cause = future1.cause();
            if (!future1.isSuccess() && cause != null) {
                context.setReturnValues(BLangConnectorSPIUtil
                                                .createBStruct(context, HttpConstants.PROTOCOL_PACKAGE_HTTP,
                                                               WebSocketConstants.WEBSOCKET_CONNECTOR_ERROR, message,
                                                               new BValue[]{}));
            } else {
                context.setReturnValues();
            }
            callback.notifySuccess();
        });
    }

    /**
     * Refactor the given URI.
     *
     * @param uri URI to refactor.
     * @return refactored URI.
     */
    public static String refactorUri(String uri) {
        if (!uri.startsWith("/")) {
            uri = "/".concat(uri);
        }

        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }
}
