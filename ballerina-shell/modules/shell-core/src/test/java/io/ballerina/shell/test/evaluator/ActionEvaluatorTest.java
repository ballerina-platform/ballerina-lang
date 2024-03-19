/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.shell.test.evaluator;

import io.ballerina.shell.exceptions.BallerinaShellException;
import org.testng.annotations.Test;

/**
 * Test simple snippets with action invocations.
 *
 * @since 2201.9.0
 */
public class ActionEvaluatorTest extends AbstractEvaluatorTest {
    private static final String START_EVALUATOR_TESTCASE = "testcases/evaluator/actions.start.json";
    private static final String REMOTE_EVALUATOR_TESTCASE = "testcases/evaluator/actions.remote.json";
    private static final String RESOURCE_EVALUATOR_TESTCASE = "testcases/evaluator/actions.resource.json";
    private static final String VAR_EVALUATOR_TESTCASE = "testcases/evaluator/actions.var.json";

    @Test
    public void testEvaluateStart() throws BallerinaShellException {
        testEvaluate(START_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateRemote() throws BallerinaShellException {
        testEvaluate(REMOTE_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateResource() throws BallerinaShellException {
        testEvaluate(RESOURCE_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateVar() throws BallerinaShellException {
        testEvaluate(VAR_EVALUATOR_TESTCASE);
    }
}
