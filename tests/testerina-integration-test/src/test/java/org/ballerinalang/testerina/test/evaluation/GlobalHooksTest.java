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
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class for global lifecycle hooks (@BeforeEach and @AfterEach) in evaluations.
 * Tests that global hooks execute correctly with evaluation iterations.
 */
public class GlobalHooksTest extends BaseEvaluationTest {

    @Test(description = "Test that evaluation executes correctly with @BeforeEach hook")
    public void testEvalWithBeforeEachHook() throws BallerinaTestException, IOException {
        runPackageTestAndVerify("evaluation-before-each-hook");
    }

    @Test(description = "Test that evaluation executes correctly with @AfterEach hook")
    public void testEvalWithAfterEachHook() throws BallerinaTestException, IOException {
        runPackageTestAndVerify("evaluation-after-each-hook");
    }
}
