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
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.websocket.Session;


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

    public static BStruct createAndGetBStruct(Resource resource) {
        return BLangConnectorSPIUtil.createBStruct(getProgramFile(resource), HttpConstants.HTTP_PACKAGE_PATH,
                                                   WebSocketConstants.WEBSOCKET_CONNECTOR, new BMap<>());
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

    public static void handleHandshake(WebSocketService wsService,
                                       HttpHeaders headers, BStruct wsConnection) {
        String[] subProtocols = wsService.getNegotiableSubProtocols();
        WebSocketInitMessage initMessage =
                (WebSocketInitMessage) wsConnection.getNativeData(WebSocketConstants.WEBSOCKET_MESSAGE);
        int idleTimeoutInSeconds = wsService.getIdleTimeoutInSeconds();
        HandshakeFuture future = initMessage.handshake(subProtocols, true, idleTimeoutInSeconds * 1000, headers);
        future.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                BStruct serviceEndpoint = (BStruct) wsConnection.getNativeData(WebSocketConstants.WEBSOCKET_ENDPOINT);
                populateEndpoint(session, serviceEndpoint);
                wsConnection.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_SESSION, session);
                WebSocketOpenConnectionInfo connectionInfo = new WebSocketOpenConnectionInfo(wsService,
                                                                                             serviceEndpoint);
                WebSocketConnectionManager.getInstance().addConnection(session.getId(), connectionInfo);

                Resource onOpenResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                if (onOpenResource == null) {
                    return;
                }
                List<ParamDetail> paramDetails =
                        onOpenResource.getParamDetails();
                BValue[] bValues = new BValue[paramDetails.size()];
                bValues[0] = serviceEndpoint;
                //TODO handle BallerinaConnectorException
                Executor.submit(onOpenResource, new WebSocketEmptyCallableUnitCallback(), null, null, bValues);
            }

            @Override
            public void onError(Throwable throwable) {
                ErrorHandlerUtils.printError(throwable);
            }
        });
    }

    public static void populateEndpoint(Session session, BStruct endpoint) {
        endpoint.setStringField(0, session.getId());
        endpoint.setStringField(1, session.getNegotiatedSubprotocol());
        endpoint.setBooleanField(0, session.isSecure() ? 1 : 0);
        endpoint.setBooleanField(1, session.isOpen() ? 1 : 0);
    }

    public static BValue[] getWebSocketError(Context context, ChannelFuture webSocketChannelFuture, String message)
            throws InterruptedException {
        AtomicReference<BValue[]> error = new AtomicReference<>();
        webSocketChannelFuture.addListener((ChannelFutureListener) future1 -> {
            Throwable cause = future1.cause();
            if (!future1.isSuccess() && cause != null) {
                error.set(new BValue[]{BLangConnectorSPIUtil.createBStruct(context, HttpConstants.PROTOCOL_PACKAGE_HTTP,
                                                                           WebSocketConstants.WEBSOCKET_CONNECTOR_ERROR,
                                                                           message, new BValue[]{})});
            } else {
                error.set(new BValue[0]);
            }
        });
        webSocketChannelFuture.sync();
        return error.get();
    }
}
