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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.command.executors.AddAllDocumentationExecutor;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Test Cases for CodeActions.
 *
 * @since 0.982.0
 */
public class CodeActionTest {
    private Endpoint serviceEndpoint;

    private Gson gson = new Gson();

    private JsonParser parser = new JsonParser();

    private Path sourcesPath = new File(getClass().getClassLoader().getResource("codeaction").getFile()).toPath();

    private static final Logger log = LoggerFactory.getLogger(CodeActionTest.class);

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "codeaction-no-diagnostics-data-provider")
    public void testCodeActionWithoutDiagnostics(String config, String source) throws IOException {
        String configJsonPath = "codeaction" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        int numberOfCommands = expected.get("size").getAsInt();
        JsonObject documentThis = expected.getAsJsonObject("actions").getAsJsonObject("documentThis");
        CodeActionContext codeActionContext = new CodeActionContext(new ArrayList<>());
        Range range = gson.fromJson(configJsonObject.get("range"), Range.class);

        TestUtil.openDocument(this.serviceEndpoint, sourcePath);
        String response = TestUtil.getCodeActionResponse(this.serviceEndpoint, sourcePath.toString(), range,
                                                         codeActionContext);
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
        JsonArray result = parser.parse(response).getAsJsonObject().getAsJsonArray("result");

        Assert.assertEquals(numberOfCommands, result.size());
        result.forEach(element -> {
            JsonObject left = element.getAsJsonObject().get("right").getAsJsonObject();
            String title = left.get("title").getAsString();
            JsonObject cmd = left.get("command").getAsJsonObject();
            String command = cmd.get("command").getAsString();
            switch (command) {
                case AddDocumentationExecutor.COMMAND:
                    Assert.assertEquals(title, "Document This");
                    JsonArray args = cmd.get("arguments").getAsJsonArray();
                    JsonArray documentThisArr = documentThis.getAsJsonArray("arguments");
                    Assert.assertTrue(TestUtil.isArgumentsSubArray(args, documentThisArr));
                    break;
                case AddAllDocumentationExecutor.COMMAND:
                    Assert.assertEquals(title, "Document All");
                    break;
                default:
                    Assert.fail("Invalid Command Found: [" + title + "]");
                    break;
            }
        });
    }

    @Test(dataProvider = "codeaction-diagnostics-data-provider")
    public void testCodeActionWithDiagnostics(String config, String source)
            throws IOException, CompilationFailedException {
        String configJsonPath = "codeaction" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        BallerinaFile ballerinaFile = ExtendedLSCompiler.compileFile(sourcePath, CompilerPhase.COMPILER_PLUGIN);
        List<Diagnostic> lsDiagnostics = new ArrayList<>();
        ballerinaFile.getDiagnostics().ifPresent(diagnostics -> lsDiagnostics.addAll(getLSDiagnostics(diagnostics)));
        CodeActionContext codeActionContext = new CodeActionContext(lsDiagnostics);
        Range range = gson.fromJson(configJsonObject.get("range"), Range.class);
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String res = TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, codeActionContext);
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);

        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        String title = expected.get("title").toString();
        String command = expected.get("command").toString();
        JsonArray args = expected.get("arguments").getAsJsonArray();

        boolean codeActionFound = false;
        JsonObject responseJson = this.getResponseJson(res);
        for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
            JsonObject leftItem = jsonElement.getAsJsonObject().get("right").getAsJsonObject();
            JsonObject cmd = leftItem.get("command").getAsJsonObject();
            if (leftItem.get("title").toString().equals(title) &&
                    cmd.get("command").toString().equals(command)
                    && TestUtil.isArgumentsSubArray(cmd.get("arguments").getAsJsonArray(), args)) {
                codeActionFound = true;
                break;
            }
        }

        Assert.assertTrue(codeActionFound, "Cannot find expected Code Action for: " + title);
    }

    @Test(dataProvider = "codeaction-testgen-data-provider", enabled = false)
    public void testCodeActionWithTestGen(String config, Path source) throws IOException, CompilationFailedException {
        String configJsonPath = "codeaction" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        BallerinaFile ballerinaFile = ExtendedLSCompiler.compileFile(sourcePath, CompilerPhase.COMPILER_PLUGIN);
        List<Diagnostic> lsDiagnostics = new ArrayList<>();
        ballerinaFile.getDiagnostics().ifPresent(diagnostics -> lsDiagnostics.addAll(getLSDiagnostics(diagnostics)));
        CodeActionContext context = new CodeActionContext(lsDiagnostics);
        JsonArray ranges = configJsonObject.getAsJsonArray("cursor");
        for (JsonElement rangeElement : ranges) {
            JsonObject rangeObject = rangeElement.getAsJsonObject();
            Position pos = new Position(rangeObject.get("line").getAsInt(), rangeObject.get("character").getAsInt());
            Range range = new Range(pos, pos);
            String res = TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, context);

            JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
            String title = expected.get("title").toString();
            String command = expected.get("command").toString();

            boolean codeActionFound = false;
            JsonObject responseJson = this.getResponseJson(res);
            for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
                JsonElement right = jsonElement.getAsJsonObject().get("right");
                if (right.getAsJsonObject().get("title").toString().equals(title)
                        && right.getAsJsonObject().get("command").getAsJsonObject().get("command").toString().equals(
                        command)) {
                    codeActionFound = true;
                    break;
                }
            }
            String cursorStr = range.getStart().getLine() + ":" + range.getEnd().getCharacter();
            Assert.assertTrue(codeActionFound,
                              "Cannot find expected Code Action for: " + title + ", cursor at " + cursorStr);
        }
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
    }

    @Test(dataProvider = "codeaction-quickfixes-data-provider")
    public void testCodeActionWithQuickFixes(String config, String source)
            throws IOException, CompilationFailedException {
        String configJsonPath = "codeaction" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        BallerinaFile ballerinaFile = ExtendedLSCompiler.compileFile(sourcePath, CompilerPhase.COMPILER_PLUGIN);
        List<Diagnostic> lsDiagnostics = new ArrayList<>();
        ballerinaFile.getDiagnostics().ifPresent(diagnostics -> lsDiagnostics.addAll(getLSDiagnostics(diagnostics)));
        CodeActionContext context = new CodeActionContext(lsDiagnostics);
        Position pos = new Position(configJsonObject.get("line").getAsInt(),
                                    configJsonObject.get("character").getAsInt());
        Range range = new Range(pos, pos);
        String res = TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, context);

        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        String title = expected.get("title").getAsString();

        boolean codeActionFound = false;
        JsonObject responseJson = this.getResponseJson(res);
        for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
            JsonElement right = jsonElement.getAsJsonObject().get("right");
            JsonElement editText = right.getAsJsonObject().get("edit");
            if (editText == null) {
                continue;
            }
            JsonObject edit = editText.getAsJsonObject().get("documentChanges")
                    .getAsJsonArray().get(0).getAsJsonObject().get("edits").getAsJsonArray().get(0)
                    .getAsJsonObject();
            if (right.getAsJsonObject().get("title").getAsString().equals(title) && edit.equals(expected.get("edit"))) {
                codeActionFound = true;
                break;
            }
        }
        String cursorStr = range.getStart().getLine() + ":" + range.getEnd().getCharacter();
        Assert.assertTrue(codeActionFound,
                          "Cannot find expected Code Action for: " + title + ", cursor at " + cursorStr);
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);
    }

    @DataProvider(name = "codeaction-quickfixes-data-provider")
    public Object[][] codeActionQuickFixesDataProvider() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"fixReturnType1.json", "fixReturnType.bal"},
                {"fixReturnType2.json", "fixReturnType.bal"},
                {"fixReturnType3.json", "fixReturnType.bal"},
                {"markUntaintedCodeAction1.json", "taintedVariable.bal"},
                {"markUntaintedCodeAction2.json", "taintedVariable.bal"},
                {"variableAssignmentRequiredCodeAction1.json", "createVariable.bal"},
                {"variableAssignmentRequiredCodeAction2.json", "createVariable.bal"},
                {"variableAssignmentRequiredCodeAction3.json", "createVariable.bal"},
                {"variableAssignmentRequiredCodeAction4.json", "createVariable.bal"},
                {"ignoreReturnValueCodeAction.json", "createVariable.bal"},
                {"typeGuardCodeAction1.json", "typeGuard.bal"},
                {"typeGuardCodeAction2.json", "typeGuard.bal"},
                {"typeGuardCodeAction3.json", "typeGuard.bal"},
