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

import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
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
public class STExternalFunctionBodyNode extends STFunctionBodyNode {
    public final STNode equalsToken;
    public final STNode annotations;
    public final STNode externalKeyword;
    public final STNode semicolonToken;

    STExternalFunctionBodyNode(
            STNode equalsToken,
            STNode annotations,
            STNode externalKeyword,
            STNode semicolonToken) {
        this(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken,
                Collections.emptyList());
    }

    STExternalFunctionBodyNode(
            STNode equalsToken,
            STNode annotations,
            STNode externalKeyword,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.EXTERNAL_FUNCTION_BODY, diagnostics);
        this.equalsToken = equalsToken;
        this.annotations = annotations;
        this.externalKeyword = externalKeyword;
        this.semicolonToken = semicolonToken;

        addChildren(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STExternalFunctionBodyNode(
                this.equalsToken,
                this.annotations,
                this.externalKeyword,
                this.semicolonToken,
                diagnostics);
    }

    public STExternalFunctionBodyNode modify(
            STNode equalsToken,
            STNode annotations,
            STNode externalKeyword,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken)) {
            return this;
        }

        return new STExternalFunctionBodyNode(
                equalsToken,
                annotations,
                externalKeyword,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ExternalFunctionBodyNode(this, position, parent);
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
