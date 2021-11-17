/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.datamapper.codeaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.datamapper.util.FileUtils;
import org.ballerinalang.datamapper.util.TestUtil;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static org.ballerinalang.datamapper.util.DataMapperTestUtils.getCodeActionResponse;


/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class CodeActionTest {

    private static Endpoint serviceEndpoint;
    private static final Logger log = LoggerFactory.getLogger(CodeActionTest.class);
    private final Path sourcesPath = new File(getClass().getClassLoader().getResource("codeaction").getFile()).toPath();

    @BeforeClass
    private void init() {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        String startConfigPath = "codeaction" + File.separator + "config" + File.separator +
                "startConfig.json";
        JsonObject configs = FileUtils.fileContentAsObject(startConfigPath);
        TestUtil.setWorkspaceConfig(serviceEndpoint, configs);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-1")
    public void testDataMapperCodeAction_1(String config, String source) throws Exception {
        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-10")
    public void testDataMapperCodeAction_10(String config, String source) throws Exception {
        // Read expected results
        String configJsonPath = "codeaction" + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        // Get code action from language server
        JsonObject responseJson = getCodeActionResponse(source, configJsonObject, serviceEndpoint);

        if (responseJson.getAsJsonArray("result").size() == 0) {
            Assert.assertFalse(false);
        } else {
            JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
            String title = expectedResponse.get("title").getAsString();

            boolean codeActionFound = false;
            for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
                JsonElement right = jsonElement.getAsJsonObject().get("right");
                if (right.getAsJsonObject().get("title").getAsString().equals(title)) {
                    codeActionFound = true;
                }
            }
            Assert.assertFalse(codeActionFound, "Found a data mapper code action");
        }
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-1")
    public Object[][] codeActionDataMapperDataProvider_1() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper1.json", "dataMapper1.bal"},
                {"dataMapper2.json", "dataMapper2.bal"},
                {"dataMapper3.json", "dataMapper3.bal"},
                {"dataMapper4.json", "dataMapper4.bal"},
                {"dataMapper6.json", "dataMapper6.bal"},
                {"dataMapper7.json", "dataMapper7.bal"},
                {"dataMapper8.json", "dataMapper8.bal"},
                {"dataMapper9.json", "dataMapper9.bal"},
                {"dataMapper10.json", "dataMapper10.bal"},
                {"dataMapper11.json", "dataMapper11.bal"},
                {"dataMapper12.json", "dataMapper12.bal"},
                {"dataMapper13.json", "dataMapper13.bal"},
                {"dataMapper14.json", "dataMapper14.bal"},
                {"dataMapper15.json", "dataMapper15.bal"},
                {"dataMapper17.json", "dataMapper17.bal"},
                {"module-response/defaultDataMapper1.json", "datamapper-module-test/defaultDataMapper1.bal"},
                {"module-response/defaultDataMapper2.json", "datamapper-module-test/defaultDataMapper2.bal"},
                {"module-response/defaultDataMapper3.json", "datamapper-module-test/defaultDataMapper3.bal"},
                {"module-response/defaultDataMapper4.json", "datamapper-module-test/defaultDataMapper4.bal"},
                {"module-response/moduleDataMapper1.json",
                        "datamapper-module-test/modules/module1/moduleDataMapper1.bal"},
                {"module-response/moduleDataMapper2.json",
                        "datamapper-module-test/modules/module2/moduleDataMapper2.bal"},
                {"module-response/moduleDataMapper3.json",
                        "datamapper-module-test/modules/module3/moduleDataMapper3.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-10")
    public Object[] codeActionDataMapperDataProvider_10() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper18.json", "dataMapper18.bal"},
        };
    }

    @AfterClass
    private void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(serviceEndpoint);
    }

    public void checkAssertion(String config, String source) throws Exception {
        // Read expected results
        String configJsonPath = "codeaction" + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        Path sourcePath = sourcesPath.resolve("source").resolve(source);

        // Get code action from language server
        JsonObject responseJson = getCodeActionResponse(source, configJsonObject, serviceEndpoint);

        JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
        String title = expectedResponse.get("title").getAsString();

        boolean codeActionFound = false;
        for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
            String expTitle = "Generate mapping function";
            JsonObject right = jsonElement.getAsJsonObject().get("right").getAsJsonObject();
            if (right == null) {
                continue;
            }

            // Match title
            String actualTitle = right.get("title").getAsString();
            if (!expTitle.equals(actualTitle)) {
                continue;
            }
            // Match edits
            if (expectedResponse.get("edits") != null) {
                JsonArray actualEdit = right.get("edit").getAsJsonObject().get("documentChanges")
                        .getAsJsonArray().get(0).getAsJsonObject().get("edits").getAsJsonArray();
                JsonArray expEdit = expectedResponse.get("edits").getAsJsonArray();
                if (!expEdit.equals(actualEdit)) {
                    continue;
                }
            }
            // Match args
            if (expectedResponse.get("command") != null) {
                JsonObject expectedCommand = expectedResponse.get("command").getAsJsonObject();
                JsonObject actualCommand = right.get("command").getAsJsonObject();

                if (!Objects.equals(actualCommand.get("command"), expectedCommand.get("command"))) {
                    continue;
                }

                if (!Objects.equals(actualCommand.get("title"), expectedCommand.get("title"))) {
                    continue;
                }

                JsonArray actualArgs = actualCommand.getAsJsonArray("arguments");
                JsonArray expArgs = expectedCommand.getAsJsonArray("arguments");
                if (!org.ballerinalang.langserver.util.TestUtil.isArgumentsSubArray(actualArgs, expArgs)) {
                    continue;
                }

                boolean docUriFound = false;
                for (JsonElement actualArg : actualArgs) {
                    JsonObject arg = actualArg.getAsJsonObject();
                    if ("doc.uri".equals(arg.get("key").getAsString())) {
                        Optional<Path> docPath = CommonUtil.getPathFromURI(arg.get("value").getAsString());
                        if (docPath.isPresent()) {
                            // We just check file names, since one refers to file in build/ while
                            // the other refers to the file in test resources
                            docUriFound = docPath.get().getFileName().equals(sourcePath.getFileName());
                        }
                    }
                }

                if (!docUriFound) {
                    continue;
                }
            }
            // Code-action matched
            codeActionFound = true;
            break;
        }
        Assert.assertTrue(codeActionFound,
                "Cannot find expected Code Action for: " + title + " in " + sourcePath);
    }
}
