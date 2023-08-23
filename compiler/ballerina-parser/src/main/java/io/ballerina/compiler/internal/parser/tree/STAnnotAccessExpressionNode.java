/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
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
public class STAnnotAccessExpressionNode extends STExpressionNode {
    public final STNode expression;
    public final STNode annotChainingToken;
    public final STNode annotTagReference;

    STAnnotAccessExpressionNode(
            STNode expression,
            STNode annotChainingToken,
            STNode annotTagReference) {
        this(
                expression,
                annotChainingToken,
                annotTagReference,
                Collections.emptyList());
    }

    STAnnotAccessExpressionNode(
            STNode expression,
            STNode annotChainingToken,
            STNode annotTagReference,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ANNOT_ACCESS, diagnostics);
        this.expression = expression;
        this.annotChainingToken = annotChainingToken;
        this.annotTagReference = annotTagReference;

        addChildren(
                expression,
                annotChainingToken,
                annotTagReference);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STAnnotAccessExpressionNode(
                this.expression,
                this.annotChainingToken,
                this.annotTagReference,
                diagnostics);
    }

    public STAnnotAccessExpressionNode modify(
            STNode expression,
            STNode annotChainingToken,
            STNode annotTagReference) {
        if (checkForReferenceEquality(
                expression,
                annotChainingToken,
                annotTagReference)) {
            return this;
        }

        return new STAnnotAccessExpressionNode(
                expression,
                annotChainingToken,
                annotTagReference,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new AnnotAccessExpressionNode(this, position, parent);
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
