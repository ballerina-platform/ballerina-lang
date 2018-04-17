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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
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
        programFile = BCompileUtil.compile("test-src/statements/transaction/transaction-stmt.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/transaction/transaction-stmt-negative.bal");
    }

    @Test
    public void testTransactionStmt1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx end");
    }

    @Test
    public void testAbortStatement() {
        BValue[] returns = BRunUtil.invoke(programFile, "testAbortStatement");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "BeforeTR WithinTR BeforAbort AfterTR ");
    }

    @Test
    public void testTransactionStmt2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort end");
    }

    @Test
    public void testTransactionStmt3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inTrx inFailed inTrx inFailed err end");
    }

    @Test
    public void testTransactionStmt4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx end");
    }


    @Test
    public void testOptionalFailed1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx end");
    }

    @Test
    public void testOptionalFailed2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort end");
    }

    @Test
    public void testOptionalFailed3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx err end");
    }

    @Test
    public void testOptionalFailed4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx end");
    }

    @Test
    public void testNestedTransaction1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inOuterTrx inInnerTrx endInnerTrx endOuterTrx  end");
    }

    @Test
    public void testNestedTransaction2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inOuterTrx inInnerTrx abort endOuterTrx  end");
    }

    @Test(enabled = false)
    public void testNestedTransaction3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inOuterTrx inInnerTrx inInnerTrx inInnerTrx inOuterTrx "
                + "inInnerTrx inInnerTrx inInnerTrx inOuterTrx inInnerTrx inInnerTrx inInnerTrx err end");
    }

    @Test
    public void testNestedTransaction4() {
        BValue[] args = { new BInteger(-10) };
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx trxErr endInnerTrx endOuterTrx  end");
    }

    @Test(enabled = false)
    public void testNestedTransactionWithFailed1() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransactionWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inOuterTrx inInnerTrx innerFailed inInnerTrx innerFailed "
                + "outerFailed inOuterTrx inInnerTrx innerFailed inInnerTrx innerFailed outerFailed inOuterTrx "
                + "inInnerTrx innerFailed inInnerTrx innerFailed outerFailed err end");

    }

    @Test
    public void testNestedTransactionWithFailed2() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransactionWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx trxErr endInnerTrx endOuterTrx  end");
    }

    @Test
    public void testTransactionStmtWithFailedAndNonDefaultRetries1() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailedAndNonDefaultRetries", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inTrx inFailed inTrx inFailed inTrx "
                + "inFailed err end");
    }

    @Test
    public void testTransactionStmtWithFailedAndNonDefaultRetries2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailedAndNonDefaultRetries", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort end");
    }

    @Test
    public void testTransactionStmtWithFailedAndNonDefaultRetries3() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailedAndNonDefaultRetries", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx end");
    }

    @Test
    public void testTransactionStmtWithFailedAndNonDefaultRetries4() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailedAndNonDefaultRetries", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx success endTrx end");
    }

    @Test
    public void testTransactionStmtWithRetryOff() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithRetryOff", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed err end");
    }

    @Test
    public void testTransactionStmtWithConstRetryFailed() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithConstRetryFailed");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inTrx inFailed inTrx inFailed inTrx "
                + "inFailed err end");
    }

    @Test
    public void testTransactionStmtWithConstRetryFailed2() {
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
    public void testMultipleTransactionStmtFailed1() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionStmtFailed1");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inFirstTrxBlock inFirstTrxFld inFirstTrxBlock inFirstTrxFld err end");
    }

    @Test
    public void testMultipleTransactionStmtFailed2() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionStmtFailed2");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inFirstTrxBlock inFirstTrxFld inFirstTrxBlock inFirstTrxFld err inSecTrxBlock inFSecTrxEnd end");
    }

    @Test()
    public void testValidAbortAndReturn() {
        BValue[] returns = BRunUtil.invoke(programFile, "testAbort", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inOuterTrx inInnerTrx inOuterTrxEnd afterOuterTrx");
    }

    @Test()
    public void testTransactionWithBreakValid() {
        BValue[] returns = BRunUtil.invoke(programFile, "transactionWithBreak", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }

    @Test()
    public void testTransactionWithNextValid() {
        BValue[] returns = BRunUtil.invoke(programFile, "transactionWithNext", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }

    @Test()
    public void testTransactionStmtWithFail() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFail");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inTrx inFailed inTrx inFailed inTrx inFailed inTrx inFailed end");
    }

    @Test()
    public void testSimpleNestedTransactionAbort() {
        BValue[] returns = BRunUtil.invoke(programFile, "testSimpleNestedTransactionAbort");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inOuterTxstart  inInnerTxstart  abortingInnerTxstart  endOuterTxstart");
    }

    @Test()
    public void testValidReturn() {
        BValue[] returns = BRunUtil.invoke(programFile, "testValidReturn");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inOuterTxstart  inInnerTxstart  foo endInnerTx foo endOuterTx");
    }

    @Test()
    public void testValidDoneWithinTransaction() {
        BValue[] returns = BRunUtil.invoke(programFile, "testValidDoneWithinTransaction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  withinTx withinworker beforeDone endTx afterTx");
    }

    @Test(description = "Test transaction statement with errors")
    public void testTransactionNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 8);
        BAssertUtil.validateError(resultNegative, 0, "abort cannot be used outside of a transaction block", 3, 5);
        BAssertUtil.validateError(resultNegative, 1, "unreachable code", 12, 9);
        BAssertUtil.validateError(resultNegative, 2, "unreachable code", 27, 17);
        BAssertUtil.validateError(resultNegative, 3, "unreachable code", 31, 9);
        BAssertUtil
                .validateError(resultNegative, 4, "break statement cannot be used to exit from a transaction", 41, 17);
        BAssertUtil
                .validateError(resultNegative, 5, "next statement cannot be used to exit from a transaction", 54, 17);
        BAssertUtil
                .validateError(resultNegative, 6, "return statement cannot be used to exit from a transaction", 67, 17);
        BAssertUtil
                .validateError(resultNegative, 7, "done statement cannot be used to exit from a transaction", 82, 13);
    }

    @Test(description = "Test transaction statement with errors")
    public void testRetryNegativeCases() {
        CompileResult res = BCompileUtil.compile("test-src/statements/transaction/transaction-retry-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 4);
        BAssertUtil.validateError(res, 0, "invalid transaction retry count", 3, 32);
        BAssertUtil.validateError(res, 1, "invalid transaction retry count", 16, 32);
        BAssertUtil.validateError(res, 2, "incompatible types: expected 'int', found 'float'", 31, 32);
        BAssertUtil.validateError(res, 3, "invalid transaction retry count", 31, 32);
    }
}
