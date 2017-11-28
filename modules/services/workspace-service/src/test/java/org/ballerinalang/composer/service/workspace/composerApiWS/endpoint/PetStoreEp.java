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

package org.ballerinalang.composer.service.workspace.composerApiWS.endpoint;

import com.google.gson.Gson;
import org.ballerinalang.composer.service.workspace.composerApiWS.utils.RequestHandler;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.websocket.WebSocketEndpoint;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * This is a Sample class for WebSocket.
 * This provides a pet store
 */

@ServerEndpoint(value = "/pet-store")
public class PetStoreEp implements WebSocketEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetStoreEp.class);
    Session userSession = null;
    RequestHandler requestHandler = new RequestHandler();
    Endpoint serviceAsEndpoint;
    private Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        PetStoreApiImpl petStoreservice = new PetStoreApiImpl();
        serviceAsEndpoint = ServiceEndpoints.toEndpoint(petStoreservice);
        userSession = session;
        LOGGER.info("Opening websocket service pet-store");
    }

    @OnMessage
    public void onTextMessage(String text, Session session) throws IOException {
        LOGGER.info("Received request from : " + text + " from  " + session.getId());
            String responseStr = requestHandler.routeRequestAndNotify(serviceAsEndpoint, text);
            if (responseStr != null) {
                // Sync the server and client
                sync(responseStr);
            }
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        LOGGER.info("Connection is closed with status code : " + closeReason.getCloseCode().getCode()
                + " On reason " + closeReason.getReasonPhrase());
        userSession = null;
        sync("Closing websocket service pet-store");
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        LOGGER.error("Error found in method : " + throwable.toString());
    }


    private void sync(String message) {
        try {
            userSession.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}