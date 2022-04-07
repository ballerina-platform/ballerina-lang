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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.ClassDefinitionNodeContextUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link ClassDefinitionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ClassDefinitionNodeContext extends AbstractCompletionProvider<ClassDefinitionNode> {

    public ClassDefinitionNodeContext() {
        super(ClassDefinitionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ClassDefinitionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.withinBody(context, node)) {
            completionItems.addAll(this.getClassBodyCompletions(context, node));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ClassDefinitionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token classKeyword = node.classKeyword();

        return !classKeyword.isMissing() && cursor > classKeyword.textRange().endOffset()
                && cursor <= node.closeBrace().textRange().startOffset();
    }

    private boolean withinBody(BallerinaCompletionContext context, ClassDefinitionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openBrace = node.openBrace();
        Token closeBrace = node.closeBrace();

        if (openBrace.isMissing() || closeBrace.isMissing()) {
            return false;
        }

        return cursor >= openBrace.textRange().endOffset() && cursor <= closeBrace.textRange().startOffset();
    }

    private List<LSCompletionItem> getClassBodyCompletions(BallerinaCompletionContext context,
                                                           ClassDefinitionNode node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> typesInModule = QNameReferenceUtil.getTypesInModule(context, qNameRef);
            return this.getCompletionItemList(typesInModule, context);
        }
        if (onSuggestionsAfterQualifiers(context, context.getNodeAtCursor())) {
            return this.getCompletionItemsOnQualifiers(context.getNodeAtCursor(), context);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>();

        // Here we do not add the function keyword as type descriptor completion items add it.
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PRIVATE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PUBLIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_REMOTE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_RESOURCE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_REMOTE_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_RESOURCE_FUNCTION_SIGNATURE.get()));
        if (ClassDefinitionNodeContextUtil.onSuggestInitFunction(node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_INIT_FUNCTION.get()));
        }
        completionItems.addAll(this.getTypeDescContextItems(context));

        return completionItems;
    }

    @Override
    protected List<LSCompletionItem> getCompletionItemsOnQualifiers(Node node, BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(super.getCompletionItemsOnQualifiers(node, context));
        List<Token> qualifiers = CommonUtil.getQualifiersOfNode(context, node);
        if (qualifiers.isEmpty()) {
            return completionItems;
        }
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);
        if (lastQualifier.kind() == SyntaxKind.ISOLATED_KEYWORD
                || lastQualifier.kind() == SyntaxKind.TRANSACTIONAL_KEYWORD) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_REMOTE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_RESOURCE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_REMOTE_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_RESOURCE_FUNCTION_SIGNATURE.get()));
        } else if (lastQualifier.kind() == SyntaxKind.PRIVATE_KEYWORD
                || lastQualifier.kind() == SyntaxKind.PUBLIC_KEYWORD) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_REMOTE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_RESOURCE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_REMOTE_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_RESOURCE_FUNCTION_SIGNATURE.get()));
            if (ClassDefinitionNodeContextUtil.onSuggestInitFunction(node)) {
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_INIT_FUNCTION.get()));
            }
            completionItems.addAll(this.getTypeDescContextItems(context));
        }
        return completionItems;
    }
}
