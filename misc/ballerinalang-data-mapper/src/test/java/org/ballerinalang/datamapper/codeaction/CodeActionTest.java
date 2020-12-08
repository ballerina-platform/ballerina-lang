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
import com.google.gson.JsonParser;
import org.ballerinalang.datamapper.util.FileUtils;
import org.ballerinalang.datamapper.util.TestUtil;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class CodeActionTest {

    private Endpoint serviceEndpoint;

    private JsonParser parser = new JsonParser();

    private Path sourcesPath = new File(getClass().getClassLoader().getResource("codeaction").getFile()).toPath();

    private static final WorkspaceManager workspaceManager = new BallerinaWorkspaceManager();

    private static final Logger log = LoggerFactory.getLogger(CodeActionTest.class);

    @BeforeClass
    private void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
        String startConfigPath = "codeaction" + File.separator + "config" + File.separator + "startConfig.json";
        JsonObject configs = FileUtils.fileContentAsObject(startConfigPath);
        TestUtil.setWorkspaceConfig(serviceEndpoint, configs);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider", enabled = false)
    public void testDataMapperCodeAction(String config, String source) throws Exception {
        // Read expected results
        String configJsonPath = "codeaction" + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        // Get code action from language server
        JsonObject responseJson = getCodeActionResponse(source, configJsonObject);

        JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
        String title = expectedResponse.get("title").getAsString();

        int numberOfDataMappingCodeAction = 0;
        boolean codeActionFound = false;
        boolean codeActionFoundOnlyOnce = false;
        for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
            JsonElement right = jsonElement.getAsJsonObject().get("right");
            JsonElement editText = right.getAsJsonObject().get("edit");
            if (editText == null) {
                continue;
            }
            JsonArray edit = editText.getAsJsonObject().get("documentChanges")
                    .getAsJsonArray().get(0).getAsJsonObject().get("edits").getAsJsonArray();

            boolean editsMatched = expectedResponse.get("edits").getAsJsonArray().equals(edit);
            if (right.getAsJsonObject().get("title").getAsString().equals(title) && editsMatched) {
                codeActionFound = true;
                numberOfDataMappingCodeAction = numberOfDataMappingCodeAction + 1;
            }
        }
        if (codeActionFound && numberOfDataMappingCodeAction == 1) {
            codeActionFoundOnlyOnce = true;
        }
        Assert.assertTrue(codeActionFoundOnlyOnce,
                "Cannot find expected Code Action for: " + title);
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider")
    public Object[][] codeActionDataMapperDataProvider() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper1.json", "dataMapper1.bal"},
                {"dataMapper2.json", "dataMapper2.bal"},
                {"dataMapper3.json", "dataMapper3.bal"},
                {"dataMapper4.json", "dataMapper4.bal"},
                {"dataMapper5.json", "dataMapper5.bal"},
        };
    }

    @AfterClass
    private void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private JsonObject getResponseJson(String response) {
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }

    private JsonObject getCodeActionResponse(String source, JsonObject configJsonObject) throws IOException {

        // Read expected results
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        // Filter diagnostics for the cursor position
        List<Diagnostic> diags = new ArrayList<>(
                CodeActionUtil.toDiagnostics(TestUtil.compileAndGetDiagnostics(sourcePath, workspaceManager)));
        Position pos = new Position(configJsonObject.get("line").getAsInt(),
                configJsonObject.get("character").getAsInt());
        diags = diags.stream().
                filter(diag -> CommonUtil.isWithinRange(pos, diag.getRange()))
                .collect(Collectors.toList());
        CodeActionContext codeActionContext = new CodeActionContext(diags);
        Range range = new Range(pos, pos);
        String response = TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range,
                codeActionContext);
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);

        return this.getResponseJson(response);
    }
}
