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
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link IncludedRecordParameterNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class IncludedRecordParameterNodeContext extends AbstractCompletionProvider<IncludedRecordParameterNode> {
    public IncludedRecordParameterNodeContext() {
        super(IncludedRecordParameterNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, IncludedRecordParameterNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION
                && CommonUtil.getRawType(((TypeDefinitionSymbol) symbol).typeDescriptor())
                .typeKind() == TypeDescKind.RECORD;
        List<Symbol> recordTypes;

        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode nameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            recordTypes = QNameRefCompletionUtil.getModuleContent(ctx, nameRef, predicate);
        } else {
            List<Symbol> visibleSymbols = ctx.visibleSymbols(ctx.getCursorPosition());
            recordTypes = visibleSymbols.stream()
                    .filter(symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION
                            && CommonUtil.getRawType(((TypeDefinitionSymbol) symbol).typeDescriptor())
                            .typeKind() == TypeDescKind.RECORD)
                    .collect(Collectors.toList());
            // Add the keywords and snippets related to record type descriptor
            completionItems.addAll(Arrays.asList(
                    new SnippetCompletionItem(ctx, Snippet.KW_RECORD.get()),
                    new SnippetCompletionItem(ctx, Snippet.DEF_RECORD_TYPE_DESC.get()),
                    new SnippetCompletionItem(ctx, Snippet.DEF_CLOSED_RECORD_TYPE_DESC.get())
            ));
            // Add the modules
            completionItems.addAll(this.getModuleCompletionItems(ctx));
        }
        completionItems.addAll(this.getCompletionItemList(recordTypes, ctx));
        this.sort(ctx, node, completionItems);

        return completionItems;
    }
}
