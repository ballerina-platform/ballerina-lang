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
public class STNaturalExpressionNode extends STExpressionNode {
    public final STNode constKeyword;
    public final STNode naturalKeyword;
    public final STNode parenthesizedArgList;
    public final STNode openBraceToken;
    public final STNode prompt;
    public final STNode closeBraceToken;

    STNaturalExpressionNode(
            STNode constKeyword,
            STNode naturalKeyword,
            STNode parenthesizedArgList,
            STNode openBraceToken,
            STNode prompt,
            STNode closeBraceToken) {
        this(
                constKeyword,
                naturalKeyword,
                parenthesizedArgList,
                openBraceToken,
                prompt,
                closeBraceToken,
                Collections.emptyList());
    }

    STNaturalExpressionNode(
            STNode constKeyword,
            STNode naturalKeyword,
            STNode parenthesizedArgList,
            STNode openBraceToken,
            STNode prompt,
            STNode closeBraceToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.NATURAL_EXPRESSION, diagnostics);
        this.constKeyword = constKeyword;
        this.naturalKeyword = naturalKeyword;
        this.parenthesizedArgList = parenthesizedArgList;
        this.openBraceToken = openBraceToken;
        this.prompt = prompt;
        this.closeBraceToken = closeBraceToken;

        addChildren(
                constKeyword,
                naturalKeyword,
                parenthesizedArgList,
                openBraceToken,
                prompt,
                closeBraceToken);
    }

    @Override
    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STNaturalExpressionNode(
                this.constKeyword,
                this.naturalKeyword,
                this.parenthesizedArgList,
                this.openBraceToken,
                this.prompt,
                this.closeBraceToken,
                diagnostics);
    }

    public STNaturalExpressionNode modify(
            STNode constKeyword,
            STNode naturalKeyword,
            STNode parenthesizedArgList,
            STNode openBraceToken,
            STNode prompt,
            STNode closeBraceToken) {
        if (checkForReferenceEquality(
                constKeyword,
                naturalKeyword,
                parenthesizedArgList,
                openBraceToken,
                prompt,
                closeBraceToken)) {
            return this;
        }

        return new STNaturalExpressionNode(
                constKeyword,
                naturalKeyword,
                parenthesizedArgList,
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
