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

import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ModuleXMLNamespaceDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ModuleXMLNamespaceDeclarationNodeContext extends
        AbstractCompletionProvider<ModuleXMLNamespaceDeclarationNode> {

    public ModuleXMLNamespaceDeclarationNodeContext() {
        super(ModuleXMLNamespaceDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ModuleXMLNamespaceDeclarationNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (this.onSuggestConstants(context, node)) {
            Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.CONSTANT
                    && ((ConstantSymbol) symbol).broaderTypeDescriptor().typeKind() == TypeDescKind.STRING;
            NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

            if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, predicate);
                completionItems.addAll(this.getCompletionItemList(moduleContent, context));
            } else {
                List<Symbol> constants = context.visibleSymbols(context.getCursorPosition()).stream()
                        .filter(predicate)
                        .collect(Collectors.toList());
                completionItems.addAll(this.getCompletionItemList(constants, context));
                completionItems.addAll(this.getModuleCompletionItems(context));
            }
        } else if (this.onSuggestAsKeyword(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_AS.get()));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean onSuggestConstants(BallerinaCompletionContext context, ModuleXMLNamespaceDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token xmlnsKeyword = node.xmlnsKeyword();
        Optional<Token> asKeyword = node.asKeyword();
        ExpressionNode namespaceuri = node.namespaceuri();

        return xmlnsKeyword.textRange().endOffset() < cursor
                && (asKeyword.isEmpty() || asKeyword.get().textRange().startOffset() > cursor)
                && (namespaceuri.isMissing() || cursor < namespaceuri.textRange().endOffset() + 1);
    }

    private boolean onSuggestAsKeyword(BallerinaCompletionContext context, ModuleXMLNamespaceDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        ExpressionNode namespaceuri = node.namespaceuri();
        Optional<Token> asKeyword = node.asKeyword();

        return !namespaceuri.isMissing() && cursor > namespaceuri.textRange().endOffset()
                && (asKeyword.isEmpty() || asKeyword.get().isMissing());
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ModuleXMLNamespaceDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token xmlnsKeyword = node.xmlnsKeyword();
        
        return !xmlnsKeyword.isMissing() && cursor > xmlnsKeyword.textRange().endOffset();
    }
}
