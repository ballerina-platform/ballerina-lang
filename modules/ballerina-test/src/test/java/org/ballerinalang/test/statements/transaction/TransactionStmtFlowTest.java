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

    CompileResult programFile;
    CompileResult resultNegative;

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
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx inCmt end");
    }

    @Test
    public void testTransactionStmt2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort inAbt end");
    }

    @Test
    public void testTransactionStmt3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx inAbt err end");
    }

    @Test
    public void testTransactionStmt4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx inCmt end");
    }


    @Test
    public void testOptionalAborted1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx inCmt end");
    }

    @Test
    public void testOptionalAborted2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort end");
    }

    @Test
    public void testOptionalAborted3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx err end");
    }

    @Test
    public void testOptionalAborted4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx inCmt end");
    }

    @Test
    public void testOptionalCommitted1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx end");
    }

    @Test
    public void testOptionalCommitted2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort inAbt end");
    }

    @Test
    public void testOptionalCommittedStmt3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx inAbt err end");
    }

    @Test
    public void testOptionalCommitted4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx end");
    }

    @Test
    public void testNestedTransaction1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");
    }

    @Test
    public void testNestedTransaction2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx abort innerAborted endOuterTrx inOuterCmt  end");
    }

    @Test
    public void testNestedTransaction3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
              "start inOuterTrx inInnerTrx inInnerTrx inInnerTrx innerAbortedinOuterTrx inInnerTrx inInnerTrx "
              + "inInnerTrx innerAbortedinOuterTrx inInnerTrx inInnerTrx inInnerTrx innerAborted outerAborted err end");

    }

    @Test
    public void testNestedTransaction4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx trxErr endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");

    }

    @Test
    public void testNestedTransactionWithFailed1() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransactionWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inOuterTrx inInnerTrx innerFailed inInnerTrx innerFailed innerAborted outerFailed inOuterTrx "
                     + "inInnerTrx innerFailed inInnerTrx innerFailed innerAborted outerFailed inOuterTrx "
                     + "inInnerTrx innerFailed inInnerTrx innerFailed innerAborted outerFailed outerAborted err end");

    }

    @Test
    public void testNestedTransactionWithFailed2() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BRunUtil.invoke(programFile, "testNestedTransactionWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inOuterTrx inInnerTrx trxErr endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");

    }

    @Test
    public void testTransactionStmtWithFailed1() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inTrx inFailed inTrx inFailed inTrx "
                + "inFailed inAbt err end");
    }

    @Test
    public void testTransactionStmtWithFailed2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort inAbt end");
    }

    @Test
    public void testTransactionStmtWithFailed3() {
        BValue[] args = {new BInteger(-2)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx inCmt end");
    }

    @Test
    public void testTransactionStmtWithFailed4() {
        BValue[] args = {new BInteger(1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx success endTrx inCmt end");
    }

    @Test
    public void testTransactionStmtWithRetryOff() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithRetryOff", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inAbt err end");
    }

    @Test
    public void testTransactionStmtWithoutFailed() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtWithoutFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx err end");
    }

    @Test
    public void testTransactionStmtConstRetry() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtConstRetry");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inTrx inFailed inTrx inFailed inTrx "
                + "inFailed err end");
    }

    @Test
    public void testTransactionStmtSuccess() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionStmtSuccess");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inCmt end");
    }

    @Test
    public void testMultipleTransactionStmtSuccess() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionStmtSuccess");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inFirstTrxBlockBegin inFirstTrxBlockEnd inFirstCmt "
                + "inFirstTrxEnd inSecTrxBlockBegin inSecTrxBlockEnd inSecCmt inFSecTrxEnd end");
    }

    @Test
    public void testMultipleTransactionStmtError() {
        BValue[] returns = BRunUtil.invoke(programFile, "testMultipleTransactionStmtError");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inFirstTrxBlockBegin inFirstTrxBlockEnd inFirstTFld "
                + "inFirstTrxBlockBegin inFirstTrxBlockEnd inFirstTFld inFirstTAbt err end");
    }

    @Test()
    public void testValidAbortAndReturn() {
        BValue[] returns = BRunUtil.invoke(programFile, "test", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inTrx inAbt outAbt");

        returns = BRunUtil.invoke(programFile, "testReturn1", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inTrx com");

        returns = BRunUtil.invoke(programFile, "testReturn2", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inTrx abt");
    }

    @Test()
    public void testTransactionWithBreakValid() {
        BValue[] returns = BRunUtil.invoke(programFile, "transactionWithBreak1", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BRunUtil.invoke(programFile, "transactionWithBreak2", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BRunUtil.invoke(programFile, "transactionWithBreak3", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }

    @Test()
    public void testTransactionWithNextValid() {
        BValue[] returns = BRunUtil.invoke(programFile, "transactionWithNext1", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BRunUtil.invoke(programFile, "transactionWithNext2", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BRunUtil.invoke(programFile, "transactionWithNext3", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }

    @Test(description = "Test transaction statement with errors")
    public void testTransactionNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 16);
        BAssertUtil.validateError(resultNegative, 0, "incompatible types: expected 'int', found 'float'", 19, 15);
        BAssertUtil.validateError(resultNegative, 1, "invalid transaction retry count", 19, 15);
        BAssertUtil.validateError(resultNegative, 2,
                "invalid retry statement position, should be a root level statement within failed block", 7, 9);
        BAssertUtil.validateError(resultNegative, 3, "abort cannot be used outside of a transaction block", 33, 9);
        BAssertUtil.validateError(resultNegative, 4, "abort cannot be used outside of a transaction block", 43, 9);
        BAssertUtil.validateError(resultNegative, 5, "abort cannot be used outside of a transaction block", 49, 5);
        BAssertUtil.validateError(resultNegative, 6, "unreachable code", 58, 9);
        BAssertUtil.validateError(resultNegative, 7, "unreachable code", 70, 9);
        BAssertUtil.validateError(resultNegative, 8, "unreachable code", 89, 17);
        BAssertUtil.validateError(resultNegative, 9, "unreachable code", 93, 9);
        BAssertUtil.validateError(resultNegative, 10,
                "invalid retry statement position, should be a root level statement within failed block", 106, 13);
        BAssertUtil.validateError(resultNegative, 11,
                "invalid retry statement position, should be a root level statement within failed block", 123, 13);
        BAssertUtil.validateError(resultNegative, 12,
                "invalid retry statement position, should be a root level statement within failed block", 140, 13);
        BAssertUtil.validateError(resultNegative, 13,
                "invalid retry statement position, should be a root level statement within failed block", 155, 13);
        BAssertUtil.validateError(resultNegative, 14,
                "invalid retry statement position, should be a root level statement within failed block", 174, 13);
        BAssertUtil.validateError(resultNegative, 15,
                "invalid retry statement position, should be a root level statement within failed block", 191, 13);
    }

    @Test(description = "Test transaction statement with errors")
    public void testRetryNegativeCases() {
        CompileResult res = BCompileUtil.compile("test-src/statements/transaction/transaction-retry-negative.bal");
        Assert.assertEquals(res.getErrorCount(), 7);
        BAssertUtil.validateError(res, 0, "invalid transaction retry count", 7, 15);
        BAssertUtil.validateError(res, 1, "invalid transaction retry count", 23, 15);
        BAssertUtil.validateError(res, 2,
                "invalid retry statement position, should be a root level statement within failed block", 42, 13);
        BAssertUtil.validateError(res, 3,
                "invalid retry statement position, should be a root level statement within failed block", 44, 13);
        BAssertUtil.validateError(res, 4,
                "invalid retry statement position, should be a root level statement within failed block", 62, 13);
        BAssertUtil.validateError(res, 5, "break statement cannot be used to exit from a transaction", 78, 17);
        BAssertUtil.validateError(res, 6, "next statement cannot be used to exit from a transaction", 91, 17);
    }
}
