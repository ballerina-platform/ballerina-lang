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

import io.ballerina.compiler.syntax.tree.AsyncSendActionNode;
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
public class STAsyncSendActionNode extends STActionNode {
    public final STNode expression;
    public final STNode rightArrowToken;
    public final STNode peerWorker;

    STAsyncSendActionNode(
            STNode expression,
            STNode rightArrowToken,
            STNode peerWorker) {
        this(
                expression,
                rightArrowToken,
                peerWorker,
                Collections.emptyList());
    }

    STAsyncSendActionNode(
            STNode expression,
            STNode rightArrowToken,
            STNode peerWorker,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ASYNC_SEND_ACTION, diagnostics);
        this.expression = expression;
        this.rightArrowToken = rightArrowToken;
        this.peerWorker = peerWorker;

        addChildren(
                expression,
                rightArrowToken,
                peerWorker);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STAsyncSendActionNode(
                this.expression,
                this.rightArrowToken,
                this.peerWorker,
                diagnostics);
    }

    public STAsyncSendActionNode modify(
            STNode expression,
            STNode rightArrowToken,
            STNode peerWorker) {
        if (checkForReferenceEquality(
                expression,
                rightArrowToken,
                peerWorker)) {
            return this;
        }

        return new STAsyncSendActionNode(
                expression,
                rightArrowToken,
                peerWorker,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new AsyncSendActionNode(this, position, parent);
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
