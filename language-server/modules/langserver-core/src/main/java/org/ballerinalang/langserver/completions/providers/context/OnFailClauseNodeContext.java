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
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.OnFailClauseNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link OnFailClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class OnFailClauseNodeContext extends AbstractCompletionProvider<OnFailClauseNode> {

    public OnFailClauseNodeContext() {
        super(OnFailClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, OnFailClauseNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.onSuggestFailKeyword(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FAIL.get()));
        } else if (!this.onSuggestTypeDescriptors(context, node)) {
            return Collections.emptyList();
        } else {
            NonTerminalNode symbolAtCursor = context.getNodeAtCursor();

            Predicate<Symbol> errorPredicate = SymbolUtil::isError;
            if (symbolAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                QualifiedNameReferenceNode qRef = (QualifiedNameReferenceNode) symbolAtCursor;
                List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, qRef, errorPredicate);
                completionItems.addAll(this.getCompletionItemList(moduleContent, context));
            } else {
                completionItems.addAll(this.getModuleCompletionItems(context));
                List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
                List<Symbol> errEntries = visibleSymbols.stream()
                        .filter(errorPredicate)
                        .collect(Collectors.toList());
                completionItems.addAll(this.getCompletionItemList(errEntries, context));

                // Add 'var' completion item
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
            }
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, OnFailClauseNode node) {
        return !node.onKeyword().isMissing();
    }
    
    private boolean onSuggestFailKeyword(BallerinaCompletionContext context, OnFailClauseNode node) {
        int cursor = context.getCursorPositionInTree();
        if (node.onKeyword().isMissing()) {
            return false;
        }

        return node.failKeyword().isMissing() && node.onKeyword().textRange().endOffset() < cursor;
    }

    private boolean onSuggestTypeDescriptors(BallerinaCompletionContext context, OnFailClauseNode node) {
        int cursor = context.getCursorPositionInTree();
        BlockStatementNode blockStatement = node.blockStatement();
        Token failKeyword = node.failKeyword();

        if (failKeyword.isMissing()) {
            return false;
        }

        return cursor > failKeyword.textRange().endOffset()
                && (blockStatement.isMissing()
                || cursor < blockStatement.openBraceToken().textRange().endOffset());
    }
}
