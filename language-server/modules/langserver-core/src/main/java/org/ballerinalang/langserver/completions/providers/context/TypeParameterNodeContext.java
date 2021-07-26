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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.TypeCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion Provider for {@link TypeParameterNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TypeParameterNodeContext extends AbstractCompletionProvider<TypeParameterNode> {

    public TypeParameterNodeContext() {
        super(TypeParameterNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TypeParameterNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (node.parent().kind() == SyntaxKind.XML_TYPE_DESC) {
            completionItems.addAll(this.getXMLTypeDescSymbols(context, node));
        } else if (node.parent().kind() == SyntaxKind.ERROR_TYPE_DESC) {
            completionItems.addAll(this.getErrorTypeDescSymbols(context, node));
        } else {
            completionItems.addAll(this.getOtherTypeDescSymbols(context, node));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private Collection<? extends LSCompletionItem> getOtherTypeDescSymbols(BallerinaCompletionContext context,
                                                                           TypeParameterNode node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode refNode = (QualifiedNameReferenceNode) nodeAtCursor;
            /*
            Covers the following
            (1) [typedesc | map | future]<mod:*cursor*>
            (2) [typedesc | map | future]<mod:x*cursor*>
             */
            List<Symbol> moduleContent = QNameReferenceUtil.getTypesInModule(context, refNode);
            return this.getCompletionItemList(moduleContent, context);
        } else {
            /*
                Covers the following
                (1) [typedesc | map | future]<*cursor*>
                (2) [typedesc | map | future]<x*cursor*>
                 */
            return this.getTypeDescContextItems(context);
        }
    }

    private List<LSCompletionItem> getErrorTypeDescSymbols(BallerinaCompletionContext context, TypeParameterNode node) {
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
            completionItems.add(new TypeCompletionItem(context, null, Snippet.TYPE_MAP.get().build(context)));
        }

        return completionItems;
    }

    private List<LSCompletionItem> getXMLTypeDescSymbols(BallerinaCompletionContext context, TypeParameterNode node) {
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
    public boolean onPreValidation(BallerinaCompletionContext context, TypeParameterNode node) {
        int cursor = context.getCursorPositionInTree();
        int gtToken = node.gtToken().textRange().endOffset();
        int ltToken = node.ltToken().textRange().startOffset();

        return ltToken < cursor && gtToken > cursor;
    }
}
