/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.langserver.apispec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Contains tests for the LS API Specification Generator.
 *
 * @since 2201.12.0
 */
public class ApiSpecificationGeneratorTest {

    private static final String RESOURCE_DIR = "src/test/resources/apispec";
    private Gson gson;
    private Map<String, JsonRpcMethod> jsonRpcMethodMap;

    @BeforeClass
    public void init() {
        BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        this.jsonRpcMethodMap = languageServer.supportedMethods();
    }

    @Test(dataProvider = "ConfigDataProvider")
    public void test(Path config) throws IOException {
        JsonObject expectedMethod = gson.fromJson(Files.newBufferedReader(config), JsonObject.class);
        JsonRpcMethod method = jsonRpcMethodMap.get(expectedMethod.getAsJsonPrimitive("method").getAsString());
        JsonObject actualMethod = ApiSpecGenerator.generate(method);

        if (!actualMethod.equals(expectedMethod)) {
            updateConfig(config, actualMethod);
            Assert.fail("Failed test: " + config);
        }
    }

    @DataProvider(name = "ConfigDataProvider")
    public Object[] configDataProvider() {
        try (Stream<Path> stream = Files.walk(Path.of(RESOURCE_DIR))) {
            return stream
                    .filter(path -> {
                        File file = path.toFile();
                        return file.isFile() && !file.getName().startsWith(".")
                                && file.getName().endsWith(".json");
                    })
                    .toArray(Path[]::new);
        } catch (IOException e) {
            // If failed to load tests, then it's a failure
            Assert.fail("Unable to load test config", e);
            return new Object[0][];
        }
    }

    private void updateConfig(Path configJsonPath, Object updatedConfig) throws IOException {
        String objStr = gson.toJson(updatedConfig).concat(System.lineSeparator());
        Files.writeString(configJsonPath, objStr);
    }
}
