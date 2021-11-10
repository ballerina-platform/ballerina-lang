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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.NamedArgCompletionItem;
import org.ballerinalang.langserver.completions.builder.NamedArgCompletionItemBuilder;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ContextTypeResolver;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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

        if (node.kind() == SyntaxKind.EXPLICIT_NEW_EXPRESSION
                && !CommonUtil.isInNewExpressionParameterContext(context, (ExplicitNewExpressionNode) node)) {
            super.sort(context, node, completionItems);
            return;
        } else if (node.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION &&
                !CommonUtil.isInNewExpressionParameterContext(context, (ImplicitNewExpressionNode) node)) {
            super.sort(context, node, completionItems);
            return;
        }
        ContextTypeResolver resolver = new ContextTypeResolver(context);
        Optional<TypeSymbol> parameterSymbol = node.apply(resolver);
        if (parameterSymbol.isEmpty()) {
            super.sort(context, node, completionItems);
            return;
        }
        for (
                LSCompletionItem completionItem : completionItems) {
            if (completionItem.getType() == LSCompletionItem.CompletionItemType.NAMED_ARG) {
                String sortText = SortingUtil.genSortText(1) +
                        SortingUtil.genSortText(SortingUtil.toRank(context, completionItem));
                completionItem.getCompletionItem().setSortText(sortText);
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
