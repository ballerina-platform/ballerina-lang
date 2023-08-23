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

import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
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
public class STExpressionFunctionBodyNode extends STFunctionBodyNode {
    public final STNode rightDoubleArrow;
    public final STNode expression;
    public final STNode semicolon;

    STExpressionFunctionBodyNode(
            STNode rightDoubleArrow,
            STNode expression,
            STNode semicolon) {
        this(
                rightDoubleArrow,
                expression,
                semicolon,
                Collections.emptyList());
    }

    STExpressionFunctionBodyNode(
            STNode rightDoubleArrow,
            STNode expression,
            STNode semicolon,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.EXPRESSION_FUNCTION_BODY, diagnostics);
        this.rightDoubleArrow = rightDoubleArrow;
        this.expression = expression;
        this.semicolon = semicolon;

        addChildren(
                rightDoubleArrow,
                expression,
                semicolon);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STExpressionFunctionBodyNode(
                this.rightDoubleArrow,
                this.expression,
                this.semicolon,
                diagnostics);
    }

    public STExpressionFunctionBodyNode modify(
            STNode rightDoubleArrow,
            STNode expression,
            STNode semicolon) {
        if (checkForReferenceEquality(
                rightDoubleArrow,
                expression,
                semicolon)) {
            return this;
        }

        return new STExpressionFunctionBodyNode(
                rightDoubleArrow,
                expression,
                semicolon,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ExpressionFunctionBodyNode(this, position, parent);
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
