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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.ballerinalang.composer.service.workspace.langserver.consts.LangServerConstants;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.dto.DidSaveTextDocumentParams;
import org.ballerinalang.composer.service.workspace.langserver.dto.ErrorData;
import org.ballerinalang.composer.service.workspace.langserver.dto.InitializeResult;
import org.ballerinalang.composer.service.workspace.langserver.dto.Message;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.ballerinalang.composer.service.workspace.langserver.dto.ResponseErrorDTO;
import org.ballerinalang.composer.service.workspace.langserver.dto.ResponseMessage;
import org.ballerinalang.composer.service.workspace.langserver.dto.SymbolInformation;
import org.ballerinalang.composer.service.workspace.langserver.dto.TextDocumentIdentifier;
import org.ballerinalang.composer.service.workspace.langserver.dto.TextDocumentItem;
import org.ballerinalang.composer.service.workspace.langserver.dto.TextDocumentPositionParams;
import org.ballerinalang.composer.service.workspace.langserver.dto.capabilities.ServerCapabilitiesDTO;
import org.ballerinalang.composer.service.workspace.langserver.util.WorkspaceSymbolProvider;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BFile;
import org.ballerinalang.composer.service.workspace.suggetions.AutoCompleteSuggester;
import org.ballerinalang.composer.service.workspace.suggetions.AutoCompleteSuggesterImpl;
import org.ballerinalang.composer.service.workspace.suggetions.CapturePossibleTokenStrategy;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilter;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.BallerinaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Language server Manager which manage langServer requests from the clients.
 */
public class LangServerManager {

    private static final Logger logger = LoggerFactory.getLogger(LangServerManager.class);

    private static LangServerManager langServerManagerInstance;

    private LangServer langserver;

    private LangServerSession langServerSession;

    private boolean initialized;

    private Map<String, TextDocumentItem> openDocumentSessions = new HashMap<>();

    private Map<String, TextDocumentItem> closedDocumentSessions = new HashMap<>();

    private Gson gson;

    private WorkspaceSymbolProvider symbolProvider = new WorkspaceSymbolProvider();

    /**
     * Private constructor
     */
    private LangServerManager() {
        this.initialized = false;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    /**
     * Launch manager singleton
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
        Gson gson = new Gson();
        RequestMessage message = gson.fromJson(json, RequestMessage.class);

        if (message.getId() != null) {
            // Request Message Received
            processRequest(message);
        } else {
            // Notification message Received
            processNotification(message);
        }
    }

    /**
     * Process the received Requests
     * @param message Message
     */
    private void processRequest(RequestMessage message) {
        if (message.getMethod().equals(LangServerConstants.INITIALIZE)) {
            this.initialize(message);
        } else if (this.isInitialized()) {
            switch (message.getMethod()) {
                case LangServerConstants.SHUTDOWN:
                    this.shutdown(message);
                    break;
                case LangServerConstants.WORKSPACE_SYMBOL:
                    this.getWorkspaceSymbol(message);
                    break;
                case LangServerConstants.TEXT_DOCUMENT_COMPLETION:
                    this.getCompletionItems(message);
                    break;
                default:
                    // Valid Method could not be found
                    this.invalidMethodFound(message);
                    break;
            }
        } else {
            // Did not receive the initialize request
            this.sendErrorResponse(LangServerConstants.SERVER_NOT_INITIALIZED_LINE,
                    LangServerConstants.SERVER_NOT_INITIALIZED, message, null);
        }
    }

    /**
     * Process received notifications
     * @param message Message
     */
    private void processNotification(RequestMessage message) {
        if (message.getMethod().equals(LangServerConstants.EXIT)) {
            this.exit(message);
        } else if (this.isInitialized()) {
            switch (message.getMethod()) {
                case LangServerConstants.TEXT_DOCUMENT_DID_OPEN:
                    this.documentDidOpen(message);
                    break;
                case LangServerConstants.TEXT_DOCUMENT_DID_CLOSE:
                    this.documentDidClose(message);
                    break;
                case LangServerConstants.TEXT_DOCUMENT_DID_SAVE:
                    this.documentDidSave(message);
                    break;
                default:
                    // Valid Method could not be found
                    // Only log a warn since this is a notification
                    logger.warn("Invalid Notification Method " + message.getMethod() + " Found");
                    break;
            }
        } else {
            // Drop the notification without responding
            logger.warn("Dropped the notification [" + message.getMethod() + "]");
        }
    }

    /**
     * Push message to client.
     * @param session current session
     * @param response response message
     */
    private void pushMessageToClient(LangServerSession session, ResponseMessage response) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(response);
        session.getChannel().write(new TextWebSocketFrame(json));
        session.getChannel().flush();
    }

