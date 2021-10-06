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

package io.ballerina;

import com.google.gson.JsonParser;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.util.TestUtil;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tests for Performance Analyzer.
 */
public class PerformanceAnalyzerTest {

    private static final String PERFORMANCE_ANALYZE = "performanceAnalyzer/getEndpoints";
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final Path project = RES_DIR.resolve("ballerina")
            .resolve("main.bal");
    private static final Path resultJson = RES_DIR.resolve("result")
            .resolve("result.json");
    private static final LanguageServerContext serverContext = new LanguageServerContextImpl();
    private static final WorkspaceManager workspaceManager
            = BallerinaWorkspaceManager.getInstance(serverContext);
    private static final JsonParser parser = new JsonParser();

    @Test(description = "Test performance analyzer")
    public void testPerformanceAnalyzer() throws IOException, ExecutionException, InterruptedException {

        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, project);

        BallerinaProjectParams projectParams = new BallerinaProjectParams();
        projectParams.setDocumentIdentifier(new TextDocumentIdentifier(project.toString()));
        CompletableFuture<?> result = serviceEndpoint.request(PERFORMANCE_ANALYZE, projectParams);
        String json = getResult(result);

        String expectedJson = Files.readString(resultJson);
        Assert.assertEquals(json, expectedJson);
    }

    private static String getResult(CompletableFuture result) {

        return parser.parse(TestUtil.getResponseString(result)).getAsJsonObject().
                getAsJsonObject("result").getAsString();
    }
}
