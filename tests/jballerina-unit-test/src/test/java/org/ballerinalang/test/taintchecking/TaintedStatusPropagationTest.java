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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test tainted status propagation with different types of statements and expressions.
 *
 * @since 0.965.0
 */
public class TaintedStatusPropagationTest {

    @Test(dataProvider = "functionNamesProvider")
    public void testSuccessful(String fileName) {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/" + fileName);
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @DataProvider(name = "functionNamesProvider")
    public Object[] getSuccessfulFileNames() {
        return new String[]{"returns.bal", "variables.bal", "receiver.bal", "record.bal", "array.bal", "json.bal",
                "map.bal", "xml.bal", "basic-worker.bal", "if-condition.bal", "ternary.bal", "lambda.bal",
                "tuple-return.bal", "string-template.bal", "iterable.bal", "iterable-within-iterable.bal",
                "foreach.bal", "multiple-invocation-levels.bal", "cast.bal", "chained-invocations.bal",
                "global-variables.bal", "compound-assignment.bal", "is.bal", "object-functions.bal",
                "global-object-functions.bal", "object-functions-with-constructor.bal",
                "simple-worker-interaction.bal", "in-out-param-basic.bal", "param-status-with-native-invocations.bal",
                "error.bal", "call.bal", "closure-variable-assignment.bal", "global-func-pointer-async-invocation.bal",
                "let.bal"};
    }

    @Test
    public void testReturnNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/returns-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 2, 20);
    }

    @Test
    public void testVariableNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/variables-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 6);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 10, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 14, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 19, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to untainted parameter 'secureIn'", 25, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to untainted parameter 'secureIn'", 30, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to untainted parameter 'secureIn'", 37, 20);
    }

    @Test
    public void testReceiverNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/receiver-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 3, 20);
    }

    @Test
    public void testRecordNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/record-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 9, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 13, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 17, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to untainted parameter 'secureIn'", 21, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to untainted parameter 'secureIn'", 26, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to untainted parameter 'secureIn'", 31, 20);
        BAssertUtil.validateError(result, 6, "tainted value passed to untainted parameter 'secureIn'", 37, 20);
        BAssertUtil.validateError(result, 7, "tainted value passed to untainted parameter 'secureIn'", 43, 20);
    }

    @Test
    public void testArrayNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/array-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 6);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 4, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 13, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to untainted parameter 'secureIn'", 19, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to untainted parameter 'secureIn'", 23, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to untainted parameter 'secureIn'", 28, 20);
    }

    @Test
    public void testJsonNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/json-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 4, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 12, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to untainted parameter 'secureIn'", 16, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to untainted parameter 'secureIn'", 21, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to untainted parameter 'secureIn'", 26, 20);
        BAssertUtil.validateError(result, 6, "tainted value passed to untainted parameter 'secureIn'", 32, 20);
        BAssertUtil.validateError(result, 7, "tainted value passed to untainted parameter 'secureIn'", 38, 20);
    }

    @Test
    public void testMapNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/map-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 8);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 4, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 12, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to untainted parameter 'secureIn'", 16, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to untainted parameter 'secureIn'", 21, 20);
        BAssertUtil.validateError(result, 5, "tainted value passed to untainted parameter 'secureIn'", 26, 20);
        BAssertUtil.validateError(result, 6, "tainted value passed to untainted parameter 'secureIn'", 32, 20);
        BAssertUtil.validateError(result, 7, "tainted value passed to untainted parameter 'secureIn'", 38, 20);
    }

    @Test
    public void testXMLNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/xml-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 4);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 7, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 10, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 13, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to untainted parameter 'secureIn'", 14, 20);
    }

    @Test
    public void testBasicWorkerNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/basic-worker-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 3, 24);
    }

    @Test
    public void testIfConditionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/if-condition-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 4);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 8, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 16, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 24, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to untainted parameter 'secureIn'", 32, 20);
    }

    @Test
    public void testTernaryExprNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/ternary-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 3, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 6, 20);
    }

    @Test
    public void testLambdaNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/lambda-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 28, 20);
    }

    @Test
    public void testTupleReturnNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/tuple-return-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 6, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 7, 20);
    }

    @Test
    public void testStringTemplateNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/string-template-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 4, 20);
    }

    @Test
    public void testIterableNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/iterable-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 3, 20);
    }

    @Test
    public void testLambdaAsArgumentToIterableLanglibFunction() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/lambda-as-argument-to-iterable-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 6, 28);
    }

    @Test
    public void testForEachNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/foreach-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 5, 24);
    }

    @Test
    public void testMultipleInvocationLevelsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/multiple-invocation-levels-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'normalInput'", 2, 20);
    }

    @Test
    public void testCastNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/cast-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 4, 20);
    }

    @Test
    public void testChainedInvocationsNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/chained-invocations-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 2, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'rec'", 6, 22);
    }

    @Test
    public void testWithoutUserDataNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/without-user-data-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 7, 20);
    }

    @Test
    public void testGlobalVariablesNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/global-variables-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "tainted value passed to global variable 'KK'", 3, 1);
        BAssertUtil.validateError(result, i++, "tainted value passed to untainted parameter 'pa'", 10, 9);
        BAssertUtil.validateError(result, i++, "tainted value passed to global variable 'M'", 11, 5);
        BAssertUtil.validateError(result, i++, "tainted value passed to global variable 'REC'", 12, 5);
        BAssertUtil.validateError(result, i++, "tainted value passed to global variable 'REC'", 13, 5);
        BAssertUtil.validateError(result, i++, "tainted value passed to global variable 'REC'", 14, 5);
        BAssertUtil.validateError(result, i++, "tainted value passed to global variable 'globalVariable'", 22, 5);
        Assert.assertEquals(result.getDiagnostics().length, i);
    }

