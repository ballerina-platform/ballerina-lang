/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.SemanticTokensContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Represents semantic tokens context implementation.
 *
 * @since 2.0.0
 */
public class SemanticTokensContextImpl extends AbstractDocumentServiceContext implements SemanticTokensContext {

    SemanticTokensContextImpl(LSOperation operation, String fileUri,
                              WorkspaceManager wsManager,
                              LanguageServerContext serverContext,
                              CancelChecker cancelChecker) {
        super(operation, fileUri, wsManager, serverContext, cancelChecker);
    }

    /**
     * Represents semantic tokens context Builder.
     */
    protected static class SemanticTokensContextBuilder extends
            AbstractContextBuilder<SemanticTokensContextImpl.SemanticTokensContextBuilder> {

        public SemanticTokensContextBuilder(LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_SEMANTIC_TOKENS_FULL, serverContext);
        }

        public SemanticTokensContext build() {
            return new SemanticTokensContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.serverContext,
                    this.cancelChecker);
        }

        @Override
        public SemanticTokensContextImpl.SemanticTokensContextBuilder self() {
            return this;
        }
    }
}
