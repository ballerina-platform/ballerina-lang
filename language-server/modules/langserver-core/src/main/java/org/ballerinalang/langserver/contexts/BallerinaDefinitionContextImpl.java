/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.BallerinaDefinitionContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.Optional;

/**
 * Implementation for the {@link BallerinaDefinitionContext}.
 *
 * @since 2.0.0
 */
public class BallerinaDefinitionContextImpl 
        extends PositionedOperationContextImpl implements BallerinaDefinitionContext {
    private boolean capturedEnclosingNode = false;
    private ModuleMemberDeclarationNode enclosingNode = null;

    BallerinaDefinitionContextImpl(LSOperation operation,
                                   String fileUri,
                                   WorkspaceManager wsManager,
                                   Position position,
                                   LanguageServerContext serverContext,
                                   CancelChecker cancelChecker) {
        super(operation, fileUri, position, wsManager, serverContext, cancelChecker);
    }

    @Override
    public Optional<ModuleMemberDeclarationNode> enclosedModuleMember() {
        if (!this.capturedEnclosingNode) {
            this.capturedEnclosingNode = true;
            Optional<SyntaxTree> syntaxTree = this.currentSyntaxTree();
            if (syntaxTree.isEmpty()) {
                throw new RuntimeException("Cannot find a valid syntax tree");
            }
            this.enclosingNode = BallerinaContextUtils.getEnclosingModuleMember(syntaxTree.get(),
                    this.getCursorPositionInTree()).orElse(null);
        }

        return Optional.ofNullable(this.enclosingNode);
    }
    
    /**
     * Represents Language server completion context Builder.
     *
     * @since 2.0.0
     */
    protected static class DefinitionContextBuilder extends AbstractContextBuilder<DefinitionContextBuilder> {
        private Position cursor;

        /**
         * Context Builder constructor.
         */
        public DefinitionContextBuilder(LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_DEFINITION, serverContext);
        }

        /**
         * Setter for the position.
         *
         * @param position cursor position where the completion triggered
         */
        public DefinitionContextBuilder withCursorPosition(Position position) {
            this.cursor = position;
            return self();
        }

        public BallerinaDefinitionContext build() {
            return new BallerinaDefinitionContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.cursor,
                    this.serverContext,
                    this.cancelChecker);
        }

        @Override
        public DefinitionContextBuilder self() {
            return this;
        }
    }
}
