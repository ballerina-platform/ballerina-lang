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
        String lastPoppedToken = poppedTokens.get(poppedTokens.size() - 1);
        if (lastPoppedToken.equals(UtilSymbolKeys.IMPORT_KEYWORD_KEY)) {
            completionItems.addAll(this.getOrgNameCompletionItems(packagesList));
        } else {
            int slashIndex = poppedTokens.indexOf(UtilSymbolKeys.SLASH_KEYWORD_KEY);
            
            if (slashIndex > 0) {
                String orgName = poppedTokens.get(slashIndex - 1);
                completionItems.addAll(this.getPackageNameCompletions(orgName, packagesList));
            } else {
                completionItems.addAll(this.getOrgNameCompletionItems(packagesList));
            }
        }

        return completionItems;
    }

    private ArrayList<CompletionItem> getOrgNameCompletionItems(List<BallerinaPackage> packagesList) {
        List<String> orgNames = new ArrayList<>();
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        packagesList.stream()
                .filter(ballerinaPackage -> !orgNames.contains(ballerinaPackage.getOrgName()))
                .forEach(ballerinaPackage -> orgNames.add(ballerinaPackage.getOrgName()));

        orgNames.forEach(orgName -> fillImportCompletion(orgName, orgName, completionItems));

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
