/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class MappingBindingPatternNegativeTest {
    private CompileResult warningResult, negativeResult;
    private String patternNotMatched = "pattern will not be matched";
    private String unreachablePattern = "unreachable pattern";

    @BeforeClass
    public void setup() {
        warningResult = BCompileUtil.compile("test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                "/mapping_binding_pattern_negative.bal");
        negativeResult = BCompileUtil.compile(
                "test-src/statements/matchstmt/varbindingpatternmatchpattern" +
                        "/mapping_binding_pattern_semantics_negative.bal");
    }

    @Test
    public void testMappingBindingPatternNegative() {
        int i = -1;
        BAssertUtil.validateWarning(warningResult, ++i, patternNotMatched, 20, 9);
        BAssertUtil.validateWarning(warningResult, ++i, patternNotMatched, 27, 9);
        BAssertUtil.validateWarning(warningResult, ++i, unreachablePattern, 38, 28);
        BAssertUtil.validateWarning(warningResult, ++i, unreachablePattern, 42, 9);
        BAssertUtil.validateWarning(warningResult, ++i, unreachablePattern, 46, 9);
        BAssertUtil.validateWarning(warningResult, ++i, unreachablePattern, 54, 9);
        BAssertUtil.validateWarning(warningResult, ++i, unreachablePattern, 61, 9);
        BAssertUtil.validateWarning(warningResult, ++i, patternNotMatched, 73, 9);
        BAssertUtil.validateWarning(warningResult, ++i, unreachablePattern, 77, 9);
        BAssertUtil.validateWarning(warningResult, ++i, unreachablePattern, 90, 9);
        BAssertUtil.validateWarning(warningResult, ++i, patternNotMatched, 112, 9);
        Assert.assertEquals(warningResult.getWarnCount(), i + 1);
    }

    @Test
    public void testMappingBindingPatternSemanticNegative() {
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
        warningResult = null;
        negativeResult = null;
    }
}
