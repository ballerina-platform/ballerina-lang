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

import com.google.common.collect.Lists;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.CompletionSearchProvider;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
            (6) import org/mod.<cursor>
            
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
            contextScope = ContextScope.AS_KW;
        } else if (node.orgName().isPresent()) {
            /*
            Covers case (4)
             */
            completionItems.addAll(this.moduleNameContextCompletions(ctx, node));
            contextScope = ContextScope.MODULE_NAME;
        } else {
            /*
            Covers cases (1) to (3)
             */
            completionItems.addAll(this.orgNameContextCompletions(ctx));
            contextScope = ContextScope.ORG_NAME;
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

        if (metaData[0] == ContextScope.MODULE_NAME) {
            String modulePart = null;
            // If the module name of the import declaration has more than one part (eg: abc.xyz), we take the module
            // part until the last dot (eg: xyz) and compare that with the label when sorting completion items.
            if (node.moduleName().size() > 1) {
                StringBuilder modName = new StringBuilder();
                for (int i = 0; i < node.moduleName().size() - 1; i++) {
                    modName.append(node.moduleName().get(i).text());
                    modName.append(".");
                }
                modulePart = modName.toString();
            }
            /*
            Sorts only the completions when the org name is ballerina
            Context covered:
            (1) import ballerina/<cursor
            (2) import ballerina/a<cursor
            (3) import ballerina/lang.<cursor>
             */
            for (LSCompletionItem completion : cItems) {
                CompletionItem cItem = completion.getCompletionItem();
                String label = cItem.getLabel();
                cItem.setSortText(genSortText(this.rankModuleName(label, modulePart)));
            }
            return;
        }

        if (metaData[0] == ContextScope.ORG_NAME) {
            for (LSCompletionItem completion : cItems) {
                CompletionItem cItem = completion.getCompletionItem();
                String label = cItem.getLabel();
                cItem.setSortText(genSortText(this.rankOrgName(label)));
            }
            return;
        }

        super.sort(context, node, cItems, metaData);
    }

    /**
     * Ranks the module name based on the it's ballerina module, whether it's a langlib or starts with the module part
     * user has already written.
     *
     * @param label      Module name
     * @param modulePart The module name user has already written
     * @return {@link Integer} rank
     */
    private int rankModuleName(String label, String modulePart) {
        // If there's a module part partially written, check if the label starts with that.
        if (modulePart != null && label.startsWith(modulePart)) {
            return 1;
        }
        if (label.startsWith(LANGLIB_MODULE_PREFIX)) {
            return 2;
        }
        if (label.startsWith(BALLERINA_MODULE_PREFIX)) {
            return 3;
        }

        return 4;
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
        Set<String> orgNames = new HashSet<>();
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<LSPackageLoader.ModuleInfo> moduleList =
                LSPackageLoader.getInstance(ctx.languageServercontext()).getAllVisiblePackages(ctx);
        moduleList.forEach(pkg -> {
            String orgName = pkg.packageOrg().value();
            String pkgName = pkg.packageName().value();
            if (orgName.equals(Names.BALLERINA_INTERNAL_ORG.getValue())
                    || ModuleUtil.matchingImportedModule(ctx, pkg).isPresent()) {
                // Avoid suggesting the ballerinai org name
                return;
            }
            List<String> pkgNameComps = Arrays.stream(pkgName.split("\\."))
                    .map(ModuleUtil::escapeModuleName)
                    .map(CommonUtil::escapeReservedKeyword)
                    .collect(Collectors.toList());
            String label = pkg.packageOrg().value().isEmpty() ? String.join(".", pkgNameComps)
                    : CommonUtil.getPackageLabel(pkg);
            String insertText = orgName.isEmpty() ? "" : orgName + Names.ORG_NAME_SEPARATOR.getValue();

            if (orgName.equals(Names.BALLERINA_ORG.value)
                    && pkgName.startsWith(PackageName.LANG_LIB_PACKAGE_NAME_PREFIX + Names.DOT.getValue())) {
                insertText += getLangLibModuleNameInsertText(pkgName);
            } else {
                insertText += orgName.isEmpty() ? label : pkgName;
            }
            // Do not add the semicolon with the insert text since the user should be allowed to use the as keyword
            LSCompletionItem fullPkgImport = getImportCompletion(ctx, label, insertText);
            completionItems.add(fullPkgImport);
            if (!orgName.isEmpty() && !orgNames.contains(orgName)) {
                LSCompletionItem orgNameImport =
                        getImportCompletion(ctx, orgName, (orgName + Names.ORG_NAME_SEPARATOR.getValue()));
                completionItems.add(orgNameImport);
                orgNames.add(orgName);
            }
        });

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
                    || ModuleUtil.matchingImportedModule(context, "", qualifiedModuleName).isPresent()) {
                return;
            }

            List<String> moduleNameParts = Lists.newArrayList(module.moduleName().moduleNamePart().split("\\."));
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
                                                                     ImportDeclarationNode node) {
        String orgName = node.orgName().get().orgName().text();
        List<TextEdit> additionalEdits = new ArrayList<>();
        // If the module name contains a dot, we should replace what's before the last dot to make sure a part
        // of the module name is not repeated.
        if (node.moduleName().size() > 1) {
            // There can be 2 cases:
            // 1) orgName/mod.xx<cursor>
            // 2) orgName/mod.<cursor>
            IdentifierToken lastModeNamePart = node.moduleName().get(node.moduleName().size() - 1);
            LinePosition startPos = node.orgName().get().lineRange().endLine();
            LinePosition endPos = lastModeNamePart.lineRange().endLine();
            if (!lastModeNamePart.isMissing()) {
                // Case (2) above
                endPos = lastModeNamePart.lineRange().startLine();
            }
            Range editRange = new Range(PositionUtil.toPosition(startPos), PositionUtil.toPosition(endPos));
            TextEdit removeModNameEdit = new TextEdit(editRange, "");
            additionalEdits.add(removeModNameEdit);
        }
        
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<String> addedPkgNames = new ArrayList<>();
        LanguageServerContext serverContext = context.languageServercontext();
        List<LSPackageLoader.ModuleInfo> moduleList;

        if (orgName.equals("ballerinax")) {
            List<String> packageList = new ArrayList<>();
            String prefix = node.moduleName().stream().filter(identifierToken -> !identifierToken.isMissing())
                    .map(IdentifierToken::text)
                    .collect(Collectors.joining("."));

            moduleList = LSPackageLoader.getInstance(serverContext).getCentralPackages();
            moduleList.forEach(ballerinaPackage -> packageList.add(ballerinaPackage.packageName().value()));
            List<String> filteredPackageNames = getFilteredPackages(packageList, prefix, context);
            for (String filteredPackage : filteredPackageNames) {
                LSCompletionItem completionItem = getImportCompletion(context, filteredPackage, filteredPackage);
                completionItem.getCompletionItem().setAdditionalTextEdits(additionalEdits);
                completionItems.add(completionItem);
            }
            return completionItems;
        }
        moduleList = LSPackageLoader.getInstance(serverContext).getAllVisiblePackages(context);
        moduleList.forEach(ballerinaPackage -> {
            String packageName = ballerinaPackage.packageName().value();
            String insertText;
            if (orgName.equals(ballerinaPackage.packageOrg().value()) && !addedPkgNames.contains(packageName)
                    && ModuleUtil.matchingImportedModule(context, ballerinaPackage).isEmpty()) {
                if (orgName.equals(Names.BALLERINA_ORG.value)
                        && packageName.startsWith(Names.LANG.getValue() + Names.DOT.getValue())) {
                    insertText = getLangLibModuleNameInsertText(packageName);
                } else {
                    insertText = packageName;
                }
                addedPkgNames.add(packageName);
                // Do not add the semicolon at the end of the insert text since the user might type the as keyword
                LSCompletionItem completionItem = getImportCompletion(context, packageName, insertText);
                completionItem.getCompletionItem().setAdditionalTextEdits(additionalEdits);
                completionItems.add(completionItem);
            }
        });

        return completionItems;
    }


    private static List<String> getFilteredPackages(List<String> packageList, String prefix,
                                                   BallerinaCompletionContext context) {
        CompletionSearchProvider completionSearchProvider = CompletionSearchProvider
                .getInstance(context.languageServercontext());
        completionSearchProvider.indexNames(packageList);
        return completionSearchProvider.getSuggestions(prefix);
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
        AS_KW,
        MODULE_NAME,
        ORG_NAME,
        OTHER
    }
}
