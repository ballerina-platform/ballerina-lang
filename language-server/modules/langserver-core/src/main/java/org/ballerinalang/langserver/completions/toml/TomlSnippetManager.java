/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.toml;

import io.ballerina.toml.validator.schema.Schema;
import org.apache.commons.io.IOUtils;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains all the supported snippets for Toml.
 *
 * @since 2.0.0
 */
public class TomlSnippetManager {
    private static final LanguageServerContext.Key<TomlSnippetManager> TOML_SNIPPET_MANAGER_KEY =
            new LanguageServerContext.Key<>();
    private final Map<Parent, Map<String, CompletionItem>> completions;

    private TomlSnippetManager(LanguageServerContext context) {
        Schema c2cRootSchema = Schema.from(getValidationSchema());
        TomlSchemaVisitor visitor = new TomlSchemaVisitor();
        c2cRootSchema.accept(visitor);
        this.completions = optimizeCompletionsWithOnlyTables(visitor.getAllCompletionSnippets());
        context.put(TOML_SNIPPET_MANAGER_KEY, this);
    }

    public Map<Parent, Map<String, CompletionItem>> getCompletions() {
        return Collections.unmodifiableMap(completions);
    }

    public static TomlSnippetManager getInstance(LanguageServerContext context) {
        TomlSnippetManager snippetManager = context.get(TOML_SNIPPET_MANAGER_KEY);
        if (snippetManager == null) {
            snippetManager = new TomlSnippetManager(context);
        }

        return snippetManager;
    }

    private Map<Parent, Map<String, CompletionItem>> optimizeCompletionsWithOnlyTables(Map<Parent, Map<String,
            CompletionItem>> completions) {
        Map<Parent, Map<String, CompletionItem>> optimizedCompletions = new HashMap<>();
        for (Map.Entry<Parent, Map<String, CompletionItem>> entry : completions.entrySet()) {
            Parent key = entry.getKey();
            Map<String, CompletionItem> value = entry.getValue();
            if (!isContainsOnlyTable(value)) {
                optimizedCompletions.put(key, value);
            }
        }
        return optimizedCompletions;
    }

    private boolean isContainsOnlyTable(Map<String, CompletionItem> value) {
        boolean isRedundant = true;
        for (Map.Entry<String, CompletionItem> childEntry : value.entrySet()) {
            CompletionItem completionItem = childEntry.getValue();
            if (!(completionItem.getDetail().equals(TomlSyntaxTreeUtil.TABLE) ||
                    completionItem.getDetail().equals(TomlSyntaxTreeUtil.TABLE_ARRAY))) {
                isRedundant = false;
            }
        }
        return isRedundant;
    }

    private String getValidationSchema() {
        try {
            return IOUtils.resourceToString("c2c-schema.json", StandardCharsets.UTF_8, getClass().getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException("Schema Not found");
        }
    }
}

