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
import io.ballerinalang.compiler.internal.parser.BallerinaParser;
import io.ballerinalang.compiler.internal.parser.ParserFactory;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocumentChange;
import io.ballerinalang.compiler.text.TextDocuments;

/**
 * The {@code SyntaxTree} represents a parsed Ballerina source file.
 *
 * @since 2.0.0
 */
public class SyntaxTree {
    private final NonTerminalNode rootNode;
    private final String filePath;

    private TextDocument textDocument;

    SyntaxTree(NonTerminalNode rootNode, TextDocument textDocument, String filePath, boolean clone) {
        this.rootNode = modifyWithMe(rootNode, clone);
        this.textDocument = textDocument;
        this.filePath = filePath;
    }

    static SyntaxTree from(NonTerminalNode rootNode, boolean clone) {
        return new SyntaxTree(rootNode, null, null, clone);
    }

    public static SyntaxTree from(TextDocument textDocument) {
        return from(textDocument, null);
    }

    public static SyntaxTree from(TextDocument textDocument, String filePath) {
        BallerinaParser parser = ParserFactory.getParser(textDocument);
        return new SyntaxTree(parser.parse().createUnlinkedFacade(),
                textDocument, filePath, false);
    }

    public static SyntaxTree from(SyntaxTree oldTree, TextDocumentChange textDocumentChange) {
        // TODO Improve the logic behind the creation of the new document
        TextDocument newTextDocument = oldTree.textDocument().apply(textDocumentChange);
        BallerinaParser parser = ParserFactory.getParser(oldTree, newTextDocument, textDocumentChange);
        return new SyntaxTree(parser.parse().createUnlinkedFacade(),
                newTextDocument, oldTree.filePath(), false);
    }

    public TextDocument textDocument() {
        if (textDocument != null) {
            return textDocument;
        }

        textDocument = TextDocuments.from(rootNode.toSourceCode());
        return textDocument;
    }

    public boolean containsModulePart() {
        return rootNode.kind() == SyntaxKind.MODULE_PART;
    }

    @SuppressWarnings("unchecked")
    public <T extends NonTerminalNode> T rootNode() {
        return (T) rootNode;
    }

    public String filePath() {
        return this.filePath;
    }

    // Syntax tree modification methods

    public SyntaxTree modifyWith(ModulePartNode rootNode) {
        return new SyntaxTree(rootNode, null, filePath, true);
    }

    public SyntaxTree replaceNode(Node target, Node replacement) {
        ModulePartNode newRootNode = rootNode.replace(target, replacement);
        return this.modifyWith(newRootNode);
    }

    public Iterable<Diagnostic> diagnostics() {
        return rootNode.diagnostics();
    }

    public boolean hasDiagnostics() {
        return rootNode.hasDiagnostics();
    }

    @Override
    public String toString() {
        return rootNode.toString();
    }

    /**
     * Converts the syntax tree into source code and returns it as a string.
     *
     * @return source code as a string
     */
    public String toSourceCode() {
        return rootNode.toSourceCode();
    }

    private <T extends NonTerminalNode> T modifyWithMe(T node, boolean clone) {
        T clonedNode = clone ? node.internalNode().createUnlinkedFacade() : node;
        clonedNode.setSyntaxTree(this);
        return clonedNode;
    }
}
