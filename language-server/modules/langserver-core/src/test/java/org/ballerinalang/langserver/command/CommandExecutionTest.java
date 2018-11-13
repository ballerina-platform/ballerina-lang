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
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.completion.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
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

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }
    
    @Test(dataProvider = "package-import-data-provider")
    public void testImportPackageCommand(String config, String source) {
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        List<Object> args = Arrays.asList(
                new CommandUtil.CommandArgument("module", configJsonObject.get("module").getAsString()),
                new CommandUtil.CommandArgument("doc.uri", sourcePath.toUri().toString()));
        JsonObject responseJson = getCommandResponse(args, CommandConstants.CMD_IMPORT_MODULE);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));
        Assert.assertTrue(responseJson.equals(expected));
    }

    @Test(dataProvider = "add-doc-data-provider")
    public void testAddSingleDocumentation(String config, String source) {
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        List<Object> args = Arrays.asList(
                new CommandUtil.CommandArgument("node.type", configJsonObject.get("nodeType").getAsString()),
                new CommandUtil.CommandArgument("doc.uri", sourcePath.toUri().toString()),
                new CommandUtil.CommandArgument("node.line", configJsonObject.get("nodeLine").getAsString()));
        JsonObject responseJson = getCommandResponse(args, CommandConstants.CMD_ADD_DOCUMENTATION);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));
        Assert.assertTrue(responseJson.equals(expected));
    }

    @Test(dataProvider = "add-all-doc-data-provider")
    public void testAddAllDocumentation(String config, String source) {
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        List<Object> args = Collections.singletonList(
                new CommandUtil.CommandArgument("doc.uri", sourcePath.toUri().toString()));
        JsonObject responseJson = getCommandResponse(args, CommandConstants.CMD_ADD_ALL_DOC);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));
        Assert.assertTrue(responseJson.equals(expected));
    }
    
    @Test(description = "Test Create Constructor for object")
    public void testCreateConstructor() {
        String configJsonPath = "command" + File.separator + "createConstructor.json";
        Path sourcePath = sourcesPath.resolve("source").resolve("commonDocumentation.bal");
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        List<Object> args = Arrays.asList(
                new CommandUtil.CommandArgument("node.type", configJsonObject.get("nodeType").getAsString()),
                new CommandUtil.CommandArgument("doc.uri", sourcePath.toUri().toString()),
                new CommandUtil.CommandArgument("node.line", configJsonObject.get("nodeLine").getAsString()));
        JsonObject responseJson = getCommandResponse(args, CommandConstants.CMD_CREATE_CONSTRUCTOR);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));
        Assert.assertTrue(responseJson.equals(expected));
    }
    
    @Test(dataProvider = "create-function-data-provider")
    public void testCreateFunction(String config, String source) {
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        List<Object> args = new ArrayList<>();
        args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_DOC_URI, sourcePath.toUri().toString()));
        args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_FUNC_ARGS,
                configJsonObject.get("arguments").getAsString()));
        args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_FUNC_NAME,
                configJsonObject.get("functionName").getAsString()));
        if (configJsonObject.get("returns") != null && configJsonObject.get("returnsDefault") != null) {
            args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_RETURN_TYPE,
                    configJsonObject.get("returns").toString()));
            args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_RETURN_DEFAULT_VAL,
                    configJsonObject.get("returnsDefault").toString()));
        }
        JsonObject responseJson = getCommandResponse(args, CommandConstants.CMD_CREATE_FUNCTION);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));
        Assert.assertTrue(responseJson.equals(expected));
    }

    @Test(dataProvider = "create-variable-data-provider")
    public void testCreateVariable(String config, String source) {
        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        List<Object> args = new ArrayList<>();
        args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_DOC_URI, sourcePath.toUri().toString()));
        args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_FUNC_LOCATION,
                                                 configJsonObject.get("functionLocation").getAsString()));
        args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_RETURN_TYPE,
                                                 configJsonObject.get("functionReturnType").getAsString()));
        args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_VAR_NAME,
                                                 configJsonObject.get("variableName").getAsString()));
        JsonObject responseJson = getCommandResponse(args, CommandConstants.CMD_CREATE_VARIABLE);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));
        Assert.assertTrue(responseJson.equals(expected));
    }

    @DataProvider(name = "package-import-data-provider")
    public Object[][] addImportDataProvider() {
        return new Object[][] {
                {"importPackage1.json", "importPackage1.bal"},
                {"importPackage2.json", "importPackage2.bal"},
        };
    }

    @DataProvider(name = "add-doc-data-provider")
    public Object[][] addDocDataProvider() {
        return new Object[][] {
                {"addSingleFunctionDocumentation1.json", "addSingleFunctionDocumentation1.bal"},
                {"addSingleFunctionDocumentation2.json", "commonDocumentation.bal"},
                {"addObjectFunctionDocumentation.json", "commonDocumentation.bal"},
                {"addSingleEndpointDocumentation.json", "commonDocumentation.bal"},
                {"addSingleServiceDocumentation.json", "commonDocumentation.bal"},
                {"addSingleRecordDocumentation.json", "commonDocumentation.bal"},
                {"addSingleObjectDocumentation.json", "commonDocumentation.bal"},
                {"serviceDocumentationWithAnnotations.json", "serviceDocumentationWithAnnotations.bal"},
        };
    }

    @DataProvider(name = "add-all-doc-data-provider")
    public Object[][] addAllDocDataProvider() {
        return new Object[][] {
                {"addAllDocumentation.json", "commonDocumentation.bal"},
                {"addAllDocumentationWithAnnotations.json", "addAllDocumentationWithAnnotations.bal"}
        };
    }

    @DataProvider(name = "create-function-data-provider")
    public Object[][] createFunctionDataProvider() {
        return new Object[][] {
                {"createUndefinedFunction1.json", "createUndefinedFunction.bal"},
                {"createUndefinedFunction2.json", "createUndefinedFunction.bal"},
        };
    }

    @DataProvider(name = "create-variable-data-provider")
    public Object[][] createVariableDataProvider() {
        return new Object[][] {
                {"createVariable1.json", "createVariable.bal"},
                {"createVariable2.json", "createVariable.bal"},
                {"createVariable3.json", "createVariable.bal"},
        };
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
    
    private List argsToTreeMap(List<Object> args) {
        return gson.fromJson(gson.toJsonTree(args).getAsJsonArray().toString(), List.class);
    }
    
    private JsonObject getCommandResponse(List<Object> args, String command) {
        List treeMapList = argsToTreeMap(args);
        ExecuteCommandParams params  = new ExecuteCommandParams(command, treeMapList);
        String response = TestUtil.getExecuteCommandResponse(params, this.serviceEndpoint).replace("\\r\\n", "\\n");
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }
}
