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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STBinaryExpressionNode extends STExpressionNode {
    public final STNode lhsExpr;
    public final STNode operator;
    public final STNode rhsExpr;

    STBinaryExpressionNode(
            SyntaxKind kind,
            STNode lhsExpr,
            STNode operator,
            STNode rhsExpr) {
        this(
                kind,
                lhsExpr,
                operator,
                rhsExpr,
                Collections.emptyList());
    }

    STBinaryExpressionNode(
            SyntaxKind kind,
            STNode lhsExpr,
            STNode operator,
            STNode rhsExpr,
            Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
        this.lhsExpr = lhsExpr;
        this.operator = operator;
        this.rhsExpr = rhsExpr;

        addChildren(
                lhsExpr,
                operator,
                rhsExpr);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STBinaryExpressionNode(
                this.kind,
                this.lhsExpr,
                this.operator,
                this.rhsExpr,
                diagnostics);
    }

    public STBinaryExpressionNode modify(
            SyntaxKind kind,
            STNode lhsExpr,
            STNode operator,
            STNode rhsExpr) {
        if (checkForReferenceEquality(
                lhsExpr,
                operator,
                rhsExpr)) {
            return this;
        }

        return new STBinaryExpressionNode(
                kind,
                lhsExpr,
                operator,
                rhsExpr,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new BinaryExpressionNode(this, position, parent);
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
