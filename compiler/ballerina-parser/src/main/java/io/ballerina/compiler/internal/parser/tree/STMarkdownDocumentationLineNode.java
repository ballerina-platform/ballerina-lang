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

import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
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
public class STMarkdownDocumentationLineNode extends STDocumentationNode {
    public final STNode hashToken;
    public final STNode documentElements;

    STMarkdownDocumentationLineNode(
            SyntaxKind kind,
            STNode hashToken,
            STNode documentElements) {
        this(
                kind,
                hashToken,
                documentElements,
                Collections.emptyList());
    }

    STMarkdownDocumentationLineNode(
            SyntaxKind kind,
            STNode hashToken,
            STNode documentElements,
            Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
        this.hashToken = hashToken;
        this.documentElements = documentElements;

        addChildren(
                hashToken,
                documentElements);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMarkdownDocumentationLineNode(
                this.kind,
                this.hashToken,
                this.documentElements,
                diagnostics);
    }

    public STMarkdownDocumentationLineNode modify(
            SyntaxKind kind,
            STNode hashToken,
            STNode documentElements) {
        if (checkForReferenceEquality(
                hashToken,
                documentElements)) {
            return this;
        }

        return new STMarkdownDocumentationLineNode(
                kind,
                hashToken,
                documentElements,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MarkdownDocumentationLineNode(this, position, parent);
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
