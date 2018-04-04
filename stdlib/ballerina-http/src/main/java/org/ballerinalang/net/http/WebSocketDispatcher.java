/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contract.websocket.WebSocketMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.websocket.Session;

/**
 * {@code WebSocketDispatcher} This is the web socket request dispatcher implementation which finds best matching
 * resource for incoming web socket request.
 *
 * @since 0.94
 */
public class WebSocketDispatcher {

    /**
     * This will find the best matching service for given web socket request.
     *
     * @param webSocketMessage incoming message.
     * @return matching service.
     */
    public static WebSocketService findService(WebSocketServicesRegistry servicesRegistry,
                                               Map<String, String> pathParams, WebSocketMessage webSocketMessage,
                                               HTTPCarbonMessage msg) {
        try {
            String serviceUri = webSocketMessage.getTarget();
            serviceUri = WebSocketUtil.refactorUri(serviceUri);
            URI requestUri;
            try {
                requestUri = URI.create(serviceUri);
            } catch (IllegalArgumentException e) {
                throw new BallerinaConnectorException(e.getMessage());
            }
            WebSocketService service = servicesRegistry.getUriTemplate().matches(requestUri.getPath(), pathParams,
                                                                                 webSocketMessage);
            if (service == null) {
                throw new BallerinaConnectorException("no Service found to handle the service request: " + serviceUri);
            }
            msg.setProperty(HttpConstants.QUERY_STR, requestUri.getRawQuery());
            return service;
        } catch (Throwable throwable) {
            ErrorHandlerUtils.printError(throwable);
            throw new BallerinaConnectorException("no Service found to handle the service request");
        }
    }

    public static void dispatchTextMessage(WebSocketOpenConnectionInfo connectionInfo,
                                           WebSocketTextMessage textMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onTextMessageResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_TEXT);
        if (onTextMessageResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onTextMessageResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = connectionInfo.getWebSocketEndpoint();
        bValues[1] = new BString(textMessage.getText());
        if (paramDetails.size() == 3) {
            bValues[2] = new BBoolean(!textMessage.isFinalFragment());
        }
        //TODO handle BallerinaConnectorException
        Executor.submit(onTextMessageResource, new WebSocketEmptyCallableUnitCallback(), null,
                        null, bValues);
    }

    public static void dispatchBinaryMessage(WebSocketOpenConnectionInfo connectionInfo,
                                             WebSocketBinaryMessage binaryMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onBinaryMessageResource = wsService.getResourceByName(
                WebSocketConstants.RESOURCE_NAME_ON_BINARY);
        if (onBinaryMessageResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onBinaryMessageResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = connectionInfo.getWebSocketEndpoint();
        bValues[1] = new BBlob(binaryMessage.getByteArray());
        if (paramDetails.size() == 3) {
            bValues[2] = new BBoolean(!binaryMessage.isFinalFragment());
        }
        //TODO handle BallerinaConnectorException
        Executor.submit(onBinaryMessageResource, new WebSocketEmptyCallableUnitCallback(), null,
                        null, bValues);
    }

    public static void dispatchControlMessage(WebSocketOpenConnectionInfo connectionInfo,
                                              WebSocketControlMessage controlMessage) {
        if (controlMessage.getControlSignal() == WebSocketControlSignal.PING) {
            WebSocketDispatcher.dispatchPingMessage(connectionInfo, controlMessage);
        } else if (controlMessage.getControlSignal() == WebSocketControlSignal.PONG) {
            WebSocketDispatcher.dispatchPongMessage(connectionInfo, controlMessage);
        } else {
            throw new BallerinaConnectorException("Received unknown control signal");
        }
    }

    private static void dispatchPingMessage(WebSocketOpenConnectionInfo connectionInfo,
                                            WebSocketControlMessage controlMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onPingMessageResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_PING);
        if (onPingMessageResource == null) {
            pingAutomatically(controlMessage);
            return;
        }
        List<ParamDetail> paramDetails = onPingMessageResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = connectionInfo.getWebSocketEndpoint();
        bValues[1] = new BBlob(controlMessage.getByteArray());
        //TODO handle BallerinaConnectorException
        Executor.submit(onPingMessageResource, new WebSocketEmptyCallableUnitCallback(), null,
                        null, bValues);
    }

    private static void dispatchPongMessage(WebSocketOpenConnectionInfo connectionInfo,
                                            WebSocketControlMessage controlMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onPongMessageResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_PONG);
        if (onPongMessageResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onPongMessageResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = connectionInfo.getWebSocketEndpoint();
        bValues[1] = new BBlob(controlMessage.getByteArray());
        //TODO handle BallerinaConnectorException
        Executor.submit(onPongMessageResource, new WebSocketEmptyCallableUnitCallback(), null,
                        null, bValues);
    }

    public static void dispatchCloseMessage(WebSocketOpenConnectionInfo connectionInfo,
                                            WebSocketCloseMessage closeMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onCloseResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_CLOSE);
        if (onCloseResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onCloseResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = connectionInfo.getWebSocketEndpoint();
        bValues[1] = new BInteger(closeMessage.getCloseCode());
        bValues[2] = new BString(closeMessage.getCloseReason());
        //TODO handle BallerinaConnectorException
        Executor.submit(onCloseResource, new WebSocketEmptyCallableUnitCallback(), null,
                        null, bValues);
    }

    public static void dispatchIdleTimeout(WebSocketOpenConnectionInfo connectionInfo,
                                           WebSocketControlMessage controlMessage) {
        WebSocketService wsService = connectionInfo.getService();
        Resource onIdleTimeoutResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_IDLE_TIMEOUT);
        if (onIdleTimeoutResource == null) {
            return;
        }
        List<ParamDetail> paramDetails = onIdleTimeoutResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = connectionInfo.getWebSocketEndpoint();
        //TODO handle BallerinaConnectorException
        Executor.submit(onIdleTimeoutResource, new WebSocketEmptyCallableUnitCallback(), null,
                        null, bValues);
    }

    private static void pingAutomatically(WebSocketControlMessage controlMessage) {
        Session session = controlMessage.getChannelSession();
        try {
            session.getAsyncRemote().sendPong(controlMessage.getPayload());
        } catch (IOException ex) {
            ErrorHandlerUtils.printError(ex);
        }
    }

    public static void setPathParams(BValue[] bValues, List<ParamDetail> paramDetails, Map<String, String> pathParams,
                                     int defaultArgSize) {
        int parameterDetailsSize = paramDetails.size();
        if (parameterDetailsSize > defaultArgSize) {
            for (int i = defaultArgSize; i < parameterDetailsSize; i++) {
                bValues[i] = new BString(pathParams.get(paramDetails.get(i).getVarName()));
            }
        }
    }
}
