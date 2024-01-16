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
import io.ballerina.compiler.syntax.tree.SingleReceiveNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.9.0
 */
public class STSingleReceiveNode extends STNode {
    public final STNode worker;

    STSingleReceiveNode(
            STNode worker) {
        this(
                worker,
                Collections.emptyList());
    }

    STSingleReceiveNode(
            STNode worker,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.SINGLE_RECEIVE, diagnostics);
        this.worker = worker;

        addChildren(
                worker);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STSingleReceiveNode(
                this.worker,
                diagnostics);
    }

    public STSingleReceiveNode modify(
            STNode worker) {
        if (checkForReferenceEquality(
                worker)) {
            return this;
        }

        return new STSingleReceiveNode(
                worker,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new SingleReceiveNode(this, position, parent);
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
