/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.test.evaluator;

import io.ballerina.shell.exceptions.BallerinaShellException;
import org.testng.annotations.Test;

/**
 * Test simple snippets.
 *
 * @since 2.0.0
 */
public class MiscEvaluatorTest extends AbstractEvaluatorTest {
    private static final String ENUM_EVALUATOR_TESTCASE = "testcases/evaluator/values.enum.json";
    private static final String RAW_TEMP_EVALUATOR_TESTCASE = "testcases/evaluator/values.raw.template.json";
    private static final String LET_EVALUATOR_TESTCASE = "testcases/evaluator/values.let.json";

    @Test
    public void testEvaluateEnum() throws BallerinaShellException {
        testEvaluate(ENUM_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateRawTemplate() throws BallerinaShellException {
        testEvaluate(RAW_TEMP_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateLet() throws BallerinaShellException {
        testEvaluate(LET_EVALUATOR_TESTCASE);
    }
}
