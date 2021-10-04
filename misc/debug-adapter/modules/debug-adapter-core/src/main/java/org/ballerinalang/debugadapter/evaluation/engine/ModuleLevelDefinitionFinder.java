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
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
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
    private final List<ModuleMemberDeclarationNode> result = new ArrayList<>();

    public ModuleLevelDefinitionFinder(SuspendedContext context) {
        this.context = context;
    }

    public void addInclusiveFilter(SyntaxKind topLevelNodeKind) {
        filters.add(topLevelNodeKind);
    }

    /**
     * Search for all the top level declarations, in the the ballerina module that contains the debug hit source line.
     *
     * @return any function definitions with the specified name, if present.
     */
    public List<ModuleMemberDeclarationNode> getCurrentModuleDeclarations() {
        return getModuleDeclarations(context.getModule());
    }

    /**
     * Search for all the top level declarations, within the given ballerina module instance.
     *
     * @param module Ballerina module
     * @return any function definitions with the specified name, if present.
     */
    public List<ModuleMemberDeclarationNode> getModuleDeclarations(Module module) {
        for (DocumentId documentId : module.documentIds()) {
            searchInFile(module.document(documentId));
        }
        return result;
    }

    private void searchInFile(Document document) {
        document.syntaxTree().rootNode().accept(this);
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        if (node instanceof ModuleMemberDeclarationNode) {
            if (!filters.contains(node.kind())) {
                return;
            }
            // Need to ignore the entry points ('main' function and services), when capturing top level definitions.
            if (!(node instanceof FunctionDefinitionNode) ||
                    !((FunctionDefinitionNode) node).functionName().toSourceCode().equals(MAIN_FUNCTION_NAME)) {
                result.add((ModuleMemberDeclarationNode) node);
            }
        } else if (node instanceof Token) {
            node.accept(this);
        } else {
            NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
            for (Node child : nonTerminalNode.children()) {
                child.accept(this);
            }
        }
    }
}
