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

import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Item sorter for the connector context.
 */
class ConnectorContextItemSorter extends CompletionItemSorter {
    @Override
    public void sortItems(LSServiceOperationContext ctx, List<CompletionItem> completionItems) {
        BLangNode previousNode = ctx.get(CompletionKeys.PREVIOUS_NODE_KEY);

        /*
        Remove the statement type completion type. When the going through the parser
        rule contexts such as typeNameContext, we add the statements as well.
        Sorters are responsible for to the next level of such filtering.
         */
        this.removeCompletionsByType(new ArrayList<>(Collections.singletonList(ItemResolverConstants.STATEMENT_TYPE)),
                completionItems);
        if (previousNode == null) {
            this.populateWhenCursorBeforeOrAfterEp(completionItems);
        } else if (previousNode instanceof BLangVariableDef) {
//            BType bLangType = ((BLangVariableDef) previousNode).var.type;
//            if (bLangType instanceof BEndpointType) {
//                this.populateWhenCursorBeforeOrAfterEp(completionItems);
//            } else {
                this.setPriorities(completionItems);
                CompletionItem resItem = this.getActionSnippet();
                resItem.setSortText(Priority.PRIORITY160.toString());
                completionItems.add(resItem);
//            }
        } else if (previousNode instanceof BLangAction) {
            completionItems.clear();
            completionItems.add(this.getActionSnippet());
        }
    }

    private void populateWhenCursorBeforeOrAfterEp(List<CompletionItem> completionItems) {
        CompletionItem epSnippet = this.getEndpointSnippet();
        CompletionItem resSnippet = this.getActionSnippet();
        this.setPriorities(completionItems);

        epSnippet.setSortText(Priority.PRIORITY150.toString());
        resSnippet.setSortText(Priority.PRIORITY160.toString());
        completionItems.add(epSnippet);
        completionItems.add(resSnippet);
    }
    
    private CompletionItem getActionSnippet() {
        CompletionItem connectorActionItem = new CompletionItem();
        connectorActionItem.setLabel(ItemResolverConstants.ACTION);
        connectorActionItem.setInsertText(Snippet.CONNECTOR_ACTION.toString());
        connectorActionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        connectorActionItem.setDetail(ItemResolverConstants.ACTION_TYPE);
        return connectorActionItem;
    }
}
