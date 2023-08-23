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

import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
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
public class STConstantDeclarationNode extends STModuleMemberDeclarationNode {
    public final STNode metadata;
    public final STNode visibilityQualifier;
    public final STNode constKeyword;
    public final STNode typeDescriptor;
    public final STNode variableName;
    public final STNode equalsToken;
    public final STNode initializer;
    public final STNode semicolonToken;

    STConstantDeclarationNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode constKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {
        this(
                metadata,
                visibilityQualifier,
                constKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken,
                Collections.emptyList());
    }

    STConstantDeclarationNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode constKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.CONST_DECLARATION, diagnostics);
        this.metadata = metadata;
        this.visibilityQualifier = visibilityQualifier;
        this.constKeyword = constKeyword;
        this.typeDescriptor = typeDescriptor;
        this.variableName = variableName;
        this.equalsToken = equalsToken;
        this.initializer = initializer;
        this.semicolonToken = semicolonToken;

        addChildren(
                metadata,
                visibilityQualifier,
                constKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STConstantDeclarationNode(
                this.metadata,
                this.visibilityQualifier,
                this.constKeyword,
                this.typeDescriptor,
                this.variableName,
                this.equalsToken,
                this.initializer,
                this.semicolonToken,
                diagnostics);
    }

    public STConstantDeclarationNode modify(
            STNode metadata,
            STNode visibilityQualifier,
            STNode constKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                constKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken)) {
            return this;
        }

        return new STConstantDeclarationNode(
                metadata,
                visibilityQualifier,
                constKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ConstantDeclarationNode(this, position, parent);
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
