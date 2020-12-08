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
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STObjectConstructorExpressionNode extends STExpressionNode {
    public final STNode annotations;
    public final STNode objectTypeQualifiers;
    public final STNode objectKeyword;
    public final STNode typeReference;
    public final STNode openBraceToken;
    public final STNode members;
    public final STNode closeBraceToken;

    STObjectConstructorExpressionNode(
            STNode annotations,
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode typeReference,
            STNode openBraceToken,
            STNode members,
            STNode closeBraceToken) {
        this(
                annotations,
                objectTypeQualifiers,
                objectKeyword,
                typeReference,
                openBraceToken,
                members,
                closeBraceToken,
                Collections.emptyList());
    }

    STObjectConstructorExpressionNode(
            STNode annotations,
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode typeReference,
            STNode openBraceToken,
            STNode members,
            STNode closeBraceToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.OBJECT_CONSTRUCTOR, diagnostics);
        this.annotations = annotations;
        this.objectTypeQualifiers = objectTypeQualifiers;
        this.objectKeyword = objectKeyword;
        this.typeReference = typeReference;
        this.openBraceToken = openBraceToken;
        this.members = members;
        this.closeBraceToken = closeBraceToken;

        addChildren(
                annotations,
                objectTypeQualifiers,
                objectKeyword,
                typeReference,
                openBraceToken,
                members,
                closeBraceToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STObjectConstructorExpressionNode(
                this.annotations,
                this.objectTypeQualifiers,
                this.objectKeyword,
                this.typeReference,
                this.openBraceToken,
                this.members,
                this.closeBraceToken,
                diagnostics);
    }

    public STObjectConstructorExpressionNode modify(
            STNode annotations,
            STNode objectTypeQualifiers,
            STNode objectKeyword,
            STNode typeReference,
            STNode openBraceToken,
            STNode members,
            STNode closeBraceToken) {
        if (checkForReferenceEquality(
                annotations,
                objectTypeQualifiers,
                objectKeyword,
                typeReference,
                openBraceToken,
                members,
                closeBraceToken)) {
            return this;
        }

        return new STObjectConstructorExpressionNode(
                annotations,
                objectTypeQualifiers,
                objectKeyword,
                typeReference,
                openBraceToken,
                members,
                closeBraceToken,
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
