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

import com.google.common.reflect.TypeToken;
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
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract test for code action related tests.
 *
 * @since 2.0.0
 */
public abstract class AbstractCodeActionTest extends AbstractLSTest {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path sourcesPath = new File(getClass().getClassLoader().getResource("codeaction").getFile()).toPath();

    private  WorkspaceManager workspaceManager;

    private LanguageServerContext serverContext;

    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        Path configJsonPath = getConfigJsonPath(config);
        TestConfig testConfig = gson.fromJson(Files.newBufferedReader(configJsonPath), TestConfig.class);
        Path sourcePath = sourcesPath.resolve(getResourceDir()).resolve("source").resolve(testConfig.source);
        TestUtil.openDocument(getServiceEndpoint(), sourcePath);

        // Filter diagnostics for the cursor position
        List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics
                = TestUtil.compileAndGetDiagnostics(sourcePath, workspaceManager, serverContext);
        List<Diagnostic> diags = new ArrayList<>(CodeActionUtil.toDiagnostics(diagnostics));
        Position pos = testConfig.position;
        diags = diags.stream().
                filter(diag -> PositionUtil.isWithinRange(pos, diag.getRange()))
                .collect(Collectors.toList());
        CodeActionContext codeActionContext = new CodeActionContext(diags);

        Range range = new Range(pos, pos);
        String res = getResponse(sourcePath, range, codeActionContext);

        List<CodeActionObj> mismatchedCodeActions = new ArrayList<>();
        for (CodeActionObj expected : testConfig.expected) {
            // Create an object to keep track of the actual code action received
            CodeActionObj actual = new CodeActionObj();

            boolean codeActionFound = false;
            JsonObject responseJson = getResponseJson(res);
            for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
                JsonObject right = jsonElement.getAsJsonObject().get("right").getAsJsonObject();
                if (right == null) {
                    continue;
                }

                // Match title
                String actualTitle = right.get("title").getAsString();
                if (!expected.title.equals(actualTitle)) {
                    continue;
                }

                // We have to make sure the title is a match since we are checking against all the
                // code actions received.
                actual.title = actualTitle;

                // Match code action kind
                actual.kind = right.get("kind") == null ? null : right.get("kind").getAsString();
                if (expected.kind != null) {
                    if (!expected.kind.equals(actual.kind)) {
                        continue;
                    }
                }

                // Match edits
                if (expected.edits != null) {
                    JsonElement editsElement = right.get("edit").getAsJsonObject().get("documentChanges")
                            .getAsJsonArray().get(0).getAsJsonObject().get("edits");
                    Type type = new TypeToken<List<TextEdit>>() {
                    }.getType();
                    List<TextEdit> actualEdit = gson.fromJson(editsElement, type);
                    List<TextEdit> expEdit = expected.edits;
                    if (!expEdit.equals(actualEdit)) {
                        actual.edits = actualEdit;
                        continue;
                    }
                }
                // Match args
                if (expected.command != null) {
                    JsonObject expectedCommand = expected.command;
                    JsonObject actualCommand = right.get("command").getAsJsonObject();

                    if (!Objects.equals(actualCommand.get("command"), expectedCommand.get("command"))) {
                        actual.command = actualCommand;
                        continue;
                    }

                    if (!Objects.equals(actualCommand.get("title"), expectedCommand.get("title"))) {
                        actual.command = actualCommand;
                        continue;
                    }

                    JsonArray actualArgs = actualCommand.getAsJsonArray("arguments");
                    JsonArray expArgs = expectedCommand.getAsJsonArray("arguments");
                    if (!TestUtil.isArgumentsSubArray(actualArgs, expArgs)) {
                        actual.command = actualCommand;
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
                        actual.command = actualCommand;
                        continue;
                    }
                }
                // Code-action matched
                codeActionFound = true;
                break;
            }

            if (!codeActionFound && actual.title != null) {
                mismatchedCodeActions.add(actual);
            }
        }
        TestUtil.closeDocument(getServiceEndpoint(), sourcePath);

        String cursorStr = range.getStart().getLine() + ":" + range.getEnd().getCharacter();
        if (!mismatchedCodeActions.isEmpty()) {
//            updateConfig(testConfig, mismatchedCodeActions, configJsonPath);
            Assert.fail(String.format("Cannot find expected code action(s) for: '%s', cursor at [%s] in '%s': %s",
                    Arrays.toString(mismatchedCodeActions.toArray()),
                    cursorStr, sourcePath, testConfig.description));
        }
    }

    public String getResponse(Path sourcePath, Range range, CodeActionContext codeActionContext) {
        return TestUtil.getCodeActionResponse(getServiceEndpoint(), sourcePath.toString(), range, codeActionContext);
    }

    /**
     * For testing negative cases like cases where code actions should not be suggested.
     *
     * @param config Config file name
     */
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        Endpoint endpoint = getServiceEndpoint();
        Path configJsonPath = getConfigJsonPath(config);
        TestConfig testConfig = gson.fromJson(Files.newBufferedReader(configJsonPath), TestConfig.class);
        Path sourcePath = sourcesPath.resolve(getResourceDir()).resolve("source").resolve(testConfig.source);
        TestUtil.openDocument(endpoint, sourcePath);

        // Filter diagnostics for the cursor position
        List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics
                = TestUtil.compileAndGetDiagnostics(sourcePath, workspaceManager, serverContext);
        List<Diagnostic> diags = new ArrayList<>(CodeActionUtil.toDiagnostics(diagnostics));
        Position pos = testConfig.position;
        diags = diags.stream().
                filter(diag -> PositionUtil.isWithinRange(pos, diag.getRange()))
                .collect(Collectors.toList());
        CodeActionContext codeActionContext = new CodeActionContext(diags);

        Range range = new Range(pos, pos);
        String res = TestUtil.getCodeActionResponse(endpoint, sourcePath.toString(), range, codeActionContext);
        for (CodeActionObj expected : testConfig.expected) {
            JsonObject responseJson = this.getResponseJson(res);
            for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
                JsonObject right = jsonElement.getAsJsonObject().get("right").getAsJsonObject();
                if (right == null) {
                    continue;
                }

                // Match title
                String actualTitle = right.get("title").getAsString();
                Assert.assertNotEquals(expected.title, actualTitle,
                        String.format("Found an unexpected code action: %s", testConfig.description));
            }
        }
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

    /**
     * Update mismatched code actions in the test config.
     *
     * @param testConfig            Original test config
     * @param mismatchedCodeActions Mismatched code actions
     * @param configPath            Config file path
     * @throws IOException File operations, etc
     */
    private void updateConfig(TestConfig testConfig, List<CodeActionObj> mismatchedCodeActions, Path configPath)
            throws IOException {
        for (int i = 0; i < testConfig.expected.size(); i++) {
            CodeActionObj codeAction = testConfig.expected.get(i);
            final int idx = i;
            mismatchedCodeActions.stream()
                    .filter(item -> item.title.equals(codeAction.title))
                    .findFirst()
                    .ifPresent(mismatchedCodeAction -> testConfig.expected.set(idx, mismatchedCodeAction));
        }
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

    /**
     * Represents a code action test config.
     */
    static class TestConfig {
        Position position;
        String source;
        List<CodeActionObj> expected;

        String description;
    }

    static class CodeActionObj {
        String title;
        String kind;
        List<TextEdit> edits;
        JsonObject command;

        @Override
        public String toString() {
            return title;
        }
    }
}
