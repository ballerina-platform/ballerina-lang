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

import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
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
public class STImplicitAnonymousFunctionExpressionNode extends STAnonymousFunctionExpressionNode {
    public final STNode params;
    public final STNode rightDoubleArrow;
    public final STNode expression;

    STImplicitAnonymousFunctionExpressionNode(
            STNode params,
            STNode rightDoubleArrow,
            STNode expression) {
        this(
                params,
                rightDoubleArrow,
                expression,
                Collections.emptyList());
    }

    STImplicitAnonymousFunctionExpressionNode(
            STNode params,
            STNode rightDoubleArrow,
            STNode expression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION, diagnostics);
        this.params = params;
        this.rightDoubleArrow = rightDoubleArrow;
        this.expression = expression;

        addChildren(
                params,
                rightDoubleArrow,
                expression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STImplicitAnonymousFunctionExpressionNode(
                this.params,
                this.rightDoubleArrow,
                this.expression,
                diagnostics);
    }

    public STImplicitAnonymousFunctionExpressionNode modify(
            STNode params,
            STNode rightDoubleArrow,
            STNode expression) {
        if (checkForReferenceEquality(
                params,
                rightDoubleArrow,
                expression)) {
            return this;
        }

        return new STImplicitAnonymousFunctionExpressionNode(
                params,
                rightDoubleArrow,
                expression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ImplicitAnonymousFunctionExpressionNode(this, position, parent);
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
