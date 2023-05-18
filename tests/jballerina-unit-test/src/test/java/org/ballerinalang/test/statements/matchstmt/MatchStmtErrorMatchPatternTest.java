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
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the error-match-pattern.
 *
 * @since 2.0.0
 */
public class MatchStmtErrorMatchPatternTest {
    private CompileResult result, restPatternResult, resultNegative, moduleResult;
    private String patternNotMatched = "pattern will not be matched";
    private String unreachablePattern = "unreachable pattern";
    private String unnecessaryCondition = "unnecessary condition: expression will always evaluate to 'true'";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/error_match_pattern.bal");
        restPatternResult = BCompileUtil.compile("test-src/statements/matchstmt" +
                "/error_match_pattern_with_rest_match_pattern.bal");
        moduleResult = BCompileUtil.compile("test-src/statements/matchstmt/error-match-project");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/error_match_pattern_negative.bal");
    }

    @Test
    public void testErrorMatchPattern1() {
        BRunUtil.invoke(result, "testErrorMatchPattern1");
    }

    @Test
    public void testErrorMatchPattern2() {
        BRunUtil.invoke(result, "testErrorMatchPattern2");
    }

    @Test
    public void testErrorMatchPattern3() {
        BRunUtil.invoke(result, "testErrorMatchPattern3");
    }

    @Test
    public void testErrorMatchPattern4() {
        BRunUtil.invoke(result, "testErrorMatchPattern4");
    }

    @Test
    public void testErrorMatchPattern5() {
        BRunUtil.invoke(result, "testErrorMatchPattern5");
    }

    @Test
    public void testErrorMatchPattern6() {
        BRunUtil.invoke(result, "testErrorMatchPattern6");
    }

    @Test
    public void testErrorMatchPattern7() {
        BRunUtil.invoke(result, "testErrorMatchPattern7");
    }

    @Test
    public void testErrorMatchPattern8() {
        BRunUtil.invoke(result, "testErrorMatchPattern8");
    }

    @Test
    public void testErrorMatchPattern9() {
        BRunUtil.invoke(result, "testErrorMatchPattern9");
    }

    @Test
    public void testErrorMatchPatter10() {
        BRunUtil.invoke(result, "testErrorMatchPattern10");
    }

    @Test
    public void testErrorMatchPatter11() {
        BRunUtil.invoke(result, "testErrorMatchPattern11");
    }

    @Test
    public void testErrorMatchPatter12() {
        BRunUtil.invoke(result, "testErrorMatchPattern12");
    }

    @Test
    public void testErrorMatchPatter13() {
        BRunUtil.invoke(result, "testErrorMatchPattern13");
    }

    @Test
    public void testErrorMatchPatter14() {
        BRunUtil.invoke(result, "testErrorMatchPattern14");
    }

    @Test
    public void testErrorMatchPattern15() {
        BRunUtil.invoke(result, "testErrorMatchPattern15");
    }

    @Test
    public void testErrorMatchPattern16() {
        BRunUtil.invoke(result, "testErrorMatchPattern16");
    }

    @Test
    public void testErrorMatchPattern17() {
        BRunUtil.invoke(result, "testErrorMatchPattern17");
    }

    @Test
    public void testErrorMatchPatternWithRestPattern1() {
        BRunUtil.invoke(restPatternResult, "testErrorMatchPattern1");
    }

    @Test
    public void testErrorMatchPatternWithRestPattern2() {
        BRunUtil.invoke(restPatternResult, "testErrorMatchPattern2");
    }

    @Test
    public void testErrorMatchPatternWithRestPattern3() {
        BRunUtil.invoke(restPatternResult, "testErrorMatchPattern3");
    }

    @Test
    public void testErrorMatchWithQualifiedReference() {
        BRunUtil.invoke(moduleResult, "testErrorMatchWithQualifiedReference");
    }

    @Test
    public void testErrorMatchPatternNegative() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        //Assert.assertEquals(resultNegative.getWarnCount(), 10);
        int i = 0;
        BAssertUtil.validateWarning(resultNegative, i++, patternNotMatched, 31, 9);
        BAssertUtil.validateWarning(resultNegative, i++, patternNotMatched, 36, 9);
        BAssertUtil.validateWarning(resultNegative, i++, patternNotMatched, 41, 9);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 48, 19);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 49, 28);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 50, 20);
        BAssertUtil.validateError(resultNegative, i++, "all match patterns should contain the same set of variables",
                51, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'a'", 51, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'b'", 51, 9);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 51, 24);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 52, 42);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 53, 49);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 55, 44);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorA'", 62, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorB'", 63, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorC'", 68, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorD'", 69, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorA'", 76, 9);
        BAssertUtil.validateHint(resultNegative, i++, unnecessaryCondition, 76, 23);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 78, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorB'", 78, 9);
        BAssertUtil.validateHint(resultNegative, i++, unnecessaryCondition, 78, 23);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorC'", 84, 9);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 85, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorD'", 85, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorA'", 92, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorB'", 93, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorC'", 98, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorD'", 99, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorA'", 106, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorB'", 107, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorC'", 112, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorD'", 113, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorA'", 120, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorB'", 121, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorC'", 126, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'errorD'", 127, 9);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        restPatternResult = null;
        resultNegative = null;
        moduleResult = null;
    }
}
