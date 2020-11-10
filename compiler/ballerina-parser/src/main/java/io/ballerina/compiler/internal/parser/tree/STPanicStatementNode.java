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
import io.ballerina.compiler.syntax.tree.PanicStatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STPanicStatementNode extends STStatementNode {
    public final STNode panicKeyword;
    public final STNode expression;
    public final STNode semicolonToken;

    STPanicStatementNode(
            STNode panicKeyword,
            STNode expression,
            STNode semicolonToken) {
        this(
                panicKeyword,
                expression,
                semicolonToken,
                Collections.emptyList());
    }

    STPanicStatementNode(
            STNode panicKeyword,
            STNode expression,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.PANIC_STATEMENT, diagnostics);
        this.panicKeyword = panicKeyword;
        this.expression = expression;
        this.semicolonToken = semicolonToken;

        addChildren(
                panicKeyword,
                expression,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STPanicStatementNode(
                this.panicKeyword,
                this.expression,
                this.semicolonToken,
                diagnostics);
    }

    public STPanicStatementNode modify(
            STNode panicKeyword,
            STNode expression,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                panicKeyword,
                expression,
                semicolonToken)) {
            return this;
        }

        return new STPanicStatementNode(
                panicKeyword,
                expression,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new PanicStatementNode(this, position, parent);
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
