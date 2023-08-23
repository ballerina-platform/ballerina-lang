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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.WhereClauseNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STWhereClauseNode extends STIntermediateClauseNode {
    public final STNode whereKeyword;
    public final STNode expression;

    STWhereClauseNode(
            STNode whereKeyword,
            STNode expression) {
        this(
                whereKeyword,
                expression,
                Collections.emptyList());
    }

    STWhereClauseNode(
            STNode whereKeyword,
            STNode expression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.WHERE_CLAUSE, diagnostics);
        this.whereKeyword = whereKeyword;
        this.expression = expression;

        addChildren(
                whereKeyword,
                expression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STWhereClauseNode(
                this.whereKeyword,
                this.expression,
                diagnostics);
    }

    public STWhereClauseNode modify(
            STNode whereKeyword,
            STNode expression) {
        if (checkForReferenceEquality(
                whereKeyword,
                expression)) {
            return this;
        }

        return new STWhereClauseNode(
                whereKeyword,
                expression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new WhereClauseNode(this, position, parent);
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
