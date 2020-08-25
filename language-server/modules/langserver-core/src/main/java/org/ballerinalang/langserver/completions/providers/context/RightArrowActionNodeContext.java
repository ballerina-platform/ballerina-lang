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

import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Parent completion provider for Right arrow action nodes.
 *
 * @param <T> Action node type
 * @since 2.0.0
 */
public class RightArrowActionNodeContext<T extends Node> extends AbstractCompletionProvider<T> {

    public RightArrowActionNodeContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    protected List<LSCompletionItem> getFilteredItems(LSContext context, ExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ArrayList<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Predicate<Scope.ScopeEntry> predicate;
        switch (node.kind()) {
            case SIMPLE_NAME_REFERENCE:
                predicate = scopeEntry -> {
                    BSymbol symbol = scopeEntry.symbol;
                    return !(symbol instanceof BInvokableSymbol)
                            && symbol.getName().getValue().equals(((SimpleNameReferenceNode) node).name().text());
                };
                break;
            case FUNCTION_CALL:
                predicate = scopeEntry -> {
                    BSymbol symbol = scopeEntry.symbol;
                    return symbol instanceof BInvokableSymbol && !(symbol instanceof BOperatorSymbol)
                            && symbol.getName().getValue()
                            .equals(((SimpleNameReferenceNode) ((FunctionCallExpressionNode) node)
                                    .functionName()).name().text());
                };
                break;
            default:
                return completionItems;
        }

        Optional<Scope.ScopeEntry> filteredEntry = visibleSymbols.stream().filter(predicate).findAny();

        if (!filteredEntry.isPresent()) {
            return completionItems;
        }
        if (CommonUtil.isClientObject(filteredEntry.get().symbol)) {
            /*
            Covers the following case where a is a client object and we suggest the remote actions
            (1) a -> g<cursor>
             */
            List<Scope.ScopeEntry> clientActions =
                    FilterUtils.getClientActions((BObjectTypeSymbol) filteredEntry.get().symbol.type.tsymbol);
            completionItems.addAll(this.getCompletionItemList(clientActions, context));
        } else {
            /*
            Covers the following case where a is any other variable and we suggest the workers
            (1) a -> <cursor>
            (2) a -> w<cursor>
             */
            List<Scope.ScopeEntry> filteredWorkers = visibleSymbols.stream()
                    .filter(scopeEntry -> !(scopeEntry.symbol instanceof BInvokableSymbol)
                            && (scopeEntry.symbol.flags & Flags.WORKER) == Flags.WORKER)
                    .collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(filteredWorkers, context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_DEFAULT.get()));
        }

        return completionItems;
    }
}
