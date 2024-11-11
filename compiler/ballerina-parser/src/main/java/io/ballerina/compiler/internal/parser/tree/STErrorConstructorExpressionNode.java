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

import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
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
public class STErrorConstructorExpressionNode extends STExpressionNode {
    public final STNode errorKeyword;
    public final STNode typeReference;
    public final STNode openParenToken;
    public final STNode arguments;
    public final STNode closeParenToken;

    STErrorConstructorExpressionNode(
            STNode errorKeyword,
            STNode typeReference,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {
        this(
                errorKeyword,
                typeReference,
                openParenToken,
                arguments,
                closeParenToken,
                Collections.emptyList());
    }

    STErrorConstructorExpressionNode(
            STNode errorKeyword,
            STNode typeReference,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ERROR_CONSTRUCTOR, diagnostics);
        this.errorKeyword = errorKeyword;
        this.typeReference = typeReference;
        this.openParenToken = openParenToken;
        this.arguments = arguments;
        this.closeParenToken = closeParenToken;

        addChildren(
                errorKeyword,
                typeReference,
                openParenToken,
                arguments,
                closeParenToken);
    }

    @Override
    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STErrorConstructorExpressionNode(
                this.errorKeyword,
                this.typeReference,
                this.openParenToken,
                this.arguments,
                this.closeParenToken,
                diagnostics);
    }

    public STErrorConstructorExpressionNode modify(
            STNode errorKeyword,
            STNode typeReference,
            STNode openParenToken,
            STNode arguments,
            STNode closeParenToken) {
        if (checkForReferenceEquality(
                errorKeyword,
                typeReference,
                openParenToken,
                arguments,
                closeParenToken)) {
            return this;
        }

        return new STErrorConstructorExpressionNode(
                errorKeyword,
                typeReference,
                openParenToken,
                arguments,
                closeParenToken,
                diagnostics);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new ErrorConstructorExpressionNode(this, position, parent);
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
