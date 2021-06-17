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
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;

/**
 * Handles the completions for {@link StartActionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class StartActionNodeContext extends AbstractCompletionProvider<StartActionNode> {

    public StartActionNodeContext() {
        super(StartActionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, StartActionNode node)
            throws LSCompletionException {

        List<LSCompletionItem> completionItems = new ArrayList<>();

        completionItems.addAll(this.expressionCompletions(context, node));
        this.sort(context, node, completionItems);
        return completionItems;
    }

    @Override
    protected List<LSCompletionItem> expressionCompletions(BallerinaCompletionContext context,
                                                           StartActionNode startActionNode) {

        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Supports the following
            (1) future<int> f = start module:<cursor>
            (2) future<int> f = start module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> ctxEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);

            return this.getCompletionItemList(ctxEntries, context);
        }
        /*
        Supports the following
        (1) future<int> f = start <cursor>
        (2) future<int> f = start a<cursor>
         */
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getTypeDescContextItems(context));
        
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> (symbol instanceof VariableSymbol || symbol.kind() == FUNCTION))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        return completionItems;
    }


    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, StartActionNode node) {
        TextRange startKWRange = node.startKeyword().textRange();
        int cursorPosition = context.getCursorPositionInTree();

        return startKWRange.endOffset() < cursorPosition;
    }

}
