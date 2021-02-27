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

import io.ballerina.compiler.syntax.tree.FlushActionNode;
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
public class STFlushActionNode extends STExpressionNode {
    public final STNode flushKeyword;
    public final STNode peerWorker;

    STFlushActionNode(
            STNode flushKeyword,
            STNode peerWorker) {
        this(
                flushKeyword,
                peerWorker,
                Collections.emptyList());
    }

    STFlushActionNode(
            STNode flushKeyword,
            STNode peerWorker,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.FLUSH_ACTION, diagnostics);
        this.flushKeyword = flushKeyword;
        this.peerWorker = peerWorker;

        addChildren(
                flushKeyword,
                peerWorker);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STFlushActionNode(
                this.flushKeyword,
                this.peerWorker,
                diagnostics);
    }

    public STFlushActionNode modify(
            STNode flushKeyword,
            STNode peerWorker) {
        if (checkForReferenceEquality(
                flushKeyword,
                peerWorker)) {
            return this;
        }

        return new STFlushActionNode(
                flushKeyword,
                peerWorker,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new FlushActionNode(this, position, parent);
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
