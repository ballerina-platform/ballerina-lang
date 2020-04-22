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

import io.ballerinalang.compiler.internal.parser.BallerinaParser;
import io.ballerinalang.compiler.internal.parser.ParserFactory;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocumentChange;

/**
 * The {@code SyntaxTree} represents a parsed Ballerina source file.
 *
 * @since 2.0.0
 */
public class SyntaxTree {
    private final TextDocument textDocument;
    private final ModulePartNode modulePart;

    SyntaxTree(ModulePartNode modulePart, TextDocument textDocument) {
        this.modulePart = cloneWithMe(modulePart);
        this.textDocument = textDocument;
    }

    public static SyntaxTree from(TextDocument textDocument) {
        BallerinaParser parser = ParserFactory.getParser(textDocument);
        return new SyntaxTree(parser.parse().createUnlinkedFacade(), textDocument);
    }

    public static SyntaxTree from(SyntaxTree oldTree, TextDocumentChange textDocumentChange) {
        // TODO Improve the logic behind the creation of the new document
        TextDocument newTextDocument = oldTree.textDocument().apply(textDocumentChange);
        BallerinaParser parser = ParserFactory.getParser(oldTree, newTextDocument, textDocumentChange);
        return new SyntaxTree(parser.parse().createUnlinkedFacade(), newTextDocument);
    }

    public TextDocument textDocument() {
        return textDocument;
    }

    public ModulePartNode modulePart() {
        return modulePart;
    }

    @Override
    public String toString() {
        return modulePart.toString();
    }

    private <T extends Node> T cloneWithMe(T node) {
        T clonedNode = node.internalNode().createUnlinkedFacade();
        clonedNode.setSyntaxTree(this);
        return clonedNode;
    }
}
