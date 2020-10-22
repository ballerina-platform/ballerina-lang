/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.ballerinalang.test.error;

import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for Error.
 *
 * @since 0.985.0
 */
public class ErrorTest {

    private CompileResult errorTestResult;
    private CompileResult distinctErrorTestResult;
    private CompileResult negativeCompileResult;
    private CompileResult negativeDistinctErrorRes;

    private static final String ERROR1 = "error1";
    private static final String ERROR2 = "error2";
    private static final String ERROR3 = "error3";
    private static final String EMPTY_CURLY_BRACE = "{}";
    private static final String CONST_ERROR_REASON = "reason one";

    @BeforeClass
    public void setup() {
        errorTestResult = BCompileUtil.compile("test-src/error/error_test.bal");
        distinctErrorTestResult = BCompileUtil.compile("test-src/error/distinct_error_test.bal");
        negativeDistinctErrorRes = BCompileUtil.compile("test-src/error/distinct_error_test_negative.bal");
        negativeCompileResult = BCompileUtil.compile("test-src/error/error_test_negative.bal");
    }

    @Test
    public void testDistinctFooError() {
        BValue[] errors = BRunUtil.invoke(distinctErrorTestResult, "testFooError");
        Assert.assertEquals(errors[0].stringValue(), "error message {\"detailField\":true}");
    }

    @Test
    public void testFunctionCallInDetailArgExpr() {
         BRunUtil.invoke(distinctErrorTestResult, "testFunctionCallInDetailArgExpr");
    }

    @Test
    public void testNegativeDistinctError() {
        int i = 0;
        BAssertUtil.validateError(negativeDistinctErrorRes, i++,
                "missing error detail arg for error detail field 'code'", 8, 13);
        BAssertUtil.validateError(negativeDistinctErrorRes, i++,
                "incompatible types: expected 'Foo', found 'error'", 11, 13);
        BAssertUtil.validateError(negativeDistinctErrorRes, i++,
                "incompatible types: expected 'Foo', found 'error'", 15, 12);
        Assert.assertEquals(negativeDistinctErrorRes.getErrorCount(), i);
    }

    @Test
    public void testIndirectErrorCtor() {
        BValue[] errors = BRunUtil.invoke(errorTestResult, "testIndirectErrorConstructor");
        Assert.assertEquals(errors.length, 4);
        Assert.assertEquals(errors[0].stringValue(), "arg {message:\"\", data:{}}");
        Assert.assertEquals(errors[1].stringValue(), "arg {message:\"\", data:{}}");
        Assert.assertEquals(errors[2], errors[0]);
        Assert.assertEquals(errors[3], errors[1]);
    }

