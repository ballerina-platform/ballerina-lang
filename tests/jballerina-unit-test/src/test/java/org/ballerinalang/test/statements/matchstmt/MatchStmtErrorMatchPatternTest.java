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
    private CompileResult result, restPatternResult, resultNegative;
    private String patternNotMatched = "pattern will not be matched";
    private String unreachablePattern = "unreachable pattern";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/error_match_pattern.bal");
        restPatternResult = BCompileUtil.compile("test-src/statements/matchstmt" +
                "/error_match_pattern_with_rest_match_pattern.bal");
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
    public void testErrorMatchPatternNegative() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        //Assert.assertEquals(resultNegative.getWarnCount(), 10);
        int i = 0;
        BAssertUtil.validateWarning(resultNegative, i++, patternNotMatched, 23, 9);
        BAssertUtil.validateWarning(resultNegative, i++, patternNotMatched, 28, 9);
        BAssertUtil.validateWarning(resultNegative, i++, patternNotMatched, 33, 9);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 40, 19);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 41, 28);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 42, 20);
        BAssertUtil.validateError(resultNegative, i++, "all match patterns should contain the same set of variables",
                43, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'a'", 43, 9);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'b'", 43, 9);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 43, 24);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 44, 42);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 45, 49);
        BAssertUtil.validateWarning(resultNegative, i++, unreachablePattern, 47, 44);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        restPatternResult = null;
        resultNegative = null;
    }
}
