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

import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Item Sorter for the Variable Definition context.
 */
public class VariableDefContextItemSorter extends CompletionItemSorter {
    /**
     * Sort Completion Items based on a particular criteria.
     *
     * @param ctx             Completion context
     * @param completionItems List of initial completion items
     */
    @Override
    public void sortItems(LSServiceOperationContext ctx, List<CompletionItem> completionItems) {
        this.setPriorities(completionItems);
        String variableType = this.getVariableType(ctx);
        int startTokenIndex = ctx.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY).getStart().getTokenIndex();
        int stopTokenIndex = ctx.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY).getStop().getTokenIndex();
        TokenStream tokenStream = ctx.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        String tokenString;
        boolean isEqualTokenFound = false;
        int tokenCounter = startTokenIndex + 1;
        
        while (true) {
            if (tokenCounter > stopTokenIndex) {
                break;
            }
            tokenString = tokenStream.get(tokenCounter).getText();
            if ("=".equals(tokenString)) {
                isEqualTokenFound = true;
                break;
            }
            tokenCounter++;
        }
        
        if (!isEqualTokenFound) {
            completionItems.clear();
        } else {
            completionItems.forEach(completionItem -> {
                if (completionItem.getDetail().equals(ItemResolverConstants.FUNCTION_TYPE)) {
                    String label = completionItem.getLabel();
                    String functionReturnType = label.substring(label.lastIndexOf("(") + 1, label.lastIndexOf(")"));
                    
                    if (variableType.equals(functionReturnType)) {
                        this.increasePriority(completionItem);
                    }
                }
            });
        }
    }
    
    private void increasePriority(CompletionItem completionItem) {
        int sortText = Integer.parseInt(completionItem.getSortText());
        completionItem.setSortText(Integer.toString(sortText - 1));
    }

    /**
     * Get the variable type.
     * @param ctx   Document Service context (Completion context)
     * @return      {@link String} type of the variable
     */
    String getVariableType(LSServiceOperationContext ctx) {
        return ctx.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY).start.getText();
    }
}
