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
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LineRange;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.internal.configurable.providers.toml.TomlConstants.CONFIG_DATA_ENV_VARIABLE;
import static io.ballerina.runtime.internal.configurable.providers.toml.Utils.getOneBasedLineRange;
import static io.ballerina.runtime.internal.errors.ErrorCodes.CONFIG_TOML_READ_FAILED;

/**
 * Represents configuration TOML document for `configurable` variables.
 *
 * @since 2.0.0
 */
public class ConfigToml {
    private Path filePath;
    private String content;
    private TomlTableNode tomlAstNode;

    protected ConfigToml(Path filePath) {
        this.filePath = filePath;
    }

    protected ConfigToml(String content) {
        this.content = content;
    }

    public TomlTableNode tomlAstNode() {
        if (tomlAstNode != null) {
            return tomlAstNode;
        }
        if (content != null) {
            tomlAstNode = Toml.read(content, CONFIG_DATA_ENV_VARIABLE).rootNode();
        } else {
            tomlAstNode = getTomlRootFromFile();
        }

        List<Diagnostic> diagnosticList = getDiagnostics();
        if (!diagnosticList.isEmpty()) {
            throw new ConfigException(ErrorCodes.CONFIG_TOML_INVALID_FILE, getErrorMessage(diagnosticList));
        }
        return tomlAstNode;
    }

    private TomlTableNode getTomlRootFromFile() {
        try {
             return Toml.read(filePath).rootNode();
        } catch (IOException e) {
            throw new ConfigException(CONFIG_TOML_READ_FAILED, filePath, e);
        }
    }

    private String getErrorMessage(List<Diagnostic> diagnosticList) {
        StringBuilder errorMessage = new StringBuilder("\n");
        for (Diagnostic diagnostic : diagnosticList) {
            LineRange lineRange = getOneBasedLineRange(diagnostic.location().lineRange());
            errorMessage.append("[").append(lineRange.fileName()).append(":")
                    .append(lineRange).append("] ").append(diagnostic.message()).append("\n");
        }
        return errorMessage.toString();
    }

    private List<Diagnostic> getDiagnostics() {
        return new ArrayList<>(tomlAstNode.diagnostics());
    }
}
