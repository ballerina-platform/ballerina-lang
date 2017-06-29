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

package org.ballerinalang.composer.service.workspace.suggetions;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ResolveCommandExecutor;

import java.util.ArrayList;

/**
 * Filter out the suggestions from the data model
 */
public class SuggestionsFilter {

    private ResolveCommandExecutor resolveCommandExecutor = new ResolveCommandExecutor();

    /**
     * Get the completion items list
     * @param dataModel - Suggestion filter data model
     * @return {@link ArrayList} - completion items list
     */
    public ArrayList<CompletionItem> getCompletionItems(SuggestionsFilterDataModel dataModel,
                                                        ArrayList<SymbolInfo> symbols) {
        if (dataModel.getContext() == null) {
            return resolveCommandExecutor.resolveCompletionItems(null, dataModel, symbols);
        } else {
            return resolveCommandExecutor.resolveCompletionItems(dataModel.getContext().getClass(), dataModel, symbols);
        }
    }
}