//    @Test
//    public void testHttpService() {
//        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/http-service.bal");
//        Assert.assertEquals(result.getDiagnostics().length, 3);
//        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 14, 24);
//        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 15, 24);
//        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'payload'", 22, 37);
//    }

//
//    @Test
//    public void testHttpServiceInlineListenerDecl() {
//        CompileResult result = BCompileUtil.compile(
//                "test-src/taintchecking/propagation/http-service-in-line-listener.bal");
//        Assert.assertEquals(result.getDiagnostics().length, 3);
//        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 28, 24);
//        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 29, 24);
//        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'payload'", 36, 37);
//    }

    @Test
    public void testCompoundAssignmentNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/compound-assignment-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 3);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 7, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 11, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 16, 24);
    }

    @Test
    public void testMatchNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/is-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 6, 24);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 10, 24);
    }

    @Test
    public void testObjectFunctionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/object-functions-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 14, 20);
    }

    @Test
    public void testGlobalObjectFunctionNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/taintchecking/propagation/global-object-functions-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "method invocation taint global object 'G'", 20, 5);
    }

    @Test
    public void testTaintedAssignmentToUntaintedObjReference() {
        CompileResult result = BCompileUtil.compile(
                "test-src/taintchecking/propagation/object-tainted-assigment-to-untainted-objref.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0,
                "tainted value passed to untainted parameter 'secureIn' originating from object method " +
                        "'test2' invocation", 52, 15);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 56, 20);
    }

    @Test
    public void testObjectFunctionWithConstructorNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/object-functions-with-constructor-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 21, 20);
    }

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/17497
    public void testSimpleWorkerInteractionNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/simple-worker-interaction-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 6, 24);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 11, 24);
    }

    @Test
    public void testSimpleBlockedWorkerInteractionNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/simple-worker-interaction-blocked-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 5, 24);
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
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 13, 24);
    }

    @Test
    public void testInOutParametersNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/" +
                "in-out-param-basic-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 5);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 28, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 32, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 36, 20);
        BAssertUtil.validateError(result, 3, "tainted value passed to untainted parameter 'secureIn'", 40, 20);
        BAssertUtil.validateError(result, 4, "tainted value passed to untainted parameter 'secureIn'", 44, 20);
    }

    @Test
    public void testParameterStatusWithNativeInvocationsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/" +
                "param-status-with-native-invocations-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'key'", 19, 26);
    }

    @Test
    public void testErrorNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/error-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 4);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 19, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 22, 20);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'secureIn'", 24, 21);
        BAssertUtil.validateError(result, 3, "tainted value passed to untainted parameter 'secureIn'", 25, 21);
    }

    @Test
    public void testCallNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/call-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 18, 20);
    }

    @Test
    public void testClosureVariableAssignmentNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/" +
                "closure-variable-assignment-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to closure variable 'test'", 22, 9);
    }

    @Test
    public void testLangLibFunctionTaintPropagationNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/taintchecking/propagation/lang-lib-function-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 3);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'p'", 33, 16);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'p'", 34, 16);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'p'", 39, 16);
    }

    @Test
    public void testTaintedStatusPropagationThroughListConstructor() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/list-ctor-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 't'", 20, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'a'", 21, 20);
    }

    @Test
    public void testTaintednessPropagationIntoTypeGuardNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/into-type-guard-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'arg'", 21, 13);
    }

    @Test
    public void testTaintednessPropagationCheckExpressionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/check-expression-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'arg'", 19, 9);
        BAssertUtil.validateError(result, 1,
                "functions returning tainted value are required to annotate return signature @tainted: 'foo'", 22, 24);
    }

     @Test
     public void testTaintLetExprNegative() {
         CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/let-negative.bal");
         Assert.assertEquals(result.getDiagnostics().length, 2);
         BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 19, 20);
         BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 22, 20);
     }

    @Test
    public void testNamedArgToNewExpr() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/named-arg-to-new.bal");
        Assert.assertEquals(result.getDiagnostics().length, 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'username'", 18, 16);
    }

    @Test
    public void testRestArgs() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/rest-args.bal");
        Assert.assertEquals(result.getDiagnostics().length, 3);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'rest'", 14, 18);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'arg'", 27, 18);
        BAssertUtil.validateError(result, 2, "tainted value passed to untainted parameter 'argv'", 28, 23);
    }

    @Test
    public void testClassMemberWithConstructorNegative() {
        CompileResult result = BCompileUtil
                .compile("test-src/taintchecking/propagation/class-member-with-constructor-negative.bal");
        Assert.assertEquals(result.getDiagnostics().length, 2);
        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureIn'", 30, 20);
        BAssertUtil.validateError(result, 1, "tainted value passed to untainted parameter 'secureIn'", 31, 20);
    }

//    @Test () Disabled on #21823
//    public void testLambdaFunctionBlockedAnalysis() {
//        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/lambda-func.bal");
//        Assert.assertEquals(result.getDiagnostics().length, 1);
//        BAssertUtil.validateError(result, 0, "tainted value passed to untainted parameter 'secureParameter'", 5, 36);
//    }
}
