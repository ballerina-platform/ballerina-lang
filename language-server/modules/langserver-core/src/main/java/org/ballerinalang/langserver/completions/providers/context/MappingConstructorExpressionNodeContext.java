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

import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ComputedNameFieldNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SpreadCompletionItem;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link MappingConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class MappingConstructorExpressionNodeContext extends
        MappingContextProvider<MappingConstructorExpressionNode> {

    public MappingConstructorExpressionNodeContext() {
        super(MappingConstructorExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 MappingConstructorExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        Optional<Node> evalNode = CommonUtil.getMappingContextEvalNode(nodeAtCursor);
        if (evalNode.isEmpty()) {
            return completionItems;
        }

        Scope scope = Scope.OTHER;
        if (this.withinValueExpression(context, evalNode.get())) {
            scope = Scope.VALUE_EXPR;
            completionItems.addAll(getCompletionsInValueExpressionContext(context));
        } else if (this.withinComputedNameContext(context, evalNode.get())) {
            scope = Scope.COMPUTED_FIELD_NAME;
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                completionItems.addAll(this.getExpressionsCompletionsForQNameRef(context, qNameRef));
            } else {
                completionItems.addAll(this.getComputedNameCompletions(context));
            }
        } else {
            scope = Scope.FIELD_NAME;
            completionItems.addAll(this.getFieldCompletionItems(context, node, evalNode.get()));
        }
        this.sort(context, node, completionItems, scope);
        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, MappingConstructorExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        return !node.openBrace().isMissing() && !node.closeBrace().isMissing()
                && cursor > node.openBrace().textRange().startOffset()
                && cursor < node.closeBrace().textRange().endOffset();
    }

    @Override
    public void sort(BallerinaCompletionContext context, MappingConstructorExpressionNode node,
                     List<LSCompletionItem> completionItems, Object... metaData) {
        final Scope scope;
        if (metaData.length > 0 && metaData[0] instanceof Scope) {
            scope = (Scope) metaData[0];
        } else {
            scope = Scope.OTHER;
        }
        Optional<TypeSymbol> contextType = context.getContextType();
        if (contextType.isEmpty()) {
            super.sort(context, node, completionItems);
            return;
        }
        completionItems.forEach(lsCItem -> {
            // In the field name context, we have to give a special consideration to the map type variables
            // suggested with the spread operator (...map1).
            if (scope == Scope.FIELD_NAME && lsCItem.getType() == LSCompletionItem.CompletionItemType.SPREAD) {
                Optional<Symbol> expression = ((SpreadCompletionItem) lsCItem).getExpression();

                Optional<TypeSymbol> mapTypeParam = expression
                        .flatMap(SymbolUtil::getTypeDescriptor)
                        .filter(typeDesc -> typeDesc.typeKind() == TypeDescKind.MAP)
                        .map(typeDesc -> (MapTypeSymbol) typeDesc)
                        .map(MapTypeSymbol::typeParam);

                // If the completion item is a map type variable and is the spread operator, we give it priority
                if ((mapTypeParam.isPresent() && mapTypeParam.get().subtypeOf(contextType.get()))
                        || expression.isPresent()) {
                    int lastRank = expression.map(expr -> expr.kind() == SymbolKind.FUNCTION ? 4 : 3)
                            .orElse(3);
                    String sortText = SortingUtil.genSortText(1) + SortingUtil.genSortText(lastRank);
                    lsCItem.getCompletionItem().setSortText(sortText);
                    return;
                }
            }
            String sortText = SortingUtil.genSortTextByAssignability(context, lsCItem, contextType.get());
            lsCItem.getCompletionItem().setSortText(sortText);
        });
    }

    private boolean withinComputedNameContext(BallerinaCompletionContext context, Node evalNodeAtCursor) {
        if (evalNodeAtCursor.kind() != SyntaxKind.COMPUTED_NAME_FIELD) {
            return false;
        }

        int openBracketEnd = ((ComputedNameFieldNode) evalNodeAtCursor).openBracket().textRange().endOffset();
        int closeBracketStart = ((ComputedNameFieldNode) evalNodeAtCursor).closeBracket().textRange().startOffset();
        int cursorPosInTree = context.getCursorPositionInTree();

        return cursorPosInTree >= openBracketEnd && cursorPosInTree <= closeBracketStart;
    }

    private List<LSCompletionItem> getComputedNameCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());

        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> symbol instanceof VariableSymbol || symbol.kind() == SymbolKind.FUNCTION)
                .toList();
        List<LSCompletionItem> completionItems = this.getCompletionItemList(filteredList, context);
        completionItems.addAll(this.getModuleCompletionItems(context));

        return completionItems;
    }

    @Override
    protected List<String> getFields(MappingConstructorExpressionNode node) {
        return node.fields().stream()
                .filter(field -> !field.isMissing() && field.kind() == SyntaxKind.SPECIFIC_FIELD
                        && ((SpecificFieldNode) field).fieldName().kind() == SyntaxKind.IDENTIFIER_TOKEN)
                .map(field -> ((IdentifierToken) ((SpecificFieldNode) field).fieldName()).text())
                .toList();
    }

    private enum Scope {
        VALUE_EXPR,
        FIELD_NAME,
        COMPUTED_FIELD_NAME,
        OTHER
    }
}
