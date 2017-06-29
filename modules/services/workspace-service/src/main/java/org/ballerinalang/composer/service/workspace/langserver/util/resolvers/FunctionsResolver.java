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
import org.ballerinalang.composer.service.workspace.langserver.consts.SymbolKind;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.completion.PackageItemResolver;
import org.ballerinalang.composer.service.workspace.model.Function;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the functions in a package
 */
public class FunctionsResolver implements ItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols) {
        String packageName = dataModel.getContext().getStart().getText();
        PackageItemResolver packageItemResolver = PackageItemResolver.getInstance();
        List<Function> functions = packageItemResolver.getFunctionInvocations(packageName);
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        functions.forEach(function -> {
            CompletionItem item = new CompletionItem();
            item.setLabel(function.getName());
            item.setDetail(function.getDescription());
            item.setKind(SymbolKind.FUNCTION_DEF);
            completionItems.add(item);
        });

        return completionItems;
    }
}
