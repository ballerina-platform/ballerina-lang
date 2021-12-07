// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the mapping-match-pattern.
 *
 * @since Swan Lake
 */
public class MatchStmtMappingMatchPatternNegativeTest {

    private CompileResult negativeResult, warningResult;
    private String patternNotMatched = "pattern will not be matched";
    private String unreachablePattern = "unreachable pattern";

    @BeforeClass
    public void setup() {
        negativeResult = BCompileUtil.compile(
                "test-src/statements/matchstmt/mapping_match_pattern_semantic_negative.bal");
        warningResult = BCompileUtil.compile("test-src/statements/matchstmt/mapping-mach-pattern-negative.bal");
    }

    @Test
    public void testMappingMatchPatternNegative() {
        int i = 0;
        BAssertUtil.validateWarning(warningResult, i++, patternNotMatched, 23, 9);
        BAssertUtil.validateWarning(warningResult, i++, patternNotMatched, 30, 9);
        BAssertUtil.validateWarning(warningResult, i++, patternNotMatched, 33, 9);
        BAssertUtil.validateWarning(warningResult, i++, patternNotMatched, 33, 26);
        BAssertUtil.validateWarning(warningResult, i++, patternNotMatched, 36, 9);
        BAssertUtil.validateWarning(warningResult, i++, patternNotMatched, 43, 9);
        BAssertUtil.validateWarning(warningResult, i++, patternNotMatched, 46, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'a'", 60, 9);
        BAssertUtil.validateWarning(warningResult, i++, unreachablePattern, 60, 28);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'a'", 62, 9);
        BAssertUtil.validateWarning(warningResult, i++, unreachablePattern, 64, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'a'", 64, 9);
        BAssertUtil.validateWarning(warningResult, i++, unreachablePattern, 66, 26);
        BAssertUtil.validateWarning(warningResult, i++, unreachablePattern, 68, 24);
        BAssertUtil.validateWarning(warningResult, i++, unreachablePattern, 72, 9);
        BAssertUtil.validateHint(warningResult, i++, "unnecessary condition: expression will always evaluate to " +
                "'true'", 74, 30);
        BAssertUtil.validateWarning(warningResult, i++, unreachablePattern, 76, 9);
        BAssertUtil.validateHint(warningResult, i++, "unnecessary condition: expression will always evaluate to " +
                "'true'", 76, 30);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'a'", 83, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'b'", 83, 9);
        BAssertUtil.validateWarning(warningResult, i++, unreachablePattern, 84, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'c'", 84, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'd'", 84, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'a'", 90, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'b'", 90, 9);
        BAssertUtil.validateWarning(warningResult, i++, unreachablePattern, 91, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'c'", 91, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'd'", 91, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'a'", 97, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'b'", 97, 9);
        BAssertUtil.validateWarning(warningResult, i++, unreachablePattern, 98, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'a'", 98, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'b'", 98, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'c'", 98, 9);
        BAssertUtil.validateWarning(warningResult, i++, patternNotMatched, 110, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'a'", 110, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'rest'", 110, 9);
        BAssertUtil.validateWarning(warningResult, i++, patternNotMatched, 117, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'rest'", 117, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'x'", 117, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'id'", 119, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'name'", 119, 9);
        BAssertUtil.validateWarning(warningResult, i++, "unused variable 'rest'", 119, 9);
        Assert.assertEquals(warningResult.getWarnCount(), i - 2);
        Assert.assertEquals(warningResult.getHintCount(), 2);
    }

    @Test
    public void testMappingMatchPatternTypeNegative() {
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
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'boolean', found 'int'", 47, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<boolean>', " +
                "found 'record {| never id?; string name; boolean employed; never...; |}'", 48, 30);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 63, 24);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'boolean', " +
                        "found 'record {| never i?; never...; |}'",
                64, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', " +
                "found 'record {| int i?; never...; |}'", 67, 24);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        negativeResult = null;
        warningResult = null;
    }
}
