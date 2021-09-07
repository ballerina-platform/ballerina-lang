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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.NamedArgCompletionItem;
import org.ballerinalang.langserver.completions.builder.NamedArgCompletionItemBuilder;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Completion Provider for {@link FunctionCallExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class FunctionCallExpressionNodeContext extends InvocationNodeContextProvider<FunctionCallExpressionNode> {

    public FunctionCallExpressionNodeContext() {
        super(FunctionCallExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, FunctionCallExpressionNode node)
            throws LSCompletionException {
        if (QNameReferenceUtil.onQualifiedNameIdentifier(ctx, ctx.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) ctx.getNodeAtCursor();
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.actionKWCompletions(ctx));
        completionItems.addAll(this.expressionCompletions(ctx));
        if (!withInNamedArgAssignmentContext(ctx)) {
            completionItems.addAll(this.getNamedArgExpressionCompletionItems(ctx, node));
        }
        this.sort(ctx, node, completionItems);
        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, FunctionCallExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openParen = node.openParenToken();
        Token closeParen = node.closeParenToken();

        return cursor > openParen.textRange().startOffset() && cursor < closeParen.textRange().endOffset();
    }

    private List<LSCompletionItem> getNamedArgExpressionCompletionItems(BallerinaCompletionContext context,
                                                                        FunctionCallExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return completionItems;
        }
        Optional<Symbol> symbol = semanticModel.get().symbol(node);
        if (symbol.isEmpty() || !(symbol.get().kind() == SymbolKind.FUNCTION
                || symbol.get().kind() == SymbolKind.METHOD
                || symbol.get().kind() == SymbolKind.RESOURCE_METHOD)) {
            return completionItems;
        }
        FunctionSymbol functionSymbol = (FunctionSymbol) symbol.get();
        return getNamedArgCompletionItems(context, functionSymbol, node.arguments());
    }

    protected List<LSCompletionItem> getNamedArgCompletionItems(BallerinaCompletionContext context,
                                                            FunctionSymbol functionSymbol,
                                                            SeparatedNodeList<FunctionArgumentNode> argumentNodeList) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (!CommonUtil.isValidNamedArgContext(context, argumentNodeList)) {
            return completionItems;
        }

        FunctionTypeSymbol functionTypeSymbol = functionSymbol.typeDescriptor();
        Optional<List<ParameterSymbol>> params = functionTypeSymbol.params();
        if (params.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> existingNamedArgs = CommonUtil.getDefinedArgumentNames(context, params.get(), argumentNodeList);
        Predicate<ParameterSymbol> predicate = parameter -> parameter.paramKind() == ParameterKind.REQUIRED
                || parameter.paramKind() == ParameterKind.DEFAULTABLE;
        params.get().stream().filter(predicate).forEach(parameterSymbol -> {
            Optional<String> paramName = parameterSymbol.getName();
            TypeSymbol paramType = parameterSymbol.typeDescriptor();
            String defaultValue = CommonUtil.getDefaultValueForType(paramType).orElse("");
            if (paramName.isEmpty() || paramName.get().isEmpty() || existingNamedArgs.contains(paramName.get())) {
                return;
            }
            CompletionItem completionItem = NamedArgCompletionItemBuilder.build(paramName.get(), defaultValue);
            completionItems.add(new NamedArgCompletionItem(context, completionItem, Either.forLeft(parameterSymbol)));
        });
        return completionItems;
    }
}
