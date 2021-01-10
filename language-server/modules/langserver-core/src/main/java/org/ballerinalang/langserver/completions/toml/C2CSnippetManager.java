/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.eclipse.lsp4j.CompletionItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Maintains all the snippets for c2c.
 *
 * @since 2.0.0
 */
public class C2CSnippetManager {

    private static volatile C2CSnippetManager snippetManager = null;
    private final Map<String, Map<String, CompletionItem>> completions;

    private C2CSnippetManager() {
        Schema c2cRootSchema = Schema.from(getValidationSchema());
        C2CSchemaVisitor visitor = new C2CSchemaVisitor();
        c2cRootSchema.accept(visitor);
        this.completions = visitor.getAllCompletionSnippets();
    }

    public Map<String, Map<String, CompletionItem>> getCompletions() {
        return completions;
    }

    public static synchronized C2CSnippetManager getInstance() {
        if (snippetManager == null) {
            snippetManager = new C2CSnippetManager();
        }
        return snippetManager;
    }

    private String getValidationSchema() {
        try {
            return IOUtils.resourceToString("c2c-schema.json", StandardCharsets.UTF_8, getClass().getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException("Schema Not found");
        }
    }
}

