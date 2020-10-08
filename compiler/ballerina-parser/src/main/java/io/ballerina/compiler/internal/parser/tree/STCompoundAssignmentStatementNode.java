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
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STCompoundAssignmentStatementNode extends STStatementNode {
    public final STNode lhsExpression;
    public final STNode binaryOperator;
    public final STNode equalsToken;
    public final STNode rhsExpression;
    public final STNode semicolonToken;

    STCompoundAssignmentStatementNode(
            STNode lhsExpression,
            STNode binaryOperator,
            STNode equalsToken,
            STNode rhsExpression,
            STNode semicolonToken) {
        this(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken,
                Collections.emptyList());
    }

    STCompoundAssignmentStatementNode(
            STNode lhsExpression,
            STNode binaryOperator,
            STNode equalsToken,
            STNode rhsExpression,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT, diagnostics);
        this.lhsExpression = lhsExpression;
        this.binaryOperator = binaryOperator;
        this.equalsToken = equalsToken;
        this.rhsExpression = rhsExpression;
        this.semicolonToken = semicolonToken;

        addChildren(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STCompoundAssignmentStatementNode(
                this.lhsExpression,
                this.binaryOperator,
                this.equalsToken,
                this.rhsExpression,
                this.semicolonToken,
                diagnostics);
    }

    public STCompoundAssignmentStatementNode modify(
            STNode lhsExpression,
            STNode binaryOperator,
            STNode equalsToken,
            STNode rhsExpression,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken)) {
            return this;
        }

        return new STCompoundAssignmentStatementNode(
                lhsExpression,
                binaryOperator,
                equalsToken,
                rhsExpression,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new CompoundAssignmentStatementNode(this, position, parent);
    }

    @Override
    public void accept(STNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }
}
