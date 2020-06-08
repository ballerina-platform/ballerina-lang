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

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TypeTestExpressionNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTypeTestExpressionNode extends STExpressionNode {
    public final STNode expression;
    public final STNode isKeyword;
    public final STNode typeDescriptor;

    STTypeTestExpressionNode(
            STNode expression,
            STNode isKeyword,
            STNode typeDescriptor) {
        this(
                expression,
                isKeyword,
                typeDescriptor,
                Collections.emptyList());
    }

    STTypeTestExpressionNode(
            STNode expression,
            STNode isKeyword,
            STNode typeDescriptor,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TYPE_TEST_EXPRESSION, diagnostics);
        this.expression = expression;
        this.isKeyword = isKeyword;
        this.typeDescriptor = typeDescriptor;

        addChildren(
                expression,
                isKeyword,
                typeDescriptor);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTypeTestExpressionNode(
                this.expression,
                this.isKeyword,
                this.typeDescriptor,
                diagnostics);
    }

    public STTypeTestExpressionNode modify(
            STNode expression,
            STNode isKeyword,
            STNode typeDescriptor) {
        if (checkForReferenceEquality(
                expression,
                isKeyword,
                typeDescriptor)) {
            return this;
        }

        return new STTypeTestExpressionNode(
                expression,
                isKeyword,
                typeDescriptor,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new TypeTestExpressionNode(this, position, parent);
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
