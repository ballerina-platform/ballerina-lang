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

import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
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
public class STIntersectionTypeDescriptorNode extends STTypeDescriptorNode {
    public final STNode leftTypeDesc;
    public final STNode bitwiseAndToken;
    public final STNode rightTypeDesc;

    STIntersectionTypeDescriptorNode(
            STNode leftTypeDesc,
            STNode bitwiseAndToken,
            STNode rightTypeDesc) {
        this(
                leftTypeDesc,
                bitwiseAndToken,
                rightTypeDesc,
                Collections.emptyList());
    }

    STIntersectionTypeDescriptorNode(
            STNode leftTypeDesc,
            STNode bitwiseAndToken,
            STNode rightTypeDesc,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.INTERSECTION_TYPE_DESC, diagnostics);
        this.leftTypeDesc = leftTypeDesc;
        this.bitwiseAndToken = bitwiseAndToken;
        this.rightTypeDesc = rightTypeDesc;

        addChildren(
                leftTypeDesc,
                bitwiseAndToken,
                rightTypeDesc);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STIntersectionTypeDescriptorNode(
                this.leftTypeDesc,
                this.bitwiseAndToken,
                this.rightTypeDesc,
                diagnostics);
    }

    public STIntersectionTypeDescriptorNode modify(
            STNode leftTypeDesc,
            STNode bitwiseAndToken,
            STNode rightTypeDesc) {
        if (checkForReferenceEquality(
                leftTypeDesc,
                bitwiseAndToken,
                rightTypeDesc)) {
            return this;
        }

        return new STIntersectionTypeDescriptorNode(
                leftTypeDesc,
                bitwiseAndToken,
                rightTypeDesc,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new IntersectionTypeDescriptorNode(this, position, parent);
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
