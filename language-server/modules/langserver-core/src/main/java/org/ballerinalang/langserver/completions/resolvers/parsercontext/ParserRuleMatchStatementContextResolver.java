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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Completion Item resolver for the match statement parser rule context.
 */
public class ParserRuleMatchStatementContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        int currentTokenIndex = completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        List<SymbolInfo> visibleSymbols = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);

        while (true) {
            if (currentTokenIndex < 0) {
                // Ideally should not come to this point
                return completionItems;
            }

            Token token = CommonUtil.getPreviousDefaultToken(tokenStream, currentTokenIndex);
            if (token.getText().equals(UtilSymbolKeys.MATCH_KEYWORD_KEY)) {
                currentTokenIndex = token.getTokenIndex();
                break;
            } else {
                currentTokenIndex = token.getTokenIndex();
            }
        }

        String identifierMatched = CommonUtil.getNextDefaultToken(tokenStream, currentTokenIndex).getText();

        SymbolInfo identifierSymbol = visibleSymbols.stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol.getName().getValue().equals(identifierMatched))
                .findFirst()
                .orElseGet(null);

        if (identifierSymbol == null) {
            return completionItems;
        } else if (identifierSymbol.getScopeEntry().symbol.type instanceof BUnionType) {
            Set<BType> memberTypes = ((BUnionType) identifierSymbol.getScopeEntry().symbol.type).getMemberTypes();
            memberTypes.forEach(bType -> {
                BTypeSymbol bTypeSymbol = bType.tsymbol;
                String insertTextSnippet = bTypeSymbol.pkgID.getName().getValue()
                        + UtilSymbolKeys.PKG_DELIMITER_KEYWORD + bTypeSymbol.getName().getValue()
                        + " => {" + System.lineSeparator() + "\t${1}" + System.lineSeparator() + "}";
                String label = bTypeSymbol.pkgID.getName().getValue()
                        + UtilSymbolKeys.PKG_DELIMITER_KEYWORD + bTypeSymbol.getName().getValue();
                CompletionItem completionItem = this.populateCompletionItem(insertTextSnippet,
                        ItemResolverConstants.B_TYPE, label);
                completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
                completionItems.add(completionItem);
            });
        } else if (identifierSymbol.getScopeEntry().symbol.type instanceof BJSONType) {
            ArrayList<Integer> typeTagsList = new ArrayList<>(Arrays.asList(TypeTags.INT, TypeTags.FLOAT,
                    TypeTags.BOOLEAN, TypeTags.STRING, TypeTags.NIL, TypeTags.JSON));
            List<SymbolInfo> filteredBasicTypes = visibleSymbols.stream().filter(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                return bSymbol instanceof BTypeSymbol
                        && typeTagsList.contains(bSymbol.getType().tag);
            }).collect(Collectors.toList());
            this.populateCompletionItemList(filteredBasicTypes, completionItems);
        } else if (identifierSymbol.getScopeEntry().symbol.type instanceof BStructType) {
            List<SymbolInfo> structSymbols = visibleSymbols.stream()
                    .filter(symbolInfo -> {
                        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                        return bSymbol instanceof BStructSymbol
                                && !bSymbol.getName().getValue().startsWith(UtilSymbolKeys.ANON_STRUCT_CHECKER);
                    })
                    .collect(Collectors.toList());
            this.populateCompletionItemList(structSymbols, completionItems);
        }

        return completionItems;
    }
}
