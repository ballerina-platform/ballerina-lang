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
import java.util.Arrays;
import java.util.List;

/**
 * Command Execution Test Cases.
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
                new CommandUtil.CommandArgument("package", configJsonObject.get("package").getAsString()),
                new CommandUtil.CommandArgument("doc.uri", sourcePath.toUri().toString()));
        JsonObject responseJson = getCommandResponse(args, CommandConstants.CMD_IMPORT_PACKAGE);
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
                {"addSingleFunctionDocumentation2.json", "addSingleFunctionDocumentation2.bal"},
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
