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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.composer.service.workspace.common.Utils;
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
import org.ballerinalang.composer.service.workspace.langserver.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.langserver.util.WorkspaceSymbolProvider;
import org.ballerinalang.composer.service.workspace.rest.datamodel.InMemoryPackageRepository;
import org.ballerinalang.composer.service.workspace.suggetions.CapturePossibleTokenStrategy;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilter;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.EmptyStackException;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;

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

    private Map<String, TextDocumentItem> openDocumentSessions = new HashMap<>();

    private Map<String, TextDocumentItem> closedDocumentSessions = new HashMap<>();

    private Gson gson;

    private WorkspaceSymbolProvider symbolProvider = new WorkspaceSymbolProvider();

    private Set<Map.Entry<String, ModelPackage>> packages;

    private static final String BAL_EXTENTION = ".bal";

    /**
     * Caching the built in packages.
     */
    private Map<String, ModelPackage> builtInNativePackages;

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
        JsonReader jsonReader;
        try {
            byte[] bytes = json.getBytes(Charset.forName("UTF-8"));
            jsonReader = new JsonReader(new InputStreamReader(
                    new ByteArrayInputStream(bytes), Charset.forName("UTF-8")));
            jsonReader.beginObject();

            while (jsonReader.hasNext()) {
                RequestMessage message = gson.fromJson(json, RequestMessage.class);
                if (LangServerConstants.PING.equals(message.getMethod())) {
                    sendPong();
                } else if (message.getId() != null) {
                    // Request Message Received
                    processRequest(message);
                } else {
                    // Notification message Received
                    processNotification(message);
                }
                break;
            }
            jsonReader.close();
        } catch (IOException e) {
            sendErrorResponse(LangServerConstants.METHOD_NOT_FOUND_LINE, LangServerConstants.METHOD_NOT_FOUND,
                    new RequestMessage(), null);
        }
    }

    /**
     * Process the received Requests
     *
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
                case LangServerConstants.BUILT_IN_PACKAGES:
                    this.getBuiltInPackages(message);
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
     * Send Ping Reply
     */
    private void sendPong() {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setId(LangServerConstants.PONG);
        this.pushMessageToClient(langServerSession, responseMessage);
    }

    /**
     * Process received notifications
     *
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
                case LangServerConstants.PING:
                    this.sendPong();
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
     * Process Invalid Method found
     *
     * @param message Message
     */
    private void invalidMethodFound(Message message) {
        sendErrorResponse(LangServerConstants.METHOD_NOT_FOUND_LINE, LangServerConstants.METHOD_NOT_FOUND,
                message, null);
    }

    /**
     * Send error response to invalid requests
     *
     * @param errorMessage Error Message
     * @param errorCode    Error code
     * @param message      Message
     * @param errorData    ErrorData
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
     *
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
     *
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
     *
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
     *
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
     *
     * @param message Request Message
     */
    private void shutdown(Message message) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(JsonNull.INSTANCE);
        pushMessageToClient(langServerSession, responseMessage);
    }

    /**
     * Handle exit notification
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
     * Get the completion items
     *
     * @param message - Request Message
     */
    private void getCompletionItems(Message message) {
        if (message instanceof RequestMessage) {
            String comilationUnitId = getRandomComilationUnitId();
            ArrayList<CompletionItem> completionItems;
            ArrayList<SymbolInfo> symbols = new ArrayList<>();

            JsonObject params = gson.toJsonTree(((RequestMessage) message).getParams()).getAsJsonObject();
            TextDocumentPositionParams posParams = gson.fromJson(params.toString(), TextDocumentPositionParams.class);

            Position position = posParams.getPosition();
            String textContent = posParams.getText();
            CompilerContext compilerContext = new CompilerContext();

            // TODO: Disabling the LangServer Package Repository and. Enable after adding package support to LangServer
            // HashMap<String, byte[]> contentMap = new HashMap<>();
            // contentMap.put("test.bal", textContent.getBytes(StandardCharsets.UTF_8));

             options = CompilerOptions.getInstance(compilerContext);
             options.put(COMPILER_PHASE, CompilerPhase.TYPE_CHECK.toString());

            // TODO: Disabling the LangServer Package Repository and. Enable after adding package support to LangServer
            // LangServerPackageRepository pkgRepo =
            //        new LangServerPackageRepository(Paths.get(options.get(SOURCE_ROOT)), contentMap);
            SuggestionsFilterDataModel filterDataModel = new SuggestionsFilterDataModel();

            List<Name> names = new ArrayList<>();
            names.add(new org.wso2.ballerinalang.compiler.util.Name("."));
            PackageID tempPackageID = new PackageID(names, new org.wso2.ballerinalang.compiler.util.Name("0.0.0"));
            InMemoryPackageRepository inMemoryPackageRepository = new InMemoryPackageRepository(tempPackageID, "",
                    comilationUnitId, textContent.getBytes(StandardCharsets.UTF_8));
            compilerContext.put(PackageRepository.class, inMemoryPackageRepository);

            CapturePossibleTokenStrategy errStrategy = new CapturePossibleTokenStrategy(compilerContext,
                    position, filterDataModel);
            compilerContext.put(DefaultErrorStrategy.class, errStrategy);

            Compiler compiler = Compiler.getInstance(compilerContext);
            try {
                // here we need to compile the whole package
                compiler.compile(comilationUnitId);
                BLangPackage bLangPackage = (BLangPackage) compiler.getAST();

                // Visit the package to resolve the symbols
                TreeVisitor treeVisitor = new TreeVisitor(comilationUnitId, compilerContext,
                        symbols, position, filterDataModel);
                bLangPackage.accept(treeVisitor);
                // Set the symbol table
                filterDataModel.setSymbolTable(treeVisitor.getSymTable());

                // Filter the suggestions
                SuggestionsFilter suggestionsFilter = new SuggestionsFilter();
                filterDataModel.setPackages(this.getPackages());

                completionItems = suggestionsFilter.getCompletionItems(filterDataModel, symbols);
            } catch (NullPointerException | EmptyStackException e ) {
                //TODO : These exceptions are throwing from core, a proper solution should be followed to handle these
                 logger.debug("Failed to resolve completion items", e.getMessage());
                completionItems = new ArrayList<>();
            }
            // Create the response message for client request
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setId(((RequestMessage) message).getId());
            responseMessage.setResult(completionItems.toArray(new CompletionItem[0]));
            pushMessageToClient(langServerSession, responseMessage);
        } else {
            logger.warn("Invalid Message type found");
        }
    }

    /**
     * Get packages
     *
     * @return a map contains package details
     */
    private Set<Map.Entry<String, ModelPackage>> getPackages() {
        return this.packages;
    }

    /**
     * Set packages
     *
     * @param packages - packages set
     */
    private void setPackages(Set<Map.Entry<String, ModelPackage>> packages) {
        this.packages = packages;
    }

    private static String getRandomComilationUnitId() {
        return UUID.randomUUID().toString().replace("-", "") + BAL_EXTENTION;
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
