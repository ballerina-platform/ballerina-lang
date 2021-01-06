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

    public Map<String, Map<String, CompletionItem>> getAllSnippetsFromSchema() {
        Schema c2cRootSchema = Schema.from(getValidationSchema());
        C2CSchemaVisitor visitor = new C2CSchemaVisitor();
        c2cRootSchema.accept(visitor);
        return visitor.getAllCompletionSnippets();
    }

    private String getValidationSchema() {
        try {
            return IOUtils.resourceToString("c2c-schema.json", StandardCharsets.UTF_8, getClass().getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException("Schema Not found");
        }
    }
}

