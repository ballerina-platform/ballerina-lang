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
import org.ballerinalang.datamapper.utils.HttpClientRequest;
import org.ballerinalang.datamapper.utils.HttpResponse;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.testng.PowerMockTestCase;
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
import java.util.Map;

import static org.mockito.Matchers.any;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientRequest.class)
public class CodeActionTest extends PowerMockTestCase {

    private Endpoint serviceEndpoint;

    private JsonParser parser = new JsonParser();

    private Path sourcesPath = new File(getClass().getClassLoader().getResource("codeaction").getFile()).toPath();

    private static final Logger log = LoggerFactory.getLogger(CodeActionTest.class);
    private static final int HTTP_200_OK = 200;
    private static final int HTTP_422_UN_PROCESSABLE_ENTITY = 422;
    private static final int HTTP_500_INTERNAL_SERVER_ERROR = 500;

    @BeforeClass
    private void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
        String startConfigPath = "codeaction" + File.separator + "config" + File.separator + "startConfig.json";
        JsonObject configs = FileUtils.fileContentAsObject(startConfigPath);
        TestUtil.setWorkspaceConfig(serviceEndpoint, configs);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider")
    public void testDataMapperCodeAction(String config, String source) throws Exception {
        // Mocking server response
        String responseData = "{\"answer\":\"\\nfunction mapStudentToGrades (Student student) " +
                "returns Grades {\\n// Some record fields might be missing in the AI based mapping.\\n\\t" +
                "Grades grades = {maths: student.grades.maths, chemistry: student.grades.chemistry, " +
                "physics: student.grades.physics};\\n\\treturn grades;\\n}\"}";
        HttpResponse httpResponse = new HttpResponse(responseData, HTTP_200_OK);
        PowerMockito.spy(HttpClientRequest.class);
        PowerMockito.doReturn(httpResponse).when(HttpClientRequest.class, "doPost", any(String.class),
                any(String.class), any(Map.class));

        // Read expected results
        String configJsonPath = "codeaction" + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
        String title = expectedResponse.get("title").getAsString();

        // Get code action from language server
        JsonObject responseJson = getCodeActionResponse(source, configJsonObject);

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

    @Test(dataProvider = "codeAction-data-mapper-data-provider-un-processable-data")
    public void testDataMapperCodeActionWithUnProcessableData(String config, String source) throws Exception {
        // Mocking server response
        String responseData = "";
        HttpResponse httpResponse = new HttpResponse(responseData, HTTP_422_UN_PROCESSABLE_ENTITY);
        PowerMockito.spy(HttpClientRequest.class);
        PowerMockito.doReturn(httpResponse).when(HttpClientRequest.class, "doPost", any(String.class),
                any(String.class), any(Map.class));

        // Read expected results
        String configJsonPath = "codeaction" + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
        String title = expectedResponse.get("title").getAsString();

        // Get code action from language server
        JsonObject responseJson = getCodeActionResponse(source, configJsonObject);

        boolean codeActionFound = false;
        for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
            JsonElement right = jsonElement.getAsJsonObject().get("right");
            JsonElement editText = right.getAsJsonObject().get("edit");
            if (editText == null) {
                continue;
            }
            if (right.getAsJsonObject().get("title").getAsString().equals(title)) {
                codeActionFound = true;
            }
        }
        Assert.assertFalse(codeActionFound, "Returned an invalid code action");
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-server-error")
    public void testDataMapperCodeActionWithServerError(String config, String source) throws Exception {
        // Mocking server response
        String responseData = "";
        HttpResponse httpResponse = new HttpResponse(responseData, HTTP_500_INTERNAL_SERVER_ERROR);
        PowerMockito.spy(HttpClientRequest.class);
        PowerMockito.doReturn(httpResponse).when(HttpClientRequest.class, "doPost", any(String.class),
                any(String.class), any(Map.class));

        // Read expected results
        String configJsonPath = "codeaction" + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
        String title = expectedResponse.get("title").getAsString();

        // Get code action from language server
        JsonObject responseJson = getCodeActionResponse(source, configJsonObject);

        boolean codeActionFound = false;
        for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
            JsonElement right = jsonElement.getAsJsonObject().get("right");
            JsonElement editText = right.getAsJsonObject().get("edit");
            if (editText == null) {
                continue;
            }
            if (right.getAsJsonObject().get("title").getAsString().equals(title)) {
                codeActionFound = true;
            }
        }
        Assert.assertFalse(codeActionFound,
                "Returned an invalid code action");
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider")
    public Object[][] codeActionDataMapperDataProvider() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper1.json", "dataMapper1.bal"},
                {"dataMapper1.json", "dataMapper1.bal"},
                {"dataMapper2.json", "dataMapper2.bal"},
                {"dataMapper4.json", "dataMapper4.bal"},
                {"dataMapper5.json", "dataMapper5.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-un-processable-data")
    public Object[][] codeActionDataMapperDataProviderUnProcessableData() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper3.json", "dataMapper3.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-server-error")
    public Object[][] codeActionDataMapperDataProviderServerError() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper3.json", "dataMapper3.bal"},
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

    private JsonObject getCodeActionResponse(String source, JsonObject configJsonObject) throws IOException,
            CompilationFailedException {
        Position position = new Position(configJsonObject.get("line").getAsInt(),
                configJsonObject.get("character").getAsInt());
        Range range = new Range(position, position);
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        List<Diagnostic> diagnostics = new ArrayList<>(
                CodeActionUtil.toDiagnostics(TestUtil.compileAndGetDiagnostics(sourcePath)));
        CodeActionContext codeActionContext = new CodeActionContext(diagnostics);
        String response =
                TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, codeActionContext);

        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
        return this.getResponseJson(response);
    }
}
