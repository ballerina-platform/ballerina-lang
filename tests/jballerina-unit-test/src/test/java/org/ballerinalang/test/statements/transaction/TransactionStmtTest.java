/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.transaction;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in TransactionStatement.
 */

@Test(enabled = false)
public class TransactionStmtTest {

    private CompileResult programFile;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
//        programFile = BCompileUtil.compile("test-src/statements/transaction/transaction_stmt.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/transaction/transaction_stmt_negative.bal");
    }

//    @Test
    public void testTransactionStmtSuccess() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx rc:1 end");
    }

    @Test(description = "Test transaction statement with errors")
    public void testTransactionNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 9);
        BAssertUtil.validateError(resultNegative, 0, "invalid transaction commit count",
                5, 5);
        BAssertUtil.validateError(resultNegative, 1, "rollback not allowed here",
                11, 9);
        BAssertUtil.validateError(resultNegative, 2, "transaction statement cannot be used " +
                        "within a transactional scope", 20, 5);
        BAssertUtil.validateError(resultNegative, 3, "usage of start within a transactional " +
                "scope is prohibited", 29, 19);
        BAssertUtil.validateError(resultNegative, 4, "usage of start within a transactional " +
                "scope is prohibited", 38, 21);
        BAssertUtil.validateError(resultNegative, 5, "invoking transactional function outside " +
                        "transactional scope is prohibited", 40, 15);
        BAssertUtil.validateError(resultNegative, 6, "commit not allowed here",
                59, 17);
        BAssertUtil.validateError(resultNegative, 7, "invoking transactional function outside " +
                        "transactional scope is prohibited", 70, 21);
        BAssertUtil.validateError(resultNegative, 8, "commit not allowed here",
                73, 17);
    }
}
