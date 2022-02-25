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
package org.ballerinalang.langserver.extensions.ballerina.document.visitor;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.extensions.ballerina.document.ASTModification;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Common node visitor to override and remove assertion errors from NodeVisitor methods.
 */
public class UnusedSymbolsVisitor extends NodeVisitor {

    private Map<String, ImportDeclarationNode> unusedImports = new HashMap<>();
    private Map<String, ImportDeclarationNode> usedImports = new HashMap<>();
    private Map<LineRange, ASTModification> deleteRanges;
    private Map<LineRange, ASTModification> toBeDeletedRanges = new HashMap<>();
    private SemanticModel semanticModel;
    private Document srcFile;

    public UnusedSymbolsVisitor(Document srcFile, SemanticModel semanticModel,
                                Map<LineRange, ASTModification> deleteRanges) {
        this.semanticModel = semanticModel;
        this.deleteRanges = deleteRanges;
        this.toBeDeletedRanges.putAll(deleteRanges);
        this.srcFile = srcFile;
    }

    private void addUnusedImportNode(ImportDeclarationNode importDeclarationNode) {
        unusedImports.put(getImportModuleName(importDeclarationNode.orgName().isPresent()
                        ? importDeclarationNode.orgName().get() : null, importDeclarationNode.moduleName()),
                importDeclarationNode);
    }

    private String getImportModuleName(ImportOrgNameNode importOrgNameNode,
                                       SeparatedNodeList<IdentifierToken> importModuleName) {
        StringBuilder moduleName = new StringBuilder();
        if (importOrgNameNode != null) {
            String importOrgName = importOrgNameNode.orgName().text();
            moduleName.append(importOrgName);
            moduleName.append("/");
        }

        for (int i = 0; i < importModuleName.size(); i++) {
            IdentifierToken identifierToken = importModuleName.get(i);
            moduleName.append(identifierToken.text());
            if (i != (importModuleName.size() - 1)) {
                moduleName.append(".");
            }
        }

        return moduleName.toString();
    }

    private LineRange getDeleteRange(LineRange lineRange) {
        if (lineRange != null) {
            for (LineRange aPosition : deleteRanges.keySet()) {
                if (aPosition.startLine().line() <= lineRange.startLine().line()
                        && aPosition.startLine().offset() <= lineRange.startLine().offset()) {
                    if (aPosition.endLine().line() == lineRange.endLine().line()
                            && aPosition.endLine().offset() >= lineRange.endLine().offset()
                            || aPosition.endLine().line() > lineRange.endLine().line()) {
                        return aPosition;
                    }
                }
            }
        }
        return null;
    }

    private boolean isWithinLineRange(LineRange containerRange, LineRange childRange) {
        return containerRange.startLine().line() <= childRange.startLine().line() &&
                containerRange.endLine().line() >= childRange.endLine().line() &&
                containerRange.startLine().offset() <= childRange.startLine().offset() &&
                containerRange.endLine().offset() >= childRange.endLine().offset();
    }

    private void moveUnusedtoUsedImport(LineRange lineRange, ImportDeclarationNode importDeclarationNode) {
        Optional<Symbol> symbol = semanticModel.symbol(importDeclarationNode);
        if (symbol.isEmpty()) {
            return;
        }

        List<Location> locations = this.semanticModel.references(symbol.get());
        boolean availableOutSideDeleteRange = false;
        for (Location location : locations) {
            if (isWithinLineRange(importDeclarationNode.lineRange(), location.lineRange())) {
                continue;
            }
            LineRange deleteRange = getDeleteRange(location.lineRange());
            if (deleteRange == null) {
                availableOutSideDeleteRange = true;
            }
        }

        // If import has the prefix `_` then treat is as an used import.
        if (importDeclarationNode.prefix().isPresent()
                && "_".equals(importDeclarationNode.prefix().get().prefix().text())) {
            availableOutSideDeleteRange = true;
        }

        if (availableOutSideDeleteRange) {
            this.unusedImports.remove(getImportModuleName(importDeclarationNode.orgName().isPresent()
                    ? importDeclarationNode.orgName().get() : null, importDeclarationNode.moduleName()));
            this.usedImports.put(getImportModuleName(importDeclarationNode.orgName().isPresent()
                            ? importDeclarationNode.orgName().get() : null, importDeclarationNode.moduleName()),
                    importDeclarationNode);
        }
    }

    private void decideVariablesToBeDeleted(LineRange lineRange) {
        LineRange deleteRange = getDeleteRange(lineRange);
        if (deleteRange == null) {
            return;
        }

        List<Location> locations = this.semanticModel.references(srcFile, lineRange.startLine());
        for (Location location : locations) {
            if (isWithinLineRange(deleteRange, location.lineRange())) {
                continue;
            }
            LineRange range = getDeleteRange(location.lineRange());
            if (range == null) {
                toBeDeletedRanges.remove(deleteRange);
            }
        }
    }

    public Collection<ASTModification> toBeDeletedRanges() {
        return toBeDeletedRanges.values();
    }

    public Map<String, ImportDeclarationNode> getUnusedImports() {
        return unusedImports;
    }

    public Map<String, ImportDeclarationNode> getUsedImports() {
        return usedImports;
    }

    @Override
    public void visit(ModulePartNode modulePartNode) {
        if (!modulePartNode.imports().isEmpty()) {
            modulePartNode.imports().forEach(importDeclarationNode -> {
                importDeclarationNode.accept(this);
            });
        }

        if (!modulePartNode.members().isEmpty()) {
            modulePartNode.members().forEach(moduleMemberDeclarationNode -> {
                moduleMemberDeclarationNode.accept(this);
            });
        }
    }

    @Override
    public void visit(ImportDeclarationNode importDeclarationNode) {
        addUnusedImportNode(importDeclarationNode);

        int moduleNamePosition = importDeclarationNode.moduleName().size() - 1;
        if (importDeclarationNode.moduleName().size() > 0
                && importDeclarationNode.moduleName().get(moduleNamePosition) != null) {
            Optional<ImportPrefixNode> prefix = importDeclarationNode.prefix();
            if (prefix.isPresent()) {
                moveUnusedtoUsedImport(prefix.get().prefix().lineRange(), importDeclarationNode);
            } else {
                moveUnusedtoUsedImport(importDeclarationNode.moduleName().get(moduleNamePosition).lineRange(),
                        importDeclarationNode);
            }
        }
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        if (variableDeclarationNode.typedBindingPattern() != null
                && variableDeclarationNode.typedBindingPattern().bindingPattern() != null
                && variableDeclarationNode.typedBindingPattern().bindingPattern().lineRange() != null) {
            decideVariablesToBeDeleted(variableDeclarationNode.typedBindingPattern().bindingPattern().lineRange());
        }
    }

    @Override
    public void visit(AnnotationNode annotationNode) {
        super.visit(annotationNode);
        Optional<Symbol> annotationNodeSymbol = semanticModel.symbol(annotationNode);
        if (annotationNodeSymbol.isEmpty()) {
            return;
        }

        Optional<ModuleSymbol> moduleSymbol = annotationNodeSymbol.get().getModule();

        if (moduleSymbol.isPresent()) {
            ModuleID moduleID = moduleSymbol.get().id();
            String importKey = String.format("%s/%s",
                    moduleID.orgName(), moduleID.moduleName());
            if (unusedImports.containsKey(importKey)) {
                this.usedImports.put(importKey, this.unusedImports.get(importKey));
                this.unusedImports.remove(importKey);
            }
        }
    }
}
