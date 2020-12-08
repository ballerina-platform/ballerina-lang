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
import io.ballerina.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STOnConflictClauseNode extends STClauseNode {
    public final STNode onKeyword;
    public final STNode conflictKeyword;
    public final STNode expression;

    STOnConflictClauseNode(
            STNode onKeyword,
            STNode conflictKeyword,
            STNode expression) {
        this(
                onKeyword,
                conflictKeyword,
                expression,
                Collections.emptyList());
    }

    STOnConflictClauseNode(
            STNode onKeyword,
            STNode conflictKeyword,
            STNode expression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ON_CONFLICT_CLAUSE, diagnostics);
        this.onKeyword = onKeyword;
        this.conflictKeyword = conflictKeyword;
        this.expression = expression;

        addChildren(
                onKeyword,
                conflictKeyword,
                expression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STOnConflictClauseNode(
                this.onKeyword,
                this.conflictKeyword,
                this.expression,
                diagnostics);
    }

    public STOnConflictClauseNode modify(
            STNode onKeyword,
            STNode conflictKeyword,
            STNode expression) {
        if (checkForReferenceEquality(
                onKeyword,
                conflictKeyword,
                expression)) {
            return this;
        }

        return new STOnConflictClauseNode(
                onKeyword,
                conflictKeyword,
                expression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new OnConflictClauseNode(this, position, parent);
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
