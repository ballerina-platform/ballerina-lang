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
import org.ballerinalang.langserver.common.utils.completion.BPackageSymbolUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.ballerinalang.langserver.completions.util.sorters.MatchContextItemSorter;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Completion Item resolver for the match statement parser rule context.
 */
public class ParserRuleMatchStatementContextResolver extends AbstractItemResolver {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<String> poppedTokens = CommonUtil.popNFromStack(ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY), 3)
                .stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        List<SymbolInfo> symbolInfoList = ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);

        if (poppedTokens.contains(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)) {
            String pkgAlias = poppedTokens.get(poppedTokens.indexOf(UtilSymbolKeys.PKG_DELIMITER_KEYWORD) - 1);
            SymbolInfo pkgSymbolInfo = symbolInfoList.stream().filter(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                if (bSymbol instanceof BPackageSymbol) {
                    List<Name> nameComps = ((BPackageSymbol) bSymbol).pkgID.getNameComps();
                    return pkgAlias.equals(nameComps.get(nameComps.size() - 1).getValue());
                }
                return false;
            }).findFirst().orElse(null);

            if (pkgSymbolInfo != null && pkgSymbolInfo.getScopeEntry().symbol instanceof BPackageSymbol) {
                pkgSymbolInfo.getScopeEntry().symbol.scope.entries.forEach((name, scopeEntry) -> {
                    BSymbol scopeEntrySymbol = scopeEntry.symbol;
                    if (scopeEntrySymbol instanceof BInvokableSymbol) {
                        this.fillInvokableSymbolMatchSnippet((BInvokableSymbol) scopeEntrySymbol, completionItems, ctx);
                    }
                });
            }
        } else if (poppedTokens.contains(UtilSymbolKeys.DOT_SYMBOL_KEY)) {
            String variableName = poppedTokens.get(poppedTokens.indexOf(UtilSymbolKeys.DOT_SYMBOL_KEY) - 1);
            List<SymbolInfo> filteredSymbols =
                    CommonUtil.invocationsAndFieldsOnIdentifier(ctx, variableName, UtilSymbolKeys.DOT_SYMBOL_KEY)
                            .stream().filter(symbolInfo -> {
                BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                return symbol instanceof BInvokableSymbol || symbol instanceof BVarSymbol;
            }).collect(Collectors.toList());
            filteredSymbols.forEach(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                if (bSymbol instanceof BInvokableSymbol) {
                    this.fillInvokableSymbolMatchSnippet((BInvokableSymbol) bSymbol, completionItems, ctx);
                } else if (bSymbol instanceof BVarSymbol) {
                    this.fillVarSymbolMatchSnippet((BVarSymbol) bSymbol, completionItems, ctx);
                }
            });
        } else {
            List<SymbolInfo> filteredSymbolInfo = symbolInfoList.stream()
                    .filter(symbolInfo -> {
                        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                        return bSymbol instanceof BVarSymbol || bSymbol instanceof BPackageSymbol;
                    })
                    .collect(Collectors.toList());

            filteredSymbolInfo.forEach(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                if (bSymbol instanceof BInvokableSymbol) {
                    this.fillInvokableSymbolMatchSnippet((BInvokableSymbol) bSymbol, completionItems, ctx);
                } else if (bSymbol instanceof BVarSymbol) {
                    this.fillVarSymbolMatchSnippet((BVarSymbol) bSymbol, completionItems, ctx);
                    completionItems.add(this.populateVariableDefCompletionItem(symbolInfo));
                } else if (bSymbol instanceof BPackageSymbol
                        && !bSymbol.pkgID.name.getValue().equals("builtin")
                        && !bSymbol.pkgID.name.getValue().contains("runtime")) {
                    completionItems.add(BPackageSymbolUtil.getBTypeCompletionItem(symbolInfo.getSymbolName()));
                }
            });
        }
        ItemSorters.getSorterByClass(MatchContextItemSorter.class).sortItems(ctx, completionItems);

        return completionItems;
    }

    private String getMatchFieldsSnippet(BType bType, LSContext ctx) {
        final Set<BType> memberTypes = bType instanceof BUnionType ? ((BUnionType) bType).getMemberTypes() :
                new LinkedHashSet<>(Collections.singletonList(bType));
        StringBuilder fieldsSnippet = new StringBuilder("{");
        fieldsSnippet.append(LINE_SEPARATOR);

        memberTypes.forEach(type -> fieldsSnippet
                .append("\t").append(CommonUtil.getBTypeName(type, ctx)).append(" => {")
                .append(LINE_SEPARATOR)
                .append("\t\t")
                .append(LINE_SEPARATOR)
                .append("\t").append("}")
                .append(LINE_SEPARATOR));
        fieldsSnippet.append("}");
        
        return fieldsSnippet.toString();
    }

    private CompletionItem getFunctionCompletionItem(BInvokableSymbol func, String matchFieldSnippet) {
        CompletionItem completionItem = getFunctionCompletionItem(func);
        completionItem.setInsertText(completionItem.getInsertText() + " " + matchFieldSnippet);
        completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        
        return completionItem;
    }

    private CompletionItem getFunctionCompletionItem(BInvokableSymbol func) {
        CompletionItem completionItem = new CompletionItem();
        String functionSignature = this.getFunctionSignature(func);
        completionItem.setLabel(functionSignature);
        completionItem.setInsertText(completionItem.getLabel());
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        completionItem.setKind(CompletionItemKind.Function);
        
        return completionItem;
    }

    private CompletionItem getVariableCompletionItem(BVarSymbol varSymbol, String matchFieldSnippet) {
        CompletionItem completionItem = getVariableCompletionItem(varSymbol);
        completionItem.setInsertText(completionItem.getInsertText() + " " + matchFieldSnippet);
        completionItem.setInsertTextFormat(InsertTextFormat.Snippet);        
        
        return completionItem;
    }

    private CompletionItem getVariableCompletionItem(BVarSymbol varSymbol) {
        String typeName = varSymbol.type.toString();
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(varSymbol.getName().getValue());
        completionItem.setInsertText(varSymbol.getName().getValue());
        completionItem.setDetail((typeName.equals("")) ? ItemResolverConstants.NONE : typeName);
        completionItem.setKind(CompletionItemKind.Unit);

        return completionItem;
    }

    private String getFunctionSignature(BInvokableSymbol func) {
        String[] nameComps = func.getName().getValue().split("\\.");
        StringBuilder signature = new StringBuilder(nameComps[nameComps.length - 1]);
        List<String> params = new ArrayList<>();
        signature.append(UtilSymbolKeys.OPEN_BRACKET_KEY);
        func.getParameters().forEach(bVarSymbol -> params.add(bVarSymbol.getName().getValue()));
        func.getDefaultableParameters().forEach(bVarSymbol -> params.add(bVarSymbol.getName().getValue()));
        signature.append(String.join(",", params)).append(")");
        
        return signature.toString();
    }
    
    private void fillInvokableSymbolMatchSnippet(BInvokableSymbol bInvokableSymbol,
                                                  List<CompletionItem> completionItems, LSContext ctx) {
        BType returnType = bInvokableSymbol.getType().getReturnType();
        if (!(bInvokableSymbol instanceof BOperatorSymbol) && bInvokableSymbol.receiverSymbol == null
                && !bInvokableSymbol.getName().getValue().contains("<")) {
            CompletionItem completionItem = getFunctionCompletionItem(bInvokableSymbol,
                    this.getMatchFieldsSnippet(returnType, ctx));
            completionItems.add(completionItem);
        }
    }
    
    private void fillVarSymbolMatchSnippet(BVarSymbol varSymbol, List<CompletionItem> completionItems, LSContext ctx) {
        BType symbolType = varSymbol.getType();
        completionItems.add(getVariableCompletionItem(varSymbol, this.getMatchFieldsSnippet(symbolType, ctx)));
    }
}
