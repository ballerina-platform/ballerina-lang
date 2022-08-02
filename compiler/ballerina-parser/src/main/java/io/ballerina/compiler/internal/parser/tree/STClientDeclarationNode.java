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

import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
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
public class STClientDeclarationNode extends STStatementNode {
    public final STNode clientKeyword;
    public final STNode clientUri;
    public final STNode asKeyword;
    public final STNode clientPrefix;
    public final STNode semicolonToken;

    STClientDeclarationNode(
            STNode clientKeyword,
            STNode clientUri,
            STNode asKeyword,
            STNode clientPrefix,
            STNode semicolonToken) {
        this(
                clientKeyword,
                clientUri,
                asKeyword,
                clientPrefix,
                semicolonToken,
                Collections.emptyList());
    }

    STClientDeclarationNode(
            STNode clientKeyword,
            STNode clientUri,
            STNode asKeyword,
            STNode clientPrefix,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.CLIENT_DECLARATION, diagnostics);
        this.clientKeyword = clientKeyword;
        this.clientUri = clientUri;
        this.asKeyword = asKeyword;
        this.clientPrefix = clientPrefix;
        this.semicolonToken = semicolonToken;

        addChildren(
                clientKeyword,
                clientUri,
                asKeyword,
                clientPrefix,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STClientDeclarationNode(
                this.clientKeyword,
                this.clientUri,
                this.asKeyword,
                this.clientPrefix,
                this.semicolonToken,
                diagnostics);
    }

    public STClientDeclarationNode modify(
            STNode clientKeyword,
            STNode clientUri,
            STNode asKeyword,
            STNode clientPrefix,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                clientKeyword,
                clientUri,
                asKeyword,
                clientPrefix,
                semicolonToken)) {
            return this;
        }

        return new STClientDeclarationNode(
                clientKeyword,
                clientUri,
                asKeyword,
                clientPrefix,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ClientDeclarationNode(this, position, parent);
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
