/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;

/**
 * Visitor to validate an expression node.
 *
 * @since 2201.5.0
 */
public class ExpressionNodeValidator extends NodeVisitor {

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
    public void visit(UnaryExpressionNode node) {
    }

    @Override
    public void visit(FieldAccessExpressionNode fieldAccessExpressionNode) {

    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
    }

    @Override
    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {

    }

    @Override
    public void visit(OptionalFieldAccessExpressionNode optionalFieldAccessExpressionNode) {

    }

    @Override
    public void visit(IndexedExpressionNode indexedExpressionNode) {

    }

    @Override
    public void visit(TypeCastExpressionNode typeCastExpressionNode) {

    }

    @Override
    public void visit(ImplicitNewExpressionNode implicitNewExpressionNode) {

    }

    @Override
    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {

    }

    @Override
    protected void visitSyntaxNode(Node node) {
        invalidNode = true;
    }

    public Boolean isInvalidNode() {
        return invalidNode;
    }
}
