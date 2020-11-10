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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.StartActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STStartActionNode extends STExpressionNode {
    public final STNode annotations;
    public final STNode startKeyword;
    public final STNode expression;

    STStartActionNode(
            STNode annotations,
            STNode startKeyword,
            STNode expression) {
        this(
                annotations,
                startKeyword,
                expression,
                Collections.emptyList());
    }

    STStartActionNode(
            STNode annotations,
            STNode startKeyword,
            STNode expression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.START_ACTION, diagnostics);
        this.annotations = annotations;
        this.startKeyword = startKeyword;
        this.expression = expression;

        addChildren(
                annotations,
                startKeyword,
                expression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STStartActionNode(
                this.annotations,
                this.startKeyword,
                this.expression,
                diagnostics);
    }

    public STStartActionNode modify(
            STNode annotations,
            STNode startKeyword,
            STNode expression) {
        if (checkForReferenceEquality(
                annotations,
                startKeyword,
                expression)) {
            return this;
        }

        return new STStartActionNode(
                annotations,
                startKeyword,
                expression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new StartActionNode(this, position, parent);
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