    /**
     * Process Invalid Method found
     * @param message Message
     */
    private void invalidMethodFound(Message message) {
        sendErrorResponse(LangServerConstants.METHOD_NOT_FOUND_LINE, LangServerConstants.METHOD_NOT_FOUND,
                message, null);
    }

    /**
     * Send error response to invalid requests
     * @param errorMessage Error Message
     * @param errorCode Error code
     * @param message Message
     * @param errorData ErrorData
     */
    private void sendErrorResponse(String errorMessage, int errorCode, Message message, ErrorData errorData) {
        ResponseMessage responseMessageDTO = new ResponseMessage();
        ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO();
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


    // Start Request Handlers
    /**
     * Process initialize request
     * @param message Request Message
     */
    private void initialize(Message message) {
        this.setInitialized(true);

        ResponseMessage responseMessage = new ResponseMessage();
        InitializeResult initializeResult = new InitializeResult();
        if (message instanceof RequestMessage) {
            responseMessage.setId(((RequestMessage) message).getId());
        }
        ServerCapabilitiesDTO serverCapabilities = new ServerCapabilitiesDTO();
        initializeResult.setCapabilities(serverCapabilities);
        responseMessage.setResult(initializeResult);
        pushMessageToClient(langServerSession, responseMessage);
    }
    // End Request Handlers


    // Start Notification handlers
    /**
     * Handle Document did open notification
     * @param message Request Message
     */
    private void documentDidOpen(Message message) {
        if (message instanceof RequestMessage) {
            try {
                LinkedTreeMap textDocument = (LinkedTreeMap) ((LinkedTreeMap) ((RequestMessage) message).
                        getParams()).get("textDocument");
                JsonObject jsonObject = gson.toJsonTree(textDocument).getAsJsonObject();
                TextDocumentItem textDocumentItem = gson.fromJson(jsonObject, TextDocumentItem.class);
                this.getOpenDocumentSessions().put(textDocumentItem.getDocumentUri(), textDocumentItem);
            } catch (Exception e) {
                logger.error("Invalid document received [" + e.getMessage() + "]");
            }
        } else {
            // Invalid message type found
            logger.warn("Invalid Message type found");
        }
    }

    /**
     * Handle Document did close notification
     * @param message Request Message
     */
    private void documentDidClose(Message message) {
        if (message instanceof RequestMessage) {
            try {
                LinkedTreeMap textDocument = (LinkedTreeMap) ((LinkedTreeMap) ((RequestMessage) message).
                        getParams()).get("textDocument");
                JsonObject jsonObject = gson.toJsonTree(textDocument).getAsJsonObject();
                TextDocumentIdentifier textDocumentIdentifier = gson.fromJson(jsonObject,
                        TextDocumentIdentifier.class);

                if (this.getOpenDocumentSessions().containsKey(textDocumentIdentifier.getDocumentUri())) {
                    this.getClosedDocumentSessions().put(textDocumentIdentifier.getDocumentUri(),
                            this.getOpenDocumentSessions().get(textDocumentIdentifier.getDocumentUri()));
                    this.getOpenDocumentSessions().remove(textDocumentIdentifier.getDocumentUri());
                } else {
                    // Could not find the particular document identifier in the open document sessions
                    logger.error("Invalid document Identifier");
                }
            } catch (Exception e) {
                logger.error("Invalid document received [" + e.getMessage() + "]");
            }
        } else {
            // Invalid message type found
            logger.warn("Invalid Message type found");
        }
    }

    private void documentDidSave(Message message) {
        if (message instanceof RequestMessage) {
            JsonObject params = gson.toJsonTree(((RequestMessage) message).getParams()).getAsJsonObject();
            DidSaveTextDocumentParams didSaveTextDocumentParams = gson.fromJson(params.toString(),
                    DidSaveTextDocumentParams.class);
            TextDocumentIdentifier textDocumentIdentifier = didSaveTextDocumentParams.getTextDocument();

            /**
             * If the text document have not been persisted then this is the first time we try to
             * persist the document. In that case we need to remove the previous temp entry
             */
            TextDocumentItem textDocumentItem;
            if (this.getOpenDocumentSessions().containsKey("/temp/" + textDocumentIdentifier.getDocumentId())) {
                textDocumentItem = this.getOpenDocumentSessions()
                        .get("/temp/" + textDocumentIdentifier.getDocumentId());
                this.getOpenDocumentSessions()
                        .remove("/temp/" + textDocumentIdentifier.getDocumentId());
                textDocumentItem.setText(didSaveTextDocumentParams.getText());
                this.getOpenDocumentSessions().put(textDocumentIdentifier.getDocumentUri(), textDocumentItem);
            } else if (this.getOpenDocumentSessions().containsKey(textDocumentIdentifier.getDocumentUri())) {
                textDocumentItem = this.getOpenDocumentSessions()
                        .get(textDocumentIdentifier.getDocumentUri());
                textDocumentItem.setText(didSaveTextDocumentParams.getText());
            } else {
                logger.warn("Invalid document uri");
            }
        } else {
            // Invalid message type found
            logger.warn("Invalid Message type found");
        }
    }

    /**
     * Handle the get workspace symbol requests
     * @param message Request Message
     */
    private void getWorkspaceSymbol(Message message) {
        if (message instanceof RequestMessage) {
            String query = (String) ((LinkedTreeMap) ((RequestMessage) message).getParams()).get("query");
            SymbolInformation[] symbolInformations = symbolProvider.getSymbols(query);
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setId(((RequestMessage) message).getId());
            responseMessage.setResult(symbolInformations);
            pushMessageToClient(langServerSession, responseMessage);
        } else {
            logger.warn("Invalid Message type found");
        }
    }

    /**
     * Process Shutdown notification
     * @param message Request Message
     */
    private void shutdown(Message message) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(JsonNull.INSTANCE);
        pushMessageToClient(langServerSession, responseMessage);
    }

