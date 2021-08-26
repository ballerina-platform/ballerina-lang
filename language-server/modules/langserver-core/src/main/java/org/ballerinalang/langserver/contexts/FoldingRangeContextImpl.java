/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.ballerinalang.langserver.commons.FoldingRangeContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Represents folding range context implementation.
 *
 * @since 2.0.0
 */
public class FoldingRangeContextImpl extends AbstractDocumentServiceContext implements FoldingRangeContext {

    private final boolean lineFoldingOnly;

    FoldingRangeContextImpl(LSOperation operation,
                            String uri,
                            WorkspaceManager wsManager,
                            boolean lineFoldingOnly,
                            LanguageServerContext serverContext,
                            CancelChecker cancelChecker) {
        super(operation, uri, wsManager, serverContext, cancelChecker);
        this.lineFoldingOnly = lineFoldingOnly;
    }

    @Override
    public boolean getLineFoldingOnly() {
        return this.lineFoldingOnly;
    }

    /**
     * Represents folding range context Builder.
     */
    protected static class FoldingRangeContextBuilder extends AbstractContextBuilder<FoldingRangeContextBuilder> {

        private final boolean lineFoldingOnly;

        public FoldingRangeContextBuilder(boolean lineFoldingOnly, LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_FOLDING_RANGE, serverContext);
            this.lineFoldingOnly = lineFoldingOnly;
        }

        public FoldingRangeContext build() {
            return new FoldingRangeContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.lineFoldingOnly,
                    this.serverContext,
                    this.cancelChecker);
        }

        @Override
        public FoldingRangeContextBuilder self() {
            return this;
        }
    }
}
