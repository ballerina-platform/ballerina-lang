/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.ballerina.plugins.idea.extensions.client;

import com.intellij.openapi.diagnostic.Logger;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTDidChange;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTDidChangeResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTRequest;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaDocumentService;
import io.ballerina.plugins.idea.extensions.server.BallerinaEndpointsResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaExtendedLangServer;
import io.ballerina.plugins.idea.extensions.server.BallerinaProjectService;
import io.ballerina.plugins.idea.extensions.server.BallerinaSymbolService;
import io.ballerina.plugins.idea.extensions.server.ModulesRequest;
import io.ballerina.plugins.idea.extensions.server.ModulesResponse;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.DefaultRequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;

import java.util.concurrent.CompletableFuture;

/**
 * Request manager extension for Ballerina language.
 */
public class BallerinaRequestManager extends DefaultRequestManager {

    private static final Logger LOG = Logger.getInstance(BallerinaRequestManager.class);

    private BallerinaDocumentService ballerinaDocumentService;
    private BallerinaSymbolService ballerinaSymbolService;
    private BallerinaProjectService ballerinaProjectService;

    public BallerinaRequestManager(LanguageServerWrapper wrapper, LanguageServer server, LanguageClient client,
                                   ServerCapabilities serverCapabilities) {
        super(wrapper, server, client, serverCapabilities);
        BallerinaExtendedLangServer extendedServer = (BallerinaExtendedLangServer) server;
        ballerinaDocumentService = extendedServer.getBallerinaDocumentService();
        ballerinaSymbolService = extendedServer.getBallerinaSymbolService();
        ballerinaProjectService = extendedServer.getBallerinaProjectService();

    }

    public CompletableFuture<BallerinaASTResponse> ast(BallerinaASTRequest request) {
        if (checkStatus()) {
            try {
                return ballerinaDocumentService.ast(request);
            } catch (Exception e) {
                LOG.warn("Unexpected error occurred in ballerina language client request manager", e);
                return null;
            }
        } else {
            LOG.warn("Language server is not initialized.");
            return null;
        }
    }

    public CompletableFuture<ModulesResponse> modules(ModulesRequest request) {
        if (checkStatus()) {
            try {
                return ballerinaProjectService.modules(request);
            } catch (Exception e) {
                LOG.warn("Unexpected error occurred in ballerina language client request manager", e);
                return null;
            }
        } else {
            LOG.warn("Language server is not initialized.");
            return null;
        }
    }

    public CompletableFuture<BallerinaASTDidChangeResponse> astDidChange(BallerinaASTDidChange notification) {
        if (checkStatus()) {
            try {
                return ballerinaDocumentService.astDidChange(notification);
            } catch (Exception e) {
                LOG.warn("Unexpected error occurred in ballerina language client request manager", e);
                return null;
            }
        } else {
            LOG.warn("Language server is not initialized.");
            return null;
        }
    }

    public CompletableFuture<BallerinaEndpointsResponse> endpoints() {
        if (checkStatus()) {
            try {
                return ballerinaSymbolService.endpoints();
            } catch (Exception e) {
                LOG.warn("Unexpected error occurred in ballerina language client request manager", e);
                return null;
            }
        } else {
            LOG.warn("Language server is not initialized.");
            return null;
        }
    }
}
