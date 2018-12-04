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
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/returns.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testReturnNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/returns-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 20);
    }

    @Test
    public void testVariable() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/variables.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testVariableNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/variables-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 6);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 10, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 14, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 19, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 25, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 30, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 37, 20);
    }

    @Test
    public void testReceiver() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/receiver.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testReceiverNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/receiver-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 20);
    }

    @Test
    public void testRecord() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/record.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testRecordNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/record-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 8);
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
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/array.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testArrayNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/array-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 6);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 13, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 19, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 23, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to sensitive parameter 'secureIn'", 28, 20);
    }

    @Test
    public void testJson() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/json.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testJsonNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/json-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 8);
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
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/map.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testMapNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/map-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 8);
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
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/xml.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testXMLNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/xml-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 5);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 7, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 10, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 13, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 14, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 18, 20);
    }

    @Test
    public void testBasicWorker() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/basic-worker.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testBasicWorkerNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/basic-worker-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 24);
    }

    @Test
    public void testIfCondition() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/if-condition.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testIfConditionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/if-condition-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 4);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 16, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 24, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 32, 20);
    }

    @Test
    public void testTernaryExpr() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/ternary.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testTernaryExprNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/ternary-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 6, 20);
    }

    @Test
    public void testLambda() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/lambda.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testTupleReturn() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/tuple-return.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testTupleReturnNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/tuple-return-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 6, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 7, 20);
    }

    @Test
    public void testStringTemplate() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/string-template.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testStringTemplateNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/string-template-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 20);
    }

    @Test
    public void testIterable() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/iterable.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testIterableNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/iterable-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 3, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 5, 59);
    }

    @Test
    public void testIterableWitinIterable() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/iterable-within-iterable.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testIterableWitinIterableNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/iterable-within-iterable-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 7, 32);
        BAssertUtil.validateError(result, 1, "tainted value passed to global variable 'globalVar'", 12, 38);
    }

    @Test
    public void testForEach() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/foreach.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testForEachNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/foreach-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 5, 24);
    }

    @Test
    public void testMultipleInvocationLevels() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/multiple-invocation-levels.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testMultipleInvocationLevelsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/multiple-invocation-levels-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'normalInput'", 2, 20);
    }

    @Test
    public void testCast() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/cast.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testCastNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/cast-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 4, 20);
    }

    @Test
    public void testChainedInvocations() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/chained-invocations.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testChainedInvocationsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/chained-invocations-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 2, 20);
    }

    @Test
    public void testWithoutUserDataNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/without-user-data-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 7, 20);
    }

    @Test
    public void testGlobalVariables() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/global-variables.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testGlobalVariablesNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/global-variables-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to global variable 'globalVariable'", 12, 5);
    }

    @Test
    public void testServiceVariables() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/service-level-variables.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testServiceVariablesNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/service-level-variables-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to global variable 'serviceLevelVariable'", 17, 9);
        BAssertUtil.validateError(result, 1, "tainted value passed to global variable 'globalLevelVariable'", 18, 9);
    }

    @Test
    public void testHttpService() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/http-service.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 14, 24);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 15, 24);
    }

    @Test
    public void testCompoundAssignment() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/compound-assignment.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testCompoundAssignmentNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/compound-assignment-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 3);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 5, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 9, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 14, 24);
    }

    @Test
    public void testMatch() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/is.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testMatchNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/is-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 6, 24);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 10, 24);
    }

    @Test
    public void testObjectFunction() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/object-functions.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testObjectFunctionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/object-functions-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 14, 20);
    }


    @Test
    public void testObjectExternalFunction() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/object-external-functions.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testObjectExternalFunctionNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-external-functions-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 16, 20);
    }

    @Test
    public void testObjectFunctionWithConstructor() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-functions-with-constructor.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testObjectFunctionWithConstructorNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-functions-with-constructor-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 22, 20);
    }

    @Test
    public void testSimpleWorkerInteraction() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/simple-worker-interaction.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test (enabled = false)
    public void testSimpleWorkerInteractionNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/simple-worker-interaction-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 11, 24);
    }

    @Test (enabled = false)
    public void testSimpleBlockedWorkerInteractionNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/simple-worker-interaction-blocked-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 5, 24);
    }

    @Test (enabled = false)
    public void testSimpleWorkerInteractionWithTupleAssignment() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/simple-worker-interaction-with-tuple-assignment.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test (enabled = false)
    public void testSimpleWorkerInteractionWithTupleAssignmentNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/" +
                "simple-worker-interaction-with-tuple-assignment-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 13, 24);
    }

    @Test (enabled = false)
    public void testForkJoin() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/fork-join.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test (enabled = false)
    public void testForkJoinNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/fork-join-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 10, 24);
    }

    @Test
    public void testInOutParameters() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/in-out-param-basic.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testInOutParametersNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/" +
                "in-out-param-basic-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 5);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 28, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 32, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 36, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 40, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to sensitive parameter 'secureIn'", 44, 20);
    }

    @Test
    public void testParameterStatusWithNativeInvocations() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/" +
                "param-status-with-native-invocations.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testParameterStatusWithNativeInvocationsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/" +
                "param-status-with-native-invocations-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'sqlQuery'", 32, 43);
    }

    @Test
    public void testError() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/error.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testErrorNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/error-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 4);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 19, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'secureIn'", 22, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'secureIn'", 24, 21);
        BAssertUtil.validateError(result, 3, "tainted value passed to sensitive parameter 'secureIn'", 25, 21);
    }

    @Test
    public void testCall() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/call.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testCallNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/call-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 18, 25);
    }
}
