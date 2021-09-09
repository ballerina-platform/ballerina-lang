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

import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.HoverContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;

import javax.annotation.Nonnull;

/**
 * Hover context implementation.
 *
 * @since 1.2.0
 */
public class HoverContextImpl extends AbstractDocumentServiceContext implements HoverContext {

    private Token tokenAtCursor;
    
    private final Position cursorPosition;

    HoverContextImpl(LSOperation operation,
                     String fileUri,
                     WorkspaceManager wsManager,
                     Position cursorPosition,
                     LanguageServerContext serverContext) {
        super(operation, fileUri, wsManager, serverContext);
        this.cursorPosition = cursorPosition;
    }

    @Override
    public void setTokenAtCursor(@Nonnull Token token) {
        this.tokenAtCursor = token;
    }

    @Override
    public Token getTokenAtCursor() {
        if (this.tokenAtCursor == null) {
            throw new RuntimeException("Token has to be set before accessing");
        }

        return this.tokenAtCursor;
    }

    @Override
    public Position getCursorPosition() {
        return this.cursorPosition;
    }

    /**
     * Represents Language server signature help context Builder.
     *
     * @since 2.0.0
     */
    protected static class HoverContextBuilder extends AbstractContextBuilder<HoverContextBuilder> {
        
        private Position cursorPosition;

        public HoverContextBuilder(LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_HOVER, serverContext);
        }

        public HoverContext build() {
            return new HoverContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.cursorPosition,
                    this.serverContext);
        }
        
        public HoverContextBuilder withPosition(Position cursorPosition) {
            this.cursorPosition = cursorPosition;
            return self();
        }

        @Override
        public HoverContextBuilder self() {
            return this;
        }
    }
}
