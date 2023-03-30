/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Command Execution Test Cases.
 */
public abstract class AbstractCommandExecutionTest {

    private Endpoint serviceEndpoint;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final JsonParser parser = new JsonParser();

    private final Path resourcesPath = new File(getClass().getClassLoader().getResource("command").getFile()).toPath();

    private static final Logger log = LoggerFactory.getLogger(AbstractCommandExecutionTest.class);

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    public void performTest(String config, String command) throws IOException {
        Path configJsonPath = FileUtils.RES_DIR.resolve("command")
                .resolve(getSourceRoot())
                .resolve("config")
                .resolve(config);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath.toString());
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();
        String source = configJsonObject.get("source").getAsString();

        Path sourcePath = resourcesPath.resolve(getSourceRoot()).resolve("source").resolve(source);
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        List<Object> args = getArgs(configJsonObject, sourcePath);

        JsonObject responseJson = getCommandResponse(args, command);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));

        log.debug("Actual response: {}", responseJson.get("result").getAsJsonObject().get("edit"));
        log.debug("Expected response: {}", expected.get("result").getAsJsonObject().get("edit"));

        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        Assert.assertEquals(responseJson, expected, "Test Failed for: " + config);
    }

    /**
     * Get args to be sent to LS.
     *
     * @param configJson Config json
     * @param sourcePath Source file path
     * @return List of args
     */
    protected List<Object> getArgs(JsonObject configJson, Path sourcePath) {
        List<Object> args = new ArrayList<>();
        args.add(CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, sourcePath.toUri().toString()));

        JsonObject arguments = configJson.get("arguments").getAsJsonObject();
        args.addAll(getArgs(arguments));

        return args;
    }

    protected abstract List<Object> getArgs(JsonObject argsObject);

    /**
     * Get the root directory name where test sources and test config are located.
     *
     * @return directory name
     */
    protected abstract String getSourceRoot();

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
