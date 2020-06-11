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

import io.ballerinalang.compiler.syntax.tree.BlockStatementNode;
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
public class STBlockStatementNode extends STStatementNode {
    public final STNode openBraceToken;
    public final STNode statements;
    public final STNode closeBraceToken;

    STBlockStatementNode(
            STNode openBraceToken,
            STNode statements,
            STNode closeBraceToken) {
        this(
                openBraceToken,
                statements,
                closeBraceToken,
                Collections.emptyList());
    }

    STBlockStatementNode(
            STNode openBraceToken,
            STNode statements,
            STNode closeBraceToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.BLOCK_STATEMENT, diagnostics);
        this.openBraceToken = openBraceToken;
        this.statements = statements;
        this.closeBraceToken = closeBraceToken;

        addChildren(
                openBraceToken,
                statements,
                closeBraceToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STBlockStatementNode(
                this.openBraceToken,
                this.statements,
                this.closeBraceToken,
                diagnostics);
    }

    public STBlockStatementNode modify(
            STNode openBraceToken,
            STNode statements,
            STNode closeBraceToken) {
        if (checkForReferenceEquality(
                openBraceToken,
                statements,
                closeBraceToken)) {
            return this;
        }

        return new STBlockStatementNode(
                openBraceToken,
                statements,
                closeBraceToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new BlockStatementNode(this, position, parent);
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
