/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.configurable.providers.toml;

import io.ballerina.runtime.internal.configurable.exceptions.ConfigException;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlTransformer;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getOneBasedLineRange;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TOML_PARSE_FAILED;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.CONFIG_TOML_READ_FAILED;

/**
 * Represents configuration TOML document for `configurable` variables.
 *
 * @since 2.0.0
 */
public class ConfigToml {
    private final Path filePath;
    private TextDocument textDocument;
    private TomlTableNode tomlAstNode;
    private SyntaxTree syntaxTree;

    protected ConfigToml(Path filePath) {
        this.filePath = filePath;
    }

    public TomlTableNode tomlAstNode() {
        if (tomlAstNode != null) {
            return tomlAstNode;
        }

        parseToml();
        List<Diagnostic> diagnosticList = getDiagnostics();
        if (!diagnosticList.isEmpty()) {
            throw new ConfigException(RuntimeErrors.CONFIG_TOML_INVALID_FILE, getErrorMessage(diagnosticList));
        }
        return tomlAstNode;
    }

    private String getErrorMessage(List<Diagnostic> diagnosticList) {
        StringBuilder errorMessage = new StringBuilder("\n");
        for (Diagnostic diagnostic : diagnosticList) {
            LineRange lineRange = getOneBasedLineRange(diagnostic.location().lineRange());
            errorMessage.append("[").append(lineRange.filePath()).append(":")
                    .append(lineRange).append("] ").append(diagnostic.message()).append("\n");
        }
        return errorMessage.toString();
    }

    private TextDocument textDocument() {
        if (textDocument != null) {
            return textDocument;
        }

        try {
            textDocument = TextDocuments.from(Files.readString(filePath));
        } catch (IOException e) {
            throw new ConfigException(CONFIG_TOML_READ_FAILED, filePath, e);
        }
        return textDocument;
    }

    private void parseToml() {
        TextDocument tomlDocument = textDocument();
        try {
            syntaxTree = SyntaxTree.from(tomlDocument, getFileName(filePath));
            TomlTransformer nodeTransformer = new TomlTransformer();
            tomlAstNode = (TomlTableNode) nodeTransformer.transform((DocumentNode) syntaxTree.rootNode());
        } catch (RuntimeException e) {
            // The toml parser throws runtime exceptions for some cases
            throw new ConfigException(CONFIG_TOML_PARSE_FAILED, filePath, e);
        }
    }

    private String getFileName(Path filePath) {
        final Path fileNamePath = filePath.getFileName();
        if (fileNamePath != null) {
            return fileNamePath.toString();
        }
        return null;
    }

    private List<Diagnostic> getDiagnostics() {
        return new ArrayList<>(tomlAstNode.diagnostics());
    }
}
