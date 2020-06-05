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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.diagnostics.Diagnostic;
import io.ballerinalang.compiler.internal.diagnostics.SyntaxDiagnostic;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerinalang.compiler.text.LineRange;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextRange;

/**
 * This class represents a node in the syntax tree.
 *
 * @since 1.3.0
 */
public abstract class Node {
    protected final STNode internalNode;
    protected final int position;
    protected final NonTerminalNode parent;

    /**
     * A reference to the syntaxTree to which this node belongs.
     */
    private SyntaxTree syntaxTree;
    private LineRange lineRange;

    // TextRange - starting startOffset and width
    private TextRange textRange;
    private TextRange textRangeWithMinutiae;

    public Node(STNode internalNode, int position, NonTerminalNode parent) {
        this.internalNode = internalNode;
        this.position = position;
        this.parent = parent;
    }

    public int position() {
        return position;
    }

    public NonTerminalNode parent() {
        return parent;
    }

    public TextRange textRange() {
        if (textRange != null) {
            return textRange;
        }
        int leadingMinutiaeDelta = internalNode.widthWithLeadingMinutiae() - internalNode.width();
        int positionWithoutLeadingMinutiae = this.position + leadingMinutiaeDelta;
        textRange = TextRange.from(positionWithoutLeadingMinutiae, internalNode.width());
        return textRange;
    }

    public TextRange textRangeWithMinutiae() {
        if (textRangeWithMinutiae != null) {
            return textRangeWithMinutiae;
        }
        textRangeWithMinutiae = TextRange.from(position, internalNode.widthWithMinutiae());
        return textRangeWithMinutiae;
    }

    public SyntaxKind kind() {
        return internalNode.kind;
    }

    public NodeLocation location() {
        return new NodeLocation(this);
    }

    public abstract Iterable<Diagnostic> diagnostics();

    public boolean hasDiagnostics() {
        return internalNode.hasDiagnostics();
    }

    public boolean isMissing() {
        return internalNode.isMissing();
    }

    public SyntaxTree syntaxTree() {
        return populateSyntaxTree();
    }

    public LineRange lineRange() {
        if (lineRange != null) {
            return lineRange;
        }

        SyntaxTree syntaxTree = syntaxTree();
        TextDocument textDocument = syntaxTree.textDocument();
        lineRange = LineRange.from(syntaxTree.filePath(), textDocument.linePositionFrom(textRange().startOffset()),
                textDocument.linePositionFrom(textRange().endOffset()));
        return lineRange;
    }

    public abstract MinutiaeList leadingMinutiae();

    public abstract MinutiaeList trailingMinutiae();

    /**
     * Accepts an instance of the {@code NodeVisitor}, which can be used to
     * traverse the syntax tree.
     *
     * @param visitor an instance of the {@code NodeVisitor}
     */
    public abstract void accept(NodeVisitor visitor);

    /**
     * Applies the given {@code NodeTransformer} to this node and returns
     * the transformed object.
     *
     * @param transformer an instance of the {@code NodeTransformer}
     * @param <T>         the type of transformed object
     * @return the transformed object
     */
    public abstract <T> T apply(NodeTransformer<T> transformer);

    public STNode internalNode() {
        return internalNode;
    }

    @Override
    public String toString() {
        return internalNode.toString();
    }

    private SyntaxTree populateSyntaxTree() {
        if (syntaxTree != null) {
            return syntaxTree;
        }

        Node parent = this.parent;
        if (parent == null) {
            throw new IllegalStateException("SyntaxTree instance cannot be null");
        }
        setSyntaxTree(parent.populateSyntaxTree());
        return syntaxTree;
    }

    void setSyntaxTree(SyntaxTree syntaxTree) {
        this.syntaxTree = syntaxTree;
    }

    protected Diagnostic createSyntaxDiagnostic(STNodeDiagnostic nodeDiagnostic) {
        return SyntaxDiagnostic.from(nodeDiagnostic, this.location());
    }
}
