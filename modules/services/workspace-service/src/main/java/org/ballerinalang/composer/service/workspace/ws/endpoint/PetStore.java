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

package org.ballerinalang.composer.service.workspace.ws.endpoint;

import com.google.gson.Gson;
import org.ballerinalang.composer.service.workspace.ws.ApiHandlerRegistry;
import org.ballerinalang.composer.service.workspace.ws.exception.BallerinaWebSocketException;
import org.ballerinalang.composer.service.workspace.ws.model.JSONRPCError;
import org.ballerinalang.composer.service.workspace.ws.model.JSONRPCRequest;
import org.ballerinalang.composer.service.workspace.ws.model.JSONRPCResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.websocket.WebSocketEndpoint;

import java.io.IOException;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * This is a Sample class for WebSocket.
 * This provides a pet store
 */

@ServerEndpoint(value = "/pet-store")
public class PetStore implements WebSocketEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetStore.class);
    Session userSession = null;
    private Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        userSession = session;
        LOGGER.info("Opening websocket service pet-store");
    }

    @OnMessage
    public void onTextMessage(String text, Session session) throws IOException, BallerinaWebSocketException {
        LOGGER.info("Received request from : " + text + " from  " + session.getId());
        JSONRPCRequest jsonrpcRequest = null;
        JSONRPCResponse jsonrpcResponse = new JSONRPCResponse();
        String responseStr = null;
        // Check if the text sent by the client is a valid JSON
        if(isJSONValid(text)) {
            jsonrpcRequest = gson.fromJson(text, JSONRPCRequest.class);
            // Call handler based method
            Object responseObj = ApiHandlerRegistry.getInstance().callHandler(jsonrpcRequest);
            // Check if response object is null or not
            if (responseObj != null) {
                jsonrpcResponse = jsonrpcResponse.buildResponseString(jsonrpcRequest, responseObj);
            }
        } else {
            JSONRPCError jsonrpcError = new JSONRPCError();
            jsonrpcError.setCode(-32600);
            jsonrpcError.setMessage("Invalid Request");
            // Invalid json passed from client
            jsonrpcResponse = jsonrpcResponse.buildResponseString(jsonrpcRequest, jsonrpcError );
        }
        // Convert the response object to a json string
        responseStr = gson.toJson(jsonrpcResponse);
        // Sync the server and client
        sendMessageToAll(responseStr);
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        LOGGER.info("Connection is closed with status code : " + closeReason.getCloseCode().getCode()
                + " On reason " + closeReason.getReasonPhrase());
        userSession = null;
        sendMessageToAll("Closing websocket service pet-store");
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        LOGGER.error("Error found in method : " + throwable.toString());
    }


    private void sendMessageToAll(String message) {
        try {
            userSession.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isJSONValid(String jsonInString) {
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }
}