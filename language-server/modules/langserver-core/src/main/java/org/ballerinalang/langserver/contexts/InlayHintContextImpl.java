/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
import org.ballerinalang.langserver.commons.InlayHintContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Inlay hint context implementation.
 *
 * @since 2201.6.0
 */
public class InlayHintContextImpl extends AbstractDocumentServiceContext implements InlayHintContext {
    InlayHintContextImpl(LSOperation operation,
                         String fileUri,
                         WorkspaceManager wsManager,
                         LanguageServerContext serverContext,
                         CancelChecker cancelChecker) {
        super(operation, fileUri, wsManager, serverContext, cancelChecker);
    }

    /**
     * Represents the inlay hint context Builder.
     */
    public static class InlayHintContextBuilder extends AbstractContextBuilder<InlayHintContextBuilder> {
        public InlayHintContextBuilder(LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_INLAY_HINT, serverContext);
        }

        @Override
        public InlayHintContext build() {
            return new InlayHintContextImpl(
                    this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.serverContext,
                    this.cancelChecker);
        }

        @Override
        public InlayHintContextBuilder self() {
            return this;
        }
    }
}
