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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.ballerinalang.composer.service.workspace.common.Utils;
import org.ballerinalang.composer.service.workspace.composerapi.ComposerApiImpl;
import org.ballerinalang.composer.service.workspace.composerapi.utils.RequestHandler;
import org.ballerinalang.composer.service.workspace.langserver.consts.LangServerConstants;
import org.ballerinalang.composer.service.workspace.langserver.dto.ErrorData;
import org.ballerinalang.composer.service.workspace.langserver.model.ModelPackage;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.Map;
import java.util.Set;

/**
 * Language server Manager which manage langServer requests from the clients.
 */
public class LangServerManager {

    private static final Logger logger = LoggerFactory.getLogger(LangServerManager.class);

    private static LangServerManager langServerManagerInstance;

    private CompilerOptions options;

    private LangServer langserver;

    private LangServerSession langServerSession;

    private boolean initialized;

    private Gson gson;

    private Set<Map.Entry<String, ModelPackage>> packages;

    private static final String BAL_EXTENTION = ".bal";

    private Endpoint languageServerServiceEndpoint;

    private RequestHandler requestHandler;

    /**
     * Caching the built in packages.
     */
    private Map<String, ModelPackage> builtInNativePackages;

    /**
     * Private constructor.
     */
    private LangServerManager() {
        this.initialized = false;
        this.gson = new GsonBuilder().serializeNulls().create();
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
        String response = requestHandler.routeRequestAndNotify(languageServerServiceEndpoint, json);
        langServerSession.getChannel().write(new TextWebSocketFrame(response));
        langServerSession.getChannel().flush();
    }

    /**
     * Send Ping Reply.
     */
    private void sendPong() {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setId(LangServerConstants.PONG);
        this.pushMessageToClient(langServerSession, responseMessage);
    }

    /**
     * Push message to client.
     *
     * @param session  current session
     * @param response response message
     */
    private void pushMessageToClient(LangServerSession session, ResponseMessage response) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(response);
        session.getChannel().write(new TextWebSocketFrame(json));
        session.getChannel().flush();
    }

    /**
     * Process Invalid Method found.
     *
     * @param message Message
     */
    private void invalidMethodFound(Message message) {
        sendErrorResponse(LangServerConstants.METHOD_NOT_FOUND_LINE, LangServerConstants.METHOD_NOT_FOUND,
                message, null);
    }

    /**
     * Send error response to invalid requests.
     *
     * @param errorMessage Error Message
     * @param errorCode    Error code
     * @param message      Message
     * @param errorData    ErrorData
     */
    private void sendErrorResponse(String errorMessage, int errorCode, Message message, ErrorData errorData) {
        ResponseMessage responseMessageDTO = new ResponseMessage();
        ResponseError responseErrorDTO = new ResponseError();
        if (message instanceof RequestMessage) {
            responseMessageDTO.setId(((RequestMessage) message).getId());
        }
        responseErrorDTO.setMessage(errorMessage);
        responseErrorDTO.setCode(errorCode);
        responseMessageDTO.setError(responseErrorDTO);

        // If Error Data is available set it
        if (errorData != null) {
            responseErrorDTO.setData(errorData);
        }
        pushMessageToClient(langServerSession, responseMessageDTO);
    }

    // Start Notification handlers

    /**
     * Handle exit notification.
     *
     * @param message Request Message
     */
    private void exit(Message message) {
        //Exit the process
    }

    /**
     * Get all the built-in packages.
     *
     * @param message Request Message
     */
    private void getBuiltInPackages(Message message) {
        if (message instanceof RequestMessage) {
            JsonObject response = new JsonObject();
            // Load all the packages associated the runtime
            if (builtInNativePackages == null) {
                builtInNativePackages = Utils.getAllPackages();
            }
            LangServerManager.this.setPackages(builtInNativePackages.entrySet());

            // add package info into response
            Gson gson = new Gson();
            String json = gson.toJson(builtInNativePackages.values());
            JsonParser parser = new JsonParser();
            JsonArray packagesArray = parser.parse(json).getAsJsonArray();
            response.add("packages", packagesArray);

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setId(((RequestMessage) message).getId());
            responseMessage.setResult(response);
            pushMessageToClient(langServerSession, responseMessage);

        } else {
            logger.warn("Invalid Message type found");
        }
    }

    /**
     * Set packages.
     *
     * @param packages - packages set
     */
    private void setPackages(Set<Map.Entry<String, ModelPackage>> packages) {
        this.packages = packages;
    }

    // End Notification Handlers

    private boolean isInitialized() {
        return initialized;
    }

    private void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
