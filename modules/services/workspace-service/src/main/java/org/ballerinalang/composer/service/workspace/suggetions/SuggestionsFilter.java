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

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.consts.LangServerConstants;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ResolveCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Filter out the suggestions from the data model
 */
public class SuggestionsFilter {

    private static final Logger logger = LoggerFactory.getLogger(SuggestionsFilter.class);
    private ResolveCommandExecutor resolveCommandExecutor = new ResolveCommandExecutor();

    /**
     * Get the completion items list
     * @param dataModel - Suggestion filter data model
     * @return {@link ArrayList} - completion items list
     */
    public ArrayList<CompletionItem> getCompletionItems(SuggestionsFilterDataModel dataModel,
                                                        ArrayList<SymbolInfo> symbols) {
        return resolveCommandExecutor.resolveCompletionItems(dataModel.getContext().getClass(), dataModel, symbols);
    }

    /**
     * Filter out the suggestion criteria
     * @param dataModel - Suggestion filter data model
     * @return {@link int} - suggestion criteria
     */
    public int filterSuggestionCriteria(SuggestionsFilterDataModel dataModel) {
//        ParserRuleContext currentContext = dataModel.getContext();
        TokenStream tokenStream = dataModel.getTokenStream();
        Token nextToken = null;
        int currentTokenIndex = dataModel.getTokenIndex();
        int nextTokenIndex = currentTokenIndex + 1;
        boolean invalid = true;

        while (invalid) {
            if (tokenStream.get(nextTokenIndex).getChannel() == Token.DEFAULT_CHANNEL) {
                nextToken = tokenStream.get(nextTokenIndex);
                invalid = false;
            } else {
                nextTokenIndex++;
            }
        }

        if (nextToken != null && nextToken.getText().equals(":")) {
            return LangServerConstants.FUNCTION_INVOCATION_CRITERIA;
        } else {
            return LangServerConstants.INVALID_CRITERIA;
        }
    }
}
