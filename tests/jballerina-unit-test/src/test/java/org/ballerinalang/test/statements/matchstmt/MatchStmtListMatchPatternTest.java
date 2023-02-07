// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.test.statements.matchstmt;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the const-pattern.
 *
 * @since 2.0.0
 */
public class MatchStmtListMatchPatternTest {

    private CompileResult result, resultNegative, resultSemanticsNegative, restMatchPatternResult;
    private String patternNotMatched = "pattern will not be matched";
    private String unreachablePattern = "unreachable pattern";
    private String unreachableCode = "unreachable code";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/list-match-pattern.bal");
        restMatchPatternResult = BCompileUtil.compile("test-src/statements/matchstmt/list-match-pattern-with-rest" +
                "-match-pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/list-match-pattern-negative.bal");
        resultSemanticsNegative = BCompileUtil.compile("test-src/statements/matchstmt/list-match-pattern-negative" +
                "-semantics.bal");
    }

    @DataProvider
    public Object[] dataToTestListMatchPatterns() {
        return new Object[]{
                "testListMatchPattern1",
                "testListMatchPattern2",
                "testListMatchPattern3",
                "testListMatchPattern4",
                "testListMatchPattern5",
                "testListMatchPattern6",
                "testListMatchPattern7",
                "testListMatchPattern8",
                "testListMatchPattern9",
                "testListMatchPattern10",
                "testListMatchPattern11",
                "testListMatchPattern12",
                "testListMatchPattern13",
                "testListMatchPattern14",
                "testListMatchPattern15",
                "testListMatchPattern16",
                "testListMatchPattern17",
                "testListMatchPattern18",
                "testListMatchPattern19",
                "testListMatchPattern20",
                "testListMatchPattern21",
                "testListMatchPattern22",
                "testListMatchPattern23",
                "testListMatchPattern24",
                "testListMatchPattern25",
                "testListMatchPattern26",
                "testListMatchPattern27",
                "testListMatchPatternWithWildCard",
                "testListMatchPattern29",
                "testListMatchPattern30",
                "testListMatchPattern31",
                "testListMatchPattern32",
                "testListMatchPattern33",
                "testListMatchPattern34",
                "testListMatchPattern35",
                "testListMatchPattern36",
                "testListMatchPattern37",
                "testListMatchPattern38",
                "testListMatchPattern39",
                "testListMatchPattern40",
                "testListMatchPattern41",
                "testListMatchPattern42",
                "testListMatchPattern43",
                "testListMatchPattern44",
                "testListMatchPattern45",
                "testListMatchPattern46",
                "testListMatchPattern47",
                "testListMatchPatternWithArrayAndAnydataIntersection",
        };
    }

    @Test(dataProvider = "dataToTestListMatchPatterns")
    public void testListMatchPatterns(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @Test(dataProvider = "dataToTestListMatchPatternWithRestPattern", description = "Test list match pattern with " +
            "rest match pattern")
    public void testListMatchPatternWithRestPattern(String functionName) {
        BRunUtil.invoke(restMatchPatternResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestListMatchPatternWithRestPattern() {
        return new Object[]{
                "testListMatchPatternWithRest1",
                "testListMatchPatternWithRest2",
                "testListMatchPatternWithRest3",
                "testListMatchPatternWithRest4",
                "testListMatchPatternWithRest5",
                "testListMatchPatternWithRestPattern11",
                "testListMatchPatternWithRestPattern13",
                "testListMatchPatternWithRestPattern14",
                "testListMatchPatternWithRestPattern15",
                "testListMatchPatternWithRestPattern16",
                "testListMatchPatternWithRestPatternWithArrayAndAnydataIntersection",
                "testListMatchPatternWithClosedArray",
        };
    }

    @Test(description = "invalid match patterns")
    public void testListMatchPatternNegative() {
        Assert.assertEquals(resultNegative.getErrorCount(), 7);
        Assert.assertEquals(resultNegative.getWarnCount(), 98);
        Assert.assertEquals(resultNegative.getHintCount(), 2);
        int i = -1;
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 20, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 23, 9);
        BAssertUtil.validateError(resultNegative, ++i, "all match patterns should contain the same set of variables",
                32, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 32, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 32, 17);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 40, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 43, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 43, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 43, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 53, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 53, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 53, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachableCode, 58, 5);
        BAssertUtil.validateError(resultNegative, ++i, "all match patterns should contain the same set of variables",
                64, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 64, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 64, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachableCode, 69, 5);
        BAssertUtil.validateError(resultNegative, ++i, "all match patterns should contain the same set of variables",
                71, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 71, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 71, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'c'", 71, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 71, 39);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 71, 39);
        BAssertUtil.validateError(resultNegative, ++i, unreachableCode, 78, 5);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 86, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 86, 22);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 88, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 90, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 90, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 92, 18);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 94, 18);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 98, 9);
        BAssertUtil.validateHint(resultNegative, ++i, "unnecessary condition: expression will always evaluate to " +
                "'true'", 100, 24);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 102, 9);
        BAssertUtil.validateHint(resultNegative, ++i, "unnecessary condition: expression will always evaluate to " +
                "'true'", 102, 24);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 109, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 109, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 110, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'c'", 110, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'd'", 110, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 116, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 116, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 117, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'c'", 117, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'd'", 117, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'x'", 123, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'y'", 123, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 124, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 124, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 124, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'c'", 124, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 130, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 130, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'c'", 130, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 131, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'x'", 131, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'y'", 131, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'z'", 131, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 139, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 139, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 141, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 150, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 150, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 152, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 162, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 162, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 164, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 173, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 184, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 191, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'x'", 191, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'y'", 191, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 197, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 197, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'c'", 197, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'd'", 197, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 199, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 199, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 199, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'c'", 199, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'd'", 199, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 201, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'a'", 201, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 201, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 216, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 225, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 233, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 242, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 251, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 260, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 273, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 282, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 290, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 299, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 308, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 317, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 333, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 342, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 351, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 364, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 373, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 382, 9);
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 400, 5);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 408, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 417, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 427, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unreachable pattern", 436, 9);
    }

    @Test(description = "test negative semantics")
    public void testNegativeSemantics() {
        int i = -1;
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "same variable cannot repeat in a match pattern", 20,
                17);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "redeclared symbol 'a'", 20, 21);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "same variable cannot repeat in a match pattern", 21,
                17);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "redeclared symbol 'a'", 21, 22);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected 'string[]', found " +
                "'int[]'", 28, 26);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected 'int[][]', found '" +
                "(int|error)[][]'", 29, 27);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected 'int', found 'json'",
                37, 21);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected 'boolean[]', found " +
                        "'json[]'", 37, 24);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected 'int', found 'json'",
                40, 21);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected 'boolean', found 'json'",
                40, 25);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected 'int', found 'string'",
                                  55, 21);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected 'string?', " +
                                          "found 'int'", 58, 25);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected '()'," +
                        " found '(int|string)'", 61, 20);
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), i + 1);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
        resultSemanticsNegative = null;
        restMatchPatternResult = null;
    }
}
