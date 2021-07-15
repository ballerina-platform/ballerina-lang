/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the structured patterns with match statement in Ballerina.
 *
 * @since 0.985.0
 */
public class MatchStructuredPatternsTest {

    private CompileResult result, resultNegative, resultNegative2, resultSemanticsNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/structured_match_patterns.bal");
        resultNegative = BCompileUtil.compile(
                "test-src/statements/matchstmt/structured_match_patterns_negative.bal");
        resultSemanticsNegative = BCompileUtil.compile(
                "test-src/statements/matchstmt/structured_match_patterns_semantics_negative.bal");
        resultNegative2 = BCompileUtil.compile(
                "test-src/statements/matchstmt/structured_match_patterns_unreachable_negative.bal");
    }

    @Test(description = "Test basics of structured pattern match statement 1")
    public void testMatchStatementBasics1() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        BString bString = (BString) returns[0];

        Assert.assertEquals(bString.stringValue(), "Matched Values : S, 24, 6.6, 4, true");
    }

    @Test(description = "Test basics of structured pattern match statement 2")
    public void testMatchStatementBasics2() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        BString bString = (BString) returns[0];

        Assert.assertEquals(bString.stringValue(), "Matched Values : S, 23, 5.6, 100, 12, S, 24, 5.6, 200");
    }

    @Test(description = "Test error not being match to wildcard match pattern using 'var _' pattern")
    public void testErrorShouldNotMatchWildCardPatternVarIgnore() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorShouldNotMatchWildCardPatternVarIgnore");
        Assert.assertEquals(returns[0].stringValue(), "no-match");
    }

    @Test(description = "Test error match fall through '_' to error pattern")
    public void testErrorNotMatchingVarIgnoreAndFallThroughToErrorPattern() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorNotMatchingVarIgnoreAndFallThroughToErrorPattern");
        Assert.assertEquals(returns[0].stringValue(), "{UserGenError}Error");
    }

    @Test(description = "Test pattern will not be matched")
    public void testPatternNotMatchedSemanticsNegative() {
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), 1);
        Assert.assertEquals(resultSemanticsNegative.getWarnCount(), 15);
        int i = -1;

        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 33, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 34, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 37, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 38, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 39, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 49, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 50, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 60, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 61, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 62, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 63, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 65, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unreachable pattern", 65, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unreachable pattern", 66, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unreachable pattern", 67, 9);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "unreachable code", 69, 5);
    }

    @Test(description = "Test pattern will not be matched")
    public void testPatternNotMatched() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        Assert.assertEquals(resultNegative.getWarnCount(), 5);
        int i = -1;
        String patternNotMatched = "pattern will not be matched";

        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 35, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 45, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 55, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 57, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 58, 9);
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 60, 5);
    }

    @Test(description = "Test pattern will not be matched 2")
    public void testUnreachablePatterns() {
        Assert.assertEquals(resultNegative2.getErrorCount(), 6);
        Assert.assertEquals(resultNegative2.getWarnCount(), 25);
        int i = -1;
        String unreachablePattern = "unreachable pattern";
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 32, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 34, 9);
         BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 46, 9);
         BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 47, 9);
         BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 50, 9);
         BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 60, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 61, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 67, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 72, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 73, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 74, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 75, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 78, 5);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 92, 35);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 94, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 95, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 99, 5);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 106, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 108, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 110, 9);
         BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 121, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 123, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 124, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 142, 5);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 149, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 157, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "this function must return a result", 159, 1);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 165, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 166, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 167, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 170, 5);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
        resultSemanticsNegative = null;
        resultNegative2 = null;
    }
}
