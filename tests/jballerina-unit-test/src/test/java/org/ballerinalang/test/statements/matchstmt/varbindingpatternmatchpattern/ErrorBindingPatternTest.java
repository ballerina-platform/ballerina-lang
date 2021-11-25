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
 * Test cases to verify the behaviour of the error-binding-pattern.
 *
 * @since 2.0.0
 */
public class ErrorBindingPatternTest {
    private CompileResult result, restPatternResult, resultNegative;
    private String patternNotMatched = "pattern will not be matched";

    @AfterClass
    public void tearDown() {
        result = null;
        restPatternResult = null;
        resultNegative = null;
    }

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/error_binding_pattern.bal");
        restPatternResult = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/error_binding_pattern_with_rest_binding_pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/error_binding_pattern_negative.bal");
    }

    @Test
    public void testNegativeSemantics() {
        CompileResult buildError = BCompileUtil.compile(
                "test-src/statements/matchstmt/varbindingpatternmatchpattern/error_type_ref_negative.bal");
        int i = -1;
        BAssertUtil.validateError(buildError, ++i, "invalid error binding pattern with type 'other'", 18, 11);
        BAssertUtil.validateError(buildError, ++i, "unknown type 'myError'", 18, 17);
        Assert.assertEquals(buildError.getErrorCount(), i + 1);
    }

    @Test
    public void testErrorBindingPattern1() {
        BRunUtil.invoke(result, "testErrorBindingPattern1");
    }

    @Test
    public void testErrorBindingPattern2() {
        BRunUtil.invoke(result, "testErrorBindingPattern2");
    }

    @Test
    public void testErrorBindingPattern3() {
        BRunUtil.invoke(result, "testErrorBindingPattern3");
    }

    @Test
    public void testErrorBindingPattern4() {
        BRunUtil.invoke(result, "testErrorBindingPattern4");
    }

    @Test
    public void testErrorBindingPattern5() {
        BRunUtil.invoke(result, "testErrorBindingPattern5");
    }

    @Test
    public void testErrorBindingPattern6() {
        BRunUtil.invoke(result, "testErrorBindingPattern6");
    }

    @Test
    public void testErrorBindingPattern7() {
        BRunUtil.invoke(result, "testErrorBindingPattern7");
    }

    @Test
    public void testErrorBindingPattern8() {
        BRunUtil.invoke(result, "testErrorBindingPattern8");
    }

    @Test
    public void testErrorBindingPattern9() {
        BRunUtil.invoke(result, "testErrorBindingPattern9");
    }

    @Test
    public void testErrorBindingPattern10() {
        BRunUtil.invoke(result, "testErrorBindingPattern10");
    }

    @Test
    public void testErrorBindingPatternWithRestBindingPattern1() {
        BRunUtil.invoke(restPatternResult, "testErrorBindingPattern1");
    }

    @Test
    public void testErrorBindingPatternWithRestBindingPattern2() {
        BRunUtil.invoke(restPatternResult, "testErrorBindingPattern2");
    }

    @Test
    public void testErrorBindingPatternWithRestBindingPattern3() {
        BRunUtil.invoke(restPatternResult, "testErrorBindingPattern3");
    }

    @Test
    public void testErrorBindingPatternNegative() {
        int i = 0;
        BAssertUtil.validateWarning(resultNegative, i++, patternNotMatched, 23, 9);
        BAssertUtil.validateWarning(resultNegative, i++, patternNotMatched, 28, 9);

        Assert.assertEquals(resultNegative.getWarnCount(), i);
    }
}
