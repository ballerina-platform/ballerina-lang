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
    public final STNode serviceKeyword;
    public final STNode serviceName;
    public final STNode onKeyword;
    public final STNode expressions;
    public final STNode serviceBody;

    STServiceDeclarationNode(
            STNode metadata,
            STNode serviceKeyword,
            STNode serviceName,
            STNode onKeyword,
            STNode expressions,
            STNode serviceBody) {
        this(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody,
                Collections.emptyList());
    }

    STServiceDeclarationNode(
            STNode metadata,
            STNode serviceKeyword,
            STNode serviceName,
            STNode onKeyword,
            STNode expressions,
            STNode serviceBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.SERVICE_DECLARATION, diagnostics);
        this.metadata = metadata;
        this.serviceKeyword = serviceKeyword;
        this.serviceName = serviceName;
        this.onKeyword = onKeyword;
        this.expressions = expressions;
        this.serviceBody = serviceBody;

        addChildren(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STServiceDeclarationNode(
                this.metadata,
                this.serviceKeyword,
                this.serviceName,
                this.onKeyword,
                this.expressions,
                this.serviceBody,
                diagnostics);
    }

    public STServiceDeclarationNode modify(
            STNode metadata,
            STNode serviceKeyword,
            STNode serviceName,
            STNode onKeyword,
            STNode expressions,
            STNode serviceBody) {
        if (checkForReferenceEquality(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody)) {
            return this;
        }

        return new STServiceDeclarationNode(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody,
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
