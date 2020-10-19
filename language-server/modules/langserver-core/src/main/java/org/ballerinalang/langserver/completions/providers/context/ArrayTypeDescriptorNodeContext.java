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
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ArrayTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ArrayTypeDescriptorNodeContext extends AbstractCompletionProvider<ArrayTypeDescriptorNode> {

    public ArrayTypeDescriptorNodeContext() {
        super(ArrayTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ArrayTypeDescriptorNode node) {
        List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        Optional<Node> arrayLength = node.arrayLength();

        if (arrayLength.isPresent() && this.onQualifiedNameIdentifier(context, arrayLength.get())) {
            QualifiedNameReferenceNode qName = (QualifiedNameReferenceNode) arrayLength.get();
            List<Symbol> moduleConstants = QNameReferenceUtil.getModuleContent(context, qName, constantFilter());

            return this.getCompletionItemList(moduleConstants, context);
        }

        List<Symbol> constants = visibleSymbols.stream()
                .filter(constantFilter())
                .collect(Collectors.toList());
        List<LSCompletionItem> completionItems = this.getModuleCompletionItems(context);
        completionItems.addAll(this.getCompletionItemList(constants, context));

        return completionItems;
    }

    private Predicate<Symbol> constantFilter() {
        return symbol -> {
            Optional<? extends BallerinaTypeDescriptor> typeDescriptor = SymbolUtil.getTypeDescriptor(symbol);
            typeDescriptor = typeDescriptor.isPresent() && typeDescriptor.get().kind() == TypeDescKind.TYPE_REFERENCE ?
                    Optional.ofNullable(((TypeReferenceTypeDescriptor) typeDescriptor.get()).typeDescriptor())
                    : typeDescriptor;
            return symbol.kind() == SymbolKind.CONSTANT
                    && typeDescriptor.isPresent()
                    && typeDescriptor.get().kind() == TypeDescKind.INT;
        };
    }
}
