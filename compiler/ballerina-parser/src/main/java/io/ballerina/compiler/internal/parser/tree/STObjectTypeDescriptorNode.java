/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STObjectTypeDescriptorNode extends STTypeDescriptorNode {
    public final STNode objectTypeQualifiers;
    public final STNode objectKeyword;
    public final STNode openBrace;
    public final STNode members;
    public final STNode closeBrace;

    STObjectTypeDescriptorNode(
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode openBrace,
            STNode members,
            STNode closeBrace) {
        this(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace,
                Collections.emptyList());
    }

    STObjectTypeDescriptorNode(
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode openBrace,
            STNode members,
            STNode closeBrace,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.OBJECT_TYPE_DESC, diagnostics);
        this.objectTypeQualifiers = objectTypeQualifiers;
        this.objectKeyword = objectKeyword;
        this.openBrace = openBrace;
        this.members = members;
        this.closeBrace = closeBrace;

        addChildren(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STObjectTypeDescriptorNode(
                this.objectTypeQualifiers,
                this.objectKeyword,
                this.openBrace,
                this.members,
                this.closeBrace,
                diagnostics);
    }

    public STObjectTypeDescriptorNode modify(
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode openBrace,
            STNode members,
            STNode closeBrace) {
        if (checkForReferenceEquality(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace)) {
            return this;
        }

        return new STObjectTypeDescriptorNode(
                objectTypeQualifiers,
                objectKeyword,
                openBrace,
                members,
                closeBrace,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ObjectTypeDescriptorNode(this, position, parent);
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
