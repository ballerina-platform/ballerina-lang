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

import io.ballerina.compiler.syntax.tree.MatchClauseNode;
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
public class STMatchClauseNode extends STNode {
    public final STNode matchPatterns;
    public final STNode matchGuard;
    public final STNode rightDoubleArrow;
    public final STNode blockStatement;

    STMatchClauseNode(
            STNode matchPatterns,
            STNode matchGuard,
            STNode rightDoubleArrow,
            STNode blockStatement) {
        this(
                matchPatterns,
                matchGuard,
                rightDoubleArrow,
                blockStatement,
                Collections.emptyList());
    }

    STMatchClauseNode(
            STNode matchPatterns,
            STNode matchGuard,
            STNode rightDoubleArrow,
            STNode blockStatement,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.MATCH_CLAUSE, diagnostics);
        this.matchPatterns = matchPatterns;
        this.matchGuard = matchGuard;
        this.rightDoubleArrow = rightDoubleArrow;
        this.blockStatement = blockStatement;

        addChildren(
                matchPatterns,
                matchGuard,
                rightDoubleArrow,
                blockStatement);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMatchClauseNode(
                this.matchPatterns,
                this.matchGuard,
                this.rightDoubleArrow,
                this.blockStatement,
                diagnostics);
    }

    public STMatchClauseNode modify(
            STNode matchPatterns,
            STNode matchGuard,
            STNode rightDoubleArrow,
            STNode blockStatement) {
        if (checkForReferenceEquality(
                matchPatterns,
                matchGuard,
                rightDoubleArrow,
                blockStatement)) {
            return this;
        }

        return new STMatchClauseNode(
                matchPatterns,
                matchGuard,
                rightDoubleArrow,
                blockStatement,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MatchClauseNode(this, position, parent);
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
