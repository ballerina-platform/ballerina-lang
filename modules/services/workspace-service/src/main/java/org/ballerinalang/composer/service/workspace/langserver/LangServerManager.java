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
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.composer.service.workspace.Constants;
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
import org.ballerinalang.composer.service.workspace.langserver.util.WorkspaceSymbolProvider;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BFile;
import org.ballerinalang.composer.service.workspace.suggetions.AutoCompleteSuggester;
import org.ballerinalang.composer.service.workspace.suggetions.AutoCompleteSuggesterImpl;
import org.ballerinalang.composer.service.workspace.suggetions.CapturePossibleTokenStrategy;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilter;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.composer.service.workspace.util.WorkspaceUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangPrograms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private Set<Map.Entry<String, ModelPackage>> packages;
    private Map<Path, Map<String, ModelPackage>> programPackagesMap;
    private Map<Path, BLangProgram> programMap;

    /**
     * Private constructor
     */
    private LangServerManager() {
        this.initialized = false;
        this.gson = new GsonBuilder().serializeNulls().create();
        programPackagesMap = new HashMap<>();
        programMap = new HashMap<>();
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
                case LangServerConstants.PROGRAM_DIRECTORY_PACKAGES:
                    this.getProgramPackages(message);
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
     * Get all the packages in the program directory. If the given file is not saved in the file-system, this will
     * return only the native/built-in packages. And also calling this method will update the "programPackagesMap"
     * which is used to keep program packages against a file path
     *
     * @param message Request Message
     */
    private void getProgramPackages(Message message) {
        if (message instanceof RequestMessage) {
            JsonObject response = new JsonObject();
            Map<String, ModelPackage> packages;
            JsonObject params = gson.toJsonTree(((RequestMessage) message).getParams()).getAsJsonObject();
            TextDocumentPositionParams textDocumentPositionParams = gson.fromJson(params.toString(),
                    TextDocumentPositionParams.class);
            String fileName = textDocumentPositionParams.getFileName();
            String filePath = textDocumentPositionParams.getFilePath();
            String packageName = textDocumentPositionParams.getPackageName();

            if ("temp".equals(filePath)) {
                // Load all the packages associated the runtime
                packages = Utils.getAllPackages();

            } else {
                Path file = Paths.get(filePath + File.separator + fileName);
                packages = resolveProgramPackages(Paths.get(filePath), packageName);
                programPackagesMap.put(file, packages);
            }
            LangServerManager.this.setPackages(packages.entrySet());

            // add package info into response
            Gson gson = new Gson();
            String json = gson.toJson(packages.values());
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
            JsonObject params = gson.toJsonTree(((RequestMessage) message).getParams()).getAsJsonObject();
            TextDocumentPositionParams textDocumentPositionParams = gson.fromJson(params.toString(),
                    TextDocumentPositionParams.class);
            String textContent = textDocumentPositionParams.getText();
            Position position = textDocumentPositionParams.getPosition();
            String filePath = textDocumentPositionParams.getFilePath();
            String packageName = textDocumentPositionParams.getPackageName();
            ArrayList<CompletionItem> completionItems = new ArrayList<>();

            BFile bFile = new BFile();
            bFile.setContent(textContent);
            bFile.setFilePath("/temp");
            bFile.setFileName("temp.bal");
            bFile.setPackageName(".");

            AutoCompleteSuggester autoCompleteSuggester = new AutoCompleteSuggesterImpl();
            CapturePossibleTokenStrategy capturePossibleTokenStrategy = new CapturePossibleTokenStrategy(position);
            try {
                SuggestionsFilterDataModel dm = capturePossibleTokenStrategy.getSuggestionsFilterDataModel();
                ArrayList symbols = new ArrayList<>();

                CompletionItemAccumulator completionItemAccumulator = new CompletionItemAccumulator(symbols, position);

                if ("temp".equals(filePath)) {
                    BallerinaFile ballerinaFile =
                            autoCompleteSuggester.getBallerinaFile(bFile, position, capturePossibleTokenStrategy);
                    capturePossibleTokenStrategy.getSuggestionsFilterDataModel().setBallerinaFile(ballerinaFile);
                    ballerinaFile.accept(completionItemAccumulator);
                } else {
                    Path file = Paths.get(filePath);
                    BLangProgram bLangProgram;
                    if (programMap.containsKey(file)) {
                        bLangProgram = programMap.get(file);
                    } else {
                        bLangProgram = getBLangProgram(Paths.get(filePath), packageName);
                        programMap.put(file, bLangProgram);
                    }
                    bLangProgram.accept(completionItemAccumulator);
                }

                SuggestionsFilter suggestionsFilter = new SuggestionsFilter();
                dm.setClosestScope(completionItemAccumulator.getClosestScope());
                // set all the packages associated with the runtime. "this.getPackages()" might return null as process
                // of loading packages is running in a separate thread. See initBackgroundJobs() method.
                dm.setPackages(this.getPackages());

                completionItems = suggestionsFilter.getCompletionItems(dm, symbols);
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

    /**
     * Get packages
     * @return a map contains package details
     */
    private Set<Map.Entry<String, ModelPackage>> getPackages() {
        return this.packages;
    }

    /**
     * Get BLangProgram
     * @param filePath    - file path to parent directory of the .bal file
     * @param packageName - package name
     * @return
     */
    private BLangProgram getBLangProgram(java.nio.file.Path filePath, String packageName) {
        java.nio.file.Path programDirPath = WorkspaceUtils.gerProgramDirectory(filePath, packageName);
        int compare = filePath.compareTo(programDirPath);
        String sourcePath = (String) filePath.toString().subSequence(filePath.toString().length() - compare + 1,
                filePath.toString().length());
        BLangProgram bLangProgram = new BLangProgramLoader().loadMain(programDirPath, Paths.get(sourcePath));

        return bLangProgram;
    }

    /**
     * Set packages
     * @param packages - packages set
     */
    private void setPackages(Set<Map.Entry<String, ModelPackage>> packages) {
        this.packages = packages;
    }

    /**
     * Generate a json with packages in program directory
     *
     * @param filePath    - file path to parent directory of the .bal file
     * @param packageName - package name
     */
    private Map<String, ModelPackage> resolveProgramPackages(java.nio.file.Path filePath, String packageName) {
        // Filter out Default package scenario
        if (!".".equals(packageName)) {
            // find nested directory count using package name
            int directoryCount = (packageName.contains(".")) ? packageName.split("\\.").length
                    : 1;

            // find program directory
            java.nio.file.Path parentDir = filePath;
            for (int i = 0; i < directoryCount; ++i) {
                if (parentDir != null) {
                    parentDir = parentDir.getParent();
                }
            }

            // we shouldn't proceed if the parent directory is null
            if (parentDir == null) {
                return null;
            }

            // get packages in program directory
            return getPackagesInProgramDirectory(parentDir);
        }
        return null;
    }


    /**
     * Get packages in program directory
     *
     * @param programDirPath
     * @return a map contains package details
     * @throws BallerinaException
     */
    private Map<String, ModelPackage> getPackagesInProgramDirectory(java.nio.file.Path programDirPath) {
        Map<String, ModelPackage> modelPackageMap = new HashMap();

        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);
        List<java.nio.file.Path> filePaths = new ArrayList<>();
        searchFilePathsForBalFiles(programDirPath, filePaths, Constants.DIRECTORY_DEPTH);

        // add resolved packages into map
        for (java.nio.file.Path filePath : filePaths) {
            int compare = filePath.compareTo(programDirPath);
            String sourcePath = (String) filePath.toString().subSequence(filePath.toString().length() - compare + 1,
                    filePath.toString().length());
            try {
                BLangProgram bLangProgram = new BLangProgramLoader()
                        .loadMain(programDirPath, Paths.get(sourcePath));

                //
                java.nio.file.Path path = programDirPath.resolve(sourcePath);
                programMap.put(path, bLangProgram);

                String[] packageNames = {bLangProgram.getMainPackage().getName()};
                modelPackageMap.putAll(WorkspaceUtils.getResolvedPackagesMap(bLangProgram, packageNames));
            } catch (BallerinaException e) {
                logger.warn(e.getMessage());
                // TODO : we shouldn't catch runtime exceptions. Need to validate properly before executing

                // There might be situations where program directory contains unresolvable/un-parsable .bal files. In
                // those scenarios we still needs to proceed even without package resolving for that particular package.
                // Hence ignoring the exception.
            }
        }
        return modelPackageMap;
    }

    /**
     * Recursive method to search for .bal files and add their parent directory paths to the provided List
     *
     * @param programDirPath - program directory path
     * @param filePaths      - file path list
     * @param depth          - depth of the directory hierarchy which we should search from the program directory
     */
    private void searchFilePathsForBalFiles(java.nio.file.Path programDirPath,
                                            List<java.nio.file.Path> filePaths, int depth) {
        // this method is a recursive method. depth is the iteration count and we should return based on the depth count
        if (depth < 0) {
            return;
        }
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(programDirPath);
            depth = depth - 1;
            for (java.nio.file.Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    searchFilePathsForBalFiles(entry, filePaths, depth);
                }
                java.nio.file.Path file = entry.getFileName();
                if (file != null) {
                    String fileName = file.toString();
                    if (fileName.endsWith(".bal")) {
                        filePaths.add(entry.getParent());
                    }
                }
            }
            stream.close();
        } catch (IOException e) {
            // we are ignoring any exception and proceed.
            return;
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
