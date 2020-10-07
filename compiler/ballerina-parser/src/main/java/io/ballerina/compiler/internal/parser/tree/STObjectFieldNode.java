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
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STObjectFieldNode extends STNode {
    public final STNode metadata;
    public final STNode visibilityQualifier;
    public final STNode finalKeyword;
    public final STNode typeName;
    public final STNode fieldName;
    public final STNode equalsToken;
    public final STNode expression;
    public final STNode semicolonToken;

    STObjectFieldNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode finalKeyword,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {
        this(
                metadata,
                visibilityQualifier,
                finalKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken,
                Collections.emptyList());
    }

    STObjectFieldNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode finalKeyword,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.OBJECT_FIELD, diagnostics);
        this.metadata = metadata;
        this.visibilityQualifier = visibilityQualifier;
        this.finalKeyword = finalKeyword;
        this.typeName = typeName;
        this.fieldName = fieldName;
        this.equalsToken = equalsToken;
        this.expression = expression;
        this.semicolonToken = semicolonToken;

        addChildren(
                metadata,
                visibilityQualifier,
                finalKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STObjectFieldNode(
                this.metadata,
                this.visibilityQualifier,
                this.finalKeyword,
                this.typeName,
                this.fieldName,
                this.equalsToken,
                this.expression,
                this.semicolonToken,
                diagnostics);
    }

    public STObjectFieldNode modify(
            STNode metadata,
            STNode visibilityQualifier,
            STNode finalKeyword,
            STNode typeName,
            STNode fieldName,
            STNode equalsToken,
            STNode expression,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                finalKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken)) {
            return this;
        }

        return new STObjectFieldNode(
                metadata,
                visibilityQualifier,
                finalKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ObjectFieldNode(this, position, parent);
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
