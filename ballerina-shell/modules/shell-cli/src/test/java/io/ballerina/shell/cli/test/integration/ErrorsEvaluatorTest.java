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

package io.ballerina.shell.cli.test.integration;

import org.testng.annotations.Test;

/**
 * Test simple snippets.
 *
 * @since 2.0.0
 */
public class ErrorsEvaluatorTest extends AbstractIntegrationTest {
    private static final String CHECK_EVALUATOR_TESTCASE = "testcases/errors.check.json";
    private static final String CHECKPANIC_EVALUATOR_TESTCASE = "testcases/errors.checkpanic.json";
    private static final String TRAP_EVALUATOR_TESTCASE = "testcases/errors.trap.json";

    @Test
    public void testEvaluateCheck() throws Exception {
        test(CHECK_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateCheckPanic() throws Exception {
        test(CHECKPANIC_EVALUATOR_TESTCASE);
    }

    @Test
    public void testEvaluateTrap() throws Exception {
        test(TRAP_EVALUATOR_TESTCASE);
    }
}
