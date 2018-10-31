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
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Completion item resolver for service definition context.
 */
public class ParserRuleServiceDefinitionContextResolver extends AbstractItemResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(ParserRuleServiceDefinitionContextResolver.class);
    
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        List<CompletionItem> completionItems = new ArrayList<>();
        TokenStream tokenStream = ctx.get(CompletionKeys.TOKEN_STREAM_KEY);
        Stack<Token> poppedTokens = ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY);
        Stack<String> fieldsStack = new Stack<>();
        int startIndex = poppedTokens.peek().getTokenIndex() + 1;
        boolean bindKeywordFound = false;
        boolean isWithinRecordLiteral = false;
        String serviceType = "";
        String serviceTypePkg = "";
        int skipCount = 0;
        
        // Backtrack the tokens from the head of the popped tokens in order determine the cursor position
        tokenScanner:
        while (true) {
            Token token = CommonUtil.getPreviousDefaultToken(tokenStream, startIndex);
            String tokenString = token.getText();
            switch (tokenString) {
                case ItemResolverConstants.SERVICE:
                    break tokenScanner;
                case ItemResolverConstants.BIND:
                    bindKeywordFound = true;
                    break;
                case UtilSymbolKeys.OPEN_BRACE_KEY:
                    isWithinRecordLiteral = true;
                    Token tokenBefore = CommonUtil.getPreviousDefaultToken(tokenStream, token.getTokenIndex());
                    if (tokenBefore.getText().equals(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)) {
                        Token fieldToken = CommonUtil.getPreviousDefaultToken(tokenStream, tokenBefore.getTokenIndex());
                        if (skipCount == 0) {
                            fieldsStack.push(fieldToken.getText());
                        } else {
                            skipCount--;
                        }
                        startIndex = fieldToken.getTokenIndex();
                    } else {
                        startIndex = token.getTokenIndex();
                    }
                    continue tokenScanner;
                case UtilSymbolKeys.CLOSE_BRACE_KEY:
                    skipCount++;
                    break;
                case UtilSymbolKeys.GT_SYMBOL_KEY:
                    // Find the Service Type and the service type package
                    Token serviceTypeToken = CommonUtil.getPreviousDefaultToken(tokenStream, token.getTokenIndex());
                    serviceType = serviceTypeToken.getText();
                    Token pkgDelim = CommonUtil.getPreviousDefaultToken(tokenStream, serviceTypeToken.getTokenIndex());
                    if (pkgDelim.getText().equals(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)) {
                        serviceTypePkg = CommonUtil.getPreviousDefaultToken(tokenStream, pkgDelim.getTokenIndex())
                                .getText();
                    }
                    // After matching the service type, there is no need to keep scanning the tokens.
                    // Hence break the loop
                    break tokenScanner;
                default:
                    break;
            }

            startIndex = token.getTokenIndex();
        }

        if (bindKeywordFound) {
            if (!serviceType.isEmpty()) {
                BType listenerType = getServiceTypeObject(serviceTypePkg, serviceType, ctx);
                if (isWithinRecordLiteral) {
                    // an endpoint listener is defined inline from the service types.
                    // Filter the fields from the listener types.
                    BRecordType listenerEndpointConfigRecord = getListenerEndpointConfigRecord(listenerType);
                    if (listenerEndpointConfigRecord == null) {
                        logger.error("Invalid listener type found");
                    } else {
                        completionItems.addAll(getFieldCompletionItems(listenerEndpointConfigRecord, fieldsStack));
                    }
                } else {
                    // Suggest the defined listeners of service type
                    List<SymbolInfo> endpointSymbols = ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                            .stream()
                            .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BEndpointVarSymbol
                                    && symbolInfo.getScopeEntry().symbol.type == listenerType)
                            .collect(Collectors.toList());
                    completionItems.addAll(this.getCompletionItemList(endpointSymbols));
                }
            } else {
                // suggest all the visible, defined endpoints
                List<SymbolInfo> endpointSymbols = ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                        .stream()
                        .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BEndpointVarSymbol)
                        .collect(Collectors.toList());
                completionItems.addAll(this.getCompletionItemList(endpointSymbols));
            }
        } else {
            boolean isSnippet = ctx.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem().getSnippetSupport();
            // Fill the bind keyword completion item
            CompletionItem bindItem = Snippet.KW_BIND.get().build(new CompletionItem(), isSnippet);
            completionItems.add(bindItem);
        }
        
        return completionItems;
    }
    
    private static BType getServiceTypeObject(String pkgName, String serviceType, LSContext ctx) {
        List<SymbolInfo> visibleSymbols = ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        BType listenerType = null;

        for (SymbolInfo visibleSymbol : visibleSymbols) {
            BSymbol symbol = visibleSymbol.getScopeEntry().symbol;
            if (symbol instanceof BPackageSymbol
                    && pkgName.equals(((BPackageSymbol) symbol).pkgID.getName().getValue())) {

                Scope.ScopeEntry scopeEntry = ((BPackageSymbol) symbol).scope.entries.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().symbol instanceof BObjectTypeSymbol
                                && entry.getValue().symbol.getName().getValue().equals(serviceType))
                        .findFirst().get().getValue();

                if (scopeEntry.symbol instanceof BObjectTypeSymbol) {
                    BAttachedFunction getEPFunction = ((BObjectTypeSymbol) scopeEntry.symbol).attachedFuncs
                            .stream()
                            .filter(function -> function.funcName.getValue().equals("getEndpoint"))
                            .findFirst().get();
                    listenerType = getEPFunction.symbol.type.getReturnType();
                    break;
                }
            }
        }
        return listenerType;
    }
    
    private BRecordType getListenerEndpointConfigRecord(BType listenerType) {
        if (listenerType.tsymbol instanceof BObjectTypeSymbol) {
            BAttachedFunction getActionsFUnction = ((BObjectTypeSymbol) listenerType.tsymbol).attachedFuncs
                    .stream()
                    .filter(function -> function.funcName.getValue().equals("getCallerActions"))
                    .findFirst().get();
            if (!(getActionsFUnction.type.retType instanceof BObjectType)) {
                return null;
            }
            BField configField = ((BObjectType) getActionsFUnction.type.retType).fields
                    .stream()
                    .filter(bField -> bField.getName().getValue().equals("config"))
                    .findFirst().get();
            if (configField.type instanceof BRecordType) {
                return (BRecordType) configField.type;
            }
        }
        return null;
    }

    private static List<CompletionItem> getFieldCompletionItems(BRecordType recordType, Stack<String> fieldStack) {
        List<CompletionItem> completionItems = new ArrayList<>();
        if (fieldStack.isEmpty()) {
            completionItems.addAll(CommonUtil.getStructFieldCompletionItems(recordType.fields));
            completionItems.add(CommonUtil.getFillAllStructFieldsItem(recordType.fields));
            return completionItems;
        } else {
            String fieldName = fieldStack.pop();
            BField field = recordType.fields.stream()
                    .filter(bField -> bField.getName().getValue().equals(fieldName))
                    .findFirst().get();
            if (field.type instanceof BRecordType) {
                return getFieldCompletionItems((BRecordType) field.type, fieldStack);
            }
            // In the ideal case, empty list should not be returned
            return new ArrayList<>();
        }
    }
}
