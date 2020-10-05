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
import io.ballerina.compiler.syntax.tree.ServiceRemoteAttachPointIdentifierNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STServiceRemoteAttachPointIdentifierNode extends STNode {
    public final STNode serviceKeyword;
    public final STNode remoteKeyword;

    STServiceRemoteAttachPointIdentifierNode(
            STNode serviceKeyword,
            STNode remoteKeyword) {
        this(
                serviceKeyword,
                remoteKeyword,
                Collections.emptyList());
    }

    STServiceRemoteAttachPointIdentifierNode(
            STNode serviceKeyword,
            STNode remoteKeyword,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.SERVICE_REMOTE_ATTACH_POINT_IDENTIFIER, diagnostics);
        this.serviceKeyword = serviceKeyword;
        this.remoteKeyword = remoteKeyword;

        addChildren(
                serviceKeyword,
                remoteKeyword);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STServiceRemoteAttachPointIdentifierNode(
                this.serviceKeyword,
                this.remoteKeyword,
                diagnostics);
    }

    public STServiceRemoteAttachPointIdentifierNode modify(
            STNode serviceKeyword,
            STNode remoteKeyword) {
        if (checkForReferenceEquality(
                serviceKeyword,
                remoteKeyword)) {
            return this;
        }

        return new STServiceRemoteAttachPointIdentifierNode(
                serviceKeyword,
                remoteKeyword,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ServiceRemoteAttachPointIdentifierNode(this, position, parent);
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
