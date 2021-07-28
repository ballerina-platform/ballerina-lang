package org.ballerinalang.langserver.toml.completion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.ballerina.toml.validator.schema.Schema;
import org.ballerinalang.langserver.toml.common.completion.visitor.TomlNode;
import org.ballerinalang.langserver.toml.common.completion.visitor.TomlSchemaVisitor;
import org.ballerinalang.langserver.util.FileUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Schema Visitor Test.
 */
public class SchemaVisitorTest {

    @Test(dataProvider = "testDataProvider")
    public void test(String configFileName) throws IOException {
        String configPath = FileUtils.RES_DIR.resolve("toml").resolve("completion")
                .resolve("schema_visitor").resolve("config").resolve(configFileName).toString();
        JsonObject configJson =
                FileUtils.fileContentAsObject(configPath);
        String schemaFile = configJson.getAsJsonPrimitive("source").getAsString();
        JsonObject expected = configJson.getAsJsonObject("expected");
        String schemaString = FileUtils.fileContent(FileUtils.RES_DIR.resolve("toml").resolve("completion")
                .resolve("schema_visitor").resolve("schema").resolve(schemaFile).toString());

        Gson gson = new Gson();
        Type collectionType = new TypeToken<Map<String, Map<String, CompletionItem>>>() {
        }.getType();
        Map<String, Map<String, CompletionItem>> expectedMap = gson.fromJson(expected, collectionType);

        Schema validationSchema = Schema.from(schemaString);
        TomlSchemaVisitor visitor = new TomlSchemaVisitor();
        validationSchema.accept(visitor);
        Map<TomlNode, Map<String, CompletionItem>> completions = visitor.getAllCompletionSnippets();
        boolean isValid = validateResponse(completions, expectedMap);
        if (!isValid) {
            //updateConfig(completions, configJson, configPath);
            Assert.fail("Failed Test for: " + configPath);
        }
    }

    @DataProvider
    private Object[] testDataProvider() {
        return new Object[]{"config1.json", "config2.json"};
    }

    private boolean validateResponse(Map<TomlNode, Map<String, CompletionItem>> response,
                                     Map<String, Map<String, CompletionItem>> expected) {
        if (response.keySet().size() != expected.keySet().size()) {
            return false;
        }
        for (Map.Entry<TomlNode, Map<String, CompletionItem>> entry : response.entrySet()) {
            String key = entry.getKey().getKey();
            if (expected.keySet().contains(entry.getKey().getKey())) {
                Map<String, CompletionItem> value1 = entry.getValue();
                Map<String, CompletionItem> value2 = expected.get(key);
                if (!value1.equals(value2)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private void updateConfig(Map<TomlNode, Map<String, CompletionItem>> response,
                              JsonObject configJsonObject, String configJsonPath) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(response);
        JsonObject obj = new JsonObject();
        obj.add("source", configJsonObject.get("source"));
        obj.add("expected", JsonParser.parseString(json));
        String objStr = obj.toString().concat(System.lineSeparator());
        java.nio.file.Files.write(FileUtils.RES_DIR.resolve(configJsonPath),
                objStr.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
