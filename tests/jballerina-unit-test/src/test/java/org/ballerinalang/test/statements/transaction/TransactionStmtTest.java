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
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in TransactionStatement.
 */

@Test(enabled = false)
public class TransactionStmtTest {

//    @Test
    public void testTransactionStmtSuccess() {
        CompileResult programFile =
                BCompileUtil.compile("test-src/statements/transaction/transaction_stmt.bal");
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx rc:1 end");
    }

    @Test(description = "Test transaction statement with errors")
    public void testTransactionNegativeCases() {
        CompileResult resultNegative =
                BCompileUtil.compile("test-src/statements/transaction/transaction_stmt_negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 21);
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
        BAssertUtil.validateError(resultNegative, 9, "rollback not allowed here",
                96, 17);
        BAssertUtil.validateError(resultNegative, 10, "rollback not allowed here",
                100, 9);
        BAssertUtil.validateError(resultNegative, 11, "commit not allowed here",
                102, 17);
        BAssertUtil.validateError(resultNegative, 12, "invalid transaction commit count",
                116, 9);
        BAssertUtil.validateError(resultNegative, 13, "break statement cannot be used to " +
                        "exit from a transaction without a commit or a rollback statement",
                118, 17);
        BAssertUtil.validateError(resultNegative, 14, "commit cannot be used outside " +
                "a transaction statement", 122, 13);
        BAssertUtil.validateError(resultNegative, 15, "continue statement cannot be used " +
                "to exit from a transaction without a commit or a rollback statement", 132, 17);
        BAssertUtil.validateError(resultNegative, 16, "return statement cannot be used to exit " +
                "from a transaction without a commit or a rollback statement", 147, 17);
        BAssertUtil.validateError(resultNegative, 17, "return statement cannot be used to exit " +
                "from a transaction without a commit or a rollback statement", 164, 13);
        BAssertUtil.validateError(resultNegative, 18, "return statement cannot be used to exit " +
                "from a transaction without a commit or a rollback statement", 184, 21);
        BAssertUtil.validateError(resultNegative, 19, "return statement cannot be used to exit " +
                "from a transaction without a commit or a rollback statement", 188, 21);
        BAssertUtil.validateError(resultNegative, 20, "invoking transactional function outside " +
                "transactional scope is prohibited", 207, 16);
    }
}
