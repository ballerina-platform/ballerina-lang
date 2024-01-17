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
package io.ballerina.shell.service.test;

import io.ballerina.shell.service.BalShellGetResultRequest;
import io.ballerina.shell.service.BalShellGetResultResponse;
import io.ballerina.shell.service.DeleteRequest;
import io.ballerina.shell.service.MetaInfo;
import io.ballerina.shell.service.ShellFileSourceResponse;
import io.ballerina.shell.service.test.getresult.GetResultTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tests for BalShellService.
 *
 * @since 2201.1.1
 */
public class BalShellServiceTests extends AbstractShellServiceTest {
    @Test(description = "Test for get bal shell file source")
    public void testGetShellFileSource() throws ExecutionException, InterruptedException {
        CompletableFuture<?> result = serviceEndpoint.request(GET_SHELL_FILE_SOURCE, null);
        ShellFileSourceResponse generatedResult  = (ShellFileSourceResponse) result.get();
        Assert.assertNotNull(generatedResult.getFilePath());
    }

    @Test(description = "Test for getVariables")
    public void testGetVariables() throws IOException {
        Path file = RES_DIR.resolve("testcases").resolve("get.variables.json");
        GetVariableTestCase[] testCases = TestUtils.loadVariableTestCases(file);
        for (GetVariableTestCase testCase: testCases) {
            BalShellGetResultRequest request = new BalShellGetResultRequest(testCase.getSource());
            CompletableFuture<?> snippetExecute = serviceEndpoint.request(GET_RESULT, request).thenRun(() -> {
                CompletableFuture<?> result = serviceEndpoint.request(GET_VARIABLES, null);
                List<Map<String, String>> generatedResult;
                try {
                    generatedResult = (List<Map<String, String>>) result.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                List<Map<String, String>> expected = testCase.getResult();
                generatedResult.forEach(genResult -> Assert.assertTrue(expected.contains(genResult)));
            });
            snippetExecute.join();
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
        String filename = "vars.to.delete.json";
        // define module Declarations and variables to test
        List<String> varsToTestDelete = new ArrayList<>();
        Path file = RES_DIR.resolve("testcases").resolve(filename);
        GetResultTestCase[] testCases = TestUtils.loadResultTestCases(file);
        for (GetResultTestCase testCase: testCases) {
            BalShellGetResultRequest request = new BalShellGetResultRequest(testCase.getSource());
            CompletableFuture<BalShellGetResultResponse> balShellResponse =
                    (CompletableFuture<BalShellGetResultResponse>) serviceEndpoint.request(GET_RESULT, request);
            MetaInfo metainfo = balShellResponse.get().getMetaInfo();
            varsToTestDelete.addAll(metainfo.getDefinedVars());
            varsToTestDelete.addAll(metainfo.getModuleDclns());
        }

        // invoke the end point
        DeleteRequest request = new DeleteRequest(varsToTestDelete.get(0));
        CompletableFuture<?> passed = serviceEndpoint.request(DELETE_DCLNS, request);
        boolean passedBool = (boolean) passed.get();
        Assert.assertTrue(passedBool);
        passed.thenRun(() -> {
            // test for values not in the memory anymore
            CompletableFuture<?> failed = serviceEndpoint.request(DELETE_DCLNS, request);
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
            for (String definedVarToTest: varsToTestDelete) {
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
