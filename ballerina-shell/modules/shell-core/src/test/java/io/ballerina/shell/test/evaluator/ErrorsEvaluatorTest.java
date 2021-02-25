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
public class ErrorsEvaluatorTest extends AbstractEvaluatorTest {
    private static final String HANDLING_EVALUATOR_TESTCASE = "testcases/evaluator/errors.handling.json";
    private static final String FAIL_EVALUATOR_TESTCASE = "testcases/evaluator/errors.fail.json";
    private static final String CHECK_EVALUATOR_TESTCASE = "testcases/evaluator/errors.check.json";
    private static final String PANIC_EVALUATOR_TESTCASE = "testcases/evaluator/errors.panic.json";
    private static final String CHECKPANIC_EVALUATOR_TESTCASE = "testcases/evaluator/errors.checkpanic.json";
    private static final String TRAP_EVALUATOR_TESTCASE = "testcases/evaluator/errors.trap.json";
    private static final String USER_DEF_EVALUATOR_TESTCASE = "testcases/evaluator/errors.user.def.json";
    private static final String SINGLE_PLACE_EVALUATOR_TESTCASE = "testcases/evaluator/errors.single.place.json";

    @Test
    public void testEvaluateErrorHandling() throws BallerinaShellException {
        testEvaluate(HANDLING_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateFail() throws BallerinaShellException {
        testEvaluate(FAIL_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateCheck() throws BallerinaShellException {
        testEvaluate(CHECK_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluatePanic() throws BallerinaShellException {
        testEvaluate(PANIC_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateCheckPanic() throws BallerinaShellException {
        testEvaluate(CHECKPANIC_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateTrap() throws BallerinaShellException {
        testEvaluate(TRAP_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateUserDef() throws BallerinaShellException {
        testEvaluate(USER_DEF_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateSinglePlace() throws BallerinaShellException {
        // TODO: Test transaction on fail clause
        testEvaluate(SINGLE_PLACE_EVALUATOR_TESTCASE);
    }
}
