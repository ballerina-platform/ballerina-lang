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

package org.ballerinalang.debugadapter.evaluation.engine;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import org.ballerinalang.debugadapter.SuspendedContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.ballerina.runtime.api.constants.RuntimeConstants.MAIN_FUNCTION_NAME;

/**
 * A {@link NodeVisitor} implementation which can be used to search for all the module level definitions
 * (e.g. function,class,type) within a given ballerina module.
 *
 * @since 2.0.0
 */
public class ModuleLevelDefinitionFinder extends NodeVisitor {

    private final SuspendedContext context;
    private final Set<SyntaxKind> filters = new HashSet<>();
    private final List<NonTerminalNode> result = new ArrayList<>();

    private static final String ANNOTATION_BUILTINSUBTYPE = "builtinSubtype";

    public ModuleLevelDefinitionFinder(SuspendedContext context) {
        this.context = context;
    }

    public void addInclusiveFilter(SyntaxKind topLevelNodeKind) {
        filters.add(topLevelNodeKind);
    }

    /**
     * Search for all the top level declarations, in the ballerina module that contains the debug hit source line.
     *
     * @return any function definitions with the specified name, if present.
     */
    public List<NonTerminalNode> getCurrentModuleDeclarations() {
        return getModuleDeclarations(context.getModule());
    }

    /**
     * Search for all the top level declarations, within the given ballerina module instance.
     *
     * @param module Ballerina module
     * @return any function definitions with the specified name, if present.
     */
    public List<NonTerminalNode> getModuleDeclarations(Module module) {
        for (DocumentId documentId : module.documentIds()) {
            searchInFile(module.document(documentId));
        }
        return result;
    }

    private void searchInFile(Document document) {
        if (!document.syntaxTree().containsModulePart()) {
            return;
        }
        ModulePartNode modulePartNode = document.syntaxTree().rootNode();
        modulePartNode.imports().forEach(this::visitSyntaxNode);
        modulePartNode.members().forEach(this::visitSyntaxNode);
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        if (!filters.contains(node.kind()) || node instanceof Token) {
            return;
        }

        if (!(node instanceof ImportDeclarationNode) && !(node instanceof ModuleMemberDeclarationNode)) {
            return;
        }

        // Ignores entry points (main function definitions).
        if (node instanceof FunctionDefinitionNode functionDefinitionNode &&
                functionDefinitionNode.functionName().toSourceCode().equals(MAIN_FUNCTION_NAME)) {
            return;
        }

        // Ignores external function definitions.
        if (node instanceof FunctionDefinitionNode functionDefinitionNode &&
                functionDefinitionNode.functionBody().kind() == SyntaxKind.EXTERNAL_FUNCTION_BODY) {
            return;
        }

        // Ignores type definitions with @builtinSubtype annotations (specific to Ballerina library sources).
        if (node instanceof TypeDefinitionNode typeDefinitionNode && typeDefinitionNode.metadata().isPresent()
                && typeDefinitionNode.metadata().get().annotations().stream().anyMatch(annotationNode ->
                annotationNode.annotReference().toSourceCode().trim().equalsIgnoreCase(ANNOTATION_BUILTINSUBTYPE))) {
            return;
        }

        result.add((NonTerminalNode) node);
    }
}
