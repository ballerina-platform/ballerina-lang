/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.debugadapter.completion.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import org.ballerinalang.debugadapter.completion.resolver.ContextTypeResolver;
import org.ballerinalang.debugadapter.completion.util.CommonUtil;
import org.ballerinalang.debugadapter.completion.util.QNameReferenceUtil;
import org.ballerinalang.debugadapter.completion.util.SymbolUtil;
import org.eclipse.lsp4j.debug.CompletionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.debugadapter.completion.util.CommonUtil.getClientActions;
import static org.ballerinalang.debugadapter.completion.util.CompletionUtil.getCompletionItemList;

/**
 * Handles the completions for the {@link RemoteMethodCallActionNode}.
 *
 * @since 2201.1.0
 */
public class RemoteMethodCallActionNodeContext {

    /**
     * Get Completion items for the scope/ context.
     *
     * @param context Debug completion context
     * @param node    Node instance for the parser context
     * @return {@link List}     List of calculated Completion Items
     */
    public List<CompletionItem> getCompletions(CompletionContext context, RemoteMethodCallActionNode node) {
        List<CompletionItem> completionItems = new ArrayList<>();
        ContextTypeResolver resolver = new ContextTypeResolver(context);
        Optional<TypeSymbol> expressionType = node.expression().apply(resolver);

        if (expressionType.isEmpty() || !SymbolUtil.isClient(expressionType.get())) {
            return Collections.emptyList();
        }

        if (onSuggestClientActions(node, context)) {
            /*
            Covers the following case where a is a client object and we suggest the remote actions
            (1) a -> g<cursor>
             */
            List<Symbol> clientActions = getClientActions(expressionType.get());
            completionItems.addAll(getCompletionItemList(clientActions, context));
        } else if (CommonUtil.isInMethodCallParameterContext(context, node)) {
            /*
             * Covers the following cases:
             * 1. a->func(<cursor>)
             * 2. a->func(mod1:<cursor>)
             */
            if (QNameReferenceUtil.onQualifiedNameIdentifier(context, node)) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
                List<Symbol> exprEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);
                List<CompletionItem> items = getCompletionItemList(exprEntries, context);
                completionItems.addAll(items);
            } else {
                // TODO: Need to implement
                // completionItems.addAll(this.actionKWCompletions(context));
                // completionItems.addAll(this.expressionCompletions(context));
                // completionItems.addAll(this.getNamedArgExpressionCompletionItems(context, node));
            }
        }

        List<CompletionItem> completionItems1 = new ArrayList<>();

        for (CompletionItem completionItem : completionItems) {
            if (!completionItems1.contains(completionItem)) {
                completionItems1.add(completionItem);
            }
        }

        return completionItems1;
    }

    private boolean onSuggestClientActions(RemoteMethodCallActionNode node, CompletionContext context) {
        int cursor = context.getCursorPositionInTree();
        return node.rightArrowToken().textRange().endOffset() <= cursor &&
                (node.openParenToken().isMissing() || cursor <= node.openParenToken().textRange().startOffset());
    }
}
