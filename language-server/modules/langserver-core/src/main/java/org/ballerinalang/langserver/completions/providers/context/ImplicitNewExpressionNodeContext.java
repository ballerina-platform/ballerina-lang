/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ImplicitNewExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ImplicitNewExpressionNodeContext extends InvocationNodeContextProvider<ImplicitNewExpressionNode> {

    public ImplicitNewExpressionNodeContext() {
        super(ImplicitNewExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ImplicitNewExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.withinArgs(context, node)) {
            /*
            Covers
            lhs = new(<cursor>)
             */
            completionItems.addAll(this.getCompletionsWithinArgs(context, node));
        } else {
            /*
            Supports the following
            (1) lhs = new <cursor>
            */
            List<Symbol> filteredSymbols = context.visibleSymbols(context.getCursorPosition()).stream()
                    .filter(this.getSymbolFilterPredicate(node))
                    .collect(Collectors.toList());
            filteredSymbols.forEach(symbol -> {
                Optional<LSCompletionItem> cItem = this.getExplicitNewCompletionItem(symbol, context);
                cItem.ifPresent(completionItems::add);
            });
            completionItems.addAll(this.getModuleCompletionItems(context));
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }

    private Predicate<Symbol> getSymbolFilterPredicate(Node node) {
        if (node.parent().kind() == SyntaxKind.SERVICE_DECLARATION
                || node.parent().kind() == SyntaxKind.LISTENER_DECLARATION) {
            return symbol -> symbol.kind() == SymbolKind.CLASS && SymbolUtil.isListener(symbol);
        }

        return symbol -> symbol.kind() == SymbolKind.CLASS
                || (symbol.kind() == SymbolKind.TYPE_DEFINITION
                && ((TypeDefinitionSymbol) symbol).typeDescriptor().typeKind() == TypeDescKind.STREAM);
    }

    private boolean withinArgs(BallerinaCompletionContext context, ImplicitNewExpressionNode node) {
        if (node.parenthesizedArgList().isEmpty()) {
            return false;
        }
        ParenthesizedArgList parenthesizedArgList = node.parenthesizedArgList().get();
        int cursor = context.getCursorPositionInTree();

        return cursor > parenthesizedArgList.openParenToken().textRange().startOffset()
                && cursor < parenthesizedArgList.closeParenToken().textRange().endOffset();
    }

    private List<LSCompletionItem> getCompletionsWithinArgs(BallerinaCompletionContext ctx,
                                                            ImplicitNewExpressionNode node) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameRefCompletionUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.expressionCompletions(ctx));
        completionItems.addAll(getNamedArgExpressionCompletionItems(ctx, node));

        return completionItems;
    }

    private List<LSCompletionItem> getNamedArgExpressionCompletionItems(BallerinaCompletionContext context,
                                                                        ImplicitNewExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<TypeSymbol> type = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            LinePosition linePosition = node.parent().location().lineRange().startLine();
            type = context.currentSemanticModel().get().expectedType(context.currentDocument().get(), linePosition);
        }

        if (type.isEmpty()) {
            return completionItems;
        }
        TypeSymbol typeSymbol = CommonUtil.getRawType(type.get());
        if (typeSymbol.kind() != SymbolKind.CLASS) {
            return completionItems;
        }
        Optional<MethodSymbol> methodSymbol = ((ClassSymbol) typeSymbol).initMethod();
        if (methodSymbol.isEmpty() || node.parenthesizedArgList().isEmpty()) {
            return completionItems;
        }
        completionItems.addAll(getNamedArgCompletionItems(context, methodSymbol.get(),
                node.parenthesizedArgList().get().arguments()));
        return completionItems;
    }
}
