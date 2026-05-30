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
 * Test class for before hook functions in evaluation tests.
 * Tests before function execution with various configurations.
 */
public class BeforeHookTest extends BaseEvaluationTest {

    /**
     * Provides test data for before hook function execution scenarios.
     */
    @DataProvider(name = "beforeHookTests")
    public Object[][] beforeHookTestsDataProvider() {
        return new Object[][]{
                {"testIsolatedEval"},
                {"testNonIsolatedEval"},
                {"testIsolatedEvalWithDataProvider"},
                {"testNonIsolatedEvalWithDataProvider"}
        };
    }

    @Test(dataProvider = "beforeHookTests",
            description = "Test that before hook function executes before each evaluation iteration")
    public void testBeforeHookExecution(String testName)
            throws BallerinaTestException, IOException {
        runTestAndVerify(testName, "evaluation-before-hook");
    }

    /**
     * Provides test data for before function failure scenarios.
     */
    @DataProvider(name = "beforeHookFailureTests")
    public Object[][] beforeHookFailureTestsDataProvider() {
        return new Object[][]{
                {"testIsolatedEvalBeforeFunctionFailure"},
                {"testNonIsolatedEvalBeforeFunctionFailure"},
                {"testIsolatedEvalBeforeFunctionFailureWithDataProvider"},
                {"testNonIsolatedEvalBeforeFunctionFailureWithDataProvider"}
        };
    }

    @Test(dataProvider = "beforeHookFailureTests",
            description = "Test that evaluation skipped appropriately when before function fails")
    public void testBeforeHookFailure(String testName)
            throws BallerinaTestException, IOException {
        runTestAndVerify(testName, "evaluation-before-hook");
    }

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
}
