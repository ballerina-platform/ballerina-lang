/*
 *   Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.transactionstatement;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in Transaction Statements.
 */
public class TransactionStmtNegativeTest {

    private CompileResult negativeFile;

    @BeforeClass
    public void setup() {
        negativeFile = BCompileUtil.compile(
            "test-src/statements/transactionstatement/trx-stmt-negative.bal");
    }

    @Test(description = "Check pre commit/rollback statements.")
    public void testNegative1() {
        int index = 0;
        BAssertUtil.validateError(negativeFile, index++, "commit not allowed here", 11, 15);
        BAssertUtil.validateError(negativeFile, index++,
            "premature exit detected before commit/rollback", 27, 5);
        BAssertUtil.validateError(negativeFile, index++, "invalid transaction commit count", 48, 5);
        BAssertUtil.validateError(negativeFile, index++,
            "premature exit detected before commit/rollback", 48, 5);
        BAssertUtil.validateError(negativeFile, index++, "invalid transaction commit count", 68, 5);
        BAssertUtil.validateError(negativeFile, index++,
            "premature exit detected before commit/rollback", 79, 8);
        BAssertUtil.validateError(negativeFile, index++, "rollback not allowed here", 122, 13);
        BAssertUtil.validateError(negativeFile, index++, "invalid transaction commit count", 139, 5);
        BAssertUtil.validateError(negativeFile, index++,
            "premature exit detected before commit/rollback", 139, 5);
        BAssertUtil.validateError(negativeFile, index++, "commit not allowed here", 162, 15);
        BAssertUtil.validateError(negativeFile, index++,
            "premature exit detected before commit/rollback", 180, 9);
        BAssertUtil.validateError(negativeFile, index++,
            "premature exit detected before commit/rollback", 207, 9);
        BAssertUtil.validateError(negativeFile, index++,
            "premature exit detected before commit/rollback", 220, 5);
        BAssertUtil.validateError(negativeFile, index++, "commit not allowed here", 229, 19);
        BAssertUtil.validateError(negativeFile, index++, "commit not allowed here", 244, 15);
        BAssertUtil.validateError(negativeFile, index++, "commit not allowed here", 258, 19);
        BAssertUtil.validateError(negativeFile, index++,
            "premature exit detected before commit/rollback", 266, 5);
        Assert.assertEquals(negativeFile.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        negativeFile = null;
    }
}