    /**
     * Handle exit notification
     * @param message Request Message
     */
    private void exit(Message message) {
        //Exit the process
    }

    /**
     * Get the completion items
     * @param message - Request Message
     */
    private void getCompletionItems(Message message) {
        if (message instanceof RequestMessage) {
            JsonObject params = gson.toJsonTree(((RequestMessage) message).getParams()).getAsJsonObject();
            TextDocumentPositionParams textDocumentPositionParams = gson.fromJson(params.toString(),
                    TextDocumentPositionParams.class);
            String textContent = textDocumentPositionParams.getText();
            Position position = textDocumentPositionParams.getPosition();
            ArrayList<CompletionItem> completionItems = new ArrayList<>();

            BFile bFile = new BFile();
            bFile.setContent(textContent);
            bFile.setFilePath("/temp");
            bFile.setFileName("temp.bal");
            bFile.setPackageName(".");

            AutoCompleteSuggester autoCompleteSuggester = new AutoCompleteSuggesterImpl();
            CapturePossibleTokenStrategy capturePossibleTokenStrategy = new CapturePossibleTokenStrategy(position);
            try {
                BallerinaFile ballerinaFile =
                        autoCompleteSuggester.getBallerinaFile(bFile, position, capturePossibleTokenStrategy);
                capturePossibleTokenStrategy.getSuggestionsFilterDataModel().setBallerinaFile(ballerinaFile);
                SuggestionsFilterDataModel dm = capturePossibleTokenStrategy.getSuggestionsFilterDataModel();
                ArrayList symbols = new ArrayList<>();

                CompletionItemAccumulator jsonModelBuilder = new CompletionItemAccumulator(symbols, position);
                dm.getBallerinaFile().accept(jsonModelBuilder);

//                for (Object symbol : symbols) {
//                    if (symbol instanceof SymbolName) {
//                        CompletionItem completionItem = new CompletionItem();
//                        completionItem.setLabel(((SymbolName) symbol).getName());
//                        completionItems.add(completionItem);
//                    }
//                }
                SuggestionsFilter suggestionsFilter = new SuggestionsFilter();
                completionItems = suggestionsFilter.getCompletionItems(dm, symbols);
//                for (Object symbol : cis) {
//                    if (symbol instanceof SymbolName) {
//                        CompletionItem completionItem = new CompletionItem();
//                        completionItem.setLabel(((SymbolName) symbol).getName());
//                        completionItems.add(completionItem);
//                    }
//                }
            } catch (IOException e) {
                this.sendErrorResponse(LangServerConstants.INTERNAL_ERROR_LINE,
                        LangServerConstants.INTERNAL_ERROR, message, null);
            }

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setId(((RequestMessage) message).getId());
            responseMessage.setResult(completionItems.toArray(new CompletionItem[0]));
            pushMessageToClient(langServerSession, responseMessage);
        } else {
            logger.warn("Invalid Message type found");
        }
    }

    // End Notification Handlers

    private boolean isInitialized() {
        return initialized;
    }

    private void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public Map<String, TextDocumentItem> getOpenDocumentSessions() {
        return this.openDocumentSessions;
    }

    public Map<String, TextDocumentItem> getClosedDocumentSessions() {
        return closedDocumentSessions;
    }
}
