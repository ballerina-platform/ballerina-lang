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
import org.ballerinalang.composer.service.workspace.suggetions.PossibleToken;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Statement context resolver for resolving the items of the statement context
 */
public class StatementContextResolver implements ItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols) {
//        ParserRuleContext currentContext = dataModel.getContext();
        List<PossibleToken> possibleTokens = dataModel.getPossibleTokens();
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        for (PossibleToken token : possibleTokens) {
            // For each token call the api to get the items related to the token
            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel(token.getTokenName());
            completionItems.add(completionItem);
        }
        return completionItems;
    }
}
