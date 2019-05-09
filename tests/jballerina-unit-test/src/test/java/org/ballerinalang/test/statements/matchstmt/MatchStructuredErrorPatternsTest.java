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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.testng.Assert;
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
        String msg = "Matched with ";
        Assert.assertEquals(returns[++i].stringValue(), msg + "error : Error Code {\"message\":\"Msg\"}");
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
        Assert.assertEquals(returns[++i].stringValue(), msg + "error : Error Code {}");
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
        Assert.assertEquals(results.getString(++i), msg + "an error : Error Code 1 {}");
        Assert.assertEquals(results.getString(++i), msg + "an error : Error Code 1 {\"message\":\"Something Wrong\"}");
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
                "an error {ballerina}KeyNotFound {\"message\":\"cannot find key 'invalid'\"}");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testErrorWithUnderscore() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorWithUnderscore", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        BValueArray results = (BValueArray) returns[0];
        int i = -1;
        String msg = "Matched with ";
        Assert.assertEquals(results.getString(++i), msg + "error var : Error One");
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
        Assert.assertEquals(results.getString(++i), "Error One {\"message\":\"msgOne\", \"fatal\":true}");
        Assert.assertEquals(results.getString(++i), "Error Three {\"message\":\"msgTwo\", \"fatal\":false}");
    }

    @Test(description = "Test pattern will not be matched 2")
    public void testUnreachablePatterns() {
        Assert.assertEquals(resultNegative.getErrorCount(), 9);
        int i = -1;
        String unreachablePattern = "unreachable pattern: " +
                "preceding patterns are too general or the pattern ordering is not correct";
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 27, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 32, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 42, 13);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid record binding pattern; unknown field 'detail' in record type 'ClosedFoo'", 47, 28);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 48, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 58, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 63, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 77, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 82, 13);
    }
}
