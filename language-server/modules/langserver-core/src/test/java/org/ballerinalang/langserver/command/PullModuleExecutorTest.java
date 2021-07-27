/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.internal.environment.BallerinaDistribution;
import io.ballerina.projects.internal.repositories.BallerinaDistributionRepository;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.command.executors.PullModuleExecutor;
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
import java.util.Map;

/**
 * Test to cover pull module code action execution.
 */
public class PullModuleExecutorTest {

    private BallerinaLanguageServer languageServer;
    private LanguageServerContext serverContext;
    private WorkspaceManager workspaceManager;

    private Endpoint serviceEndpoint;

    private final Gson gson = new Gson();
    private final JsonParser parser = new JsonParser();

    private final Path sourcesDir = new File(getClass().getClassLoader()
            .getResource("command").getFile()).toPath();

    @BeforeClass
    public void init() {
        System.clearProperty("LANG_REPO_BUILD");
        this.languageServer = new BallerinaLanguageServer();
        serverContext = languageServer.getServerContext();
        workspaceManager = languageServer.getWorkspaceManager();

        this.serviceEndpoint = TestUtil.initializeLanguageSever(languageServer);
    }

    /**
     * TODO This is disabled because there's no way to gracefully clear pulled packages at the end of the test.
     *          Will be revisited later
     */
    @Test(enabled = false)
    public void testPullModule() throws IOException, InterruptedException {
        Path sourcePath = sourcesDir.resolve("pull-module").resolve("source").resolve("pull_module_source1.bal");
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        BallerinaDistribution distribution = BallerinaDistribution.from(EnvironmentBuilder.buildDefault());
        BallerinaDistributionRepository ballerinaDistributionRepository = distribution.packageRepository();
        Map<String, List<String>> packages = ballerinaDistributionRepository.getPackages();
        if (packages.containsKey("lstest")) {
            packages.get("lstest").stream()
                    .filter(packageName -> packageName.contains("pullmodule.sample"))
                    .findAny()
                    .ifPresent(p -> Assert.fail("Package already exists: " + p));
        }

        // Check for unresolved module diagnostic
        DiagnosticResult diagnosticResult = workspaceManager.waitAndGetPackageCompilation(sourcePath)
                .get().diagnosticResult();
        long unresolvedModCount = diagnosticResult.diagnostics().stream()
                .filter(diagnostic -> DiagnosticErrorCode.MODULE_NOT_FOUND.diagnosticId()
                        .equals(diagnostic.diagnosticInfo().code()))
                .count();
        Assert.assertEquals(unresolvedModCount, 1);

        List<Object> args = new ArrayList<>();
        args.add(CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, sourcePath.toUri().toString()));

        JsonObject response = getCommandResponse(args, PullModuleExecutor.COMMAND);
        Assert.assertNotNull(response);
        
        Thread.sleep(60 * 1000);

        diagnosticResult = workspaceManager.waitAndGetPackageCompilation(sourcePath).get().diagnosticResult();
        unresolvedModCount = diagnosticResult.diagnostics().stream()
                .filter(diagnostic -> DiagnosticErrorCode.MODULE_NOT_FOUND.diagnosticId()
                        .equals(diagnostic.diagnosticInfo().code()))
                .count();
        Assert.assertEquals(unresolvedModCount, 0);

        TestUtil.closeDocument(serviceEndpoint, sourcePath);
    }

    @AfterClass
    public void teardown() {
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
