/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.ReFlagsOnOffNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.3.0
 */
public class STReFlagsOnOffNode extends STNode {
    public final STNode lhsReFlags;
    public final STNode minusToken;
    public final STNode rhsReFlags;

    STReFlagsOnOffNode(
            STNode lhsReFlags,
            STNode minusToken,
            STNode rhsReFlags) {
        this(
                lhsReFlags,
                minusToken,
                rhsReFlags,
                Collections.emptyList());
    }

    STReFlagsOnOffNode(
            STNode lhsReFlags,
            STNode minusToken,
            STNode rhsReFlags,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RE_FLAGS_ON_OFF, diagnostics);
        this.lhsReFlags = lhsReFlags;
        this.minusToken = minusToken;
        this.rhsReFlags = rhsReFlags;

        addChildren(
                lhsReFlags,
                minusToken,
                rhsReFlags);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReFlagsOnOffNode(
                this.lhsReFlags,
                this.minusToken,
                this.rhsReFlags,
                diagnostics);
    }

    public STReFlagsOnOffNode modify(
            STNode lhsReFlags,
            STNode minusToken,
            STNode rhsReFlags) {
        if (checkForReferenceEquality(
                lhsReFlags,
                minusToken,
                rhsReFlags)) {
            return this;
        }

        return new STReFlagsOnOffNode(
                lhsReFlags,
                minusToken,
                rhsReFlags,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReFlagsOnOffNode(this, position, parent);
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