    @Test
    public void errorConstructReasonTest() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "errorConstructReasonTest");

        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), ERROR1);
        Assert.assertEquals(((BError) returns[0]).getDetails().stringValue(), EMPTY_CURLY_BRACE);
        Assert.assertTrue(returns[1] instanceof BError);
        Assert.assertEquals(((BError) returns[1]).getReason(), ERROR2);
        Assert.assertEquals(((BError) returns[1]).getDetails().stringValue(), EMPTY_CURLY_BRACE);
        Assert.assertTrue(returns[2] instanceof BError);
        Assert.assertEquals(((BError) returns[2]).getReason(), ERROR3);
        Assert.assertEquals(((BError) returns[2]).getDetails().stringValue(), EMPTY_CURLY_BRACE);
        Assert.assertEquals(returns[3].stringValue(), ERROR1);
        Assert.assertEquals(returns[4].stringValue(), ERROR2);
        Assert.assertEquals(returns[5].stringValue(), ERROR3);
    }

    @Test
    public void errorConstructDetailTest() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "errorConstructDetailTest");
        String detail1 = "{\"message\":\"msg1\"}";
        String detail2 = "{\"message\":\"msg2\"}";
        String detail3 = "{\"message\":\"msg3\"}";
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), ERROR1);
        Assert.assertEquals(((BError) returns[0]).getDetails().stringValue().trim(), detail1);
        Assert.assertTrue(returns[1] instanceof BError);
        Assert.assertEquals(((BError) returns[1]).getReason(), ERROR2);
        Assert.assertEquals(((BError) returns[1]).getDetails().stringValue().trim(), detail2);
        Assert.assertTrue(returns[2] instanceof BError);
        Assert.assertEquals(((BError) returns[2]).getReason(), ERROR3);
        Assert.assertEquals(((BError) returns[2]).getDetails().stringValue().trim(), detail3);
        Assert.assertEquals(returns[3].stringValue().trim(), detail1);
        Assert.assertEquals(returns[4].stringValue().trim(), detail2);
        Assert.assertEquals(returns[5].stringValue().trim(), detail3);
    }

    @Test
    public void errorPanicTest() {
        // Case without panic
        BValue[] args = new BValue[] { new BInteger(10) };
        BValue[] returns = BRunUtil.invoke(errorTestResult, "errorPanicTest", args);
        Assert.assertEquals(returns[0].stringValue(), "done");

        // Now panic
        args = new BValue[] { new BInteger(15) };
        Exception expectedException = null;
        try {
            BRunUtil.invoke(errorTestResult, "errorPanicTest", args);
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String message = ((BLangRuntimeException) expectedException).getMessage();

        Assert.assertEquals(message,
                "error: largeNumber {\"message\":\"large number\"}\n" +
                        "\tat error_test:errorPanicCallee(error_test.bal:37)\n" +
                        "\t   error_test:errorPanicTest(error_test.bal:31)");
    }

    @Test
    public void errorTrapTest() {
        // Case without panic
        BValue[] args = new BValue[] { new BInteger(10) };
        BValue[] returns = BRunUtil.invoke(errorTestResult, "errorTrapTest", args);
        Assert.assertEquals(returns[0].stringValue(), "done");

        // Now panic
        args = new BValue[] { new BInteger(15) };
        returns = BRunUtil.invoke(errorTestResult, "errorTrapTest", args);
        String result = "largeNumber {\"message\":\"large number\"}";
        Assert.assertEquals(returns[0].stringValue(), result.trim());
    }

    @Test
    public void customErrorDetailsTest() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testCustomErrorDetails");
        Assert.assertEquals(returns[0].stringValue(), "trxErr {message:\"\", data:\"test\"}");
        Assert.assertEquals(((BError) returns[0]).getDetails().getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        Assert.assertEquals(((BError) returns[0]).getDetails().getType().getName(), "TrxErrorData & readonly");
    }

    @Test
    public void testCustomErrorDetails2() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testCustomErrorDetails2");
        Assert.assertEquals(returns[0].stringValue(), "test");
    }

    @Test
    public void testErrorWithErrorConstructor() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testErrorWithErrorConstructor");
        Assert.assertEquals(returns[0].stringValue(), "test");
    }

//    @Test(groups = { "disableOnOldParser" })
//    public void testGetCallStack() {
//        BValue[] returns = BRunUtil.invoke(errorTestResult, "getCallStackTest");
//        Assert.assertEquals(returns[0].stringValue(), "{callableName:\"getCallStack\", " +
//                                                      "moduleName:\"ballerina.runtime.0_5_0.errors\"," +
//                                                      " fileName:\"errors.bal\", lineNumber:38}");
//    }

    @Test
    public void testConsecutiveTraps() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testConsecutiveTraps");
        Assert.assertEquals(returns[0].stringValue(), "Error");
        Assert.assertEquals(returns[1].stringValue(), "Error");
    }

    @Test
    public void testOneLinePanic() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testOneLinePanic");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray array = (BValueArray) returns[0];
        Assert.assertEquals(array.getString(0), "Error1");
        Assert.assertEquals(array.getString(1), "Error2");
        Assert.assertEquals(array.getString(2), "Something Went Wrong");
        Assert.assertEquals(array.getString(3), "Error3");
        Assert.assertEquals(array.getString(4), "Something Went Wrong");
        Assert.assertEquals(array.getString(5), "1");
    }

    @Test
    public void testGenericErrorWithDetailRecord() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testGenericErrorWithDetailRecord");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTrapSuccessScenario() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testTrapWithSuccessScenario");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(dataProvider = "userDefTypeAsReasonTests")
    public void testErrorWithUserDefinedReasonType(String testFunction) {
        BValue[] returns = BRunUtil.invoke(errorTestResult, testFunction);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), CONST_ERROR_REASON);
    }

    @Test(dataProvider = "constAsReasonTests")
    public void testErrorWithConstantAsReason(String testFunction) {
        BValue[] returns = BRunUtil.invoke(errorTestResult, testFunction);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), CONST_ERROR_REASON);
        Assert.assertEquals(((BMap) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "error detail message");
    }

