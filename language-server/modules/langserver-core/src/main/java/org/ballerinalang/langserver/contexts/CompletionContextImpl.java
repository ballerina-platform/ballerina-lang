/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.contexts;

import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class CompletionContextImpl extends PositionedOperationContextImpl implements CompletionContext {

    private final CompletionCapabilities capabilities;

    CompletionContextImpl(LSOperation operation,
                          String fileUri,
                          WorkspaceManager wsManager,
                          CompletionCapabilities capabilities,
                          Position cursorPosition,
                          LanguageServerContext serverContext) {
        this(operation, fileUri, wsManager, capabilities, cursorPosition, serverContext, null);
    }

    CompletionContextImpl(LSOperation operation,
                          String fileUri,
                          WorkspaceManager wsManager,
                          CompletionCapabilities capabilities,
                          Position cursorPosition,
                          LanguageServerContext serverContext,
                          CancelChecker cancelChecker) {
        super(operation, fileUri, cursorPosition, wsManager, serverContext, cancelChecker);
        this.capabilities = capabilities;
    }

    @Override
    public CompletionCapabilities getCapabilities() {
        return this.capabilities;
    }
    
    /**
     * Represents Language server completion context Builder.
     *
     * @since 2.0.0
     */
    protected static class CompletionContextBuilder extends AbstractContextBuilder<CompletionContextBuilder> {

        private CompletionCapabilities capabilities;
        private Position cursor;

        /**
         * Context Builder constructor.
         */
        public CompletionContextBuilder(LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_COMPLETION, serverContext);
        }

        /**
         * Setter for the capabilities.
         *
         * @param capabilities completion capabilities to set
         */
        public CompletionContextBuilder withCapabilities(CompletionCapabilities capabilities) {
            this.capabilities = capabilities;
            return self();
        }

        /**
         * Setter for the position.
         *
         * @param position cursor position where the completion triggered
         */
        public CompletionContextBuilder withCursorPosition(Position position) {
            this.cursor = position;
            return self();
        }

        public CompletionContext build() {
            return new CompletionContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.capabilities,
                    this.cursor,
                    this.serverContext);
        }

        @Override
        public CompletionContextBuilder self() {
            return this;
        }
    }
}
