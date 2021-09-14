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

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.completions.util.ContextTypeResolver;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Language server context implementation.
 *
 * @since 2.0.0
 */
public class BallerinaCompletionContextImpl extends CompletionContextImpl implements BallerinaCompletionContext {
    private final List<Node> resolverChain = new ArrayList<>();
    private Token tokenAtCursor;
    private NonTerminalNode nodeAtCursor;
    private boolean isContextTypeCaptured = false;
    private TypeSymbol contextType;
    private boolean isCapturedEnclosingNode = false;
    private ModuleMemberDeclarationNode enclosingNode = null;
    private final CompletionParams completionParams;

    public BallerinaCompletionContextImpl(CompletionContext context, LanguageServerContext serverContext,
                                          CompletionParams completionParams) {
        super(context.operation(),
                context.fileUri(),
                context.workspace(),
                context.getCapabilities(),
                context.getCursorPosition(),
                serverContext);
        this.completionParams = completionParams;
    }

    public BallerinaCompletionContextImpl(CompletionContext context,
                                          LanguageServerContext serverContext,
                                          CompletionParams completionParams,
                                          CancelChecker cancelChecker) {
        super(context.operation(),
                context.fileUri(),
                context.workspace(),
                context.getCapabilities(),
                context.getCursorPosition(),
                serverContext,
                cancelChecker);
        this.completionParams = completionParams;
    }

    @Override
    public void setTokenAtCursor(Token token) {
        if (this.tokenAtCursor != null) {
            throw new RuntimeException("Setting the token more than once is not allowed");
        }
        this.tokenAtCursor = token;
    }

    @Override
    public Token getTokenAtCursor() {
        return this.tokenAtCursor;
    }

    @Override
    public void setNodeAtCursor(NonTerminalNode node) {
        if (this.nodeAtCursor != null) {
            throw new RuntimeException("Setting the node more than once is not allowed");
        }
        this.nodeAtCursor = node;
    }

    @Override
    public NonTerminalNode getNodeAtCursor() {
        return this.nodeAtCursor;
    }

    @Override
    public void addResolver(Node node) {
        this.resolverChain.add(node);
    }

    @Override
    public List<Node> getResolverChain() {
        return this.resolverChain;
    }

    @Override
    public Optional<TypeSymbol> getContextType() {
        if (!this.isContextTypeCaptured) {
            this.isContextTypeCaptured = true;
            NonTerminalNode node = getNodeAtCursor();
            do {
                ContextTypeResolver contextTypeResolver = new ContextTypeResolver(this);
                Optional<TypeSymbol> typeSymbol = node.apply(contextTypeResolver);
                if (typeSymbol == null || typeSymbol.isEmpty()) {
                    this.contextType = null;
                } else {
                    this.contextType = typeSymbol.get();
                }
                node = node.parent();
            } while (this.contextType == null && node != null);
        }

        return Optional.ofNullable(this.contextType);
    }

    @Override
    public CompletionParams getCompletionParams() {
        return this.completionParams;
    }

    @Override
    public Optional<ModuleMemberDeclarationNode> enclosedModuleMember() {
        if (!this.isCapturedEnclosingNode) {
            this.isCapturedEnclosingNode = true;
            Optional<SyntaxTree> syntaxTree = this.currentSyntaxTree();
            if (syntaxTree.isEmpty()) {
                throw new RuntimeException("Cannot find a valid syntax tree");
            }
            this.enclosingNode = BallerinaContextUtils.getEnclosingModuleMember(syntaxTree.get(),
                    this.getCursorPositionInTree()).orElse(null);
        }

        return Optional.ofNullable(this.enclosingNode);
    }
}
