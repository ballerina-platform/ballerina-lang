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
import io.ballerinalang.compiler.syntax.tree.TrippleGTTokenNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTrippleGTTokenNode extends STNode {
    public final STNode openGTToken;
    public final STNode middleGTToken;
    public final STNode endGTToken;

    STTrippleGTTokenNode(
            STNode openGTToken,
            STNode middleGTToken,
            STNode endGTToken) {
        this(
                openGTToken,
                middleGTToken,
                endGTToken,
                Collections.emptyList());
    }

    STTrippleGTTokenNode(
            STNode openGTToken,
            STNode middleGTToken,
            STNode endGTToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TRIPPLE_GT_TOKEN, diagnostics);
        this.openGTToken = openGTToken;
        this.middleGTToken = middleGTToken;
        this.endGTToken = endGTToken;

        addChildren(
                openGTToken,
                middleGTToken,
                endGTToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTrippleGTTokenNode(
                this.openGTToken,
                this.middleGTToken,
                this.endGTToken,
                diagnostics);
    }

    public STTrippleGTTokenNode modify(
            STNode openGTToken,
            STNode middleGTToken,
            STNode endGTToken) {
        if (checkForReferenceEquality(
                openGTToken,
                middleGTToken,
                endGTToken)) {
            return this;
        }

        return new STTrippleGTTokenNode(
                openGTToken,
                middleGTToken,
                endGTToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new TrippleGTTokenNode(this, position, parent);
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
