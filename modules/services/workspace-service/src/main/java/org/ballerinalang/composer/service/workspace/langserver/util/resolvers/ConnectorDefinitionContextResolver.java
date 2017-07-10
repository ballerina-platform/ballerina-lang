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
import org.ballerinalang.model.statements.VariableDefStmt;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Resolver for Connector definition context
 */
public class ConnectorDefinitionContextResolver extends AbstractItemResolver {
    @Override
    ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                           HashMap<Class, AbstractItemResolver> resolvers) {

        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        CompletionItem connectorActionItem = new CompletionItem();
        connectorActionItem.setLabel(ItemResolverConstants.ACTION);
        connectorActionItem.setInsertText(ItemResolverConstants.CONNECTOR_ACTION_TEMPLATE);
        connectorActionItem.setDetail(ItemResolverConstants.ACTION_TYPE);
        connectorActionItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItems.add(connectorActionItem);

        completionItems.addAll(resolvers.get(VariableDefStmt.class).resolveItems(dataModel, symbols , resolvers));

        return completionItems;
    }
}
