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

import io.ballerinalang.compiler.syntax.tree.MatchStatementNode;
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
public class STMatchStatementNode extends STStatementNode {
    public final STNode matchKeyword;
    public final STNode condition;
    public final STNode openBrace;
    public final STNode matchClauses;
    public final STNode closeBrace;

    STMatchStatementNode(
            STNode matchKeyword,
            STNode condition,
            STNode openBrace,
            STNode matchClauses,
            STNode closeBrace) {
        this(
                matchKeyword,
                condition,
                openBrace,
                matchClauses,
                closeBrace,
                Collections.emptyList());
    }

    STMatchStatementNode(
            STNode matchKeyword,
            STNode condition,
            STNode openBrace,
            STNode matchClauses,
            STNode closeBrace,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.MATCH_STATEMENT, diagnostics);
        this.matchKeyword = matchKeyword;
        this.condition = condition;
        this.openBrace = openBrace;
        this.matchClauses = matchClauses;
        this.closeBrace = closeBrace;

        addChildren(
                matchKeyword,
                condition,
                openBrace,
                matchClauses,
                closeBrace);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMatchStatementNode(
                this.matchKeyword,
                this.condition,
                this.openBrace,
                this.matchClauses,
                this.closeBrace,
                diagnostics);
    }

    public STMatchStatementNode modify(
            STNode matchKeyword,
            STNode condition,
            STNode openBrace,
            STNode matchClauses,
            STNode closeBrace) {
        if (checkForReferenceEquality(
                matchKeyword,
                condition,
                openBrace,
                matchClauses,
                closeBrace)) {
            return this;
        }

        return new STMatchStatementNode(
                matchKeyword,
                condition,
                openBrace,
                matchClauses,
                closeBrace,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MatchStatementNode(this, position, parent);
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
