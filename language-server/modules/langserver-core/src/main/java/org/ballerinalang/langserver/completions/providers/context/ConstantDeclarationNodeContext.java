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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ModulePartNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ConstantDeclarationNodeContext extends NodeWithRHSInitializerProvider<ConstantDeclarationNode> {

    public ConstantDeclarationNodeContext() {
        super(ConstantDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ConstantDeclarationNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        ResolvedContext resolvedContext = ResolvedContext.NONE;
        if (this.onTypeDescContext(context, node)) {
            if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                List<Symbol> typesInModule = QNameReferenceUtil.getTypesInModule(context, qNameRef);
                completionItems.addAll(this.getCompletionItemList(typesInModule, context));
            } else {
                completionItems.addAll(this.getTypeDescContextItems(context));
            }
            resolvedContext = ResolvedContext.TYPEDESC;
        } else if (this.onExpressionContext(context, node)) {
            completionItems.addAll(this.initializerContextCompletions(context,
                    node.typeDescriptor().orElse(null), node.initializer()));
            resolvedContext = ResolvedContext.EXPRESSION;
        }
        this.sort(context, node, completionItems, resolvedContext);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ConstantDeclarationNode node,
                     List<LSCompletionItem> completionItems, Object... metaData) {
        ResolvedContext resolvedContext = (ResolvedContext) metaData[0];

        switch (resolvedContext) {
            case TYPEDESC:
                completionItems.forEach(lsCItem -> {
                    String sortText = SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
                    lsCItem.getCompletionItem().setSortText(sortText);
                });
                break;
            case EXPRESSION:
                super.sort(context, node, completionItems);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ConstantDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token constKeyword = node.constKeyword();

        return !constKeyword.isMissing() && cursor > constKeyword.textRange().endOffset();
    }

    @Override
    protected List<LSCompletionItem> initializerContextCompletions(BallerinaCompletionContext context,
                                                                   Node typeDesc, Node initializer) {
        // Note: Type descriptor is possibly null. Hence, if we are going to use it, we have to be aware of that.
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        Predicate<Symbol> predicate =
                symbol -> symbol.kind() == SymbolKind.CONSTANT || symbol.kind() == SymbolKind.ENUM_MEMBER;
        List<Symbol> constants;
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            constants = QNameReferenceUtil.getModuleContent(context, qNameRef, predicate);
        } else {
            constants = context.visibleSymbols(context.getCursorPosition()).stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRUE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FALSE.get()));
        }
        completionItems.addAll(this.getCompletionItemList(constants, context));

        return completionItems;
    }

    private boolean onExpressionContext(BallerinaCompletionContext context, ConstantDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token equalsToken = node.equalsToken();

        return !equalsToken.isMissing() && cursor > equalsToken.textRange().startOffset();
    }

    private boolean onTypeDescContext(BallerinaCompletionContext context, ConstantDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token constKeyword = node.constKeyword();
        Token variableName = node.variableName();
        Optional<TypeDescriptorNode> tDesc = node.typeDescriptor();

        if (cursor < constKeyword.textRange().endOffset() + 1) {
            return false;
        }
        if (tDesc.isPresent()) {
            return cursor <= tDesc.get().textRange().endOffset();
        }
        if (!variableName.isMissing()) {
            return cursor <= variableName.textRange().endOffset();
        }

        return true;
    }

    enum ResolvedContext {
        EXPRESSION,
        TYPEDESC,
        NONE
    }
}
