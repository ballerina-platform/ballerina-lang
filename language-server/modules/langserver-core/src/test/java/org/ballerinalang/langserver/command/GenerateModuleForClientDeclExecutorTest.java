/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ballerina.projects.DiagnosticResult;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.command.executors.GenerateModuleForClientDeclExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.util.TestUtil;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit Test for {@link org.ballerinalang.langserver.command.executors.GenerateModuleForClientDeclExecutor}.
 *
 * @since 2201.3.0
 */
public class GenerateModuleForClientDeclExecutorTest {

    private BallerinaLanguageServer languageServer;

    private LanguageServerContext serverContext;

    private WorkspaceManager workspaceManager;

    protected Endpoint serviceEndpoint;

    private final Path resourcesPath =
            new File(getClass().getClassLoader().getResource("command").getFile()).toPath();

    private final Path sourcesDir = new File(getClass().getClassLoader()
            .getResource("command").getFile()).toPath();

    private final JsonParser parser = new JsonParser();
    private final Gson gson = new Gson();

    @BeforeClass
    public void init() throws Exception {
        System.clearProperty("LANG_REPO_BUILD");
        this.languageServer = new BallerinaLanguageServer();
        this.serverContext = languageServer.getServerContext();
        this.workspaceManager = languageServer.getWorkspaceManager();
        this.serviceEndpoint = TestUtil.initializeLanguageSever(languageServer);
    }

    @Test
    public void test() throws IOException, InterruptedException {
        Path sourcePath = sourcesDir.resolve("generate-module-for-client-decl")
                .resolve("source").resolve("client_decl").resolve("main.bal");

        TestUtil.openDocument(serviceEndpoint, sourcePath);
        DiagnosticResult diagnosticResult = workspaceManager.waitAndGetPackageCompilation(sourcePath)
                .get().diagnosticResult();
        long noModuleGeneratedCount = diagnosticResult.diagnostics().stream()
                .filter(diagnostic -> DiagnosticErrorCode.NO_MODULE_GENERATED_FOR_CLIENT_DECL.diagnosticId()
                        .equals(diagnostic.diagnosticInfo().code()))
                .count();
        Assert.assertEquals(noModuleGeneratedCount, 1);

        List<Object> args = new ArrayList<>();
        args.add(CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, sourcePath.toUri().toString()));
        JsonObject response = getCommandResponse(args, GenerateModuleForClientDeclExecutor.COMMAND);
        Assert.assertNotNull(response);

        Thread.sleep(20 * 1000);

        diagnosticResult = workspaceManager.waitAndGetPackageCompilation(sourcePath)
                .get().diagnosticResult();
        noModuleGeneratedCount = diagnosticResult.diagnostics().stream()
                .filter(diagnostic -> DiagnosticErrorCode.NO_MODULE_GENERATED_FOR_CLIENT_DECL.diagnosticId()
                        .equals(diagnostic.diagnosticInfo().code()))
                .count();
        //Todo:Diagnostic does not clear
        Assert.assertEquals(noModuleGeneratedCount, 0);

        TestUtil.closeDocument(serviceEndpoint, sourcePath);
    }

    @AfterClass
    public void shutdown() {
        languageServer.shutdown();
        System.setProperty("LANG_REPO_BUILD", "true");
    }

    private JsonObject getCommandResponse(List<Object> args, String command) {
        List argsList = argsToJson(args);
        ExecuteCommandParams params = new ExecuteCommandParams(command, argsList);
        String response = TestUtil.getExecuteCommandResponse(params, this.serviceEndpoint)
                .replace("\\r\\n", "\\n");
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }

    private List argsToJson(List<Object> args) {
        List<JsonObject> jsonArgs = new ArrayList<>();
        for (Object arg : args) {
            jsonArgs.add((JsonObject) gson.toJsonTree(arg));
        }
        return jsonArgs;
    }
}
