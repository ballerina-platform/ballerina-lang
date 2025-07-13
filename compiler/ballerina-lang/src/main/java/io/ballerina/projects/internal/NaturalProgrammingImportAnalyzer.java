/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.projects.internal;

import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NaturalExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;

import java.util.Optional;

/**
 * Check and add an import for the natural programming module if there is a natural expression.
 *
 * @since 2201.13.0
 */
public class NaturalProgrammingImportAnalyzer extends NodeVisitor {

    private boolean shouldImportNaturalProgrammingModule = false;
    private Optional<String> naturalLangLibPrefix = Optional.of(NATURAL_WITH_QUOTE);

    private static final String BALLERINA = "ballerina";
    private static final String NATURAL = "natural";
    private static final String NATURAL_WITH_QUOTE = "'natural";
    private static final String CODE_ANNOTATION = "code";

    public boolean shouldImportNaturalProgrammingModule(ModulePartNode modulePartNode) {
        modulePartNode.accept(this);
        return this.shouldImportNaturalProgrammingModule;
    }

    @Override
    public void visit(ImportDeclarationNode importDeclarationNode) {
        Optional<String> prefixStr = Optional.empty();
        boolean isNaturalPrefix = false;

        Optional<ImportPrefixNode> prefix = importDeclarationNode.prefix();
        if (prefix.isPresent()) {
            String prefixValue = prefix.get().prefix().text();
            if (NATURAL_WITH_QUOTE.equals(prefixValue)) {
                isNaturalPrefix = true;
            }
            prefixStr = Optional.of(prefixValue);
        }

        if (isNaturalLangLibImport(importDeclarationNode)) {
            if (prefixStr.isPresent() && !isNaturalPrefix) {
                naturalLangLibPrefix = prefixStr;
            }
            return;
        }

        if (isNaturalPrefix &&
                naturalLangLibPrefix.isPresent() &&
                NATURAL_WITH_QUOTE.equals(naturalLangLibPrefix.get())) {
            naturalLangLibPrefix = Optional.empty();
        }
    }

    @Override
    public void visit(NaturalExpressionNode naturalExpressionNode) {
        // For now, we will import the np module for runtime natural expressions also, to add the schema annotation
        // for expected type of natural expressions.
        this.shouldImportNaturalProgrammingModule = true;
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        if (this.shouldImportNaturalProgrammingModule) {
            return;
        }

        if (isExternalFunctionWithCodeAnnotation(functionDefinitionNode)) {
            this.shouldImportNaturalProgrammingModule = true;
            return;
        }

        super.visit(functionDefinitionNode);
    }

    private boolean isExternalFunctionWithCodeAnnotation(FunctionDefinitionNode functionDefinitionNode) {
        if (!(functionDefinitionNode.functionBody() instanceof ExternalFunctionBodyNode externalFunctionBodyNode)) {
            return false;
        }

        if (naturalLangLibPrefix.isEmpty()) {
            return false;
        }

        return externalFunctionBodyNode.annotations().stream()
                .anyMatch(annotation ->
                        annotation.annotReference() instanceof QualifiedNameReferenceNode qualifiedNameReferenceNode &&
                                isNaturalPrefix(naturalLangLibPrefix.get()) &&
                                isNaturalPrefix(qualifiedNameReferenceNode.modulePrefix().text()) &&
                                CODE_ANNOTATION.equals(qualifiedNameReferenceNode.identifier().text()));
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        if (this.shouldImportNaturalProgrammingModule) {
            return;
        }
        super.visitSyntaxNode(node);
    }

    private boolean isNaturalLangLibImport(ImportDeclarationNode importDeclarationNode) {
        Optional<ImportOrgNameNode> importOrgNameNode = importDeclarationNode.orgName();
        if (importOrgNameNode.isEmpty() || !BALLERINA.equals(importOrgNameNode.get().orgName().text())) {
            return false;
        }

        SeparatedNodeList<IdentifierToken> moduleName = importDeclarationNode.moduleName();
        return moduleName.size() == 2 &&
                moduleName.get(0).text().equals("lang") &&
                moduleName.get(1).text().equals(NATURAL_WITH_QUOTE);
    }

    private boolean isNaturalPrefix(String prefix) {
        return NATURAL.equals(prefix) || NATURAL_WITH_QUOTE.equals(prefix);
    }
}
