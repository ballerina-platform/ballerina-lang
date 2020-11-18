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

import io.ballerina.compiler.syntax.tree.DoubleGTTokenNode;
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
public class STDoubleGTTokenNode extends STNode {
    public final STNode openGTToken;
    public final STNode endGTToken;

    STDoubleGTTokenNode(
            STNode openGTToken,
            STNode endGTToken) {
        this(
                openGTToken,
                endGTToken,
                Collections.emptyList());
    }

    STDoubleGTTokenNode(
            STNode openGTToken,
            STNode endGTToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.DOUBLE_GT_TOKEN, diagnostics);
        this.openGTToken = openGTToken;
        this.endGTToken = endGTToken;

        addChildren(
                openGTToken,
                endGTToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STDoubleGTTokenNode(
                this.openGTToken,
                this.endGTToken,
                diagnostics);
    }

    public STDoubleGTTokenNode modify(
            STNode openGTToken,
            STNode endGTToken) {
        if (checkForReferenceEquality(
                openGTToken,
                endGTToken)) {
            return this;
        }

        return new STDoubleGTTokenNode(
                openGTToken,
                endGTToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new DoubleGTTokenNode(this, position, parent);
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
