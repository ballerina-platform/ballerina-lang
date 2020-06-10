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

import io.ballerinalang.compiler.syntax.tree.ListenerDeclarationNode;
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
public class STListenerDeclarationNode extends STModuleMemberDeclarationNode {
    public final STNode metadata;
    public final STNode visibilityQualifier;
    public final STNode listenerKeyword;
    public final STNode typeDescriptor;
    public final STNode variableName;
    public final STNode equalsToken;
    public final STNode initializer;
    public final STNode semicolonToken;

    STListenerDeclarationNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode listenerKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {
        this(
                metadata,
                visibilityQualifier,
                listenerKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken,
                Collections.emptyList());
    }

    STListenerDeclarationNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode listenerKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.LISTENER_DECLARATION, diagnostics);
        this.metadata = metadata;
        this.visibilityQualifier = visibilityQualifier;
        this.listenerKeyword = listenerKeyword;
        this.typeDescriptor = typeDescriptor;
        this.variableName = variableName;
        this.equalsToken = equalsToken;
        this.initializer = initializer;
        this.semicolonToken = semicolonToken;

        addChildren(
                metadata,
                visibilityQualifier,
                listenerKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STListenerDeclarationNode(
                this.metadata,
                this.visibilityQualifier,
                this.listenerKeyword,
                this.typeDescriptor,
                this.variableName,
                this.equalsToken,
                this.initializer,
                this.semicolonToken,
                diagnostics);
    }

    public STListenerDeclarationNode modify(
            STNode metadata,
            STNode visibilityQualifier,
            STNode listenerKeyword,
            STNode typeDescriptor,
            STNode variableName,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                listenerKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken)) {
            return this;
        }

        return new STListenerDeclarationNode(
                metadata,
                visibilityQualifier,
                listenerKeyword,
                typeDescriptor,
                variableName,
                equalsToken,
                initializer,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ListenerDeclarationNode(this, position, parent);
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
