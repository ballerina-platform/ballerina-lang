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

import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
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
public class STMethodCallExpressionNode extends STExpressionNode {
    public final STNode expression;
    public final STNode dotToken;
    public final STNode methodName;
    public final STNode openParenToken;
    public final STNode arguments;
    public final STNode closeParenToken;

    STMethodCallExpressionNode(
            STNode expression,
            STNode dotToken,
            STNode methodName,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {
        this(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken,
                Collections.emptyList());
    }

    STMethodCallExpressionNode(
            STNode expression,
            STNode dotToken,
            STNode methodName,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.METHOD_CALL, diagnostics);
        this.expression = expression;
        this.dotToken = dotToken;
        this.methodName = methodName;
        this.openParenToken = openParenToken;
        this.arguments = arguments;
        this.closeParenToken = closeParenToken;

        addChildren(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMethodCallExpressionNode(
                this.expression,
                this.dotToken,
                this.methodName,
                this.openParenToken,
                this.arguments,
                this.closeParenToken,
                diagnostics);
    }

    public STMethodCallExpressionNode modify(
            STNode expression,
            STNode dotToken,
            STNode methodName,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {
        if (checkForReferenceEquality(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken)) {
            return this;
        }

        return new STMethodCallExpressionNode(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MethodCallExpressionNode(this, position, parent);
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
