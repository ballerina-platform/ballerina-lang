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

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.LSContext;
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
 * Parent completion provider for Right arrow action nodes.
 *
 * @param <T> Action node type
 * @since 2.0.0
 */
public abstract class RightArrowActionNodeContext<T extends Node> extends AbstractCompletionProvider<T> {

    public RightArrowActionNodeContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    protected List<LSCompletionItem> getFilteredItems(LSContext context, ExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ArrayList<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Predicate<Symbol> predicate;
        switch (node.kind()) {
            case SIMPLE_NAME_REFERENCE:
                predicate = symbol -> symbol.kind() != SymbolKind.FUNCTION
                        && symbol.name().equals(((SimpleNameReferenceNode) node).name().text());
                break;
            case FUNCTION_CALL:
                predicate = symbol -> symbol.kind() == SymbolKind.FUNCTION
                        && symbol.name().equals(((SimpleNameReferenceNode) ((FunctionCallExpressionNode) node)
                        .functionName()).name().text());
                break;
            default:
                return completionItems;
        }

        Optional<Symbol> filteredEntry = visibleSymbols.stream().filter(predicate).findAny();

        if (filteredEntry.isEmpty()) {
            return completionItems;
        }
        if (SymbolUtil.isClient(filteredEntry.get())) {
            /*
            Covers the following case where a is a client object and we suggest the remote actions
            (1) a -> g<cursor>
             */
            List<Symbol> clientActions = this.getClientActions(filteredEntry.get());
            completionItems.addAll(this.getCompletionItemList(clientActions, context));
        } else {
            /*
            Covers the following case where a is any other variable and we suggest the workers
            (1) a -> <cursor>
            (2) a -> w<cursor>
             */
            List<Symbol> filteredWorkers = visibleSymbols.stream()
                    .filter(symbol -> symbol.kind() == SymbolKind.WORKER)
                    .collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(filteredWorkers, context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_DEFAULT.get()));
        }

        return completionItems;
    }

    /**
     * Get the actions defined over and endpoint.
     *
     * @param symbol Endpoint variable symbol to evaluate
     * @return {@link List} List of extracted actions as Symbol Info
     */
    public List<Symbol> getClientActions(Symbol symbol) {
        if (!SymbolUtil.isObject(symbol)) {
            return new ArrayList<>();
        }
        BallerinaTypeDescriptor typeDescriptor = CommonUtil.getRawType(((VariableSymbol) symbol).typeDescriptor());
        return ((ObjectTypeDescriptor) typeDescriptor).methods().stream()
                .filter(method -> method.qualifiers().contains(Qualifier.REMOTE))
                .collect(Collectors.toList());
    }
}
