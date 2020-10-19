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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the const-pattern.
 *
 * @since 2.0.0
 */
@Test(groups = {"disableOnOldParser"})
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

    @Test
    public void testListMatchPattern1() {
        BRunUtil.invoke(result, "testListMatchPattern1");
    }

    @Test
    public void testListMatchPattern2() {
        BRunUtil.invoke(result, "testListMatchPattern2");
    }

    @Test
    public void testListMatchPattern3() {
        BRunUtil.invoke(result, "testListMatchPattern3");
    }

    @Test
    public void testListMatchPattern4() {
        BRunUtil.invoke(result, "testListMatchPattern4");
    }

    @Test
    public void testListMatchPattern5() {
        BRunUtil.invoke(result, "testListMatchPattern5");
    }

    @Test
    public void testListMatchPattern6() {
        BRunUtil.invoke(result, "testListMatchPattern6");
    }

    @Test
    public void testListMatchPattern7() {
        BRunUtil.invoke(result, "testListMatchPattern7");
    }

    @Test
    public void testListMatchPattern8() {
        BRunUtil.invoke(result, "testListMatchPattern8");
    }

    @Test
    public void testListMatchPattern9() {
        BRunUtil.invoke(result, "testListMatchPattern9");
    }

    @Test
    public void testListMatchPattern10() {
        BRunUtil.invoke(result, "testListMatchPattern10");
    }

    @Test
    public void testListMatchPattern11() {
        BRunUtil.invoke(result, "testListMatchPattern11");
    }

    @Test
    public void testListMatchPattern12() {
        BRunUtil.invoke(result, "testListMatchPattern12");
    }

    @Test
    public void testListMatchPattern13() {
        BRunUtil.invoke(result, "testListMatchPattern13");
    }

    @Test
    public void testRestMatchPattern1() {
        BRunUtil.invoke(restMatchPatternResult, "testListMatchPatternWithRest1");
    }

    @Test
    public void testRestMatchPattern2() {
        BRunUtil.invoke(restMatchPatternResult, "testListMatchPatternWithRest2");
    }

    @Test
    public void testRestMatchPattern3() {
        BRunUtil.invoke(restMatchPatternResult, "testListMatchPatternWithRest3");
    }

    @Test
    public void testRestMatchPattern4() {
        BRunUtil.invoke(restMatchPatternResult, "testListMatchPatternWithRest4");
    }

    @Test(description = "invalid match patterns")
    public void testListMatchPatternNegative() {
        Assert.assertEquals(resultNegative.getErrorCount(), 12);

        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 23, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachableCode, 28, 5);
        BAssertUtil.validateError(resultNegative, ++i, "all match patterns should contain the same set of variables",
                30, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 30, 17);
        BAssertUtil.validateError(resultNegative, ++i, unreachableCode, 35, 5);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 37, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 40, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 47, 13);
        BAssertUtil.validateError(resultNegative, ++i, unreachableCode, 52, 5);
        BAssertUtil.validateError(resultNegative, ++i, "all match patterns should contain the same set of variables",
                58, 9);
        BAssertUtil.validateError(resultNegative, ++i, "all match patterns should contain the same set of variables",
                65, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 65, 9);
    }

    @Test(description = "test negative semantics")
    public void testNegativeSemantics() {
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), 2);

        int i = -1;
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "same variable cannot repeat in a match pattern", 20,
                17);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "same variable cannot repeat in a match pattern", 21,
                17);
    }
}
