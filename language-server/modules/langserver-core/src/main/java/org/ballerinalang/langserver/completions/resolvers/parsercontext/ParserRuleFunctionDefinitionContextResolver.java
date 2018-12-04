/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.common.utils.CommonUtil.FunctionGenerator.generateTypeDefinition;

/**
 * Completion Item Resolver for the Definition Context.
 *
 * @since v0.982.0
 */
public class ParserRuleFunctionDefinitionContextResolver extends AbstractItemResolver {
    private static List<String> getFuncArguments(BInvokableSymbol bInvokableSymbol) {
        List<String> list = new ArrayList<>();
        if (bInvokableSymbol.type instanceof BInvokableType) {
            BInvokableType bInvokableType = (BInvokableType) bInvokableSymbol.type;
            List<BType> paramTypes = bInvokableType.getParameterTypes();
            List<BVarSymbol> params = bInvokableSymbol.getParameters();
            if (bInvokableType.paramTypes.isEmpty()) {
                return list;
            }
            for (int i = 0; i < params.size(); i++) {
                String argName = params.get(i).name.getValue();
                String argType = generateTypeDefinition(null, bInvokableSymbol.pkgID, paramTypes.get(i));
                list.add(argType + " " + argName);
            }
        }
        return (!list.isEmpty()) ? list : new ArrayList<>();
    }

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        List<String> consumedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY).stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        if (!consumedTokens.get(0).equals(UtilSymbolKeys.FUNCTION_KEYWORD_KEY)
                || !CommonUtil.getLastItem(consumedTokens).equals(".")) {
            return completionItems;
        }

        String objectName = consumedTokens.get(1);
        Optional filtered = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                .stream()
                .filter(symbolInfo -> {
                    BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                    return symbol instanceof BObjectTypeSymbol && symbol.getName().getValue().equals(objectName);
                }).findFirst();

        if (!(filtered.isPresent()
                && ((SymbolInfo) filtered.get()).getScopeEntry().symbol instanceof BObjectTypeSymbol)) {
            return completionItems;
        }

        BObjectTypeSymbol objectType = (BObjectTypeSymbol) (((SymbolInfo) filtered.get()).getScopeEntry().symbol);

        objectType.attachedFuncs.stream()
                .filter(attachedFunc -> !attachedFunc.symbol.bodyExist)
                .forEach(attachedFunc -> {
                    String functionName = attachedFunc.funcName.getValue();
                    List<String> funcArguments = getFuncArguments(attachedFunc.symbol);
                    String label = functionName + "(" + String.join(", ", funcArguments) + ")";
                    if (!(attachedFunc.symbol.retType instanceof BNilType)) {
                        label += " returns " + generateTypeDefinition(null, objectType.pkgID,
                                                                      attachedFunc.symbol.retType);
                    }
                    String insertText = label + " {" + CommonUtil.LINE_SEPARATOR + "\t${1}"
                            + CommonUtil.LINE_SEPARATOR + "}";
                    completionItems.add(BFunctionCompletionItemBuilder.build(attachedFunc.symbol, label, insertText));
                });
        return completionItems;
    }
}
