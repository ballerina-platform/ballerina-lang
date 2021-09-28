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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.util.ContextTypeResolver;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Handles the completions for the {@link RemoteMethodCallActionNode}.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class RemoteMethodCallActionNodeContext extends RightArrowActionNodeContext<RemoteMethodCallActionNode> {

    public RemoteMethodCallActionNodeContext() {
        super(RemoteMethodCallActionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, RemoteMethodCallActionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
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
            List<Symbol> clientActions = this.getClientActions(expressionType.get());
            completionItems.addAll(this.getCompletionItemList(clientActions, context));
        } else if (this.withinParameterContext(node, context)) {
            /*
             * Covers the following cases:
             * 1. a->func(<cursor>)
             * 2. a->func(mod1:<cursor>)
             */
            if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
                List<Symbol> exprEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);
                List<LSCompletionItem> items = this.getCompletionItemList(exprEntries, context);
                completionItems.addAll(items);
            } else {
                completionItems.addAll(this.actionKWCompletions(context));
                completionItems.addAll(this.expressionCompletions(context));
            }
        }

        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean onSuggestClientActions(RemoteMethodCallActionNode node, BallerinaCompletionContext context) {
        int cursor = context.getCursorPositionInTree();
        return node.rightArrowToken().textRange().endOffset() <= cursor &&
                (node.openParenToken().isMissing() || cursor <= node.openParenToken().textRange().startOffset());
    }

    private boolean withinParameterContext(RemoteMethodCallActionNode node, BallerinaCompletionContext context) {
        int cursor = context.getCursorPositionInTree();
        return !node.openParenToken().isMissing() && node.openParenToken().textRange().endOffset() <= cursor &&
                (node.closeParenToken().isMissing() || cursor <= node.closeParenToken().textRange().startOffset());
    }

    @Override
    public void sort(BallerinaCompletionContext context,
                     RemoteMethodCallActionNode node,
                     List<LSCompletionItem> completionItems) {
        if (!withinParameterContext(node, context)) {
            super.sort(context, node, completionItems);
            return;
        }
        Optional<TypeSymbol> parameterSymbol = context.getContextType();
        if (parameterSymbol.isEmpty()) {
            SortingUtil.toDefaultSorting(context, completionItems);
            return;
        }
        TypeSymbol symbol = parameterSymbol.get();
        for (LSCompletionItem completionItem : completionItems) {
            completionItem.getCompletionItem()
                    .setSortText(SortingUtil.genSortTextByAssignability(context, completionItem, symbol));
        }
    }
}
