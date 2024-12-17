/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.inlayhint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.ballerinalang.langserver.AbstractLSTest;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Test class for inlay hints.
 *
 * @since 2201.6.0
 */
public class InlayHintTest extends AbstractLSTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLSTest.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Path sourcesPath = new File(getClass().getClassLoader()
            .getResource("inlayhint").getFile()).toPath();

    private final Path testRoot = FileUtils.RES_DIR.resolve("inlayhint");

    @Test(dataProvider = "data-provider")
    public void test(String config, String source) throws IOException {
        Path configPath = getConfigJsonPath(config);
        TestConfig testConfig = gson.fromJson(Files.newBufferedReader(configPath), TestConfig.class);
        Path sourcePath = sourcesPath.resolve(testConfig.source);
        TestUtil.openDocument(getServiceEndpoint(), sourcePath);

        InlayHintParams inlayHintParams = new InlayHintParams();
        TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier(testRoot.toUri().toString());
        inlayHintParams.setTextDocument(textDocumentIdentifier);
        Range range = testConfig.range;

        String response = getResponse(sourcePath.toString(), range, sourcePath.toString());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        JsonArray resultList = json.getAsJsonArray("result");
        List<InlayHint> responseItemList = gson.fromJson(resultList, new TypeToken<>() { });

        if (responseItemList.size() != testConfig.getResult().size()) {
//            updateConfig(configPath, testConfig, responseItemList);
            List<InlayHint> mismatchedList1 = responseItemList.stream()
                    .filter(item -> !testConfig.getResult().contains(item)).toList();
            List<InlayHint> mismatchedList2 = testConfig.getResult().stream()
                    .filter(item -> !responseItemList.contains(item)).toList();
            LOG.info("Inlay-hint results which are in response but not in test config : " + mismatchedList1);
            LOG.info("Inlay-hint results which are in test config but not in response : " + mismatchedList2);
            Assert.fail(String.format("Failed test: '%s' (%s)", testConfig.getDescription(), configPath));
        }
    }

    public String getResponse(String filePath, Range range, String sourcePath) {
        Endpoint serviceEndpoint = getServiceEndpoint();
        String inlayHintsResponse = TestUtil.getInlayHintsResponse(serviceEndpoint, filePath, range);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        return inlayHintsResponse;
    }

    protected Path getConfigJsonPath(String configFilePath) {
        return FileUtils.RES_DIR.resolve("inlayhint")
                .resolve("config")
                .resolve(configFilePath);
    }

    private void updateConfig(Path configJsonPath, TestConfig testConfig, List<InlayHint> responseItemList)
            throws IOException {
        TestConfig updatedConfig = new TestConfig();
        updatedConfig.setRange(testConfig.getRange());
        updatedConfig.setSource(testConfig.getSource());
        updatedConfig.setDescription(testConfig.getDescription());

        List<InlayHint> results = new ArrayList<>();
        List<InlayHint> copyOfResultList = new ArrayList<>(responseItemList);
        List<InlayHint> expectedList = testConfig.getResult();

        for (InlayHint inlayHint : expectedList) {
            int i = 0;
            for (; i < copyOfResultList.size(); i++) {
                if (inlayHint.getLabel().equals(copyOfResultList.get(i).getLabel())) {
                    break;
                }
            }

            if (i != copyOfResultList.size()) {
                results.add(copyOfResultList.get(i));
                copyOfResultList.remove(i);
            }
        }
        results.addAll(copyOfResultList);

        updatedConfig.setResult(results);

        String objStr = gson.toJson(updatedConfig).concat(System.lineSeparator());
        Files.write(configJsonPath, objStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Represents a test config.
     */
    protected static class TestConfig {
        Range range;
        String source;
        List<InlayHint> result;
        String description;

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public List<InlayHint> getResult() {
            return result;
        }

        public void setResult(List<InlayHint> result) {
            this.result = result;
        }

        public String getDescription() {
            return description == null ? "" : description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @DataProvider(name = "data-provider")
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    public Object[][] testSubset() {
        return new Object[0][];
    }

    public String getTestResourceDir() {
        return "inlayhint";
    }

    public List<String> skipList() {
        return new ArrayList<>();
    }

    protected Object[][] getConfigsList() {
        if (this.testSubset().length != 0) {
            return this.testSubset();
        }
        List<String> skippedTests = this.skipList();
        try (Stream<Path> configPaths = Files.walk(this.testRoot.resolve("config"))) {
            return configPaths.filter(path -> {
                        File file = path.toFile();
                        return file.isFile() && file.getName().endsWith(".json")
                                && !skippedTests.contains(file.getName());
                    })
                    .map(path -> new Object[]{path.toFile().getName(), this.getTestResourceDir()})
                    .toArray(size -> new Object[size][2]);
        } catch (IOException e) {
            // If failed to load tests, then it's a failure
            Assert.fail("Unable to load test config", e);
            return new Object[0][];
        }
    }
}
