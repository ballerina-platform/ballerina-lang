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
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

/**
 * Tests for Performance Analyzer.
 */
public class PerformanceAnalyzerTest {

    private static final String PERFORMANCE_ANALYZE = "performanceAnalyzer/getEndpoints";
    private static final Path RES_DIR = Paths.get("src", "test", "resources").toAbsolutePath();

    @Test(description = "Test performance analyzer")
    public void testFunction() throws IOException, ExecutionException, InterruptedException {

        Path project = RES_DIR.resolve("ballerina").resolve("main.bal");
        Path resultJson = RES_DIR.resolve("result").resolve("main.json");

        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, project);

        PerformanceAnalyzerGraphRequest request = new PerformanceAnalyzerGraphRequest();
        request.setDocumentIdentifier(new TextDocumentIdentifier(project.toString()));
        request.setRange(new Range(new Position(21, 4), new Position(28, 5)));

        CompletableFuture<?> result = serviceEndpoint.request(PERFORMANCE_ANALYZE, request);
        JsonObject json = (JsonObject) result.get();
        JsonObject actionInvocations = json.getAsJsonObject("actionInvocations");

        BufferedReader br = new BufferedReader(new FileReader(resultJson.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        JsonObject expectedActionInvocations = expected.getAsJsonObject("actionInvocations");

        assertThatJson(actionInvocations.toString()).isEqualTo(expectedActionInvocations.toString());
        validateEndpoints(json, expected);
    }

    @Test(description = "Test performance analyzer")
    public void testIfElse() throws IOException, ExecutionException, InterruptedException {

        Path project = RES_DIR.resolve("ballerina").resolve("ifElse.bal");
        Path resultJson = RES_DIR.resolve("result").resolve("ifElse.json");

        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, project);

        PerformanceAnalyzerGraphRequest request = new PerformanceAnalyzerGraphRequest();
        request.setDocumentIdentifier(new TextDocumentIdentifier(project.toString()));
        request.setRange(new Range(new Position(20, 4), new Position(30, 5)));

        CompletableFuture<?> result = serviceEndpoint.request(PERFORMANCE_ANALYZE, request);
        JsonObject json = (JsonObject) result.get();
        JsonObject actionInvocations = json.getAsJsonObject("actionInvocations");

        BufferedReader br = new BufferedReader(new FileReader(resultJson.toAbsolutePath().toString()));
        JsonObject expected = JsonParser.parseReader(br).getAsJsonObject();
        JsonObject expectedActionInvocations = expected.getAsJsonObject("actionInvocations");

        assertThatJson(actionInvocations.toString()).isEqualTo(expectedActionInvocations.toString());
        validateEndpoints(json, expected);
    }

    private void validateEndpoints(JsonObject json, JsonObject expected) {

        JsonObject endpoints = json.getAsJsonObject("endpoints");
        JsonObject expectedEndpoints = expected.getAsJsonObject("endpoints");

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
