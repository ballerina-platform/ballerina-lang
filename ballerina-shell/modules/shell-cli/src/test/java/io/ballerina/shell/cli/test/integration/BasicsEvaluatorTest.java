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
public class BasicsEvaluatorTest extends AbstractIntegrationTest {
    private static final String BASICS_MODULES_TESTCASE = "testcases/basics.modules.json";
    private static final String BASICS_VARIABLES_TESTCASE = "testcases/basics.variables.json";
    private static final String BASICS_VAR_TESTCASE = "testcases/basics.var.json";
    private static final String BASICS_FUNCTIONS_TESTCASE = "testcases/basics.functions.json";
    private static final String BASICS_REQ_PARAMS_TESTCASE = "testcases/basics.params.req.json";

    @Test
    public void testBasicsModules() throws Exception {
        test(BASICS_MODULES_TESTCASE);
    }

    @Test
    public void testBasicsVariables() throws Exception {
        test(BASICS_VARIABLES_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsVar() throws Exception {
        test(BASICS_VAR_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsFunctions() throws Exception {
        test(BASICS_FUNCTIONS_TESTCASE);
    }

    @Test
    public void testEvaluateBasicsReqParams() throws Exception {
        test(BASICS_REQ_PARAMS_TESTCASE);
    }
}
