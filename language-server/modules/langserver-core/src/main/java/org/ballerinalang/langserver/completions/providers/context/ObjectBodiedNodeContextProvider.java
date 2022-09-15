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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic completion resolver for the nodes containing an object body block.
 *
 * @param <T> block node type
 * @since 2.0.0
 */
public abstract class ObjectBodiedNodeContextProvider<T extends Node> extends AbstractCompletionProvider<T> {
    public ObjectBodiedNodeContextProvider(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    /**
     * Get the snippets to be suggested at the constructor body.
     *
     * @param context {@link BallerinaCompletionContext}
     * @return {@link List} of completion items
     */
    protected List<LSCompletionItem> getBodyContextItems(BallerinaCompletionContext context, Node node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> typesInModule = QNameRefCompletionUtil.getTypesInModule(context, qNameRef);
            return this.getCompletionItemList(typesInModule, context);
        }
        
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getTypeDescContextItems(context));

        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PRIVATE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PUBLIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));
        
        if (this.isServiceOrClientObject(node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_REMOTE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_RESOURCE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_REMOTE_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_RESOURCE_FUNCTION_SIGNATURE.get()));
        }

        if (this.onSuggestInitMethod(node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_INIT_FUNCTION.get()));
        }

        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
        // Function keyword will be added through type items
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));

        return completionItems;
    }

    private boolean isServiceOrClientObject(Node node) {
        return node.kind() == SyntaxKind.SERVICE_DECLARATION || (node.kind() == SyntaxKind.OBJECT_CONSTRUCTOR
                && ((ObjectConstructorExpressionNode) node).objectTypeQualifiers().stream()
                .anyMatch(token -> token.kind() == SyntaxKind.CLIENT_KEYWORD
                        || token.kind() == SyntaxKind.CLIENT_KEYWORD));
    }

    private boolean onSuggestInitMethod(Node node) {
        NodeList<Node> members = null;
        if (node.kind() == SyntaxKind.SERVICE_DECLARATION) {
            members = ((ServiceDeclarationNode) node).members();
        } else if (node.kind() == SyntaxKind.OBJECT_CONSTRUCTOR) {
            members = ((ObjectConstructorExpressionNode) node).members();
        }

        return members != null && members.stream()
                .filter(member-> member.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION)
                .map(member->(FunctionDefinitionNode) member)
                .noneMatch(funcDef -> ItemResolverConstants.INIT.equals(funcDef.functionName().text()));
    }
}
