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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a generic TOML document in a Ballerina package.
 *
 * @since 2.0.0
 */
public abstract class TomlDocument {
    private final Path filePath;
    private TextDocument textDocument;
    private SyntaxTree syntaxTree;
    private TomlTableNode tomlAstNode;

    protected TomlDocument(Path filePath) {
        this.filePath = filePath;
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

        try {
            textDocument = TextDocuments.from(Files.readString(filePath));
        } catch (IOException e) {
            throw new ProjectException("Failed to read file: " + filePath, e);
        }
        return textDocument;
    }

    private void parseToml() {
        TextDocument textDocument = textDocument();
        try {
            syntaxTree = SyntaxTree.from(textDocument, getFileName(filePath));
            TomlTransformer nodeTransformer = new TomlTransformer();
            tomlAstNode = (TomlTableNode) nodeTransformer.transform((DocumentNode) syntaxTree.rootNode());
        } catch (RuntimeException e) {
            // The toml parser throws runtime exceptions for some cases
            throw new ProjectException("Failed to parse file: " + getFileName(filePath), e);
        }
    }

    private String getFileName(Path filePath) {
        final Path fileNamePath = filePath.getFileName();
        if (fileNamePath != null) {
            return fileNamePath.toString();
        } else {
            // This branch may never be executed.
            throw new ProjectException("Failed to retrieve the TOML file name from the path: " + filePath);
        }
    }
}
