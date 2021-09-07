/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;

/**
 * Readable context identifier for a optional field access expression.
 * Optional field access expressions can only be used for value reading.
 * The language does not allow to assign/ write to an optional field access expression.
 * This visitor will go through the tree and identify which contexts leads to assigning to an optional field access
 * expression and which contexts has ambiguity or determining whether a read or write
 * 
 * returns false if the context where the field access expression and the cursor resides is ambiguous/ possibly a write
 * 
 * @since 2.0.0
 */
class OptionalFieldAccessWriteAmbiguityResolver extends NodeTransformer<Boolean> {
    private final BallerinaCompletionContext context;

    public OptionalFieldAccessWriteAmbiguityResolver(BallerinaCompletionContext context) {
        this.context = context;
    }

    @Override
    public Boolean transform(ExpressionStatementNode node) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean transform(AssignmentStatementNode node) {
        int cursor = context.getCursorPositionInTree();
        Token equalsToken = node.equalsToken();

        /*
        Returns false (write ambiguous) if the cursor is before the equals sign
        Eg: testField.<cursor> = ...
         */
        return !equalsToken.isMissing() && cursor > equalsToken.textRange().startOffset();
    }

    @Override
    public Boolean transform(CompoundAssignmentStatementNode node) {
        int cursor = context.getCursorPositionInTree();
        Token binaryOperator = node.binaryOperator();
        /*
        Returns false (write ambiguous) if the cursor is before the binary operator
        Eg: testField.<cursor> += ...
         */
        return !binaryOperator.isMissing() && cursor > binaryOperator.textRange().startOffset();
    }

    @Override
    public Boolean transform(FieldAccessExpressionNode node) {
        return this.visit(node.parent());
    }

    @Override
    public Boolean transform(MethodCallExpressionNode node) {
        return this.visit(node.parent());
    }

    @Override
    public Boolean transform(SimpleNameReferenceNode node) {
        return this.visit(node.parent());
    }

    @Override
    protected Boolean transformSyntaxNode(Node node) {
        return Boolean.TRUE;
    }

    private Boolean visit(Node node) {
        if (node == null) {
            return Boolean.TRUE;
        }
        return node.apply(this);
    }
}
