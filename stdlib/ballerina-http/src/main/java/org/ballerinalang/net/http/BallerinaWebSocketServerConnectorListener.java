/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.http;

import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.mime.util.Constants;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketInitMessage;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.SERVICE_ENDPOINT;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class BallerinaWebSocketServerConnectorListener implements WebSocketConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaWebSocketServerConnectorListener.class);
    private final WebSocketServicesRegistry servicesRegistry;
    private final WebSocketConnectionManager connectionManager;

    public BallerinaWebSocketServerConnectorListener(WebSocketServicesRegistry servicesRegistry) {
        this.servicesRegistry = servicesRegistry;
        this.connectionManager = WebSocketConnectionManager.getInstance();
    }

    @Override
    public void onMessage(WebSocketInitMessage webSocketInitMessage) {
        BMap<String, BString> queryParams = new BMap<>();
        WebSocketService wsService = WebSocketDispatcher.findService(servicesRegistry, webSocketInitMessage,
                                                                     queryParams);
        Resource onUpgradeResource = wsService.getResourceByName(WebSocketConstants.RESOURCE_NAME_ON_UPGRADE);
        if (onUpgradeResource != null) {
            Semaphore semaphore = new Semaphore(0);
            AtomicBoolean isResourceExeSuccessful = new AtomicBoolean(false);
            // TODO: Resource should be able to run without any parameter.
            BConnector serverConnector = BLangConnectorSPIUtil.createBConnector(
                    WebSocketUtil.getProgramFile(wsService.getResources()[0]),
                    PROTOCOL_PACKAGE_HTTP, SERVICE_ENDPOINT);
            serverConnector.setNativeData(WebSocketConstants.WEBSOCKET_MESSAGE, webSocketInitMessage);
            serverConnector.setNativeData(WebSocketConstants.NATIVE_DATA_QUERY_PARAMS, queryParams);
            serverConnector.setNativeData(WebSocketConstants.WEBSOCKET_SERVICE, wsService);
//            serverConnector.setStringField(0, webSocketInitMessage.getSessionID());
//            serverConnector.setBooleanField(0, webSocketInitMessage.isConnectionSecured() ? 1 : 0);

            BStruct inRequest = BLangConnectorSPIUtil.createBStruct(
                    WebSocketUtil.getProgramFile(wsService.getResources()[0]), PROTOCOL_PACKAGE_HTTP,
                    HttpConstants.REQUEST);
            BStruct inRequestEntity = BLangConnectorSPIUtil.createBStruct(
                    WebSocketUtil.getProgramFile(wsService.getResources()[0]),
                    org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME, Constants.ENTITY);

            BStruct mediaType = BLangConnectorSPIUtil.createBStruct(
                    WebSocketUtil.getProgramFile(wsService.getResources()[0]),
                    org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME, Constants.MEDIA_TYPE);
            HttpUtil.populateInboundRequest(inRequest, inRequestEntity, mediaType, new HTTPCarbonMessage(
                    ((DefaultWebSocketInitMessage) webSocketInitMessage).getHttpRequest()));

            // Creating map
            Map<String, String> upgradeHeaders = webSocketInitMessage.getHeaders();
            BMap<String, BString> bUpgradeHeaders = new BMap<>();
            upgradeHeaders.forEach((headerKey, headerVal) -> bUpgradeHeaders.put(headerKey, new BString(headerVal)));
            serverConnector.setRefField(0, bUpgradeHeaders);
            List<ParamDetail> paramDetails = onUpgradeResource.getParamDetails();
            BValue[] bValues = new BValue[paramDetails.size()];
            bValues[0] = serverConnector;
            bValues[1] = inRequest;
            Executor.submit(onUpgradeResource, new CallableUnitCallback() {
                @Override
                public void notifySuccess() {
                    isResourceExeSuccessful.set(true);
                    semaphore.release();
                }

                @Override
                public void notifyFailure(BStruct error) {
                    ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
                    semaphore.release();
                }
            }, null, bValues);
            try {
                semaphore.acquire();
                if (isResourceExeSuccessful.get() && !webSocketInitMessage.isCancelled() &&
                        !webSocketInitMessage.isHandshakeStarted()) {
                    WebSocketUtil.handleHandshake(webSocketInitMessage, wsService, queryParams, null);
                }
            } catch (InterruptedException e) {
                throw new BallerinaConnectorException("Connection interrupted during handshake");
            }

        } else {
            WebSocketUtil.handleHandshake(webSocketInitMessage, wsService, queryParams, null);
        }
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        WebSocketOpenConnectionInfo wsService = connectionManager.getConnectionInfo(
                webSocketTextMessage.getSessionID());
        WebSocketDispatcher.dispatchTextMessage(wsService, webSocketTextMessage);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        WebSocketOpenConnectionInfo wsService = connectionManager.getConnectionInfo(
                webSocketBinaryMessage.getSessionID());
        WebSocketDispatcher.dispatchBinaryMessage(wsService, webSocketBinaryMessage);
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        WebSocketOpenConnectionInfo wsService = connectionManager.getConnectionInfo(
                webSocketControlMessage.getSessionID());
        WebSocketDispatcher.dispatchControlMessage(wsService, webSocketControlMessage);
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        WebSocketOpenConnectionInfo wsService = connectionManager.removeConnection(
                webSocketCloseMessage.getSessionID());
        WebSocketDispatcher.dispatchCloseMessage(wsService, webSocketCloseMessage);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Unexpected error occurred in WebSocket transport", throwable);
    }

    @Override
    public void onIdleTimeout(WebSocketControlMessage controlMessage) {
        WebSocketOpenConnectionInfo wsService = connectionManager.getConnectionInfo(controlMessage.getSessionID());
        WebSocketDispatcher.dispatchIdleTimeout(wsService, controlMessage);
    }

}


