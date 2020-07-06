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
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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

    @Test(description = "Test basics of structured pattern match statement 1", groups = "brokenOnErrorChange")
    public void testMatchStatementBasics1() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic1", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        BString bString = (BString) returns[0];

        Assert.assertEquals(bString.stringValue(), "Matched Values : S, 24, 6.6, 4, true");
    }

    @Test(description = "Test basics of structured pattern match statement 2", groups = "brokenOnErrorChange")
    public void testMatchStatementBasics2() {
        BValue[] returns = BRunUtil.invoke(result, "testStructuredMatchPatternsBasic2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        BString bString = (BString) returns[0];

        Assert.assertEquals(bString.stringValue(), "Matched Values : S, 23, 5.6, 100, 12, S, 24, 5.6, 200");
    }

    @Test(description = "Test error not being match to wildcard match pattern using 'var _' pattern",
            groups = "brokenOnErrorChange")
    public void testErrorShouldNotMatchWildCardPatternVarIgnore() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorShouldNotMatchWildCardPatternVarIgnore");
        Assert.assertEquals(returns[0].stringValue(), "no-match");
    }

    @Test(description = "Test error match fall through '_' to error pattern", groups = "brokenOnErrorChange")
    public void testErrorNotMatchingVarIgnoreAndFallThroughToErrorPattern() {
        BValue[] returns = BRunUtil.invoke(result, "testErrorNotMatchingVarIgnoreAndFallThroughToErrorPattern");
        Assert.assertEquals(returns[0].stringValue(), "{UserGenError}Error");
    }

    @Test(description = "Test pattern will not be matched")
    public void testPatternNotMatchedSemanticsNegative() {
        int i = -1;
        String invalidRecordPattern = "invalid record binding pattern; ";
        String invalidTuplePattern = "invalid tuple binding pattern: ";

        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                invalidRecordPattern + "unknown field 'f' in record type 'ClosedFoo'", 33, 13);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                invalidRecordPattern + "unknown field 'f' in record type 'ClosedFoo'", 34, 13);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                invalidRecordPattern + "unknown field 'a' in record type 'ClosedFoo'", 37, 13);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                invalidTuplePattern + "expected a tuple type, but found 'ClosedFoo'", 39, 13);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                invalidTuplePattern + "expected a tuple type, but found 'OpenedFoo'", 50, 13);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid tuple binding pattern; member variable count mismatch with member type count", 60, 13);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid record binding pattern with type '[string,int,ClosedFoo]'", 61, 13);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                invalidTuplePattern + "expected a tuple type, but found 'ClosedFoo'", 63, 20);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                invalidRecordPattern + "unknown field 'f' in record type 'ClosedFoo'", 65, 20);
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), i + 1);
    }

    @Test(description = "Test pattern will not be matched")
    public void testPatternNotMatched() {
        int i = -1;
        String patternNotMatched = "pattern will not be matched";
        String unreachablePattern =
                "unreachable pattern: preceding patterns are too general or the pattern ordering is not correct";

        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 35, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 45, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 55, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 57, 13);
        BAssertUtil.validateError(resultNegative, ++i, "pattern will always be matched", 66, 9);

        Assert.assertEquals(resultNegative.getErrorCount(), i + 1);
    }

    @Test(description = "Test pattern will not be matched 2")
    public void testUnreachablePatterns() {
        Assert.assertEquals(resultNegative2.getErrorCount(), 29);
        int i = -1;
        String unreachablePattern = "unreachable pattern: " +
                "preceding patterns are too general or the pattern ordering is not correct";
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 32, 12);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 34, 12);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 46, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 47, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 50, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 60, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 61, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 67, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 72, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 73, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 74, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 75, 13);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 78, 5);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 92, 35);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 94, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 95, 13);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 99, 5);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 106, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 108, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 110, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 121, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 123, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 124, 13);
        BAssertUtil.validateError(resultNegative2, ++i, "unreachable code", 142, 5);
        BAssertUtil.validateError(resultNegative2, ++i,
                "match statement has a static value default pattern and a binding value default pattern", 147, 5);
        BAssertUtil.validateError(resultNegative2, ++i, "this function must return a result", 153, 1);
        BAssertUtil.validateError(resultNegative2, ++i,
                "match statement has a static value default pattern and a binding value default pattern", 155, 5);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 166, 9);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 167, 13);
    }
}
