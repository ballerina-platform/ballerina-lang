/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.composer.service.ballerina.launcher.service;

import org.ballerinalang.composer.server.core.ServerConfig;
import org.ballerinalang.composer.server.core.ServerConstants;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ServiceInfo;
import org.ballerinalang.composer.server.spi.ServiceType;
import org.ballerinalang.composer.service.ballerina.launcher.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Micro service for ballerina launcher.
 */
@ServerEndpoint(value = ServerConstants.CONTEXT_ROOT + "/" + Constants.SERVICE_PATH)
public class BallerinaLauncherService implements ComposerService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaLauncherService.class);

    private ServerConfig serverConfig;

    public BallerinaLauncherService() {
    }

    public BallerinaLauncherService(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @OnOpen
    public void onOpen (Session session) {
        LaunchManager.getInstance(serverConfig).setSession(session);
    }

    @OnMessage
    public void onTextMessage(String request, Session session) {
        LaunchManager.getInstance(serverConfig).setSession(session);
        LaunchManager.getInstance(serverConfig).processCommand(request);
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        LaunchManager.getInstance(serverConfig).stopProcess();
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        logger.error("Error found in method : " + throwable.toString());
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return new ServiceInfo(Constants.SERVICE_NAME, Constants.SERVICE_PATH, ServiceType.WS);
    }
}
