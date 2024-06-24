/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.MarkdownCodeBlockNode;
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
public class STMarkdownCodeBlockNode extends STDocumentationNode {
    public final STNode startLineHashToken;
    public final STNode startBacktick;
    public final STNode langAttribute;
    public final STNode codeLines;
    public final STNode endLineHashToken;
    public final STNode endBacktick;

    STMarkdownCodeBlockNode(
            STNode startLineHashToken,
            STNode startBacktick,
            STNode langAttribute,
            STNode codeLines,
            STNode endLineHashToken,
            STNode endBacktick) {
        this(
                startLineHashToken,
                startBacktick,
                langAttribute,
                codeLines,
                endLineHashToken,
                endBacktick,
                Collections.emptyList());
    }

    STMarkdownCodeBlockNode(
            STNode startLineHashToken,
            STNode startBacktick,
            STNode langAttribute,
            STNode codeLines,
            STNode endLineHashToken,
            STNode endBacktick,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.MARKDOWN_CODE_BLOCK, diagnostics);
        this.startLineHashToken = startLineHashToken;
        this.startBacktick = startBacktick;
        this.langAttribute = langAttribute;
        this.codeLines = codeLines;
        this.endLineHashToken = endLineHashToken;
        this.endBacktick = endBacktick;

        addChildren(
                startLineHashToken,
                startBacktick,
                langAttribute,
                codeLines,
                endLineHashToken,
                endBacktick);
    }

    @Override
    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMarkdownCodeBlockNode(
                this.startLineHashToken,
                this.startBacktick,
                this.langAttribute,
                this.codeLines,
                this.endLineHashToken,
                this.endBacktick,
                diagnostics);
    }

    public STMarkdownCodeBlockNode modify(
            STNode startLineHashToken,
            STNode startBacktick,
            STNode langAttribute,
            STNode codeLines,
            STNode endLineHashToken,
            STNode endBacktick) {
        if (checkForReferenceEquality(
                startLineHashToken,
                startBacktick,
                langAttribute,
                codeLines,
                endLineHashToken,
                endBacktick)) {
            return this;
        }

        return new STMarkdownCodeBlockNode(
                startLineHashToken,
                startBacktick,
                langAttribute,
                codeLines,
                endLineHashToken,
                endBacktick,
                diagnostics);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new MarkdownCodeBlockNode(this, position, parent);
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
