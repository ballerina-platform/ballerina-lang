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

import io.ballerina.runtime.api.Module;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Toml parser that reads from text content for configurable implementation.
 *
 * @since 2.0.0
 */
public class TomlContentProvider extends TomlProvider {

    private final String configContent;
    private static final Pattern UNESCAPED_NEWLINE_CHAR = Pattern.compile("\\\\n(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    private static final Pattern UNESCAPED_CARRIAGE_CHAR =
            Pattern.compile("(\\\\r|\\\\t)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    public TomlContentProvider(Module rootModule, String configContent, Set<Module> moduleSet) {
        super(rootModule, moduleSet);
        this.configContent = cleanContent(configContent);
    }

    private String cleanContent(String configContent) {
        // Finds the `\n` characters that is not inside values to replace with the system line separator
        String content =  UNESCAPED_NEWLINE_CHAR.matcher(configContent).replaceAll(System.lineSeparator());
        // Finds the `\r` and `\t` characters that is not inside values to remove them
        return UNESCAPED_CARRIAGE_CHAR.matcher(content).replaceAll("");
    }

    @Override
    public void initialize() {
        if (configContent.isEmpty()) {
            return;
        }
        super.tomlNode = new ConfigToml(configContent).tomlAstNode();
        super.initialize();
    }

}
