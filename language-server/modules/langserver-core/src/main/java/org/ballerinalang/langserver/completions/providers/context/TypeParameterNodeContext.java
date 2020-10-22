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
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion Provider for {@link TypeParameterNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class TypeParameterNodeContext extends AbstractCompletionProvider<TypeParameterNode> {

    public TypeParameterNodeContext() {
        super(TypeParameterNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, TypeParameterNode node)
            throws LSCompletionException {
        List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode refNode = ((QualifiedNameReferenceNode) nodeAtCursor);
            List<Symbol> moduleContent;

            if (node.parent().kind() == SyntaxKind.XML_TYPE_DESC) {
                /*
                Covers the following
                (1) xml<mod:*cursor*>
                (2) xml<mod:x*cursor*>
                 */
                Predicate<Symbol> predicate = (symbol -> {
                    Optional<TypeDescKind> typeDescKind = SymbolUtil.getTypeKind(symbol);
                    return typeDescKind.isPresent() && typeDescKind.get() == TypeDescKind.XML;
                });
                moduleContent = QNameReferenceUtil.getModuleContent(context, refNode, predicate);
            } else {
                /*
                Covers the following
                (1) [typedesc | map]<mod:*cursor*>
                (2) [typedesc | map]<mod:x*cursor*>
                 */
                moduleContent = QNameReferenceUtil.getTypesInModule(context, refNode);
            }

            return this.getCompletionItemList(moduleContent, context);
        }

        List<LSCompletionItem> completionItems = new ArrayList<>(this.getModuleCompletionItems(context));

        if (node.parent().kind() == SyntaxKind.XML_TYPE_DESC) {
            /*
            Covers the following
            (1) xml<*cursor*>
            (2) xml<x*cursor*>
             */
            // modules and the xml sub types are suggested
            List<Symbol> xmlSubTypes = visibleSymbols.stream()
                    .filter(symbol -> {
                        Optional<TypeDescKind> typeDescKind = SymbolUtil.getTypeKind(symbol);
                        return typeDescKind.isPresent() && typeDescKind.get() == TypeDescKind.XML;
                    })
                    .collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(xmlSubTypes, context));
        } else {
            /*
            Covers the following
            (1) [typedesc | map | future]<*cursor*>
            (2) [typedesc | map | future]<x*cursor*>
             */
            completionItems.addAll(this.getTypeItems(context));
        }

        return completionItems;
    }

    @Override
    public boolean onPreValidation(LSContext context, TypeParameterNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        int gtToken = node.gtToken().textRange().endOffset();
        int ltToken = node.ltToken().textRange().startOffset();

        return ltToken < cursor && gtToken > cursor;
    }
}
