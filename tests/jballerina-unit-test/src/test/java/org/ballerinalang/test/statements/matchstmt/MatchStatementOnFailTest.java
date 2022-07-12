/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test cases to verify the behaviour of the static/constant value patterns with match statement in Ballerina.
 *
 * @since 0.985.0
 */
public class MatchStatementOnFailTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/matchstmt_on_fail.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/matchstmt/matchstmt_on_fail_negative.bal");
    }

    @Test(description = "Test basics of static pattern match statement with fail statement")
    public void testStaticMatchPatternsWithFailStmt() {
        BRunUtil.invoke(result, "testStaticMatchPatternsWithFailStmt");
    }

    @Test
    public void testStaticMatchPatternsWithOnFailStmtWithoutVariable() {
        BRunUtil.invoke(result, "testStaticMatchPatternsWithOnFailStmtWithoutVariable");
    }

    @Test(description = "Test basics of static pattern match statement with check expression")
    public void testStaticMatchPatternsWithCheckExpr() {
         BRunUtil.invoke(result, "testStaticMatchPatternsWithCheckExpr");
    }

    @Test(description = "Test basics of static pattern match statement 2")
    public void testNestedMatchPatternsWithFail() {
        BRunUtil.invoke(result, "testNestedMatchPatternsWithFail");
    }

    @Test(description = "Test basics of static pattern match statement 2")
    public void testNestedMatchPatternsWithCheck() {
        BRunUtil.invoke(result, "testNestedMatchPatternsWithCheck");
    }

    @Test(description = "Test using var defined in match clause within on-fail")
    public void testVarInMatchPatternWithinOnfail() {
        BRunUtil.invoke(result, "testVarInMatchPatternWithinOnfail", new Object[]{});
    }

    @Test(description = "Check not incompatible types and reachable statements.")
    public void testNegative1() {
        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 29, 14);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'e'", 31, 7);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'err'", 56, 14);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 59, 7);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'e'", 59, 7);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'err'", 85, 14);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 88, 7);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'e'", 88, 7);
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 90, 9);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible error definition type: " +
                "'ErrorTypeA' will not be matched to 'ErrorTypeB'", 124, 7);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'e'", 124, 7);
        Assert.assertEquals(resultNegative.getDiagnostics().length, i + 1);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
