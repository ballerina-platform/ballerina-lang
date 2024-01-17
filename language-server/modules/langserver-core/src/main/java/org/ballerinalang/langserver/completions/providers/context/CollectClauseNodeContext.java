/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.CollectClauseNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.QueryExpressionUtil;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link CollectClauseNode} context.
 *
 * @since 2201.7.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class CollectClauseNodeContext extends AbstractCompletionProvider<CollectClauseNode> {

    public CollectClauseNodeContext() {
        super(CollectClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, CollectClauseNode node) {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> exprEntries = QNameRefCompletionUtil.getExpressionContextEntries(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(exprEntries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
            List<FunctionSymbol> functionSymbols = QueryExpressionUtil.getLangLibMethods(context);
            functionSymbols.stream()
                    .filter(symbol -> symbol.typeDescriptor().restParam().isPresent())
                    .filter(symbol -> symbol.getName().isPresent() && !symbol.getName().get().contains("$"))
                    .filter(symbol -> completionItems
                            .addAll(populateBallerinaFunctionCompletionItems(symbol, context)))
                    .collect(Collectors.toList());
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }
}
