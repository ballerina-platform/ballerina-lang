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
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.completions.providers.context.util.ImportDeclarationContextUtil.getImportCompletion;
import static org.ballerinalang.langserver.completions.providers.context.util.ImportDeclarationContextUtil.getLangLibModuleNameInsertText;
import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortText;

/**
 * Completion provider for {@link ImportDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ImportDeclarationNodeContext extends AbstractCompletionProvider<ImportDeclarationNode> {

    private static final String LANGLIB_MODULE_PREFIX = Names.BALLERINA_ORG.getValue()
            + Names.ORG_NAME_SEPARATOR.getValue() + Names.LANG.getValue() + Names.DOT.getValue();
    private static final String BALLERINA_MODULE_PREFIX = Names.BALLERINA_ORG.getValue()
            + Names.ORG_NAME_SEPARATOR.getValue();

    public ImportDeclarationNodeContext() {
        super(ImportDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, ImportDeclarationNode node) {
        /*
        Following use cases are addressed.
        Eg: (1) import <cursor>
            (2) import b<cursor>
            (3) import abc.xy<cursor>
            (4) import org/mod<cursor>
            (5) import org/mod v<cursor>
            
            Suggests org names and the module names within the same directory
         */
        List<IdentifierToken> moduleName = node.moduleName().stream()
                .filter(token -> !token.isMissing())
                .collect(Collectors.toList());

        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        ContextScope contextScope;

        if (this.onPrefixContext(ctx, node)) {
            return Collections.emptyList();
        }

        if (onSuggestCurrentProjectModules(ctx, node, moduleName)) {
            completionItems.addAll(this.getCurrentProjectModules(ctx, moduleName));
            contextScope = ContextScope.OTHER;
        } else if (onSuggestAsKeyword(ctx, node)) {
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_AS.get()));
            contextScope = ContextScope.SCOPE1;
        } else if (node.orgName().isPresent()) {
            /*
            Covers case (4)
             */
            String orgName = node.orgName().get().orgName().text();
            completionItems.addAll(this.moduleNameContextCompletions(ctx, orgName));
            contextScope = ContextScope.SCOPE2;
        } else {
            /*
            Covers cases (1) to (3)
             */
            completionItems.addAll(this.orgNameContextCompletions(ctx));
            contextScope = ContextScope.SCOPE3;
        }

        this.sort(ctx, node, completionItems, contextScope);
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context,
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
        if (label.startsWith(LANGLIB_MODULE_PREFIX)) {
            return 1;
        }
        if (label.startsWith(BALLERINA_MODULE_PREFIX)) {
            return 2;
        }

        return 3;
    }

    private int rankOrgName(String label) {
        if (!label.contains(Names.ORG_NAME_SEPARATOR.getValue()) && !label.equals(Names.BALLERINA_ORG.getValue())) {
            // This is a module under the current project
            return 1;
        }
        if (!label.contains(Names.ORG_NAME_SEPARATOR.getValue())) {
            return 2;
        }
        if (label.startsWith(Names.BALLERINA_ORG.getValue() + Names.ORG_NAME_SEPARATOR.getValue())) {
            return 3;
        }
        if (label.startsWith(Names.BALLERINA_ORG.getValue()
                + Names.ORG_NAME_SEPARATOR.getValue() + Names.LANG.getValue() + Names.DOT.getValue())) {
            return 4;
        }

        return 5;
    }

    private ArrayList<LSCompletionItem> orgNameContextCompletions(BallerinaCompletionContext ctx) {
        List<String> orgNames = new ArrayList<>();
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<Package> pkgList = LSPackageLoader.getInstance(ctx.languageServercontext()).getDistributionRepoPackages();

        pkgList.forEach(pkg -> {
            String orgName = pkg.packageOrg().value();
            String pkgName = pkg.packageName().value();
            if (orgName.equals(Names.BALLERINA_INTERNAL_ORG.getValue())
                    || CommonUtil.matchingImportedModule(ctx, pkg).isPresent()) {
                // Avoid suggesting the ballerinai org name
                return;
            }
            String fullPkgNameLabel = orgName + Names.ORG_NAME_SEPARATOR.getValue() + pkgName;
            String insertText = orgName + Names.ORG_NAME_SEPARATOR.getValue();
            if (orgName.equals(Names.BALLERINA_ORG.value)
                    && pkgName.startsWith(PackageName.LANG_LIB_PACKAGE_NAME_PREFIX + Names.DOT.getValue())) {
                insertText += getLangLibModuleNameInsertText(pkgName);
            } else {
                insertText += pkgName;
            }
            // Do not add the semicolon with the insert text since the user should be allowed to use the as keyword
            LSCompletionItem fullPkgImport = getImportCompletion(ctx, fullPkgNameLabel, insertText);
            completionItems.add(fullPkgImport);
            if (!orgNames.contains(orgName)) {
                LSCompletionItem orgNameImport =
                        getImportCompletion(ctx, orgName, (orgName + Names.ORG_NAME_SEPARATOR.getValue()));
                completionItems.add(orgNameImport);
                orgNames.add(orgName);
            }
        });
        
        /*
        If within a project, suggest the project modules except the owner module of the file which being processed
         */
        Optional<Project> currentProject = ctx.workspace().project(ctx.filePath());
        if (currentProject.isEmpty() || currentProject.get().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            return completionItems;
        }

        Package currentPackage = currentProject.get().currentPackage();
        String currentPackageName = CommonUtil.escapeReservedKeyword(currentPackage.packageName().value());

        completionItems.add(getImportCompletion(ctx, currentPackageName, currentPackageName + "."));
        Optional<Module> currentModule = ctx.currentModule();
        for (Module module : currentPackage.modules()) {
            if (module.isDefaultModule()
                    || (currentModule.isPresent() && module.moduleId().equals(currentModule.get().moduleId()))) {
                continue;
            }
            String moduleNamePart = Arrays.stream(module.moduleName().moduleNamePart().split("\\."))
                    .map(CommonUtil::escapeReservedKeyword)
                    .collect(Collectors.joining("."));
            String qualifiedModuleName = module.moduleName().packageName().value()
                    + Names.DOT + moduleNamePart;
            if (CommonUtil.matchingImportedModule(ctx, "", qualifiedModuleName).isPresent()) {
                continue;
            }
            String label = currentPackageName + "." + moduleNamePart;
            completionItems.add(getImportCompletion(ctx, label, label));
        }

        return completionItems;
    }

    private List<LSCompletionItem> getCurrentProjectModules(BallerinaCompletionContext context,
                                                            List<IdentifierToken> moduleName) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        String pkgName = context.workspace().module(context.filePath()).get().packageInstance().packageName().value();
        /*
        In cases where the names are reserved keywords, then they are escaped. When comparing we need to remove ' char.
         */
        List<String> modNameString = moduleName.stream()
                .map(token -> token.text().replace("'", ""))
                .collect(Collectors.toList());
        Optional<Project> currentProject = context.workspace().project(context.filePath());
        if (currentProject.isEmpty() || currentProject.get().kind() == ProjectKind.SINGLE_FILE_PROJECT
                || !modNameString.get(0).equals(pkgName)) {
            return completionItems;
        }

        Optional<Module> currentModule = context.currentModule();
        currentProject.get().currentPackage().modules().forEach(module -> {
            String qualifiedModuleName = module.moduleName().packageName().toString() + Names.DOT
                    + module.moduleName().moduleNamePart();
            if (module.isDefaultModule()
                    || (currentModule.isPresent() && module.moduleId().equals(currentModule.get().moduleId()))
                    || CommonUtil.matchingImportedModule(context, "", qualifiedModuleName).isPresent()) {
                return;
            }

            List<String> moduleNameParts = Arrays.asList(module.moduleName().moduleNamePart().split("\\."));
            moduleName.forEach(token -> moduleNameParts.remove(token.text()));
            String label = module.moduleName().moduleNamePart();
            String insertText = moduleNameParts.stream()
                    .map(CommonUtil::escapeReservedKeyword)
                    .collect(Collectors.joining("."));

            completionItems.add(getImportCompletion(context, label, insertText));
        });

        return completionItems;
    }

    private ArrayList<LSCompletionItem> moduleNameContextCompletions(BallerinaCompletionContext context,
                                                                     String orgName) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<String> addedPkgNames = new ArrayList<>();
        LanguageServerContext serverContext = context.languageServercontext();
        List<Package> packageList = LSPackageLoader.getInstance(serverContext).getDistributionRepoPackages();
        packageList.forEach(ballerinaPackage -> {
            String packageName = ballerinaPackage.packageName().value();
            String insertText;
            if (orgName.equals(ballerinaPackage.packageOrg().value()) && !addedPkgNames.contains(packageName)
                    && CommonUtil.matchingImportedModule(context, ballerinaPackage).isEmpty()) {
                if (orgName.equals(Names.BALLERINA_ORG.value)
                        && packageName.startsWith(Names.LANG.getValue() + Names.DOT.getValue())) {
                    insertText = getLangLibModuleNameInsertText(packageName);
                } else {
                    insertText = packageName;
                }
                addedPkgNames.add(packageName);
                // Do not add the semi colon at the end of the insert text since the user might type the as keyword
                completionItems.add(getImportCompletion(context, packageName, insertText));
            }
        });

        return completionItems;
    }

    private boolean onSuggestAsKeyword(BallerinaCompletionContext context, ImportDeclarationNode node) {
        SeparatedNodeList<IdentifierToken> moduleName = node.moduleName();

        if (moduleName.isEmpty() || moduleName.get(moduleName.size() - 1).isMissing()) {
            /*
            Is missing part added in order to avoid falsely identifying the following as true within this method
            import <cursor>
             */
            return false;
        }
        int moduleNameEnd = moduleName.get(moduleName.size() - 1).textRange().endOffset();
        int cursor = context.getCursorPositionInTree();

        /*
        The as keyword missing check is added to satisfy the following case where the prefix exist while the
        as keyword being empty
        eg:
        (1) import alpha3_packagex.mod2 <cursor>
        (2) import alpha3_packagex.mod2 a<cursor>
         */
        return (node.prefix().isEmpty() || node.prefix().get().asKeyword().isMissing())
                && cursor > moduleNameEnd;
    }

    private boolean onPrefixContext(BallerinaCompletionContext context, ImportDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Optional<ImportPrefixNode> prefix = node.prefix();

        return prefix.isPresent() && !prefix.get().asKeyword().isMissing() &&
                cursor > prefix.get().asKeyword().textRange().endOffset();
    }

    private boolean onSuggestCurrentProjectModules(BallerinaCompletionContext context, ImportDeclarationNode node,
                                                   List<IdentifierToken> moduleNameComponents) {
        Optional<Module> module = context.currentModule();
        /*
        Either the module current module is not present or does not satisfy the following cases, we returns false
        (1) import pkgname.
        (2) import pkg.m
         */
        int cursor = context.getCursorPositionInTree();
        return !moduleNameComponents.isEmpty()
                && module.isPresent() && (moduleNameComponents.size() >= 2 || node.moduleName().separatorSize() != 0)
                && node.orgName().isEmpty()
                && moduleNameComponents.get(moduleNameComponents.size() - 1).textRange().endOffset() <= cursor;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ImportDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token semicolon = node.semicolon();
        if (semicolon.isMissing()) {
            return true;
        }
        
        return cursor < semicolon.textRange().endOffset();
    }

    private enum ContextScope {
        SCOPE1,
        SCOPE2,
        SCOPE3,
        OTHER
    }
}
