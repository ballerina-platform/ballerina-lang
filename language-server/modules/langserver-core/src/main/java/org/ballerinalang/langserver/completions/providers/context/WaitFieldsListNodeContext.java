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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.WaitFieldsListNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.WORKER;

/**
 * Completion provider for {@link WaitFieldsListNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class WaitFieldsListNodeContext extends AbstractCompletionProvider<WaitFieldsListNode> {

    public WaitFieldsListNodeContext() {
        super(WaitFieldsListNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, WaitFieldsListNode node)
            throws LSCompletionException {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.VARIABLE
                    && ((VariableSymbol) symbol).typeDescriptor().typeKind() == TypeDescKind.FUTURE;
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, predicate);
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
            completionItems.addAll(this.getModuleCompletionItems(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }
    
    protected List<LSCompletionItem> expressionCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<Symbol> filteredList = visibleSymbols.stream() 
                .filter(CommonUtil.getVariableFilterPredicate().or(symbol -> symbol.kind() == WORKER))
                .collect(Collectors.toList());
        return this.getCompletionItemList(filteredList, context);
    }
    
    @Override
    public void sort(BallerinaCompletionContext context, WaitFieldsListNode node, 
                     List<LSCompletionItem> completionItems) {

        Optional<TypeSymbol> contextType = context.getContextType();
        if (contextType.isPresent()) {
            completionItems.forEach(lsCItem -> {
                String sortText = SortingUtil.genSortTextByAssignability(context, lsCItem, contextType.get());
                lsCItem.getCompletionItem().setSortText(sortText);
            });
            return;
        }
        super.sort(context, node, completionItems);
    }

}
