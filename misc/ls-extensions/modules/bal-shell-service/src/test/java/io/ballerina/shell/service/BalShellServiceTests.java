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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tests for BalShellService.
 *
 * @since 2201.1.1
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

    @BeforeTest
    public void restartShell() {
        ShellWrapper.getInstance().restart();
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
        for (GetVariableTestCase testCase: testCases) {
            BalShellGetResultRequest request = new BalShellGetResultRequest(testCase.getSource());
            CompletableFuture<?> snippetExecute = serviceEndpoint.request(GET_RESULT, request); // execute the snippets
            snippetExecute.thenRun(() -> {
                CompletableFuture<?> result = serviceEndpoint.request(GET_VARIABLES, null);
                List<Map<String, String>> generatedResult;
                try {
                    generatedResult = (List<Map<String, String>>) result.get();
                } catch (InterruptedException | ExecutionException e) {
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
        String filename = "delete_vars.json";
        // define module Declarations and variables to test
        List<String> definedVars = new ArrayList<>();
        List<String> moduleDclns = new ArrayList<>();
        Path file = RES_DIR.resolve("testcases").resolve(filename);
        GetResultTestCase[] testCases = TestUtils.loadResultTestCases(file);
        for (GetResultTestCase testCase: testCases) {
            BalShellGetResultRequest request = new BalShellGetResultRequest(testCase.getSource());
            CompletableFuture<BalShellGetResultResponse> balShellResponse =
                    (CompletableFuture<BalShellGetResultResponse>) serviceEndpoint.request(GET_RESULT, request);
            MetaInfo metainfo = balShellResponse.get().getMetaInfo();
            definedVars.addAll(metainfo.getDefinedVars());
            moduleDclns.addAll(metainfo.getModuleDclns());
        }

        // get random value set from each to test
        Random random = new Random();
        List<String> definedVarsToTest = TestUtils.getRandomSlice(definedVars, random.nextInt(definedVars.size()));
        List<String> moduleDclnsToTest = TestUtils.getRandomSlice(moduleDclns, random.nextInt(moduleDclns.size()));
        MetaInfo metaInfo = new MetaInfo(definedVarsToTest, moduleDclnsToTest);
        // invoke the end point
        CompletableFuture<?> passed = serviceEndpoint.request(DELETE_DCLNS, metaInfo);
        boolean passedBool = (boolean) passed.get();
        Assert.assertTrue(passedBool);
        passed.thenRun(() -> {
            // test for values not in the memory anymore
            CompletableFuture<?> failed = serviceEndpoint.request(DELETE_DCLNS, metaInfo);
            // check those values are not in invoker memory
            CompletableFuture<?> result = serviceEndpoint.request(GET_VARIABLES, null);
            List<String> currentVars = new ArrayList<>();
            try {
                boolean failedBool = (boolean) failed.get();
                Assert.assertFalse(failedBool);
                List<Map<String, String>> varMapList = (List<Map<String, String>>) result.get();
                for (Map<String, String> varMap: varMapList) {
                    currentVars.add(varMap.get("name"));
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            for (String definedVarToTest: definedVarsToTest) {
                // deleted vars should not contain in the current var list
                Assert.assertFalse(currentVars.contains(definedVarToTest));
            }
        });
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
