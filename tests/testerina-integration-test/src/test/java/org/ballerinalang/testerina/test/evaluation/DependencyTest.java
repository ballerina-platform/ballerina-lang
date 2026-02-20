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
 * Test class for evaluation test dependencies.
 * Tests that dependent evaluations are skipped when parent evaluation fails.
 */
public class DependencyTest extends BaseEvaluationTest {

    @Test(description = "Test that dependent evaluations are skipped when dependency fails")
    public void testDependentEvaluationsSkipped() throws BallerinaTestException, IOException {
        runPackageTestAndVerify("evaluation-dependencies");
    }
}
