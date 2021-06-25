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

package org.ballerinalang.debugadapter.evaluation.engine;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;

import java.util.Optional;

/**
 * A {@link NodeVisitor} implementation which can be used to search for function definition information just by using
 * the function name.
 *
 * @since 2.0.0
 */
public class FunctionNodeFinder extends NodeVisitor {

    private final String functionName;
    private FunctionDefinitionNode result;

    public FunctionNodeFinder(String functionName) {
        this.functionName = functionName;
    }

    /**
     * Search for function definitions with the specified name, within the given ballerina module instance.
     *
     * @param module Ballerina module
     * @return any function definitions with the specified name, if present.
     */
    public Optional<FunctionDefinitionNode> searchInModule(Module module) {
        for (DocumentId documentId : module.documentIds()) {
            if (result != null) {
                break;
            }
            Optional<FunctionDefinitionNode> result = searchInFile(module.document(documentId));
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.ofNullable(result);
    }

    public Optional<FunctionDefinitionNode> searchInFile(Document document) {
        document.syntaxTree().rootNode().accept(this);
        return Optional.ofNullable(result);
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.functionName().toSourceCode().equals(functionName)) {
            result = functionDefinitionNode;
        }
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        // Do an early exit if the result is already found.
        if (result != null) {
            return;
        }

        if (node instanceof Token) {
            node.accept(this);
            return;
        }

        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        for (Node child : nonTerminalNode.children()) {
            child.accept(this);
        }
    }
}
