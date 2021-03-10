/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

import static io.ballerina.runtime.internal.configurable.ConfigConstants.CONFIG_FILE_NAME;

/**
 * Exception class used for TOML related configuration specific errors.
 *
 * @since 2.0.0
 */
public class ConfigTomlException extends ConfigException {

    public ConfigTomlException(String message) {
        super(message);
    }

    public ConfigTomlException(String message, TomlNode tomlNode) {
        this(getLineRange(tomlNode) + message);
    }

    public ConfigTomlException(String message, Throwable e) {
        super(message, e);
    }

    private static String getLineRange(TomlNode node) {
        if (node.location() == null) {
            return "[" + CONFIG_FILE_NAME + "] ";
        }
        LineRange lineRange = node.location().lineRange();
        LineRange oneBasedLineRange = LineRange.from(
                lineRange.filePath(),
                LinePosition.from(lineRange.startLine().line() + 1, lineRange.startLine().offset() + 1),
                LinePosition.from(lineRange.endLine().line() + 1, lineRange.endLine().offset() + 1));
        return "[" + oneBasedLineRange.filePath() + ":" + oneBasedLineRange.toString() + "] ";
    }
}
