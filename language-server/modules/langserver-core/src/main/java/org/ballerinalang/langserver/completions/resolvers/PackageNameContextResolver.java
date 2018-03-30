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

import org.ballerinalang.langserver.BLangPackageContext;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.ArrayList;
import java.util.List;


/**
 * Completion Item Resolver for the Package name context.
 */
public class PackageNameContextResolver extends AbstractItemResolver {
    
    @Override
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        BLangPackageContext bLangPkgContext = completionContext.get(DocumentServiceKeys.B_LANG_PACKAGE_CONTEXT_KEY);
        ArrayList<PackageID> sdkPackages = bLangPkgContext
                .getSDKPackages(completionContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));

        sdkPackages.forEach(packageID -> {
            String insertText = packageID.getOrgName().getValue() + "/" + packageID.getName().getValue();
            String label = insertText + ":" + packageID.getPackageVersion().getValue();
            fillImportCompletion(label, insertText, completionItems);
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
