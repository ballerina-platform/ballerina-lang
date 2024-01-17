/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.statements.checkingstatement;

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
 * This contains methods to test checking statement.
 *
 * @since 2.2.0
 */
public class CheckingStmtTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/checkingstatement/checking-stmt.bal");
    }

    @Test(dataProvider = "testCheckingStmtDataProvider")
    public void testCheckingStmt(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] testCheckingStmtDataProvider() {
        return new Object[]{
                "testInvocationExpr",
                "testNilLiteral",
                "testVariableReference",
                "testFieldAccessExpr",
                "testMemberAccessExpr"
        };
    }

    @Test(description = "The static type of the checking-expr must be nil.")
    public void testNegative() {
        CompileResult negativeCompileResult = BCompileUtil.compile(
                "test-src/statements/checkingstatement/checking-stmt-negative.bal");

        int i = 0;
        BAssertUtil.validateError(negativeCompileResult, i, "variable assignment is required", 2, 5);
        BAssertUtil.validateError(negativeCompileResult, i += 2, "variable assignment is required", 3, 5);
        BAssertUtil.validateError(negativeCompileResult, i += 3, "variable assignment is required", 4, 5);
        BAssertUtil.validateError(negativeCompileResult, i += 2, "variable assignment is required", 5, 5);
        BAssertUtil.validateError(negativeCompileResult, i += 2, "variable assignment is required", 6, 5);
        BAssertUtil.validateError(negativeCompileResult, i += 2, "variable assignment is required", 7, 5);
        BAssertUtil.validateError(negativeCompileResult, i += 2, "variable assignment is required", 9, 5);
        BAssertUtil.validateError(negativeCompileResult, i + 1, "variable assignment is required", 11, 5);
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 8);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
