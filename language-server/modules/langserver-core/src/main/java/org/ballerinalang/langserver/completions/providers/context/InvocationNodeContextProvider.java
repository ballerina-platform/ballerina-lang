/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.TypeResolverUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.NamedArgCompletionItem;
import org.ballerinalang.langserver.completions.builder.NamedArgCompletionItemBuilder;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic completion resolver for invocation nodes.
 *
 * @param <T> invocation node type.
 * @since 2.0.0
 */
public class InvocationNodeContextProvider<T extends Node> extends AbstractCompletionProvider<T> {

    public InvocationNodeContextProvider(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, T node)
            throws LSCompletionException {
        return Collections.emptyList();
    }

    @Override
    public void sort(BallerinaCompletionContext context, T node, List<LSCompletionItem> completionItems) {
        if ((node.kind() == SyntaxKind.EXPLICIT_NEW_EXPRESSION && 
                !TypeResolverUtil.isInNewExpressionParameterContext((ExplicitNewExpressionNode) node, 
                        context.getCursorPositionInTree())) 
                || (node.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION &&
                        !TypeResolverUtil.isInNewExpressionParameterContext(
                                (ImplicitNewExpressionNode) node, context.getCursorPositionInTree()))) {
            super.sort(context, node, completionItems);
            return;
        }

        Optional<TypeSymbol> parameterSymbol = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            parameterSymbol = context.currentSemanticModel().get().expectedType(context.currentDocument().get(),
                    LinePosition.from(context.getCursorPosition().getLine(),
                            context.getCursorPosition().getCharacter()));
        }

        for (LSCompletionItem completionItem : completionItems) {
            if (completionItem.getType() == LSCompletionItem.CompletionItemType.NAMED_ARG) {
                NamedArgCompletionItem argCompletionItem = (NamedArgCompletionItem) completionItem;
                Either<ParameterSymbol, RecordFieldSymbol> symbol = argCompletionItem.getParameterSymbol();
                String sortText;
                if (symbol.isRight()) {
                    RecordFieldSymbol right = symbol.getRight();
                    if (right.isOptional()) {
                        sortText = SortingUtil.genSortText(1) + SortingUtil.genSortText(3);
                    } else if (right.hasDefaultValue()) {
                        sortText = SortingUtil.genSortText(1) + SortingUtil.genSortText(2);
                    } else {
                        sortText = SortingUtil.genSortText(1) + SortingUtil.genSortText(1);
                    }
                } else {
                    sortText = SortingUtil.genSortText(1) +
                            SortingUtil.genSortText(SortingUtil.toRank(context, completionItem));
                }
                completionItem.getCompletionItem().setSortText(sortText);
            } else if (parameterSymbol.isEmpty()) {
                completionItem.getCompletionItem().setSortText(SortingUtil.genSortText(
                        SortingUtil.toRank(context, completionItem)));
            } else {
                completionItem.getCompletionItem().setSortText(
                        SortingUtil.genSortTextByAssignability(context, completionItem, parameterSymbol.get()));
            }
        }

    }

    protected List<LSCompletionItem> getNamedArgCompletionItems(BallerinaCompletionContext context,
                                                            FunctionSymbol functionSymbol,
                                                            SeparatedNodeList<FunctionArgumentNode> argumentNodeList) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (!isValidNamedArgContext(context, argumentNodeList)) {
            return completionItems;
        }
        FunctionTypeSymbol functionTypeSymbol = functionSymbol.typeDescriptor();
        Optional<List<ParameterSymbol>> params = functionTypeSymbol.params();
        if (params.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> existingNamedArgs = NameUtil.getDefinedArgumentNames(context, params.get(), argumentNodeList);
        for (ParameterSymbol parameterSymbol : params.get()) {
            if (parameterSymbol.paramKind() == ParameterKind.REQUIRED ||
                    parameterSymbol.paramKind() == ParameterKind.DEFAULTABLE) {
                Optional<String> paramName = parameterSymbol.getName();
                if (paramName.isEmpty() || paramName.get().isEmpty() || existingNamedArgs.contains(paramName.get())) {
                    continue;
                }
                CompletionItem completionItem = NamedArgCompletionItemBuilder.build(paramName.get(),
                        parameterSymbol.typeDescriptor());
                completionItems.add(
                        new NamedArgCompletionItem(context, completionItem, Either.forLeft(parameterSymbol)));
            } else if (parameterSymbol.paramKind() == ParameterKind.INCLUDED_RECORD) {
                TypeSymbol typeSymbol = CommonUtil.getRawType(parameterSymbol.typeDescriptor());
                if (typeSymbol.typeKind() != TypeDescKind.RECORD) {
                    // Impossible
                    continue;
                }
                RecordTypeSymbol includedRecordType = (RecordTypeSymbol) typeSymbol;
                Map<String, RecordFieldSymbol> fieldSymbolMap = includedRecordType.fieldDescriptors();
                fieldSymbolMap.forEach((key, value) -> {
                    Optional<String> fieldName = value.getName();
                    if (fieldName.isEmpty() || fieldName.get().isEmpty() ||
                            existingNamedArgs.contains(fieldName.get())) {
                        return;
                    }
                    TypeSymbol fieldType = value.typeDescriptor();
                    CompletionItem completionItem = NamedArgCompletionItemBuilder.build(fieldName.get(), fieldType);
                    completionItems.add(
                            new NamedArgCompletionItem(context, completionItem, Either.forRight(value)));
                });
            }
        }
        return completionItems;
    }


    /**
     * Check if the cursor is positioned in call expression context so that named arg
     * completions can be suggested.
     *
     * @param context          completion context.
     * @param argumentNodeList argument node list.
     * @return {@link Boolean} whether the cursor is positioned so that the named arguments can  be suggested.
     */
    private boolean isValidNamedArgContext(BallerinaCompletionContext context,
                                                 SeparatedNodeList<FunctionArgumentNode> argumentNodeList) {
        int cursorPosition = context.getCursorPositionInTree();
        for (Node child : argumentNodeList) {
            TextRange textRange = child.textRange();
            int startOffset = textRange.startOffset();
            if (startOffset > cursorPosition
                    && child.kind() == SyntaxKind.POSITIONAL_ARG || child.kind() == SyntaxKind.REST_ARG) {
                return false;
            }
        }
        return true;
    }
}
