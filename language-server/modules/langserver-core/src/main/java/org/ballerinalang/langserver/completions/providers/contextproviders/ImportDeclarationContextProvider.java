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
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Completion Item Resolver for the Package name context.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class ImportDeclarationContextProvider extends AbstractCompletionProvider {

    public ImportDeclarationContextProvider() {
        this.attachmentPoints.add(BallerinaParser.ImportDeclarationContext.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<BallerinaPackage> packagesList = new ArrayList<>();
        Stream.of(LSPackageLoader.getSdkPackages(), LSPackageLoader.getHomeRepoPackages())
                .forEach(packagesList::addAll);
        List<CommonToken> lhsTokens = ctx.get(CompletionKeys.LHS_TOKENS_KEY);
        List<CommonToken> lhsDefaultTokens = ctx.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
        List<Integer> lhsDefaultTokenTypes = ctx.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        int divIndex = lhsDefaultTokenTypes.indexOf(BallerinaParser.DIV);
        int importTokenIndex = lhsDefaultTokenTypes.indexOf(BallerinaParser.IMPORT);
        CommonToken lastToken = CommonUtil.getLastItem(lhsTokens);

        if (divIndex > -1 && (divIndex == lhsDefaultTokenTypes.size() - 1 
                || divIndex == lhsDefaultTokenTypes.size() - 2)) {
            String orgName = lhsDefaultTokens.get(lhsDefaultTokenTypes.indexOf(BallerinaParser.DIV) - 1).getText();
            completionItems.addAll(this.getPackageNameCompletions(orgName, packagesList));
        } else if (importTokenIndex > -1 && (importTokenIndex == lhsDefaultTokenTypes.size() - 1
                || importTokenIndex == lhsDefaultTokenTypes.size() - 2)) {
            completionItems.addAll(this.getItemsIncludingOrgName(packagesList, ctx));
        } else if (importTokenIndex > -1 && lhsDefaultTokenTypes.size() >= 2
                && (lastToken.getChannel() == Token.HIDDEN_CHANNEL
                || lhsTokens.get(lhsTokens.size() - 2).getChannel() == Token.HIDDEN_CHANNEL)) {
            completionItems.add(getAsKeyword());
        }

        return completionItems;
    }

    private ArrayList<CompletionItem> getItemsIncludingOrgName(List<BallerinaPackage> packagesList, LSContext ctx) {
        List<String> orgNames = new ArrayList<>();
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        packagesList.forEach(pkg -> {
            String fullPkgNameLabel = pkg.getOrgName() + "/" + pkg.getPackageName();
            String insertText = pkg.getOrgName() + "/";
            if (pkg.getOrgName().equals(Names.BALLERINA_ORG.value)
                    && pkg.getPackageName().startsWith(Names.LANG.value + ".")) {
                insertText += getLangLibModuleNameInsertText(pkg.getPackageName());
            } else {
                insertText += pkg.getPackageName();
            }
            // Do not add the semicolon with the insert text since the user should be allowed to use the as keyword
            CompletionItem fullPkgImport = getImportCompletion(fullPkgNameLabel, insertText);
            fullPkgImport.setSortText(Priority.PRIORITY120.toString());
            completionItems.add(fullPkgImport);
            if (!orgNames.contains(pkg.getOrgName())) {
                CompletionItem orgNameImport = getImportCompletion(pkg.getOrgName(), (pkg.getOrgName() + "/"));
                orgNameImport.setSortText(Priority.PRIORITY110.toString());
                completionItems.add(orgNameImport);
                orgNames.add(pkg.getOrgName());
            }
        });

        /*
        If within a project, suggest the project modules except the owner module of the file which being processed
         */
        LSDocument lsDocument = ctx.get(DocumentServiceKeys.LS_DOCUMENT_KEY);
        if (lsDocument != null && lsDocument.isWithinProject()) {
            String ownerModule = lsDocument.getOwnerModule();
            List<String> projectModules = lsDocument.getProjectModules();
            projectModules.forEach(module -> {
                if (!module.equals(ownerModule)) {
                    completionItems.add(getImportCompletion(module, module));
                }
            });
        }

        return completionItems;
    }
    
    private String getLangLibModuleNameInsertText(String pkgName) {
        return pkgName.replace(".", ".'") + ";";
    }

    private ArrayList<CompletionItem> getPackageNameCompletions(String orgName, List<BallerinaPackage> packagesList) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<String> pkgNameLabels = new ArrayList<>();

        packagesList.forEach(ballerinaPackage -> {
            String packageName = ballerinaPackage.getPackageName();
            String insertText;
            if (orgName.equals(ballerinaPackage.getOrgName()) && !pkgNameLabels.contains(packageName)) {
                if (orgName.equals(Names.BALLERINA_ORG.value)
                        && packageName.startsWith(Names.LANG.value + ".")) {
                    insertText = getLangLibModuleNameInsertText(packageName);
                } else {
                    insertText = packageName;
                }
                pkgNameLabels.add(packageName);
                // Do not add the semi colon at the end of the insert text since the user might type the as keyword
                completionItems.add(getImportCompletion(packageName, insertText));
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
    
    private static CompletionItem getAsKeyword() {
        CompletionItem item = new CompletionItem();
        item.setLabel("as");
        item.setInsertText("as ");
        item.setKind(CompletionItemKind.Keyword);
        item.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        
        return item;
    }
}
