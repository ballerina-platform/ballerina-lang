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
import io.ballerinalang.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STObjectConstructorExpressionNode extends STExpressionNode {
    public final STNode annotations;
    public final STNode objectTypeQualifier;
    public final STNode objectKeyword;
    public final STNode typeDescriptor;
    public final STNode objectConstructorBody;

    STObjectConstructorExpressionNode(
            STNode annotations,
            STNode objectTypeQualifier,
            STNode objectKeyword,
            STNode typeDescriptor,
            STNode objectConstructorBody) {
        this(
                annotations,
                objectTypeQualifier,
                objectKeyword,
                typeDescriptor,
                objectConstructorBody,
                Collections.emptyList());
    }

    STObjectConstructorExpressionNode(
            STNode annotations,
            STNode objectTypeQualifier,
            STNode objectKeyword,
            STNode typeDescriptor,
            STNode objectConstructorBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.OBJECT_CONSTRUCTOR, diagnostics);
        this.annotations = annotations;
        this.objectTypeQualifier = objectTypeQualifier;
        this.objectKeyword = objectKeyword;
        this.typeDescriptor = typeDescriptor;
        this.objectConstructorBody = objectConstructorBody;

        addChildren(
                annotations,
                objectTypeQualifier,
                objectKeyword,
                typeDescriptor,
                objectConstructorBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STObjectConstructorExpressionNode(
                this.annotations,
                this.objectTypeQualifier,
                this.objectKeyword,
                this.typeDescriptor,
                this.objectConstructorBody,
                diagnostics);
    }

    public STObjectConstructorExpressionNode modify(
            STNode annotations,
            STNode objectTypeQualifier,
            STNode objectKeyword,
            STNode typeDescriptor,
            STNode objectConstructorBody) {
        if (checkForReferenceEquality(
                annotations,
                objectTypeQualifier,
                objectKeyword,
                typeDescriptor,
                objectConstructorBody)) {
            return this;
        }

        return new STObjectConstructorExpressionNode(
                annotations,
                objectTypeQualifier,
                objectKeyword,
                typeDescriptor,
                objectConstructorBody,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ObjectConstructorExpressionNode(this, position, parent);
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
