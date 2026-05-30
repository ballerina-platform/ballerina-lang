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
 * Test class for evaluation failure scenarios.
 * Verifies correct failure detection and reporting during evaluation.
 */
public class FailureReportTest extends BaseEvaluationTest {

    private static final String PACKAGE_NAME = "evaluation-failures";

    /**
     * Provides test data for low pass rate failure scenarios.
     */
    @DataProvider(name = "lowPassRateFailureTests")
    public Object[][] lowPassRateFailureTestsDataProvider() {
        return new Object[][]{
                {"testIsolatedEvalFailureWithLowPassRate"},
                {"testNonIsolatedEvalFailureWithLowPassRate"},
                {"testIsolatedEvalFailureWithLowPassRateWithDataProvider"},
                {"testNonIsolatedEvalFailureWithLowPassRateWithDataProvider"}
        };
    }

    @Test(dataProvider = "lowPassRateFailureTests",
            description = "Test evaluation fails when minPassRate threshold is not met")
    public void testEvalFailsOnLowPassRate(String testName) throws BallerinaTestException, IOException {
        runTestAndVerify(testName, PACKAGE_NAME);
    }

    /**
     * Provides test data for invalid evaluation input failure scenarios.
     */
    @DataProvider(name = "invalidInputFailureTests")
    public Object[][] invalidInputFailureTestsDataProvider() {
        return new Object[][]{
                {"testIsolatedEvalFailureForInvalidInput"},
                {"testNonIsolatedEvalFailureForInvalidInput"},
                {"testIsolatedEvalFailureForInvalidInputWithDataProvider"},
                {"testNonIsolatedEvalFailureForInvalidInputWithDataProvider"},
                {"testIsolatedEvalFailureForEmptyDataProvider"},
                {"testNonIsolatedEvalFailureForEmptyDataProvider"},
                {"testIsolatedEvalFailureForNonReadOnlyDataEntry"},
                {"testNonIsolatedEvalFailureForNonReadOnlyDataEntry"},
        };
    }

    @Test(dataProvider = "invalidInputFailureTests",
            description = "Evaluation fails when invalid input is provided")
    public void testInvalidInputFailure(String testName) throws BallerinaTestException, IOException {
        runTestAndVerify(testName, PACKAGE_NAME);
    }

    /**
     * Provides test data for scenarios where the evaluation logic returns an error.
     */
    @DataProvider(name = "evaluationReturningErrorTests")
    public Object[][] evaluationReturningErrorTestsDataProvider() {
        return new Object[][]{
                {"testIsolatedEvalReturningError"},
                {"testNonIsolatedEvalReturningError"},
                {"testIsolatedEvalReturningErrorWithDataProvider"},
                {"testNonIsolatedEvalReturningErrorWithDataProvider"}
        };
    }

    @Test(dataProvider = "evaluationReturningErrorTests",
            description = "Tests scenarios where the evaluation logic returns an error")
    public void evaluationReturningErrorTests(String testName) throws BallerinaTestException, IOException {
        runTestAndVerify(testName, PACKAGE_NAME);
    }
}