//    @Test
//    public void testCustomErrorWithMappingOfSelf() {
//        BValue[] returns = BRunUtil.invoke(errorTestResult, "testCustomErrorWithMappingOfSelf");
//        Assert.assertTrue(returns[0] instanceof BBoolean);
//        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
//    }

    @Test
    public void testUnspecifiedErrorDetailFrozenness() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testUnspecifiedErrorDetailFrozenness");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testErrorNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeCompileResult, i++,
                "invalid error detail type 'map<any>', expected a subtype of 'map<(anydata|readonly)>'", 41, 28);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "invalid error detail type 'boolean', expected a subtype of 'map<(anydata|readonly)>'", 42, 28);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'error<Foo>', found 'error'", 45, 17);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "invalid error detail type 'boolean', expected a subtype of 'map<(anydata|readonly)>'", 48, 11);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'error<boolean>', found 'error'", 48, 24);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'string', found 'boolean'", 48, 30);
        BAssertUtil.validateError(negativeCompileResult, i++, "self referenced variable 'e3'", 54, 22);
        BAssertUtil.validateError(negativeCompileResult, i++, "self referenced variable 'e3'", 54, 36);
        BAssertUtil.validateError(negativeCompileResult, i++, "self referenced variable 'e4'", 55, 34);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "missing mandatory error message argument in call to error constructor", 56, 27);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "missing mandatory error message argument in call to error constructor", 57, 19);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "missing mandatory error message argument in call to error constructor", 96, 18);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "missing mandatory error message argument in call to error constructor", 97, 21);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'UserDefErrorTwoA', found 'error'", 110, 28);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'UserDefErrorTwoA', found 'error'", 112, 28);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'error', found '(error|int)'", 118, 11);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'error<record {| " +
                        "string message?; error cause?; int i; anydata...; |}>', found 'int'", 122, 65);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'error', found 'int'", 127, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'error', found 'string'", 128, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'error', found 'record {| string a; |}'", 129, 5);
        Assert.assertEquals(negativeCompileResult.getErrorCount(), i);
    }

    @DataProvider(name = "userDefTypeAsReasonTests")
    public Object[][] userDefTypeAsReasonTests() {
        return new Object[][] {
                { "testErrorConstrWithConstForUserDefinedReasonType" },
                { "testErrorConstrWithLiteralForUserDefinedReasonType" }
        };
    }

    @DataProvider(name = "constAsReasonTests")
    public Object[][] constAsReasonTests() {
        return new Object[][] {
                { "testErrorConstrWithConstForConstReason" },
                { "testErrorConstrWithConstLiteralForConstReason" }
        };
    }

    @Test()
    public void errorReasonSubtypeTest() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "errorReasonSubType");
        Assert.assertEquals(((BError) returns[0]).getReason(), "ErrNo-1");
        Assert.assertEquals(((BError) returns[1]).getReason(), "ErrorNo-2");
        Assert.assertEquals(((BError) returns[2]).getReason(), "ErrNo-1");
        Assert.assertEquals(((BError) returns[3]).getReason(), "ErrorNo-2");
    }

    @Test()
    public void indirectErrorCtorTest() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "indirectErrorCtor");
        Assert.assertEquals(returns[0].stringValue(), "foo");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertEquals(returns[2].stringValue(), "foo {code:3456}");
    }

    @Test()
    public void testUnionLhsWithIndirectErrorRhs() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testUnionLhsWithIndirectErrorRhs");
        Assert.assertEquals(((BError) returns[0]).getReason(), "Foo");
    }

    @Test()
    public void testOptionalErrorReturn() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testOptionalErrorReturn");
        Assert.assertEquals(returns[0].stringValue(), "this is broken {\"message\":\"too bad\"}");
    }

    @Test()
    public void testIndirectErrorReturn() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testIndirectErrorReturn");
        Assert.assertEquals(returns[0].stringValue(), "Foo {message:\"error msg\"}");
    }

    @Test(groups = { "disableOnOldParser" }, enabled = false)
    public void testStackTraceInNative() {
        Exception expectedException = null;
        try {
            BRunUtil.invoke(errorTestResult, "testStackTraceInNative");
        } catch (Exception e) {
            expectedException = e;
        }

        Assert.assertNotNull(expectedException);
        String message = expectedException.getMessage();
        Assert.assertEquals(message,
                "error: array index out of range: index: 4, size: 2\n\t" +
                        "at ballerina.lang_array.1_1_0:slice(array.bal:106)\n\t" +
                        "   error_test:testStackTraceInNative(error_test.bal:280)");
    }

    @Test
    public void testPanicOnErrorUnion() {
        BValue[] args = new BValue[] { new BInteger(0) };
        BValue[] result = BRunUtil.invoke(errorTestResult, "testPanicOnErrorUnion", args);
        Assert.assertEquals(result[0].stringValue(), "str");
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = "error: x.*")
    public void testPanicOnErrorUnionCustomError() {
        BValue[] args = new BValue[] { new BInteger(1) };
        BRunUtil.invoke(errorTestResult, "testPanicOnErrorUnion", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp = "error: y " +
            "\\{\"code\":4\\}.*")
    public void testPanicOnErrorUnionCustomError2() {
        BValue[] args = new BValue[] { new BInteger(2) };
        BRunUtil.invoke(errorTestResult, "testPanicOnErrorUnion", args);
    }

    @Test
    public void testErrorUnionPassedToErrorParam() {
        BValue[] result = BRunUtil.invoke(errorTestResult, "testErrorUnionPassedToErrorParam");
        Assert.assertEquals(result[0].stringValue(), "a1");
    }

    @Test
    public void testStackOverFlow() {
        BValue[] result = BRunUtil.invoke(errorTestResult, "testStackOverFlow");
        String expected1 = "{callableName:\"bar\", moduleName:\"error_test\", fileName:\"error_test.bal\", " +
                "lineNumber:344}";
        String expected2 = "{callableName:\"bar2\", moduleName:\"error_test\", fileName:\"error_test.bal\", " +
                "lineNumber:348}";
        String resultStack = ((BValueArray) result[0]).getRefValue(0).toString();
        Assert.assertTrue(resultStack.equals(expected1) || resultStack.equals(expected2), "Received unexpected " +
                "stacktrace element: " + resultStack);
        Assert.assertEquals(result[1].stringValue(), "{ballerina}StackOverflow");
    }

    @Test
    public void testErrorTrapVarReuse() {
        BValue[] returns = BRunUtil.invoke(errorTestResult, "testErrorTrapVarReuse");
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertNull(returns[1]);
        BError bError = (BError) returns[0];
        Assert.assertEquals(bError.getReason(), "panic now");
    }

    @Test
    public void testErrorTypeDescriptionInferring() {
        BRunUtil.invoke(errorTestResult, "testErrorTypeDescriptionInferring");
    }

    @Test
    public void testDefaultErrorTypeDescriptionInferring() {
        BRunUtil.invoke(errorTestResult, "testDefaultErrorTypeDescriptionInferring");
    }

    @Test
    public void testUnionErrorTypeDescriptionInferring() {
        BRunUtil.invoke(errorTestResult, "testUnionErrorTypeDescriptionInferring");
    }
}
