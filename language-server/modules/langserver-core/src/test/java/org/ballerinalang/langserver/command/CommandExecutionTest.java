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
package org.ballerinalang.langserver.command;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.command.executors.AddAllDocumentationExecutor;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.ballerinalang.langserver.command.executors.CreateFunctionExecutor;
import org.ballerinalang.langserver.command.executors.CreateTestExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command Execution Test Cases.
 *
 * @since 0.982.0
 */
public class CommandExecutionTest {

    private Endpoint serviceEndpoint;

    private Gson gson = new Gson();

    private JsonParser parser = new JsonParser();

    private Path sourcesPath = new File(getClass().getClassLoader().getResource("command").getFile()).toPath();

    private static final Logger log = LoggerFactory.getLogger(CommandExecutionTest.class);

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "add-doc-data-provider")
    public void testAddSingleDocumentation(String config, String source) {
        LSContextManager.getInstance().clearAllContexts();
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        List<Object> args = Arrays.asList(
                new CommandArgument("node.type", configJsonObject.get("nodeType").getAsString()),
                new CommandArgument("doc.uri", sourcePath.toUri().toString()),
                new CommandArgument("node.line", configJsonObject.get("nodeLine").getAsString()));
        JsonObject responseJson = getCommandResponse(args, AddDocumentationExecutor.COMMAND);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));
        Assert.assertEquals(responseJson, expected, "Test Failed for: " + config);
    }

    @Test(dataProvider = "add-all-doc-data-provider")
    public void testAddAllDocumentation(String config, String source) {
        LSContextManager.getInstance().clearAllContexts();
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        List<Object> args = Collections.singletonList(
                new CommandArgument("doc.uri", sourcePath.toUri().toString()));
        JsonObject responseJson = getCommandResponse(args, AddAllDocumentationExecutor.COMMAND);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));
        Assert.assertEquals(responseJson, expected, "Test Failed for: " + config);
    }

    @Test(dataProvider = "create-function-data-provider")
    public void testCreateFunction(String config, String source) throws IOException {
        LSContextManager.getInstance().clearAllContexts();
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        List<Object> args = new ArrayList<>();
        JsonObject arguments = configJsonObject.get("arguments").getAsJsonObject();
        args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, sourcePath.toUri().toString()));
        args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, arguments.get("node.line").getAsString()));
        args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, arguments.get("node.column").getAsString()));
        JsonObject responseJson = getCommandResponse(args, CreateFunctionExecutor.COMMAND);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        Assert.assertEquals(responseJson, expected, "Test Failed for: " + config);
    }

    @Test(dataProvider = "testgen-fail-data-provider", enabled = false)
    public void testTestGenerationFailCases(String config, Path source) throws IOException {
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        Path testFilePath = LSCompilerUtil.getCurrentModulePath(sourcePath).resolve(ProjectDirConstants.TEST_DIR_NAME)
                .resolve(CreateTestExecutor.generateTestFileName(sourcePath));

        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonArray cases = configJsonObject.getAsJsonArray("cases");

        for (JsonElement testCase : cases) {
            // Clear old test file
            Files.deleteIfExists(testFilePath);

            List<Object> args = new ArrayList<>();
            JsonObject testCaseConfig = testCase.getAsJsonObject();
            JsonObject arguments = testCaseConfig.get("arguments").getAsJsonObject();
            args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, sourcePath.toUri().toString()));
            args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, arguments.get("node.line").getAsString()));
            args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN,
                    arguments.get("node.column").getAsString()));
            JsonObject responseJson = getCommandResponse(args, CreateTestExecutor.COMMAND);
            JsonElement resultElm = responseJson.get("result");
            if (resultElm.getAsBoolean()) {
                Assert.fail("This test should expected to fail but received:\n" + resultElm.toString());
            }
        }
    }

    @Test(dataProvider = "testgen-data-provider", enabled = false)
    public void testTestGeneration(String config, Path source) throws IOException, CompilationFailedException {
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        Path testFilePath = LSCompilerUtil.getCurrentModulePath(sourcePath).resolve(ProjectDirConstants.TEST_DIR_NAME)
                .resolve(CreateTestExecutor.generateTestFileName(sourcePath));

        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonArray cases = configJsonObject.getAsJsonArray("cases");

        for (JsonElement testCase : cases) {
            // Clear old test file
            Files.deleteIfExists(testFilePath);

            List<Object> args = new ArrayList<>();
            JsonObject testCaseConfig = testCase.getAsJsonObject();
            JsonObject arguments = testCaseConfig.get("arguments").getAsJsonObject();
            args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, sourcePath.toUri().toString()));
            args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, arguments.get("node.line").getAsString()));
            args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN,
                    arguments.get("node.column").getAsString()));
            JsonObject responseJson = getCommandResponse(args, CreateTestExecutor.COMMAND);
            JsonElement resultElm = responseJson.get("result");
            String content = resultElm.getAsJsonObject().get("edit").getAsJsonObject()
                    .getAsJsonArray("documentChanges").get(1).getAsJsonObject().getAsJsonArray("edits").get(0)
                    .getAsJsonObject().get("newText").getAsString();

            // Need to write all text-edits into the test file
            try (FileOutputStream outputStream = new FileOutputStream(testFilePath.toFile())) {
                byte[] strToBytes = content.getBytes(Charset.defaultCharset());
                outputStream.write(strToBytes);
            }

            // Compile the test file through the actual path, since it depends on the source-code
            Path currentModule = LSCompilerUtil.getCurrentModulePath(testFilePath);
            BallerinaFile balFile = ExtendedLSCompiler.compileFile(currentModule, CompilerPhase.TAINT_ANALYZE);
            // Check for compiler errors and diagnostics
            if (!balFile.getBLangPackage().isPresent() ||
                    (balFile.getDiagnostics().isPresent() && !balFile.getDiagnostics().get().isEmpty())) {
                Assert.fail("Generated test file has errors! path: " + testFilePath);
            }

            JsonObject result = testCaseConfig.get("expected").getAsJsonObject();
            List<String> imports = new ArrayList<>();
            List<String> globals = new ArrayList<>();
            List<String> functions = new ArrayList<>();
            List<String> services = new ArrayList<>();
            result.get("imports").getAsJsonArray().forEach(importNode -> imports.add(importNode.getAsString()));
            result.get("globals").getAsJsonArray().forEach(global -> globals.add(global.getAsString()));
            result.get("functions").getAsJsonArray().forEach(func -> functions.add(func.getAsString()));
            result.get("services").getAsJsonArray().forEach(service -> services.add(service.getAsString()));
            BLangPackage bLangPackage = balFile.getBLangPackage().get();
            BLangTestablePackage testablePkg = bLangPackage.getTestablePkg();
            testablePkg.getCompilationUnits()
                    .forEach(unit -> unit.getTopLevelNodes()
                            .forEach(node -> {
                                        if (node instanceof BLangImportPackage) {
                                            BLangImportPackage importPkg = (BLangImportPackage) node;
                                            imports.removeIf(pkgName -> {
                                                return pkgName.equals(importPkg.orgName.value + "/" + importPkg.alias);
                                            });
                                        }
                                    }
                            )
                    );
            // Remove found values from the expected values
            testablePkg.getGlobalVariables().forEach(variable -> {
                globals.removeIf(global -> variable.name.value.equals(global));
            });
            testablePkg.getFunctions().forEach(function -> {
                functions.removeIf(func -> function.name.value.equals(func));
            });
            testablePkg.getServices().forEach(service -> {
                services.removeIf(ser -> service.name.value.equals(ser));
            });
            testablePkg.getGlobalVariables().stream()
                    .filter(simpleVariable -> simpleVariable.type instanceof BServiceType)
                    .forEach(simpleVariable ->
                            services.removeIf(serviceName -> serviceName.equals(simpleVariable.name.value)));
            // Check for pending expected values
            String failMsgTemplate = "Generated test file does not contain following %s:\n%s";
            if (!imports.isEmpty()) {
                Assert.fail(String.format(failMsgTemplate, "imports", String.join(", ", imports)));
            }
            if (!globals.isEmpty()) {
                Assert.fail(String.format(failMsgTemplate, "globals", String.join(", ", globals)));
            }
            if (!functions.isEmpty()) {
                Assert.fail(String.format(failMsgTemplate, "functions", String.join(", ", functions)));
            }
            if (!services.isEmpty()) {
                Assert.fail(String.format(failMsgTemplate, "services", String.join(", ", services)));
            }
        }
        // Clear old test file
        Files.deleteIfExists(testFilePath);
    }

    // TODO: #23371
    // TODO: #23371

    @DataProvider(name = "add-doc-data-provider")
    public Object[][] addDocDataProvider() {
        log.info("Test workspace/executeCommand for command {}", AddDocumentationExecutor.COMMAND);
        return new Object[][] {
                //TODO: Disabled failing doc tests
//                {"addSingleFunctionDocumentation1.json", "addSingleFunctionDocumentation1.bal"},
                {"addSingleFunctionDocumentation2.json", "commonDocumentation.bal"},
                {"addObjectFunctionDocumentation.json", "commonDocumentation.bal"},
                {"addSingleServiceDocumentation.json", "commonDocumentation.bal"},
                {"addSingleRecordDocumentation.json", "commonDocumentation.bal"},
                {"addSingleObjectDocumentation.json", "commonDocumentation.bal"},
                {"serviceDocumentationWithAnnotations.json", "serviceDocumentationWithAnnotations.bal"},
        };
    }

    @DataProvider(name = "add-all-doc-data-provider")
    public Object[][] addAllDocDataProvider() {
        log.info("Test workspace/executeCommand for command {}", AddAllDocumentationExecutor.COMMAND);
        return new Object[][] {
                {"addAllDocumentation.json", "commonDocumentation.bal"},
                {"addAllDocumentationWithAnnotations.json", "addAllDocumentationWithAnnotations.bal"}
        };
    }

    @DataProvider(name = "create-function-data-provider")
    public Object[][] createFunctionDataProvider() {
        log.info("Test workspace/executeCommand for command {}", CreateFunctionExecutor.COMMAND);
        return new Object[][] {
                {"createUndefinedFunction1.json", "createUndefinedFunction.bal"},
                {"createUndefinedFunction2.json", "createUndefinedFunction.bal"},
                {"createUndefinedFunction3.json", "createUndefinedFunction.bal"},
                {"createUndefinedFunction4.json", "createUndefinedFunction2.bal"},
                {"createUndefinedFunction5.json", "createUndefinedFunction3.bal"},
                {"createUndefinedFunction6.json", "createUndefinedFunction4.bal"},
                {"createUndefinedFunction7.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction8.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction9.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction10.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction11.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction12.json", "createUndefinedFunction5.bal"},
                {"createUndefinedFunction13.json", "createUndefinedFunction5.bal"},
        };
    }

    @DataProvider(name = "testgen-data-provider")
    public Object[][] testGenerationDataProvider() {
        log.info("Test workspace/executeCommand for command {}", CreateTestExecutor.COMMAND);
        return new Object[][]{
//                {"testGenerationForFunctions.json", Paths.get("testgen", "module1", "functions.bal")},
                {"testGenerationForServices.json", Paths.get("testgen", "module2", "services.bal")}
        };
    }

    @DataProvider(name = "testgen-fail-data-provider")
    public Object[][] testGenerationNegativeDataProvider() {
        log.info("Test, test generation command failed cases");
        return new Object[][]{
                {"testGenerationForServicesNegative.json", Paths.get("testgen", "module2", "services.bal")},
        };
    }

    @DataProvider(name = "testgen-append-data-provider")
    public Object[][] testGenerationAppendDataProvider() {
        return new Object[][]{
                {"testGenerationForServicesNegative.json", Paths.get("testgen", "module2", "services.bal")},
        };
    }


    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private List argsToJson(List<Object> args) {
        List<JsonObject> jsonArgs = new ArrayList<>();
        for (Object arg: args) {
            jsonArgs.add((JsonObject) gson.toJsonTree(arg));
        }
        return jsonArgs;
    }

    private JsonObject getCommandResponse(List<Object> args, String command) {
        List argsList = argsToJson(args);
        ExecuteCommandParams params  = new ExecuteCommandParams(command, argsList);
        String response = TestUtil.getExecuteCommandResponse(params, this.serviceEndpoint).replace("\\r\\n", "\\n");
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }
}
