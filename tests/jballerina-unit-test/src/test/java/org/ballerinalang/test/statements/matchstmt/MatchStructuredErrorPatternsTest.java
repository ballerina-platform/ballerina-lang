/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.statements.matchstmt;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the structured error patterns with match statement in Ballerina.
 *
 * @since 0.990.4
 */
@Test(groups = "brokenOnErrorChange")
public class MatchStructuredErrorPatternsTest {
    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/structured_error_match_patterns.bal");
        resultNegative = BCompileUtil.compile(
                "test-src/statements/matchstmt/structured_error_match_patterns_negative.bal");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testBasicErrorMatch() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorMatch", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        int i = -1;
        Assert.assertEquals(returns[++i].stringValue(), "Error Code:Msg");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testBasicErrorMatch2() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorMatch2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(returns[++i].stringValue(), msg + "error : Error Code {\"message\":\"Msg\"}");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testBasicErrorMatch3() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorMatch3", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(returns[++i].stringValue(), msg + "error : Error Code");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testBasicErrorMatch4() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorMatch4", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        BValueArray results = (BValueArray) returns[0];
        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "boolean : true");
        Assert.assertEquals(results.getString(++i), msg + "string : It's fatal");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testBasicErrorMatch5() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorMatch5", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        BValueArray results = (BValueArray) returns[0];
        int i = -1;
        String msg = "Matched with ";
        // todo: uncomment after fixing record match, and remove ++i below
        //Assert.assertEquals(results.getString(++i), msg + "a record : true");
        ++i;
        Assert.assertEquals(results.getString(++i), msg + "an error : Error Code 1");
        Assert.assertEquals(results.getString(++i), msg + "an error : Error Code 1 {}");
        Assert.assertEquals(results.getString(++i), msg + "an error 1: Error Code 1");
        Assert.assertEquals(results.getString(++i), msg + "an error : Error Code 1, message = Something Wrong");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testBasicErrorMatch6() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorMatch6", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        BValueArray results = (BValueArray) returns[0];
        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "string");
        Assert.assertEquals(results.getString(++i), msg +
                "an error: reason = Just Panic, message = Bit of detail");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testErrorWithUnderscore() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorWithUnderscore", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        BValueArray results = (BValueArray) returns[0];
        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "error reason = Error One");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testBasicErrorMatch7() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicErrorMatch7", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        BValueArray results = (BValueArray) returns[0];
        int i = -1;
        String msg = "Matched with ";
        // todo: uncomment after fixing record match, and remove ++i below
        //Assert.assertEquals(results.getString(++i), msg + "a record : true");
        ++i;
        Assert.assertEquals(results.getString(++i), "Default");
        Assert.assertEquals(results.getString(++i), msg + "an error : Error Code 1Something Wrong");
    }

    @Test(description = "Test error pattern matching with union of finite type reason")
    public void testFiniteTypedReasonVariable() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypedReasonVariable");
        Assert.assertEquals(returns.length, 1);
        BValueArray results = (BValueArray) returns[0];
        int i = -1;
        Assert.assertEquals(results.getString(++i), "reason = Error One, message = msgOne, fatal = true");
        Assert.assertEquals(results.getString(++i), "reason = Error Three, message = msgTwo, fatal = false");
    }

    @Test(description = "TestMatchingErrorRestParameter")
    public void testErrorRestParameterMatch() {
        BInteger[] args0 = { new BInteger(0) };
        BValue[] returns0 = BRunUtil.invoke(result, "testErrorRestParamMatch", args0);
        Assert.assertEquals(returns0[0].stringValue(), "Msg of error-0");

        BInteger[] args1 = { new BInteger(1) };
        BValue[] returns1 = BRunUtil.invoke(result, "testErrorRestParamMatch", args1);
        Assert.assertEquals(returns1[0].stringValue(), "x 1");

        BInteger[] args2 = { new BInteger(2) };
        BValue[] returns2 = BRunUtil.invoke(result, "testErrorRestParamMatch", args2);
        Assert.assertEquals(returns2[0].stringValue(), "x");

        BInteger[] args3 = { new BInteger(3) };
        BValue[] returns3 = BRunUtil.invoke(result, "testErrorRestParamMatch", args3);
        Assert.assertEquals(returns3[0].stringValue(), "Error Code foo=foo");
    }

    @Test(description = "Test error match pattern")
    public void testErrorMatchPattern() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorMatchPattern", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        int i = -1;
        Assert.assertEquals(returns[++i].stringValue(), "Error Code:Msg");
    }

    @Test(description = "Test error const reason match pattern")
    public void testErrorConstReasonMatchPattern() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorConstReasonMatchPattern", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        int i = -1;
        Assert.assertEquals(returns[++i].stringValue(), "Const reason:Msg");
    }

    @Test(description = "Test indirect error match pattern")
    public void testIndirectErrorMatchPattern() {
        BValue[] returns = BRunUtil.invoke(result, "testIndirectErrorMatchPattern", new BValue[]{});
        Assert.assertEquals(returns[0].stringValue(), "Msg");
    }

    @Test(description = "Test pattern will not be matched 2")
    public void testUnreachablePatterns() {
        Assert.assertEquals(resultNegative.getErrorCount(), 8);
        int i = -1;
        String unreachablePattern = "unreachable pattern: " +
                "preceding patterns are too general or the pattern ordering is not correct";
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 29, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 34, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 44, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 50, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 60, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 65, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 80, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 85, 13);
    }

    @Test(description = "Test unsupported error match pattern")
    public void testErrorMatchPatternNotSupportedErrors() {
        CompileResult result = BCompileUtil.compile(
                "test-src/statements/matchstmt/structured_error_match_patterns_negative2.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++,
                "error match pattern with a constant reference as the reason is not yet supported", 33, 15);
        BAssertUtil.validateError(result, i++,
                "invalid error reason binding pattern, error reason should be 'var reason'", 36, 15);
        BAssertUtil.validateError(result, i++,
                "invalid error reason binding pattern, error reason should be 'var r'", 45, 15);
        BAssertUtil.validateError(result, i++,
                "invalid error detail type 'ErrorDataABC', expected a subtype of " +
                        "'record {| string message?; error cause?; (anydata|error)...; |}'", 52, 24);
        BAssertUtil.validateError(result, i++, "unknown type 'ErrorDataABC'", 52, 24);
        BAssertUtil.validateError(result, i++, "undefined symbol 'm'", 57, 62);
        Assert.assertEquals(result.getErrorCount(), i);

    }

    @Test()
    public void testErrorMatchWihtoutReason() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorMatchWihtoutReason");
        Assert.assertEquals(returns[0].stringValue(), "error detail message");
    }
}
