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
import io.ballerina.compiler.syntax.tree.ReFlagsNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.3.0
 */
public class STReFlagsNode extends STNode {
    public final STNode reFlag;

    STReFlagsNode(
            STNode reFlag) {
        this(
                reFlag,
                Collections.emptyList());
    }

    STReFlagsNode(
            STNode reFlag,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RE_FLAGS, diagnostics);
        this.reFlag = reFlag;

        addChildren(
                reFlag);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReFlagsNode(
                this.reFlag,
                diagnostics);
    }

    public STReFlagsNode modify(
            STNode reFlag) {
        if (checkForReferenceEquality(
                reFlag)) {
            return this;
        }

        return new STReFlagsNode(
                reFlag,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReFlagsNode(this, position, parent);
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
