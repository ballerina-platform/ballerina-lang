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
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link DistinctTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class DistinctTypeDescriptorNodeContext extends AbstractCompletionProvider<DistinctTypeDescriptorNode> {

    public DistinctTypeDescriptorNodeContext() {
        super(DistinctTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, DistinctTypeDescriptorNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Predicate<Symbol> predicate = CommonUtil.typesFilter()
                .and(symbol -> {
                    if (symbol.kind() == SymbolKind.CLASS) {
                        return true;
                    }
                    if (symbol.kind() != SymbolKind.TYPE_DEFINITION) {
                        return false;
                    }
                    TypeSymbol typeSymbol = ((TypeDefinitionSymbol) symbol).typeDescriptor();
                    TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);

                    return rawType.typeKind() == TypeDescKind.OBJECT || rawType.typeKind() == TypeDescKind.ERROR;
                });

        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, qNameRef, predicate);
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            // Error type will be added via the module completion items
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get()));
            List<Symbol> filteredSymbols = context.visibleSymbols(context.getCursorPosition()).stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(filteredSymbols, context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, DistinctTypeDescriptorNode node,
                     List<LSCompletionItem> completionItems) {
        completionItems.forEach(lsCItem -> {
            String sortText = SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
            lsCItem.getCompletionItem().setSortText(sortText);
        });
    }
}
