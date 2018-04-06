/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.taintchecking;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test tainted status propagation with different types of statements and expressions.
 *
 * @since 0.965.0
 */
public class TaintedStatusPropagationTest {

    @Test
    public void testReturn() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/returns.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testReturnNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/returns-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 20);
    }

    @Test
    public void testVariable() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/variables.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testVariableNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/variables-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 5, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 9, 20);
    }

    @Test
    public void testReceiver() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/receiver.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testReceiverNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/receiver-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 20);
    }

    @Test
    public void testRecord() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/record.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testRecordNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/record-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 9, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 13, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 17, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 21, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 26, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 31, 20);
        BAssertUtil.validateError(result, 6, "tainted value passed to sensitive parameter 'secureIn'", 37, 20);
        BAssertUtil.validateError(result, 7, "tainted value passed to sensitive parameter 'secureIn'", 43, 20);
    }

    @Test
    public void testArray() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/array.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testArrayNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/array-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 6);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 13, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 19, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 23, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 28, 20);
    }

    @Test
    public void testJson() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/json.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testJsonNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/json-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 12, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 16, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 21, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 26, 20);
        BAssertUtil.validateError(result, 6, "tainted value passed to sensitive parameter 'secureIn'", 32, 20);
        BAssertUtil.validateError(result, 7, "tainted value passed to sensitive parameter 'secureIn'", 38, 20);
    }

    @Test
    public void testMap() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/map.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testMapNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/map-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 12, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 16, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 21, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 26, 20);
        BAssertUtil.validateError(result, 6, "tainted value passed to sensitive parameter 'secureIn'", 32, 20);
        BAssertUtil.validateError(result, 7, "tainted value passed to sensitive parameter 'secureIn'", 38, 20);
    }

    @Test
    public void testXML() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/xml.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testXMLNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/xml-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 4);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 7, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 10, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 13, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 14, 20);
    }

    @Test
    public void testBasicWorker() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/basic-worker.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testBasicWorkerNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/basic-worker-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 24);
    }

    @Test
    public void testIfCondition() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/if-condition.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testIfConditionNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/if-condition-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 16, 20);
    }

    @Test
    public void testTernaryExpr() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/ternary.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testTernaryExprNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/ternary-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 6, 20);
    }

    @Test
    public void testLambda() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/lambda.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testTupleReturn() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/tuple-return.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testTupleReturnNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/tuple-return-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 6, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 7, 20);
    }

    @Test
    public void testStringTemplate() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/string-template.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testStringTemplateNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/string-template-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 20);
    }

    @Test
    public void testIterable() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/iterable.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testIterableNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/iterable-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 5, 53);
    }

    @Test
    public void testForEach() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/foreach.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testForEachNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/foreach-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 5, 24);
    }

    @Test
    public void testMultipleInvocationLevels() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/multiple-invocation-levels.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testMultipleInvocationLevelsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/multiple-invocation-levels-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 10, 20);
    }

    @Test
    public void testCast() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/cast.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testCastNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/cast-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 20);
    }

    @Test
    public void testChainedInvocations() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/chained-invocations.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testChainedInvocationsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/chained-invocations-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 20);
    }

    @Test
    public void testWithoutUserDataNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/without-user-data-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 7, 20);
    }

    @Test
    public void testGlobalVariables() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/global-variables.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testGlobalVariablesNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/global-variables-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to global variable 'globalVariable'", 12, 5);
    }

    @Test
    public void testServiceVariables() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/service-level-variables.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testServiceVariablesNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/service-level-variables-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to global variable 'serviceLevelVariable'", 19, 9);
        BAssertUtil.validateError(result, 1, "tainted value passed to global variable 'globalLevelVariable'", 20, 9);
    }

    @Test
    public void testHttpService() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/http-service.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 16, 24);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 17, 24);
    }

    @Test
    public void testCompoundAssignment() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/compound-assignment.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testCompoundAssignmentNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/compound-assignment-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 3);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 5, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 9, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 14, 20);
    }

    @Test
    public void testMatch() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/match.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testMatchNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/match-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 7, 28);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 12, 28);
    }

    @Test
    public void testObjectFunction() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-functions.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testObjectFunctionNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-functions-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 14, 20);
    }


    @Test
    public void testObjectExternalFunction() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-external-functions.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testObjectExternalFunctionNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-external-functions-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 16, 20);
    }

    @Test
    public void testObjectFunctionWithConstructor() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-functions-with-constructor.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testObjectFunctionWithConstructorNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-functions-with-constructor-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 20, 20);
    }
}
