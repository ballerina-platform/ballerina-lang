/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.filters.PackageActionFunctionAndTypesFilter;
import org.ballerinalang.langserver.completions.util.filters.StatementTemplateFilter;
import org.ballerinalang.langserver.completions.util.sorters.CompletionItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.DefaultItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser Rule based item resolver for Type Name Context.
 */
public class ParserRuleTypeNameContextResolver extends AbstractItemResolver {
    
    private static final String CONNECTOR_KIND = "CONNECTOR";
    
    private static final String CATCH_KEY_WORD = "catch";
    
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {

        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        ParserRuleContext parserRuleContext = completionContext.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY);
        CompletionItemSorter itemSorter = ItemSorters.getSorterByClass(DefaultItemSorter.class);

        if (parserRuleContext.getParent() instanceof BallerinaParser.CatchClauseContext
                && CommonUtil.isWithinBrackets(completionContext, Collections.singletonList(CATCH_KEY_WORD))) {
            this.populateCompletionItemList(filterCatchConditionSymbolInfo(completionContext), completionItems);
        } else if (tokenStream.get(completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY)).getText().equals(":")) {
            /*
            TODO: ATM, this particular condition becomes true only when try to access packages' items in the 
            endpoint definition context
             */
            this.populateCompletionItemList(filterEndpointContextSymbolInfo(completionContext), completionItems);
        } else {
            StatementTemplateFilter statementTemplateFilter = new StatementTemplateFilter();
            // Add the statement templates
            completionItems.addAll(statementTemplateFilter.filterItems(completionContext));
            this.populateBasicTypes(completionItems, completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY));
            itemSorter = 
                    ItemSorters.getSorterByClass(completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY).getClass());
        }
        itemSorter.sortItems(completionContext, completionItems);

        return completionItems;
    }

    private static List<SymbolInfo> filterEndpointContextSymbolInfo(LSServiceOperationContext context) {
        List<SymbolInfo> symbolInfos = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        int currentTokenIndex = context.get(DocumentServiceKeys.TOKEN_INDEX_KEY) - 1;
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        Token packageAlias = CommonUtil.getPreviousDefaultToken(tokenStream, currentTokenIndex);
        Token constraintStart = CommonUtil.getPreviousDefaultToken(tokenStream, packageAlias.getTokenIndex() - 1);
        List<SymbolInfo> returnList = new ArrayList<>();
        
        if (!(constraintStart.getText().equals("<") || constraintStart.getText().equals("create"))) {
            PackageActionFunctionAndTypesFilter filter = new PackageActionFunctionAndTypesFilter();
            returnList.addAll(filter.filterItems(context));
        } else {
            SymbolInfo packageSymbolInfo = symbolInfos.stream().filter(item -> {
                Scope.ScopeEntry scopeEntry = item.getScopeEntry();
                return item.getSymbolName().equals(packageAlias.getText())
                        && scopeEntry.symbol instanceof BPackageSymbol;
            }).findFirst().orElse(null);

            if (packageSymbolInfo != null) {
                packageSymbolInfo.getScopeEntry().symbol.scope.entries.forEach((name, value) -> {
                    if (value.symbol.kind != null && value.symbol.kind.toString().equals(CONNECTOR_KIND)) {
                        returnList.add(new SymbolInfo(name.toString(), value));
                    }
                });
            }
        }
        
        return returnList;
    }

    private static List<SymbolInfo> filterCatchConditionSymbolInfo(LSServiceOperationContext context) {
        List<SymbolInfo> symbolInfos = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);

        return symbolInfos.stream().filter(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return bSymbol.getType() instanceof BStructType
                    && checkErrorStructEquivalence((BStructType) bSymbol.getType());
        }).collect(Collectors.toList());
    }
    
    private static boolean checkErrorStructEquivalence(BStructType bStructType) {
        List<BStructType.BStructField> fields = bStructType.getFields();
        String errorField = "cause";
        String errorType = "error";
        String msgField = "message";
        String msgType = "string";
        int fieldCounter = 0;

        for (BStructType.BStructField field : fields) {
            if ((field.getName().getValue().equals(errorField) && field.getType().toString().equals(errorType))
                    || (field.getName().getValue().equals(msgField) && field.getType().toString().equals(msgType))) {
                fieldCounter++;
            }

            if (fieldCounter == 2) {
                return true;
            }
        }
        
        return false;
    }
}
