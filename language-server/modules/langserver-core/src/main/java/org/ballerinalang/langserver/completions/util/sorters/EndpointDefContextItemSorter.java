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
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;

import java.util.List;

/**
 * Endpoint definition context item sorter.
 */
public class EndpointDefContextItemSorter extends CompletionItemSorter {
    /**
     * Sort Completion Items based on a particular criteria.
     *
     * @param ctx             Completion context
     * @param completionItems List of initial completion items
     */
    @Override
    public void sortItems(LSServiceOperationContext ctx, List<CompletionItem> completionItems) {
        this.setPriorities(completionItems);
        BLangVariable bLangVariable = (BLangVariable) ctx.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        if (!(bLangVariable.typeNode instanceof BLangEndpointTypeNode)) {
            return;
        }
        String constraintType = ((BLangEndpointTypeNode) bLangVariable.typeNode).endpointType
                .type.toString();
        completionItems.forEach(completionItem -> {
            if (completionItem.getDetail().equals(ItemResolverConstants.FUNCTION_TYPE)) {
                String label = completionItem.getLabel();
                String[] returnTypes = label.substring(label.lastIndexOf("(") + 1, label.lastIndexOf(")")).split(",");
                if (returnTypes.length == 1 && returnTypes[0].equals(constraintType)) {
                    String newPriority = Priority.shiftPriority(completionItem.getSortText(), -1);
                    completionItem.setSortText(String.valueOf(newPriority));
                }
            } else if (completionItem.getDetail().equals(ItemResolverConstants.KEYWORD_TYPE)) {
                completionItem.setSortText(Priority.shiftPriority(Priority.PRIORITY110.toString(), -1));
            } else if (completionItem.getDetail().equals(constraintType)) {
                completionItem.setSortText(Priority.shiftPriority(completionItem.getSortText(), -1));
            }
        });
    }
}
