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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.command.executors.AddAllDocumentationExecutor;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.ballerinalang.langserver.command.executors.CreateFunctionExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.command.CommandArgument;
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

import java.io.File;
import java.io.IOException;
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

    private final Gson gson = new Gson();

    private final JsonParser parser = new JsonParser();

    private final Path sourcesPath = new File(getClass().getClassLoader().getResource("command").getFile()).toPath();

    private static final Logger log = LoggerFactory.getLogger(CommandExecutionTest.class);

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "add-doc-data-provider")
    public void testAddSingleDocumentation(String config, String source) {
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

    // TODO: #23371
    // TODO: #23371

    @DataProvider(name = "add-doc-data-provider")
    public Object[][] addDocDataProvider() {
        log.info("Test workspace/executeCommand for command {}", AddDocumentationExecutor.COMMAND);
        return new Object[][]{
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
        return new Object[][]{
                {"addAllDocumentation.json", "commonDocumentation.bal"},
                {"addAllDocumentationWithAnnotations.json", "addAllDocumentationWithAnnotations.bal"}
        };
    }

    @DataProvider(name = "create-function-data-provider")
    public Object[][] createFunctionDataProvider() {
        log.info("Test workspace/executeCommand for command {}", CreateFunctionExecutor.COMMAND);
        return new Object[][]{
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
        for (Object arg : args) {
            jsonArgs.add((JsonObject) gson.toJsonTree(arg));
        }
        return jsonArgs;
    }

    private JsonObject getCommandResponse(List<Object> args, String command) {
        List argsList = argsToJson(args);
        ExecuteCommandParams params = new ExecuteCommandParams(command, argsList);
        String response = TestUtil.getExecuteCommandResponse(params, this.serviceEndpoint).replace("\\r\\n", "\\n");
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }
}
