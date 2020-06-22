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

import io.ballerinalang.compiler.syntax.tree.IfElseStatementNode;
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
public class STIfElseStatementNode extends STStatementNode {
    public final STNode ifKeyword;
    public final STNode condition;
    public final STNode ifBody;
    public final STNode elseBody;

    STIfElseStatementNode(
            STNode ifKeyword,
            STNode condition,
            STNode ifBody,
            STNode elseBody) {
        this(
                ifKeyword,
                condition,
                ifBody,
                elseBody,
                Collections.emptyList());
    }

    STIfElseStatementNode(
            STNode ifKeyword,
            STNode condition,
            STNode ifBody,
            STNode elseBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.IF_ELSE_STATEMENT, diagnostics);
        this.ifKeyword = ifKeyword;
        this.condition = condition;
        this.ifBody = ifBody;
        this.elseBody = elseBody;

        addChildren(
                ifKeyword,
                condition,
                ifBody,
                elseBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STIfElseStatementNode(
                this.ifKeyword,
                this.condition,
                this.ifBody,
                this.elseBody,
                diagnostics);
    }

    public STIfElseStatementNode modify(
            STNode ifKeyword,
            STNode condition,
            STNode ifBody,
            STNode elseBody) {
        if (checkForReferenceEquality(
                ifKeyword,
                condition,
                ifBody,
                elseBody)) {
            return this;
        }

        return new STIfElseStatementNode(
                ifKeyword,
                condition,
                ifBody,
                elseBody,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new IfElseStatementNode(this, position, parent);
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
