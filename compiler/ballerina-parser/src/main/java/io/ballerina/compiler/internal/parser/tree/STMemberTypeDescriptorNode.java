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

import io.ballerina.compiler.syntax.tree.MemberTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.4.0
 */
public class STMemberTypeDescriptorNode extends STNode {
    public final STNode annotations;
    public final STNode typeDescriptor;

    STMemberTypeDescriptorNode(
            STNode annotations,
            STNode typeDescriptor) {
        this(
                annotations,
                typeDescriptor,
                Collections.emptyList());
    }

    STMemberTypeDescriptorNode(
            STNode annotations,
            STNode typeDescriptor,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.MEMBER_TYPE_DESC, diagnostics);
        this.annotations = annotations;
        this.typeDescriptor = typeDescriptor;

        addChildren(
                annotations,
                typeDescriptor);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMemberTypeDescriptorNode(
                this.annotations,
                this.typeDescriptor,
                diagnostics);
    }

    public STMemberTypeDescriptorNode modify(
            STNode annotations,
            STNode typeDescriptor) {
        if (checkForReferenceEquality(
                annotations,
                typeDescriptor)) {
            return this;
        }

        return new STMemberTypeDescriptorNode(
                annotations,
                typeDescriptor,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MemberTypeDescriptorNode(this, position, parent);
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
