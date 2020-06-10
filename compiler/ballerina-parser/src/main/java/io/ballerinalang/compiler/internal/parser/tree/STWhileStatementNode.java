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

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.WhileStatementNode;

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

    STWhileStatementNode(
            STNode whileKeyword,
            STNode condition,
            STNode whileBody) {
        this(
                whileKeyword,
                condition,
                whileBody,
                Collections.emptyList());
    }

    STWhileStatementNode(
            STNode whileKeyword,
            STNode condition,
            STNode whileBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.WHILE_STATEMENT, diagnostics);
        this.whileKeyword = whileKeyword;
        this.condition = condition;
        this.whileBody = whileBody;

        addChildren(
                whileKeyword,
                condition,
                whileBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STWhileStatementNode(
                this.whileKeyword,
                this.condition,
                this.whileBody,
                diagnostics);
    }

    public STWhileStatementNode modify(
            STNode whileKeyword,
            STNode condition,
            STNode whileBody) {
        if (checkForReferenceEquality(
                whileKeyword,
                condition,
                whileBody)) {
            return this;
        }

        return new STWhileStatementNode(
                whileKeyword,
                condition,
                whileBody,
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
