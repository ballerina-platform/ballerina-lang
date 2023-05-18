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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STServiceDeclarationNode extends STModuleMemberDeclarationNode {
    public final STNode metadata;
    public final STNode qualifiers;
    public final STNode serviceKeyword;
    public final STNode typeDescriptor;
    public final STNode absoluteResourcePath;
    public final STNode onKeyword;
    public final STNode expressions;
    public final STNode openBraceToken;
    public final STNode members;
    public final STNode closeBraceToken;
    public final STNode semicolonToken;

    STServiceDeclarationNode(
            STNode metadata,
            STNode qualifiers,
            STNode serviceKeyword,
            STNode typeDescriptor,
            STNode absoluteResourcePath,
            STNode onKeyword,
            STNode expressions,
            STNode openBraceToken,
            STNode members,
            STNode closeBraceToken,
            STNode semicolonToken) {
        this(
                metadata,
                qualifiers,
                serviceKeyword,
                typeDescriptor,
                absoluteResourcePath,
                onKeyword,
                expressions,
                openBraceToken,
                members,
                closeBraceToken,
                semicolonToken,
                Collections.emptyList());
    }

    STServiceDeclarationNode(
            STNode metadata,
            STNode qualifiers,
            STNode serviceKeyword,
            STNode typeDescriptor,
            STNode absoluteResourcePath,
            STNode onKeyword,
            STNode expressions,
            STNode openBraceToken,
            STNode members,
            STNode closeBraceToken,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.SERVICE_DECLARATION, diagnostics);
        this.metadata = metadata;
        this.qualifiers = qualifiers;
        this.serviceKeyword = serviceKeyword;
        this.typeDescriptor = typeDescriptor;
        this.absoluteResourcePath = absoluteResourcePath;
        this.onKeyword = onKeyword;
        this.expressions = expressions;
        this.openBraceToken = openBraceToken;
        this.members = members;
        this.closeBraceToken = closeBraceToken;
        this.semicolonToken = semicolonToken;

        addChildren(
                metadata,
                qualifiers,
                serviceKeyword,
                typeDescriptor,
                absoluteResourcePath,
                onKeyword,
                expressions,
                openBraceToken,
                members,
                closeBraceToken,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STServiceDeclarationNode(
                this.metadata,
                this.qualifiers,
                this.serviceKeyword,
                this.typeDescriptor,
                this.absoluteResourcePath,
                this.onKeyword,
                this.expressions,
                this.openBraceToken,
                this.members,
                this.closeBraceToken,
                this.semicolonToken,
                diagnostics);
    }

    public STServiceDeclarationNode modify(
            STNode metadata,
            STNode qualifiers,
            STNode serviceKeyword,
            STNode typeDescriptor,
            STNode absoluteResourcePath,
            STNode onKeyword,
            STNode expressions,
            STNode openBraceToken,
            STNode members,
            STNode closeBraceToken,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                qualifiers,
                serviceKeyword,
                typeDescriptor,
                absoluteResourcePath,
                onKeyword,
                expressions,
                openBraceToken,
                members,
                closeBraceToken,
                semicolonToken)) {
            return this;
        }

        return new STServiceDeclarationNode(
                metadata,
                qualifiers,
                serviceKeyword,
                typeDescriptor,
                absoluteResourcePath,
                onKeyword,
                expressions,
                openBraceToken,
                members,
                closeBraceToken,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ServiceDeclarationNode(this, position, parent);
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
