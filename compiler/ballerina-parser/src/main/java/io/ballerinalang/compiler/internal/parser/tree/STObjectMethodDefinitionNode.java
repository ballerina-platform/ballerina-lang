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
import io.ballerinalang.compiler.syntax.tree.ObjectMethodDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STObjectMethodDefinitionNode extends STNode {
    public final STNode metadata;
    public final STNode visibilityQualifier;
    public final STNode remoteKeyword;
    public final STNode transactionalKeyword;
    public final STNode functionKeyword;
    public final STNode methodName;
    public final STNode methodSignature;
    public final STNode functionBody;

    STObjectMethodDefinitionNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode remoteKeyword,
            STNode transactionalKeyword,
            STNode functionKeyword,
            STNode methodName,
            STNode methodSignature,
            STNode functionBody) {
        this(
                metadata,
                visibilityQualifier,
                remoteKeyword,
                transactionalKeyword,
                functionKeyword,
                methodName,
                methodSignature,
                functionBody,
                Collections.emptyList());
    }

    STObjectMethodDefinitionNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode remoteKeyword,
            STNode transactionalKeyword,
            STNode functionKeyword,
            STNode methodName,
            STNode methodSignature,
            STNode functionBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.OBJECT_METHOD_DEFINITION, diagnostics);
        this.metadata = metadata;
        this.visibilityQualifier = visibilityQualifier;
        this.remoteKeyword = remoteKeyword;
        this.transactionalKeyword = transactionalKeyword;
        this.functionKeyword = functionKeyword;
        this.methodName = methodName;
        this.methodSignature = methodSignature;
        this.functionBody = functionBody;

        addChildren(
                metadata,
                visibilityQualifier,
                remoteKeyword,
                transactionalKeyword,
                functionKeyword,
                methodName,
                methodSignature,
                functionBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STObjectMethodDefinitionNode(
                this.metadata,
                this.visibilityQualifier,
                this.remoteKeyword,
                this.transactionalKeyword,
                this.functionKeyword,
                this.methodName,
                this.methodSignature,
                this.functionBody,
                diagnostics);
    }

    public STObjectMethodDefinitionNode modify(
            STNode metadata,
            STNode visibilityQualifier,
            STNode remoteKeyword,
            STNode transactionalKeyword,
            STNode functionKeyword,
            STNode methodName,
            STNode methodSignature,
            STNode functionBody) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                remoteKeyword,
                transactionalKeyword,
                functionKeyword,
                methodName,
                methodSignature,
                functionBody)) {
            return this;
        }

        return new STObjectMethodDefinitionNode(
                metadata,
                visibilityQualifier,
                remoteKeyword,
                transactionalKeyword,
                functionKeyword,
                methodName,
                methodSignature,
                functionBody,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ObjectMethodDefinitionNode(this, position, parent);
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
