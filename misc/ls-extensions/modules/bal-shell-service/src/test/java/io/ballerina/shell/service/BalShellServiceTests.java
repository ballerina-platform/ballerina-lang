/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.shell.service;

import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tests for BalShellService.
 *
 * @since 2.0.0
 */
public class BalShellServiceTests {
    private static final String GET_RESULT = "balShell/getResult";
    private static final String NOTEBOOK_RESTART = "balShell/restartNotebook";
    private static final String GET_VARIABLES = "balShell/getVariableValues";
    private static final String DELETE_DCLNS = "balShell/deleteDeclarations";
    private static final String GET_SHELL_FILE_SOURCE = "balShell/getShellFileSource";
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    private Endpoint serviceEndpoint;

    @BeforeClass
    public void startLanguageServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test with simple arithmetic")
    public void testSimpleArithmetic() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("simple_arithmetic.json");
    }

    @Test(description = "Test with variable definitions")
    public void testBasicVariables() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("basic_variables.json");
    }

    @Test(description = "Test with function definitions")
    public void testFunctions() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("functions.json");
    }

    @Test(description = "Test with mime types")
    public void testMimeTypes() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("mime_types.json");
    }

    @Test(description = "Test with errors")
    public void testErrors() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("errors.json");
    }

    @Test(description = "Test for get bal shell file source")
    public void getShellFileSource() throws ExecutionException, InterruptedException {
        CompletableFuture<?> result = serviceEndpoint.request(GET_SHELL_FILE_SOURCE, null);
        ShellFileSourceResponse generatedResult  = (ShellFileSourceResponse) result.get();
        Assert.assertNotNull(generatedResult.getFilePath());
    }

    @Test(description = "Test for getVariables")
    public void testGetVariables() throws IOException {
        Path file = RES_DIR.resolve("testcases").resolve("get_variables.json");
        GetVariableTestCase[] testCases = TestUtils.loadVariableTestCases(file);
        serviceEndpoint.request(NOTEBOOK_RESTART, null); // restart the notebook
        for (GetVariableTestCase testCase: testCases) {
            BalShellGetResultRequest request = new BalShellGetResultRequest(testCase.getSource());
            CompletableFuture<?> snippetExecute = serviceEndpoint.request(GET_RESULT, request); // execute the snippets
            snippetExecute.thenRun(() -> {
                CompletableFuture<?> result = serviceEndpoint.request(GET_VARIABLES, null);
                List<Map<String, String>> generatedResult;
                try {
                    generatedResult = (List<Map<String, String>>) result.get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
                TestUtils.assertJsonValues(generatedResult, testCase.getResult());
            });
        }
    }

    @Test(description = "Test for notebookRestart")
    public void testRestart() throws ExecutionException, InterruptedException {
        CompletableFuture<?> result = serviceEndpoint.request(NOTEBOOK_RESTART, null);
        boolean generatedResult  = (boolean) result.get();
        Assert.assertTrue(generatedResult);
    }

    @Test(description = "Test for definition delete")
    public void testDelete() throws ExecutionException, InterruptedException, IOException {
        // check for variable declarations
        runDeleteDclnsTest("basic_variables.json");
        // check for module declarations
        runDeleteDclnsTest("functions.json");
    }

    private void runDeleteDclnsTest(String filename) throws IOException, InterruptedException, ExecutionException {
        Path file = RES_DIR.resolve("testcases").resolve(filename);
        GetResultTestCase[] testCases = TestUtils.loadResultTestCases(file);
        for (GetResultTestCase testCase: testCases) {
            BalShellGetResultRequest request = new BalShellGetResultRequest(testCase.getSource());
            CompletableFuture<BalShellGetResultResponse> balShellResponse =
                    (CompletableFuture<BalShellGetResultResponse>) serviceEndpoint.request(GET_RESULT, request);
            MetaInfo metainfo = balShellResponse.get().getMetaInfo();
            CompletableFuture<?> result = serviceEndpoint.request(DELETE_DCLNS, metainfo);
            boolean generatedResult = (boolean) result.get();
            Assert.assertTrue(generatedResult);
        }
    }

    private void runGetResultTest(String filename) throws ExecutionException, IOException, InterruptedException {
        Path file = RES_DIR.resolve("testcases").resolve(filename);
        GetResultTestCase[] testCases = TestUtils.loadResultTestCases(file);
        for (GetResultTestCase testCase: testCases) {
            BalShellGetResultRequest request = new BalShellGetResultRequest(testCase.getSource());
            CompletableFuture<?> result = serviceEndpoint.request(GET_RESULT, request);
            BalShellGetResultResponse generatedResult  = (BalShellGetResultResponse) result.get();
            TestUtils.assertJsonValues(generatedResult, testCase.getResult());
        }
    }
}
