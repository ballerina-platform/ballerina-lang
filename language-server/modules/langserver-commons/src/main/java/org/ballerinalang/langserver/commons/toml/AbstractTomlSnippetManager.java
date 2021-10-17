/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.commons.toml;

import io.ballerina.toml.validator.schema.Schema;
import org.ballerinalang.langserver.commons.toml.visitor.TomlNode;
import org.ballerinalang.langserver.commons.toml.visitor.TomlSchemaVisitor;
import org.eclipse.lsp4j.CompletionItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Toml Snippet Manager represents the holder of completion items for a particular toml config file.
 *
 * @since 2.0.0
 */
public abstract class AbstractTomlSnippetManager {

    private Map<TomlNode, Map<String, CompletionItem>> completions = new HashMap<>();
    private boolean isSchemaVisited = false;

    /**
     * Returns a Map of Toml node and corresponding list of completion items that is
     * allowed under each top-level Toml node.
     *
     * @return {@link Map< TomlNode, Map<String,CompletionItem>>}.
     */
    public Map<TomlNode, Map<String, CompletionItem>> getCompletionProposals() {
        if (!isSchemaVisited) {
            Schema schema = Schema.from(getValidationSchema());
            TomlSchemaVisitor visitor = new TomlSchemaVisitor();
            schema.accept(visitor);
            this.completions = visitor.getAllCompletionSnippets();
            this.isSchemaVisited = true;
        }
        return Collections.unmodifiableMap(completions);
    }

    /**
     * Returns the toml validation schema string.
     *
     * @return {@link String} Schema string corresponding to the snippet manager.
     */
    public abstract String getValidationSchema();
}
