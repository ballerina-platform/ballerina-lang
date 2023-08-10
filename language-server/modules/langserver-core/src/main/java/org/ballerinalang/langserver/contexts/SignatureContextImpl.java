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

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.SignatureContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelpCapabilities;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.Optional;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class SignatureContextImpl extends PositionedOperationContextImpl implements SignatureContext {

    private final SignatureHelpCapabilities capabilities;
    private NonTerminalNode nodeAtCursor;

    SignatureContextImpl(LSOperation operation,
                         String fileUri,
                         WorkspaceManager wsManager,
                         SignatureHelpCapabilities capabilities,
                         Position cursorPos,
                         LanguageServerContext serverContext,
                         CancelChecker cancelChecker) {
        super(operation, fileUri, cursorPos, wsManager, serverContext, cancelChecker);
        this.capabilities = capabilities;
    }

    @Override
    public SignatureHelpCapabilities capabilities() {
        return this.capabilities;
    }

    @Override
    public Optional<NonTerminalNode> getNodeAtCursor() {
        return Optional.ofNullable(this.nodeAtCursor);
    }

    @Override
    public void setNodeAtCursor(NonTerminalNode node) {
        if (this.nodeAtCursor != null) {
            throw new RuntimeException("Setting the node more than once is not allowed");
        }
        this.nodeAtCursor = node;
    }

    /**
     * Represents Language server signature help context Builder.
     *
     * @since 2.0.0
     */
    protected static class SignatureContextBuilder extends AbstractContextBuilder<SignatureContextBuilder> {

        private final SignatureHelpCapabilities capabilities;
        private Position position;

        public SignatureContextBuilder(LanguageServerContext serverContext, SignatureHelpCapabilities capabilities) {
            super(LSContextOperation.TXT_SIGNATURE, serverContext);
            this.capabilities = capabilities;
        }

        /**
         * Setter for the cursor position.
         *
         * @param position cursor position
         */
        public SignatureContextBuilder withPosition(Position position) {
            this.position = position;
            return self();
        }

        public SignatureContext build() {
            return new SignatureContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.capabilities,
                    this.position,
                    this.serverContext,
                    this.cancelChecker);
        }

        @Override
        public SignatureContextBuilder self() {
            return this;
        }
    }
}
