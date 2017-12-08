/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.langserver;

import com.google.gson.JsonParser;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.ballerinalang.composer.service.workspace.composerapi.ComposerApiImpl;
import org.ballerinalang.composer.service.workspace.composerapi.utils.RequestHandler;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

/**
 * Language server Manager which manage langServer requests from the clients.
 */
public class LangServerManager {

    private static LangServerManager langServerManagerInstance;

    private LangServer langserver;

    private LangServerSession langServerSession;

    private Endpoint languageServerServiceEndpoint;

    private RequestHandler requestHandler;

    private static final String ID_KEY = "id";

    /**
     * Private constructor.
     */
    private LangServerManager() {
        ComposerApiImpl composerApiService = new ComposerApiImpl();
        this.languageServerServiceEndpoint = ServiceEndpoints.toEndpoint(composerApiService);
        this.requestHandler = new RequestHandler();
    }

    /**
     * Launch manager singleton.
     *
     * @return LangServerManager instance
     */
    public static LangServerManager getInstance() {
        synchronized (LangServerManager.class) {
            if (langServerManagerInstance == null) {
                langServerManagerInstance = new LangServerManager();
            }
        }
        return langServerManagerInstance;
    }

    public void init(int port) {
        // start the language server if it is not started yet.
        if (this.langserver == null) {
            this.langserver = new LangServer(port);
            this.langserver.startServer();
        }
    }

    void addLaunchSession(Channel channel) {
        this.langServerSession = new LangServerSession(channel);
    }

    void processFrame(String json) {
        JsonParser parser = new JsonParser();
        boolean isNotification = !parser.parse(json).getAsJsonObject().has(ID_KEY);
        String response = requestHandler.routeRequestAndNotify(languageServerServiceEndpoint, json);
        if (!isNotification) {
            langServerSession.getChannel().write(new TextWebSocketFrame(response));
            langServerSession.getChannel().flush();
        }
    }
}
