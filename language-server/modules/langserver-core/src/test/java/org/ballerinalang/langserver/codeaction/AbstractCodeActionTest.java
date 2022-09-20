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
import org.ballerinalang.langserver.commons.codeaction.CodeActionData;
import org.ballerinalang.langserver.commons.codeaction.ResolvableCodeAction;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.CodeAction;
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
import java.nio.file.Paths;
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

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Path sourcesPath = new File(getClass().getClassLoader().getResource("codeaction").getFile()).toPath();

    private WorkspaceManager workspaceManager;

    private LanguageServerContext serverContext;

    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        Path configJsonPath = getConfigJsonPath(config);
        TestConfig testConfig = gson.fromJson(Files.newBufferedReader(configJsonPath), TestConfig.class);
        Path sourceRoot = sourcesPath.resolve(getResourceDir()).resolve("source");
        Path sourcePath = sourceRoot.resolve(testConfig.source);
        TestUtil.openDocument(getServiceEndpoint(), sourcePath);

        // Filter diagnostics for the cursor position
        List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics
                = TestUtil.compileAndGetDiagnostics(sourcePath, workspaceManager, serverContext);
        List<Diagnostic> diags = new ArrayList<>(CodeActionUtil.toDiagnostics(diagnostics));
        Range range = testConfig.range;
        if (range == null) {
            Position pos = testConfig.position;
            range = new Range(pos, pos);
        }

        Range finalRange = range;
        diags = diags.stream()
                .filter(diag -> PositionUtil.isRangeWithinRange(finalRange, diag.getRange()))
                .collect(Collectors.toList());
        CodeActionContext codeActionContext = new CodeActionContext(diags);

        String res = getResponse(sourcePath, finalRange, codeActionContext);

        List<CodeActionObj> mismatchedCodeActions = new ArrayList<>();
        int matchedCodeActionsCount = 0;
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
                matchedCodeActionsCount++;
                boolean misMatched = false;
                // We have to make sure the title is a match since we are checking against all the
                // code actions received.
                actual.title = actualTitle;

                // Match code action kind
                actual.kind = right.get("kind") == null ? null : right.get("kind").getAsString();
                if (expected.kind != null) {
                    if (!expected.kind.equals(actual.kind)) {
                        misMatched = true;
                    }
                }

                // Match edits
                if (expected.edits != null) {
                    JsonElement editsElement;
                    if (expected.resolvable) {
                        actual.resolvable = true;
                        //Resolvable code actions can't return edits before it's resolved
                        Assert.assertFalse(right.has("edits"));
                        ResolvableCodeAction action = ResolvableCodeAction.from(gson.fromJson(right, CodeAction.class));
                        String response = TestUtil.getCodeActionResolveResponse(getServiceEndpoint(), action);
                        JsonObject resolvedResponse = getResponseJson(response);
                        editsElement = resolvedResponse.get("result").getAsJsonObject().get("edit").getAsJsonObject()
                                .get("documentChanges").getAsJsonArray().get(0).getAsJsonObject().get("edits");
                        JsonElement actualData = right.get("data");
                        actual.data = gson.fromJson(actualData, CodeActionData.class);
                        this.alterFileUri(actual, sourceRoot);
                        if (expected.data == null || actual.data == null
                                || !compareCodeActionData(actual.data, expected.data)) {
                            misMatched = true;
                        }
                    } else {
                        editsElement = right.get("edit").getAsJsonObject().get("documentChanges")
                                .getAsJsonArray().get(0).getAsJsonObject().get("edits");
                    }

                    Type type = new TypeToken<List<TextEdit>>() {
                    }.getType();
                    List<TextEdit> actualEdit = gson.fromJson(editsElement, type);
                    List<TextEdit> expEdit = expected.edits;
                    actual.edits = actualEdit;
                    if (!expEdit.equals(actualEdit)) {
                        misMatched = true;
                    }
                }

                // Match args
                JsonObject actualCommand = right.get("command").getAsJsonObject();
                if (actualCommand != null &&
                        !("Report usage statistics".equals(actualCommand.get("title").getAsString()))) {
                    if (expected.command == null) {
                        misMatched = true;
                    } else {
                        JsonObject expectedCommand = expected.command;
                        if (!Objects.equals(actualCommand.get("command"), expectedCommand.get("command"))) {
                            misMatched = true;
                        }

                        if (!Objects.equals(actualCommand.get("title"), expectedCommand.get("title"))) {
                            misMatched = true;
                        }

                        JsonArray actualArgs = actualCommand.getAsJsonArray("arguments");
                        JsonArray expArgs = expectedCommand.getAsJsonArray("arguments");
                        if (!validateAndModifyArguments(actualCommand, actualArgs, expArgs, sourceRoot, sourcePath)) {
                            misMatched = true;
                        }
                        actual.command = actualCommand;
                    }
                }

                // Code-action matched
                if (!misMatched) {
                    codeActionFound = true;
                    break;
                }
            }

            if (!codeActionFound && actual.title != null) {
                mismatchedCodeActions.add(actual);
            }
        }
        TestUtil.closeDocument(getServiceEndpoint(), sourcePath);

        String cursorStartStr = range.getStart().getLine() + ":" + range.getStart().getCharacter();
        String cursorEndStr = range.getEnd().getLine() + ":" + range.getEnd().getCharacter();
        if (!mismatchedCodeActions.isEmpty()) {
//            updateConfig(testConfig, mismatchedCodeActions, configJsonPath);
            Assert.fail(
                    String.format("Cannot find expected code action(s) for: '%s', range from [%s] to [%s] in '%s': %s",
                            Arrays.toString(mismatchedCodeActions.toArray()),
                            cursorStartStr, cursorEndStr, sourcePath, testConfig.description));
        }

        if (matchedCodeActionsCount != testConfig.expected.size()) {
            Assert.fail(
                    String.format("Cannot find expected code action(s) for: '%s', range from [%s] to [%s] in '%s': %s",
                            Arrays.toString(mismatchedCodeActions.toArray()),
                            cursorStartStr, cursorEndStr, sourcePath, testConfig.description));
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
        Range range = testConfig.range;
        if (range == null) {
            Position position = testConfig.position;
            range = new Range(position, position);
        }
        Range finalRange = range;
        diags = diags.stream()
                .filter(diag -> PositionUtil.isRangeWithinRange(finalRange, diag.getRange()))
                .collect(Collectors.toList());
        CodeActionContext codeActionContext = new CodeActionContext(diags);

        String res = TestUtil.getCodeActionResponse(endpoint, sourcePath.toString(), finalRange, codeActionContext);
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

    protected void alterExpectedUri(CodeActionData data, Path root) throws IOException {
        if (data == null || data.getFileUri() == null) {
            return;
        }
        String[] uriComponents = data.getFileUri().replace("\"", "").split("/");
        Path expectedPath = Paths.get(root.toUri());
        for (String uriComponent : uriComponents) {
            expectedPath = expectedPath.resolve(uriComponent);
        }
        data.setFileUri(expectedPath.toFile().getCanonicalPath());
    }

    protected void alterFileUri(CodeActionObj actual, Path root) {
        if (actual.data == null || actual.data.getFileUri() == null) {
            return;
        }
        String fileUri = actual.data.getFileUri().replace(root.toUri().toString(), "");
        actual.data.setFileUri(fileUri);
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

        Range range;
        Position position;
        String source;
        String description;
        List<CodeActionObj> expected;

    }

    static class CodeActionObj {

        String title;
        String kind;
        List<TextEdit> edits;
        JsonObject command;
        boolean resolvable = false;
        CodeActionData data;

        @Override
        public String toString() {
            return title;
        }
    }

    protected Object convertActionData(Object actionData) {
        return actionData;
    }

    protected boolean compareCodeActionData(CodeActionData actual, CodeActionData expected) {
        JsonElement actualActionData = gson.toJsonTree(convertActionData(actual.getActionData()));
        JsonElement expectedActionData = gson.toJsonTree(convertActionData(expected.getActionData()));
        return expected.getFileUri().equals(actual.getFileUri())
                && expected.getCodeActionName().equals(actual.getCodeActionName())
                && expected.getExtName().equals(actual.getExtName())
                && expected.getRange().equals(actual.getRange())
                && expectedActionData.equals(actualActionData);
    }

    protected boolean validateAndModifyArguments(JsonObject actualCommand,
                                                 JsonArray actualArgs,
                                                 JsonArray expArgs,
                                                 Path sourceRoot,
                                                 Path sourcePath) {
        //Validate the args of rename command
        if ("ballerina.action.rename".equals(actualCommand.get("command").getAsString())) {
            if (actualArgs.size() == 2) {
                Optional<String> actualFilePath =
                        PathUtil.getPathFromURI(actualArgs.get(0).getAsString())
                                .map(path -> path.toString().replace(sourceRoot.toString(), ""));
                int actualRenamePosition = expArgs.get(1).getAsInt();

                String expectedFilePath = expArgs.get(0).getAsString();
                int expectedRenamePosition = expArgs.get(1).getAsInt();
                if (actualFilePath.isPresent()) {
                    String actualPath = actualFilePath.get();
                    if (actualFilePath.get().startsWith("/") || actualFilePath.get().startsWith("\\")) {
                        actualPath = actualFilePath.get().substring(1);
                    }
                    if (sourceRoot.resolve(actualPath).equals(sourceRoot.resolve(expectedFilePath))
                            && actualRenamePosition == expectedRenamePosition) {
                        return true;
                    }
                    JsonArray newArgs = new JsonArray();
                    newArgs.add(actualPath);
                    newArgs.add(actualRenamePosition);

                    //Replace the args of the actual command to update the test config
                    actualCommand.add("arguments", newArgs);
                }
            }
            return false;
        }
        if (!TestUtil.isArgumentsSubArray(actualArgs, expArgs)) {
            return false;
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

        return docUriFound;
    }
}
