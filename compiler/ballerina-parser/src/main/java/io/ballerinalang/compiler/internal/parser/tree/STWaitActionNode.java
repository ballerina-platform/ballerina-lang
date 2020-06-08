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
import io.ballerinalang.compiler.syntax.tree.WaitActionNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STWaitActionNode extends STActionNode {
    public final STNode waitKeyword;
    public final STNode waitFutureExpr;

    STWaitActionNode(
            STNode waitKeyword,
            STNode waitFutureExpr) {
        this(
                waitKeyword,
                waitFutureExpr,
                Collections.emptyList());
    }

    STWaitActionNode(
            STNode waitKeyword,
            STNode waitFutureExpr,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.WAIT_ACTION, diagnostics);
        this.waitKeyword = waitKeyword;
        this.waitFutureExpr = waitFutureExpr;

        addChildren(
                waitKeyword,
                waitFutureExpr);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STWaitActionNode(
                this.waitKeyword,
                this.waitFutureExpr,
                diagnostics);
    }

    public STWaitActionNode modify(
            STNode waitKeyword,
            STNode waitFutureExpr) {
        if (checkForReferenceEquality(
                waitKeyword,
                waitFutureExpr)) {
            return this;
        }

        return new STWaitActionNode(
                waitKeyword,
                waitFutureExpr,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new WaitActionNode(this, position, parent);
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
