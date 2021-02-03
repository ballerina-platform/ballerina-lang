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
import io.ballerina.compiler.syntax.tree.ErrorTypeParamsNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.TypeCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ErrorTypeParamsNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ErrorTypeParamsNodeContext extends AbstractCompletionProvider<ErrorTypeParamsNode> {

    public ErrorTypeParamsNodeContext() {
        super(ErrorTypeParamsNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ErrorTypeParamsNode node) {
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
        if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            mappingTypes = QNameReferenceUtil.getModuleContent(context, (QualifiedNameReferenceNode) nodeAtCursor,
                    predicate);
            completionItems.addAll(this.getCompletionItemList(mappingTypes, context));
        } else {
            List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
            mappingTypes = visibleSymbols.stream().filter(predicate).collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(mappingTypes, context));
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.add(new TypeCompletionItem(context, null, Snippet.TYPE_MAP.get().build(context)));
        }
        this.sort(context, node, completionItems);
        
        return completionItems;
    }
}
