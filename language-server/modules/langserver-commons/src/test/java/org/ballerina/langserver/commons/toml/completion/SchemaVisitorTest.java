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
package org.ballerina.langserver.commons.toml.completion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.ballerina.toml.validator.schema.Schema;
import org.ballerina.langserver.commons.toml.util.FileUtils;
import org.ballerinalang.langserver.commons.toml.visitor.TomlNode;
import org.ballerinalang.langserver.commons.toml.visitor.TomlSchemaVisitor;
import org.eclipse.lsp4j.CompletionItem;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Schema Visitor Test.
 *
 * @since 2.0.0
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
