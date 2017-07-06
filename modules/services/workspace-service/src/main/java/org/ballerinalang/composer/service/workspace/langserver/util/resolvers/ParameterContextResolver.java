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
import org.ballerinalang.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parameter context resolver for resolving the items of the parameter context
 */
public class ParameterContextResolver implements ItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> filteredSymbols = symbols.stream()
                .filter(symbolInfo -> symbolInfo.getSymbol() instanceof BType)
                .collect(Collectors.toList());

        filteredSymbols.forEach(symbolInfo -> {
            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel(symbolInfo.getSymbolName());
            completionItem.setInsertText(symbolInfo.getSymbolName());
            completionItem.setDetail("BType");
            completionItems.add(completionItem);
        });

        return completionItems;
    }
}
