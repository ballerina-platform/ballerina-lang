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
package io.ballerina.shell.service.test.getresult;

import io.ballerina.shell.service.BalShellGetResultRequest;
import io.ballerina.shell.service.BalShellGetResultResponse;
import io.ballerina.shell.service.test.AbstractShellServiceTest;
import io.ballerina.shell.service.test.TestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Base class for getResult End point tests.
 *
 * @since 2201.1.1
 */
public abstract class AbstractGetResultTest extends AbstractShellServiceTest {
    private static final String GET_RESULT = "balShell/getResult";
    private static final Path GET_RESULT_RES_DIR = RES_DIR.resolve("testcases").resolve("getResult");

    protected void runGetResultTest(String filename) throws ExecutionException, IOException, InterruptedException {
        Path file = GET_RESULT_RES_DIR.resolve(filename);
        GetResultTestCase[] testCases = TestUtils.loadResultTestCases(file);
        for (GetResultTestCase testCase: testCases) {
            BalShellGetResultRequest request = new BalShellGetResultRequest(testCase.getSource());
            CompletableFuture<?> result = serviceEndpoint.request(GET_RESULT, request);
            BalShellGetResultResponse generatedResult  = (BalShellGetResultResponse) result.get();
            TestUtils.assertJsonValues(generatedResult, testCase.getResult());
        }
    }
}
