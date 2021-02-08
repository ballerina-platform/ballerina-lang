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
package io.ballerina.projects;

import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlTransformer;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

/**
 * Represents a generic TOML document in a Ballerina package.
 *
 * @since 2.0.0
 */
public class TomlDocument {
    private final String fileName;
    private final String content;
    private TextDocument textDocument;
    private SyntaxTree syntaxTree;
    private TomlTableNode tomlAstNode;

    protected TomlDocument(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    public static TomlDocument from(String fileName, String content) {
        return new TomlDocument(fileName, content);
    }

    public SyntaxTree syntaxTree() {
        if (syntaxTree != null) {
            return syntaxTree;
        }

        parseToml();
        return syntaxTree;
    }

    public TomlTableNode tomlAstNode() {
        if (tomlAstNode != null) {
            return tomlAstNode;
        }

        parseToml();
        return tomlAstNode;
    }

    public TextDocument textDocument() {
        if (textDocument != null) {
            return textDocument;
        }

        textDocument = TextDocuments.from(content);
        return textDocument;
    }

    private void parseToml() {
        TextDocument textDocument = textDocument();
        try {
            syntaxTree = SyntaxTree.from(textDocument, fileName);
            TomlTransformer nodeTransformer = new TomlTransformer();
            tomlAstNode = (TomlTableNode) nodeTransformer.transform((DocumentNode) syntaxTree.rootNode());
        } catch (RuntimeException e) {
            // The toml parser throws runtime exceptions for some cases
            throw new ProjectException("Failed to parse file: " + fileName, e);
        }
    }
}
