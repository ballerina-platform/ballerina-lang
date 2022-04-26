/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.debugadapter.completion;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugadapter.completion.context.CompletionContext;
import org.ballerinalang.debugadapter.completion.util.CommonUtil;
import org.eclipse.lsp4j.debug.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is being used to build function type completion item.
 *
 * @since 2201.1.0
 */
public final class FunctionCompletionItemBuilder {

    private static final String PARENTHESIS = "()";
    private static final String OPEN_PARENTHESIS = "(";
    private static final String CLOSE_PARENTHESIS = ")";

    private FunctionCompletionItemBuilder() {
    }

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSymbol
     * @param context        Debug completion Context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(FunctionSymbol functionSymbol, CompletionContext context) {
        CompletionItem item = new CompletionItem();
        if (functionSymbol != null && functionSymbol.getName().isPresent()) {
            String funcName = functionSymbol.getName().get();
            Pair<String, String> functionSignature = getFunctionInvocationSignature(functionSymbol, funcName, context);
            item.setText(functionSignature.getLeft());
            item.setLabel(functionSignature.getRight());
        }
        return item;
    }

    /**
     * Get the function invocation signature.
     *
     * @param functionSymbol Ballerina function instance
     * @param functionName   Function name
     * @param context        Debug completion Context
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    private static Pair<String, String> getFunctionInvocationSignature(FunctionSymbol functionSymbol,
                                                                       String functionName,
                                                                       CompletionContext context) {
        if (functionSymbol == null) {
            return ImmutablePair.of(functionName + PARENTHESIS, functionName + PARENTHESIS);
        }
        StringBuilder signature = new StringBuilder(functionName + OPEN_PARENTHESIS);
        StringBuilder insertText = new StringBuilder(functionName + OPEN_PARENTHESIS);
        List<String> funcArguments = getFuncArguments(functionSymbol, context);
        if (!funcArguments.isEmpty()) {
            signature.append(String.join(", ", funcArguments));
        }
        signature.append(CLOSE_PARENTHESIS);
        insertText.append(CLOSE_PARENTHESIS);

        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    /**
     * Get the list of function arguments from the invokable symbol.
     *
     * @param symbol  Invokable symbol to extract the arguments
     * @param context Debug completion Context
     * @return {@link List} List of arguments
     */
    private static List<String> getFuncArguments(FunctionSymbol symbol, CompletionContext context) {
        List<String> args = new ArrayList<>();
        boolean skipFirstParam = skipFirstParam(context, symbol);
        FunctionTypeSymbol functionTypeDesc = symbol.typeDescriptor();
        Optional<ParameterSymbol> restParam = functionTypeDesc.restParam();
        List<ParameterSymbol> parameterDefs = new ArrayList<>();
        if (functionTypeDesc.params().isPresent()) {
            parameterDefs.addAll(functionTypeDesc.params().get());
        }
        for (int i = 0; i < parameterDefs.size(); i++) {
            if (i == 0 && skipFirstParam) {
                continue;
            }
            ParameterSymbol param = parameterDefs.get(i);
            if (param.typeDescriptor().typeKind() == TypeDescKind.COMPILATION_ERROR) {
                // Invalid parameters are ignored, but empty string is used to indicate there's a parameter
                args.add("");
            } else {
                args.add(CommonUtil.getModifiedTypeName(context, param.typeDescriptor()) + (param.getName().isEmpty() ?
                        "" : " " + param.getName().get()));
            }
        }
        restParam.ifPresent(param -> {
            // Rest param is represented as an array type symbol
            ArrayTypeSymbol typeSymbol = (ArrayTypeSymbol) param.typeDescriptor();
            args.add(CommonUtil.getModifiedTypeName(context, typeSymbol.memberTypeDescriptor())
                    + (param.getName().isEmpty() ? "" : "... "
                    + param.getName().get()));
        });
        return (!args.isEmpty()) ? args : new ArrayList<>();
    }

    /**
     * Whether we skip the first parameter being included as a label in the signature.
     * When showing a lang lib invokable symbol over DOT(invocation) we do not show the first param, but when we
     * showing the invocation over package of the langlib with the COLON we show the first param.
     * <p>
     * When the langlib function is retrieved from the Semantic API, those functions are filtered where the first param
     * type not being same as the langlib type. Hence we need to chek whether the function is from a langlib.
     *
     * @param context        Debug completion Context
     * @param functionSymbol Invokable symbol
     * @return {@link Boolean} whether we show the first param or not
     */
    private static boolean skipFirstParam(CompletionContext context, FunctionSymbol functionSymbol) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        return CommonUtil.isLangLib(functionSymbol.getModule().get().id())
                && nodeAtCursor.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE;
    }
}
