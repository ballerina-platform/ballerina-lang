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
package org.ballerinalang.test.statements.transaction;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for committed aborted handlers TransactionStatement.
 */
public class TransactionHandlerTest {

    private CompileResult programFile;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/transaction/transaction-handler-test.bal");
    }

    @Test
    public void testTransactionStmtWithNoHandlers() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithNoHandlers");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx end");
    }

    @Test
    public void testTransactionStmtCommitWithCommitHandler() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtCommitWithCommitHandler");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx incommitFunction end");
    }

    @Test
    public void testTransactionAbortStmtWithAbortHandler() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionAbortStmtWithAbortHandler");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inAbortFunction end");
    }

    @Test
    public void testTransactionAbortStmtWithNoAbortHandler() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionAbortStmtWithNoAbortHandler");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx end");
    }

    @Test
    public void testTransactionAbortStmtWithCommitHandler() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionAbortStmtWithCommitHandler");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx end");
    }

    @Test
    public void testTransactionAbortStmtWithAllHandlers() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionAbortStmtWithAllHandlers");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inAbortFunction end");
    }

    @Test
    public void testTransactionCommitStmtWithAllHandlers() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionCommitStmtWithAllHandlers");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx incommitFunction end");
    }

    @Test
    public void testTransactionThrowWithAllHandlers() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionThrowWithAllHandlers");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inTrx inFailed inTrx inFailed inTrx inFailed inTrx inFailed inAbortFunction trxErr end");
    }

    @Test
    public void testTransactionCommitAfterFailureWithAllHandlers() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionCommitAfterFailureWithAllHandlers");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inTrx inFailed inTrx inFailed inTrx endTrx incommitFunction end");
    }

    @Test
    public void testMultipleTransactionsWithAllHandlers() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionsWithAllHandlers");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inFirstTrx endFirstTrx incommitFunction inSecondTrx endSecondTrx incommitFunctionSecond end");
    }

    @Test
    public void testMultipleTransactionsFailedWithAllHandlers() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionsFailedWithAllHandlers");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inFirstTrx beforeRetry-First inAbortFunction trxErr inSecondTrx inAbortFunctionSecond end");
    }

    @Test
    public void testMultipleTransactionsWithAllHandlersWithID() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionsWithAllHandlersWithID");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(returns[0].stringValue(),
                "start inFirstTrx endFirstTrx incommitFunction inSecondTrx endSecondTrx incommitFunctionSecond end");
        String tx1ID = returns[1].stringValue();
        String tx1HandlerID = returns[2].stringValue();
        Assert.assertTrue(tx1ID.equals(tx1HandlerID));
        String tx2ID = returns[3].stringValue();
        String tx2HandlerID = returns[4].stringValue();
        Assert.assertTrue(tx2ID.equals(tx2HandlerID));
    }

    @Test
    public void testMultipleTransactionsFailedWithAllHandlersWithID() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionsFailedWithAllHandlersWithID");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(returns[0].stringValue(),
                "start inFirstTrx beforeRetry-First inAbortFunction trxErr inSecondTrx inAbortFunctionSecond end");
        String tx1ID = returns[1].stringValue();
        String tx1HandlerID = returns[2].stringValue();
        Assert.assertTrue(tx1ID.equals(tx1HandlerID));
        String tx2ID = returns[3].stringValue();
        String tx2HandlerID = returns[4].stringValue();
        Assert.assertTrue(tx2ID.equals(tx2HandlerID));
    }

    @Test(description = "Test transaction handler function with invalid argument")
    public void testInvalidHandlers() {
        CompileResult res = BCompileUtil.compile("test-src/statements/transaction/transaction-handler-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 10);
        BAssertUtil.validateError(res, 0,
                "transaction handler function required single string parameter which is transaction id", 4, 50);
        BAssertUtil.validateError(res, 1,
                "transaction handler function required single string parameter which is transaction id", 4, 70);
        BAssertUtil.validateError(res, 2,
                "transaction handler function required single string parameter which is transaction id", 29, 46);
        BAssertUtil.validateError(res, 3,
                "transaction handler function required single string parameter which is transaction id", 29, 71);
        BAssertUtil.validateError(res, 4, "undefined symbol 'commitFunction3'", 54, 46);
        BAssertUtil
                .validateError(res, 5, "lambda function with string input parameter is required as transaction handler",
                        54, 46);
        BAssertUtil.validateError(res, 6, "undefined symbol 'abortFunction3'", 54, 71);
        BAssertUtil
                .validateError(res, 7, "lambda function with string input parameter is required as transaction handler",
                        54, 71);
        BAssertUtil
                .validateError(res, 8, "lambda function with string input parameter is required as transaction handler",
                        71, 46);
        BAssertUtil
                .validateError(res, 9, "lambda function with string input parameter is required as transaction handler",
                        71, 57);
    }
}
