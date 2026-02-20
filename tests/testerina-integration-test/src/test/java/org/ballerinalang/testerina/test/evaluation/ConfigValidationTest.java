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
 * Test class for evaluation configuration validation.
 * Tests invalid configuration values for confidence and iterations.
 */
public class ConfigValidationTest extends BaseEvaluationTest {

    private static final String PACKAGE_NAME = "evaluation-config";

    /**
     * Provides test data for invalid evaluation configuration scenarios.
     */
    @DataProvider(name = "invalidConfigurationTest")
    public Object[][] invalidConfigurationTestDataProvider() {
        return new Object[][]{
                {"testInvalidConfidenceValue"},
                {"testInvalidIterationValue"}
        };
    }

    @Test(dataProvider = "invalidConfigurationTest",
            description = "Test that evaluation fails when invalid configuration provided")
    public void testInvalidConfigurations(String testName) throws BallerinaTestException, IOException {
        runTestAndVerify(testName, PACKAGE_NAME);
    }
}
