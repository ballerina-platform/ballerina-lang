/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.contexts;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Code action resolve context implementation.
 *
 * @since 2201.2.1
 */
public class CodeActionResolveContextImpl extends AbstractDocumentServiceContext implements CodeActionResolveContext {

    private final BallerinaLanguageServer languageServer;

    private final ExtendedLanguageClient languageClient;

    CodeActionResolveContextImpl(LSOperation operation,
                                 String fileUri,
                                 WorkspaceManager wsManager,
                                 LanguageServerContext serverContext,
                                 CancelChecker cancelChecker,
                                 BallerinaLanguageServer languageServer) {
        super(operation, fileUri, wsManager, serverContext, cancelChecker);
        this.languageServer = languageServer;
        this.languageClient = this.languageServer.getClient();
    }

    @Override
    public LanguageServer getLanguageServer() {
        return this.languageServer;
    }

    @Override
    public ExtendedLanguageClient getLanguageClient() {
        return this.languageClient;
    }

    /**
     * Represents code action resolve context builder.
     */
    protected static class CodeActionResolveContextBuilder
            extends AbstractContextBuilder<CodeActionResolveContextBuilder> {

        private BallerinaLanguageServer languageServer;

        public CodeActionResolveContextBuilder(LanguageServerContext serverContext, BallerinaLanguageServer languageServer) {
            super(LSContextOperation.TXT_RESOLVE_CODE_ACTION, serverContext);
            this.languageServer = languageServer;
        }

        public CodeActionResolveContext build() {
            return new CodeActionResolveContextImpl(
                    this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.serverContext,
                    this.cancelChecker,
                    this.languageServer);
        }

        @Override
        public CodeActionResolveContextBuilder self() {
            return this;
        }
    }
}
