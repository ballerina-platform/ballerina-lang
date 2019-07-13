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
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BTypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.common.utils.FunctionGenerator.generateTypeDefinition;

/**
 * Completion Item Resolver for the Definition Context.
 *
 * @since v0.982.0
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class FunctionDefinitionContextProvider extends LSCompletionProvider {

    public FunctionDefinitionContextProvider() {
        this.attachmentPoints.add(BallerinaParser.FunctionDefinitionContext.class);
    }

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
    public List<CompletionItem> getCompletions(LSContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        List<CommonToken> lhsDefaultTokens = context.get(CompletionKeys.LHS_TOKENS_KEY).stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        
        if (!lhsDefaultTokens.isEmpty() && lhsDefaultTokens.get(0).getType() == BallerinaParser.FUNCTION) {
            if (CommonUtil.getLastItem(lhsDefaultTokens).getType() == BallerinaParser.DOT
                    || (lhsDefaultTokens.size() > 3
                    && lhsDefaultTokens.get(lhsDefaultTokens.size() - 2).getType() == BallerinaParser.DOT)) {
                /*
                Consider the following case
                Eg: function x.
                    function x.y
                 */
                return this.getObjectAttachedFunctions(context, lhsDefaultTokens);
            }
            /*
            Consider the following cases
            Eg: function 
                function x
             */
            return context.get(CommonKeys.VISIBLE_SYMBOLS_KEY).stream()
                    .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BObjectTypeSymbol)
                    .map(symbolInfo -> {
                        BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                        String symbolName = symbol.getName().getValue();
                        CompletionItem item = BTypeCompletionItemBuilder.build((BTypeSymbol) symbol, symbolName);
                        item.setInsertText(symbolName + ".");
                        return item;
                    }).collect(Collectors.toList());
        }
        return completionItems;
    }

    private List<CompletionItem> getObjectAttachedFunctions(LSContext context, List<CommonToken> lhsDefaultTokens) {
        String objectName = lhsDefaultTokens.get(1).getText();
        List<CompletionItem> completionItems = new ArrayList<>();
        Optional<BObjectTypeSymbol> filtered = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY)
                .stream()
                .filter(symbolInfo -> {
                    BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                    return symbol instanceof BObjectTypeSymbol && symbol.getName().getValue().equals(objectName);
                }).map(symbolInfo -> (BObjectTypeSymbol) symbolInfo.getScopeEntry().symbol).findAny();

        if (!filtered.isPresent()) {
            return completionItems;
        }

        BObjectTypeSymbol objectType = filtered.get();

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
                    completionItems.add(BFunctionCompletionItemBuilder.build(attachedFunc.symbol, label,
                            insertText));
                });
        
        return completionItems;
    }
}