//                {"typeGuardCodeAction4.json", "typeGuard.bal"},
        };
    }

    @DataProvider(name = "codeaction-no-diagnostics-data-provider")
    public Object[][] codeActionDataProvider() {
        log.info("Test textDocument/codeAction with no diagnostics");
        return new Object[][]{
                {"singleDocGeneration.json", "singleDocGeneration.bal"},
                {"singleDocGeneration1.json", "singleDocGeneration.bal"},
                {"singleDocGeneration2.json", "singleDocGeneration.bal"},
                {"singleDocGeneration3.json", "singleDocGeneration.bal"},
                {"singleDocGeneration4.json", "singleDocGeneration.bal"},
                {"singleDocGeneration5.json", "singleDocGeneration.bal"},
                {"singleDocGeneration6.json", "singleDocGeneration.bal"},
        };
    }

    @DataProvider(name = "codeaction-diagnostics-data-provider")
    public Object[][] codeActionWithDiagnosticDataProvider() {
        log.info("Test textDocument/codeAction with diagnostics");
        return new Object[][]{
                {"undefinedPackageWithinFunction.json", "codeActionCommon.bal"},
                {"undefinedFunctionCodeAction.json", "createUndefinedFunction.bal"},
                {"undefinedFunctionCodeAction2.json", "createUndefinedFunction2.bal"},
                {"packagePull.json", "packagePull.bal"},
                {"changeAbstractTypeObj1.json", "changeAbstractType.bal"},
                {"changeAbstractTypeObj2.json", "changeAbstractType.bal"}
        };
    }

    @DataProvider(name = "codeaction-testgen-data-provider")
    public Object[][] testGenCodeActionDataProvider() {
        log.info("Test textDocument/codeAction for test generation");
        return new Object[][]{
                {"testGenFunctionCodeAction.json", Paths.get("testgen", "src", "module1", "functions.bal")},
                {"testGenServiceCodeAction.json", Paths.get("testgen", "src", "module2", "services.bal")}
        };
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private JsonObject getResponseJson(String response) {
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }

    private List<Diagnostic> getLSDiagnostics(List<org.ballerinalang.util.diagnostic.Diagnostic> ballerinaDiags) {
        List<Diagnostic> lsDiagnostics = new ArrayList<>();
        ballerinaDiags.forEach(diagnostic -> {
            Diagnostic lsDiagnostic = new Diagnostic();
            lsDiagnostic.setSeverity(DiagnosticSeverity.Error);
            lsDiagnostic.setMessage(diagnostic.getMessage());
            Range r = new Range();

            int startLine = diagnostic.getPosition().getStartLine() - 1; // LSP diagnostics range is 0 based
            int startChar = diagnostic.getPosition().getStartColumn() - 1;
            int endLine = diagnostic.getPosition().getEndLine() - 1;
            int endChar = diagnostic.getPosition().getEndColumn() - 1;

            if (endLine <= 0) {
                endLine = startLine;
            }

            if (endChar <= 0) {
                endChar = startChar + 1;
            }

            r.setStart(new Position(startLine, startChar));
            r.setEnd(new Position(endLine, endChar));
            lsDiagnostic.setRange(r);

            lsDiagnostics.add(lsDiagnostic);
        });

        return lsDiagnostics;
    }
}
