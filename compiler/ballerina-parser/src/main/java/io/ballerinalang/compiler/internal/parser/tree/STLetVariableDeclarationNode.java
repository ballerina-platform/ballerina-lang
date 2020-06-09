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

import io.ballerinalang.compiler.syntax.tree.LetVariableDeclarationNode;
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
public class STLetVariableDeclarationNode extends STNode {
    public final STNode annotations;
    public final STNode typedBindingPattern;
    public final STNode equalsToken;
    public final STNode expression;

    STLetVariableDeclarationNode(
            STNode annotations,
            STNode typedBindingPattern,
            STNode equalsToken,
            STNode expression) {
        this(
                annotations,
                typedBindingPattern,
                equalsToken,
                expression,
                Collections.emptyList());
    }

    STLetVariableDeclarationNode(
            STNode annotations,
            STNode typedBindingPattern,
            STNode equalsToken,
            STNode expression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.LET_VAR_DECL, diagnostics);
        this.annotations = annotations;
        this.typedBindingPattern = typedBindingPattern;
        this.equalsToken = equalsToken;
        this.expression = expression;

        addChildren(
                annotations,
                typedBindingPattern,
                equalsToken,
                expression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STLetVariableDeclarationNode(
                this.annotations,
                this.typedBindingPattern,
                this.equalsToken,
                this.expression,
                diagnostics);
    }

    public STLetVariableDeclarationNode modify(
            STNode annotations,
            STNode typedBindingPattern,
            STNode equalsToken,
            STNode expression) {
        if (checkForReferenceEquality(
                annotations,
                typedBindingPattern,
                equalsToken,
                expression)) {
            return this;
        }

        return new STLetVariableDeclarationNode(
                annotations,
                typedBindingPattern,
                equalsToken,
                expression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new LetVariableDeclarationNode(this, position, parent);
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
