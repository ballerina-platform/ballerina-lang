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

package org.ballerinalang.langserver.completions.resolvers;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSContextManager;
import org.ballerinalang.langserver.LSPackageCache;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;


/**
 * Completion Item Resolver for the Package name context.
 */
public class PackageNameContextResolver extends AbstractItemResolver {
    
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        int currentIndex = completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        if (tokenStream.get(currentIndex).getText().equals(UtilSymbolKeys.IMPORT_KEYWORD_KEY)) {
            completionItems.addAll(this.getOrgNameCompletionItems());
        } else {
            StringBuilder orgNameComponent = new StringBuilder();
            while (true) {
                if (currentIndex < 0) {
                    return new ArrayList<>();
                }
                Token token = CommonUtil.getPreviousDefaultToken(tokenStream, currentIndex);
                if (token.getText().equals(UtilSymbolKeys.IMPORT_KEYWORD_KEY)) {
                    break;
                }
                orgNameComponent.append(token.getText());
                currentIndex = token.getTokenIndex();
            }

            if (orgNameComponent.toString().contains("/")) {
                String orgName = orgNameComponent.toString().replace("/", "").trim();
                completionItems.addAll(this.getPackageNameCompletions(orgName));
            } else {
                completionItems.addAll(this.getOrgNameCompletionItems());
            }
        }

        return completionItems;
    }

    private ArrayList<CompletionItem> getOrgNameCompletionItems() {
        List<String> orgNames = new ArrayList<>();
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        CompilerContext context = LSContextManager.getInstance().getBuiltInPackagesCompilerContext();
        LSPackageCache.getInstance(context).getPackageMap().entrySet().forEach(pkgEntry -> {
            if (!orgNames.contains(pkgEntry.getValue().packageID.getOrgName().toString())) {
                orgNames.add(pkgEntry.getValue().packageID.getOrgName().toString());
            }
        });

        orgNames.forEach(orgName -> {
            String insertText = orgName + "/";
            fillImportCompletion(orgName, insertText, completionItems);
        });

        return completionItems;
    }

    private ArrayList<CompletionItem> getPackageNameCompletions(String orgName) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        CompilerContext context = LSContextManager.getInstance().getBuiltInPackagesCompilerContext();
        LSPackageCache.getInstance(context).getPackageMap().entrySet().forEach(pkgEntry -> {
            PackageID packageID = pkgEntry.getValue().packageID;
            if (orgName.equals(packageID.orgName.getValue())) {
                String label = packageID.getName().getValue();
                String insertText = label + ";";
                fillImportCompletion(label, insertText, completionItems);
            }
        });
        
        return completionItems;
    }
    
    private static void fillImportCompletion(String label, String insertText, List<CompletionItem> completionItems) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setKind(CompletionItemKind.File);
        item.setDetail(ItemResolverConstants.PACKAGE_TYPE);
        completionItems.add(item);
    }
}
