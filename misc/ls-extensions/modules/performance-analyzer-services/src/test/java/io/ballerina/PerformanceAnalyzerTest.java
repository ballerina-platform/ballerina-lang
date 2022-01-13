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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.javacrumbs.jsonunit.core.Option;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.ballerina.Constants.MESSAGE;
import static io.ballerina.Constants.SUCCESS;
import static io.ballerina.Constants.TYPE;
import static io.ballerina.PerformanceAnalyzerNodeVisitor.ACTION_INVOCATION_KEY;
import static io.ballerina.PerformanceAnalyzerNodeVisitor.ENDPOINTS_KEY;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

/**
 * Tests for Performance Analyzer.
 */
public class PerformanceAnalyzerTest {

    private static final String BALLERINA = "ballerina";
    private static final String RESULT = "result";
    private static final String PERFORMANCE_ANALYZE = "performanceAnalyzer/getResourcesWithEndpoints";
    private static final Path RES_DIR = Paths.get("src", "test", "resources").toAbsolutePath();

    @Test(description = "Test performance analyzer")
    public void testFunction() throws IOException, ExecutionException, InterruptedException {

        compare("main.bal", "main.json");
    }

    @Test(description = "Test performance analyzer")
    public void testIfElse() throws IOException, ExecutionException, InterruptedException {

        compare("ifElse.bal", "ifElse.json");
    }

    @Test(description = "Test performance analyzer")
    public void testNoData() throws IOException, ExecutionException, InterruptedException {

        compare("noData.bal", "noData.json");
    }

    @Test(description = "Test performance analyzer")
    public void testForEach() throws IOException, ExecutionException, InterruptedException {

        compare("forEach.bal", "forEach.json");
    }

    @Test(description = "Test performance analyzer")
    public void testWhile() throws IOException, ExecutionException, InterruptedException {

        compare("while.bal", "while.json");
    }

    private void compare(String balFile, String jsonFile) throws IOException, InterruptedException,
            ExecutionException {

        Path project = RES_DIR.resolve(BALLERINA).resolve(balFile);
        Path resultJson = RES_DIR.resolve(RESULT).resolve(jsonFile);

        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, project);

        PerformanceAnalyzerRequest request = new PerformanceAnalyzerRequest();
        request.setDocumentIdentifier(new TextDocumentIdentifier(project.toString()));

        CompletableFuture<?> result = serviceEndpoint.request(PERFORMANCE_ANALYZE, request);
        List<PerformanceAnalyzerResponse> endpoints = (List<PerformanceAnalyzerResponse>) result.get();
        PerformanceAnalyzerResponse endpoint = endpoints.get(0);

        BufferedReader br = new BufferedReader(new FileReader(resultJson.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();

        Assert.assertEquals(endpoint.getType(), expected.get(TYPE).getAsString());
        Assert.assertEquals(endpoint.getMessage(), expected.get(MESSAGE).getAsString());

        if (endpoint.getType().equals(SUCCESS)) {
            JsonObject actionInvocations = endpoint.getActionInvocations();
            JsonObject expectedActionInvocations = expected.getAsJsonObject(ACTION_INVOCATION_KEY);

            assertThatJson(actionInvocations.toString()).isEqualTo(expectedActionInvocations.toString());
            validateEndpoints(endpoint.getEndpoints(), expected);
        }
    }

    private void validateEndpoints(JsonObject endpoints, JsonObject expected) {

        JsonObject expectedEndpoints = expected.getAsJsonObject(ENDPOINTS_KEY);

        Assert.assertEquals(endpoints.size(), expectedEndpoints.size());
        String[] endpointsKeys = endpoints.keySet().toArray(new String[endpoints.size()]);
        String[] expectedEndpointsKeys = expectedEndpoints.keySet().toArray(new String[expectedEndpoints.size()]);

        JsonArray endpointsArr = new JsonArray();
        JsonArray expectedEndpointsArr = new JsonArray();
        for (int i = 0; i < expectedEndpointsKeys.length; i++) {
            JsonObject endpoint = endpoints.getAsJsonObject(endpointsKeys[i]);
            endpointsArr.add(endpoint);
            JsonObject expectedEndpoint = expectedEndpoints.getAsJsonObject(expectedEndpointsKeys[i]);
            expectedEndpointsArr.add(expectedEndpoint);

        }
        assertThatJson(endpointsArr.toString())
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedEndpointsArr.toString());
    }
}
