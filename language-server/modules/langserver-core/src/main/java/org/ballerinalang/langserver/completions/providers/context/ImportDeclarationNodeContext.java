/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.projects.Package;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortText;

/**
 * Completion provider for {@link ImportDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ImportDeclarationNodeContext extends AbstractCompletionProvider<ImportDeclarationNode> {

    public ImportDeclarationNodeContext() {
        super(ImportDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(CompletionContext ctx, ImportDeclarationNode node) {
        /*
        Following use cases are addressed.
        Eg: (1) import <cursor>
            (2) import b<cursor>
            (3) import abc.xy<cursor>
            (4) import org/mod<cursor>
            (5) import org/mod v<cursor>
            
            Suggests org names and the module names within the same directory
         */
        SeparatedNodeList<IdentifierToken> moduleName = node.moduleName();

        if (moduleName.isEmpty()) {
            throw new AssertionError("ModuleName cannot be empty");
        }

//        StringJoiner moduleNameJoiner = new StringJoiner(".");
//        for (IdentifierToken nameComponent : moduleName) {
//            if (nameComponent.isMissing()) {
//                break;
//            }
//            moduleNameJoiner.add(nameComponent.text());
//        }

        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        ContextScope contextScope;
        List<Package> packagesList = new ArrayList<>(LSPackageLoader.getDistributionRepoPackages());

        if (this.onPrefixContext(ctx, node)) {
            return Collections.emptyList();
        }

        if (onSuggestAsKeyword(ctx, node)) {
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_AS.get()));
            contextScope = ContextScope.SCOPE1;
        } else if (node.orgName().isPresent()) {
            /*
            Covers case (4)
             */
            String orgName = node.orgName().get().orgName().text();
            completionItems.addAll(this.moduleNameContextCompletions(ctx, orgName, packagesList));
            contextScope = ContextScope.SCOPE2;
        } else {
            /*
            Covers cases (1) to (3)
             */
            completionItems.addAll(this.orgNameContextCompletions(packagesList, ctx));
            contextScope = ContextScope.SCOPE3;
        }

        this.sort(ctx, node, completionItems, contextScope);
        return completionItems;
    }

    @Override
    public void sort(CompletionContext context,
                     ImportDeclarationNode node,
                     List<LSCompletionItem> cItems,
                     Object... metaData) {
        if (metaData.length == 0 || !(metaData[0] instanceof ContextScope)) {
            super.sort(context, node, cItems, metaData);
            return;
        }

        if (metaData[0] == ContextScope.SCOPE2) {
            /*
            Sorts only the completions when the org name is ballerina
            Context covered:
            (1) import ballerina/<cursor
            (2) import ballerina/a<cursor
             */
            for (LSCompletionItem completion : cItems) {
                CompletionItem cItem = completion.getCompletionItem();
                String label = cItem.getLabel();
                cItem.setSortText(genSortText(this.rankModuleName(label)));
            }
            return;
        }

        if (metaData[0] == ContextScope.SCOPE3) {
            for (LSCompletionItem completion : cItems) {
                CompletionItem cItem = completion.getCompletionItem();
                String label = cItem.getLabel();
                cItem.setSortText(genSortText(this.rankOrgName(label)));
            }
            return;
        }

        super.sort(context, node, cItems, metaData);
    }

    private int rankModuleName(String label) {
        if (label.startsWith("ballerina/lang.")) {
            return 2;
        }
        if (label.startsWith("ballerina/")) {
            return 1;
        }

        return 3;
    }

    private int rankOrgName(String label) {
        if (!label.contains("/") && !label.equals("ballerina")) {
            // This is a module under the current project
            return 1;
        }
        if (!label.contains("/")) {
            return 2;
        }
        if (label.startsWith("ballerina/lang.")) {
            return 4;
        }
        if (label.startsWith("ballerina/")) {
            return 3;
        }

        return 5;
    }

    private ArrayList<LSCompletionItem> orgNameContextCompletions(List<Package> packagesList,
                                                                  CompletionContext ctx) {
        List<String> orgNames = new ArrayList<>();
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();

        packagesList.forEach(pkg -> {
            if (isPreDeclaredLangLib(pkg)) {
                return;
            }
            String orgName = pkg.packageOrg().value();
            String pkgName = pkg.packageName().value();
            String fullPkgNameLabel = orgName + "/" + pkgName;
            String insertText = orgName + "/";
            if (orgName.equals(Names.BALLERINA_ORG.value)
                    && pkgName.startsWith(Names.LANG.value + ".")) {
                insertText += getLangLibModuleNameInsertText(pkgName);
            } else {
                insertText += pkgName;
            }
            // Do not add the semicolon with the insert text since the user should be allowed to use the as keyword
            LSCompletionItem fullPkgImport = getImportCompletion(ctx, fullPkgNameLabel, insertText);
            completionItems.add(fullPkgImport);
            if (!orgNames.contains(orgName)) {
                LSCompletionItem orgNameImport = getImportCompletion(ctx, orgName, (orgName + "/"));
                completionItems.add(orgNameImport);
                orgNames.add(orgName);
            }
        });

        /*
        If within a project, suggest the project modules except the owner module of the file which being processed
         */
        // TODO: Revamp
//        LSDocumentIdentifier lsDocument = ctx.get(DocumentServiceKeys.LS_DOCUMENT_KEY);
//        if (lsDocument != null && lsDocument.isWithinProject()) {
//            String ownerModule = lsDocument.getOwnerModule();
//            List<String> projectModules = lsDocument.getProjectModules();
//            projectModules.forEach(module -> {
//                if (!module.equals(ownerModule)) {
//                    completionItems.add(getImportCompletion(ctx, module, module));
//                }
//            });
//        }

        return completionItems;
    }

    private String getLangLibModuleNameInsertText(String pkgName) {
        return pkgName.replace(".", ".'") + ";";
    }

    private ArrayList<LSCompletionItem> moduleNameContextCompletions(CompletionContext context, String orgName,
                                                                     List<Package> packagesList) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<String> pkgNameLabels = new ArrayList<>();

        packagesList.forEach(ballerinaPackage -> {
            if (isPreDeclaredLangLib(ballerinaPackage)) {
                return;
            }
            String packageName = ballerinaPackage.packageName().value();
            String insertText;
            if (orgName.equals(ballerinaPackage.packageOrg().value()) && !pkgNameLabels.contains(packageName)) {
                if (orgName.equals(Names.BALLERINA_ORG.value)
                        && packageName.startsWith(Names.LANG.value + ".")) {
                    insertText = getLangLibModuleNameInsertText(packageName);
                } else {
                    insertText = packageName;
                }
                pkgNameLabels.add(packageName);
                // Do not add the semi colon at the end of the insert text since the user might type the as keyword
                completionItems.add(getImportCompletion(context, packageName, insertText));
            }
        });

        return completionItems;
    }

    private static LSCompletionItem getImportCompletion(CompletionContext context, String label, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setKind(CompletionItemKind.Module);
        item.setDetail(ItemResolverConstants.MODULE_TYPE);

        return new StaticCompletionItem(context, item, StaticCompletionItem.Kind.MODULE);
    }

    private boolean onSuggestAsKeyword(CompletionContext context, ImportDeclarationNode node) {
        SeparatedNodeList<IdentifierToken> moduleName = node.moduleName();

        if (moduleName.isEmpty() || moduleName.get(moduleName.size() - 1).isMissing()) {
            /*
            Is missing part added in order to avoid falsely identifying the following as true within this method
            import <cursor>
             */
            return false;
        }
        int moduleNameEnd = moduleName.get(moduleName.size() - 1).lineRange().endLine().offset();
        int cursor = context.getCursorPositionInTree();

        return node.prefix().isEmpty() && cursor > moduleNameEnd;
    }

    private boolean onPrefixContext(CompletionContext context, ImportDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Optional<ImportPrefixNode> prefix = node.prefix();

        return prefix.isPresent() && cursor > prefix.get().asKeyword().lineRange().endLine().offset();
    }

    private enum ContextScope {
        SCOPE1,
        SCOPE2,
        SCOPE3,
        OTHER
    }
}
