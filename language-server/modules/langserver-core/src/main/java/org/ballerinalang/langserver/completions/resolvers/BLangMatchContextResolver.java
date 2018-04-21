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
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Completion Item resolver for the BLangMatch Scope.
 */
public class BLangMatchContextResolver extends AbstractItemResolver {

    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        BLangNode symbolEnvNode = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        List<SymbolInfo> visibleSymbols = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        if (!(symbolEnvNode instanceof BLangMatch)) {
            return completionItems;
        }
        BLangMatch bLangMatch = (BLangMatch) symbolEnvNode;
        
        if (bLangMatch.expr.type instanceof BUnionType) {
            Set<BType> memberTypes = ((BUnionType) ((BLangSimpleVarRef) bLangMatch.expr).type).getMemberTypes();
            memberTypes.forEach(bType -> {
                completionItems.add(this.populateCompletionItem(bType.toString(), ItemResolverConstants.B_TYPE,
                        bType.toString()));
            });
        } else if (bLangMatch.expr.type instanceof BJSONType) {
            ArrayList<Integer> typeTagsList = new ArrayList<>(Arrays.asList(TypeTags.INT, TypeTags.FLOAT,
                    TypeTags.BOOLEAN, TypeTags.STRING, TypeTags.NIL, TypeTags.JSON));
            List<SymbolInfo> filteredBasicTypes = visibleSymbols.stream().filter(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                return bSymbol instanceof BTypeSymbol
                        && typeTagsList.contains(bSymbol.getType().tag);
            }).collect(Collectors.toList());
            this.populateCompletionItemList(filteredBasicTypes, completionItems);
        } else {
            if (bLangMatch.expr.type instanceof BStructType) {
                List<SymbolInfo> structSymbols = visibleSymbols.stream()
                        .filter(symbolInfo -> {
                            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                            return bSymbol instanceof BStructSymbol
                                    && !bSymbol.getName().getValue().startsWith(UtilSymbolKeys.ANON_STRUCT_CHECKER);
                        })
                        .collect(Collectors.toList());
                this.populateCompletionItemList(structSymbols, completionItems);
            }
        }
        
        return completionItems;
    }
}
