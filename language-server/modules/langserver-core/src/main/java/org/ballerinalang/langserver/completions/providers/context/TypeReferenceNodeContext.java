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
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link TypeReferenceNode} context.
 * This is a common handler for both the record-type-inclusion and for object-type-inclusion
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TypeReferenceNodeContext extends AbstractCompletionProvider<TypeReferenceNode> {

    public TypeReferenceNodeContext() {
        super(TypeReferenceNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TypeReferenceNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        Optional<Predicate<Symbol>> predicate = this.getTypeFilterPredicate(node);
        if (predicate.isEmpty()) {
            // Added for safety to avoid any mis-identification or any future grammar changes 
            return completionItems;
        }
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, qNameRef, predicate.get());
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            List<Symbol> symbols = context.visibleSymbols(context.getCursorPosition()).stream()
                    .filter(predicate.get()).collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(symbols, context));
            completionItems.addAll(this.getModuleCompletionItems(context));
        }
        /*
        Sorting is done according to the type descriptor context since the completion items are a subset of the 
        completion items we suggest for any context where only the type descriptors are valid.
        Any changes to the list of completion items should be aware of the sorting as well.
         */
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context,
                     TypeReferenceNode node,
                     List<LSCompletionItem> completionItems) {
        for (LSCompletionItem lsCItem : completionItems) {
            String sortText = SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
            lsCItem.getCompletionItem().setSortText(sortText);
        }
    }

    private Optional<Predicate<Symbol>> getTypeFilterPredicate(TypeReferenceNode node) {
        Predicate<Symbol> predicate = null;

        NonTerminalNode parent = node.parent();
        if (parent.kind() == SyntaxKind.CLASS_DEFINITION || parent.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
            // Object type inclusion is handled.
            // Suggest only the objects and classes
            predicate = symbol -> symbol.kind() == SymbolKind.CLASS
                    || (symbol.kind() == SymbolKind.TYPE_DEFINITION
                    && CommonUtil.getRawType(((TypeDefinitionSymbol) symbol)
                    .typeDescriptor()).typeKind() == TypeDescKind.OBJECT);
        } else if (parent.kind() == SyntaxKind.RECORD_TYPE_DESC) {
            // Handles the record type inclusion
            predicate = symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION
                    && CommonUtil.getRawType(((TypeDefinitionSymbol) symbol)
                    .typeDescriptor()).typeKind() == TypeDescKind.RECORD;
        }
        
        // Added for safety to avoid any mis-identification or any future grammar changes
        return Optional.ofNullable(predicate);
    }
}
