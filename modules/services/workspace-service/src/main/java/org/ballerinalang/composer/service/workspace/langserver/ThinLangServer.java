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
import com.google.gson.GsonBuilder;
import org.ballerinalang.composer.service.workspace.langserver.ws.WSMessageConsumer;
import org.ballerinalang.composer.service.workspace.langserver.ws.WSMessageProducer;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.eclipse.lsp4j.jsonrpc.*;
import org.eclipse.lsp4j.jsonrpc.json.*;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
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
    List<TextMessageListener> textMessageListeners = new ArrayList<>();
    List<SocketCloseListener> socketCloseListeners = new ArrayList<>();
    Launcher<LanguageClient> launcher;

    public ThinLangServer() throws IOException {
        inputStream.connect(pipedOutputStream);
    }

    @OnOpen
    public void onOpen (Session session) {
        sessions.add(session);
        if (launcher != null) {
            return;
        }
        Launcher<LanguageClient> launcher = this.launchRPCServer(server, LanguageClient.class);
        LanguageClient client = launcher.getRemoteProxy();
        ((LanguageClientAware) server).connect(client);
        Future<?> startListening = launcher.startListening();
        try {
            startListening.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onTextMessage(String request, Session session){
        textMessageListeners.stream().forEachOrdered(new Consumer<TextMessageListener>() {
            @Override
            public void accept(TextMessageListener textMessageListener) {
                textMessageListener.onTextMessage(request, session);
            }
        });
    }

    @OnMessage
    public void onBinaryMessage(byte[] bytes, Session session) {
        LOGGER.info("Reading binary Message");
        LOGGER.info(bytes.toString());
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        socketCloseListeners.stream().forEachOrdered(new Consumer<SocketCloseListener>() {
            @Override
            public void accept(SocketCloseListener socketCloseListener) {
                socketCloseListener.onSocketClose(closeReason, session);
            }
        });
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        LOGGER.error("Error found in method : " + throwable.toString());
    }

    protected <T> Launcher<T> launchRPCServer(Object localService, Class<T> remoteInterface) {

        Consumer<GsonBuilder> configureGson = (gsonBuilder) -> {
        };
        Map<String, JsonRpcMethod> supportedMethods = new LinkedHashMap();
        supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(remoteInterface));
        if (localService instanceof JsonRpcMethodProvider) {
            JsonRpcMethodProvider rpcMethodProvider = (JsonRpcMethodProvider)localService;
            supportedMethods.putAll(rpcMethodProvider.supportedMethods());
        } else {
            supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(localService.getClass()));
        }

        MessageJsonHandler jsonHandler = new MessageJsonHandler(supportedMethods, configureGson);
        MessageConsumer outGoingMessageStream = new WSMessageConsumer(this, jsonHandler);
        RemoteEndpoint serverEndpoint = new RemoteEndpoint(outGoingMessageStream, ServiceEndpoints.toEndpoint(localService));
        jsonHandler.setMethodProvider(serverEndpoint);
        final MessageConsumer messageConsumer = serverEndpoint;
        final MessageProducer reader = new WSMessageProducer(this, jsonHandler);
        final T remoteProxy = ServiceEndpoints.toServiceObject(serverEndpoint, remoteInterface);
        return new Launcher<T>() {
            public Future<?> startListening() {
                return ConcurrentMessageProcessor.startProcessing(reader, messageConsumer,
                        Executors.newCachedThreadPool());
            }

            public T getRemoteProxy() {
                return remoteProxy;
            }
        };
    }

    public void sendMessageToAll(String message) {
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

    public void addTextMessageListener(TextMessageListener listener) {
        this.textMessageListeners.add(listener);
    }

    public void addSocketCloseListner(SocketCloseListener listener) {
        this.socketCloseListeners.add(listener);
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public interface TextMessageListener {
        void onTextMessage(String message, Session session);
    }

    public interface SocketCloseListener {
        void onSocketClose(CloseReason closeReason, Session session);
    }
}
