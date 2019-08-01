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
public class TransactionStmtFlowTest {

    private CompileResult programFile;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/transaction/transaction_stmt.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/transaction/transaction_stmt_negative.bal");
    }

    @Test
    public void testTransactionStmtSuccess() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx rc:1 end");
    }

    @Test
    public void testTransactionAbortStatement() {
        BValue[] returns = BRunUtil.invoke(programFile, "testAbortStatement");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "BeforeTR WithinTR BeforAbort AfterTR ");
    }

    @Test
    public void testTransactionStmtAbort() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort rc:1 end");
    }

    @Test
    public void testPanicInTransactionStmt() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start err rc:3 end");
    }

    @Test
    public void testPanicInTransactionStmtWithNamedError() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start trxErr rc:3 end");
    }


    @Test
    public void testTransactionSuccess() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx end");
    }

    @Test
    public void testTransactionAbort() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort end");
    }

    @Test
    public void testTrappedPanicInTransactionStmt() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx err endTrx end");
    }

    @Test
    public void testTrappedPanicInTransactionStmtWithNamedError() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx end");
    }

    @Test
    public void testTransactionStmtWithFailedAndNonDefaultRetriesFail() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailedAndNonDefaultRetries", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start err rc:4 end");
    }

    @Test
    public void testTransactionStmtWithNonDefaultRetriesAbort() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailedAndNonDefaultRetries", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort rc:1 end");
    }

    @Test
    public void testTransactionStmtWithNamedErrorAndNonDefaultRetriesFail() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailedAndNonDefaultRetries", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start trxErr rc:4 end");
    }

    @Test
    public void testTransactionStmtWithNonDefaultRetriesSuccess() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailedAndNonDefaultRetries", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrxstart inTrx success endTrx rc:1 end");
    }

    @Test
    public void testTransactionStmtWithRetryOff() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithRetryOff", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx err endTrx end");
    }

    @Test
    public void testTransactionStmtWithConstRetryFailed() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithConstRetryFailed");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start err rc:4 end");
    }

    @Test
    public void testTransactionStmtWithInvalidRetryCountFail() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithConstRetryFailed2");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start invalid retry count end");
    }

    @Test
    public void testTransactionStmtWithConstRetrySuccess() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithConstRetrySuccess");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx end");
    }

    @Test
    public void testMultipleTransactionStmtSuccess() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionStmtSuccess");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inFirstTrxBlock inFirstTrxEnd inSecTrxBlock inFSecTrxEnd end");
    }

    @Test
    public void testMultipleTransactionStmtFirstStmtFailCausingPanic() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionStmtFailed1");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inFirstTrxBlock inFirstTrxFld inFirstTrxBlock aborted err end");
    }

    @Test
    public void testMultipleTransactionStmtFirstFailSecondSuccess() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionStmtFailed2");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inFirstTrxBlock inFirstTrxFld inFirstTrxBlock err inSecTrxBlock inFSecTrxEnd end");
    }

    @Test()
    public void testTransactionWithBreakValid() {
        BValue[] returns = BRunUtil.invoke(programFile, "transactionWithBreak", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }

    @Test()
    public void testTransactionWithContinueValid() {
        BValue[] returns = BRunUtil.invoke(programFile, "transactionWithContinue", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }

    @Test()
    public void testTransactionStmtWithFail() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFail");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inTrx inFailed inTrx inFailed inTrx inFailed inTrx end");
    }

    @Test()
    public void testValidReturn() {
        BValue[] returns = BRunUtil.invoke(programFile, "testValidReturn");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inOuterTxstart  foo endOuterTx");
    }

    @Test(description = "Test transaction statement with errors")
    public void testTransactionNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 10);
        BAssertUtil.validateError(resultNegative, 0, "abort cannot be used outside of a transaction block", 3, 5);
        BAssertUtil.validateError(resultNegative, 1, "unreachable code", 12, 9);
        BAssertUtil.validateError(resultNegative, 2, "unreachable code", 27, 17);
        BAssertUtil.validateError(resultNegative, 3, "unreachable code", 31, 9);
        BAssertUtil
                .validateError(resultNegative, 4, "break statement cannot be used to exit from a transaction", 41, 17);
        BAssertUtil
                .validateError(
                        resultNegative, 5, "continue statement cannot be used to exit from a transaction", 54, 17);
        BAssertUtil
                .validateError(resultNegative, 6, "return statement cannot be used to exit from a transaction", 67, 17);
        BAssertUtil
                .validateError(resultNegative, 7, "return statement cannot be used to exit from a transaction", 82, 13);
        BAssertUtil
                .validateError(resultNegative, 8, "return statement cannot be used to exit from a transaction", 97, 21);
        BAssertUtil.validateError(resultNegative, 9, "return statement cannot be used to exit from a transaction", 101,
                21);
    }

    @Test(description = "Test transaction statement with errors")
    public void testRetryNegativeCases() {
        CompileResult res = BCompileUtil.compile("test-src/statements/transaction/transaction_retry_negative.bal");
        Assert.assertEquals(res.getErrorCount(), 4);
        BAssertUtil.validateError(res, 0, "invalid transaction retry count", 3, 32);
        BAssertUtil.validateError(res, 1, "invalid transaction retry count", 15, 32);
        BAssertUtil.validateError(res, 2, "incompatible types: expected 'int', found 'float'", 30, 32);
        BAssertUtil.validateError(res, 3, "invalid transaction retry count", 30, 32);
    }
}
