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
package org.ballerinalang.langserver.completions.util.sorters;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.List;

/**
 * Item Sorter for the Assignment statement context.
 */
class AssignmentStmtContextSorter extends VariableDefContextItemSorter {
    /**
     * Get the variable type.
     *
     * @param ctx Document Service context (Completion context)
     * @return {@link String} type of the variable
     */
    @Override
    String getVariableType(LSServiceOperationContext ctx) {
        String variableName = ctx.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY).getStart().getText();
        List<SymbolInfo> visibleSymbols = ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        SymbolInfo filteredSymbol = visibleSymbols.stream().filter(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            String symbolName = symbolInfo.getSymbolName();
            return bSymbol instanceof BVarSymbol && symbolName.equals(variableName);
        }).findFirst().orElse(null);
        
        if (filteredSymbol != null) {
            return filteredSymbol.getScopeEntry().symbol.type.toString();
        }
        
        return "";
    }
}
