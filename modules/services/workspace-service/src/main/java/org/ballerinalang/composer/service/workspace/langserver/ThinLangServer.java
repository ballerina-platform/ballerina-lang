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
package org.ballerinalang.composer.service.workspace.langserver;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.ballerinalang.composer.service.workspace.composerapi.ComposerApi;
import org.ballerinalang.composer.service.workspace.composerapi.ComposerApiImpl;
import org.ballerinalang.composer.service.workspace.composerapi.utils.RequestHandler;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * This is a Sample class for WebSocket.
 * This provides a chat with multiple users.
 */

@ServerEndpoint(value = "/blangserve2")
public class ThinLangServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThinLangServer.class);
    private List<Session> sessions = new LinkedList<Session>();
    PipedOutputStream pipedOutputStream = new PipedOutputStream();
    PipedInputStream inputStream = new PipedInputStream();
    BallerinaLanguageServer server = new BallerinaLanguageServer();
    Endpoint endpoint = ServiceEndpoints.toEndpoint(server);
    private Gson gson = new Gson();

    @OnOpen
    public void onOpen (Session session) throws IOException, ExecutionException, InterruptedException {
        sessions.add(session);
        inputStream .connect(pipedOutputStream);
        BallerinaLanguageServer server = new BallerinaLanguageServer();
        Launcher<LanguageClient> l = LSPLauncher.createServerLauncher(server, inputStream,
                session.getBasicRemote().getSendStream());
        LanguageClient client = l.getRemoteProxy();
        ((LanguageClientAware) server).connect(client);
        Future<?> startListening = l.startListening();
        startListening.get();
    }

    @OnMessage
    public void onTextMessage(String request, Session session) throws IOException {
//        String json = request.split("Content-Length:\\s[\\d]*\\s\\n")[1];
//        RequestMessage jsonrpcRequest = gson.fromJson(json, RequestMessage.class);
//        ResponseMessage jsonrpcResponse = null;
//        ResponseError responseError = null;
//        String responseStr = null;
//        JsonRpcMethod delegateMethod = ServiceEndpoints.getSupportedMethods(BallerinaLanguageServer.class)
//                .get(jsonrpcRequest.getMethod());
//        if (delegateMethod != null) {
//            // Cast parameters to the type requested by the delegate method
//            Class paramCls = (Class) delegateMethod.getParameterTypes()[0];
//            CompletableFuture completableFutureResp = endpoint.request(jsonrpcRequest.getMethod(),
//                    new Gson().fromJson(new Gson().toJson((jsonrpcRequest.getParams())), paramCls));
//            jsonrpcResponse = handleResult(jsonrpcRequest, completableFutureResp);
//        } else {
//            jsonrpcResponse = new ResponseMessage();
//            jsonrpcResponse.setId(jsonrpcRequest.getId());
//            responseError = handleError(-32601, "Method not found");
//            jsonrpcResponse.setError(responseError);
//        }
//        String jsonTxt = gson.toJson(jsonrpcResponse);
//        responseStr = "Content-Length: " + jsonTxt.length() + " \n\n" + jsonTxt;
//        session.getBasicRemote().sendText(responseStr);
        byte[] bytes = request.getBytes();
        pipedOutputStream.write(bytes, 0, bytes.length);
        pipedOutputStream.flush();
    }

    @OnMessage
    public void onBinaryMessage(byte[] bytes, Session session) {
        LOGGER.info("Reading binary Message");
        LOGGER.info(bytes.toString());
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        LOGGER.info("Connection is closed with status code : " + closeReason.getCloseCode().getCode()
                + " On reason " + closeReason.getReasonPhrase());
        sessions.remove(session);
        String msg = " left the chat";
        sendMessageToAll(msg);
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        LOGGER.error("Error found in method : " + throwable.toString());
    }

    /**
     * Handle the result/response from the endpoint.
     *
     * @param jsonrpcRequest        JSON RPC Request object
     * @param completableFutureResp Result of the request sent
     * @return JSON RPC Response object
     */
    public ResponseMessage handleResult(RequestMessage jsonrpcRequest, CompletableFuture completableFutureResp) {
        ResponseMessage jsonrpcResponse = new ResponseMessage();
        ResponseError responseError = null;
        // Check if response object is null or not
        if (completableFutureResp != null) {
            try {
                jsonrpcResponse.setResult(completableFutureResp.get());
                jsonrpcResponse.setJsonrpc(jsonrpcRequest.getJsonrpc());
                jsonrpcResponse.setId(jsonrpcRequest.getId());
            } catch (InterruptedException e) {
                responseError = handleError(-32002, "Attempted to retrieve the result of a task/s " +
                        "that was aborted by throwing an exception");
                jsonrpcResponse.setError(responseError);
            } catch (ExecutionException e) {
                responseError = handleError(-32001, "Current thread was interrupted");
                jsonrpcResponse.setError(responseError);
            }
        } else {
            responseError = handleError(-32003, "Response received from the endpoint is null");
            jsonrpcResponse.setError(responseError);
        }
        return jsonrpcResponse;
    }

    /**
     * Handles the JSON RPC Error object.
     *
     * @param code    error code
     * @param message error message
     * @return JSON RPC Error object to be attached to the Response object
     */
    public ResponseError handleError(int code, String message) {
        ResponseError responseError = new ResponseError();
        responseError.setCode(code);
        responseError.setMessage(message);
        return responseError;
    }

    private void sendMessageToAll(String message) {
        sessions.forEach(
                session -> {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        LOGGER.error(e.toString());
                    }
                }
        );
    }
}
