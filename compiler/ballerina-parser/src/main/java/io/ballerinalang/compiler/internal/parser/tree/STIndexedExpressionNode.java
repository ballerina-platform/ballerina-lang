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

import io.ballerinalang.compiler.syntax.tree.IndexedExpressionNode;
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
public class STIndexedExpressionNode extends STTypeDescriptorNode {
    public final STNode containerExpression;
    public final STNode openBracket;
    public final STNode keyExpression;
    public final STNode closeBracket;

    STIndexedExpressionNode(
            STNode containerExpression,
            STNode openBracket,
            STNode keyExpression,
            STNode closeBracket) {
        this(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket,
                Collections.emptyList());
    }

    STIndexedExpressionNode(
            STNode containerExpression,
            STNode openBracket,
            STNode keyExpression,
            STNode closeBracket,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.INDEXED_EXPRESSION, diagnostics);
        this.containerExpression = containerExpression;
        this.openBracket = openBracket;
        this.keyExpression = keyExpression;
        this.closeBracket = closeBracket;

        addChildren(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STIndexedExpressionNode(
                this.containerExpression,
                this.openBracket,
                this.keyExpression,
                this.closeBracket,
                diagnostics);
    }

    public STIndexedExpressionNode modify(
            STNode containerExpression,
            STNode openBracket,
            STNode keyExpression,
            STNode closeBracket) {
        if (checkForReferenceEquality(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket)) {
            return this;
        }

        return new STIndexedExpressionNode(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new IndexedExpressionNode(this, position, parent);
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
