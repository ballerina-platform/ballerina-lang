/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
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
        String responseData = "{\"answer\":\"\\nfunction mapStudentToGrades (Student student) " +
                "returns Grades {\\n// Some record fields might be missing in the AI based mapping.\\n\\t" +
                "Grades grades = {maths: student.grades.maths, chemistry: student.grades.chemistry, " +
                "physics: student.grades.physics};\\n\\treturn grades;\\n}\"}";
        HttpResponse httpResponse = new HttpResponse(responseData, HTTP_200_OK);
        PowerMockito.spy(HttpClientRequest.class);
        PowerMockito.doReturn(httpResponse).when(HttpClientRequest.class, "doPost", any(String.class),
                any(String.class), any(Map.class));

        String configJsonPath = "codeaction" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        List<Diagnostic> diagnostics = new ArrayList<>(
                CodeActionUtil.toDiagnostics(TestUtil.compileAndGetDiagnostics(sourcePath)));
        CodeActionContext codeActionContext = new CodeActionContext(diagnostics);

        Position position = new Position(configJsonObject.get("line").getAsInt(),
                configJsonObject.get("character").getAsInt());
        Range range = new Range(position, position);
        String response =
                TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, codeActionContext);

        JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
        String title = expectedResponse.get("title").getAsString();

        int numberOfDataMappingCodeAction = 0;
        boolean codeActionFound = false;
        boolean codeActionFoundOnlyOnce = false;
        JsonObject responseJson = this.getResponseJson(response);
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
        String cursorStr = range.getStart().getLine() + ":" + range.getEnd().getCharacter();
        Assert.assertTrue(codeActionFoundOnlyOnce,
                "Cannot find expected Code Action for: " + title + ", cursor at " + cursorStr);
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-un-processable-data")
    public void testDataMapperCodeActionWithUnProcessableData(String config, String source) throws Exception {
        String responseData = "";
        HttpResponse httpResponse = new HttpResponse(responseData, HTTP_422_UN_PROCESSABLE_ENTITY);
        PowerMockito.spy(HttpClientRequest.class);
        PowerMockito.doReturn(httpResponse).when(HttpClientRequest.class, "doPost", any(String.class),
                any(String.class), any(Map.class));

        String configJsonPath = "codeaction" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        List<Diagnostic> diagnostics = new ArrayList<>(
                CodeActionUtil.toDiagnostics(TestUtil.compileAndGetDiagnostics(sourcePath)));
        CodeActionContext codeActionContext = new CodeActionContext(diagnostics);

        Position position = new Position(configJsonObject.get("line").getAsInt(),
                configJsonObject.get("character").getAsInt());
        Range range = new Range(position, position);
        String response =
                TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, codeActionContext);

        JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
        String title = expectedResponse.get("title").getAsString();

        boolean codeActionFound = false;
        JsonObject responseJson = this.getResponseJson(response);
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
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-server-error")
    public void testDataMapperCodeActionWithServerError(String config, String source) throws Exception {
        String responseData = "";
        HttpResponse httpResponse = new HttpResponse(responseData, HTTP_500_INTERNAL_SERVER_ERROR);
        PowerMockito.spy(HttpClientRequest.class);
        PowerMockito.doReturn(httpResponse).when(HttpClientRequest.class, "doPost", any(String.class),
                any(String.class), any(Map.class));

        String configJsonPath = "codeaction" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        List<Diagnostic> diagnostics = new ArrayList<>(
                CodeActionUtil.toDiagnostics(TestUtil.compileAndGetDiagnostics(sourcePath)));
        CodeActionContext codeActionContext = new CodeActionContext(diagnostics);

        Position position = new Position(configJsonObject.get("line").getAsInt(),
                configJsonObject.get("character").getAsInt());
        Range range = new Range(position, position);
        String response =
                TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, codeActionContext);

        JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
        String title = expectedResponse.get("title").getAsString();

        boolean codeActionFound = false;
        JsonObject responseJson = this.getResponseJson(response);
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
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
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

}
