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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
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
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.IMPORT_RESOLVING_ERROR;

/**
 * Import resolver implementation to capture imported modules within a given user expression (syntax node).
 *
 * @since 2.0.0
 */
public class EvaluationImportResolver extends NodeVisitor {

    private final SuspendedContext context;
    private SemanticModel semanticModel;
    private final Map<String, ModuleSymbol> capturedImports = new HashMap<>();
    private final Map<String, String> importAliasesMap = new HashMap<>();
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
    public Map<String, ModuleSymbol> getImportedModuleMap(ExpressionNode expressionNode) throws EvaluationException {
        loadImportAliasesMap();
        expressionNode.accept(this);
        if (!capturedErrors.isEmpty()) {
            throw capturedErrors.get(0);
        }
        return capturedImports;
    }

    @Override
    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        String modulePrefix = qualifiedNameReferenceNode.modulePrefix().text().trim();
        String moduleName = importAliasesMap.getOrDefault(modulePrefix, modulePrefix);

        List<Symbol> visibleSymbols = getSemanticModel().visibleSymbols(context.getDocument(), LinePosition
                .from(context.getLineNumber(), 0));
        List<ModuleSymbol> matchingModuleSymbols = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.MODULE && symbol.getName().get().equals(moduleName))
                .map(symbol -> ((ModuleSymbol) symbol))
                .collect(Collectors.toList());

        if (matchingModuleSymbols.isEmpty()) {
            capturedErrors.add(createEvaluationException(IMPORT_RESOLVING_ERROR, moduleName));
            return;
        } else if (matchingModuleSymbols.size() > 1) {
            capturedErrors.add(createEvaluationException("Multiple modules were resolved for the import'" + moduleName
                    + "'"));
            return;
        }

        capturedImports.put(moduleName, matchingModuleSymbols.get(0));
    }

    private SemanticModel getSemanticModel() {
        if (semanticModel == null) {
            semanticModel = context.getDebugCompiler().getSemanticInfo();
        }
        return semanticModel;
    }

    private void loadImportAliasesMap() {
        SyntaxTree documentTree = context.getDocument().syntaxTree();
        if (!documentTree.containsModulePart()) {
            return;
        }
        NodeList<ImportDeclarationNode> imports = ((ModulePartNode) documentTree.rootNode()).imports();
        for (ImportDeclarationNode importNode : imports) {
            if (importNode.orgName().isEmpty()) {
                continue;
            }
            if (importNode.prefix().isPresent()) {
                String prefix = importNode.prefix().get().prefix().text().trim();
                importAliasesMap.put(prefix, importNode.orgName().get().toSourceCode() + importNode.moduleName());
            }
        }
    }
}
