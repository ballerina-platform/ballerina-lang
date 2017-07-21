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
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.GlobalScope;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ServiceContextResolver
 */
public class ServiceContextResolver extends AbstractItemResolver {

    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                           HashMap<Class, AbstractItemResolver> resolvers) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        if (!this.isAnnotationContext(dataModel)) {
            // Add resource
            CompletionItem resource = new CompletionItem();
            resource.setLabel(ItemResolverConstants.RESOURCE_TYPE);
            resource.setInsertText(ItemResolverConstants.RESOURCE_TEMPLATE);
            resource.setDetail(ItemResolverConstants.SNIPPET_TYPE);
            resource.setSortText(ItemResolverConstants.PRIORITY_7);
            completionItems.add(resource);
        }

        // Add annotations
        completionItems.addAll(resolvers.get(AnnotationAttachment.class).resolveItems(dataModel, symbols, resolvers));
        // Add global symbols such as variable types(int, string etc.)
        completionItems.addAll(resolvers.get(GlobalScope.class).resolveItems(dataModel, symbols, resolvers));

        HashMap<String, Integer> prioritiesMap = new HashMap<>();
        prioritiesMap.put(ItemResolverConstants.RESOURCE_TYPE, ItemResolverConstants.PRIORITY_7);
        prioritiesMap.put(ItemResolverConstants.ANNOTATION_TYPE, ItemResolverConstants.PRIORITY_6);
        prioritiesMap.put(ItemResolverConstants.PACKAGE_TYPE, ItemResolverConstants.PRIORITY_5);
        prioritiesMap.put(ItemResolverConstants.B_TYPE, ItemResolverConstants.PRIORITY_4);
        this.assignItemPriorities(prioritiesMap, completionItems);

        return completionItems;
    }
}
