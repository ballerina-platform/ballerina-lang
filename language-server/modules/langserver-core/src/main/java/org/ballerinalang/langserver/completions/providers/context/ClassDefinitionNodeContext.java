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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.ClassDefinitionNodeContextUtil;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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
        } else if (inQualifierContext(context, node)) {
            completionItems.addAll(this.getCompletionsInQualifierContext(context, node));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private Collection<? extends LSCompletionItem> getCompletionsInQualifierContext(BallerinaCompletionContext context,
                                                                                    ClassDefinitionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NodeList<Token> qualifiers = node.classTypeQualifiers();
        List<Token> qualifiersAtCursor = CommonUtil.getQualifiersAtCursor(context);
        if (qualifiers.isEmpty() || qualifiersAtCursor.isEmpty()) {
            checkAndAddCompletionItemsForQualifierKinds(completionItems, qualifiers, context);
            return completionItems;
        }

        Token lastQualifier = qualifiersAtCursor.get(qualifiersAtCursor.size() - 1);
        //We do not suggest qualifiers after client and service keywords.
        if (lastQualifier.kind() != SyntaxKind.SERVICE_KEYWORD
                && lastQualifier.kind() != SyntaxKind.CLIENT_KEYWORD) {
            checkAndAddCompletionItemsForQualifierKinds(completionItems, qualifiers, context);
            return completionItems;
        }
        return completionItems;
    }

    private void checkAndAddCompletionItemsForQualifierKinds(List<LSCompletionItem> completionItems,
                                                             NodeList<Token> existingQualifiers,
                                                             BallerinaCompletionContext context) {

        List<SyntaxKind> qualifierKinds = List.of(SyntaxKind.ISOLATED_KEYWORD, SyntaxKind.READONLY_KEYWORD,
                SyntaxKind.DISTINCT_KEYWORD, SyntaxKind.CLIENT_KEYWORD, SyntaxKind.SERVICE_KEYWORD);
        qualifierKinds.stream().filter(kind -> !isQualifierKindAvailable(kind, existingQualifiers))
                .forEach(qualifierKind -> {
                    switch (qualifierKind) {
                        case ISOLATED_KEYWORD:
                            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
                            break;
                        case CLIENT_KEYWORD:
                            if (!isQualifierKindAvailable(SyntaxKind.SERVICE_KEYWORD, existingQualifiers)) {
                                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLIENT.get()));
                            }
                            break;
                        case SERVICE_KEYWORD:
                            if (!isQualifierKindAvailable(SyntaxKind.CLIENT_KEYWORD, existingQualifiers)) {
                                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_SERVICE.get()));
                            }
                            break;
                        case READONLY_KEYWORD:
                            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_READONLY.get()));
                            break;
                        case DISTINCT_KEYWORD:
                            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_DISTINCT.get()));
                            break;
                        default:
                            //ignore
                    }
                });
    }

    private boolean isQualifierKindAvailable(SyntaxKind qualifierKind, NodeList<Token> existingQualifiers) {
        return existingQualifiers.stream().anyMatch(token -> token.kind().equals(qualifierKind));
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ClassDefinitionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token classKeyword = node.classKeyword();

        if (withinBody(context, node)) {
            return true;
        }
        if (classKeyword.isMissing() || !(cursor <= node.closeBrace().textRange().startOffset())) {
            return false;
        }
        return inQualifierContext(context, node) || cursor > classKeyword.textRange().endOffset();
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

    private boolean inQualifierContext(BallerinaCompletionContext context, ClassDefinitionNode node) {
        Optional<Token> visibilityQualifier = node.visibilityQualifier();
        int cursor = context.getCursorPositionInTree();
        return visibilityQualifier.isPresent() && cursor >= visibilityQualifier.get().textRange().startOffset()
                && cursor <= node.classKeyword().textRange().startOffset();
    }

    private List<LSCompletionItem> getClassBodyCompletions(BallerinaCompletionContext context,
                                                           ClassDefinitionNode node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.FUNCTION;
            List<Symbol> typesInModule = QNameRefCompletionUtil.getTypesInModule(context, qNameRef);
            typesInModule.addAll(QNameRefCompletionUtil.getModuleContent(context, qNameRef, predicate));
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
