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
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
    public List<LSCompletionItem> getCompletions(LSContext ctx, ImportDeclarationNode node) {
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
        List<BallerinaPackage> packagesList = new ArrayList<>();
        Stream.of(LSPackageLoader.getSdkPackages(), LSPackageLoader.getHomeRepoPackages())
                .forEach(packagesList::addAll);

        if (withinVersionAndPrefix(ctx, node)) {
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_AS.get()));
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_VERSION.get()));
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
    public void sort(LSContext context, ImportDeclarationNode node, List<LSCompletionItem> cItems, Object... metaData) {
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

    private ArrayList<LSCompletionItem> orgNameContextCompletions(List<BallerinaPackage> packagesList, LSContext ctx) {
        List<String> orgNames = new ArrayList<>();
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();

        packagesList.forEach(pkg -> {
            if (isAnnotationLangLib(pkg)) {
                return;
            }
            String fullPkgNameLabel = pkg.getOrgName() + "/" + pkg.getPackageName();
            String insertText = pkg.getOrgName() + "/";
            if (pkg.getOrgName().equals(Names.BALLERINA_ORG.value)
                    && pkg.getPackageName().startsWith(Names.LANG.value + ".")) {
                insertText += getLangLibModuleNameInsertText(pkg.getPackageName());
            } else {
                insertText += pkg.getPackageName();
            }
            // Do not add the semicolon with the insert text since the user should be allowed to use the as keyword
            LSCompletionItem fullPkgImport = getImportCompletion(ctx, fullPkgNameLabel, insertText);
            completionItems.add(fullPkgImport);
            if (!orgNames.contains(pkg.getOrgName())) {
                LSCompletionItem orgNameImport = getImportCompletion(ctx, pkg.getOrgName(), (pkg.getOrgName() + "/"));
                completionItems.add(orgNameImport);
                orgNames.add(pkg.getOrgName());
            }
        });

        /*
        If within a project, suggest the project modules except the owner module of the file which being processed
         */
        LSDocumentIdentifier lsDocument = ctx.get(DocumentServiceKeys.LS_DOCUMENT_KEY);
        if (lsDocument != null && lsDocument.isWithinProject()) {
            String ownerModule = lsDocument.getOwnerModule();
            List<String> projectModules = lsDocument.getProjectModules();
            projectModules.forEach(module -> {
                if (!module.equals(ownerModule)) {
                    completionItems.add(getImportCompletion(ctx, module, module));
                }
            });
        }

        return completionItems;
    }

    private String getLangLibModuleNameInsertText(String pkgName) {
        return pkgName.replace(".", ".'") + ";";
    }

    private ArrayList<LSCompletionItem> moduleNameContextCompletions(LSContext context, String orgName,
                                                                     List<BallerinaPackage> packagesList) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<String> pkgNameLabels = new ArrayList<>();

        packagesList.forEach(ballerinaPackage -> {
            if (isAnnotationLangLib(ballerinaPackage)) {
                return;
            }
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
                completionItems.add(getImportCompletion(context, packageName, insertText));
            }
        });

        return completionItems;
    }

    private static LSCompletionItem getImportCompletion(LSContext context, String label, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setKind(CompletionItemKind.Module);
        item.setDetail(ItemResolverConstants.MODULE_TYPE);

        return new StaticCompletionItem(context, item, StaticCompletionItem.Kind.MODULE);
    }

    private static boolean withinVersionAndPrefix(LSContext context, ImportDeclarationNode node) {
        SeparatedNodeList<IdentifierToken> moduleName = node.moduleName();
        if (moduleName.isEmpty()) {
            return false;
        }
        Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        IdentifierToken endModuleNameComponent;
        if (moduleName.separatorSize() > 0 && moduleName.getSeparator(moduleName.separatorSize() - 1).isMissing()) {
            endModuleNameComponent = moduleName.get(moduleName.size() - 2);
        } else if (moduleName.separatorSize() == 0) {
            endModuleNameComponent = moduleName.get(moduleName.size() - 1);
        } else {
            return false;
        }

        return !endModuleNameComponent.isMissing()
                && endModuleNameComponent.lineRange().endLine().offset() < cursor.getCharacter();
    }

    private boolean isAnnotationLangLib(BallerinaPackage ballerinaPackage) {
        return "ballerina".equals(ballerinaPackage.getOrgName())
                && "lang.annotations".equals(ballerinaPackage.getPackageName());
    }

    private enum ContextScope {
        SCOPE1,
        SCOPE2,
        SCOPE3,
        OTHER
    }
}
