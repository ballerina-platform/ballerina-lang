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
public class BasicsEvaluatorTest extends AbstractEvaluatorTest {
    private static final String BASICS_MODULES_TESTCASE = "testcases/evaluator/basics.modules.json";
    private static final String BASICS_VARIABLES_TESTCASE = "testcases/evaluator/basics.variables.json";
    private static final String BASICS_VAR_TESTCASE = "testcases/evaluator/basics.var.json";
    private static final String BASICS_FUNCTIONS_TESTCASE = "testcases/evaluator/basics.functions.json";
    private static final String BASICS_REQ_PARAMS_TESTCASE = "testcases/evaluator/basics.params.req.json";
    private static final String BASICS_DEF_PARAMS_TESTCASE = "testcases/evaluator/basics.params.def.json";
    private static final String BASICS_REST_PARAMS_TESTCASE = "testcases/evaluator/basics.params.rest.json";
    private static final String BASICS_QUOTED_TESTCASE = "testcases/evaluator/basics.quoted.json";
    private static final String BASICS_ERRORS_TESTCASE = "testcases/evaluator/basics.errors.json";

    @Test
    public void testBasicsModules() throws BallerinaShellException {
        // TODO: Improve after custom classpath support
        testEvaluate(BASICS_MODULES_TESTCASE);
    }

    @Test
    public void testBasicsVariables() throws BallerinaShellException {
        testEvaluate(BASICS_VARIABLES_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsVar() throws BallerinaShellException {
        testEvaluate(BASICS_VAR_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsFunctions() throws BallerinaShellException {
        testEvaluate(BASICS_FUNCTIONS_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsReqParams() throws BallerinaShellException {
        testEvaluate(BASICS_REQ_PARAMS_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsDefParams() throws BallerinaShellException {
        testEvaluate(BASICS_DEF_PARAMS_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsRestParams() throws BallerinaShellException {
        testEvaluate(BASICS_REST_PARAMS_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsQuoted() throws BallerinaShellException {
        testEvaluate(BASICS_QUOTED_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsErrors() throws BallerinaShellException {
        testEvaluate(BASICS_ERRORS_TESTCASE);
    }
}
