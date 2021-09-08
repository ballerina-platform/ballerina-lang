/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation;

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.debugadapter.SuspendedContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.IMPORT_RESOLVING_ERROR;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.IdentifierType.OTHER;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeIdentifier;

/**
 * Import resolver implementation to capture imported modules within a given user expression (syntax node).
 *
 * @since 2.0.0
 */
public class EvaluationImportResolver extends NodeVisitor {

    private final SuspendedContext context;
    private final Map<String, ModuleSymbol> capturedImports = new HashMap<>();
    private final Map<String, BImport> visibleImports = new HashMap<>();
    private final List<ModuleSymbol> visibleModuleSymbols = new ArrayList<>();
    private final List<EvaluationException> capturedErrors = new ArrayList<>();

    EvaluationImportResolver(SuspendedContext context) {
        this.context = context;
    }

    /**
     * Returns a map of detected module symbols within the given expression node, against their module prefixes.
     *
     * @param expressionNode syntax node of the expression
     * @return a map of detected module symbols within the given expression node, against their module prefixes
     */
    public Map<String, ModuleSymbol> detectImportedModules(ExpressionNode expressionNode) throws EvaluationException {
        loadVisibleImports();
        loadVisibleModuleSymbols();
        expressionNode.accept(this);
        if (!capturedErrors.isEmpty()) {
            throw capturedErrors.get(0);
        }
        return capturedImports;
    }

    @Override
    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        String modulePrefix = qualifiedNameReferenceNode.modulePrefix().text().trim();
        if (!visibleImports.containsKey(modulePrefix)) {
            capturedErrors.add(createEvaluationException(IMPORT_RESOLVING_ERROR, modulePrefix));
            return;
        }

        BImport bImport = visibleImports.get(modulePrefix);
        List<ModuleSymbol> matchingModuleSymbols = visibleModuleSymbols.stream().filter(moduleSymbol ->
                moduleSymbol.id().orgName().equals(bImport.orgName())
                        && moduleSymbol.id().moduleName().equals(bImport.moduleName()))
                .collect(Collectors.toList());

        if (matchingModuleSymbols.isEmpty()) {
            capturedErrors.add(createEvaluationException(IMPORT_RESOLVING_ERROR, modulePrefix));
            return;
        } else if (matchingModuleSymbols.size() > 1) {
            capturedErrors.add(createEvaluationException("Multiple modules found for import '" + modulePrefix + "'"));
            return;
        }

        capturedImports.put(bImport.alias(), matchingModuleSymbols.get(0));
    }

    private void loadVisibleImports() {
        if (!this.visibleImports.isEmpty()) {
            return;
        }
        SyntaxTree documentTree = context.getDocument().syntaxTree();
        if (!documentTree.containsModulePart()) {
            return;
        }

        NodeList<ImportDeclarationNode> imports = ((ModulePartNode) documentTree.rootNode()).imports();
        for (ImportDeclarationNode importNode : imports) {
            Optional<String> orgName = resolveImportOrgName(importNode);
            Optional<String> moduleName = importNode.moduleName().stream()
                    .map(identifierToken -> encodeIdentifier(identifierToken.text().trim(), OTHER))
                    .reduce((s1, s2) -> s1 + "." + s2);

            if (orgName.isPresent() && moduleName.isPresent()) {
                String importAlias = resolveImportAlias(importNode, moduleName.get());
                BImport bImport = new BImport(orgName.get(), moduleName.get(), importAlias);
                visibleImports.put(importAlias, bImport);
            }
        }
    }

    private void loadVisibleModuleSymbols() {
        if (!this.visibleModuleSymbols.isEmpty()) {
            return;
        }
        visibleModuleSymbols.addAll(context.getSemanticInfo().visibleSymbols(context.getDocument(), LinePosition
                .from(context.getLineNumber(), 0))
                .stream()
                .filter(symbol -> symbol.kind() == SymbolKind.MODULE)
                .map(symbol -> ((ModuleSymbol) symbol))
                .collect(Collectors.toList()));
    }

    private Optional<String> resolveImportOrgName(ImportDeclarationNode importNode) {
        if (importNode.orgName().isPresent()) {
            return Optional.of(importNode.orgName().get().orgName().text().trim());
        } else {
            return context.getPackageOrg();
        }
    }

    private String resolveImportAlias(ImportDeclarationNode importNode, String moduleName) {
        if (importNode.prefix().isPresent()) {
            return importNode.prefix().get().prefix().text().trim();
        } else {
            String[] moduleNameParts = moduleName.split("\\.");
            return moduleNameParts[moduleNameParts.length - 1];
        }
    }

    /**
     * Helper class to hold information of resolved imports.
     */
    private static class BImport {

        private final String orgName;
        private final String moduleName;
        private final String alias;

        BImport(String orgName, String moduleName, String alias) {
            this.orgName = orgName;
            this.moduleName = moduleName;
            this.alias = alias;
        }

        public String orgName() {
            return orgName;
        }

        public String moduleName() {
            return moduleName;
        }

        public String alias() {
            return alias;
        }
    }
}
