/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;

/**
 * Node visitor to ensure the highlighted range does not include nodes other than BasicLiteralNode.
 *
 * @since 2201.2.0
 */
public class ConstantVisitor extends NodeVisitor {

    private boolean invalidNode = false;

    @Override
    public void visit(BinaryExpressionNode node) {
        node.lhsExpr().accept(this);
        node.rhsExpr().accept(this);
    }


    @Override
    public void visit(BasicLiteralNode node) {
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        invalidNode = true;
    }

    public Boolean getInvalidNode() {
        return invalidNode;
    }
}
