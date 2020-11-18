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
import io.ballerina.compiler.syntax.tree.OnClauseNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STOnClauseNode extends STClauseNode {
    public final STNode onKeyword;
    public final STNode lhsExpression;
    public final STNode equalsKeyword;
    public final STNode rhsExpression;

    STOnClauseNode(
            STNode onKeyword,
            STNode lhsExpression,
            STNode equalsKeyword,
            STNode rhsExpression) {
        this(
                onKeyword,
                lhsExpression,
                equalsKeyword,
                rhsExpression,
                Collections.emptyList());
    }

    STOnClauseNode(
            STNode onKeyword,
            STNode lhsExpression,
            STNode equalsKeyword,
            STNode rhsExpression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ON_CLAUSE, diagnostics);
        this.onKeyword = onKeyword;
        this.lhsExpression = lhsExpression;
        this.equalsKeyword = equalsKeyword;
        this.rhsExpression = rhsExpression;

        addChildren(
                onKeyword,
                lhsExpression,
                equalsKeyword,
                rhsExpression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STOnClauseNode(
                this.onKeyword,
                this.lhsExpression,
                this.equalsKeyword,
                this.rhsExpression,
                diagnostics);
    }

    public STOnClauseNode modify(
            STNode onKeyword,
            STNode lhsExpression,
            STNode equalsKeyword,
            STNode rhsExpression) {
        if (checkForReferenceEquality(
                onKeyword,
                lhsExpression,
                equalsKeyword,
                rhsExpression)) {
            return this;
        }

        return new STOnClauseNode(
                onKeyword,
                lhsExpression,
                equalsKeyword,
                rhsExpression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new OnClauseNode(this, position, parent);
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
