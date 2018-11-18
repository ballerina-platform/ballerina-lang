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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the structured patterns with match statement in Ballerina.
 *
 * @since 0.985.0
 */
public class MatchStructuredPatternsTest {

    private CompileResult result, resultNegative, resultNegative2;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/structured_match_patterns.bal");
        resultNegative = BCompileUtil.compile(
                "test-src/statements/matchstmt/structured_match_patterns_negative.bal");
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

    @Test(description = "Test pattern will not be matched")
    public void testPatternNotMatched() {
        Assert.assertEquals(resultNegative.getErrorCount(), 12);
        int i = -1;
        String patternNotMatched = "pattern will not be matched";
        String invalidRecordPattern = "invalid record binding pattern; ";
        String invalidTuplePattern = "invalid tuple variable; ";

        BAssertUtil.validateError(resultNegative, ++i,
                invalidRecordPattern + "unknown field 'f' in record type 'ClosedFoo'", 34, 14);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidRecordPattern + "unknown field 'f' in record type 'ClosedFoo'", 35, 14);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidRecordPattern + "unknown field 'a' in record type 'ClosedFoo'", 38, 14);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 39, 9);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidTuplePattern + "expecting a tuple type but found 'ClosedFoo' in type definition", 40, 13);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 50, 9);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidTuplePattern + "expecting a tuple type but found 'OpenedFoo' in type definition", 51, 13);

        BAssertUtil.validateError(resultNegative, ++i,
                "invalid tuple binding pattern; member variable count mismatch with member type count", 61, 13);
        BAssertUtil.validateError(resultNegative, ++i,
                "invalid record binding pattern with type '(string,int,ClosedFoo)'", 62, 14);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 63, 9);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidTuplePattern + "expecting a tuple type but found 'ClosedFoo' in type definition", 64, 20);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidRecordPattern + "unknown field 'f' in record type 'ClosedFoo'", 66, 21);
    }

    @Test(description = "Test pattern will not be matched 2")
    public void testUnreachablePatterns() {
        Assert.assertEquals(resultNegative2.getErrorCount(), 18);
        int i = -1;
        String unreachablePattern = "unreachable pattern: " +
                "preceding patterns are too general or the pattern ordering is not correct";
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 33, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 35, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 47, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 48, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 62, 14);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 68, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 73, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 74, 14);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 75, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 76, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 95, 14);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 96, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 107, 14);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 109, 14);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 111, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 122, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 124, 13);
        BAssertUtil.validateError(resultNegative2, ++i, unreachablePattern, 125, 13);
    }
}
