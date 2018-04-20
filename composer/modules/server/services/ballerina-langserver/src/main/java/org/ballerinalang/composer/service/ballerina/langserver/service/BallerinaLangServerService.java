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
package org.ballerinalang.composer.service.ballerina.langserver.service;

import com.google.gson.GsonBuilder;
import org.ballerinalang.composer.server.core.ServerConstants;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ServiceInfo;
import org.ballerinalang.composer.server.spi.ServiceType;
import org.ballerinalang.composer.service.ballerina.langserver.Constants;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.compiler.workspace.ExtendedWorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageProducer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.ConcurrentMessageProcessor;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethodProvider;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Lang Server exposed via WS.
 */
@ServerEndpoint(ServerConstants.CONTEXT_ROOT + "/" + Constants.SERVICE_PATH)
public class BallerinaLangServerService implements ComposerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaLangServerService.class);

    private final List<Session> sessions = new LinkedList<Session>();
    private final List<TextMessageListener> textMessageListeners = new ArrayList<>();
    private final List<SocketCloseListener> socketCloseListeners = new ArrayList<>();

    private final WorkspaceDocumentManager documentManager = ExtendedWorkspaceDocumentManagerImpl.getInstance();
    private final BallerinaLanguageServer server = new BallerinaLanguageServer(documentManager);
    private Launcher<LanguageClient> launcher;

    @OnOpen
    public void onOpen (Session session) {
        sessions.add(session);
        if (launcher != null) {
            return;
        }
        this.launcher = this.launchRPCServer(server, LanguageClient.class);
        LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);
        Future<?> startListening = launcher.startListening();
        try {
            startListening.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error starting language server", e);
        }
    }

    @OnMessage
    public void onTextMessage(String request, Session session) {
        textMessageListeners
                .forEach(textMessageListener -> textMessageListener.onTextMessage(request, session));
    }

    @OnMessage
    public void onBinaryMessage(byte[] bytes, Session session) {
        LOGGER.info("Reading binary Message");
        LOGGER.info(bytes.toString());
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        socketCloseListeners
                .forEach(socketCloseListener -> socketCloseListener.onSocketClose(closeReason, session));
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        LOGGER.error("Error found in method : " + throwable.toString());
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

    public void addSocketCloseListener(SocketCloseListener listener) {
        this.socketCloseListeners.add(listener);
    }

    public List<Session> getSessions() {
        return sessions;
    }

    private <T> Launcher<T> launchRPCServer(Object localService, Class<T> remoteInterface) {

        Consumer<GsonBuilder> configureGson = (gsonBuilder) -> {
        };
        Map<String, JsonRpcMethod> supportedMethods = new LinkedHashMap();
        supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(remoteInterface));
        if (localService instanceof JsonRpcMethodProvider) {
            JsonRpcMethodProvider rpcMethodProvider = (JsonRpcMethodProvider) localService;
            supportedMethods.putAll(rpcMethodProvider.supportedMethods());
        } else {
            supportedMethods.putAll(ServiceEndpoints.getSupportedMethods(localService.getClass()));
        }

        MessageJsonHandler jsonHandler = new MessageJsonHandler(supportedMethods, configureGson);
        MessageConsumer outGoingMessageStream = new WSRPCMessageConsumer(this, jsonHandler);
        RemoteEndpoint serverEndpoint = new RemoteEndpoint(outGoingMessageStream,
                ServiceEndpoints.toEndpoint(localService));
        jsonHandler.setMethodProvider(serverEndpoint);
        final MessageConsumer messageConsumer = serverEndpoint;
        final MessageProducer reader = new WSRPCMessageProducer(this, jsonHandler);
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

    @Override
    public ServiceInfo getServiceInfo() {
        return new ServiceInfo(Constants.SERVICE_NAME, Constants.SERVICE_PATH, ServiceType.WS);
    }

    /**
     * Message Listener interface.
     */
    public interface TextMessageListener {
        void onTextMessage(String message, Session session);
    }

    /**
     * Socket close listener interface.
     */
    public interface SocketCloseListener {
        void onSocketClose(CloseReason closeReason, Session session);
    }

}
