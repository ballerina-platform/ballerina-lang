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

import io.ballerinalang.compiler.syntax.tree.InferDefaultValueNode;
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
public class STInferDefaultValueNode extends STExpressionNode {
    public final STNode ltToken;
    public final STNode gtToken;

    STInferDefaultValueNode(
            STNode ltToken,
            STNode gtToken) {
        this(
                ltToken,
                gtToken,
                Collections.emptyList());
    }

    STInferDefaultValueNode(
            STNode ltToken,
            STNode gtToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.REST_ARG, diagnostics);
        this.ltToken = ltToken;
        this.gtToken = gtToken;

        addChildren(
                ltToken,
                gtToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STInferDefaultValueNode(
                this.ltToken,
                this.gtToken,
                diagnostics);
    }

    public STInferDefaultValueNode modify(
            STNode ltToken,
            STNode gtToken) {
        if (checkForReferenceEquality(
                ltToken,
                gtToken)) {
            return this;
        }

        return new STInferDefaultValueNode(
                ltToken,
                gtToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new InferDefaultValueNode(this, position, parent);
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
