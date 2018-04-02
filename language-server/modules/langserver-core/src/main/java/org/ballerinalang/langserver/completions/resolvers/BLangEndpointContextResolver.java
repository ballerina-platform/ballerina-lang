/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.ArrayList;
import java.util.List;

/**
 * BLangEndpoint context Item Resolver.
 */
public class BLangEndpointContextResolver extends AbstractItemResolver {
    
    private static final String INIT = "init";
    
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        BLangNode bLangEndpoint = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        ArrayList<SymbolInfo> configurationFields = new ArrayList<>();
        List<BStructSymbol.BAttachedFunction> attachedFunctions = new ArrayList<>();
        
        if (((BLangEndpoint) bLangEndpoint).type.tsymbol instanceof BStructSymbol) {
            attachedFunctions.addAll(((BStructSymbol) ((BLangEndpoint) bLangEndpoint).type.tsymbol).attachedFuncs);
        }

        BStructSymbol.BAttachedFunction initFunction = attachedFunctions.stream()
                .filter(bAttachedFunction -> bAttachedFunction.funcName.getValue().equals(INIT))
                .findFirst()
                .orElseGet(null);

        BVarSymbol configSymbol = initFunction.symbol.getParameters().get(0);

        BType configSymbolType = configSymbol.getType();
        if (configSymbolType instanceof BStructType) {
            ((BStructType) configSymbolType).getFields().forEach(bStructField -> configurationFields.add(
                    new SymbolInfo(bStructField.getName().getValue(), new Scope.ScopeEntry(bStructField.symbol, null))
            ));
        }
        this.populateCompletionItemList(configurationFields, completionItems);

        return completionItems;
    }
}
