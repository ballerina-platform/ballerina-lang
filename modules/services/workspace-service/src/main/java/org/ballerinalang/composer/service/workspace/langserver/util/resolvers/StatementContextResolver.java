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

package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.natives.NativeUnitProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Statement context resolver for resolving the items of the statement context
 */
public class StatementContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols) {

        //TODO: This code is a copy of the Variable Definition context. Variable Definition context code will be
        // improved in the future

        // We need to Calculate from which level of the symbol table. Decide the level by considering
        // the number of : and the . from the current token and search is terminated when an endline met

        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        List<SymbolInfo> searchList = filterPackageActionsAndFunctions(dataModel, symbols);

        populateCompletionItemList(searchList, completionItems);

        return completionItems;
    }

    void populateNativeUnitProxyCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        NativeUnit nativeUnit = ((NativeUnitProxy) symbolInfo.getSymbol()).load();
        StringBuffer signature = new StringBuffer(symbolInfo.getSymbolName());
        int i = 0;
        String initString = "(";
        for (SimpleTypeName simpleTypeName : nativeUnit.getArgumentTypeNames()) {
            signature.append(initString).append(simpleTypeName.getName()).append(" ")
                    .append(nativeUnit.getArgumentNames()[i]);
            ++i;
            initString = ", ";
        }
        signature.append(")");
        initString = "(";
        String endString = "";
        for (SimpleTypeName simpleTypeName : nativeUnit.getReturnParamTypeNames()) {
            signature.append(initString).append(simpleTypeName.getName());
            initString = ", ";
            endString = ")";
        }
        signature.append(endString);
        completionItem.setLabel(signature.toString());
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_3);
    }
}
