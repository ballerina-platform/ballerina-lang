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
import org.ballerinalang.model.tree.NodeKind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static io.ballerina.runtime.api.constants.RuntimeConstants.MAIN_FUNCTION_NAME;

/**
 * A {@link NodeVisitor} implementation which can be used to search for all the module level definitions
 * (e.g. function,class,type) in a given ballerina module.
 *
 * @since 2.0.0
 */
public class ModuleLevelDefinitionFinder extends NodeVisitor {

    private final SuspendedContext context;
    private static final Set<SyntaxKind> filters = new HashSet<>();
    private List<ModuleMemberDeclarationNode> result = new ArrayList<>();

    public ModuleLevelDefinitionFinder(SuspendedContext context) {
        this.context = context;

        filters.add(SyntaxKind.IMPORT_DECLARATION);
        filters.add(SyntaxKind.FUNCTION_DEFINITION);
        filters.add(SyntaxKind.TYPE_DEFINITION);
        filters.add(SyntaxKind.MODULE_VAR_DECL);
        filters.add(SyntaxKind.LISTENER_DECLARATION);
        filters.add(SyntaxKind.CONST_DECLARATION);
        filters.add(SyntaxKind.ANNOTATION_DECLARATION);
        filters.add(SyntaxKind.MODULE_XML_NAMESPACE_DECLARATION);
        filters.add(SyntaxKind.ENUM_DECLARATION);
        filters.add(SyntaxKind.CLASS_DEFINITION);
    }

    /**
     * Search for all the top level declarations, in the the ballerina module that contains the debug hit source line.
     *
     * @return any function definitions with the specified name, if present.
     */
    public List<ModuleMemberDeclarationNode> getModuleLevelDeclarations() {
        return getModuleLevelDeclarations(context.getModule());
    }

    /**
     * Search for all the top level declarations, within the given ballerina module instance.
     *
     * @param module Ballerina module
     * @return any function definitions with the specified name, if present.
     */
    public List<ModuleMemberDeclarationNode> getModuleLevelDeclarations(Module module) {
        for (DocumentId documentId : module.documentIds()) {
            searchInFile(module.document(documentId));
        }
        return result;
    }

    public void searchInFile(Document document) {
        document.syntaxTree().rootNode().accept(this);
    }

    @Override
    protected void visitSyntaxNode(Node node) {

        if (node instanceof ModuleMemberDeclarationNode) {
            // Need to exclude the 'main' function when generating the snippet.
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
