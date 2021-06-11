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
 * Test cases to verify the behaviour of the var binding-pattern mapping-binding-pattern.
 *
 * @since 2.0.0
 */
@Test
public class MappingBindingPatternTest {
    private CompileResult result, restMatchPatternResult, resultNegative;
    private String patternNotMatched = "pattern will not be matched";
    private String unreachablePattern = "unreachable pattern";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/mapping_binding_pattern.bal");
        restMatchPatternResult = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/mapping_binding_pattern_with_rest_binding_pattern.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/mapping_binding_pattern_negative.bal");
    }

    @Test
    public void testMappingBindingPattern1() {
        BRunUtil.invoke(result, "testMappingBindingPattern1");
    }

    @Test
    public void testMappingBindingPattern2() {
        BRunUtil.invoke(result, "testMappingBindingPattern2");
    }

    @Test
    public void testMappingBindingPattern3() {
        BRunUtil.invoke(result, "testMappingBindingPattern3");
    }

    @Test
    public void testMappingBindingPattern4() {
        BRunUtil.invoke(result, "testMappingBindingPattern4");
    }

    @Test
    public void testMappingBindingPattern5() {
        BRunUtil.invoke(result, "testMappingBindingPattern5");
    }

    @Test
    public void testMappingBindingPattern6() {
        BRunUtil.invoke(result, "testMappingBindingPattern6");
    }

    @Test
    public void testMappingBindingPattern7() {
        BRunUtil.invoke(result, "testMappingBindingPattern7");
    }

    @Test
    public void testMappingBindingPattern8() {
        BRunUtil.invoke(result, "testMappingBindingPattern8");
    }

    @Test
    public void testMappingBindingPattern9() {
        BRunUtil.invoke(result, "testMappingBindingPattern9");
    }

    @Test
    public void testMappingBindingPattern10() {
        BRunUtil.invoke(result, "testMappingBindingPattern10");
    }

    @Test
    public void testMappingBindingPattern11() {
        BRunUtil.invoke(result, "testMappingBindingPattern11");
    }

    @Test
    public void testMappingBindingPattern12() {
        BRunUtil.invoke(result, "testMappingBindingPattern12");
    }

    @Test
    public void testMappingBindingPattern13() {
        BRunUtil.invoke(result, "testMappingBindingPattern13");
    }

    @Test
    public void testMappingBindingPattern14() {
        BRunUtil.invoke(result, "testMappingBindingPattern14");
    }

    @Test
    public void testMappingBindingPattern15() {
        BRunUtil.invoke(result, "testMappingBindingPattern15");
    }

    @Test
    public void testMappingBindingPattern16() {
        BRunUtil.invoke(result, "testMappingBindingPattern16");
    }

    @Test
    public void testMappingBindingPattern17() {
        BRunUtil.invoke(result, "testMappingBindingPattern17");
    }

    @Test
    public void testMappingBindingPatternWithRest1() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest1");
    }

    @Test
    public void testMappingBindingPatternWithRest2() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest2");
    }

    @Test
    public void testMappingBindingPatternWithRest3() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest3");
    }

    @Test
    public void testMappingBindingPatternWithRest4() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest4");
    }

    @Test
    public void testMappingBindingPatternWithRest5() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest5");
    }

    @Test
    public void testMappingBindingPatternWithRest6() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest6");
    }

    @Test
    public void testMappingBindingPatternWithRest7() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest7");
    }

    @Test
    public void testMappingBindingPatternWithRest8() {
        BRunUtil.invoke(restMatchPatternResult, "testMappingBindingPatternWithRest8");
    }

    @Test
    public void testRestMappingAtRuntime() {
        BRunUtil.invoke(restMatchPatternResult, "testRestMappingAtRuntime");
    }

    @Test
    public void testRestRecordPattern() {
        BRunUtil.invoke(restMatchPatternResult, "testRestRecordPattern");
    }

    @Test
    public void testReachableMappingBinding() {
        BRunUtil.invoke(restMatchPatternResult, "testReachableMappingBinding");
    }

    @Test
    public void testMappingBindingPatternNegative() {
        int i = -1;
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 20, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 27, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 38, 28);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 42, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 46, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 54, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 61, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, patternNotMatched, 73, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 77, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 90, 9);
        Assert.assertEquals(resultNegative.getWarnCount(), i + 1);
    }

    @Test
    public void testMappingBindingPatternSemanticNegative() {
       CompileResult negativeResult = BCompileUtil.compile(
                "test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                        "/mapping_binding_pattern_semantics_negative.bal");;
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<error>', found 'map<int>'",
                20, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<map<int>>', " +
                "found 'record {| never x?; map<(int|error)>...; |}'", 21, 31);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'json'", 29, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<boolean>', " +
                "found 'record {| never x?; json...; |}'", 29, 24);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'json'", 32, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'boolean', found 'json'", 32, 28);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', " +
                "found 'record {| never a?; never b?; never c?; never...; |}'", 43, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'boolean', found 'int'", 57, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<boolean>', " +
                "found 'record {| never id?; string name; boolean employed; never...; |}'", 58, 30);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', " +
                "found 'int'", 73, 24);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'boolean', " +
                "found 'record {| never i?; never...; |}'", 74, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', " +
                "found 'record {| int i?; never...; |}'", 77, 24);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        restMatchPatternResult = null;
        resultNegative = null;
    }
}
