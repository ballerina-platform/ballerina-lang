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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases to verify the behaviour of the static/constant value patterns with match statement in Ballerina.
 *
 * @since 0.985.0
 */
public class MatchStatementOnFailTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/matchstmt/matchstmt_on_fail.bal");
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

    @Test(dataProvider = "onFailClauseWithErrorBPTestDataProvider")
    public void testOnFailWithErrorBP(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "onFailClauseWithErrorBPTestDataProvider")
    public Object[] onFailClauseWithErrorBPTestDataProvider() {
        return new Object[]{
                "testSimpleOnFailWithErrorBP",
                "testSimpleOnFailWithErrorBPWithVar",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithError",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithVar",
                "testOnFailWithErrorBPHavingUserDefinedType",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail1",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail2",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail3",
                "testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail4",
                "testOnFailWithErrorBPHavingAnonDetailRecord",
                "testOnFailWithErrorBPHavingAnonDetailRecordWithVar",
                "testOnFailWithErrorBPHavingAnonDetailRecordWithUnionType",
                "testOnFailWithErrorBPWithErrorArgsHavingBP1",
                "testOnFailWithErrorBPWithErrorArgsHavingBP2",
                "testOnFailWithErrorBPWithErrorArgsHavingBP3",
                "testOnFailWithErrorBPWithErrorArgsHavingBP4",
                "testOnFailWithErrorBPWithErrorArgsHavingBP5",
                "testMultiLevelOnFailWithErrorBP",
                "testMultiLevelOnFailWithoutErrorInOneLevel"
        };
    }

    @Test(description = "Check incompatible types.")
    public void testNegative1() {
        CompileResult resultNegative =
                BCompileUtil.compile("test-src/statements/matchstmt/matchstmt_on_fail_negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: " +
                "expected 'ErrorTypeA', found 'ErrorTypeB'", 30, 15);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: " +
                "expected 'ErrorTypeA', found 'ErrorTypeB'", 59, 15);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: " +
                "expected '(ErrorTypeA|ErrorTypeB)', found 'ErrorTypeB'", 94, 15);
        BAssertUtil.validateError(resultNegative, i++, "invalid error variable; expecting an error " +
                "type but found '(SampleComplexError|SampleError)' in type definition", 125, 15);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @Test(description = "Check reachable statements.")
    public void testNegative2() {
        CompileResult resultNegative =
                BCompileUtil.compile("test-src/statements/matchstmt/matchstmt_on_fail_negative_unreachable.bal");
        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 29, 14);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'e'", 31, 15);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'err'", 57, 14);
        BAssertUtil.validateWarning(resultNegative, ++i, "unused variable 'e'", 60, 15);
        BAssertUtil.validateError(resultNegative, ++i, "unreachable code", 62, 9);
        Assert.assertEquals(resultNegative.getDiagnostics().length, i + 1);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
