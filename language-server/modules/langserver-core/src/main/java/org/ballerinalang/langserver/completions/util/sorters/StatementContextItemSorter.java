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
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Item Sorter for the statement context.
 */
public class StatementContextItemSorter extends CompletionItemSorter {
    /**
     * Sort Completion Items based on a particular criteria.
     *
     * @param ctx             Completion context
     * @param completionItems List of initial completion items
     */
    @Override
    public void sortItems(LSServiceOperationContext ctx, List<CompletionItem> completionItems) {
        int startTokenIndex = ctx.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY).getStart().getTokenIndex();
        int stopTokenIndex = ctx.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY).getStop().getTokenIndex();
        if (startTokenIndex > 0 && stopTokenIndex < 0) {
            completionItems.clear();
        }
    }
}
