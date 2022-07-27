/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.util.ContextTypeResolver;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link ClientResourceAccessActionNode} context.
 *
 * @since 2201.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ClientResourceAccessActionNodeContext
        extends RightArrowActionNodeContext<ClientResourceAccessActionNode> {

    public ClientResourceAccessActionNodeContext() {
        super(ClientResourceAccessActionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ClientResourceAccessActionNode node) throws LSCompletionException {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        ContextTypeResolver resolver = new ContextTypeResolver(context);
        Optional<TypeSymbol> expressionType = node.expression().apply(resolver);

        if (expressionType.isEmpty() || !SymbolUtil.isClient(expressionType.get())) {
            return Collections.emptyList();
        }
        if (isInResourceMethodParameterContext(node, context)) {
            /*
             * Covers the following cases:
             * 1. a->/path/.post(<cursor>)
             * 2. a->/path/.post(mod1:<cursor>)
             */
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
                List<Symbol> exprEntries = QNameRefCompletionUtil.getExpressionContextEntries(context, qNameRef);
                List<LSCompletionItem> items = this.getCompletionItemList(exprEntries, context);
                completionItems.addAll(items);
            } else {
                completionItems.addAll(this.actionKWCompletions(context));
                completionItems.addAll(this.expressionCompletions(context));
                completionItems.addAll(this.getNamedArgExpressionCompletionItems(context, node));
            }
        } else {
            /*
            Covers the following case where a is a client object and we suggest the client resource access actions.
             a -> /path/p<cursor>
             */
            List<Symbol> clientActions = this.getClientActions(expressionType.get());
            completionItems.addAll(this.getCompletionItemList(clientActions, context));
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }

    /**
     * Checks if the cursor position is placed with in the resource method call parameter context.
     * @param node
     * @param context
     * @return
     */
    public static boolean isInResourceMethodParameterContext(ClientResourceAccessActionNode node,
                                                       PositionedOperationContext context) {
        Optional<ParenthesizedArgList> arguments = node.arguments();
        int cursor = context.getCursorPositionInTree();
        return arguments.isPresent() && arguments.get().openParenToken().textRange().startOffset() <= cursor
                && cursor <= arguments.get().closeParenToken().textRange().endOffset();

    }

    private List<LSCompletionItem> getNamedArgExpressionCompletionItems(BallerinaCompletionContext context,
                                                                        ClientResourceAccessActionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (node.arguments().isEmpty() || semanticModel.isEmpty()) {
            return completionItems;
        }
        Optional<Symbol> symbol = semanticModel.get().symbol(node);
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.METHOD) {
            return completionItems;
        }
        FunctionSymbol functionSymbol = (FunctionSymbol) symbol.get();
        return getNamedArgCompletionItems(context, functionSymbol,
                node.arguments().get().arguments());
    }

    @Override
    public void sort(BallerinaCompletionContext context, ClientResourceAccessActionNode node,
                     List<LSCompletionItem> completionItems) {
        if (isInResourceMethodParameterContext(node, context)) {
            super.sort(context, node, completionItems);
        }
    }
}
