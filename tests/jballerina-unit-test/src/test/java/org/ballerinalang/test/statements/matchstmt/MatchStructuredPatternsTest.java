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
        Assert.assertEquals(resultSemanticsNegative.getWarnCount(), 67);
        int i = -1;

        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 33, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 33, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'b'", 33, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'c'", 33, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'integer'", 33, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 33, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 34, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'f'", 34, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'integer'", 34, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 34, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'integer'", 35, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 35, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 36, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 37, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 37, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 38, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 39, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'i'", 39, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 39, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 44, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'b'", 44, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'c'", 44, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'integer'", 44, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 44, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'f'", 45, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'integer'", 45, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 45, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'integer'", 46, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 46, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 47, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 48, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 49, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 50, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'i'", 50, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 50, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 60, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 60, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'b'", 60, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 61, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'i'", 61, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 61, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 62, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 63, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 63, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'b'", 63, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'd'", 63, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'e'", 63, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 64, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'b'", 64, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 64, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "pattern will not be matched", 65, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unreachable pattern", 65, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 65, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'b'", 65, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'f'", 65, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'i'", 65, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 65, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unreachable pattern", 66, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 66, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'b'", 66, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'i'", 66, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 's'", 66, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unreachable pattern", 67, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'a'", 67, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'b'", 67, 9);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'c'", 67, 9);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "unreachable code", 69, 5);
        BAssertUtil.validateWarning(resultSemanticsNegative, ++i, "unused variable 'x'", 75, 9);
    }

    @Test(description = "Test pattern will not be matched")
    public void testPatternNotMatched() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        Assert.assertEquals(resultNegative.getWarnCount(), 30);
        int i = -1;
        String patternNotMatched = "pattern will not be matched";

        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'integer'", 33, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 33, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 34, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 35, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 40, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 40, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'c'", 40, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'integer'", 40, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 40, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'f'", 41, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'integer'", 41, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 41, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'integer'", 42, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 42, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 43, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 44, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 45, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 55, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 56, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 56, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 56, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 57, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 57, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 57, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'i'", 57, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 's'", 57, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 58, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 58, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 58, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'c'", 58, 9);
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 60, 5);
    }

    @Test(description = "Test pattern will not be matched 2")
    public void testUnreachablePatterns() {
        Assert.assertEquals(resultNegative2.getErrorCount(), 6);
        Assert.assertEquals(resultNegative2.getWarnCount(), 140);
        int i = -1;
        String unreachablePattern = "unreachable pattern";
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'age'", 31, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 31, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 32, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 32, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'p'", 32, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'q'", 32, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 33, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 34, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 34, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 43, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 43, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'i'", 43, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 's'", 43, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 44, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 44, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 44, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 45, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 45, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 45, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'd'", 45, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 46, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 46, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 46, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 46, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'e'", 46, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'f'", 46, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 47, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 47, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 47, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 47, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'i'", 47, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 's'", 47, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 49, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 49, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 50, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 50, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 50, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 50, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 59, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 60, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 60, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 60, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 61, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 61, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 65, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 65, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 66, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 66, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 66, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 67, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 67, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 67, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'x'", 71, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 72, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'y'", 72, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 73, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 73, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 73, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 74, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 74, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 74, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 75, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'y'", 75, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 78, 5);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 84, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'p'", 84, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'q'", 84, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 85, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 85, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 85, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'e'", 85, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'f'", 85, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'age'", 86, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 86, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 87, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 87, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 87, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'd'", 87, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'x'", 88, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'age'", 92, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 92, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 92, 35);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 93, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 93, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 93, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'd'", 93, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 94, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 94, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'p'", 94, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'q'", 94, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 95, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 95, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 95, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 95, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'e'", 95, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'f'", 95, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'x'", 96, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 99, 5);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 105, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 106, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name2'", 106, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 107, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 108, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'name'", 108, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 109, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 109, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 109, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'd'", 109, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'var1'", 109, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'var2'", 109, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 110, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 110, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 110, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 110, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'd'", 110, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'var1'", 110, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'var2'", 110, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 119, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 120, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 120, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 121, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 121, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 122, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 122, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 123, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 123, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'c'", 123, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 124, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 124, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 133, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'x'", 134, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 138, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'x'", 139, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 142, 5);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'x'", 148, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 149, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'x'", 156, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 157, 9);
        BAssertUtil.validateError(resultNegative2, ++i, "this function must return a result", 159, 1);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'x'", 164, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 165, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 166, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, unreachablePattern, 167, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'a'", 167, 9);
        BAssertUtil.validateWarning(resultNegative2, ++i, "unused variable 'b'", 167, 9);
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
