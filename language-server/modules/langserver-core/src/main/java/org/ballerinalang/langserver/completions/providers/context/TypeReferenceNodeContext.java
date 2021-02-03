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
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link TypeReferenceNode} context.
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
        Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.CLASS
                || (symbol.kind() == SymbolKind.TYPE || SymbolUtil.isObject(symbol));

        if (this.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, predicate);
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            List<Symbol> symbols = context.visibleSymbols(context.getCursorPosition()).stream()
                    .filter(predicate).collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(symbols, context));
            completionItems.addAll(this.getModuleCompletionItems(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }
}
