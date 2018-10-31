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
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Completion Item Resolver for the Package name context.
 */
public class PackageNameContextResolver extends AbstractItemResolver {
    
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<BallerinaPackage> packagesList = new ArrayList<>();
        Stream.of(LSPackageLoader.getSdkPackages(), LSPackageLoader.getHomeRepoPackages())
                .forEach(packagesList::addAll);
        List<String> poppedTokens = ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY)
                .stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        
        if (poppedTokens.contains(UtilSymbolKeys.SLASH_KEYWORD_KEY)) {
            String orgName = poppedTokens.get(poppedTokens.indexOf(UtilSymbolKeys.SLASH_KEYWORD_KEY) - 1);
            completionItems.addAll(this.getPackageNameCompletions(orgName, packagesList));
        } else if (poppedTokens.contains(UtilSymbolKeys.IMPORT_KEYWORD_KEY)) {
            completionItems.addAll(this.getOrgNameCompletionItems(packagesList));
        }

        return completionItems;
    }

    private ArrayList<CompletionItem> getOrgNameCompletionItems(List<BallerinaPackage> packagesList) {
        List<String> orgNames = new ArrayList<>();
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        packagesList.forEach(pkg -> {
            if (!orgNames.contains(pkg.getOrgName())) {
                completionItems.add(getImportCompletion(pkg.getOrgName(), pkg.getOrgName()));
                orgNames.add(pkg.getOrgName());
            }
        });

        return completionItems;
    }

    private ArrayList<CompletionItem> getPackageNameCompletions(String orgName, List<BallerinaPackage> packagesList) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<String> pkgNameLabels = new ArrayList<>();

        packagesList.forEach(ballerinaPackage -> {
            String label = ballerinaPackage.getPackageName();
            if (orgName.equals(ballerinaPackage.getOrgName()) && !pkgNameLabels.contains(label)) {
                pkgNameLabels.add(label);
                String insertText = label + ";";
                completionItems.add(getImportCompletion(label, insertText));
            }
        });
        
        return completionItems;
    }
    
    private static CompletionItem getImportCompletion(String label, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setKind(CompletionItemKind.Module);
        item.setDetail(ItemResolverConstants.PACKAGE_TYPE);
        
        return item;
    }
}
