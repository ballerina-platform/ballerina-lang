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

import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ActionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Parent completion provider for Right arrow action nodes.
 *
 * @param <T> Action node type
 * @since 2.0.0
 */
public abstract class RightArrowActionNodeContext<T extends ActionNode> extends InvocationNodeContextProvider<T> {

    public RightArrowActionNodeContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    protected List<LSCompletionItem> getFilteredItems(BallerinaCompletionContext context, T node,
                                                      ExpressionNode expressionNode) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        Optional<TypeSymbol> expressionType = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            LinePosition linePosition = expressionNode.location().lineRange().endLine();
            expressionType = context.currentSemanticModel().get()
                    .expectedType(context.currentDocument().get(), linePosition);
        }

        if (expressionType.isPresent() && SymbolUtil.isClient(expressionType.get())) {
            /*
            Covers the following case where a is a client object and we suggest the remote actions
            (1) a -> g<cursor>
             */
            List<Symbol> clientActions = this.getClientActions(expressionType.get());
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
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
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
        TypeSymbol typeDescriptor = CommonUtil.getRawType(SymbolUtil.getTypeDescriptor(symbol).orElseThrow());
        return ((ObjectTypeSymbol) typeDescriptor).methods().values().stream()
                .filter(method -> method.qualifiers().contains(Qualifier.REMOTE) 
                        || method.qualifiers().contains(Qualifier.RESOURCE))
                .collect(Collectors.toList());
    }
}
