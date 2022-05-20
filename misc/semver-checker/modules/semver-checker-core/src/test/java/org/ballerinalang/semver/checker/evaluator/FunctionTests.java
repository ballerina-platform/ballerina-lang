/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.semver.checker.evaluator;

import org.testng.annotations.Test;

/**
 * Extended FunctionTest class for test each test case scenarios separately.
 * @since 2201.2.0
 */
public class FunctionTests extends BaseFunctionTest {
    private static final String FUNCTION_TEST_DATA_ROOT  = "src/test/resources/testcases/functionDefinition";
    private static final String FUNCTION_ANNOTATION_TESTCASE = FUNCTION_TEST_DATA_ROOT  + "/annotation.json";
    private static final String FUNCTION_DOCUMENTATION_TESTCASE = FUNCTION_TEST_DATA_ROOT  +  "/documentation.json";
    private static final String FUNCTION_BODY_TESTCASE = FUNCTION_TEST_DATA_ROOT  + "/functionBody.json";
    private static final String FUNCTION_IDENTIFIER_TESTCASE = FUNCTION_TEST_DATA_ROOT  + "/identifier.json";
    private static final String FUNCTION_PARAMETER_TESTCASE = FUNCTION_TEST_DATA_ROOT  + "/parameter.json";
    private static final String FUNCTION_QUALIFIER_TESTCASE = FUNCTION_TEST_DATA_ROOT  + "/qualifier.json";
    private static final String FUNCTION_RETURN_TYPE_TESTCASE = FUNCTION_TEST_DATA_ROOT  + "/returnType.json";
    private static final String ADVANCE_FUNCTION_TESTCASE = FUNCTION_TEST_DATA_ROOT  + "/advanceFunction.json";

    @Test
    public void testFunctionAnnotation() throws Exception {
        testEvaluate(FUNCTION_ANNOTATION_TESTCASE );
    }

    @Test
    public void testFunctionDocumentation() throws Exception {
        testEvaluate(FUNCTION_DOCUMENTATION_TESTCASE );
    }

    @Test
    public void testFunctionBody() throws Exception {
        testEvaluate(FUNCTION_BODY_TESTCASE );
    }

    @Test
    public void testFunctionIdentifier() throws Exception {
        testEvaluate(FUNCTION_IDENTIFIER_TESTCASE );
    }

    @Test
    public void testFunctionParameter() throws Exception {
        testEvaluate(FUNCTION_PARAMETER_TESTCASE );
    }

    @Test
    public void testFunctionQualifier() throws Exception {
        testEvaluate(FUNCTION_QUALIFIER_TESTCASE );
    }

    @Test
    public void testFunctionReturnType() throws Exception {
        testEvaluate(FUNCTION_RETURN_TYPE_TESTCASE );
    }

    @Test
    public void testAdvanceFunction() throws Exception {
        testEvaluate(ADVANCE_FUNCTION_TESTCASE );
    }
}
