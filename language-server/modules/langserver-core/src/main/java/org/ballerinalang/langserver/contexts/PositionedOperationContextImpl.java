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
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Abstract implementation of the {@link PositionedOperationContext}.
 *
 * @since 2.0.0
 */
public abstract class PositionedOperationContextImpl extends AbstractDocumentServiceContext
        implements PositionedOperationContext {

    @Deprecated(forRemoval = true)
    PositionedOperationContextImpl(LSOperation operation,
                                   String fileUri,
                                   WorkspaceManager wsManager,
                                   LanguageServerContext serverContext) {
        super(operation, fileUri, wsManager, serverContext);
    }
    
    PositionedOperationContextImpl(LSOperation operation,
                                   String fileUri,
                                   WorkspaceManager wsManager,
                                   LanguageServerContext serverContext,
                                   CancelChecker cancelChecker) {
        super(operation, fileUri, wsManager, serverContext, cancelChecker);
    }

    /**
     * Represents Language server positioned operation context Builder.
     *
     * @param <T> Context type
     */
    protected abstract static class PositionedOperationContextBuilder<T extends DocumentServiceContext>
            extends AbstractContextBuilder<PositionedOperationContextBuilder<T>> {

        protected Position position;

        public PositionedOperationContextBuilder(LSContextOperation operation, LanguageServerContext serverContext) {
            super(operation, serverContext);
        }

        /**
         * Setter for the cursor position.
         *
         * @param position cursor position
         */
        public PositionedOperationContextBuilder<T> withPosition(Position position) {
            this.position = position;
            return self();
        }

        public abstract T build();

        @Override
        public PositionedOperationContextBuilder<T> self() {
            return this;
        }
    }
}
