/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.retrystmt;

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
 * Test cases for flow validation in Retry Statement.
 */
public class RetryStmtWithOnFailTest {

    private CompileResult programFile, negativeFile1;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/retrystmt/retry_stmt_with_on_fail.bal");
        negativeFile1 = BCompileUtil.compile("test-src/statements/retrystmt/retry_on_fail_negative.bal");
    }

    @Test
    public void testRetryStatement() {
        Object[] params = {};
        BRunUtil.invoke(programFile, "testRetryStatement", params);
    }

    @Test(dataProvider = "onFailClauseWithErrorBPTestDataProvider")
    public void testOnFailWithErrorBP(String funcName) {
        BRunUtil.invoke(programFile, funcName);
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

    @Test(description = "Check not incompatible types.")
    public void testNegative1() {
        int index = 0;
        BAssertUtil.validateError(negativeFile1, index++, "incompatible types: " +
                "expected 'ErrorTypeA', found 'ErrorTypeB'", 14, 14);
        BAssertUtil.validateError(negativeFile1, index++, "incompatible types: " +
                "expected '(ErrorTypeA|ErrorTypeB)', found 'ErrorTypeA'", 36, 12);
        BAssertUtil.validateError(negativeFile1, index++, "invalid error variable; " +
                "expecting an error type but found '(SampleComplexError|SampleError)' in type definition", 66, 15);
        Assert.assertEquals(negativeFile1.getDiagnostics().length, index);
    }

    @Test(description = "Check unreachable statements.")
    public void testNegative2() {
        CompileResult negativeFile2 = BCompileUtil.compile(
                "test-src/statements/retrystmt/retry_on_fail_negative_unreachable.bal");
        int index = 0;
        BAssertUtil.validateError(negativeFile2, index++, "unreachable code", 34, 9);
        BAssertUtil.validateWarning(negativeFile2, index++, "unused variable 'e'", 35, 15);
        BAssertUtil.validateWarning(negativeFile2, index++, "unused variable 'e'", 45, 15);
        BAssertUtil.validateError(negativeFile2, index++, "unreachable code", 48, 9);
        BAssertUtil.validateWarning(negativeFile2, index++, "unused variable 'e1'", 62, 15);
        BAssertUtil.validateWarning(negativeFile2, index++, "unused variable 'resB'", 77, 9);
        Assert.assertEquals(negativeFile2.getDiagnostics().length, index);
    }

    @AfterClass
    public void tearDown() {
        programFile = null;
        negativeFile1 = null;
    }
}
