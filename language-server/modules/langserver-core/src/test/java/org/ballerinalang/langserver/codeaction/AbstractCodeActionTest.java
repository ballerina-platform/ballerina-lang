/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.AbstractLSTest;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public abstract class AbstractCodeActionTest extends AbstractLSTest {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path sourcesPath = new File(getClass().getClassLoader().getResource("codeaction").getFile()).toPath();

    private  WorkspaceManager workspaceManager;

    private LanguageServerContext serverContext;

    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        Path configJsonPath = getConfigJsonPath(config);
        TestConfig testConfig = gson.fromJson(Files.newBufferedReader(configJsonPath), TestConfig.class);
        Path sourcePath = sourcesPath.resolve(getResourceDir()).resolve("source").resolve(testConfig.source);
        TestUtil.openDocument(getServiceEndpoint(), sourcePath);

        // Filter diagnostics for the cursor position
        List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics
                = TestUtil.compileAndGetDiagnostics(sourcePath, workspaceManager, serverContext);
        List<Diagnostic> diags = new ArrayList<>(CodeActionUtil.toDiagnostics(diagnostics));
        Position pos = new Position(testConfig.line, testConfig.character);
        diags = diags.stream().
                filter(diag -> PositionUtil.isWithinRange(pos, diag.getRange()))
                .collect(Collectors.toList());
        CodeActionContext codeActionContext = new CodeActionContext(diags);

        Range range = new Range(pos, pos);
        String res = getResponse(sourcePath, range, codeActionContext);

        for (JsonElement element : testConfig.expected) {
            JsonObject expected = element.getAsJsonObject();
            String expTitle = expected.get("title").getAsString();

            boolean codeActionFound = false;
            JsonObject responseJson = getResponseJson(res);
            for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
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
                if (expected.get("edits") != null) {
                    JsonArray actualEdit = right.get("edit").getAsJsonObject().get("documentChanges")
                            .getAsJsonArray().get(0).getAsJsonObject().get("edits").getAsJsonArray();
                    JsonArray expEdit = expected.get("edits").getAsJsonArray();
                    if (!expEdit.equals(actualEdit)) {
                        continue;
                    }
                }
                // Match args
                if (expected.get("command") != null) {
                    JsonObject expectedCommand = expected.get("command").getAsJsonObject();
                    JsonObject actualCommand = right.get("command").getAsJsonObject();

                    if (!Objects.equals(actualCommand.get("command"), expectedCommand.get("command"))) {
                        continue;
                    }

                    if (!Objects.equals(actualCommand.get("title"), expectedCommand.get("title"))) {
                        continue;
                    }

                    JsonArray actualArgs = actualCommand.getAsJsonArray("arguments");
                    JsonArray expArgs = expectedCommand.getAsJsonArray("arguments");
                    if (!TestUtil.isArgumentsSubArray(actualArgs, expArgs)) {
                        continue;
                    }

                    boolean docUriFound = false;
                    for (JsonElement actualArg : actualArgs) {
                        JsonObject arg = actualArg.getAsJsonObject();
                        if ("doc.uri".equals(arg.get("key").getAsString())) {
                            Optional<Path> docPath = PathUtil.getPathFromURI(arg.get("value").getAsString());
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
                // Match code action kind
                JsonElement expKind = expected.get("kind");
                if (expKind != null) {
                    String actualKind = right.get("kind").getAsString();
                    if (!expKind.getAsString().equals(actualKind)) {
                        continue;
                    }
                }
                // Code-action matched
                codeActionFound = true;
                break;
            }
            String cursorStr = range.getStart().getLine() + ":" + range.getEnd().getCharacter();
            Assert.assertTrue(codeActionFound,
                    String.format("Cannot find expected Code Action for: '%s', cursor at [%s] in '%s': %s",
                            expTitle, cursorStr, sourcePath, testConfig.description));
        }
        TestUtil.closeDocument(getServiceEndpoint(), sourcePath);
        updateConfig(testConfig, configJsonPath);
    }

    public String getResponse(Path sourcePath, Range range, CodeActionContext codeActionContext) {
        return TestUtil.getCodeActionResponse(getServiceEndpoint(), sourcePath.toString(), range, codeActionContext);
    }

    /**
     * For testing negative cases like cases where code actions should not be suggested.
     *
     * @param config Config file name
     * @param source Source file name
     */
    public void negativeTest(String config, String source) throws IOException, WorkspaceDocumentException {
        Endpoint endpoint = getServiceEndpoint();
        Path configJsonPath = getConfigJsonPath(config);
        TestConfig testConfig = gson.fromJson(Files.newBufferedReader(configJsonPath), TestConfig.class);
        Path sourcePath = sourcesPath.resolve(getResourceDir()).resolve("source").resolve(testConfig.source);
        TestUtil.openDocument(endpoint, sourcePath);

        // Filter diagnostics for the cursor position
        List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics
                = TestUtil.compileAndGetDiagnostics(sourcePath, workspaceManager, serverContext);
        List<Diagnostic> diags = new ArrayList<>(CodeActionUtil.toDiagnostics(diagnostics));
        Position pos = new Position(testConfig.line, testConfig.character);
        diags = diags.stream().
                filter(diag -> PositionUtil.isWithinRange(pos, diag.getRange()))
                .collect(Collectors.toList());
        CodeActionContext codeActionContext = new CodeActionContext(diags);

        Range range = new Range(pos, pos);
        String res = TestUtil.getCodeActionResponse(endpoint, sourcePath.toString(), range, codeActionContext);
        for (JsonElement element : testConfig.expected) {
            JsonObject expected = element.getAsJsonObject();
            String notExpectedTitle = expected.get("title").getAsString();

            JsonObject responseJson = this.getResponseJson(res);
            for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
                JsonObject right = jsonElement.getAsJsonObject().get("right").getAsJsonObject();
                if (right == null) {
                    continue;
                }

                // Match title
                String actualTitle = right.get("title").getAsString();
                Assert.assertNotEquals(notExpectedTitle, actualTitle,
                        String.format("Found an unexpected code action: %s", testConfig.description));
            }
        }
        updateConfig(testConfig, configJsonPath);
    }

    private Path getConfigJsonPath(String configFilePath) {
        return FileUtils.RES_DIR.resolve("codeaction")
                .resolve(getResourceDir())
                .resolve("config")
                .resolve(configFilePath);
    }

    private JsonObject getResponseJson(String response) {
        JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }
    
    @BeforeClass
    public void setup() {
        workspaceManager = new BallerinaWorkspaceManager(new LanguageServerContextImpl());
        serverContext = new LanguageServerContextImpl();
    }

    private void updateConfig(TestConfig testConfig, Path configPath) throws IOException {
        String objStr = gson.toJson(testConfig).concat(System.lineSeparator());
        Files.write(configPath, objStr.getBytes(StandardCharsets.UTF_8));
    }

    @DataProvider(name = "codeaction-data-provider")
    public abstract Object[][] dataProvider();

    public abstract String getResourceDir();
    
    @AfterClass
    public void cleanUp() {
        this.serverContext = null;
        this.workspaceManager = null;
    }

    static class TestConfig {
        int line;
        int character;
        Position position;
        String source;
        JsonArray expected;

        String description;
    }
}
