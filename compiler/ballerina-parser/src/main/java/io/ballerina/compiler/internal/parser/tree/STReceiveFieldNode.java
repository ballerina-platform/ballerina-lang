/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import io.ballerina.compiler.syntax.tree.ReceiveFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.9.0
 */
public class STReceiveFieldNode extends STNode {
    public final STNode fieldName;
    public final STNode colon;
    public final STNode peerWorker;

    STReceiveFieldNode(
            STNode fieldName,
            STNode colon,
            STNode peerWorker) {
        this(
                fieldName,
                colon,
                peerWorker,
                Collections.emptyList());
    }

    STReceiveFieldNode(
            STNode fieldName,
            STNode colon,
            STNode peerWorker,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RECEIVE_FIELD, diagnostics);
        this.fieldName = fieldName;
        this.colon = colon;
        this.peerWorker = peerWorker;

        addChildren(
                fieldName,
                colon,
                peerWorker);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReceiveFieldNode(
                this.fieldName,
                this.colon,
                this.peerWorker,
                diagnostics);
    }

    public STReceiveFieldNode modify(
            STNode fieldName,
            STNode colon,
            STNode peerWorker) {
        if (checkForReferenceEquality(
                fieldName,
                colon,
                peerWorker)) {
            return this;
        }

        return new STReceiveFieldNode(
                fieldName,
                colon,
                peerWorker,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReceiveFieldNode(this, position, parent);
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
