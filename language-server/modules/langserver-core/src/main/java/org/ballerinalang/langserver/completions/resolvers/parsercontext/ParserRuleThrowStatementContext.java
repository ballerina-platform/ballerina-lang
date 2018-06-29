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

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the throw statement context.
 */
public class ParserRuleThrowStatementContext extends AbstractItemResolver {
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> symbolInfoList = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        List<SymbolInfo> filteredList = symbolInfoList.stream().filter(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            if (bSymbol instanceof BVarSymbol && bSymbol.getType() instanceof BStructureType) {
                List<String> structFieldNames = ((BStructureType) bSymbol.getType()).getFields().stream()
                        .map(bStructField -> bStructField.getName().getValue())
                        .collect(Collectors.toList());
                
                return structFieldNames.contains("message") && structFieldNames.contains("cause");
            }
            
            return false;
        }).collect(Collectors.toList());
        
        this.populateCompletionItemList(filteredList, completionItems);
        
        return completionItems;
    }
}
