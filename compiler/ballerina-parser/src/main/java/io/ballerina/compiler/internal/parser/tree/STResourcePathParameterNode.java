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
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STResourcePathParameterNode extends STNode {
    public final STNode openBracketToken;
    public final STNode annotations;
    public final STNode typeDescriptor;
    public final STNode ellipsisToken;
    public final STNode paramName;
    public final STNode closeBracketToken;

    STResourcePathParameterNode(
            SyntaxKind kind,
            STNode openBracketToken,
            STNode annotations,
            STNode typeDescriptor,
            STNode ellipsisToken,
            STNode paramName,
            STNode closeBracketToken) {
        this(
                kind,
                openBracketToken,
                annotations,
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken,
                Collections.emptyList());
    }

    STResourcePathParameterNode(
            SyntaxKind kind,
            STNode openBracketToken,
            STNode annotations,
            STNode typeDescriptor,
            STNode ellipsisToken,
            STNode paramName,
            STNode closeBracketToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
        this.openBracketToken = openBracketToken;
        this.annotations = annotations;
        this.typeDescriptor = typeDescriptor;
        this.ellipsisToken = ellipsisToken;
        this.paramName = paramName;
        this.closeBracketToken = closeBracketToken;

        addChildren(
                openBracketToken,
                annotations,
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STResourcePathParameterNode(
                this.kind,
                this.openBracketToken,
                this.annotations,
                this.typeDescriptor,
                this.ellipsisToken,
                this.paramName,
                this.closeBracketToken,
                diagnostics);
    }

    public STResourcePathParameterNode modify(
            SyntaxKind kind,
            STNode openBracketToken,
            STNode annotations,
            STNode typeDescriptor,
            STNode ellipsisToken,
            STNode paramName,
            STNode closeBracketToken) {
        if (checkForReferenceEquality(
                openBracketToken,
                annotations,
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken)) {
            return this;
        }

        return new STResourcePathParameterNode(
                kind,
                openBracketToken,
                annotations,
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ResourcePathParameterNode(this, position, parent);
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
