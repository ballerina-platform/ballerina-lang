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

import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
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
public class STMarkdownParameterDocumentationLineNode extends STDocumentationNode {
    public final STNode hashToken;
    public final STNode plusToken;
    public final STNode parameterName;
    public final STNode minusToken;
    public final STNode documentElements;

    STMarkdownParameterDocumentationLineNode(
            SyntaxKind kind,
            STNode hashToken,
            STNode plusToken,
            STNode parameterName,
            STNode minusToken,
            STNode documentElements) {
        this(
                kind,
                hashToken,
                plusToken,
                parameterName,
                minusToken,
                documentElements,
                Collections.emptyList());
    }

    STMarkdownParameterDocumentationLineNode(
            SyntaxKind kind,
            STNode hashToken,
            STNode plusToken,
            STNode parameterName,
            STNode minusToken,
            STNode documentElements,
            Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
        this.hashToken = hashToken;
        this.plusToken = plusToken;
        this.parameterName = parameterName;
        this.minusToken = minusToken;
        this.documentElements = documentElements;

        addChildren(
                hashToken,
                plusToken,
                parameterName,
                minusToken,
                documentElements);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMarkdownParameterDocumentationLineNode(
                this.kind,
                this.hashToken,
                this.plusToken,
                this.parameterName,
                this.minusToken,
                this.documentElements,
                diagnostics);
    }

    public STMarkdownParameterDocumentationLineNode modify(
            SyntaxKind kind,
            STNode hashToken,
            STNode plusToken,
            STNode parameterName,
            STNode minusToken,
            STNode documentElements) {
        if (checkForReferenceEquality(
                hashToken,
                plusToken,
                parameterName,
                minusToken,
                documentElements)) {
            return this;
        }

        return new STMarkdownParameterDocumentationLineNode(
                kind,
                hashToken,
                plusToken,
                parameterName,
                minusToken,
                documentElements,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MarkdownParameterDocumentationLineNode(this, position, parent);
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
