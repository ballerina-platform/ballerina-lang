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
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the structured error patterns with match statement in Ballerina.
 *
 * @since 0.990.4
 */
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
        Assert.assertEquals(results.getString(++i), msg + "a record : true");
        Assert.assertEquals(results.getString(++i), msg + "an error : Error Code 1");
        Assert.assertEquals(results.getString(++i), msg + "an error : Error Code 1");
        Assert.assertEquals(results.getString(++i), msg + "an error 1: Error Code 1");
        Assert.assertEquals(results.getString(++i), msg + "an error 1: Error Code 1");
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
        Assert.assertEquals(results.getString(++i), msg + "a record : true");
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
        Assert.assertEquals(returns0[0].stringValue(), "Error Code{}");

        BInteger[] args1 = { new BInteger(1) };
        BValue[] returns1 = BRunUtil.invoke(result, "testErrorRestParamMatch", args1);
        Assert.assertEquals(returns1[0].stringValue(), "[\"x\",1]");

        BInteger[] args2 = { new BInteger(2) };
        BValue[] returns2 = BRunUtil.invoke(result, "testErrorRestParamMatch", args2);
        Assert.assertEquals(returns2[0].stringValue(), "x");

        BInteger[] args3 = { new BInteger(3) };
        BValue[] returns3 = BRunUtil.invoke(result, "testErrorRestParamMatch", args3);
        Assert.assertEquals(returns3[0].stringValue(), "Error Code{\"foo\":\"foo\"}");
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

    @Test
    public void testUnreachablePatterns() {
        int i = -1;
        String unreachablePattern = "unreachable pattern";
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 28, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'rest'", 28, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 29, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'detail'", 29, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 29, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'detail'", 33, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 33, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 34, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 43, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'rest'", 43, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 44, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 44, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 44, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 48, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 48, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 49, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 49, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'rest'", 49, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 49, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 50, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 50, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 50, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 59, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'rest'", 59, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 60, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 60, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 64, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'rest'", 64, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 65, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 65, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'message'", 79, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 79, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'rest'", 79, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 80, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'extra'", 80, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 80, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'message'", 84, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 84, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'rest'", 84, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 85, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'message'", 85, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'reason'", 85, 9);
        Assert.assertEquals(resultNegative.getWarnCount(), i + 1);
    }

    @Test()
    public void testErrorMatchWihtoutReason() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorMatchWihtoutReason");
        Assert.assertEquals(returns[0].stringValue(), "error detail message");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
