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
 * Test cases to verify the type narrowing in match-stmt.
 *
 * @since 2.0.0
 */
@Test
public class MatchStmtTypeNarrowingTest {

    private static final String ALWAYS_TRUE_HINT = "unnecessary condition: expression will always evaluate to 'true'";

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setUp() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/match-stmt-type-narrow.bal");
    }

    @Test
    public void testNarrowTypeInCaptureBindingPattern1() {
        BRunUtil.invoke(result, "testNarrowTypeInCaptureBindingPattern1");
    }

    @Test
    public void testNarrowTypeInCaptureBindingPattern2() {
        BRunUtil.invoke(result, "testNarrowTypeInCaptureBindingPattern2");
    }

    @Test
    public void testNarrowTypeInListBindingPattern1() {
        BRunUtil.invoke(result, "testNarrowTypeInListBindingPattern1");
    }

    @Test
    public void testNarrowTypeInListBindingPattern2() {
        BRunUtil.invoke(result, "testNarrowTypeInListBindingPattern2");
    }

    @Test
    public void testNarrowTypeInListBindingPattern3() {
        BRunUtil.invoke(result, "testNarrowTypeInListBindingPattern3");
    }

    @Test(dataProvider = "dataToTestMatchClauseWithTypeGuard", description = "Test match clause with type guard")
    public void testMatchClauseWithTypeGuard(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestMatchClauseWithTypeGuard() {
        return new Object[]{
                "testMatchClauseWithTypeGuard1",
                "testMatchClauseWithTypeGuard2",
                "testMatchClauseWithTypeGuard3",
                "testMatchClauseWithTypeGuard4",
                "testMatchClauseWithTypeGuard5",
                "testMatchClauseWithTypeGuard6",
                "testMatchClauseWithTypeGuard7",
                "testMatchClauseWithTypeGuard8",
                "testMatchClauseWithTypeGuard9",
                "testMatchClauseWithTypeGuard10",
                "testMatchClauseWithTypeGuard11",
                "testMatchClauseWithTypeGuard12"
        };
    }

    @Test(description = "Test match clause with type guard negative scenarios")
    public void testMatchClauseWithTypeGuardNegative() {
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/match_stmt_type_narrow_negative.bal");
        int i = -1;
        String unreachablePattern = "unreachable pattern";
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 24, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 37, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 47, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 57, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 70, 9);
        BAssertUtil.validateHint(resultNegative, ++i, ALWAYS_TRUE_HINT, 78, 18);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 80, 9);
        BAssertUtil.validateHint(resultNegative, ++i, ALWAYS_TRUE_HINT, 80, 18);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 90, 9);
        BAssertUtil.validateHint(resultNegative, ++i, ALWAYS_TRUE_HINT, 95, 18);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 97, 9);
        BAssertUtil.validateHint(resultNegative, ++i, ALWAYS_TRUE_HINT, 97, 18);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 110, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 112, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'b'", 112, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'name'", 117, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'rest'", 117, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 119, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'name'", 119, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'rest'", 119, 9);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 132, 9);
        BAssertUtil.validateHint(resultNegative, ++i, ALWAYS_TRUE_HINT, 137, 18);
        BAssertUtil.validateWarning(resultNegative, ++i, unreachablePattern, 139, 9);
        BAssertUtil.validateHint(resultNegative, ++i, ALWAYS_TRUE_HINT, 139, 18);
        Assert.assertEquals(resultNegative.getErrorCount(), 0);
        Assert.assertEquals(resultNegative.getHintCount(), 6);
        Assert.assertEquals(resultNegative.getWarnCount(), ++i - 6);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
