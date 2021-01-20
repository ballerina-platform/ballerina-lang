/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.statements.matchstmt.varbindingpatternmatchpattern;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the var binding-pattern list-binding-pattern.
 *
 * @since 2.0.0
 */
@Test(groups = { "disableOnOldParser" })
public class ListBindingPatternTest {
    private CompileResult result, restMatchPatternResult, resultNegative;
    private String patternNotMatched = "pattern will not be matched";
    private String unreachablePattern = "unreachable pattern";
    private String unreachableCode = "unreachable code";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/list_binding_pattern.bal");
        restMatchPatternResult = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/list_binding_pattern_with_rest_binding_pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/list_binding_pattern_negative.bal");
    }

    @Test
    public void testListBindingPattern1() {
        BRunUtil.invoke(result, "testListBindingPattern1");
    }

    @Test
    public void testListBindingPattern2() {
        BRunUtil.invoke(result, "testListBindingPattern2");
    }

    @Test
    public void testListBindingPattern3() {
        BRunUtil.invoke(result, "testListBindingPattern3");
    }

    @Test
    public void testListBindingPattern4() {
        BRunUtil.invoke(result, "testListBindingPattern4");
    }

    @Test
    public void testListBindingPattern5() {
        BRunUtil.invoke(result, "testListBindingPattern5");
    }

    @Test
    public void testListBindingPattern6() {
        BRunUtil.invoke(result, "testListBindingPattern6");
    }

    @Test
    public void testListBindingPattern7() {
        BRunUtil.invoke(result, "testListBindingPattern7");
    }

    @Test
    public void testListBindingPattern8() {
        BRunUtil.invoke(result, "testListBindingPattern8");
    }

    @Test
    public void testListBindingPattern9() {
        BRunUtil.invoke(result, "testListBindingPattern9");
    }

    @Test
    public void testListBindingPattern10() {
        BRunUtil.invoke(result, "testListBindingPattern10");
    }

    @Test
    public void testListBindingPattern11() {
        BRunUtil.invoke(result, "testListBindingPattern11");
    }

    @Test
    public void testListBindingPattern12() {
        BRunUtil.invoke(result, "testListBindingPattern12");
    }

    @Test
    public void testListBindingPattern13() {
        BRunUtil.invoke(result, "testListBindingPattern13");
    }

    @Test
    public void testListBindingPattern14() {
        BRunUtil.invoke(result, "testListBindingPattern14");
    }

    @Test
    public void testListBindingPattern15() {
        BRunUtil.invoke(result, "testListBindingPattern15");
    }

    @Test
    public void testListBindingPattern16() {
        BRunUtil.invoke(result, "testListBindingPattern16");
    }

    @Test
    public void testListBindingPattern17() {
        BRunUtil.invoke(result, "testListBindingPattern17");
    }

    @Test
    public void testListBindingPattern18() {
        BRunUtil.invoke(result, "testListBindingPattern18");
    }

    @Test
    public void testRestBindingPattern1() {
        BRunUtil.invoke(restMatchPatternResult, "testListBindingPatternWithRest1");
    }

    @Test
    public void testRestBindingPattern2() {
        BRunUtil.invoke(restMatchPatternResult, "testListBindingPatternWithRest2");
    }

    @Test
    public void testRestBindingPattern3() {
        BRunUtil.invoke(restMatchPatternResult, "testListBindingPatternWithRest3");
    }

    @Test
    public void testRestBindingPattern4() {
        BRunUtil.invoke(restMatchPatternResult, "testListBindingPatternWithRest4");
    }

    @Test
    public void testRestBindingPattern5() {
        BRunUtil.invoke(restMatchPatternResult, "testListBindingPatternWithRest5");
    }

    @Test
    public void testRestBindingPattern6() {
        BRunUtil.invoke(restMatchPatternResult, "testListBindingPatternWithRest6");
    }

    @Test
    public void testListBindingPatternNegative() {
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
        BAssertUtil.validateError(resultNegative, ++i, unreachableCode, 63, 5);
        BAssertUtil.validateError(resultNegative, ++i, "all match patterns should contain the same set of variables",
                65, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 65, 35);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 65, 35);
        BAssertUtil.validateError(resultNegative, ++i, unreachableCode, 71, 5);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 79, 22);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 79, 22);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 81, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 81, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 83, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 83, 9);
        BAssertUtil.validateError(resultNegative, ++i, patternNotMatched, 85, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 85, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 85, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 93, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 100, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 107, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 114, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 124, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 135, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 146, 9);
        BAssertUtil.validateError(resultNegative, ++i, unreachablePattern, 166, 9);

        Assert.assertEquals(resultNegative.getErrorCount(), i + 1);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        restMatchPatternResult = null;
        resultNegative = null;
    }
}
