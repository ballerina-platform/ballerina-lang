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

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Completion item sorter for conditional statements such as if statement and while statement conditions.
 * @since 0.965.0
 */
public class ConditionalStatementItemSorter extends CompletionItemSorter {

    private static final String OPEN_BRACKET = "(";
    
    private static final String CLOSE_BRACKET = ")";
    /**
     * Sort Completion Items based on a particular criteria.
     *
     * @param ctx             Completion context
     * @param completionItems List of initial completion items
     */
    @Override
    public void sortItems(LSServiceOperationContext ctx, List<CompletionItem> completionItems) {
        this.setPriorities(completionItems);
        completionItems.forEach(completionItem -> {
            String detail = completionItem.getDetail();
            if (detail.equals(ItemResolverConstants.FUNCTION_TYPE)) {
                String signature = completionItem.getLabel();
                String returnType = signature
                        .substring(signature.lastIndexOf(OPEN_BRACKET), signature.lastIndexOf(CLOSE_BRACKET)).trim();
                if (returnType.endsWith(ItemResolverConstants.BOOLEAN_TYPE)) {
                    completionItem.setSortText(Priority.shiftPriority(completionItem.getSortText(), -1));
                }
            } else if (detail.equals(ItemResolverConstants.BOOLEAN_TYPE)) {
                completionItem.setSortText(Priority.shiftPriority(completionItem.getSortText(), -1));
            }
        });
    }
}
