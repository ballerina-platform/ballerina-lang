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

import io.ballerinalang.compiler.syntax.tree.NamedArgumentNode;
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
public class STNamedArgumentNode extends STFunctionArgumentNode {
    public final STNode leadingComma;
    public final STNode argumentName;
    public final STNode equalsToken;
    public final STNode expression;

    STNamedArgumentNode(
            STNode leadingComma,
            STNode argumentName,
            STNode equalsToken,
            STNode expression) {
        this(
                leadingComma,
                argumentName,
                equalsToken,
                expression,
                Collections.emptyList());
    }

    STNamedArgumentNode(
            STNode leadingComma,
            STNode argumentName,
            STNode equalsToken,
            STNode expression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.NAMED_ARG, diagnostics);
        this.leadingComma = leadingComma;
        this.argumentName = argumentName;
        this.equalsToken = equalsToken;
        this.expression = expression;

        addChildren(
                leadingComma,
                argumentName,
                equalsToken,
                expression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STNamedArgumentNode(
                this.leadingComma,
                this.argumentName,
                this.equalsToken,
                this.expression,
                diagnostics);
    }

    public STNamedArgumentNode modify(
            STNode leadingComma,
            STNode argumentName,
            STNode equalsToken,
            STNode expression) {
        if (checkForReferenceEquality(
                leadingComma,
                argumentName,
                equalsToken,
                expression)) {
            return this;
        }

        return new STNamedArgumentNode(
                leadingComma,
                argumentName,
                equalsToken,
                expression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new NamedArgumentNode(this, position, parent);
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
