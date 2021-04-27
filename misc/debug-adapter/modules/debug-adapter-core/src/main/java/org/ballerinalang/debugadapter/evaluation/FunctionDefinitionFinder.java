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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;

/**
 * @since 2.0.0
 */
public class FunctionDefinitionFinder extends NodeVisitor {

    private final String searchString;
    private boolean functionDefFound;
    private FunctionDefinitionNode result;

    public FunctionDefinitionFinder(String functionName) {
        this.searchString = functionName;
        this.functionDefFound = false;
    }

    public boolean isFound() {
        return functionDefFound;
    }

    public FunctionDefinitionNode getResult() {
        return result;
    }

    @Override
    public void visit(FunctionDefinitionNode node) {
        if (node.functionName().toSourceCode().trim().equals(searchString)) {
            functionDefFound = true;
            result = node;
        }
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        if (functionDefFound) {
            return;
        }
        super.visitSyntaxNode(node);
    }
}
