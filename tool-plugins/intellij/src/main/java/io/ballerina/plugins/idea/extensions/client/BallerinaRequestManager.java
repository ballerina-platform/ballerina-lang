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

import io.ballerina.plugins.idea.extensions.server.BallerinaASTRequest;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaDocumentService;
import io.ballerina.plugins.idea.extensions.server.BallerinaExtendedLangServer;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.DefaultRequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;

import java.util.concurrent.CompletableFuture;

public class BallerinaRequestManager extends DefaultRequestManager {

    BallerinaDocumentService ballerinaDocumentService;

    public BallerinaRequestManager(LanguageServerWrapper wrapper, LanguageServer server, LanguageClient client,
            ServerCapabilities serverCapabilities) {
        super(wrapper, server, client, serverCapabilities);
        BallerinaExtendedLangServer extendedServer = (BallerinaExtendedLangServer) server;
        ballerinaDocumentService = extendedServer.getBallerinaDocumentService();
    }

    public CompletableFuture<BallerinaASTResponse> ast(BallerinaASTRequest request) {
        if (checkStatus()) {
            try {
                return ballerinaDocumentService.ast(request);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }

    }
}
