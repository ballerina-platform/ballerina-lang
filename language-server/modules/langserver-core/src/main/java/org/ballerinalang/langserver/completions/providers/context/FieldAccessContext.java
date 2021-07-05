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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.FieldAccessCompletionResolver;
import org.ballerinalang.langserver.completions.util.ForeachCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.ballerinalang.langserver.completions.util.TypeGuardCompletionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generic Completion provider for field access providers.
 * eg: Optional Field access and Field Access
 *
 * @param <T> Field access node type
 * @since 2.0.0
 */
public abstract class FieldAccessContext<T extends Node> extends AbstractCompletionProvider<T> {

    public FieldAccessContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    /**
     * Get the entries for the given field access expression.
     * This particular logic is written in order to capture the chain completion usage as well.
     *
     * @param ctx  language server operation context
     * @param expr expression node to evaluate
     * @return {@link List} of filtered scope entries
     */
    protected List<LSCompletionItem> getEntries(BallerinaCompletionContext ctx,
                                                ExpressionNode expr,
                                                boolean optionalFieldAccess) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        FieldAccessCompletionResolver resolver = new FieldAccessCompletionResolver(ctx);
        List<Symbol> symbolList = resolver.getVisibleEntries(expr);
        //Add typegurad and foreach snippets.
        if (expr.parent().kind() == SyntaxKind.FIELD_ACCESS) {
            Optional<TypeSymbol> typeSymbol = resolver.getTypeSymbol(expr);
            if (typeSymbol.isPresent()) {
                FieldAccessExpressionNode fieldAccessExpr = (FieldAccessExpressionNode) expr.parent();
                completionItems.addAll(TypeGuardCompletionUtil.getTypeGuardDestructedItems(
                        ctx, fieldAccessExpr, typeSymbol.get()));
                completionItems.addAll(ForeachCompletionUtil.getForeachCompletionItemsForIterable(ctx,
                        fieldAccessExpr, typeSymbol.get()));
            }
        }
        completionItems.addAll(this.getCompletionItemList(symbolList, ctx));
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, T node, List<LSCompletionItem> completionItems) {
        // We assign higher priority to record/object fields while assigning other completion items default priorities
        completionItems.forEach(completionItem -> {
            int rank;
            switch (completionItem.getType()) {
                case OBJECT_FIELD:
                case RECORD_FIELD:
                    rank = 1;
                    break;
                default:
                    rank = SortingUtil.toRank(completionItem, 1);
            }

            completionItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank));
        });
    }
}
