/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.testerina.test.evaluation;

import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class for after hook functions in evaluation tests.
 * Tests after function execution with various configurations.
 */
public class AfterHookTest extends BaseEvaluationTest {

    /**
     * Provides test data for after hook function execution scenarios.
     */
    @DataProvider(name = "afterHookTests")
    public Object[][] afterHookTestsDataProvider() {
        return new Object[][]{
                {"testNonIsolatedEval"},
                {"testIsolatedEval"},
                {"testIsolatedEvalWithDataProvider"},
                {"testNonIsolatedEvalWithDataProvider"}
        };
    }

    @Test(dataProvider = "afterHookTests",
            description = "Test that after hook function executes after each evaluation iteration")
    public void testAfterHookExecution(String testName)
            throws BallerinaTestException, IOException {
        runTestAndVerify(testName, "evaluation-after-hook");
    }

    /**
     * Provides test data for after function failure scenarios.
     */
    @DataProvider(name = "afterHookFailureTests")
    public Object[][] afterHookFailureTestsDataProvider() {
        return new Object[][]{
                {"testIsolatedEvalAfterFunctionFailure"},
                {"testNonIsolatedEvalAfterFunctionFailure"},
                {"testIsolatedEvalAfterFunctionFailureWithDataProvider"},
                {"testNonIsolatedEvalAfterFunctionFailureWithDataProvider"}
        };
    }

    @Test(dataProvider = "afterHookFailureTests",
            description = "Test that after hook function failure is reported appropriately")
    public void testAfterHookFailure(String testName)
            throws BallerinaTestException, IOException {
        runTestAndVerify(testName, "evaluation-after-hook");
    }
}
