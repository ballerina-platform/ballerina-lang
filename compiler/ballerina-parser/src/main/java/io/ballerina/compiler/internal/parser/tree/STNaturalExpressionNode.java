/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

import io.ballerina.compiler.syntax.tree.NaturalExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.13.0
 */
public class STNaturalExpressionNode extends STNode {
    public final STNode naturalKeyword;
    public final STNode naturalModel;
    public final STNode openBraceToken;
    public final STNode prompt;
    public final STNode closeBraceToken;

    STNaturalExpressionNode(
            STNode naturalKeyword,
            STNode naturalModel,
            STNode openBraceToken,
            STNode prompt,
            STNode closeBraceToken) {
        this(
                naturalKeyword,
                naturalModel,
                openBraceToken,
                prompt,
                closeBraceToken,
                Collections.emptyList());
    }

    STNaturalExpressionNode(
            STNode naturalKeyword,
            STNode naturalModel,
            STNode openBraceToken,
            STNode prompt,
            STNode closeBraceToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.NATURAL_EXPRESSION, diagnostics);
        this.naturalKeyword = naturalKeyword;
        this.naturalModel = naturalModel;
        this.openBraceToken = openBraceToken;
        this.prompt = prompt;
        this.closeBraceToken = closeBraceToken;

        addChildren(
                naturalKeyword,
                naturalModel,
                openBraceToken,
                prompt,
                closeBraceToken);
    }

    @Override
    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STNaturalExpressionNode(
                this.naturalKeyword,
                this.naturalModel,
                this.openBraceToken,
                this.prompt,
                this.closeBraceToken,
                diagnostics);
    }

    public STNaturalExpressionNode modify(
            STNode naturalKeyword,
            STNode naturalModel,
            STNode openBraceToken,
            STNode prompt,
            STNode closeBraceToken) {
        if (checkForReferenceEquality(
                naturalKeyword,
                naturalModel,
                openBraceToken,
                prompt,
                closeBraceToken)) {
            return this;
        }

        return new STNaturalExpressionNode(
                naturalKeyword,
                naturalModel,
                openBraceToken,
                prompt,
                closeBraceToken,
                diagnostics);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new NaturalExpressionNode(this, position, parent);
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
