/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.symbol;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Ballerina Symbol Service LS Extension.
 *
 * @since 0.981.2
 */
public class BallerinaSymbolServiceImpl implements BallerinaSymbolService {
    private final WorkspaceManager workspaceManager;
    private final LSClientLogger clientLogger;
    private final LanguageServerContext serverContext;

    public BallerinaSymbolServiceImpl(WorkspaceManager workspaceManager, LanguageServerContext serverContext) {
        this.workspaceManager = workspaceManager;
        this.clientLogger = LSClientLogger.getInstance(serverContext);
        this.serverContext = serverContext;
    }

    @Override
    public CompletableFuture<BallerinaEndpointsResponse> endpoints() {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaEndpointsResponse response = new BallerinaEndpointsResponse();
            response.setEndpoints(getClientEndpoints());
            return response;
        });
    }

    private List<Endpoint> getClientEndpoints() {
        final List<Endpoint> endpoints = new ArrayList<>();
        // TODO: Implementation Required
        return endpoints;
    }

    @Override
    public CompletableFuture<TypeSymbol> type(ExpressionTypeRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String fileUri = request.getDocumentIdentifier().getUri();
            Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
            if (filePath.isEmpty()) {
                return null;
            }

            try {
                LineRange lineRange = LineRange.from(null, request.getStartPosition(), request.getEndPosition());

                // Get the semantic model.
                Optional<SemanticModel> semanticModel = this.workspaceManager.semanticModel(filePath.get());

                if (semanticModel.isPresent()){
                    Optional<TypeSymbol> typeSymbol = semanticModel.get().type(lineRange);
                    if (typeSymbol.isPresent()) {
                        return typeSymbol.get();
                    }
                }
                return null;
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaSymbol/type' failed!";
                this.clientLogger.logError(LSContextOperation.DOC_TYPE, msg, e, request.getDocumentIdentifier(),
                        (Position) null);
                return null;
            }
        });
    }
}
