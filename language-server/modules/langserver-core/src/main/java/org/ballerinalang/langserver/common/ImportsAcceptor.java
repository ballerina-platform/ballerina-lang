/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.common;

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.projects.Project;
import org.ballerinalang.langserver.codeaction.CodeActionModuleId;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This class provides imports acceptor and its functionalities.
 *
 * @since 1.3.0
 */
public class ImportsAcceptor {

    private final Set<String> newImports;
    private final Map<ImportDeclarationNode, ModuleSymbol> currentModuleImportsMap;
    private final BiConsumer<String, CodeActionModuleId> onExistCallback;

    public ImportsAcceptor(DocumentServiceContext context) {
        this(context, null);
    }

    public ImportsAcceptor(DocumentServiceContext context, BiConsumer<String, CodeActionModuleId> onExistCallback) {
        this.newImports = new HashSet<>();
        this.currentModuleImportsMap = context.currentDocImportsMap();
        this.onExistCallback = onExistCallback;
    }

    /**
     * Return imports acceptor.
     *
     * @return Returns imports acceptor
     */
    public BiConsumer<String, CodeActionModuleId> getAcceptor(DocumentServiceContext context) {
        Optional<Project> project = context.workspace().project(context.filePath());
        //The project should not be empty.
        String currentPkgName = CommonUtil.escapeReservedKeyword(project.get().currentPackage().packageName().value());
        String currentOrgName = CommonUtil.escapeReservedKeyword(project.get().currentPackage().packageOrg().value());

        return (orgName, codeActionModuleId) -> {
            boolean notFound = currentModuleImportsMap.keySet().stream().noneMatch(
                    pkg -> {
                        String importAlias = pkg.moduleName().stream()
                                .map(identifierToken -> CommonUtil.escapeReservedKeyword(identifierToken.text()))
                                .collect(Collectors.joining("."));
                        boolean isCurrentPkgModule = pkg.orgName().isEmpty()
                                && importAlias.startsWith(currentPkgName + ".");
                        boolean aliasMatched = importAlias.equals(codeActionModuleId.moduleName());
                        return (isCurrentPkgModule
                                || (pkg.orgName().get().orgName().text().equals(orgName))) && aliasMatched;
                    }
            );
            if (notFound) {
                String pkgName;

                String moduleName = codeActionModuleId.moduleName();
                String modulePrefix = ModuleUtil.escapeModuleName(moduleName).replaceAll(".*\\.", "");
                if (!codeActionModuleId.modulePrefix().isEmpty() &&
                        !modulePrefix.equals(codeActionModuleId.modulePrefix())) {
                    moduleName = codeActionModuleId.moduleName() + " as " + codeActionModuleId.modulePrefix();
                }

                pkgName = orgName.isEmpty() || orgName.equals(currentOrgName) ?
                        moduleName : orgName + "/" + moduleName;
                newImports.add(pkgName);
                if (onExistCallback != null) {
                    onExistCallback.accept(orgName, codeActionModuleId);
                }
            }
        };
    }

    /**
     * Return new import text edits.
     *
     * @return a list of {@link TextEdit}
     */
    public List<TextEdit> getNewImportTextEdits() {
        List<TextEdit> edits = new ArrayList<>();
        newImports.forEach(i -> edits.add(createImportTextEdit(i)));
        return edits;
    }

    /**
     * Return set of new imports.
     *
     * @return a set of  {@link String}
     */
    public Set<String> getNewImports() {
        return newImports;
    }

    private TextEdit createImportTextEdit(String pkgName) {
        Optional<ImportDeclarationNode> lastImport =
                CommonUtil.getLastItem(new ArrayList<>(currentModuleImportsMap.keySet()));

        int endCol = 0;
        int endLine = lastImport.isEmpty() ? 0 : lastImport.get().location().lineRange().endLine().line();

        String editText = String.format("%s %s;%n", ItemResolverConstants.IMPORT, pkgName);
        Range range = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        return new TextEdit(range, editText);
    }
}
