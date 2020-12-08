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
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STWhileStatementNode extends STStatementNode {
    public final STNode whileKeyword;
    public final STNode condition;
    public final STNode whileBody;
    public final STNode onFailClause;

    STWhileStatementNode(
            STNode whileKeyword,
            STNode condition,
            STNode whileBody,
            STNode onFailClause) {
        this(
                whileKeyword,
                condition,
                whileBody,
                onFailClause,
                Collections.emptyList());
    }

    STWhileStatementNode(
            STNode whileKeyword,
            STNode condition,
            STNode whileBody,
            STNode onFailClause,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.WHILE_STATEMENT, diagnostics);
        this.whileKeyword = whileKeyword;
        this.condition = condition;
        this.whileBody = whileBody;
        this.onFailClause = onFailClause;

        addChildren(
                whileKeyword,
                condition,
                whileBody,
                onFailClause);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STWhileStatementNode(
                this.whileKeyword,
                this.condition,
                this.whileBody,
                this.onFailClause,
                diagnostics);
    }

    public STWhileStatementNode modify(
            STNode whileKeyword,
            STNode condition,
            STNode whileBody,
            STNode onFailClause) {
        if (checkForReferenceEquality(
                whileKeyword,
                condition,
                whileBody,
                onFailClause)) {
            return this;
        }

        return new STWhileStatementNode(
                whileKeyword,
                condition,
                whileBody,
                onFailClause,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new WhileStatementNode(this, position, parent);
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
