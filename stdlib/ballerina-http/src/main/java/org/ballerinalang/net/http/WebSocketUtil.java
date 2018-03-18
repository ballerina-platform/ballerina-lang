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

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.websocket.HandshakeFuture;
import org.wso2.transport.http.netty.contract.websocket.HandshakeListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

import java.util.List;
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

    public static BConnector createAndGetConnector(Resource resource) {
        return BLangConnectorSPIUtil.createBConnector(getProgramFile(resource), HttpConstants.HTTP_PACKAGE_PATH,
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

    public static void handleHandshake(WebSocketInitMessage initMessage, WebSocketService wsService,
                                       BMap<String, BString> queryParams, HttpHeaders headers) {
        String[] subProtocols = wsService.getNegotiableSubProtocols();
        int idleTimeoutInSeconds = wsService.getIdleTimeoutInSeconds();
        HandshakeFuture future = initMessage.handshake(subProtocols, true, idleTimeoutInSeconds * 1000, headers);
        future.setHandshakeListener(new HandshakeListener() {
            @Override
            public void onSuccess(Session session) {
                BConnector wsConnection = WebSocketUtil.createAndGetConnector(wsService.getResources()[0]);
                wsConnection.setNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_SESSION, session);
                wsConnection.setNativeData(WebSocketConstants.WEBSOCKET_MESSAGE, initMessage);
                wsConnection.setNativeData(WebSocketConstants.NATIVE_DATA_UPGRADE_HEADERS, initMessage.getHeaders());
                wsConnection.setNativeData(WebSocketConstants.NATIVE_DATA_QUERY_PARAMS, queryParams);
                WebSocketConnectionManager.getInstance().addConnection(session.getId(),
                                                                       new WebSocketOpenConnectionInfo(wsService,
                                                                                                       wsConnection));

                Resource onOpenResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_OPEN);
                if (onOpenResource == null) {
                    return;
                }
                List<ParamDetail> paramDetails =
                        onOpenResource.getParamDetails();
                BValue[] bValues = new BValue[paramDetails.size()];
                bValues[0] = wsConnection;
                //TODO handle BallerinaConnectorException
                Executor.submit(onOpenResource, new WebSocketEmptyCallableUnitCallback(), null, bValues);
            }

            @Override
            public void onError(Throwable throwable) {
                ErrorHandlerUtils.printError(throwable);
            }
        });
    }
}
