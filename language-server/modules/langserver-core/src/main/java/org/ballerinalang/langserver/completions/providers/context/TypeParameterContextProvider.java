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

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic completion resolver for the Type Parameter nodes.
 *
 * @param <T> type parameter node type
 * @since 2.0.0
 */
public class TypeParameterContextProvider<T extends Node> extends AbstractCompletionProvider<T> {

    public TypeParameterContextProvider(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, T node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (node.parent().kind() == SyntaxKind.XML_TYPE_DESC) {
            completionItems.addAll(this.getXMLTypeDescSymbols(context, node));
        } else if (node.parent().kind() == SyntaxKind.ERROR_TYPE_DESC) {
            completionItems.addAll(this.getErrorTypeDescSymbols(context, node));
        } else if (node.parent().kind() == SyntaxKind.TABLE_TYPE_DESC) {
            completionItems.addAll(this.getTableTypeDescSymbols(context, node));
        } else {
            completionItems.addAll(this.getOtherTypeDescSymbols(context, node));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private Collection<? extends LSCompletionItem> getOtherTypeDescSymbols(BallerinaCompletionContext context, T node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode refNode = (QualifiedNameReferenceNode) nodeAtCursor;
            /*
            Covers the following
            (1) [typedesc | map | future]<mod:*cursor*>
            (2) [typedesc | map | future]<mod:x*cursor*>
            (3) table<R1> key<mod:*cursor*>
             */
            List<Symbol> moduleContent = QNameReferenceUtil.getTypesInModule(context, refNode);
            return this.getCompletionItemList(moduleContent, context);
        } else {
            /*
                Covers the following
                (1) [typedesc | map | future]<*cursor*>
                (2) [typedesc | map | future]<x*cursor*>
                (3) table<R1> key<*cursor*>
                 */
            return this.getTypeDescContextItems(context);
        }
    }

    private List<LSCompletionItem> getTableTypeDescSymbols(BallerinaCompletionContext context, T node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        Predicate<Symbol> predicate = symbol -> {
            if (symbol.kind() != SymbolKind.TYPE_DEFINITION) {
                return false;
            }
            TypeDescKind rawType = CommonUtil.getRawType(((TypeDefinitionSymbol) symbol).typeDescriptor()).typeKind();
            return rawType == TypeDescKind.MAP || rawType == TypeDescKind.RECORD;
        };

        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode refNode = (QualifiedNameReferenceNode) nodeAtCursor;
            /*
            Covers the following
            (1) table<mod:*cursor*>
            (2) table<mod:x*cursor*>
             */
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, refNode, predicate);
            return this.getCompletionItemList(moduleContent, context);
        } else {
            /*
            Covers the following
            (1) table<*cursor*>
            (2) table<x*cursor*>
             */
            List<Symbol> filtered = context.visibleSymbols(context.getCursorPosition()).stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
            List<LSCompletionItem> completionItems = new ArrayList<>();
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.addAll(this.getCompletionItemList(filtered, context));

            return completionItems;
        }
    }

    private List<LSCompletionItem> getErrorTypeDescSymbols(BallerinaCompletionContext context, T node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        /*
        Covers the following cases
        (1) error< <cursor> >
        (2) error< t<cursor> >
        (3) error< module:<cursor> >
        (4) error< module:t<cursor> >
         */
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        Predicate<Symbol> predicate = symbol -> {
            if (symbol.kind() != SymbolKind.TYPE_DEFINITION) {
                return false;
            }
            TypeSymbol typeDesc = ((TypeDefinitionSymbol) symbol).typeDescriptor();
            return (CommonUtil.getRawType(typeDesc).typeKind() == TypeDescKind.MAP
                    || CommonUtil.getRawType(typeDesc).typeKind() == TypeDescKind.RECORD);
        };
        List<Symbol> mappingTypes;
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            mappingTypes = QNameReferenceUtil.getModuleContent(context, (QualifiedNameReferenceNode) nodeAtCursor,
                    predicate);
            completionItems.addAll(this.getCompletionItemList(mappingTypes, context));
        } else {
            completionItems.addAll(
                    Arrays.asList(new SnippetCompletionItem(context, Snippet.DEF_RECORD_TYPE_DESC.get()),
                            new SnippetCompletionItem(context, Snippet.DEF_CLOSED_RECORD_TYPE_DESC.get())));
            List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
            mappingTypes = visibleSymbols.stream().filter(predicate).collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(mappingTypes, context));
            completionItems.addAll(this.getModuleCompletionItems(context));
        }

        return completionItems;
    }

    private List<LSCompletionItem> getXMLTypeDescSymbols(BallerinaCompletionContext context, T node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<LSCompletionItem> completionItems = new ArrayList<>();

        Predicate<Symbol> predicate = (symbol -> {
            if (symbol.kind() != SymbolKind.TYPE_DEFINITION) {
                return false;
            }
            Optional<? extends TypeSymbol> typeDescriptor = SymbolUtil.getTypeDescriptor(symbol);
            return typeDescriptor.isPresent() && typeDescriptor.get().typeKind().isXMLType();
        });

        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode refNode = (QualifiedNameReferenceNode) nodeAtCursor;
            /*
            Covers the following
            (1) xml<mod:*cursor*>
            (2) xml<mod:x*cursor*>
             */
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, refNode, predicate);
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            /*
            Covers the following
            (1) xml<*cursor*>
            (2) xml<x*cursor*>
             */
            // modules and the xml sub types are suggested
            List<Symbol> filtered = visibleSymbols.stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(filtered, context));
            completionItems.addAll(this.getModuleCompletionItems(context));
        }

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, T node, List<LSCompletionItem> completionItems) {
        if (node.parent().kind() == SyntaxKind.KEY_TYPE_CONSTRAINT
                && node.parent().parent().kind() == SyntaxKind.TABLE_TYPE_DESC) {
            TableTypeDescriptorNode tableTypeDesc = (TableTypeDescriptorNode) node.parent().parent();
            TypeParameterNode typeParameterNode = (TypeParameterNode) tableTypeDesc.rowTypeParameterNode();

            // Get type of type parameter
            Optional<TypeSymbol> typeSymbol = context.currentSemanticModel()
                    .flatMap(semanticModel -> semanticModel.symbol(typeParameterNode.typeNode()))
                    .flatMap(SymbolUtil::getTypeDescriptor);
            if (typeSymbol.isPresent() && CommonUtil.getRawType(typeSymbol.get()).typeKind() == TypeDescKind.RECORD) {
                RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) CommonUtil.getRawType(typeSymbol.get());
                Set<String> typeNames = recordTypeSymbol.fieldDescriptors().values().stream()
                        .filter(recordFieldSymbol -> recordFieldSymbol.qualifiers().contains(Qualifier.READONLY))
                        .map(recordFieldSymbol -> recordFieldSymbol.typeDescriptor().getName().orElse(
                                recordFieldSymbol.typeDescriptor().typeKind().getName()))
                        .filter(name -> !name.isEmpty()).collect(Collectors.toSet());

                completionItems.forEach(lsCItem -> {
                    String sortText;
                    if (typeNames.contains(lsCItem.getCompletionItem().getInsertText())) {
                        sortText = SortingUtil.genSortText(1) +
                                SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
                    } else {
                        sortText = SortingUtil.genSortText(2) +
                                SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
                    }
                    lsCItem.getCompletionItem().setSortText(sortText);
                });
                return;
            }
        }
        completionItems.forEach(lsCItem -> {
            String sortText = SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
            lsCItem.getCompletionItem().setSortText(sortText);
        });
    }
}
